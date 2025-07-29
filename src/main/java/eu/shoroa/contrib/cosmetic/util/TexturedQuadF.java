package eu.shoroa.contrib.cosmetic.util;

import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;

public class TexturedQuadF extends TexturedQuad {
    public TexturedQuadF(PositionTextureVertex[] vertices) {
        super(vertices);
    }

    public TexturedQuadF(PositionTextureVertex[] vertices, float texcoordU1, float texcoordV1, float texcoordU2, float texcoordV2, float textureWidth, float textureHeight)
    {
        this(vertices);
        float f = 0.0F / textureWidth;
        float f1 = 0.0F / textureHeight;
        vertices[0] = vertices[0].setTexturePosition(texcoordU2 / textureWidth - f, texcoordV1 / textureHeight + f1);
        vertices[1] = vertices[1].setTexturePosition(texcoordU1 / textureWidth + f, texcoordV1 / textureHeight + f1);
        vertices[2] = vertices[2].setTexturePosition(texcoordU1 / textureWidth + f, texcoordV2 / textureHeight - f1);
        vertices[3] = vertices[3].setTexturePosition(texcoordU2 / textureWidth - f, texcoordV2 / textureHeight - f1);
    }
}
