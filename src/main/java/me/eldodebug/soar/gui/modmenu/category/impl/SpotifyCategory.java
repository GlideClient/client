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
import me.eldodebug.soar.management.music.LyricsManager;
import me.eldodebug.soar.management.music.MusicManager;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import me.eldodebug.soar.management.notification.NotificationType;
import me.eldodebug.soar.ui.comp.impl.CompSlider;
import me.eldodebug.soar.ui.comp.impl.field.CompTextBox;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import me.eldodebug.soar.utils.mouse.Scroll;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.lwjgl.input.Keyboard;

public class SpotifyCategory extends Category implements MusicManager.TrackInfoCallback {

    private static final long VOLUME_CHANGE_DELAY = 500;
    private static final long SEARCH_DEBOUNCE_DELAY = 800;
    private static final ResourceLocation PLACEHOLDER_IMAGE = new ResourceLocation("soar/music.png");
    private static final String SPOTIFY_TUTORIAL_URL = "https://glideclient.com/spotifytutorial";

    private static final boolean DEBUG_HITBOXES = false;
    private static final Color DEBUG_COLOR = new Color(255, 0, 0, 100);

    private final CompSlider volumeSlider;
    private final CompTextBox textBox;
    private final CompTextBox clientIdTextBox;
    private final CompTextBox clientSecretTextBox;
    private final WeakReference<GuiModMenu> parentRef;
    private final ScheduledExecutorService searchDebouncer;
    private ScheduledFuture<?> pendingSearch;

    private volatile List<Track> searchResults;
    private volatile List<PlaylistSimplified> searchPlaylistResults;
    private volatile List<PlaylistSimplified> userPlaylists;
    private boolean openDownloader;
    private boolean showSetupScreen = false;
    private boolean setupError = false;
    private long trackPosition;
    private long trackDuration;
    private long lastVolumeChangeTime;
    private String lastSearchQuery = "";
    private String currentTrackId;
    private final AtomicBoolean isSearching = new AtomicBoolean(false);
    private boolean showConnectButton = true;
    private final Color noColour = new Color(0, 0, 0, 0);

    private boolean showingLyrics = false;
    private final Scroll lyricsScroll = new Scroll();
    private int currentHighlightedLyricIndex = -1;

