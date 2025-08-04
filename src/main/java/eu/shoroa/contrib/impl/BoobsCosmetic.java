package eu.shoroa.contrib.impl;

import eu.shoroa.contrib.cosmetic.Cosmetic;
import eu.shoroa.contrib.cosmetic.PositionType;
import eu.shoroa.contrib.debug.Debug3DRenderer;
import me.eldodebug.soar.injection.interfaces.IMixinMinecraft;
import me.eldodebug.soar.management.mods.impl.FemaleGenderMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

public class BoobsCosmetic extends Cosmetic {
    private final Model model = new Model();

    public BoobsCosmetic() {
        super("Boobs", PositionType.LOCAL);
    }

    @Override
    public void render(AbstractClientPlayer entityPlayer, float handSwing, float handSwingAmount, float ticks, float age, float headYaw, float headPitch, float scale) {
        model.render(entityPlayer, handSwing, handSwingAmount, ticks, age, headYaw, headPitch);
    }

    private static class Model extends ModelBase {
        private final Minecraft mc = Minecraft.getMinecraft();
        private final ModelRenderer bone;
        private final ModelRenderer leftBone;
        private final ModelRenderer rightBone;

        private float prevMotionY = 0f;
        private float jiggleAmount = 0f;
        private float jiggleVelocity = 0f;

        public Model() {
            textureWidth = 64;
            textureHeight = 64;

            bone = new ModelRenderer(this);
            bone.setRotationPoint(0.0F, 24.0F, 0.0F);

            leftBone = new ModelRenderer(this);
            leftBone.setRotationPoint(0.0F, -4.0F, 0.0F);
            bone.addChild(leftBone);
            leftBone.cubeList.add(new ModelBox(leftBone, 20, 20, 0.0F, 0.0F, 0.0F, 4, 4, 4, 0.0F, false));

            rightBone = new ModelRenderer(this);
            rightBone.setRotationPoint(0.0F, -4.0F, 0.0F);
            bone.addChild(rightBone);
            rightBone.cubeList.add(new ModelBox(rightBone, 16, 20, -4.0F, 0.0F, 0.0F, 4, 4, 4, 0.0F, false));
        }

        private void updatePhysics(Entity entity) {
            FemaleGenderMod modInstance = FemaleGenderMod.getInstance();

            float partialTicks = ((IMixinMinecraft) mc).getTimer().renderPartialTicks;
            float deltaTime = (partialTicks / 500);

            float motionY = (float) entity.motionY;
            float verticalAcceleration = (motionY - prevMotionY) / deltaTime;
            prevMotionY = motionY;

            float force = verticalAcceleration * modInstance.getAccelerationMultiplier();
            jiggleVelocity += force;

            float damping = modInstance.getDamping();
            float springStrength = modInstance.getSpringStrength();

            jiggleVelocity -= jiggleAmount * springStrength;
            jiggleVelocity *= damping;
            jiggleAmount += jiggleVelocity * deltaTime * modInstance.getJiggleMultiplier();

            if (Float.isNaN(prevMotionY) || Float.isInfinite(prevMotionY)) {
                prevMotionY = 0f;
            }

            if (Float.isNaN(jiggleVelocity) || Float.isInfinite(jiggleVelocity)) {
                jiggleVelocity = 0f;
            }

            if (Float.isNaN(jiggleAmount) || Float.isInfinite(jiggleAmount)) {
                jiggleAmount = 0f;
            }

            jiggleAmount = Math.max(-1f, Math.min(1f, jiggleAmount));
        }

        @Override
        public void render(Entity entity, float handSwing, float handSwingAmount, float ticks, float age, float headYaw, float headPitch) {
            if (!(entity instanceof AbstractClientPlayer)) {
                return;
            }

            AbstractClientPlayer player = (AbstractClientPlayer) entity;

            if (player.getCurrentArmor(2) != null) {
                return;
            }

            ModelPlayer playerModel = (ModelPlayer) ((RendererLivingEntity<?>) mc.getRenderManager().getEntityRenderObject(player)).getMainModel();
            float partialTicks = ((IMixinMinecraft) mc).getTimer().renderPartialTicks;

            double posX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
            double posY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
            double posZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

            updatePhysics(player);

            Debug3DRenderer.drawCoordinateAxis(
                    new Vec3(posX, posY, posZ),
                    0.5F
            );

            bone.offsetY = player.isSneaking() ? 0.25f : 0f;

            bone.offsetZ = -0.115f;

            copyModelAngles(playerModel.bipedBody, bone);
            bone.rotateAngleX += 1f + jiggleAmount / 8;

            leftBone.rotationPointY = 0f;
            leftBone.rotationPointZ = -4f;

            rightBone.rotationPointY = 0f;
            rightBone.rotationPointZ = -4f;

            ResourceLocation skinLocation = player.getLocationSkin();
            mc.getTextureManager().bindTexture(skinLocation);

            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 771);

            bone.render(0.0625F);

            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }

        public static void copyModelAngles(ModelRenderer source, ModelRenderer dest) {
            dest.rotateAngleX = source.rotateAngleX;
            dest.rotateAngleY = source.rotateAngleY;
            dest.rotateAngleZ = source.rotateAngleZ;
            dest.rotationPointX = source.rotationPointX + dest.offsetX;
            dest.rotationPointY = source.rotationPointY + dest.offsetY;
            dest.rotationPointZ = source.rotationPointZ + dest.offsetZ;
        }
    }
}
