package com.defiancecraft.defiancecommons.util;

import java.io.File;
import java.net.URISyntaxException;

public class FileUtils {

	/**
	 * Gets the JAR file of a class, or 
	 * returns null if there is an error in
	 * URI syntax (should not be thrown).
	 * 
	 * @param clazz Class to get JAR file of
	 * @return A File representing the JAR file
	 */
	public static File getJarFile(Class<?> clazz) {
		
		try {
			return new File(clazz.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (URISyntaxException e) {}
		
		return null;
		
	}
	
	/**
	 * Attempts to get the plugins directory
	 * from the DefianceCommons jar.
	 * 
	 * @return File object representing the plugins directory
	 */
	public static File getPluginsDirectory() {
		
		File jar = getJarFile(FileUtils.class);
		if (jar != null
				&& jar.getParentFile() != null)
			return jar.getParentFile();
		
		return null;
		
	}
	
	/**
	 * Gets the shared directory by all DCV4
	 * plugins to store configurations, etc.
	 * 
	 * @return A File representing the shared directory
	 */
	public static File getSharedDirectory() {
		
		return new File(FileUtils.getPluginsDirectory(), "DefianceCraft");
		
	}
	
	/**
	 * Gets a File object for a configuration
	 * in the shared directory, creating the
	 * shared directory if necessary.
	 * 
	 * @param name The canonical name of the file (i.e. no path, just file name)
	 * @return A File object for the config
	 * @see FileUtils#getSharedDirectory()
	 */
	public static File getSharedConfig(String name) {
		
		File dir = getSharedDirectory();
		if (!dir.exists() || !dir.isDirectory())
			dir.mkdir();
		
		return new File(dir, name);
		
	}
	
}
