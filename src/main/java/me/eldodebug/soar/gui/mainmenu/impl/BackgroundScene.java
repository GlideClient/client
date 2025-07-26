package me.eldodebug.soar.gui.mainmenu.impl;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import me.eldodebug.soar.utils.animation.normal.Animation;
import me.eldodebug.soar.utils.animation.normal.Direction;
import me.eldodebug.soar.utils.animation.normal.easing.EaseInOutCirc;
import me.eldodebug.soar.utils.buffer.ScreenAnimation;
import org.lwjgl.input.Keyboard;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.gui.mainmenu.GuiGlideMainMenu;
import me.eldodebug.soar.gui.mainmenu.MainMenuScene;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.file.FileManager;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import me.eldodebug.soar.management.profile.mainmenu.BackgroundManager;
import me.eldodebug.soar.management.profile.mainmenu.impl.Background;
import me.eldodebug.soar.management.profile.mainmenu.impl.CustomBackground;
import me.eldodebug.soar.management.profile.mainmenu.impl.DefaultBackground;
import me.eldodebug.soar.management.profile.mainmenu.impl.ShaderBackground;
import me.eldodebug.soar.management.shader.ShaderBackgroundRenderer;
import me.eldodebug.soar.utils.Multithreading;
import me.eldodebug.soar.utils.file.FileUtils;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import me.eldodebug.soar.utils.mouse.Scroll;
import net.minecraft.client.gui.ScaledResolution;

public class BackgroundScene extends MainMenuScene {

	private Animation introAnimation;
	private final ScreenAnimation screenAnimation = new ScreenAnimation();
	private Scroll scroll = new Scroll();

	public BackgroundScene(GuiGlideMainMenu parent) {super(parent);}

	@Override
	public void initScene() {
		introAnimation = new EaseInOutCirc(250, 1.0F);
		introAnimation.setDirection(Direction.FORWARDS);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		ScaledResolution sr = new ScaledResolution(mc);
		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		screenAnimation.wrap(() -> drawNanoVG(mouseX, mouseY, sr, instance, nvg), 0, 0, sr.getScaledWidth(), sr.getScaledHeight(), 2 - introAnimation.getValueFloat(), Math.min(introAnimation.getValueFloat(), 1), false);
		if(introAnimation.isDone(Direction.BACKWARDS)) {
			this.setCurrentScene(this.getSceneByClass(MainScene.class));
		}
	}

