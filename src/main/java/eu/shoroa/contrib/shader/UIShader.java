/*
 * Nanovg Blur
 * © Shoroa 2025, All Rights Reserved
 */

package eu.shoroa.contrib.shader;

import eu.shoroa.contrib.shader.uniform.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public final class UIShader {
    private int pid, vid, fid;
    private String vSrc, fSrc;
    private VBO vbo;

    public UIShader(String vSrc, String fSrc) {
        this.vSrc = vSrc;
        this.fSrc = fSrc;
    }

    public void init() throws IOException {
        vbo = new VBO();

        pid = GL20.glCreateProgram();
        vid = createShader(vSrc, GL20.GL_VERTEX_SHADER);
        fid = createShader(fSrc, GL20.GL_FRAGMENT_SHADER);

        GL20.glAttachShader(pid, vid);
        GL20.glAttachShader(pid, fid);
        GL20.glLinkProgram(pid);
        GL20.glValidateProgram(pid);

        if (GL20.glGetProgrami(pid, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            System.err.println("Failed to link program: " + GL20.glGetProgramInfoLog(pid, GL20.GL_INFO_LOG_LENGTH));
        }

        if (GL20.glGetProgrami(pid, GL20.GL_VALIDATE_STATUS) == GL11.GL_FALSE) {
            System.err.println("Failed to validate program: " + GL20.glGetProgramInfoLog(pid, GL20.GL_INFO_LOG_LENGTH));
        }

        GL20.glDeleteShader(vid);
        GL20.glDeleteShader(fid);
    }

    private int createShader(String src, int type) throws IOException {
        int id = GL20.glCreateShader(type);
        GL20.glShaderSource(id, readStream(Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(src)).getInputStream()));
        GL20.glCompileShader(id);

        if (GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            System.err.println("Failed to compile shader: " + GL20.glGetShaderInfoLog(id, GL20.GL_INFO_LOG_LENGTH));
        }

        return id;
    }

    private String readStream(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder builder = new StringBuilder();
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
        } catch (
                IOException e) {
            e.printStackTrace();
        }

        return builder.toString();
    }

    public void attach() {
        GL20.glUseProgram(pid);
    }

    public void detach() {
        GL20.glUseProgram(0);
    }

    public void free() {
        vbo.delete();
        GL20.glDeleteProgram(pid);
    }

    public void handleUniforms(ArrayList<Uniform> uniforms) {
        uniforms.forEach(this::uniform);
    }

    public void uniform(Uniform u) {
        if (u == null)
            return;
        if (u instanceof UFloatBuffer) {
            GL20.glUniform1(location(u.name()), ((UFloatBuffer) u).buffer());
        } else if (u instanceof USampler2D) {
            USampler2D s = (USampler2D) u;
            GL13.glActiveTexture(s.slot().glFunc());
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, s.texture());
            GL20.glUniform1i(location(s.name()), 0);
        } else if (u instanceof UFloat) {
            GL20.glUniform1f(location(u.name()), ((UFloat) u).value());
        } else if (u instanceof UVec2) {
            UVec2 v = (UVec2) u;
            GL20.glUniform2f(location(u.name()), v.x(), v.y());
        } else if (u instanceof UVec3) {
            UVec3 v = (UVec3) u;
            GL20.glUniform3f(location(u.name()), v.x(), v.y(), v.z());
        } else if (u instanceof UVec4) {
            UVec4 v = (UVec4) u;
            GL20.glUniform4f(location(u.name()), v.x(), v.y(), v.z(), v.w());
        } else if (u instanceof UInt) {
            GL20.glUniform1i(location(u.name()), ((UInt) u).value());
        }
    }

    private int location(String name) {
        return GL20.glGetUniformLocation(pid, name);
    }

    public void rect(float x, float y, float w, float h) {
        vbo.data(new float[]{
                x, y, 0f, 1f,
                x, y + h, 0f, 0f,
                x + w, y + h, 1f, 0f,
                x + w, y, 1f, 1f
        });

        vbo.bind();
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

        GL11.glVertexPointer(2, GL11.GL_FLOAT, 4 * 4, 0L);
        GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 4 * 4, 2 * 4L);

        GL11.glDrawArrays(GL11.GL_QUADS, 0, 4);

        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        vbo.unbind();
    }
}
