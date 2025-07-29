package eu.shoroa.contrib.debug;

import me.eldodebug.soar.injection.interfaces.IMixinMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Debug3DRenderer {
    private static final List<DebugLine> lines = new ArrayList<>();
    private static final List<DebugBox> boxes = new ArrayList<>();
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void drawLine(Vec3 start, Vec3 end, float red, float green, float blue, float alpha) {
        lines.add(new DebugLine(start, end, red, green, blue, alpha));
    }

    public static void drawBox(Vec3 min, Vec3 max, float red, float green, float blue, float alpha) {
        boxes.add(new DebugBox(min, max, red, green, blue, alpha));
    }

    public static void renderAll() {
        if (mc.theWorld == null || mc.getRenderViewEntity() == null) return;

        Entity viewer = mc.getRenderViewEntity();
        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * ((IMixinMinecraft)mc).getTimer().renderPartialTicks;
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * ((IMixinMinecraft)mc).getTimer().renderPartialTicks;
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * ((IMixinMinecraft)mc).getTimer().renderPartialTicks;

        float prevLW = GL11.glGetFloat(GL11.GL_LINE_WIDTH);
        GlStateManager.pushMatrix();
        GlStateManager.translate(-viewerX, -viewerY, -viewerZ);
        
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();

        renderLines();
        renderBoxes();

        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
        GL11.glLineWidth(prevLW);

        lines.clear();
        boxes.clear();
    }

    private static void renderLines() {
        if (lines.isEmpty()) return;

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();

        GL11.glLineWidth(2.0F);
        renderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        for (DebugLine line : lines) {
            renderer.pos(line.start.xCoord, line.start.yCoord, line.start.zCoord)
                    .color(line.red, line.green, line.blue, line.alpha).endVertex();
            renderer.pos(line.end.xCoord, line.end.yCoord, line.end.zCoord)
                    .color(line.red, line.green, line.blue, line.alpha).endVertex();
        }

        tessellator.draw();
        GL11.glLineWidth(1.0F);
    }

    private static void renderBoxes() {
        if (boxes.isEmpty()) return;

        for (DebugBox box : boxes) {
            renderWireframeBox(box.min, box.max, box.red, box.green, box.blue, box.alpha);
        }
    }

    private static void renderWireframeBox(Vec3 min, Vec3 max, float red, float green, float blue, float alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();

        GL11.glLineWidth(1.5F);
        renderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        addLine(renderer, min.xCoord, min.yCoord, min.zCoord, max.xCoord, min.yCoord, min.zCoord, red, green, blue, alpha);
        addLine(renderer, max.xCoord, min.yCoord, min.zCoord, max.xCoord, min.yCoord, max.zCoord, red, green, blue, alpha);
        addLine(renderer, max.xCoord, min.yCoord, max.zCoord, min.xCoord, min.yCoord, max.zCoord, red, green, blue, alpha);
        addLine(renderer, min.xCoord, min.yCoord, max.zCoord, min.xCoord, min.yCoord, min.zCoord, red, green, blue, alpha);

        addLine(renderer, min.xCoord, max.yCoord, min.zCoord, max.xCoord, max.yCoord, min.zCoord, red, green, blue, alpha);
        addLine(renderer, max.xCoord, max.yCoord, min.zCoord, max.xCoord, max.yCoord, max.zCoord, red, green, blue, alpha);
        addLine(renderer, max.xCoord, max.yCoord, max.zCoord, min.xCoord, max.yCoord, max.zCoord, red, green, blue, alpha);
        addLine(renderer, min.xCoord, max.yCoord, max.zCoord, min.xCoord, max.yCoord, min.zCoord, red, green, blue, alpha);

        addLine(renderer, min.xCoord, min.yCoord, min.zCoord, min.xCoord, max.yCoord, min.zCoord, red, green, blue, alpha);
        addLine(renderer, max.xCoord, min.yCoord, min.zCoord, max.xCoord, max.yCoord, min.zCoord, red, green, blue, alpha);
        addLine(renderer, max.xCoord, min.yCoord, max.zCoord, max.xCoord, max.yCoord, max.zCoord, red, green, blue, alpha);
        addLine(renderer, min.xCoord, min.yCoord, max.zCoord, min.xCoord, max.yCoord, max.zCoord, red, green, blue, alpha);

        tessellator.draw();
        GL11.glLineWidth(1.0F);
    }

    private static void addLine(WorldRenderer renderer, double x1, double y1, double z1, double x2, double y2, double z2,
                               float red, float green, float blue, float alpha) {
        renderer.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
        renderer.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
    }

    public static void drawCoordinateAxis(Vec3 origin, float size) {
        Vec3 xAxis = origin.addVector(size, 0, 0);
        Vec3 yAxis = origin.addVector(0, size, 0);
        Vec3 zAxis = origin.addVector(0, 0, size);

        drawLine(origin, xAxis, 1.0F, 0.0F, 0.0F, 1.0F); // Red X
        drawLine(origin, yAxis, 0.0F, 1.0F, 0.0F, 1.0F); // Green Y
        drawLine(origin, zAxis, 0.0F, 0.0F, 1.0F, 1.0F); // Blue Z
    }

    public static void drawSphere(Vec3 center, float radius, float red, float green, float blue, float alpha) {
        int segments = 16;
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (2 * Math.PI * i / segments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / segments);

            Vec3 p1 = center.addVector(Math.cos(angle1) * radius, Math.sin(angle1) * radius, 0);
            Vec3 p2 = center.addVector(Math.cos(angle2) * radius, Math.sin(angle2) * radius, 0);
            drawLine(p1, p2, red, green, blue, alpha);

            p1 = center.addVector(Math.cos(angle1) * radius, 0, Math.sin(angle1) * radius);
            p2 = center.addVector(Math.cos(angle2) * radius, 0, Math.sin(angle2) * radius);
            drawLine(p1, p2, red, green, blue, alpha);

            p1 = center.addVector(0, Math.cos(angle1) * radius, Math.sin(angle1) * radius);
            p2 = center.addVector(0, Math.cos(angle2) * radius, Math.sin(angle2) * radius);
            drawLine(p1, p2, red, green, blue, alpha);
        }
    }

    private static class DebugLine {
        final Vec3 start, end;
        final float red, green, blue, alpha;

        DebugLine(Vec3 start, Vec3 end, float red, float green, float blue, float alpha) {
            this.start = start;
            this.end = end;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
        }
    }

    private static class DebugBox {
        final Vec3 min, max;
        final float red, green, blue, alpha;

        DebugBox(Vec3 min, Vec3 max, float red, float green, float blue, float alpha) {
            this.min = min;
            this.max = max;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
        }
    }
}