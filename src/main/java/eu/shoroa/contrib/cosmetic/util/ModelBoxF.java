package eu.shoroa.contrib.cosmetic.util;

import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.client.renderer.WorldRenderer;

@SuppressWarnings("ALL")
public class ModelBoxF extends ModelBox {
    /**
     * The (x,y,z) vertex positions and (u,v) texture coordinates for each of the 8 points on a cube
     */
    private final PositionTextureVertex[] vertexPositions;
    /**
     * An array of 6 TexturedQuads, one for each face of a cube
     */
    private final TexturedQuad[] quadList;
    /**
     * X vertex coordinate of lower box corner
     */
    public final float posX1;
    /**
     * Y vertex coordinate of lower box corner
     */
    public final float posY1;
    /**
     * Z vertex coordinate of lower box corner
     */
    public final float posZ1;
    /**
     * X vertex coordinate of upper box corner
     */
    public final float posX2;
    /**
     * Y vertex coordinate of upper box corner
     */
    public final float posY2;
    /**
     * Z vertex coordinate of upper box corner
     */
    public final float posZ2;
    public String boxName;

    public ModelBoxF(ModelRenderer renderer, int textureX, int textureY, float posX1, float posY1, float posZ1, float offX, float offY, float offZ, float scale, boolean p_i46301_11_) {
        super(renderer, textureX, textureY, posX1, posY1, posZ1, 0, 0, 0, scale, p_i46301_11_);
        this.posX1 = posX1;
        this.posY1 = posY1;
        this.posZ1 = posZ1;
        this.posX2 = posX1 + offX;
        this.posY2 = posY1 + offY;
        this.posZ2 = posZ1 + offZ;
        this.vertexPositions = new PositionTextureVertex[8];
        this.quadList = new TexturedQuad[6];
        float f = posX1 + (float) offX;
        float f1 = posY1 + (float) offY;
        float f2 = posZ1 + (float) offZ;
        posX1 = posX1 - scale;
        posY1 = posY1 - scale;
        posZ1 = posZ1 - scale;
        f = f + scale;
        f1 = f1 + scale;
        f2 = f2 + scale;

        if (p_i46301_11_) {
            float f3 = f;
            f = posX1;
            posX1 = f3;
        }

        PositionTextureVertex positiontexturevertex7 = new PositionTextureVertex(posX1, posY1, posZ1, 0.0F, 0.0F);
        PositionTextureVertex positiontexturevertex = new PositionTextureVertex(f, posY1, posZ1, 0.0F, 8.0F);
        PositionTextureVertex positiontexturevertex1 = new PositionTextureVertex(f, f1, posZ1, 8.0F, 8.0F);
        PositionTextureVertex positiontexturevertex2 = new PositionTextureVertex(posX1, f1, posZ1, 8.0F, 0.0F);
        PositionTextureVertex positiontexturevertex3 = new PositionTextureVertex(posX1, posY1, f2, 0.0F, 0.0F);
        PositionTextureVertex positiontexturevertex4 = new PositionTextureVertex(f, posY1, f2, 0.0F, 8.0F);
        PositionTextureVertex positiontexturevertex5 = new PositionTextureVertex(f, f1, f2, 8.0F, 8.0F);
        PositionTextureVertex positiontexturevertex6 = new PositionTextureVertex(posX1, f1, f2, 8.0F, 0.0F);
        this.vertexPositions[0] = positiontexturevertex7;
        this.vertexPositions[1] = positiontexturevertex;
        this.vertexPositions[2] = positiontexturevertex1;
        this.vertexPositions[3] = positiontexturevertex2;
        this.vertexPositions[4] = positiontexturevertex3;
        this.vertexPositions[5] = positiontexturevertex4;
        this.vertexPositions[6] = positiontexturevertex5;
        this.vertexPositions[7] = positiontexturevertex6;
        this.quadList[0] = new TexturedQuadF(new PositionTextureVertex[]{positiontexturevertex4, positiontexturevertex, positiontexturevertex1, positiontexturevertex5}, textureX + offZ + offX, textureY + offZ, textureX + offZ + offX + offZ, textureY + offZ + offY, renderer.textureWidth, renderer.textureHeight);
        this.quadList[1] = new TexturedQuadF(new PositionTextureVertex[]{positiontexturevertex7, positiontexturevertex3, positiontexturevertex6, positiontexturevertex2}, textureX, textureY + offZ, textureX + offZ, textureY + offZ + offY, renderer.textureWidth, renderer.textureHeight);
        this.quadList[2] = new TexturedQuadF(new PositionTextureVertex[]{positiontexturevertex4, positiontexturevertex3, positiontexturevertex7, positiontexturevertex}, textureX + offZ, textureY, textureX + offZ + offX, textureY + offZ, renderer.textureWidth, renderer.textureHeight);
        this.quadList[3] = new TexturedQuadF(new PositionTextureVertex[]{positiontexturevertex1, positiontexturevertex2, positiontexturevertex6, positiontexturevertex5}, textureX + offZ + offX, textureY + offZ, textureX + offZ + offX + offX, textureY, renderer.textureWidth, renderer.textureHeight);
        this.quadList[4] = new TexturedQuadF(new PositionTextureVertex[]{positiontexturevertex, positiontexturevertex7, positiontexturevertex2, positiontexturevertex1}, textureX + offZ, textureY + offZ, textureX + offZ + offX, textureY + offZ + offY, renderer.textureWidth, renderer.textureHeight);
        this.quadList[5] = new TexturedQuadF(new PositionTextureVertex[]{positiontexturevertex3, positiontexturevertex4, positiontexturevertex5, positiontexturevertex6}, textureX + offZ + offX + offZ, textureY + offZ, textureX + offZ + offX + offZ + offX, textureY + offZ + offY, renderer.textureWidth, renderer.textureHeight);

        if (p_i46301_11_) {
            for (TexturedQuad texturedQuad : this.quadList) {
                texturedQuad.flipFace();
            }
        }
    }

    public void render(WorldRenderer renderer, float scale) {
        for (TexturedQuad texturedQuad : this.quadList) {
            texturedQuad.draw(renderer, scale);
        }
    }

    public ModelBoxF setBoxName(String name) {
        this.boxName = name;
        return this;
    }
}