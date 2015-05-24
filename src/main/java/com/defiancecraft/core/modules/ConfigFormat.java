package com.defiancecraft.core.modules;

public enum ConfigFormat {

	JSON("json"),
	YAML("yml");
	
	private String extension;
	
	ConfigFormat(String extension) {
		this.extension = extension;
	}
	
	public String getExtension() {
		return extension;
	}
	
}
