package me.eldodebug.soar.management.shader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import me.eldodebug.soar.logger.GlideLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;

public class ShaderManager {

	private static final String DEFAULT_VERTEX_SHADER = 
		"#version 120\n" +
		"attribute vec2 position;\n" +
		"varying vec2 fragCoord;\n" +
		"varying vec2 vTexCoord;\n" +
		"uniform vec2 resolution;\n" +
		"void main() {\n" +
		"    fragCoord = (position * 0.5 + 0.5);\n" +
		"    vTexCoord = fragCoord;\n" +
		"    gl_Position = vec4(position, 0.0, 1.0);\n" +
		"}";

	private Map<File, Integer> shaderCache = new HashMap<>();
	private Map<ResourceLocation, Integer> resourceShaderCache = new HashMap<>();
	private int quadVAO = -1;
	private boolean initialized = false;

	public void init() {
		if (initialized) return;
		
		try {
			// Create a fullscreen quad
			quadVAO = createQuad();
			initialized = true;
		} catch (Exception e) {
			GlideLogger.error("Failed to initialize shader manager", e);
		}
	}

	public int loadShader(ResourceLocation shaderResource) {
		if (!initialized) {
			init();
		}

		// Check cache first
		if (resourceShaderCache.containsKey(shaderResource)) {
			return resourceShaderCache.get(shaderResource);
		}

		try {
			String fragmentSource = readShaderResource(shaderResource);
			if (fragmentSource == null || fragmentSource.trim().isEmpty()) {
				// Use a default gradient shader if resource is empty
				fragmentSource = getDefaultFragmentShader();
			}

			int program = createShaderProgram(DEFAULT_VERTEX_SHADER, fragmentSource);
			if (program != -1) {
				resourceShaderCache.put(shaderResource, program);
			}
			return program;
		} catch (Exception e) {
			GlideLogger.error("Failed to load shader resource: " + shaderResource.toString(), e);
			return -1;
		}
	}

	public int loadShader(File fragmentShaderFile) {
		if (!initialized) {
			init();
		}

		// Check cache first
		if (shaderCache.containsKey(fragmentShaderFile)) {
			return shaderCache.get(fragmentShaderFile);
		}

		try {
			String fragmentSource = readShaderFile(fragmentShaderFile);
			if (fragmentSource == null || fragmentSource.trim().isEmpty()) {
				// Use a default gradient shader if file is empty
				fragmentSource = getDefaultFragmentShader();
			}

			int program = createShaderProgram(DEFAULT_VERTEX_SHADER, fragmentSource);
			if (program != -1) {
				shaderCache.put(fragmentShaderFile, program);
			}
			return program;
		} catch (Exception e) {
			GlideLogger.error("Failed to load shader: " + fragmentShaderFile.getName(), e);
			return -1;
		}
	}

