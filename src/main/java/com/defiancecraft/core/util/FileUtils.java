package com.defiancecraft.core.util;

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
	 * from the DefianceCore jar.
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
	
	/**
	 * Gets a File object for the modules directory,
	 * which is usually at /plugins/DefianceCraft/modules.
	 * This method creates the directory if it is non-existent.
	 * 
	 * @return File representing the modules directory.
	 */
	public static File getModulesDirectory() {
		
		File dir = new File(getSharedDirectory(), "modules");
		if (!dir.exists() || !dir.isDirectory())
			dir.mkdirs();
		
		return dir;
		
	}
	
}
