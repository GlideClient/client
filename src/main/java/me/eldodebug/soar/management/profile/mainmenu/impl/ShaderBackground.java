package me.eldodebug.soar.management.profile.mainmenu.impl;

import java.io.File;

import me.eldodebug.soar.management.language.TranslateText;
import net.minecraft.util.ResourceLocation;

public class ShaderBackground extends Background {

	private TranslateText nameTranslate;
	private File shaderFile;
	private ResourceLocation shaderResource;
	private int shaderId = -1;
	
	public ShaderBackground(int id, TranslateText nameTranslate, File shaderFile) {
		super(id, nameTranslate.getText());
		this.nameTranslate = nameTranslate;
		this.shaderFile = shaderFile;
		this.shaderResource = new ResourceLocation("soar/shaders/menu.fsh");
	}
	
	public ShaderBackground(int id, TranslateText nameTranslate, ResourceLocation shaderResource) {
		super(id, nameTranslate.getText());
		this.nameTranslate = nameTranslate;
		this.shaderResource = shaderResource;
		this.shaderFile = null;
	}
	
	@Override
	public String getName() {
		return nameTranslate.getText();
	}

	public String getNameKey() {
		return nameTranslate.getKey();
	}

	public File getShaderFile() {
		return shaderFile;
	}

	public ResourceLocation getShaderResource() {
		return shaderResource;
	}

	public int getShaderId() {
		return shaderId;
	}

	public void setShaderId(int shaderId) {
		this.shaderId = shaderId;
	}

	public boolean isShaderLoaded() {
		return shaderId != -1;
	}

	public boolean hasResourceShader() {
		return shaderResource != null;
	}

	public boolean hasFileShader() {
		return shaderFile != null && shaderFile.exists();
	}
}