	public void renderShader(int shaderId, float x, float y, float width, float height) {
		if (shaderId == -1 || !initialized) return;

		Minecraft mc = Minecraft.getMinecraft();
		ScaledResolution sr = new ScaledResolution(mc);

		try {
			// Save current GL state
			GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_TEXTURE_BIT | GL11.GL_COLOR_BUFFER_BIT);
			
			// Disable texture 2D and enable our shader
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL20.glUseProgram(shaderId);

			// Set uniforms for the shader
			setShaderUniforms(shaderId, x, y, width, height, sr);

			// Set up orthographic projection for 2D rendering
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glPushMatrix();
			GL11.glLoadIdentity();
			GL11.glOrtho(0, sr.getScaledWidth(), sr.getScaledHeight(), 0, -1, 1);
			
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glPushMatrix();
			GL11.glLoadIdentity();

			// Enable blending for proper layering
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			// Create attribute locations for our vertex shader
			int positionAttrib = GL20.glGetAttribLocation(shaderId, "position");
			if (positionAttrib != -1) {
				GL20.glEnableVertexAttribArray(positionAttrib);
			}

			// Render the background quad with texture coordinates for fragment shader
			GL11.glBegin(GL11.GL_QUADS);
			
			// Bottom-left
			if (positionAttrib != -1) GL20.glVertexAttrib2f(positionAttrib, -1.0f, -1.0f);
			GL11.glTexCoord2f(0, 1); 
			GL11.glVertex2f(x, y + height);
			
			// Bottom-right  
			if (positionAttrib != -1) GL20.glVertexAttrib2f(positionAttrib, 1.0f, -1.0f);
			GL11.glTexCoord2f(1, 1);
			GL11.glVertex2f(x + width, y + height);
			
			// Top-right
			if (positionAttrib != -1) GL20.glVertexAttrib2f(positionAttrib, 1.0f, 1.0f);
			GL11.glTexCoord2f(1, 0);
			GL11.glVertex2f(x + width, y);
			
			// Top-left
			if (positionAttrib != -1) GL20.glVertexAttrib2f(positionAttrib, -1.0f, 1.0f);
			GL11.glTexCoord2f(0, 0);
			GL11.glVertex2f(x, y);
			
			GL11.glEnd();

			// Disable vertex attributes
			if (positionAttrib != -1) {
				GL20.glDisableVertexAttribArray(positionAttrib);
			}

			// Restore matrices
			GL11.glPopMatrix();
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glPopMatrix();
			GL11.glMatrixMode(GL11.GL_MODELVIEW);

		} catch (Exception e) {
			GlideLogger.error("Error rendering shader", e);
		} finally {
			// Restore GL state
			GL20.glUseProgram(0);
			GL11.glPopAttrib();
		}
	}

	private void setShaderUniforms(int shaderId, float x, float y, float width, float height, ScaledResolution sr) {
		// Time uniforms (common in shaders)
		float currentTime = (System.currentTimeMillis() % 100000) / 1000.0f;
		
		int timeLocation = GL20.glGetUniformLocation(shaderId, "time");
		if (timeLocation != -1) {
			GL20.glUniform1f(timeLocation, currentTime);
		}

		int iTimeLocation = GL20.glGetUniformLocation(shaderId, "iTime");
		if (iTimeLocation != -1) {
			GL20.glUniform1f(iTimeLocation, currentTime);
		}

		// Resolution uniforms
		int resolutionLocation = GL20.glGetUniformLocation(shaderId, "resolution");
		if (resolutionLocation != -1) {
			GL20.glUniform2f(resolutionLocation, width, height);
		}

		int iResolutionLocation = GL20.glGetUniformLocation(shaderId, "iResolution");
		if (iResolutionLocation != -1) {
			GL20.glUniform3f(iResolutionLocation, width, height, 1.0f);
		}

		// Mouse position (normalized)
		int mouseLocation = GL20.glGetUniformLocation(shaderId, "mouse");
		if (mouseLocation != -1) {
			// For now, use a default mouse position - could be enhanced to use real mouse
			GL20.glUniform2f(mouseLocation, 0.5f, 0.5f);
		}

		int iMouseLocation = GL20.glGetUniformLocation(shaderId, "iMouse");
		if (iMouseLocation != -1) {
			GL20.glUniform4f(iMouseLocation, width * 0.5f, height * 0.5f, 0.0f, 0.0f);
		}

		// Frame/date uniforms
		int iFrameLocation = GL20.glGetUniformLocation(shaderId, "iFrame");
		if (iFrameLocation != -1) {
			GL20.glUniform1i(iFrameLocation, (int)(currentTime * 60)); // Approximate frame count
		}

		int iDateLocation = GL20.glGetUniformLocation(shaderId, "iDate");
		if (iDateLocation != -1) {
			// Year, month, day, seconds in day
			long now = System.currentTimeMillis();
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(now);
			float year = cal.get(Calendar.YEAR);
			float month = cal.get(Calendar.MONTH) + 1;
			float day = cal.get(Calendar.DAY_OF_MONTH);
			float secondsInDay = (cal.get(Calendar.HOUR_OF_DAY) * 3600 + 
								cal.get(Calendar.MINUTE) * 60 + 
								cal.get(Calendar.SECOND));
			GL20.glUniform4f(iDateLocation, year, month, day, secondsInDay);
		}
	}

	private String readShaderResource(ResourceLocation resource) {
		try {
			java.io.InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(resource).getInputStream();
			if (is == null) {
				return null;
			}
			
			java.util.Scanner scanner = new java.util.Scanner(is, "UTF-8");
			StringBuilder content = new StringBuilder();
			while (scanner.hasNextLine()) {
				content.append(scanner.nextLine()).append("\n");
			}
			scanner.close();
			is.close();
			
			return content.toString();
		} catch (Exception e) {
			GlideLogger.error("Failed to read shader resource: " + resource.toString(), e);
			return null;
		}
	}

	private String readShaderFile(File file) {
		try {
			if (!file.exists()) {
				return null;
			}
			return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
		} catch (IOException e) {
			GlideLogger.error("Failed to read shader file: " + file.getName(), e);
			return null;
		}
	}

	private String getDefaultFragmentShader() {
		return "#version 120\n" +
			"uniform float time;\n" +
			"uniform vec2 resolution;\n" +
			"varying vec2 fragCoord;\n" +
			"void main() {\n" +
			"    vec2 uv = fragCoord;\n" +
			"    vec3 color = vec3(0.5 + 0.5 * cos(time + uv.xyx + vec3(0, 2, 4)));\n" +
			"    gl_FragColor = vec4(color, 1.0);\n" +
			"}";
	}

	private int createShaderProgram(String vertexSource, String fragmentSource) {
		int vertexShader = compileShader(GL20.GL_VERTEX_SHADER, vertexSource);
		if (vertexShader == -1) return -1;

		int fragmentShader = compileShader(GL20.GL_FRAGMENT_SHADER, fragmentSource);
		if (fragmentShader == -1) {
			GL20.glDeleteShader(vertexShader);
			return -1;
		}

		int program = GL20.glCreateProgram();
		GL20.glAttachShader(program, vertexShader);
		GL20.glAttachShader(program, fragmentShader);
		GL20.glLinkProgram(program);

		if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
			String log = GL20.glGetProgramInfoLog(program, 1024);
			GlideLogger.error("Shader program linking failed: " + log);
			GL20.glDeleteProgram(program);
			GL20.glDeleteShader(vertexShader);
			GL20.glDeleteShader(fragmentShader);
			return -1;
		}

		GL20.glDeleteShader(vertexShader);
		GL20.glDeleteShader(fragmentShader);

		return program;
	}

	private int compileShader(int type, String source) {
		int shader = GL20.glCreateShader(type);
		GL20.glShaderSource(shader, source);
		GL20.glCompileShader(shader);

		if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			String log = GL20.glGetShaderInfoLog(shader, 1024);
			GlideLogger.error("Shader compilation failed: " + log);
			GL20.glDeleteShader(shader);
			return -1;
		}

		return shader;
	}

	private int createQuad() {
		// For OpenGL 1.2, we'll just use immediate mode
		return 0; // Not used in immediate mode
	}

	private void renderQuad() {
		// Render a fullscreen quad using immediate mode (OpenGL 1.2 compatible)
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0, 0); GL11.glVertex2f(-1, -1);
		GL11.glTexCoord2f(1, 0); GL11.glVertex2f(1, -1);
		GL11.glTexCoord2f(1, 1); GL11.glVertex2f(1, 1);
		GL11.glTexCoord2f(0, 1); GL11.glVertex2f(-1, 1);
		GL11.glEnd();
	}

	public void cleanup() {
		for (int shaderId : shaderCache.values()) {
			if (shaderId != -1) {
				GL20.glDeleteProgram(shaderId);
			}
		}
		for (int shaderId : resourceShaderCache.values()) {
			if (shaderId != -1) {
				GL20.glDeleteProgram(shaderId);
			}
		}
		shaderCache.clear();
		resourceShaderCache.clear();
		initialized = false;
	}
}
