package me.eldodebug.soar.management.mods.impl;

import com.wrapper.spotify.model_objects.specification.Track;
import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventRender2D;
import me.eldodebug.soar.management.event.impl.EventUpdate;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.SimpleHUDMod;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;
import me.eldodebug.soar.management.music.MusicManager;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.Icon;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import me.eldodebug.soar.utils.TimerUtils;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class MusicInfoMod extends SimpleHUDMod implements MusicManager.TrackInfoCallback {

    private static MusicInfoMod instance;
    private static final ResourceLocation PLACEHOLDER_IMAGE = new ResourceLocation("soar/music.png");
    private TimerUtils timer = new TimerUtils();
    private TimerUtils timer2 = new TimerUtils();

    private float addX;
    private boolean back;

    private BooleanSetting iconSetting = new BooleanSetting(TranslateText.ICON, this, true);

    private ComboSetting designSetting = new ComboSetting(TranslateText.DESIGN, this, TranslateText.SIMPLE,
            new ArrayList<>(Arrays.asList(
                    new Option(TranslateText.SIMPLE),
                    new Option(TranslateText.ADVANCED))));

    private long trackPosition = 0;
    private long trackDuration = 0;

    public MusicInfoMod() {
        super(TranslateText.MUSIC_INFO, TranslateText.MUSIC_INFO_DESCRIPTION);
        instance = this;
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        NanoVGManager nvg = Glide.getInstance().getNanoVGManager();
        Option option = designSetting.getOption();

        if (option.getTranslate().equals(TranslateText.SIMPLE)) {
            this.draw();
        } else if (option.getTranslate().equals(TranslateText.ADVANCED)) {
            nvg.setupAndDraw(this::drawAdvancedNanoVG);
        }
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        this.setDraggable(true);
    }

    private void drawAdvancedNanoVG() {
        MusicManager musicManager = Glide.getInstance().getMusicManager();

        this.drawBackground(155, 100);
        this.drawRect(0, 17.5F, 155, 1);

        if (musicManager.isPlaying() && musicManager.getCurrentTrack() != null) {
            Track currentTrack = musicManager.getCurrentTrack();

            // Draw album art
            String albumArtUrl = musicManager.getAlbumArtUrl(currentTrack);
            if (albumArtUrl != null && !albumArtUrl.isEmpty()) {
                File albumArtFile = new File(albumArtUrl);
                if (albumArtFile.exists()) {
                    this.drawRoundedImage(albumArtFile, 5.5F, 25F, 37, 37, 6);
                } else {
                    this.drawRoundedImage(PLACEHOLDER_IMAGE, 5.5F, 25F, 37, 37, 6);
                }
            } else {
                this.drawRoundedImage(PLACEHOLDER_IMAGE, 5.5F, 25F, 37, 37, 6);
            }

            this.save();
            this.scissor(0, 0, 155, 100);

            // Draw song name with scrolling
            this.drawText("Playing: " + currentTrack.getName(), 5.5F + addX, 6F, 10.5F, Fonts.MEDIUM);

            // Draw artist name
            String artistNames = String.join(", ", Arrays.stream(currentTrack.getArtists())
                    .map(artist -> artist.getName())
                    .toArray(String[]::new));
            this.drawText("By: " + artistNames, 47F, 35F, 9.5F, Fonts.MEDIUM);

            this.restore();

            float fontWidth = this.getTextWidth("Playing: " + currentTrack.getName(), 10.5F, Fonts.MEDIUM);

            handleScrollingText(fontWidth);

            float current = musicManager.getCurrentTime();
            float end = musicManager.getEndTime();

            // Format timestamps as MM:SS
            String currentTime = formatTime((long)current);
            String totalTime = formatTime((long)end);

            // Draw timestamps
            this.drawText(currentTime, 5.5F, 75F, 9F, Fonts.MEDIUM);
            this.drawText(totalTime, 125F, 75F, 9F, Fonts.MEDIUM);

            // Draw progress bar
            this.drawRoundedRect(6, 90.5F, (current / end) * 144 - 4, 2.5F, 1.3F);
            this.drawRoundedRect(6 + ((current / end) * 144) - 2.4F, 89.8F, 4, 4, 2);
            this.drawRoundedRect(6 + ((current / end) * 144) + 3, 90.5F, 142 - ((current / end) * 150), 2.5F, 1.3F);

        } else {
            this.drawText("Nothing is playing", 5.5F, 6F, 10.5F, Fonts.MEDIUM);
            this.drawRoundedImage(PLACEHOLDER_IMAGE, 5.5F, 25F, 37, 37, 6);
            this.drawRoundedRect(6, 90.5F, 145, 2.5F, 1.3F);
        }

        this.setWidth(155);
        this.setHeight(100);
    }

    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    private void handleScrollingText(float fontWidth) {
        if (fontWidth > this.getWidth()) {
            if (timer.delay(30)) {
                if (((this.getWidth()) - fontWidth) - 10 < addX && !back) {
                    addX = addX - 1;
                } else if (back && addX != 0) {
                    addX = addX + 1;
                }
                timer.reset();
            }

            if (addX <= ((this.getWidth()) - fontWidth) - 10) {
                if (timer2.delay(3000)) {
                    back = true;
                }
            } else {
                if (!back) {
                    timer2.reset();
                }
            }

            if (back) {
                if (addX == 0) {
                    if (timer2.delay(3000)) {
                        back = false;
                    }
                } else {
                    if (back) {
                        timer2.reset();
                    }
                }
            }
        } else {
            addX = 0;
            back = false;
        }
    }

    @Override
    public String getText() {
        MusicManager musicManager = Glide.getInstance().getMusicManager();

        if (musicManager.isPlaying()) {
            Track currentTrack = musicManager.getCurrentTrack();
            return currentTrack != null ? "Playing: " + currentTrack.getName() : "Nothing is Playing";
        } else {
            return "Nothing is Playing";
        }
    }

    @Override
    public String getIcon() {
        return iconSetting.isToggled() ? LegacyIcon.MUSIC : null;
    }

    @Override
    public void onTrackInfoUpdated(long position, long duration) {
        this.trackPosition = position;
        this.trackDuration = duration;
    }

    public static MusicInfoMod getInstance() {
        return instance;
    }

    public ComboSetting getDesignSetting() {
        return designSetting;
    }
}