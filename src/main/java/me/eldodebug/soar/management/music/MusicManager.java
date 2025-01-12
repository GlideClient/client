package me.eldodebug.soar.management.music;

import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlayingContext;
import com.wrapper.spotify.model_objects.miscellaneous.Device;
import com.wrapper.spotify.model_objects.specification.PlaylistSimplified;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import com.wrapper.spotify.requests.data.player.*;
import com.wrapper.spotify.requests.data.player.StartResumeUsersPlaybackRequest;
import com.wrapper.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistsItemsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;
import me.eldodebug.soar.Glide;
import me.eldodebug.soar.logger.GlideLogger;
import me.eldodebug.soar.management.file.FileManager;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.notification.NotificationType;
import org.apache.hc.core5.http.ParseException;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MusicManager implements AutoCloseable {

    private static final String CLIENT_ID = "dbc43579edab42cc8b3074755537f58d";
    private static final String CLIENT_SECRET = "dde4b5363919441ea79bcb27dce4d449";
    private static final URI REDIRECT_URI = SpotifyHttpManager.makeUri("http://localhost:8888/callback");
    private static final String TOKEN_FILE_NAME = "spotify_tokens.properties";
    private static final int SEARCH_LIMIT = 20;
    private static final int PLAYLIST_LIMIT = 50;
    private static final int API_TIMEOUT_SECONDS = 5;

    private final SpotifyApi spotifyApi;
    private final FileManager fileManager;
    private final AlbumArtCache albumArtCache;
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private HttpServer server;
    private boolean isAuthorized = false;
    private Track currentTrack;
    private boolean isPlaying = false;
    private int currentVolume = 100;
    private long trackPosition = 0;
    private long trackDuration = 0;
    private ScheduledExecutorService tokenRefreshScheduler = Executors.newSingleThreadScheduledExecutor();
    private static final long PLAYBACK_UPDATE_INTERVAL = 1000; // Reduced from 5000ms to 1000ms
    private static final int BATCH_SIZE = 20;
    private static final long THROTTLE_DELAY = 50; // 50ms between requests

    private static final class SimpleRateLimiter {
        private final long minTimeBetweenRequests;
        private long lastRequestTime;
        
        SimpleRateLimiter(double requestsPerSecond) {
            this.minTimeBetweenRequests = (long)(1000.0 / requestsPerSecond);
            this.lastRequestTime = 0;
        }
        
        synchronized boolean tryAcquire() {
            long now = System.currentTimeMillis();
            if (now - lastRequestTime >= minTimeBetweenRequests) {
                lastRequestTime = now;
                return true;
            }
            return false;
        }
    }
    
    // Replace the Guava RateLimiter with our custom implementation
    private final SimpleRateLimiter rateLimiter = new SimpleRateLimiter(20.0); // 20 requests per second max
    private final Map<String, Long> lastRequestTime = new ConcurrentHashMap<>();

    // Cache for avoiding duplicate API calls
    private final Map<String, CompletableFuture<List<Track>>> searchCache = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<List<PlaylistSimplified>>> playlistCache = new ConcurrentHashMap<>();

    public MusicManager(FileManager fileManager) {
        this.fileManager = fileManager;
        this.spotifyApi = new SpotifyApi.Builder()
                .setClientId(CLIENT_ID)
                .setClientSecret(CLIENT_SECRET)
                .setRedirectUri(REDIRECT_URI)
                .build();

        this.albumArtCache = new AlbumArtCache(fileManager);

        // Initialize schedulers
        initializeSchedulers();

        loadTokens();
        if (spotifyApi.getAccessToken() != null) {
            isAuthorized = true;
        } else {
            try {
                startServer();
            } catch (IOException e) {
                GlideLogger.error("Failed to start local server for Spotify authentication", e);
                Glide.getInstance().getNotificationManager().post(TranslateText.SPOTIFY_AUTH, TranslateText.valueOf("Failed to start local server"), NotificationType.ERROR);
            }
        }
        startPlaybackStateUpdater();
        scheduleTokenRefresh();

        // Add a shutdown hook for cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(this::cleanup));
    }

    private void initializeSchedulers() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        this.tokenRefreshScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
    }

    private void loadTokens() {
        File tokenFile = new File(fileManager.getMusicDir(), TOKEN_FILE_NAME);
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(tokenFile)) {
            props.load(in);
            String accessToken = props.getProperty("accessToken");
            String refreshToken = props.getProperty("refreshToken");
            if (accessToken != null && refreshToken != null) {
                spotifyApi.setAccessToken(accessToken);
                spotifyApi.setRefreshToken(refreshToken);
                // Refresh the access token to ensure it's valid
                refreshAccessToken();            }
        } catch (IOException e) {
            GlideLogger.warn("Failed to load tokens: " + e.getMessage());
        }
    }

    private void saveTokens() {
        File tokenFile = new File(fileManager.getMusicDir(), TOKEN_FILE_NAME);
        Properties props = new Properties();
        props.setProperty("accessToken", spotifyApi.getAccessToken());
        props.setProperty("refreshToken", spotifyApi.getRefreshToken());
        try (FileOutputStream out = new FileOutputStream(tokenFile)) {
            props.store(out, "Spotify Tokens");
        } catch (IOException e) {
            GlideLogger.error("Failed to save tokens", e);
            Glide.getInstance().getNotificationManager().post(TranslateText.SPOTIFY_AUTH, TranslateText.valueOf("Failed to save tokens"), NotificationType.ERROR);
        }
    }

    private void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(8888), 0);
        server.createContext("/callback", new SpotifyCallbackHandler());
        server.setExecutor(Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }));
        server.start();
    }

    private class SpotifyCallbackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String response = "Authorization successful! You can close this window now.";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();

            if (query != null && query.startsWith("code=")) {
                String authorizationCode = query.substring(5);
                CompletableFuture.runAsync(() -> requestAccessToken(authorizationCode));
            } else {
                GlideLogger.warn("Received callback without authorization code");
            }
        }
    }

    public String getAuthorizationCodeUri() {
        AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
                .scope("user-read-private user-read-email user-modify-playback-state user-read-playback-state")
                .show_dialog(true)
                .build();
        final URI uri = authorizationCodeUriRequest.execute();
        return uri.toString();
    }

    private void requestAccessToken(String code) {
        try {
            AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(code).build();
            final AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();

            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());

            isAuthorized = true;
            saveTokens(); // Save tokens after successful authorization

            Glide.getInstance().getNotificationManager().post(TranslateText.SPOTIFY_AUTH, TranslateText.SPOTIFY_AUTH_TOKEN_RECEIVED, NotificationType.SUCCESS);
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            GlideLogger.error("Failed to request access token", e);
            Glide.getInstance().getNotificationManager().post(TranslateText.SPOTIFY_AUTH, TranslateText.SPOTIFY_AUTH_FAILED, NotificationType.ERROR);
        }
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }

    // Replace search method with cached version
    public CompletableFuture<List<Track>> searchTracks(String query) {
        return searchCache.computeIfAbsent(query, q -> 
            throttleRequest("search", () -> CompletableFuture.supplyAsync(() -> {
                try {
                    final SearchTracksRequest request = spotifyApi.searchTracks(q)
                            .limit(SEARCH_LIMIT)
                            .build();
                    List<Track> tracks = Arrays.asList(request.execute().getItems());
                    
                    // Batch process album art prefetching
                    CompletableFuture.runAsync(() -> {
                        for (int i = 0; i < tracks.size(); i += BATCH_SIZE) {
                            int end = Math.min(i + BATCH_SIZE, tracks.size());
                            List<Track> batch = tracks.subList(i, end);
                            batch.forEach(this::prefetchAlbumArt);
                            try {
                                Thread.sleep(THROTTLE_DELAY);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    });
                    
                    return tracks;
                } catch (Exception e) {
                    GlideLogger.error("Search failed", e);
                    throw new CompletionException(e);
                } finally {
                    searchCache.remove(query);
                }
            }))
        );
    }

    private void prefetchAlbumArt(Track track) {
        if (track != null && track.getAlbum() != null && 
            track.getAlbum().getImages() != null && 
            track.getAlbum().getImages().length > 0) {
            String imageUrl = track.getAlbum().getImages()[0].getUrl();
            albumArtCache.getCachedAlbumArtUrlAsync(track.getId(), imageUrl)
                .exceptionally(ex -> {
                    GlideLogger.warn("Failed to prefetch album art: " + ex.getMessage());
                    return null;
                });
        }
    }

    public CompletableFuture<Void> addToQueue(String trackUri) {
        return CompletableFuture.runAsync(() -> {
            try {
                String deviceId = getActiveDeviceId();
                if (deviceId == null) {
                    throw new IllegalStateException("No active device found");
                }

                AddItemToUsersPlaybackQueueRequest addItemRequest = spotifyApi.addItemToUsersPlaybackQueue(trackUri)
                        .device_id(deviceId)
                        .build();
                addItemRequest.execute();

                fetchCurrentPlaybackState();
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                GlideLogger.error("Failed to add track to queue", e);
                throw new CompletionException(e);
            }
        });
    }

    private void cacheSearchResultsAlbumArt(List<Track> tracks) {
        for (Track track : tracks) {
            getAlbumArtUrl(track); // This will cache the album art
        }
    }

    // Optimize playback control methods by batching state updates
    public void play(String trackUri) {
        CompletableFuture.runAsync(() -> {
            try {
                String deviceId = getActiveDeviceId();
                fetchCurrentPlaybackState();
                if (deviceId == null) {
                    Glide.getInstance().getNotificationManager().post(TranslateText.SPOTIFY_PLAYBACK, TranslateText.SPOTIFY_NO_ACTIVE_DEVICE, NotificationType.ERROR);
                    return;
                }

                JsonParser parser = new JsonParser();
                final StartResumeUsersPlaybackRequest playbackRequest = spotifyApi.startResumeUsersPlayback()
                        .device_id(deviceId)
                        .uris(JsonParser.parseString("[\"" + trackUri + "\"]").getAsJsonArray())
                        .build();
                playbackRequest.execute();
                MusicManager musicManager = Glide.getInstance().getMusicManager();
                isPlaying = true;
                updatePlaybackState();
                Glide.getInstance().getNotificationManager().post(TranslateText.SPOTIFY_PLAYBACK, TranslateText.SPOTIFY_PLAYBACK_STARTED, NotificationType.SUCCESS);
            } catch (Exception e) {
                handleSpotifyException("start playback", e);
            }
        });
    }

    public void pause() {
        if (!isPlaying) return;
        CompletableFuture.runAsync(() -> {
            try {
                final PauseUsersPlaybackRequest pauseRequest = spotifyApi.pauseUsersPlayback().build();
                fetchCurrentPlaybackState();
                pauseRequest.execute();
                isPlaying = false;
            } catch (Exception e) {
                handleSpotifyException("pause playback", e);
            }
        });
    }

    public void resume() {
        if (isPlaying) return;
        CompletableFuture.runAsync(() -> {
            try {
                final StartResumeUsersPlaybackRequest resumeRequest = spotifyApi.startResumeUsersPlayback().build();
                fetchCurrentPlaybackState();
                resumeRequest.execute();
                isPlaying = true;
            } catch (Exception e) {
                handleSpotifyException("resume playback", e);
            }
        });
    }

    public void setVolume(int volumePercent) {
        if (volumePercent == currentVolume) return;
        CompletableFuture.runAsync(() -> {
            try {
                spotifyApi.setVolumeForUsersPlayback(volumePercent).build().execute();
                currentVolume = volumePercent;
            } catch (Exception e) {
                handleSpotifyException("set volume", e);
            }
        });
    }

    public void nextTrack() {
        CompletableFuture.runAsync(() -> {
            try {
                spotifyApi.skipUsersPlaybackToNextTrack().build().execute();
                fetchCurrentPlaybackState();
                updatePlaybackState();
            } catch (Exception e) {
                handleSpotifyException("skip to next track", e);
            }
        });
    }

    public void previousTrack() {
        CompletableFuture.runAsync(() -> {
            try {
                spotifyApi.skipUsersPlaybackToPreviousTrack().build().execute();
                fetchCurrentPlaybackState();
                updatePlaybackState();
            } catch (Exception e) {
                handleSpotifyException("skip to previous track", e);
            }
        });
    }

    public void seekToPosition(long positionMs) {
        CompletableFuture.runAsync(() -> {
            try {
                spotifyApi.seekToPositionInCurrentlyPlayingTrack((int) positionMs).build().execute();
                trackPosition = positionMs;
                notifyTrackInfoUpdated();
            } catch (Exception e) {
                handleSpotifyException("seek to position", e);
            }
        });
    }

    private void updatePlaybackState() {
        try {
            CurrentlyPlaying currentlyPlaying = spotifyApi.getUsersCurrentlyPlayingTrack().build().execute();
            if (currentlyPlaying != null && currentlyPlaying.getItem() != null) {
                currentTrack = (Track) currentlyPlaying.getItem();
                isPlaying = currentlyPlaying.getIs_playing();
                trackPosition = currentlyPlaying.getProgress_ms();
                trackDuration = currentTrack.getDurationMs();
                notifyTrackInfoUpdated();
            } } catch (Exception e) {
            GlideLogger.error("Error updating playback state", e);
        }
    }

    private void handleSpotifyException(String action, Exception e) {
        String errorMessage = "Failed to " + action + ": " + e.getMessage();
        GlideLogger.error(errorMessage, e);
        Glide.getInstance().getNotificationManager().post(
                TranslateText.SPOTIFY_PLAYBACK,
                TranslateText.valueOf(errorMessage),
                NotificationType.ERROR
        );
    }

    private String getActiveDeviceId() {
        try {
            final Device[] devices = spotifyApi.getUsersAvailableDevices().build().execute();
            for (Device device : devices) {
                if (device.getIs_active()) {
                    return device.getId();
                }
            }
            GlideLogger.warn("No active device found");
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            GlideLogger.error("Failed to get active device", e);
        }
        return null;
    }

    public String getAlbumArtUrl(Track track) {
        if (track == null || track.getAlbum() == null || 
            track.getAlbum().getImages() == null || 
            track.getAlbum().getImages().length == 0) {
            return null;
        }

        String imageUrl = track.getAlbum().getImages()[0].getUrl();
        try {
            return albumArtCache.getCachedAlbumArtUrlAsync(track.getId(), imageUrl)
                   .get(100, TimeUnit.MILLISECONDS); // Short timeout to prevent UI blocking
        } catch (Exception e) {
            GlideLogger.warn("Failed to get album art, falling back to URL: " + e.getMessage());
            return imageUrl;
        }
    }

    // Remove the async version if not needed elsewhere
    private CompletableFuture<String> getAlbumArtUrlAsync(Track track) {
        if (track == null || track.getAlbum() == null || 
            track.getAlbum().getImages() == null || 
            track.getAlbum().getImages().length == 0) {
            return CompletableFuture.completedFuture(null);
        }

        String imageUrl = track.getAlbum().getImages()[0].getUrl();
        return albumArtCache.getCachedAlbumArtUrlAsync(track.getId(), imageUrl);
    }

    private String getTrackId(String trackId) {
        return trackId;
    }


    private void fetchCurrentPlaybackState() {
        try {
            CurrentlyPlayingContext playbackState = spotifyApi.getInformationAboutUsersCurrentPlayback().build().execute();
            if (playbackState != null) {
                isPlaying = playbackState.getIs_playing();
                trackPosition = playbackState.getProgress_ms();

                if (playbackState.getItem() != null && playbackState.getItem() instanceof Track) {
                    Track newTrack = (Track) playbackState.getItem();
                    if (currentTrack == null || !currentTrack.getId().equals(newTrack.getId())) {
                        currentTrack = newTrack;
                        trackDuration = currentTrack.getDurationMs();
                    }
                }

                notifyTrackInfoUpdated();
            }
        } catch (Exception e) {
            GlideLogger.error("Error fetching playback state", e);
        }
    }

    public Track getCurrentTrack() {
        return currentTrack;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    private void startPlaybackStateUpdater() {
        scheduler.scheduleAtFixedRate(() -> {
            if (rateLimiter.tryAcquire()) {
                fetchCurrentPlaybackState();
            }
        }, 0, PLAYBACK_UPDATE_INTERVAL, TimeUnit.MILLISECONDS);
    }

    private TrackInfoCallback trackInfoCallback;

    public interface TrackInfoCallback {
        void onTrackInfoUpdated(long position, long duration);
    }

    public void setTrackInfoCallback(TrackInfoCallback callback) {
        this.trackInfoCallback = callback;
    }

    private void notifyTrackInfoUpdated() {
        if (trackInfoCallback != null) {
            trackInfoCallback.onTrackInfoUpdated(trackPosition, trackDuration);
        }
    }

    public void refreshAccessToken() {
        try {
            final AuthorizationCodeCredentials authorizationCodeCredentials = spotifyApi.authorizationCodeRefresh()
                    .build().execute();

            // Update the access token
            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());

            // Optionally update the refresh token if a new one is provided
            if (authorizationCodeCredentials.getRefreshToken() != null) {
                spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
            }

            saveTokens(); // Save the updated tokens
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            GlideLogger.error("Failed to refresh access token automatically", e);
            // If refresh fails, we might need to re-authenticate
            isAuthorized = false;
            Glide.getInstance().getNotificationManager().post(TranslateText.SPOTIFY_AUTH, TranslateText.SPOTIFY_AUTH_REFRESH_FAILED, NotificationType.ERROR);
        }
    }

    private void scheduleTokenRefresh() {
        // Refresh token 5 minutes before it expires
        long refreshInterval = 3600 - 300; // Assuming token expires in 1 hour (3600 seconds)
        tokenRefreshScheduler.scheduleAtFixedRate(this::refreshAccessToken, refreshInterval, refreshInterval, TimeUnit.SECONDS);
    }

    public float getCurrentTime() {
        return (float) trackPosition / 1000;
    }

    public float getEndTime() {
        return (float) trackDuration / 1000;
    }

    public void shutdown() {
        if (server != null) {
            server.stop(0);
            GlideLogger.info("Server stopped");
        }

        scheduler.shutdownNow();
        tokenRefreshScheduler.shutdownNow();
        saveTokens();
    }

    // Add cleanup method for caches
    public void cleanup() {
        searchCache.clear();
        playlistCache.clear();
        albumArtCache.cleanup();
        if (server != null) {
            server.stop(0);
        }
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        if (tokenRefreshScheduler != null) {
            tokenRefreshScheduler.shutdownNow();
        }
        saveTokens();
    }

    @Override
    public void close() {
        cleanup();
    }

    // Replace playlist fetch with cached version
    public CompletableFuture<List<PlaylistSimplified>> getUserPlaylists() {
        String cacheKey = "userPlaylists";
        return playlistCache.computeIfAbsent(cacheKey, k -> 
            throttleRequest("playlists", () -> CompletableFuture.supplyAsync(() -> {
                try {
                    List<PlaylistSimplified> allPlaylists = new ArrayList<>();
                    int offset = 0;
                    boolean hasMore = true;

                    while (hasMore && offset < 200) { // Limit to 200 playlists max
                        GetListOfCurrentUsersPlaylistsRequest request = spotifyApi.getListOfCurrentUsersPlaylists()
                                .limit(PLAYLIST_LIMIT)
                                .offset(offset)
                                .build();
                        
                        PlaylistSimplified[] batch = request.execute().getItems();
                        if (batch.length == 0) {
                            hasMore = false;
                        } else {
                            allPlaylists.addAll(Arrays.asList(batch));
                            offset += batch.length;
                            Thread.sleep(THROTTLE_DELAY);
                        }
                    }
                    
                    // Prefetch playlist images in background
                    CompletableFuture.runAsync(() -> 
                        prefetchPlaylistImages(allPlaylists));
                    
                    return allPlaylists;
                } catch (Exception e) {
                    GlideLogger.error("Failed to fetch playlists", e);
                    return Collections.emptyList();
                } finally {
                    playlistCache.remove(cacheKey);
                }
            }))
        );
    }

    private void prefetchPlaylistImages(List<PlaylistSimplified> playlists) {
        try {
            for (int i = 0; i < playlists.size(); i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, playlists.size());
                List<PlaylistSimplified> batch = playlists.subList(i, end);
                
                batch.parallelStream()
                     .filter(p -> p != null && p.getImages() != null && p.getImages().length > 0)
                     .forEach(p -> getPlaylistImageUrl(p));
                
                Thread.sleep(THROTTLE_DELAY);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private <T> CompletableFuture<T> throttleRequest(String key, Supplier<CompletableFuture<T>> request) {
        return CompletableFuture.supplyAsync(() -> {
            long lastTime = lastRequestTime.getOrDefault(key, 0L);
            long now = System.currentTimeMillis();
            long timeSinceLastRequest = now - lastTime;
            
            if (timeSinceLastRequest < THROTTLE_DELAY) {
                try {
                    Thread.sleep(THROTTLE_DELAY - timeSinceLastRequest);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
            lastRequestTime.put(key, System.currentTimeMillis());
            return request.get().join();
        });
    }

    public void playPlaylist(String playlistUri) {
        CompletableFuture.runAsync(() -> {
            try {
                StartResumeUsersPlaybackRequest request = spotifyApi.startResumeUsersPlayback()
                        .context_uri(playlistUri)
                        .build();
                request.execute();
            } catch (Exception e) {
                GlideLogger.error("Failed to play playlist", e);
                handleSpotifyException("play playlist", e);
            }
        });
    }

    public String getPlaylistImageUrl(PlaylistSimplified playlist) {
        if (playlist != null && playlist.getImages() != null && playlist.getImages().length > 0) {
            String imageUrl = playlist.getImages()[0].getUrl();
            return albumArtCache.getAlbumArt(imageUrl);
        }
        return null;
    }
}