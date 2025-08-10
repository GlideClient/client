package eu.shoroa.contrib.impl;

import eu.shoroa.contrib.cosmetic.Cosmetic;
import eu.shoroa.contrib.cosmetic.PositionType;
import eu.shoroa.contrib.util.ShMath;
import me.eldodebug.soar.injection.interfaces.IMixinMinecraft;
import me.eldodebug.soar.utils.vector.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import static java.lang.Math.PI;
import static me.eldodebug.soar.utils.MathUtils.sin;

public class DoubleHaloCosmetic extends Cosmetic {
    private final Model model;

    public DoubleHaloCosmetic(String name, String texturePath) {
        super(name, PositionType.WORLD);
        model = new Model(texturePath);
    }

    @Override
    public void render(AbstractClientPlayer entityPlayer) {
        model.render(entityPlayer, 0f, 0f, 0f, 0f, 0f, 0f);
    }

    private static class Model extends ModelBase {
        private final ModelRenderer bone;
        private final ModelRenderer bone2;
        private final ResourceLocation textureFull;

        // Track previous positions for interpolation
        private Vector3f lastPos = Vector3f.ZERO;
        private float smoothYaw = 0f;

        public Model(String texturePath) {
            textureFull = new ResourceLocation(texturePath);
            textureWidth = 64;
            textureHeight = 64;

            bone = new ModelRenderer(this);
            bone.setRotationPoint(-4.0f, 26.0f, 4.0f);
            setRotationAngle(bone);
            bone.cubeList.add(new ModelBox(bone, -32, 0, -12.0f, -9.0f, -20.0f, 32, 0, 32, 0.0f, false));

            bone2 = new ModelRenderer(this);
            bone2.setRotationPoint(-4.0f, 26.0f, 4.0f);
            setRotationAngle(bone2);
            bone2.cubeList.add(new ModelBox(bone2, -32, 32, -12.0f, -6.0f, -20.0f, 32, 0, 32, 0.0f, false));
        }

        @Override
        public void render(Entity entity, float handSwing, float handSwingAmount, float tick, float age, float headPitch, float scale) {
            IMixinMinecraft imm = ((IMixinMinecraft) Minecraft.getMinecraft());
            float DELTA = imm.getTimer().renderPartialTicks / 100;
            float pt = imm.getTimer().renderPartialTicks;

            Vector3f currentPos = new Vector3f(
                    (float) (entity.prevPosX + (entity.posX - entity.prevPosX) * pt),
                    (float) (entity.prevPosY + (entity.posY - entity.prevPosY) * pt),
                    (float) (entity.prevPosZ + (entity.posZ - entity.prevPosZ) * pt)
            );

            if (lastPos.x() == 0f && lastPos.y() == 0f && lastPos.z() == 0f) {
                lastPos = currentPos;
            }

            smoothYaw = ShMath.interpolate(smoothYaw, entity.rotationYaw, DELTA * 8f);
            if (Float.isNaN(smoothYaw) || Float.isInfinite(smoothYaw)) {
                smoothYaw = entity.rotationYaw;
            }

            float lerpFactor = 10f;

            lastPos = new Vector3f(
                    ShMath.interpolate(lastPos.x(), currentPos.x(), DELTA * lerpFactor),
                    ShMath.interpolate(lastPos.y(), currentPos.y(), DELTA * lerpFactor),
                    ShMath.interpolate(lastPos.z(), currentPos.z(), DELTA * lerpFactor)
            );

            if (Float.isNaN(lastPos.x()) || Float.isInfinite(lastPos.x()) || Float.isNaN(lastPos.y()) || Float.isInfinite(lastPos.y()) || Float.isNaN(lastPos.z()) || Float.isInfinite(lastPos.z())) {
                lastPos = currentPos;
            }

            float constScale = 0.0625f;
            float haloScale = .45f;

            long timer = System.currentTimeMillis() % 2500L;
            float ease = (float) (sin((double) timer / 2500L * PI * 2) * 0.5 + 0.5);

            float tx = ShMath.clamp((-currentPos.x() + lastPos.x()), -5f, 5f);
            float ty = ShMath.clamp((-currentPos.y() + lastPos.y()), -5f, 5f);
            float tz = ShMath.clamp((-currentPos.z() + lastPos.z()), -5f, 5f);

            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.translate(tx, ty, tz);
            GlStateManager.rotate(180f, 1f, 0f, 0f);
            GlStateManager.rotate(smoothYaw, 0f, 1f, 0f);
            GlStateManager.translate(0f, -2.17f + .06f * ease, 0f);
            GlStateManager.scale(haloScale, haloScale, haloScale);
            Minecraft.getMinecraft().getTextureManager().bindTexture(textureFull);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glCullFace(GL11.GL_NONE);
//            bone.render(constScale);
            renderHaloQuad(1.0f, 0, 0, 32, 32);
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.translate(tx, ty, tz);
            GlStateManager.rotate(180f, 1f, 0f, 0f);
            GlStateManager.rotate(smoothYaw, 0f, 1f, 0f);
            GlStateManager.translate(0f, -2.1f + .1f * ease, 0f);
            GlStateManager.scale(haloScale, haloScale, haloScale);
            Minecraft.getMinecraft().getTextureManager().bindTexture(textureFull);
//            bone2.render(constScale);
            renderHaloQuad(1.0f, 0, 32, 32, 32);
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glCullFace(GL11.GL_BACK);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }

        private void setRotationAngle(ModelRenderer modelRenderer) {
            modelRenderer.rotateAngleX = (float) -0.2618;
            modelRenderer.rotateAngleY = (float) 0.0;
            modelRenderer.rotateAngleZ = (float) 0.0;
        }

        private void renderHaloQuad(float size, int textureOffsetX, int textureOffsetY, int width, int height) {
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();

            float u1 = (float) textureOffsetX / textureWidth;
            float v1 = (float) textureOffsetY / textureHeight;
            float u2 = (float) (textureOffsetX + width) / textureWidth;
            float v2 = (float) (textureOffsetY + height) / textureHeight;

            worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            worldrenderer.pos(-size, 0, -size).tex(u1, v1).endVertex();
            worldrenderer.pos(-size, 0, size).tex(u1, v2).endVertex();
            worldrenderer.pos(size, 0, size).tex(u2, v2).endVertex();
            worldrenderer.pos(size, 0, -size).tex(u2, v1).endVertex();
            tessellator.draw();
        }
    }
}
