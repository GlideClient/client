package me.eldodebug.soar.gui;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import eu.shoroa.contrib.render.ShBlur;
import me.eldodebug.soar.management.mods.impl.InternalSettingsMod;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.event.impl.EventRender2D;
import me.eldodebug.soar.management.event.impl.EventRenderNotification;
import me.eldodebug.soar.management.mods.HUDMod;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.utils.MathUtils;
import me.eldodebug.soar.utils.animation.normal.Animation;
import me.eldodebug.soar.utils.animation.normal.Direction;
import me.eldodebug.soar.utils.animation.normal.easing.EaseBackIn;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import me.eldodebug.soar.utils.render.BlurUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.nanovg.NanoVG;

public class GuiEditHUD extends GuiScreen {

	private Animation introAnimation;
	private boolean fromModMenu;
	private boolean snapping, canSnap;
	private ArrayList<HUDMod> mods;
	int localMouseX = -1, localMouseY = -1;
	
	public GuiEditHUD(boolean fromModMenu) {
		this.fromModMenu = fromModMenu;
		this.mods = Glide.getInstance().getModManager().getHudMods();
		Collections.reverse(mods);
	}
	
	@Override
	public void initGui() {
		
		for(HUDMod m : mods) {
			m.setDragging(false);
			m.getAnimation().setValue(0.0F);
		}
		
		introAnimation = new EaseBackIn(500, 1.0F, 0F);
		introAnimation.setDirection(Direction.FORWARDS);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		
		ScaledResolution sr = new ScaledResolution(mc);
		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		ColorPalette palette = instance.getColorManager().getPalette();
		boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
		localMouseX = mouseX;
		localMouseY = mouseY;

	    snapping = false;
	    
		if(introAnimation.isDone(Direction.BACKWARDS)) {
			mc.displayGuiScreen(null);
		}
		if (!InternalSettingsMod.getInstance().getBlurSetting().isToggled()) {
			BlurUtils.drawBlurScreen((float) (Math.min(introAnimation.getValue(), 1) * 20) + 1F);
		}
		
		nvg.setupAndDraw(() -> {

			nvg.save();
			NanoVG.nvgGlobalAlpha(nvg.getContext(), (float) introAnimation.getValue());
			if (InternalSettingsMod.getInstance().getBlurSetting().isToggled()) {
				ShBlur.getInstance().drawBlur(() -> nvg.drawRect(0,0, sr.getScaledWidth(), sr.getScaledHeight(), Color.WHITE));
			}
			nvg.restore();
			nvg.drawRect(0,0, sr.getScaledWidth(), sr.getScaledHeight(), new Color(0,0,0, (int) (introAnimation.getValue() * 100)));
			int halfScreenWidth = sr.getScaledWidth() / 2;
			int halfScreenHeight = sr.getScaledHeight() / 2;

			// guide lines
			nvg.drawRect(0, halfScreenHeight, sr.getScaledWidth(), 0.5F, palette.getBackgroundColor(ColorType.DARK));
			nvg.drawRect(halfScreenWidth, 0, 0.5F, sr.getScaledHeight(), palette.getBackgroundColor(ColorType.DARK));
			// todo add more splashers
			nvg.drawCenteredText("You can resize elements by scrolling over them. Use shift for more control.", sr.getScaledWidth() / 2F, sr.getScaledHeight() - 15, new Color(255,255,255, 200), 8F, Fonts.REGULAR);

			for(HUDMod m : mods) {
				
				if(m.isToggled() && !m.isHide()) {
					
					boolean isInside = MouseUtils.isInside(mouseX, mouseY, m.getX(), m.getY(), m.getWidth(), m.getHeight()) && mods.stream().filter(m2 -> m2.isToggled() && 
							MouseUtils.isInside(mouseX, mouseY, m2.getX(), m2.getY(), m2.getWidth(), m2.getHeight())).findFirst().get().equals(m);
					
					if(isInside) {
						
						int dWheel = Mouse.getDWheel();

						if (dWheel != 0) {
							float scaleChange = shift ? 0.02F : 0.1F;

							float newScale = m.getScale();

							if (dWheel > 0) {
								newScale += scaleChange;
							}

							if (dWheel < 0) {
								newScale -= scaleChange;
							}

							float roundedScale = Math.round(newScale * 100.0F) / 100.0F;

							m.setScale(roundedScale);
						}
					}
					if(shift) canSnap = false;
					m.getAnimation().setAnimation(isInside ? 1.0F : 0.0F, 14);
					
					if(m.isDragging()) {
						m.setX(mouseX + m.getDraggingX());
						m.setY(mouseY + m.getDraggingY());
					}
					
					int modX = m.getX();
					int modY = m.getY();
					int modWidth = m.getWidth();
					int modHeight = m.getHeight();
					
					int snapRange = 5;
					
					m.setX(Math.max(0, Math.min(modX, sr.getScaledWidth() - modWidth)));
					m.setY(Math.max(0, Math.min(modY, sr.getScaledHeight() - modHeight)));
					
					if(canSnap) {
				        if (MathUtils.isInRange(modX + (modWidth / 2f), halfScreenWidth - snapRange, halfScreenWidth + snapRange)) {
				            m.setX(halfScreenWidth - (modWidth / 2));
				        }

				        if (MathUtils.isInRange(modY + (modHeight / 2f), halfScreenHeight - snapRange, halfScreenHeight + snapRange)) {
				            m.setY(halfScreenHeight - (modHeight / 2));
				        }
					}
			        
					for(HUDMod m2 : instance.getModManager().getHudMods()) {
						
						if(m2.isToggled() && m.isDragging() && !m2.equals(m) && !snapping && canSnap ) {
							
							int mod2X = m2.getX();
							int mod2Y = m2.getY();
							int mod2Width = m2.getWidth();
							int mod2Height = m2.getHeight();
							
			                if (MathUtils.isInRange(mod2X, modX - snapRange, modX + snapRange)) {
			                	nvg.drawRect(mod2X, 0, 0.5F, sr.getScaledHeight(), new Color(217, 60, 255));
			                    snapping = true;
			                    m.setX(mod2X);
			                }

			                if (MathUtils.isInRange(mod2Y, modY - snapRange, modY + snapRange)) {
			                	nvg.drawRect(0, mod2Y, sr.getScaledWidth(), 0.5F, new Color(217, 60, 255));
			                    snapping = true;
			                    m.setY(mod2Y);
			                }

			                if (MathUtils.isInRange(mod2X + mod2Width, modX - snapRange, modX + snapRange)) {
			                	nvg.drawRect(mod2X + mod2Width, 0, 0.5F, sr.getScaledHeight(), new Color(217, 60, 255));
			                    snapping = true;
			                    m.setX(mod2X + mod2Width);
			                }

			                if (MathUtils.isInRange(mod2Y + mod2Height, modY - snapRange, modY + snapRange)) {
			                	nvg.drawRect(0, mod2Y + mod2Height, sr.getScaledWidth(), 0.5F, new Color(217, 60, 255));
			                    snapping = true;
			                    m.setY(mod2Y + mod2Height);
			                }

			                if (MathUtils.isInRange(mod2X, modX + modWidth - snapRange, modX + modWidth + snapRange)) {
			                	nvg.drawRect(mod2X, 0, 0.5F, sr.getScaledHeight(), new Color(217, 60, 255));
			                    snapping = true;
			                    m.setX(mod2X - modWidth);
			                }

			                if (MathUtils.isInRange(mod2Y, modY + modHeight - snapRange, modY + modHeight + snapRange)) {
			                	nvg.drawRect(0, mod2Y, sr.getScaledWidth(), 0.5F, new Color(217, 60, 255));
			                    snapping = true;
			                    m.setY(mod2Y - modHeight);
			                }

			                if (MathUtils.isInRange(mod2X + mod2Width, modX + modWidth - snapRange, modX + modWidth + snapRange)) {
			                	nvg.drawRect(mod2X + mod2Width, 0, 0.5F, sr.getScaledHeight(), new Color(217, 60, 255));
			                    snapping = true;
			                    m.setX(mod2X + mod2Width - modWidth);
			                }
			                
			                if (MathUtils.isInRange(mod2Y + mod2Height, modY + modHeight - snapRange, modY + modHeight + snapRange)) {
			                    nvg.drawRect(0, mod2Y + mod2Height, sr.getScaledWidth(), 0.5F, new Color(217, 60, 255));
			                    snapping = true;
			                    m.setY(mod2Y + mod2Height - modHeight);
			                }
						}
					}
				}
				
				nvg.drawOutlineRoundedRect(m.getX() - 2, m.getY() - 2, m.getWidth() + 4, m.getHeight() + 4, 6.5F * m.getScale(), 2, palette.getBackgroundColor(ColorType.DARK, (int) (m.getAnimation().getValue() * 255)));
			}
		});
		
		new EventRender2D(partialTicks).call();
		new EventRenderNotification().call();
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		
		for(HUDMod m : mods) {
			if(m.isToggled() && !m.isHide()) {
				
				boolean isInside = MouseUtils.isInside(mouseX, mouseY, m.getX(), m.getY(), m.getWidth(), m.getHeight()) && mods.stream().filter(m2 -> m2.isToggled() && 
						MouseUtils.isInside(mouseX, mouseY, m2.getX(), m2.getY(), m2.getWidth(), m2.getHeight())).findFirst().get().equals(m);
				
				if(mouseButton == 0) {
					canSnap = true;
				}

				// right click to remove
				if(mouseButton == 1) {
					if(isInside) {
						m.toggle();
						initGui();
						return;
					}
				}
				// middle click resets scale
				if(mouseButton == 2 && isInside) {
					m.setScale(1.0F);
				}
				
				if(isInside) {
					m.setDragging(true);
					m.setDraggingX(m.getX() - mouseX);
					m.setDraggingY(m.getY() - mouseY);
				}
			}
		}
		
		super.drawScreen(mouseX, mouseY, mouseButton);
	}
	
	@Override
	public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		
		for(HUDMod m : mods) {
			m.setDragging(false);
		}
		
		try {
			super.mouseClicked(mouseX, mouseY, mouseButton);
		} catch (IOException e) {}
	}
	
	@Override
	public void keyTyped(char typedChar, int keyCode) {
		if(keyCode == Keyboard.KEY_ESCAPE) {
			if(fromModMenu) {
				mc.displayGuiScreen(Glide.getInstance().getModMenu());
			}else {
				introAnimation.setDirection(Direction.BACKWARDS);
			}
		}
		for(HUDMod m : mods) {
			if(m.isToggled() && !m.isHide()) {

				boolean isInside = MouseUtils.isInside(localMouseX, localMouseY, m.getX(), m.getY(), m.getWidth(), m.getHeight()) && mods.stream().filter(m2 -> m2.isToggled() &&
						MouseUtils.isInside(localMouseX, localMouseY, m2.getX(), m2.getY(), m2.getWidth(), m2.getHeight())).findFirst().get().equals(m);

				// backspace to remove
				if(keyCode == Keyboard.KEY_BACK || keyCode == Keyboard.KEY_DELETE) {
					if(isInside) {
						m.toggle();
						initGui();
						return;
					}
				}
			}
		}
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
}