    public SpotifyCategory(GuiModMenu parent) {
        super(parent, TranslateText.MUSIC, LegacyIcon.MUSIC, true, true);
        this.parentRef = new WeakReference<>(parent);
        this.volumeSlider = new CompSlider(InternalSettingsMod.getInstance().getVolumeSetting());
        this.textBox = new CompTextBox();
        this.clientIdTextBox = new CompTextBox();
        this.clientSecretTextBox = new CompTextBox();

        this.searchDebouncer = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Search-Debouncer");
            t.setDaemon(true);
            return t;
        });

        initializeComponents();
    }

    private void initializeComponents() {
        textBox.setDefaultText("Enter a Spotify link");
        clientIdTextBox.setDefaultText(TranslateText.SPOTIFY_CLIENT_ID.getText());
        clientSecretTextBox.setDefaultText(TranslateText.SPOTIFY_CLIENT_SECRET.getText());
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
        lyricsScroll.resetAll();
        showingLyrics = false;

        MusicManager musicManager = Glide.getInstance().getMusicManager();

        showSetupScreen = !musicManager.hasCredentials();
        showConnectButton = !showSetupScreen && !musicManager.isAuthorized();

        musicManager.setTrackInfoCallback(this);

        if (!showSetupScreen) {
            fetchUserPlaylists();

            CompletableFuture.runAsync(() -> {
                try {
                    musicManager.fetchAndUpdateVolume();
                    int actualVolume = musicManager.getVolume();
                    volumeSlider.getSetting().setValue(actualVolume / 100f);
                } catch (Exception e) {
                    GlideLogger.warn("Failed to sync volume slider: " + e.getMessage());
                }
            });
        } else {
            setupError = false;
            clientIdTextBox.setText("");
            clientSecretTextBox.setText("");
        }
    }

    private void fetchUserPlaylists() {
        Glide.getInstance().getMusicManager().getUserPlaylists()
                .thenAccept(playlists -> {
                    if (playlists != null) {
                        // Reverse to show most recent playlists first
                        java.util.Collections.reverse(playlists);
                    }
                    this.userPlaylists = playlists;
                })
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
                    NotificationType.ERROR);
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

        if (showingLyrics) {
            lyricsScroll.onScroll();
            lyricsScroll.onAnimation();
        }

        if (showSetupScreen) {
            drawSetupScreen(nvg, mouseX, mouseY, palette, accentColor);
        } else if (showConnectButton) {
            drawConnectButton(nvg, mouseX, mouseY);
        } else {
            nvg.save();
            try {
                if (showingLyrics) {
                    drawLyricsView(nvg, palette, accentColor, musicManager, mouseX, mouseY);
                } else {
                    nvg.save();
                    nvg.translate(0, scroll.getValue());
                    drawSearchResults(nvg, palette);
                    drawUserPlaylists(nvg, palette);
                    nvg.restore();

                    drawControlBar(nvg, palette, musicManager);
                    drawPlaybackControls(nvg, palette, musicManager);
                    drawVolumeSlider(nvg, palette, mouseX, mouseY, partialTicks);
                    drawLyricsButton(nvg, palette, mouseX, mouseY);
                    drawProgressBar(nvg, accentColor, palette);
                }
            } finally {
                nvg.restore();
            }

            if (!showingLyrics) {
                nvg.drawVerticalGradientRect(getX() + 15, this.getY(), getWidth() - 30, 12,
                        palette.getBackgroundColor(ColorType.NORMAL), noColour);
                nvg.drawVerticalGradientRect(getX() + 15, this.getY() + this.getHeight() - 58, getWidth() - 30, 12,
                        noColour, palette.getBackgroundColor(ColorType.NORMAL));
            }
        }

        updateScroll();
    }

    private void drawSetupScreen(NanoVGManager nvg, int mouseX, int mouseY, ColorPalette palette, AccentColor accentColor) {
        float centerX = this.getX() + ((float) this.getWidth() / 2);
        float centerY = this.getY() + ((float) this.getHeight() / 2) - 40;

        nvg.drawCenteredText(TranslateText.SPOTIFY_SETUP.getText(),
                centerX, this.getY() + 40,
                palette.getFontColor(ColorType.DARK), 20, Fonts.MEDIUM);

        nvg.drawText(TranslateText.SPOTIFY_CLIENT_ID.getText(),
                centerX - 150, centerY - 10,
                palette.getFontColor(ColorType.NORMAL), 12, Fonts.MEDIUM);

        clientIdTextBox.setWidth(300);
        clientIdTextBox.setHeight(20);
        clientIdTextBox.setX(centerX - 150);
        clientIdTextBox.setY(centerY + 5);
        clientIdTextBox.draw(mouseX, mouseY, 0);

        nvg.drawText(TranslateText.SPOTIFY_CLIENT_SECRET.getText(),
                centerX - 150, centerY + 40,
                palette.getFontColor(ColorType.NORMAL), 12, Fonts.MEDIUM);

        clientSecretTextBox.setWidth(300);
        clientSecretTextBox.setHeight(20);
        clientSecretTextBox.setX(centerX - 150);
        clientSecretTextBox.setY(centerY + 55);
        clientSecretTextBox.draw(mouseX, mouseY, 0);

        float tutorialButtonY = centerY + 100;
        boolean tutorialHovered = MouseUtils.isInside(mouseX, mouseY,
                centerX - 150, tutorialButtonY, 140, 30);

        nvg.drawRoundedRect(centerX - 150, tutorialButtonY, 140, 30, 5,
                palette.getBackgroundColor(ColorType.DARK));

        nvg.drawCenteredText(TranslateText.SPOTIFY_TUTORIAL.getText(),
                centerX - 80, tutorialButtonY + 11,
                tutorialHovered ? palette.getFontColor(ColorType.DARK) : palette.getFontColor(ColorType.NORMAL),
                11, Fonts.MEDIUM);

        float setupButtonX = centerX + 10;
        boolean setupHovered = MouseUtils.isInside(mouseX, mouseY,
                setupButtonX, tutorialButtonY, 140, 30);

        nvg.drawRoundedRect(setupButtonX, tutorialButtonY, 140, 30, 5,
                setupHovered ? accentColor.getInterpolateColor() : palette.getBackgroundColor(ColorType.DARK));

        nvg.drawCenteredText(TranslateText.SPOTIFY_NEXT.getText(),
                setupButtonX + 70, tutorialButtonY + 11,
                setupHovered ? Color.WHITE : palette.getFontColor(ColorType.NORMAL),
                11, Fonts.MEDIUM);

        if (setupError) {
            Glide.getInstance().getNotificationManager().post(TranslateText.MUSIC, TranslateText.SPOTIFY_INVALID_CREDENTIALS, NotificationType.ERROR);
            setupError = false;
        }
    }

    private void drawSearchResults(NanoVGManager nvg, ColorPalette palette) {
        if (searchResults == null && searchPlaylistResults == null) {
            return;
        }

        float offsetY = 13;
        if (searchResults != null) {
            for (Track track : searchResults) {
                if (offsetY + 46 >= -scroll.getValue() && offsetY <= -scroll.getValue() + getHeight()) {
                    drawTrackEntry(nvg, palette, track, offsetY);
                }
                offsetY += 56;
            }
        }
        if (searchPlaylistResults != null) {
            for (PlaylistSimplified playlist : searchPlaylistResults) {
                if (playlist == null) continue;
                if (offsetY + 46 >= -scroll.getValue() && offsetY <= -scroll.getValue() + getHeight()) {
                    drawPlaylistEntry(nvg, palette, playlist, offsetY);
                }
                offsetY += 56;
            }
        }
    }

    private void drawTrackEntry(NanoVGManager nvg, ColorPalette palette, Track track, float offsetY) {
        nvg.drawRoundedRect(this.getX() + 15, this.getY() + offsetY, this.getWidth() - 30, 46, 8, palette.getBackgroundColor(ColorType.DARK));

        drawTrackImage(nvg, track, offsetY);
        drawTrackInfo(nvg, palette, track, offsetY);

        if (DEBUG_HITBOXES) {
            nvg.drawRect(this.getX() + 15, this.getY() + offsetY, this.getWidth() - 30, 46, DEBUG_COLOR);

            nvg.drawRect(this.getX() + 20, this.getY() + offsetY + 5, 36, 36, DEBUG_COLOR);
            nvg.drawRect(this.getX() + this.getWidth() - 60, this.getY() + offsetY + 15, 16, 16, DEBUG_COLOR);
        }
    }

    private void drawTrackImage(NanoVGManager nvg, Track track, float offsetY) {
        if (track == null) {
            drawPlaceholderImage(nvg, offsetY);
            return;
        }

        String albumArtUrl = Glide.getInstance().getMusicManager().getAlbumArtUrl(track);
        if (albumArtUrl != null && new File(albumArtUrl).exists()) {
            nvg.drawRoundedImage(new File(albumArtUrl), this.getX() + 20, this.getY() + offsetY + 5, 36, 36, 6);
        } else {
            drawPlaceholderImage(nvg, offsetY);
        }
    }

    private void drawTrackInfo(NanoVGManager nvg, ColorPalette palette, Track track, float offsetY) {
        String trackName = nvg.getLimitText(track.getName(), 11, Fonts.MEDIUM, 280);
        nvg.drawText(trackName, this.getX() + 63, this.getY() + offsetY + 9, palette.getFontColor(ColorType.DARK), 11, Fonts.MEDIUM);
        nvg.drawText(track.getArtists()[0].getName(), this.getX() + 63, this.getY() + offsetY + 25, palette.getFontColor(ColorType.NORMAL), 9, Fonts.MEDIUM);
        nvg.drawText(LegacyIcon.PLUS_SQUARE, this.getX() + this.getWidth() - 60, this.getY() + offsetY + 15, palette.getFontColor(ColorType.NORMAL), 16, Fonts.LEGACYICON);
    }

    private void drawPlaceholderImage(NanoVGManager nvg, float offsetY) {
        try {
            nvg.drawRoundedImage(PLACEHOLDER_IMAGE, this.getX() + 20, this.getY() + offsetY + 5, 36, 36, 6);
        } catch (Exception e) {
            nvg.drawRoundedRect(this.getX() + 20, this.getY() + offsetY + 5, 36, 36, 6, new Color(50, 50, 50));
        }
    }

    private void drawPlaylistEntry(NanoVGManager nvg, ColorPalette palette, PlaylistSimplified playlist, float offsetY) {
        nvg.drawRoundedRect(this.getX() + 15, this.getY() + offsetY, this.getWidth() - 30, 46, 8, palette.getBackgroundColor(ColorType.DARK));

        String imageUrl = Glide.getInstance().getMusicManager().getPlaylistImageUrl(playlist);
        if (imageUrl != null) {
            File imageFile = new File(imageUrl);
            if (imageFile.exists()) {
                nvg.drawRoundedImage(imageFile, this.getX() + 20, this.getY() + offsetY + 5, 36, 36, 6);
            } else {
                drawPlaceholderImage(nvg, offsetY);
            }
        } else {
            drawPlaceholderImage(nvg, offsetY);
        }

        String playlistName = playlist.getName() != null ? playlist.getName() : "Untitled Playlist";
        nvg.drawText(nvg.getLimitText(playlistName, 11, Fonts.MEDIUM, 280), this.getX() + 63, this.getY() + offsetY + 9, palette.getFontColor(ColorType.DARK), 11, Fonts.MEDIUM);

        String ownerName = "Unknown Artist";
        if (playlist.getOwner() != null && playlist.getOwner().getDisplayName() != null) {
            ownerName = playlist.getOwner().getDisplayName();
        }

        nvg.drawText(ownerName, this.getX() + 63, this.getY() + offsetY + 25, palette.getFontColor(ColorType.NORMAL), 9, Fonts.MEDIUM);

        nvg.drawText(LegacyIcon.PLAY, this.getX() + this.getWidth() - 60, this.getY() + offsetY + 15, palette.getFontColor(ColorType.NORMAL), 16, Fonts.LEGACYICON);

        if (DEBUG_HITBOXES) {
            nvg.drawRect(this.getX() + 15, this.getY() + offsetY, this.getWidth() - 30, 46, DEBUG_COLOR);
            nvg.drawRect(this.getX() + 20, this.getY() + offsetY + 5, 36, 36, DEBUG_COLOR);
        }
    }

    private void drawUserPlaylists(NanoVGManager nvg, ColorPalette palette) {
        if (userPlaylists == null) {
            return;
        }

        float offsetY = 13 + (searchResults != null ? searchResults.size() * 56 : 0) + (searchPlaylistResults != null ? searchPlaylistResults.size() * 56 : 0);
        for (PlaylistSimplified playlist : userPlaylists) {
            if (playlist == null) {
                continue;
            }

            nvg.drawRoundedRect(this.getX() + 15, this.getY() + offsetY, this.getWidth() - 30, 46, 8, palette.getBackgroundColor(ColorType.DARK));

            String imageUrl = Glide.getInstance().getMusicManager().getPlaylistImageUrl(playlist);
            if (imageUrl != null) {
                File imageFile = new File(imageUrl);
                if (imageFile.exists()) {
                    nvg.drawRoundedImage(imageFile, this.getX() + 20, this.getY() + offsetY + 5, 36, 36, 6);
                } else {
                    drawPlaceholderImage(nvg, offsetY);
                }
            } else {
                drawPlaceholderImage(nvg, offsetY);
            }

            String playlistName = playlist.getName() != null ? playlist.getName() : "Untitled Playlist";
            nvg.drawText(nvg.getLimitText(playlistName, 11, Fonts.MEDIUM, 280), this.getX() + 63, this.getY() + offsetY + 9, palette.getFontColor(ColorType.DARK), 11, Fonts.MEDIUM);

            String ownerName = "Unknown Artist";
            if (playlist.getOwner() != null && playlist.getOwner().getDisplayName() != null) {
                ownerName = playlist.getOwner().getDisplayName();
            }

            nvg.drawText(ownerName, this.getX() + 63, this.getY() + offsetY + 25, palette.getFontColor(ColorType.NORMAL), 9, Fonts.MEDIUM);

            if (DEBUG_HITBOXES) {
                nvg.drawRect(this.getX() + 15, this.getY() + offsetY, this.getWidth() - 30, 46, DEBUG_COLOR);
                nvg.drawRect(this.getX() + 20, this.getY() + offsetY + 5, 36, 36, DEBUG_COLOR);
            }

            offsetY += 56;
        }
    }

    private void drawControlBar(NanoVGManager nvg, ColorPalette palette, MusicManager musicManager) {
        nvg.drawRoundedRectVarying(this.getX(), this.getY() + this.getHeight() - 46F, this.getWidth(), 46, 0, 0, 0, 12, palette.getBackgroundColor(ColorType.DARK));

        Track currentTrack = musicManager.getCurrentTrack();
        if (currentTrack != null) {
            String albumArtUrl = musicManager.getAlbumArtUrl(currentTrack);
            if (albumArtUrl != null) {
                File albumArtFile = new File(albumArtUrl);
                if (albumArtFile.exists()) {
                    nvg.drawRoundedImage(albumArtFile, this.getX() + 4, this.getY() + this.getHeight() - 43, 36, 36, 6);
                } else {
                    nvg.drawRoundedRect(this.getX() + 4, this.getY() + this.getHeight() - 43, 36, 36, 6, new Color(50, 50, 50));
                    try {
                        nvg.drawRoundedImage(PLACEHOLDER_IMAGE, this.getX() + 4, this.getY() + this.getHeight() - 43, 36, 36, 6);
                    } catch (Exception ignored) {}
                }
            } else {
                nvg.drawRoundedRect(this.getX() + 4, this.getY() + this.getHeight() - 43, 36, 36, 6, new Color(50, 50, 50));
                try {
                    nvg.drawRoundedImage(PLACEHOLDER_IMAGE, this.getX() + 4, this.getY() + this.getHeight() - 43, 36, 36, 6);
                } catch (Exception ignored) {}
            }

            nvg.drawText(nvg.getLimitText(currentTrack.getName(), 9, Fonts.MEDIUM, 100), this.getX() + 45, this.getY() + this.getHeight() - 39, palette.getFontColor(ColorType.DARK), 9, Fonts.MEDIUM);
            nvg.drawText(nvg.getLimitText(currentTrack.getArtists()[0].getName(), 9, Fonts.MEDIUM, 100), this.getX() + 45, this.getY() + this.getHeight() - 27, palette.getFontColor(ColorType.NORMAL), 9, Fonts.MEDIUM);
        } else {
            nvg.drawRoundedRect(this.getX() + 4, this.getY() + this.getHeight() - 43, 36, 36, 6, new Color(50, 50, 50));
            try {
                nvg.drawRoundedImage(PLACEHOLDER_IMAGE, this.getX() + 4, this.getY() + this.getHeight() - 43, 36, 36, 6);
            } catch (Exception ignored) {
            }
            nvg.drawText(TranslateText.NOTHING_IS_PLAYING.getText(), this.getX() + 45, this.getY() + this.getHeight() - 33, palette.getFontColor(ColorType.DARK), 9, Fonts.MEDIUM);
        }
    }

    private void drawPlaybackControls(NanoVGManager nvg, ColorPalette palette, MusicManager musicManager) {
        float centerX = this.getX() + ((float) this.getWidth() / 2);
        float centerY = this.getY() + this.getHeight() - 32F;
        Color normalColor = palette.getFontColor(ColorType.NORMAL);

        nvg.drawText(LegacyIcon.BACK, centerX - 32, centerY, normalColor, 16, Fonts.LEGACYICON);
        nvg.drawText(musicManager.isPlaying() ? LegacyIcon.PAUSE : LegacyIcon.PLAY, centerX - 8, centerY, normalColor, 16, Fonts.LEGACYICON);
        nvg.drawText(LegacyIcon.FORWARD, centerX + 16, centerY, normalColor, 16, Fonts.LEGACYICON);
        if (DEBUG_HITBOXES) {
            nvg.drawRect(centerX - 24 - 8, centerY, 16, 16, DEBUG_COLOR);
            nvg.drawRect(centerX - 8, centerY, 16, 16, DEBUG_COLOR);
            nvg.drawRect(centerX + 24 - 8, centerY, 16, 16, DEBUG_COLOR);
        }
    }

    private void drawVolumeSlider(NanoVGManager nvg, ColorPalette palette, int mouseX, int mouseY, float partialTicks) {
        volumeSlider.setX(this.getX() + this.getWidth() - 72);
        volumeSlider.setY(this.getY() + this.getHeight() - 20);
        volumeSlider.setWidth(62);
        volumeSlider.setHeight(4.5f);
        volumeSlider.draw(mouseX, mouseY, partialTicks);

        int volume = (int) (volumeSlider.getSetting().getValueFloat() * 100);
        String volumeIcon = getVolumeIcon(volume);
        nvg.drawText(volumeIcon, this.getX() + this.getWidth() - 94, this.getY() + this.getHeight() - 26, palette.getFontColor(ColorType.NORMAL), 16, Fonts.LEGACYICON);
    }

    private String getVolumeIcon(int volume) {
        if (volume == 0) {
            return LegacyIcon.VOLUME_X;
        }
        if (volume > 80) {
            return LegacyIcon.VOLUME_2;
        }
        if (volume > 40) {
            return LegacyIcon.VOLUME_1;
        }
        return LegacyIcon.VOLUME;
    }

    private void drawProgressBar(NanoVGManager nvg, AccentColor accentColor, ColorPalette palette) {
        if (trackDuration <= 0) {
            return;
        }

        int progressBarWidth = this.getWidth() - 40;
        int progressBarY = this.getY() + this.getHeight() - 5;
        nvg.drawRoundedRect(this.getX() + 20, progressBarY, progressBarWidth, 2, 1, palette.getBackgroundColor(ColorType.NORMAL));
        float progress = (float) trackPosition / trackDuration;
        nvg.drawRoundedRect(this.getX() + 20, progressBarY, progressBarWidth * progress, 2, 1, accentColor.getInterpolateColor());
    }

    private void checkAndUpdateSearch() {
        GuiModMenu parent = parentRef.get();
        if (parent == null) {
            return;
        }

        String currentSearchQuery = parent.getSearchBox().getText();
        if (!currentSearchQuery.equals(lastSearchQuery)) {
            scheduleSearch(currentSearchQuery);
            lastSearchQuery = currentSearchQuery;
        }
    }

    private void scheduleSearch(String query) {
        if (query.isEmpty()) {
            searchResults = null;
            searchPlaylistResults = null;
            return;
        }

        if (pendingSearch != null && !pendingSearch.isDone()) {
            pendingSearch.cancel(false);
        }

        pendingSearch = searchDebouncer.schedule(() -> {
            if (isSearching.compareAndSet(false, true)) {
                try {
                    MusicManager musicManager = Glide.getInstance().getMusicManager();
                    CompletableFuture<List<Track>> tracksFuture = musicManager.searchTracks(query);
                    CompletableFuture<List<PlaylistSimplified>> playlistsFuture = musicManager.searchPlaylists(query);

                    List<Track> results = tracksFuture.join();
                    List<PlaylistSimplified> playlists = playlistsFuture.join();

                    searchResults = results;
                    searchPlaylistResults = playlists;

                    if (results != null) {
                        int visibleTracks = Math.min(results.size(), 5);
                        for (int i = 0; i < visibleTracks; i++) {
                            musicManager.getAlbumArtUrl(results.get(i));
                        }
                    }
                    if (playlists != null) {
                        int visiblePlaylists = Math.min(playlists.size(), 5);
                        for (int i = 0; i < visiblePlaylists; i++) {
                            musicManager.getPlaylistImageUrl(playlists.get(i));
                        }
                    }
                } catch (Exception ex) {
                    GlideLogger.error("Search failed", ex);
                    Glide.getInstance().getNotificationManager().post(TranslateText.MUSIC, TranslateText.SPOTIFY_SEARCH_FAILED, NotificationType.ERROR);
                } finally {
                    isSearching.set(false);
                }
            }
        }, SEARCH_DEBOUNCE_DELAY, TimeUnit.MILLISECONDS);
    }

    private void drawLyricsButton(NanoVGManager nvg, ColorPalette palette, int mouseX, int mouseY) {
        float buttonX = this.getX() + this.getWidth() - 116;
        float buttonY = this.getY() + this.getHeight() - 26;

        boolean isHovered = MouseUtils.isInside(mouseX, mouseY, buttonX, buttonY, 16, 16);

        nvg.drawText(LegacyIcon.LIST, buttonX, buttonY, isHovered ? palette.getFontColor(ColorType.DARK) : palette.getFontColor(ColorType.NORMAL), 16, Fonts.LEGACYICON);

        if (DEBUG_HITBOXES) {
            nvg.drawRect(buttonX, buttonY, 16, 16, DEBUG_COLOR);
        }
    }

    private void drawLyricsView(NanoVGManager nvg, ColorPalette palette, AccentColor accentColor, MusicManager musicManager, int mouseX, int mouseY) {
        nvg.drawRoundedRect(this.getX(), this.getY(), this.getWidth(), this.getHeight() - 46, 0, palette.getBackgroundColor(ColorType.NORMAL));

        Track currentTrack = musicManager.getCurrentTrack();

        float backButtonX = this.getX() + 15;
        float backButtonY = this.getY() + 15;

        boolean isBackHovered = MouseUtils.isInside(mouseX, mouseY, backButtonX, backButtonY, 16, 16);

        nvg.drawText(LegacyIcon.ARROW_LEFT, backButtonX, backButtonY, isBackHovered ? palette.getFontColor(ColorType.DARK) : palette.getFontColor(ColorType.NORMAL), 16, Fonts.LEGACYICON);

        if (currentTrack != null) {
            if (musicManager.getLyricsManager() != null) {
                if (MouseUtils.isInside(mouseX, mouseY, getX(), getY(), getWidth(), getHeight() - 46)) {
                    lyricsScroll.onScroll();
                }
                lyricsScroll.onAnimation();

                drawScrollableLyrics(nvg, palette, accentColor, musicManager, mouseX, mouseY, 0, trackPosition);
            } else {
                nvg.drawCenteredText("Lyrics feature not available", this.getX() + (float) this.getWidth() / 2, this.getY() + this.getHeight() / 2.7F, palette.getFontColor(ColorType.NORMAL), 14, Fonts.MEDIUM);
            }
        } else {
            nvg.drawCenteredText("No track is currently playing", this.getX() + (float) this.getWidth() / 2, this.getY() + this.getHeight() / 2.7F, palette.getFontColor(ColorType.NORMAL), 14, Fonts.MEDIUM);
        }

        drawControlBar(nvg, palette, musicManager);
        drawPlaybackControls(nvg, palette, musicManager);
        drawVolumeSlider(nvg, palette, mouseX, mouseY, 0);
        drawProgressBar(nvg, accentColor, palette);
    }

    private void drawScrollableLyrics(NanoVGManager nvg, ColorPalette palette, AccentColor accentColor, MusicManager musicManager, int mouseX, int mouseY, float startY, long currentPosition) {
        LyricsManager lyricsManager = musicManager.getLyricsManager();
        LyricsManager.LyricsResponse lyrics = lyricsManager.getCurrentLyrics();

        if (lyrics == null || lyrics.isError() || lyrics.getLines().isEmpty()) {
            nvg.drawCenteredText("No lyrics available for this track", this.getX() + (float) this.getWidth() / 2, this.getY() + this.getHeight() / 2.7F, palette.getFontColor(ColorType.NORMAL), 14, Fonts.MEDIUM);
            return;
        }

        List<LyricsManager.LyricsLine> allLines = lyrics.getLines();

        lyricsManager.updateCurrentLineIndex(currentPosition);
        int currentLineIndex = lyricsManager.getCurrentLineIndex();
        float lyricsAreaHeight = this.getHeight() - startY - 46;

        nvg.save();
        nvg.scissor(this.getX() + 15, this.getY() + startY, this.getWidth() - 30, lyricsAreaHeight);

        float baseLineHeight = 30;
        float totalContentHeight = 0;
        float yOffset = lyricsScroll.getValue();

        currentHighlightedLyricIndex = -1;

        int[] lineHeights = new int[allLines.size()];
        String[][] wrappedLines = new String[allLines.size()][];

        float maxTextWidth = this.getWidth() - 60;

        for (int i = 0; i < allLines.size(); i++) {
            LyricsManager.LyricsLine line = allLines.get(i);
            if (line == null) {
                lineHeights[i] = (int) baseLineHeight;
                wrappedLines[i] = new String[0];
                continue;
            }

            String lyricsText = extractLyricsText(line);
            if (lyricsText.isEmpty()) {
                lineHeights[i] = (int) baseLineHeight;
                wrappedLines[i] = new String[0];
                continue;
            }

            float fontSize = (i == currentLineIndex) ? 14 : 12;

            String[] wrapped = wrapText(nvg, lyricsText, fontSize, Fonts.MEDIUM, maxTextWidth);
            wrappedLines[i] = wrapped;

            int linesCount = wrapped.length;
            lineHeights[i] = linesCount <= 1 ? (int) baseLineHeight :
                    (int) (fontSize * linesCount * 1.0f + 10);

            totalContentHeight += lineHeights[i];
        }

        float currentY = this.getY() + startY + yOffset;
        float visibleTop = this.getY() + startY;
        float visibleBottom = visibleTop + lyricsAreaHeight;

        for (int i = 0; i < allLines.size(); i++) {
            LyricsManager.LyricsLine line = allLines.get(i);
            if (line == null) {
                currentY += lineHeights[i];
                continue;
            }

            if (currentY + lineHeights[i] < visibleTop || currentY > visibleBottom) {
                currentY += lineHeights[i];
                continue;
            }

            boolean isCurrentLine = (i == currentLineIndex);
            boolean isHovered = MouseUtils.isInside(mouseX, mouseY, this.getX() + 20, currentY, this.getWidth() - 40, lineHeights[i]);

            if (isHovered) {
                currentHighlightedLyricIndex = i;
            }

            Color lineColor;
            float fontSize;

            if (isCurrentLine) {
                lineColor = accentColor.getInterpolateColor();
                fontSize = 14;

                nvg.drawRoundedRect(this.getX() + 20, currentY, this.getWidth() - 40, lineHeights[i], 4,
                        new Color(accentColor.getColor1().getRed(), accentColor.getColor1().getGreen(),
                                accentColor.getColor1().getBlue(), 30));
            } else if (isHovered) {
                lineColor = palette.getFontColor(ColorType.DARK);
                fontSize = 12;
            } else {
                lineColor = palette.getFontColor(ColorType.NORMAL);
                fontSize = 12;
            }

            String[] wrapped = wrappedLines[i];
            if (wrapped.length > 0) {
                float lineSpacing = 1.0f;
                float wrapOffset = 0;

                for (String wrappedLine : wrapped) {
                    float textWidth = nvg.getTextWidth(wrappedLine, fontSize, Fonts.MEDIUM);
                    float textX = this.getX() + ((float) this.getWidth() / 2) - (textWidth / 2);
                    float textY = currentY + wrapOffset + (fontSize / 2);

                    if (isCurrentLine) {
                        nvg.drawTextGlowing(wrappedLine, textX, textY, lineColor, 8, fontSize, Fonts.MEDIUM);
                    } else {
                        nvg.drawText(wrappedLine, textX, textY, lineColor, fontSize, Fonts.MEDIUM);
                    }

                    wrapOffset += fontSize * lineSpacing;
                }
            }

            currentY += lineHeights[i];
        }

        nvg.restore();

        float maxScroll = Math.max(0, totalContentHeight - lyricsAreaHeight + 20);
        lyricsScroll.setMaxScroll(maxScroll);

        if (lyricsScroll.getValue() < 0) {
            nvg.drawVerticalGradientRect(getX() + 15, getY() + startY, getWidth() - 30, 12, palette.getBackgroundColor(ColorType.NORMAL), noColour);
        }

        if (-lyricsScroll.getValue() < maxScroll) {
            nvg.drawVerticalGradientRect(getX() + 15, getY() + startY + lyricsAreaHeight - 12, getWidth() - 30, 12, noColour, palette.getBackgroundColor(ColorType.NORMAL));
        }
    }

    private String[] wrapText(NanoVGManager nvg, String text, float fontSize, me.eldodebug.soar.management.nanovg.font.Font font, float maxWidth) {
        if (text == null || text.isEmpty()) {
            return new String[0];
        }

        if (nvg.getTextWidth(text, fontSize, font) <= maxWidth) {
            return new String[]{text};
        }

        java.util.List<String> lines = new java.util.ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.toString() + (currentLine.length() > 0 ? " " : "") + word;
            if (nvg.getTextWidth(testLine, fontSize, font) <= maxWidth) {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }

                if (nvg.getTextWidth(word, fontSize, font) > maxWidth) {
                    StringBuilder partialWord = new StringBuilder();
                    for (char c : word.toCharArray()) {
                        String testWord = partialWord.toString() + c;
                        if (nvg.getTextWidth(testWord, fontSize, font) <= maxWidth) {
                            partialWord.append(c);
                        } else {
                            lines.add(partialWord.toString());
                            partialWord = new StringBuilder().append(c);
                        }
                    }

                    if (partialWord.length() > 0) {
                        currentLine = partialWord;
                    }
                } else {
                    currentLine.append(word);
                }
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines.toArray(new String[0]);
    }

    private String extractLyricsText(LyricsManager.LyricsLine line) {
        if (line.getWords() != null && !line.getWords().isEmpty()) {
            return line.getWords();
        }

        if (line.getRomanizedWords() != null && !line.getRomanizedWords().isEmpty()) {
            return line.getRomanizedWords();
        }

        return "";
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!MouseUtils.isInside(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())) {
            return;
        }

        if (showSetupScreen) {
            handleSetupScreenClick(mouseX, mouseY, mouseButton);
            return;
        }

        if (openDownloader) {
            handleDownloaderClick(mouseX, mouseY, mouseButton);
            return;
        }

        if (showConnectButton) {
            if (MouseUtils.isInside(mouseX, mouseY, this.getX() + (float) this.getWidth() / 2 - 75, this.getY() + (float) this.getHeight() / 2 - 20, 150, 40) && mouseButton == 0) {
                openConfirmDialog(Glide.getInstance().getMusicManager().getAuthorizationCodeUri());
                showConnectButton = false;
            }
            return;
        }

        if (!showingLyrics && mouseButton == 0) {
            float lyricsButtonX = this.getX() + this.getWidth() - 116;
            float lyricsButtonY = this.getY() + this.getHeight() - 26;

            if (MouseUtils.isInside(mouseX, mouseY, lyricsButtonX, lyricsButtonY, 16, 16)) {
                showingLyrics = true;
                lyricsScroll.resetAll();

                MusicManager musicManager = Glide.getInstance().getMusicManager();
                if (musicManager.getCurrentTrack() != null && musicManager.getLyricsManager() != null) {
                    musicManager.getLyricsManager().fetchLyrics(musicManager.getCurrentTrack());
                }
                return;
            }
        }

        boolean isInControlBar = mouseY >= this.getY() + this.getHeight() - 46;
        if (mouseButton == 0 && isInControlBar) {
            handleControlBarClick(mouseX, mouseY);
            return;
        }

        if (showingLyrics) {
            if (MouseUtils.isInside(mouseX, mouseY, this.getX() + 15, this.getY() + 15, 16, 16) && mouseButton == 0) {
                showingLyrics = false;
                return;
            }

            if (currentHighlightedLyricIndex >= 0 && mouseButton == 0) {
                MusicManager musicManager = Glide.getInstance().getMusicManager();
                LyricsManager lyricsManager = musicManager.getLyricsManager();
                LyricsManager.LyricsResponse lyrics = lyricsManager.getCurrentLyrics();

                if (lyrics != null && !lyrics.isError()
                        && currentHighlightedLyricIndex < lyrics.getLines().size()) {

                    LyricsManager.LyricsLine line = lyrics.getLines().get(currentHighlightedLyricIndex);
                    if (line != null) {
                        long startTime = line.getStartTime();
                        musicManager.seekToPosition(startTime);
                    }
                }
                return;
            }
            return;
        }

        if (mouseButton == 0 && searchResults != null && !showingLyrics) {
            handleTrackClick(mouseX, mouseY);
        } else if (mouseButton == 0 && userPlaylists != null && !showingLyrics) {
            handlePlaylistClick(mouseX, mouseY);
        }
    }

    private void handleSetupScreenClick(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) return;

        clientIdTextBox.mouseClicked(mouseX, mouseY, mouseButton);
        clientSecretTextBox.mouseClicked(mouseX, mouseY, mouseButton);

        float centerX = this.getX() + ((float) this.getWidth() / 2);
        float centerY = this.getY() + ((float) this.getHeight() / 2) - 40;
        float tutorialButtonY = centerY + 100;

        if (MouseUtils.isInside(mouseX, mouseY, centerX - 150, tutorialButtonY, 140, 30)) {
            openConfirmDialog(SPOTIFY_TUTORIAL_URL);
        }

        if (MouseUtils.isInside(mouseX, mouseY, centerX + 10, tutorialButtonY, 140, 30)) {
            String clientId = clientIdTextBox.getText();
            String clientSecret = clientSecretTextBox.getText();

            if (clientId.isEmpty() || clientSecret.isEmpty()) {
                setupError = true;
                return;
            }

            MusicManager musicManager = Glide.getInstance().getMusicManager();
            musicManager.saveCredentials(clientId, clientSecret);

            if (musicManager.hasCredentials()) {
                setupError = false;
                showSetupScreen = false;
                showConnectButton = true;

                Glide.getInstance().getNotificationManager().post(
                        TranslateText.SPOTIFY_AUTH,
                        TranslateText.CREDENTIALS_SAVED,
                        NotificationType.SUCCESS);
            } else {
                setupError = true;
            }
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

        if (MouseUtils.isInside(mouseX, mouseY, centerX - 24 - 8, centerY, 16, 16)) {
            musicManager.previousTrack();
            return;
        }

        if (MouseUtils.isInside(mouseX, mouseY, centerX - 8, centerY, 16, 16)) {
            if (musicManager.isPlaying()) {
                musicManager.pause();
            } else {
                musicManager.resume();
            }
            return;
        }

        if (MouseUtils.isInside(mouseX, mouseY, centerX + 24 - 8, centerY, 16, 16)) {
            musicManager.nextTrack();
            return;
        }

        if (MouseUtils.isInside(mouseX, mouseY, this.getX() + this.getWidth() - 72, this.getY() + this.getHeight() - 22, 62, 8)) {
            volumeSlider.mouseClicked(mouseX, mouseY, 0);
            return;
        }

        handleProgressBarClick(mouseX, mouseY, musicManager);
    }

    private void handleProgressBarClick(int mouseX, int mouseY, MusicManager musicManager) {
        int progressBarY = this.getY() + this.getHeight() - 5;
        if (MouseUtils.isInside(mouseX, mouseY, this.getX() + 20, progressBarY - 5, this.getWidth() - 40, 10)) {
            float clickPosition = (mouseX - (this.getX() + 20)) / (float) (this.getWidth() - 40);
            musicManager.seekToPosition((long) (clickPosition * trackDuration));
        }
    }

    private void handleTrackClick(int mouseX, int mouseY) {
        float offsetY = 13 + scroll.getValue();
        if (searchResults != null) {
            for (Track track : searchResults) {
                if (MouseUtils.isInside(mouseX, mouseY, this.getX() + 15, this.getY() + offsetY, this.getWidth() - 30, 46)) {
                    if (MouseUtils.isInside(mouseX, mouseY, this.getX() + this.getWidth() - 60, this.getY() + offsetY + 15, 16, 16)) {
                        addToQueue(track);
                    } else {
                        Glide.getInstance().getMusicManager().play(track.getUri());
                    }
                    return;
                }
                offsetY += 56;
            }
        }
        if (searchPlaylistResults != null) {
            for (PlaylistSimplified playlist : searchPlaylistResults) {
                if (playlist == null || playlist.getUri() == null) continue;
                if (MouseUtils.isInside(mouseX, mouseY, this.getX() + 15, this.getY() + offsetY, this.getWidth() - 30, 46)) {
                    try {
                        Glide.getInstance().getMusicManager().playPlaylist(playlist.getUri());
                    } catch (Exception e) {
                        GlideLogger.error("Failed to play playlist: " + e.getMessage());
                        Glide.getInstance().getNotificationManager().post(TranslateText.MUSIC, TranslateText.SPOTIFY_FAILED_TO_PLAY_PLAYLIST, NotificationType.ERROR);
                    }
                    return;
                }
                offsetY += 56;
            }
        }
    }

    private void handlePlaylistClick(int mouseX, int mouseY) {
        if (userPlaylists == null) {
            return;
        }
        MusicManager musicManager = Glide.getInstance().getMusicManager();
        if (musicManager == null) {
            return;
        }

        float offsetY = 13 + (searchResults != null ? searchResults.size() * 56 : 0) + (searchPlaylistResults != null ? searchPlaylistResults.size() * 56 : 0) + scroll.getValue();
        for (PlaylistSimplified playlist : userPlaylists) {
            if (playlist == null || playlist.getUri() == null) {
                continue;
            }

            if (MouseUtils.isInside(mouseX, mouseY, this.getX() + 15, this.getY() + offsetY, this.getWidth() - 30, 46)) {
                try {
                    musicManager.playPlaylist(playlist.getUri());
                } catch (Exception e) {
                    GlideLogger.error("Failed to play playlist: " + e.getMessage());
                    Glide.getInstance().getNotificationManager().post(TranslateText.MUSIC, TranslateText.SPOTIFY_FAILED_TO_PLAY_PLAYLIST, NotificationType.ERROR);
                }
                break;
            }
            offsetY += 56;
        }
    }

    private void addToQueue(Track track) {
        MusicManager musicManager = Glide.getInstance().getMusicManager();
        musicManager.addToQueue(track.getUri())
                .thenRun(() -> Glide.getInstance().getNotificationManager().post(TranslateText.MUSIC, TranslateText.SPOTIFY_ADDED_TO_QUEUE, NotificationType.SUCCESS))
                .exceptionally(ex -> {
                    Glide.getInstance().getNotificationManager().post(TranslateText.MUSIC, TranslateText.SPOTIFY_FAILED_TO_ADD_TO_QUEUE, NotificationType.ERROR);
                    return null;
                });
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (!MouseUtils.isInside(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())) {
            return;
        }

        if (!showSetupScreen) {
            volumeSlider.mouseReleased(mouseX, mouseY, mouseButton);
            updateVolume();
        }
    }

    private void updateVolume() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastVolumeChangeTime > VOLUME_CHANGE_DELAY) {
            lastVolumeChangeTime = currentTime;
            int volume = (int) (volumeSlider.getSetting().getValueFloat() * 100);
            Glide.getInstance().getMusicManager().setVolume(volume);
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (showSetupScreen) {
            clientIdTextBox.keyTyped(typedChar, keyCode);
            clientSecretTextBox.keyTyped(typedChar, keyCode);
            return;
        }

        if (openDownloader) {
            textBox.keyTyped(typedChar, keyCode);
        }

        // Disable spacebar music control if search bar is focused
        GuiModMenu parent = parentRef.get();
        boolean searchBarFocused = parent != null && parent.getSearchBox() != null && parent.getSearchBox().isFocused();

        if (keyCode == Keyboard.KEY_SPACE && !showConnectButton && !searchBarFocused) {
            MusicManager musicManager = Glide.getInstance().getMusicManager();
            if (musicManager.isPlaying()) {
                musicManager.pause();
            } else {
                musicManager.resume();
            }
        }

        if (keyCode == Keyboard.KEY_UP && !showConnectButton) {
            MusicManager musicManager = Glide.getInstance().getMusicManager();
            int currentVolume = (int) (volumeSlider.getSetting().getValueFloat() * 100);
            int newVolume = Math.min(100, currentVolume + 5);
            volumeSlider.getSetting().setValue(newVolume / 100f);
            musicManager.setVolume(newVolume);
            lastVolumeChangeTime = System.currentTimeMillis();
        } else if (keyCode == Keyboard.KEY_DOWN && !showConnectButton) {
            MusicManager musicManager = Glide.getInstance().getMusicManager();
            int currentVolume = (int) (volumeSlider.getSetting().getValueFloat() * 100);
            int newVolume = Math.max(0, currentVolume - 5);
            volumeSlider.getSetting().setValue(newVolume / 100f);
            musicManager.setVolume(newVolume);
            lastVolumeChangeTime = System.currentTimeMillis();
        }

        if (keyCode == Keyboard.KEY_RIGHT && !showConnectButton) {
            MusicManager musicManager = Glide.getInstance().getMusicManager();
            long newPosition = Math.min(trackPosition + 10000, trackDuration);
            musicManager.seekToPosition(newPosition);
        } else if (keyCode == Keyboard.KEY_LEFT && !showConnectButton) {
            MusicManager musicManager = Glide.getInstance().getMusicManager();
            long newPosition = Math.max(trackPosition - 10000, 0);
            musicManager.seekToPosition(newPosition);
        }

        if (showingLyrics) {
            lyricsScroll.onKey(keyCode);
        } else {
            scroll.onKey(keyCode);
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
                musicManager.getAlbumArtUrl(currentTrack);

                if (showingLyrics && musicManager.getLyricsManager() != null) {
                    musicManager.getLyricsManager().fetchLyrics(currentTrack);
                    lyricsScroll.resetAll();
                }
            }
        } else {
            currentTrackId = null;
        }
    }

    private void updateScroll() {
        int totalResults = 0;
        if (searchResults != null) totalResults += searchResults.size();
        if (searchPlaylistResults != null) totalResults += searchPlaylistResults.size();
        if (userPlaylists != null) totalResults += userPlaylists.size();
        scroll.setMaxScroll(totalResults * 56);
    }

    private void drawConnectButton(NanoVGManager nvg, int mouseX, int mouseY) {
        ColorPalette palette = Glide.getInstance().getColorManager().getPalette();
        AccentColor accentColor = Glide.getInstance().getColorManager().getCurrentColor();

        float centerX = this.getX() + ((float) this.getWidth() / 2);
        float centerY = this.getY() + ((float) this.getHeight() / 2);

        float buttonWidth = 150;
        float buttonHeight = 40;
        float buttonX = centerX - (buttonWidth / 2);
        float buttonY = centerY - (buttonHeight / 2);

        boolean isHovered = MouseUtils.isInside(mouseX, mouseY, buttonX, buttonY, buttonWidth, buttonHeight);
        nvg.drawRoundedRect(buttonX, buttonY, buttonWidth, buttonHeight, 8,
                isHovered ? accentColor.getInterpolateColor() : palette.getBackgroundColor(ColorType.DARK));

        String text = TranslateText.SPOTIFY_CONNECT.getText();
        float textWidth = nvg.getTextWidth(text, 11, Fonts.MEDIUM);
        float iconWidth = 16;
        float spacing = 8;
        float totalWidth = iconWidth + spacing + textWidth;

        float startX = centerX - (totalWidth / 2);

        float iconY = buttonY + (buttonHeight / 2) - 8;

        float textY = buttonY + (buttonHeight / 2) - 3;

        nvg.drawText(LegacyIcon.MUSIC, startX, iconY,
                isHovered ? Color.WHITE : palette.getFontColor(ColorType.DARK), 16, Fonts.LEGACYICON);

        nvg.drawText(text, startX + iconWidth + spacing, textY,
                isHovered ? Color.WHITE : palette.getFontColor(ColorType.DARK), 11, Fonts.MEDIUM);

        if (DEBUG_HITBOXES) {
            nvg.drawRect(buttonX, buttonY, buttonWidth, buttonHeight, DEBUG_COLOR);
        }
    }
}