	private void drawNanoVG(int mouseX, int mouseY, ScaledResolution sr, Glide instance, NanoVGManager nvg) {
		BackgroundManager backgroundManager = instance.getProfileManager().getBackgroundManager();
		ColorPalette palette = instance.getColorManager().getPalette();

		int acWidth = 240;
		int acHeight = 148;
		int acX = sr.getScaledWidth() / 2 - (acWidth / 2);
		int acY = sr.getScaledHeight() / 2 - (acHeight / 2);
		int offsetX = 0;
		int offsetY = 0;
		int index = 1;
		int prevIndex = 1;

		scroll.onScroll();
		scroll.onAnimation();

		nvg.drawRoundedRect(acX, acY, acWidth, acHeight, 8, this.getBackgroundColor());
		nvg.drawCenteredText(TranslateText.SELECT_BACKGROUND.getText(), acX + (acWidth / 2), acY + 8, Color.WHITE, 14, Fonts.SEMIBOLD);

		nvg.save();
		nvg.scissor(acX, acY + 25, acWidth, acHeight - 25);
		nvg.translate(0, scroll.getValue());

		for(Background bg : backgroundManager.getBackgrounds()) {
			boolean isSelected = backgroundManager.getCurrentBackground().equals(bg);
			float itemX = acX + 11 + offsetX;
			float itemY = acY + 35 + offsetY;
			float itemWidth = 102.5F;
			float itemHeight = 57.5F;

			// Draw selection highlight and glow effect
			if(isSelected) {
				// Outer glow
				nvg.drawGradientShadow(itemX - 1, itemY - 1, itemWidth + 2, itemHeight + 2, 7, new Color(255, 255, 255, 180),new Color(255, 255, 255, 180));
				// Inner highlight
				nvg.drawRoundedRect(itemX - 1, itemY - 1, itemWidth + 2, itemHeight + 2, 7, new Color(255, 255, 255, 180));
			}

			// Hover effect
			if(MouseUtils.isInside(mouseX, mouseY, itemX, itemY + scroll.getValue(), itemWidth, itemHeight)) {
				nvg.drawRoundedRect(itemX - 1, itemY - 1, itemWidth + 2, itemHeight + 2, 7, new Color(255, 255, 255, 100));
			}

			if(bg instanceof DefaultBackground) {
				DefaultBackground defBackground = (DefaultBackground) bg;

				if(bg.getId() == 999) {
					nvg.drawRoundedRect(acX + 11 + offsetX, acY + 35 + offsetY, 102.5F, 57.5F, 6, Color.BLACK);
					nvg.drawCenteredText(LegacyIcon.PLUS, acX + 10 + offsetX + (102.5F / 2), acY + 42.5F + offsetY, Color.WHITE, 26, Fonts.LEGACYICON);
				} else {
					nvg.drawRoundedImage(defBackground.getImage(), acX + 11 + offsetX, acY + 35 + offsetY, 102.5F, 57.5F, 6);
				}
			}

			if(bg instanceof CustomBackground) {
				CustomBackground cusBackground = (CustomBackground) bg;

				cusBackground.getTrashAnimation().setAnimation(MouseUtils.isInside(mouseX, mouseY, acX + 11 + offsetX, acY + 35 + offsetY + scroll.getValue(), 102.5F, 57.5F) ? 1.0F : 0.0F, 16);

				nvg.drawRoundedImage(cusBackground.getImage(), acX + 11 + offsetX, acY + 35 + offsetY, 102.5F, 57.5F, 6);
				nvg.drawText(LegacyIcon.TRASH, acX + offsetX + 100, acY + 38 + offsetY, palette.getMaterialRed((int) (cusBackground.getTrashAnimation().getValue() * 255)), 10, Fonts.LEGACYICON);
			}

			if(bg instanceof ShaderBackground) {
				ShaderBackground shaderBackground = (ShaderBackground) bg;

				// Draw a preview of the shader background
				ShaderBackgroundRenderer.renderShaderPreview(nvg, shaderBackground.getShaderFile(), 
					acX + 11 + offsetX, acY + 35 + offsetY, 102.5F, 57.5F);
			}

			nvg.drawRoundedRectVarying(acX + offsetX + 11, acY + offsetY + 76.5F, 102.5F, 16, 0, 0, 6, 6, this.getBackgroundColor());
			nvg.drawCenteredText(bg.getName(), acX + offsetX + 11 + (102.5F / 2), acY + offsetY + 80, Color.WHITE, 10, Fonts.REGULAR);

			offsetX+=115;

			if(index % 2 == 0) {
				offsetY+=70;
				offsetX = 0;
				prevIndex++;
			}

			index++;
		}

		nvg.restore();

		scroll.setMaxScroll(prevIndex == 1 ? 0 : offsetY - (70 / 1.56F) - (index % 2 == 1 ? 70 : 0));
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		ScaledResolution sr = new ScaledResolution(mc);

		Glide instance = Glide.getInstance();
		FileManager fileManager = instance.getFileManager();
		BackgroundManager backgroundManager = instance.getProfileManager().getBackgroundManager();

		int acWidth = 240;
		int acHeight = 148;
		int acX = sr.getScaledWidth() / 2 - (acWidth / 2);
		int acY = sr.getScaledHeight() / 2 - (acHeight / 2);
		int offsetX = 0;
		int offsetY = (int) (0 + scroll.getValue());
		int index = 1;

		if(!MouseUtils.isInside(mouseX, mouseY, acX, acY, acWidth, acHeight) && !MouseUtils.isInside(mouseX, mouseY, sr.getScaledWidth() - 28 - 28, 6, 22, 22)){
			introAnimation.setDirection(Direction.BACKWARDS);
		}

		for(Background bg : backgroundManager.getBackgrounds()) {

			if(mouseButton == 0) {
				if(MouseUtils.isInside(mouseX, mouseY, acX + 11 + offsetX, acY + 35 + offsetY, 102.5F, 57.5F)) {

					if(bg.getId() == 999) {
						Multithreading.runAsync(() -> {
							File file = FileUtils.selectImageFile();
							File bgCacheDir = new File(fileManager.getCacheDir(), "background");

							if (file != null && bgCacheDir.exists() && file.exists() && FileUtils.getExtension(file).equals("png")) {
								File destFile = new File(bgCacheDir, file.getName());

								try {
									FileUtils.copyFile(file, destFile);
									backgroundManager.addCustomBackground(destFile);
								} catch (IOException e) {
								}
							}
						});
					} else {
						backgroundManager.setCurrentBackground(bg);
					}
				}

				if(bg instanceof CustomBackground && MouseUtils.isInside(mouseX, mouseY, acX + offsetX + 98, acY + 35.5F + offsetY, 14, 14)) {
					CustomBackground cusBackground = (CustomBackground) bg;

					if(backgroundManager.getCurrentBackground().equals(cusBackground)) {
						backgroundManager.setCurrentBackground(backgroundManager.getBackgroundById(0));
					}

					backgroundManager.removeCustomBackground(cusBackground);
				}
			}

			offsetX+=115;

			if(index % 2 == 0) {
				offsetY+=70;
				offsetX = 0;
			}

			index++;
		}
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) {
		if(keyCode == Keyboard.KEY_ESCAPE) {
			introAnimation.setDirection(Direction.BACKWARDS);
		}
	}
}