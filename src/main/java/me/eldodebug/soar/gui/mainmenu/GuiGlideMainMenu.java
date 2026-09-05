package me.eldodebug.soar.gui.mainmenu;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;

import eu.shoroa.contrib.render.Blur;
import me.eldodebug.soar.GlideMeta;
import me.eldodebug.soar.gui.mainmenu.impl.UpdateScene;
import me.eldodebug.soar.gui.mainmenu.impl.welcome.*;
import me.eldodebug.soar.gui.mainmenu.widget.WidgetMenuIconButton;
import me.eldodebug.soar.types.Rect;
import me.eldodebug.soar.types.Size;
import me.eldodebug.soar.utils.Sound;
import org.lwjgl.input.Mouse;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.gui.mainmenu.impl.BackgroundScene;
import me.eldodebug.soar.gui.mainmenu.impl.MainScene;
import me.eldodebug.soar.management.event.impl.EventRenderNotification;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import me.eldodebug.soar.management.profile.mainmenu.impl.Background;
import me.eldodebug.soar.management.profile.mainmenu.impl.CustomBackground;
import me.eldodebug.soar.management.profile.mainmenu.impl.DefaultBackground;
import me.eldodebug.soar.utils.animation.normal.Animation;
import me.eldodebug.soar.utils.animation.normal.Direction;
import me.eldodebug.soar.utils.animation.normal.other.DecelerateAnimation;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.nanovg.NanoVG;

public class GuiGlideMainMenu extends GuiScreen {

    private MainMenuScene currentScene;

    private SimpleAnimation closeFocusAnimation = new SimpleAnimation();
    private SimpleAnimation backgroundSelectFocusAnimation = new SimpleAnimation();
    private SimpleAnimation[] backgroundAnimations = new SimpleAnimation[2];

    private ArrayList<MainMenuScene> scenes = new ArrayList<MainMenuScene>();
    boolean soundPlayed = false;

    private Animation fadeIconAnimation, fadeBackgroundAnimation;

    private Rect screenRect = new Rect();
    private Rect backgroundRect = new Rect();
    private Size backgroundSize = new Size();

    private WidgetMenuIconButton backgroundsButton = new WidgetMenuIconButton(Fonts.LEGACYICON, LegacyIcon.IMAGE, new me.eldodebug.soar.types.Color(0x7837FF37), 0, 0, 22, 22, () -> {
        if (!this.getCurrentScene().equals(getSceneByClass(BackgroundScene.class)))
            this.setCurrentScene(this.getSceneByClass(BackgroundScene.class));
    });

    private WidgetMenuIconButton closeButton = new WidgetMenuIconButton(Fonts.LEGACYICON, LegacyIcon.X, new me.eldodebug.soar.types.Color(0x78FF3737), 0, 0, 22, 22, () -> {
        mc.shutdown();
    });

    public GuiGlideMainMenu() {

        Glide instance = Glide.getInstance();

        for (int i = 0; i < backgroundAnimations.length; i++) {
            backgroundAnimations[i] = new SimpleAnimation();
        }

        scenes.add(new MainScene(this));
        scenes.add(new BackgroundScene(this));
        scenes.add(new WelcomeMessageScene(this));
        scenes.add(new ThemeSelectScene(this));
        scenes.add(new LanguageSelectScene(this));
        scenes.add(new AccentColorSelectScene(this));
        scenes.add(new LastMessageScene(this));
        scenes.add(new UpdateScene(this));

        if (instance.isFirstLogin()) {
            mc.gameSettings.useVbo = true;
            currentScene = getSceneByClass(WelcomeMessageScene.class);
        } else {
            if (instance.getUpdateNeeded()) {
                currentScene = getSceneByClass(UpdateScene.class);
            } else {
                currentScene = getSceneByClass(MainScene.class);
            }
        }
    }

