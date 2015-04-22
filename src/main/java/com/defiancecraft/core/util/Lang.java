package com.defiancecraft.core.util;

import java.io.File;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

public class Lang {

	private static YamlConfiguration config;
	
	/**
	 * Reloads the language config from file
	 * @throws IOException If the language config could not be loaded or was invalid
	 */
	public static void reload() throws IOException {
		
		File configFile = FileUtils.getSharedConfig("lang.json");
		if (!configFile.exists())
			configFile.createNewFile();
		
		config = YamlConfiguration.loadConfiguration(configFile);
		
	}
	
	/**
	 * Saves the language configuration to file.
	 * @throws IOException If the file could not be saved.
	 */
	public static void save() throws IOException {
		config.save(FileUtils.getSharedConfig("lang.json"));
	}
	
	/**
	 * Gets a field from the language file. Note that this field must
	 * be unique to the namespace, but does not need to be unique to the
	 * entire language file. The preferred syntax for fields is
	 * snake_case, as the file is in YAML format, although namespaces
	 * will be converted to lowercase.
	 * 
	 * Color codes in the string will be converted to Minecraft codes from
	 * ampersands (&s).
	 * 
	 * @param namespace The namespace under which the field is stored; this is
	 * 					normally the module or plugin's canonical name.
	 * @param field The name of the field to access; preferably in snake_case
	 * @return The field, if present. Otherwise, returns null.
	 * @throws IllegalStateException if the configuration could not be loaded.
	 */
	public static String get(String namespace, String field) {
		
		return get(namespace, field, (String[]) null);
		
	}
	
	/**
	 * Gets a field from the language file. Note that this field must
	 * be unique to the namespace, but does not need to be unique to the
	 * entire language file. The preferred syntax for fields is
	 * snake_case, as the file is in YAML format, although namespaces
	 * will be converted to lowercase.
	 * 
	 * Color codes in the string will be converted to Minecraft codes from
	 * ampersands (&s), and the subsequent string will be passed to String.format
	 * with the given `args`.
	 * 
	 * @param namespace The namespace under which the field is stored; this is
	 * 					normally the module or plugin's canonical name.
	 * @param field The name of the field to access; preferably in snake_case
	 * @param args Any strings to pass to String.format for the field's value
	 * @return The field, if present. Otherwise, returns null.
	 * @throws IllegalStateException if the configuration could not be loaded.
	 */
	public static String get(String namespace, String field, String... args) {
		
		// Reload config if necessary
		if (config == null) {
			try {
				Lang.reload();
			} catch (IOException e) {
				throw new IllegalStateException("Config was not/could not be loaded.");
			}
		}
		
		if (!config.isConfigurationSection(namespace)
				|| !config.getConfigurationSection(namespace).contains(field))
			return null;
		
		String str = config.getConfigurationSection(namespace).getString(field, "");
		str = ChatColor.translateAlternateColorCodes('&', str);
		str = args == null ? str : String.format(str, (Object[])args);
		
		return str;
		
	}
	
	/**
	 * Sets a string in the language file (although does not save)
	 * 
	 * @param namespace The namespace under which the field is stored; this is
	 * 					normally the module or plugin's canonical name.
	 * @param field The name of the field to access; preferably in snake_case
	 * @param value Value of string
	 */
	public static void set(String namespace, String field, String value) {
		
		// Create new config if can't load
		if (config == null) {
			try {
				Lang.reload();
			} catch (IOException e) {
				config = new YamlConfiguration();
			}
		}
		
		config.createSection(namespace);
		config.getConfigurationSection(namespace).set(field, value);
		
	}
	
	/**
	 * Sets a default value for a field in the language file.
	 * @param namespace The namespace under which the field is stored; this is
	 * 					normally the module or plugin's canonical name.
	 * @param field The name of the field to access; preferably in snake_case
	 * @param def The default value of the field
	 * @return The passed field name, for convenience (possible to set constants this way).
	 */
	public static String setDefault(String namespace, String field, String def) {
		
		// Create new config if can't load
		if (config == null) {
			try {
				Lang.reload();
			} catch (IOException e) {
				config = new YamlConfiguration();
			}
		}
		
		if (!config.isConfigurationSection(namespace))
			config.createSection(namespace);
		
		if (!config.getConfigurationSection(namespace).contains(field))
			config.getConfigurationSection(namespace).set(field, def);

		return field;
		
	}
	
}
