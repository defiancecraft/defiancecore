package com.defiancecraft.core.util;

import java.io.File;
import java.net.URISyntaxException;

public final class FileUtils {

	private FileUtils() {}
	
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
	
	/**
	 * Gets a File object for the log directory within the
	 * shared folder. Modules may log contents to here in
	 * any format they desire. The only convention is to use
	 * the file extension .log to indicate that it is a log.
	 * 
	 * @return File representing the log directory.
	 */
	public static File getLogDirectory() {
		
		File dir = new File(getSharedDirectory(), "logs");
		if (!dir.exists() || !dir.isDirectory())
			dir.mkdirs();
		
		return dir;
		
	}
	
	/**
	 * Gets a File object for a log. This method does not
	 * create the file, as this should be handled by the calling
	 * class.
	 * 
	 * @param name File name without path, i.e. mylog.log
	 * @return File object
	 */
	public static File getLogFile(String name) {
		
		return new File(getLogDirectory(), name);
		
	}

	/**
	 * Gets the directory in which module configs are stored, creating it
	 * if it does not already exist.
	 * 
	 * @return File object representing directory in which module configs are stored.
	 */
	public static File getModuleConfigsDirectory() {
		
		File dir = new File(getSharedDirectory(), "configs");
		if (!dir.exists() || !dir.isDirectory())
			dir.mkdirs();
		
		return dir;
		
	}
	
	/**
	 * Gets a config for a module from the module configs directory. This config
	 * can be of any file type, as the file name and extension are specified in the
	 * `name` parameter.
	 * 
	 * @param name Name of the file which represents the module config; should include file extension.
	 * @return File object
	 */
	public static File getModuleConfig(String name) {
		return new File(getModuleConfigsDirectory(), name);
	}
	
}
