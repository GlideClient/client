package me.eldodebug.soar.gui.modmenu.category.impl;

import com.wrapper.spotify.model_objects.specification.PlaylistSimplified;
import com.wrapper.spotify.model_objects.specification.Track;
import me.eldodebug.soar.Glide;
import me.eldodebug.soar.gui.modmenu.GuiModMenu;
import me.eldodebug.soar.gui.modmenu.category.Category;
import me.eldodebug.soar.logger.GlideLogger;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.color.ColorManager;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.impl.InternalSettingsMod;
import me.eldodebug.soar.management.music.MusicManager;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.Icon;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import me.eldodebug.soar.management.notification.NotificationType;
import me.eldodebug.soar.ui.comp.impl.CompSlider;
import me.eldodebug.soar.ui.comp.impl.field.CompTextBox;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class SpotifyCategory extends Category implements MusicManager.TrackInfoCallback {
    private static final long VOLUME_CHANGE_DELAY = 500;
    private static final long SEARCH_DEBOUNCE_DELAY = 1300; // Increased from 300ms to 800ms
    private static final ResourceLocation PLACEHOLDER_IMAGE = new ResourceLocation("soar/music.png");

    private static final boolean DEBUG_HITBOXES = false; // Set to true to show hitboxes
    private static final Color DEBUG_COLOR = new Color(255, 0, 0, 100);

    private final CompSlider volumeSlider;
    private final CompTextBox textBox;
    private final WeakReference<GuiModMenu> parentRef;
    private final ScheduledExecutorService searchDebouncer;
    private ScheduledFuture<?> pendingSearch; // Add a reference to track scheduled search

    private volatile List<Track> searchResults;
    private volatile List<PlaylistSimplified> userPlaylists;
    private boolean openDownloader;
    private long trackPosition;
    private long trackDuration;
    private long lastVolumeChangeTime;
    private String lastSearchQuery = "";
    private String currentTrackId;
    private final AtomicBoolean isSearching = new AtomicBoolean(false);
    private boolean showConnectButton = true;

    public SpotifyCategory(GuiModMenu parent) {
        super(parent, TranslateText.MUSIC, LegacyIcon.MUSIC, true, true);
        this.parentRef = new WeakReference<>(parent);
        this.volumeSlider = new CompSlider(InternalSettingsMod.getInstance().getVolumeSetting());
        this.textBox = new CompTextBox();

        this.searchDebouncer = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Search-Debouncer");t.setDaemon(true);
            return t;
        });


        initializeComponents();
    }

    private void initializeComponents() {
        textBox.setDefaultText("Enter a Spotify link");
        volumeSlider.setCircle(false);
        volumeSlider.setShowValue(false);
    }

    @Override
    public void initGui() {
        // Components already initialized in constructor
    }

    @Override
    public void initCategory() {
        scroll.resetAll();
        MusicManager musicManager = Glide.getInstance().getMusicManager();
        if (!musicManager.isAuthorized()) {
            showConnectButton = true;
        } else {
            showConnectButton = false;
        }
        musicManager.setTrackInfoCallback(this);
        fetchUserPlaylists();
    }

    private void fetchUserPlaylists() {
        Glide.getInstance().getMusicManager().getUserPlaylists()
            .thenAccept(playlists -> this.userPlaylists = playlists)
            .exceptionally(ex -> {
                GlideLogger.error("Failed to fetch user playlists: " + ex.getMessage());
                return null;
            });
    }

    private void openConfirmDialog(final String uri) {
        Minecraft mc = Minecraft.getMinecraft();
        GuiConfirmOpenLink gui = new GuiConfirmOpenLink((result, id) -> {
            if (result) {
                tryOpenBrowser(uri);
            }
            mc.displayGuiScreen(parentRef.get());
        }, uri, 0, true);
        gui.disableSecurityWarning();
        mc.displayGuiScreen(gui);
    }

    private void tryOpenBrowser(String uri) {
        try {
            Class<?> desktopClass = Class.forName("java.awt.Desktop");
            Object desktop = desktopClass.getMethod("getDesktop").invoke(null);
            desktopClass.getMethod("browse", URI.class).invoke(desktop, new URI(uri));
        } catch (Exception e) {
            Glide.getInstance().getNotificationManager().post(
                    TranslateText.SPOTIFY_AUTH,
                    TranslateText.SPOTIFY_FAIL_BROWSER,
                    NotificationType.ERROR
            );
            GlideLogger.error(String.valueOf(e));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Glide instance = Glide.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        ColorManager colorManager = instance.getColorManager();
        ColorPalette palette = colorManager.getPalette();
        AccentColor accentColor = colorManager.getCurrentColor();
        MusicManager musicManager = instance.getMusicManager();

        if (!isSearching.get()) {
            checkAndUpdateSearch();
        }

        if (showConnectButton) {
            drawConnectButton(nvg, mouseX, mouseY);
        } else {
            nvg.save();
            try {
                nvg.save();
                nvg.translate(0, scroll.getValue());
                drawSearchResults(nvg, palette);
                drawUserPlaylists(nvg, palette);
                nvg.restore();

                drawControlBar(nvg, palette, musicManager);
                drawPlaybackControls(nvg, palette, musicManager);
                drawVolumeSlider(nvg, palette, mouseX, mouseY, partialTicks);
                drawProgressBar(nvg, accentColor, palette);
            } finally {
                nvg.restore();
            }
        }

        updateScroll();
    }

    private void drawSearchResults(NanoVGManager nvg, ColorPalette palette) {
        if (searchResults == null) return;

        float offsetY = 13;
        for (Track track : searchResults) {
            if (offsetY + 46 >= -scroll.getValue() && offsetY <= -scroll.getValue() + getHeight()) {
                drawTrackEntry(nvg, palette, track, offsetY);
            }
            offsetY += 56;
        }
    }

    private void drawTrackEntry(NanoVGManager nvg, ColorPalette palette, Track track, float offsetY) {
        nvg.drawRoundedRect(
                this.getX() + 15,
                this.getY() + offsetY,
                this.getWidth() - 30,
                46,
                8,
                palette.getBackgroundColor(ColorType.DARK)
        );

        drawTrackImage(nvg, track, offsetY);
        drawTrackInfo(nvg, palette, track, offsetY);

        if (DEBUG_HITBOXES) {
            // Track entry hitbox
            nvg.drawRect(
                this.getX() + 15,
                this.getY() + offsetY,
                this.getWidth() - 30,
                46,
                DEBUG_COLOR
            );
            
            // Album art hitbox
            nvg.drawRect(
                this.getX() + 20,
                this.getY() + offsetY + 5,
                36,
                36,
                DEBUG_COLOR
            );
            
            // Add to queue button hitbox
            nvg.drawRect(
                this.getX() + this.getWidth() - 60,
                this.getY() + offsetY + 15,
                16,
                16,
                DEBUG_COLOR
            );
        }
    }

    private void drawTrackImage(NanoVGManager nvg, Track track, float offsetY) {
        if (track == null) {
            drawPlaceholderImage(nvg, offsetY);
            return;
        }

        String albumArtUrl = Glide.getInstance().getMusicManager().getAlbumArtUrl(track);
        if (albumArtUrl != null && new File(albumArtUrl).exists()) {
            nvg.drawRoundedImage(
                new File(albumArtUrl),
                this.getX() + 20,
                this.getY() + offsetY + 5,
                36,
                36,
                6
            );
        } else {
            drawPlaceholderImage(nvg, offsetY);
        }
    }

    private void drawTrackInfo(NanoVGManager nvg, ColorPalette palette, Track track, float offsetY) {
        String trackName = nvg.getLimitText(track.getName(), 11, Fonts.MEDIUM, 280);
        nvg.drawText(
                trackName,
                this.getX() + 63,
                this.getY() + offsetY + 9,
                palette.getFontColor(ColorType.DARK),
                11,
                Fonts.MEDIUM
        );

        nvg.drawText(
                track.getArtists()[0].getName(),
                this.getX() + 63,
                this.getY() + offsetY + 25,
                palette.getFontColor(ColorType.NORMAL),
                9,
                Fonts.MEDIUM
        );

        nvg.drawText(
                LegacyIcon.PLUS_SQUARE,
                this.getX() + this.getWidth() - 60,
                this.getY() + offsetY + 15,
                palette.getFontColor(ColorType.NORMAL),
                16,
                Fonts.LEGACYICON
        );
    }

    private void drawPlaceholderImage(NanoVGManager nvg, float offsetY) {
        try {
            nvg.drawRoundedImage(
                    PLACEHOLDER_IMAGE,
                    this.getX() + 20,
                    this.getY() + offsetY,
                    36,
                    36,
                    6
            );
        } catch (Exception e) {
            nvg.drawRoundedRect(
                    this.getX() + 20,
                    this.getY() + offsetY,
                    36,
                    36,
                    6,
                    new Color(50, 50, 50)
            );
        }
    }

    private void drawUserPlaylists(NanoVGManager nvg, ColorPalette palette) {
        if (userPlaylists == null) return;  // Early return if no playlists

        float offsetY = 13 + (searchResults != null ? searchResults.size() * 56 : 0);
        for (PlaylistSimplified playlist : userPlaylists) {
            if (playlist == null) continue;  // Skip null playlists
            
            // Draw playlist entry background
            nvg.drawRoundedRect(
                this.getX() + 15,
                this.getY() + offsetY,
                this.getWidth() - 30,
                46,
                8,
                palette.getBackgroundColor(ColorType.DARK)
            );
            
            // Draw playlist image or placeholder
            String imageUrl = null;
            if (playlist.getImages() != null && playlist.getImages().length > 0) {
                imageUrl = Glide.getInstance().getMusicManager().getPlaylistImageUrl(playlist);
            }
            
            if (imageUrl != null) {
                File imageFile = new File(imageUrl);
                if (imageFile.exists()) {
                    nvg.drawRoundedImage(
                        imageFile,
                        this.getX() + 20,
                        this.getY() + offsetY + 5,
                        36,
                        36,
                        6
                    );
                } else {
                    drawPlaceholderImage(nvg, offsetY + 5);
                }
            } else {
                drawPlaceholderImage(nvg, offsetY + 5);
            }

            // Draw playlist name (with null check)
            String playlistName = playlist.getName() != null ? playlist.getName() : "Untitled Playlist";
            nvg.drawText(
                nvg.getLimitText(playlistName, 11, Fonts.MEDIUM, 280),
                this.getX() + 63,
                this.getY() + offsetY + 9,
                palette.getFontColor(ColorType.DARK),
                11,
                Fonts.MEDIUM
            );

            // Draw playlist owner (with null checks)
            String ownerName = "Unknown Artist";
            if (playlist.getOwner() != null && playlist.getOwner().getDisplayName() != null) {
                ownerName = playlist.getOwner().getDisplayName();
            }
            
            nvg.drawText(
                ownerName,
                this.getX() + 63,
                this.getY() + offsetY + 25,
                palette.getFontColor(ColorType.NORMAL),
                9,
                Fonts.MEDIUM
            );

            if (DEBUG_HITBOXES) {
                nvg.drawRect(
                    this.getX() + 15,
                    this.getY() + offsetY,
                    this.getWidth() - 30,
                    46,
                    DEBUG_COLOR
                );
                
                nvg.drawRect(
                    this.getX() + 20,
                    this.getY() + offsetY + 5,
                    36,
                    36,
                    DEBUG_COLOR
                );
            }

            offsetY += 56;
        }
    }

    private void drawControlBar(NanoVGManager nvg, ColorPalette palette, MusicManager musicManager) {
        float centerX = this.getX() + ((float) this.getWidth() / 2);

        nvg.drawRoundedRectVarying(
                this.getX(),
                this.getY() + this.getHeight() - 46F,
                this.getWidth(),
                46,
                0,
                0,
                0,
                12,
                palette.getBackgroundColor(ColorType.DARK)
        );

        Track currentTrack = musicManager.getCurrentTrack();
        if (currentTrack != null) {
            String albumArtUrl = musicManager.getAlbumArtUrl(currentTrack);
            if (albumArtUrl != null) {
                File albumArtFile = new File(albumArtUrl);
                if (albumArtFile.exists()) {
                    nvg.drawRoundedImage(
                        albumArtFile,
                        this.getX() + 4,
                        this.getY() + this.getHeight() - 43,
                        36,
                        36,
                        6
                    );
                } else {
                    drawPlaceholderImage(nvg, this.getHeight() - 43F);
                }
            } else {
                drawPlaceholderImage(nvg, this.getHeight() - 43F);
            }

            nvg.drawText(
                    nvg.getLimitText(currentTrack.getName(), 9, Fonts.MEDIUM, 100),
                    this.getX() + 45,
                    this.getY() + this.getHeight() - 39, // Moved up 2 pixels
                    palette.getFontColor(ColorType.DARK),
                    9,
                    Fonts.MEDIUM
            );

            nvg.drawText(
                    nvg.getLimitText(currentTrack.getArtists()[0].getName(), 9, Fonts.MEDIUM, 100),
                    this.getX() + 45,
                    this.getY() + this.getHeight() - 27, // Moved up 2 pixels
                    palette.getFontColor(ColorType.NORMAL),
                    9,
                    Fonts.MEDIUM
            );

        } else {
            drawPlaceholderImage(nvg, this.getHeight() - 43F); // Moved up 2 pixels
            nvg.drawText(
                    TranslateText.NOTHING_IS_PLAYING.getText(),
                    this.getX() + 56,
                    this.getY() + this.getHeight() - 39, // Moved up 2 pixels
                    palette.getFontColor(ColorType.DARK),
                    9,
                    Fonts.MEDIUM
            );
        }
    }

    private void drawPlaybackControls(NanoVGManager nvg, ColorPalette palette, MusicManager musicManager) {
        float centerX = this.getX() + ((float) this.getWidth() / 2);
        float centerY = this.getY() + this.getHeight() - 32F;
        Color normalColor = palette.getFontColor(ColorType.NORMAL);

        // Draw the control icons
        nvg.drawText(
                LegacyIcon.BACK,
                centerX - 32,
                centerY,
                normalColor,
                16,
                Fonts.LEGACYICON
        );

        nvg.drawText(
                musicManager.isPlaying() ? LegacyIcon.PAUSE : LegacyIcon.PLAY,
                centerX - 8,
                centerY,
                normalColor,
                16,
                Fonts.LEGACYICON
        );

        nvg.drawText(
                LegacyIcon.FORWARD,
                centerX + 16,
                centerY,
                normalColor,
                16,
                Fonts.LEGACYICON
        );

        if (DEBUG_HITBOXES) {
            nvg.drawRect(
                centerX - 24 - 8,
                centerY,
                16, 16,
                DEBUG_COLOR
            );
            
            nvg.drawRect(
                centerX - 8,
                centerY,
                16, 16,
                DEBUG_COLOR
            );

            nvg.drawRect(
                centerX + 24 - 8,
                centerY,
                16, 16,
                DEBUG_COLOR
            );
        }
    }

    private void drawVolumeSlider(NanoVGManager nvg, ColorPalette palette, int mouseX, int mouseY, float partialTicks) {
        volumeSlider.setX(this.getX() + this.getWidth() - 72);
        volumeSlider.setY(this.getY() + this.getHeight() - 20);
        volumeSlider.setWidth(62);
        volumeSlider.setHeight(4.5f); // Add 'f' suffix to make it a float literal
        volumeSlider.draw(mouseX, mouseY, partialTicks);

        int volume = (int)(volumeSlider.getSetting().getValueFloat() * 100);
        String volumeIcon = getVolumeIcon(volume);
        nvg.drawText(
                volumeIcon,
                this.getX() + this.getWidth() - 94,
                this.getY() + this.getHeight() - 26,
                palette.getFontColor(ColorType.NORMAL),
                16,
                Fonts.LEGACYICON
        );
    }

    private String getVolumeIcon(int volume) {
        if (volume == 0) return LegacyIcon.VOLUME_X;
        if (volume > 80) return LegacyIcon.VOLUME_2;
        if (volume > 40) return LegacyIcon.VOLUME_1;
        return LegacyIcon.VOLUME;
    }

    private void drawProgressBar(NanoVGManager nvg, AccentColor accentColor, ColorPalette palette) {
        if (trackDuration <= 0) return;

        int progressBarWidth = this.getWidth() - 40;
        int progressBarY = this.getY() + this.getHeight() - 5;

        // Background bar
        nvg.drawRoundedRect(
                this.getX() + 20,
                progressBarY,
                progressBarWidth,
                2,
                1,
                palette.getBackgroundColor(ColorType.NORMAL)
        );

        // Progress bar
        float progress = (float) trackPosition / trackDuration;
        nvg.drawRoundedRect(
                this.getX() + 20,
                progressBarY,
                progressBarWidth * progress,
                2,
                1,
                accentColor.getInterpolateColor()
        );
    }

    private void checkAndUpdateSearch() {
        GuiModMenu parent = parentRef.get();
        if (parent == null) return;

        String currentSearchQuery = parent.getSearchBox().getText();
        if (!currentSearchQuery.equals(lastSearchQuery)) {
            scheduleSearch(currentSearchQuery);
            lastSearchQuery = currentSearchQuery;
        }
    }

    private void scheduleSearch(String query) {
        if (query.isEmpty()) {
            searchResults = null;
            return;
        }

        // Cancel any pending search before scheduling a new one
        if (pendingSearch != null && !pendingSearch.isDone()) {
            pendingSearch.cancel(false);
        }

        // Only set the atomic flag when actually performing the search,
        // not when scheduling it
        pendingSearch = searchDebouncer.schedule(() -> {
            if (isSearching.compareAndSet(false, true)) {
                try {
                    List<Track> results = Glide.getInstance().getMusicManager().searchTracks(query).join();
                    searchResults = results;
                    // Preload album art for visible tracks
                    if (results != null) {
                        int visibleTracks = Math.min(results.size(), 5);
                        for (int i = 0; i < visibleTracks; i++) {
                            Glide.getInstance().getMusicManager().getAlbumArtUrl(results.get(i)); // Preload album art
                        }
                    }
                } catch (Exception ex) {
                    GlideLogger.error("Search failed", ex);
                    Glide.getInstance().getNotificationManager().post(
                            TranslateText.MUSIC,
                            TranslateText.valueOf("Failed to search"),
                            NotificationType.ERROR
                    );
                } finally {
                    isSearching.set(false);
                }
            }
        }, SEARCH_DEBOUNCE_DELAY, TimeUnit.MILLISECONDS);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (openDownloader) {
            handleDownloaderClick(mouseX, mouseY, mouseButton);
            return;
        }

        boolean isInControlBar = mouseY >= this.getY() + this.getHeight() - 46;
        if (mouseButton == 0 && isInControlBar) {
            handleControlBarClick(mouseX, mouseY);
        } else if (!isInControlBar && searchResults != null) {
            handleTrackClick(mouseX, mouseY);
        } else if (!isInControlBar && userPlaylists != null) {
            handlePlaylistClick(mouseX, mouseY);
        }
    }

    private void handleDownloaderClick(int mouseX, int mouseY, int mouseButton) {
        textBox.mouseClicked(mouseX, mouseY, mouseButton);

        if (MouseUtils.isInside(mouseX, mouseY, this.getX() + this.getWidth() - 34, this.getY() + this.getHeight() - 80, 18, 18) && mouseButton == 0) {
            openDownloader = false;
            Glide.getInstance().getMusicManager().play(textBox.getText());
            return;
        }

        if (!MouseUtils.isInside(mouseX, mouseY, this.getX() + this.getWidth() - 175, this.getY() + this.getHeight() - 86, 165, 30)) {
            openDownloader = false;
        }
    }

    private void handleControlBarClick(int mouseX, int mouseY) {
        MusicManager musicManager = Glide.getInstance().getMusicManager();
        float centerX = this.getX() + ((float) this.getWidth() / 2);
        float centerY = this.getY() + this.getHeight() - 32F;

        // Previous track button - center hitbox on the icon
        if (MouseUtils.isInside(mouseX, mouseY, 
                centerX - 24 - 8, // icon position - half hitbox width
                centerY,      // icon position - half hitbox height
                16, 16)) {
            musicManager.previousTrack();
            return;
        }

        // Play/Pause button - center hitbox on the icon
        if (MouseUtils.isInside(mouseX, mouseY, 
                centerX - 8,  // icon position - half hitbox width
                centerY,      // icon position - half hitbox height
                16, 16)) {
            if (musicManager.isPlaying()) {
                musicManager.pause();
            } else {
                musicManager.resume();
            }
            return;
        }

        // Next track button - center hitbox on the icon
        if (MouseUtils.isInside(mouseX, mouseY, 
                centerX + 24 - 8, // icon position - half hitbox width
                centerY,      // icon position - half hitbox height
                16, 16)) {
            musicManager.nextTrack();
            return;
        }

        // Volume slider - update the value here too
        if (MouseUtils.isInside(mouseX, mouseY, this.getX() + this.getWidth() - 72, this.getY() + this.getHeight() - 20, 62, 4.5f)) {
            volumeSlider.mouseClicked(mouseX, mouseY, 0);
            return;
        }

        // Progress bar
        handleProgressBarClick(mouseX, mouseY, musicManager);
    }

    private void handleProgressBarClick(int mouseX, int mouseY, MusicManager musicManager) {
        int progressBarY = this.getY() + this.getHeight() - 5;
        if (MouseUtils.isInside(mouseX, mouseY, this.getX() + 20, progressBarY - 5, this.getWidth() - 40, 10)) {
            float clickPosition = (mouseX - (this.getX() + 20)) / (float)(this.getWidth() - 40);
            musicManager.seekToPosition((long)(clickPosition * trackDuration));
        }
    }

    private void handleTrackClick(int mouseX, int mouseY) {
        if (searchResults == null) return;

        float offsetY = 13 + scroll.getValue();
        for (Track track : searchResults) {
            if (MouseUtils.isInside(mouseX, mouseY, this.getX() + 15, this.getY() + offsetY, this.getWidth() - 30, 46)) {
                if (MouseUtils.isInside(mouseX, mouseY, this.getX() + this.getWidth() - 60, this.getY() + offsetY + 15, 16, 16)) {
                    addToQueue(track);
                } else {
                    Glide.getInstance().getMusicManager().play(track.getUri());
                }
                break;
            }
            offsetY += 56;
        }
    }

    private void handlePlaylistClick(int mouseX, int mouseY) {
        if (userPlaylists == null) return;
        MusicManager musicManager = Glide.getInstance().getMusicManager();
        if (musicManager == null) return;

        float offsetY = 13 + (searchResults != null ? searchResults.size() * 56 : 0) + scroll.getValue();
        for (PlaylistSimplified playlist : userPlaylists) {
            if (playlist == null || playlist.getUri() == null) continue;  // Skip invalid playlists
            
            if (MouseUtils.isInside(mouseX, mouseY, this.getX() + 15, this.getY() + offsetY, this.getWidth() - 30, 46)) {
                try {
                    musicManager.playPlaylist(playlist.getUri());
                } catch (Exception e) {
                    GlideLogger.error("Failed to play playlist: " + e.getMessage());
                    Glide.getInstance().getNotificationManager().post(
                        TranslateText.MUSIC,
                        TranslateText.valueOf("Failed to play playlist"),
                        NotificationType.ERROR
                    );
                }
                break;
            }
            offsetY += 56;
        }
    }

    private void addToQueue(Track track) {
        MusicManager musicManager = Glide.getInstance().getMusicManager();
        musicManager.addToQueue(track.getUri())
                .thenRun(() ->
                        Glide.getInstance().getNotificationManager().post(
                                TranslateText.MUSIC,
                                TranslateText.valueOf("Added to queue: " + track.getName()),
                                NotificationType.SUCCESS
                        )
                )
                .exceptionally(ex -> {
                    Glide.getInstance().getNotificationManager().post(
                            TranslateText.MUSIC,
                            TranslateText.valueOf("Failed to add to queue"),
                            NotificationType.ERROR
                    );
                    return null;
                });
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        volumeSlider.mouseReleased(mouseX, mouseY, mouseButton);
        updateVolume();
    }

    private void updateVolume() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastVolumeChangeTime > VOLUME_CHANGE_DELAY) {
            lastVolumeChangeTime = currentTime;
            int volume = (int)(volumeSlider.getSetting().getValueFloat() * 100);
            Glide.getInstance().getMusicManager().setVolume(volume);
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (openDownloader) {
            textBox.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public void onTrackInfoUpdated(long position, long duration) {
        this.trackPosition = position;
        this.trackDuration = duration;

        MusicManager musicManager = Glide.getInstance().getMusicManager();
        Track currentTrack = musicManager.getCurrentTrack();
        if (currentTrack != null) {
            String trackId = currentTrack.getId();
            if (!trackId.equals(currentTrackId)) {
                currentTrackId = trackId;
                musicManager.getAlbumArtUrl(currentTrack); // Preload album art
            }
        } else {
            currentTrackId = null;
        }
    }

    private void updateScroll() {
        int totalResults = (searchResults != null ? searchResults.size() : 0) + (userPlaylists != null ? userPlaylists.size() : 0);
        scroll.setMaxScroll(totalResults * 56);
    }

    private String getPlaylistImageUrl(PlaylistSimplified playlist) {
        if (playlist != null && playlist.getImages() != null && playlist.getImages().length > 0) {
            return playlist.getImages()[0].getUrl();
        }
        return null;
    }

    private void drawConnectButton(NanoVGManager nvg, int mouseX, int mouseY) {
        ColorPalette palette = Glide.getInstance().getColorManager().getPalette();
        AccentColor accentColor = Glide.getInstance().getColorManager().getCurrentColor();
        
        float centerX = this.getX() + ((float) this.getWidth() / 2);
        float centerY = this.getY() + ((float) this.getHeight() / 2);
        
        boolean isHovered = MouseUtils.isInside(mouseX, mouseY, centerX - 75, centerY - 20, 150, 40);
        
        // Draw button background
        nvg.drawRoundedRect(
            centerX - 75,
            centerY - 20,
            150,
            40,
            8,
            isHovered ? accentColor.getInterpolateColor() : palette.getBackgroundColor(ColorType.DARK)
        );
        
        // Calculate text width for proper centering
        String text = TranslateText.SPOTIFY_CONNECT.getText();
        float textWidth = nvg.getTextWidth(text, 11, Fonts.MEDIUM);
        float iconWidth = 16; // Icon width
        float spacing = 8; // Space between icon and text
        float totalWidth = iconWidth + spacing + textWidth;
        float startX = centerX - (totalWidth / 2);
        float textY = centerY - 5; // Keep original text position
        float iconY = textY - 2.5f; // Only adjust icon position up slightly more
        
        // Draw Spotify icon
        nvg.drawText(
            LegacyIcon.MUSIC,
            startX,
            iconY,
            isHovered ? Color.WHITE : palette.getFontColor(ColorType.DARK),
            16,
            Fonts.LEGACYICON
        );
        
        // Draw text - keeping original position
        nvg.drawText(
            text,
            startX + iconWidth + spacing,
            textY,
            isHovered ? Color.WHITE : palette.getFontColor(ColorType.DARK),
            11,
            Fonts.MEDIUM
        );
        
        if (isHovered && org.lwjgl.input.Mouse.isButtonDown(0)) {
            openConfirmDialog(Glide.getInstance().getMusicManager().getAuthorizationCodeUri());
            showConnectButton = false;
        }

        if (DEBUG_HITBOXES) {
            nvg.drawRect(
                centerX - 75,
                centerY - 20,
                150,
                40,
                DEBUG_COLOR
            );
        }
    }
}