    @Override
    public void initGui() {
        currentScene.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        ScaledResolution sr = new ScaledResolution(mc);

        Glide instance = Glide.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        boolean isFirstLogin = instance.isFirstLogin();

        backgroundAnimations[0].setAnimation(Mouse.getX(), 16);
        backgroundAnimations[1].setAnimation(Mouse.getY(), 16);

        screenRect.set(-21 + backgroundAnimations[0].getValue() / 90, backgroundAnimations[1].getValue() * -1 / 90, sr.getScaledWidth() + 21, sr.getScaledHeight() + 20);

        nvg.setupAndDraw(() -> {
            Background currentBackground = instance.getProfileManager().getBackgroundManager().getCurrentBackground();

            if (currentBackground instanceof DefaultBackground) {

                DefaultBackground bg = (DefaultBackground) currentBackground;
                if (nvg.getAssetManager().loadImage(nvg.getContext(), bg.getImage())) {
                    nvg.imageSize(nvg.getAssetManager().getImage(bg.getImage()), backgroundSize);
                    Rect.cover(backgroundSize, screenRect, backgroundRect);

                    nvg.drawImage(bg.getImage(), backgroundRect);
                }
            } else if (currentBackground instanceof CustomBackground) {
                CustomBackground bg = (CustomBackground) currentBackground;
                if (nvg.getAssetManager().loadImage(nvg.getContext(), bg.getImage())) {
                    nvg.imageSize(nvg.getAssetManager().getImage(bg.getImage()), backgroundSize);
                    Rect.cover(backgroundSize, screenRect, backgroundRect);

                    nvg.drawImage(bg.getImage(), backgroundRect);
                }
            }
        });

        Blur.render(5f);

        nvg.setupAndDraw(() -> {

            drawNanoVG(sr, instance, nvg);

            if (!isFirstLogin) {
                drawButtons(mouseX, mouseY, sr, nvg);
            }
        });

        if (currentScene != null) {
            currentScene.drawScreen(mouseX, mouseY, partialTicks);
        }

        if (fadeBackgroundAnimation == null || (fadeBackgroundAnimation != null && !fadeBackgroundAnimation.isDone(Direction.FORWARDS))) {
            nvg.setupAndDraw(() -> drawSplashScreen(sr, nvg));
            if (!soundPlayed) {
                Sound.play("soar/audio/start.wav", true);
                soundPlayed = true;
            }
        }

        nvg.setupAndDraw(() -> {
            new EventRenderNotification().call();
        });

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawNanoVG(ScaledResolution sr, Glide instance, NanoVGManager nvg) {

        String copyright = "Copyright Mojang AB. Do not distribute!";
        nvg.drawBlurredText(copyright, sr.getScaledWidth() - (nvg.getTextWidth(copyright, 9, Fonts.REGULAR)) - 4, sr.getScaledHeight() - 12, Color.BLACK, 4f, 9, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_TOP, Fonts.REGULAR);
        nvg.drawText(copyright, sr.getScaledWidth() - (nvg.getTextWidth(copyright, 9, Fonts.REGULAR)) - 4, sr.getScaledHeight() - 12, Color.WHITE, 9, Fonts.REGULAR);
        nvg.drawBlurredText("GlideClient v" + GlideMeta.VERSION_NUMBER, 4, sr.getScaledHeight() - 12, Color.BLACK, 4f, 9, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_TOP, Fonts.REGULAR);
        nvg.drawText("GlideClient v" + GlideMeta.VERSION_NUMBER, 4, sr.getScaledHeight() - 12, Color.WHITE, 9, Fonts.REGULAR);
    }

    private void drawButtons(int mouseX, int mouseY, ScaledResolution sr, NanoVGManager nvg) {
        closeButton.setPosition(sr.getScaledWidth() - 28, 6);
        backgroundsButton.setPosition(sr.getScaledWidth() - 28 - 28, 6);

        closeButton.render(nvg, mouseX, mouseY);
        backgroundsButton.render(nvg, mouseX, mouseY);
    }

    private void drawSplashScreen(ScaledResolution sr, NanoVGManager nvg) {

        if (fadeIconAnimation == null) {
            fadeIconAnimation = new DecelerateAnimation(100, 1);
            fadeIconAnimation.setDirection(Direction.FORWARDS);
            fadeIconAnimation.reset();
        }

        if (fadeIconAnimation != null) {

            if (fadeIconAnimation.isDone(Direction.FORWARDS) && fadeBackgroundAnimation == null) {
                fadeBackgroundAnimation = new DecelerateAnimation(500, 1);
                fadeBackgroundAnimation.setDirection(Direction.FORWARDS);
                fadeBackgroundAnimation.reset();
            }

            nvg.drawRect(0, 0, sr.getScaledWidth(), sr.getScaledHeight(), new Color(0, 0, 0, fadeBackgroundAnimation != null ? (int) (255 - (fadeBackgroundAnimation.getValue() * 255)) : 255));
            nvg.drawCenteredText(LegacyIcon.SOAR, sr.getScaledWidth() / 2, (sr.getScaledHeight() / 2) - (nvg.getTextHeight(LegacyIcon.SOAR, 130, Fonts.LEGACYICON) / 2) - 1, new Color(255, 255, 255, (int) (255 - (fadeIconAnimation.getValue() * 255))), 130, Fonts.LEGACYICON);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

        ScaledResolution sr = new ScaledResolution(mc);

        Glide instance = Glide.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        boolean isFirstLogin = instance.isFirstLogin();

        if (!isFirstLogin) {
            closeButton.mouseClicked(mouseX, mouseY, mouseButton);
            backgroundsButton.mouseClicked(mouseX, mouseY, mouseButton);
        }

        currentScene.mouseClicked(mouseX, mouseY, mouseButton);
        try {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        } catch (IOException e) {
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (!Glide.getInstance().isFirstLogin()) {
            closeButton.mouseReleased(mouseX, mouseY, mouseButton);
            backgroundsButton.mouseReleased(mouseX, mouseY, mouseButton);
        }

        currentScene.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        currentScene.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleInput() throws IOException {
        super.handleInput();
    }

    @Override
    public void onGuiClosed() {
        currentScene.onGuiClosed();
    }

    public MainMenuScene getCurrentScene() {
        return currentScene;
    }

    public void setCurrentScene(MainMenuScene currentScene) {

        if (this.currentScene != null) {
            this.currentScene.onSceneClosed();
        }

        this.currentScene = currentScene;

        if (this.currentScene != null) {
            this.currentScene.initScene();
        }
    }

    public boolean isDoneBackgroundAnimation() {
        return fadeBackgroundAnimation != null && fadeBackgroundAnimation.isDone(Direction.FORWARDS);
    }

    public MainMenuScene getSceneByClass(Class<? extends MainMenuScene> clazz) {

        for (MainMenuScene s : scenes) {
            if (s.getClass().equals(clazz)) {
                return s;
            }
        }

        return null;
    }

    public Color getBackgroundColor() {
        return new Color(230, 230, 230, 120);
    }
}