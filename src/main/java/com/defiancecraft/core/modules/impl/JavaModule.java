package com.defiancecraft.core.modules.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import com.defiancecraft.core.DefianceCore;
import com.defiancecraft.core.database.collections.Collection;
import com.defiancecraft.core.modules.ConfigFormat;
import com.defiancecraft.core.modules.Module;
import com.defiancecraft.core.modules.ModuleConfig;
import com.defiancecraft.core.util.FileUtils;
import com.defiancecraft.core.util.GsonUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;

/**
 * An abstract implementation of the Module class which is also a JavaPlugin.
 * <br><br>
 * Configs may be loaded using either JSON or YAML file formats. If they are
 * non-existent, they will be created upon calling {@link #getConfig(Class)} in
 * the preferred file format specified in the modules config (usually modules.json).
 * If the config for this module is present in the modules config, it <b>will be migrated</b>
 * to its own file, in the preferred format. 
 * <br><br>
 * The canonical name for JavaModules is by default the simple class name of the
 * subclass. Subclasses may need to override this if their main class is not named
 * topically, i.e. not named the same as their module.
 * <br><br>
 * 
 */
public abstract class JavaModule extends JavaPlugin implements Module {

	private static final String MIGRATION_KEY = "__migrated";
	private static final String JSON_EXTENSION = ".json";
	private static final String YAML_EXTENSION = ".yml";
	private static final Gson GSON = new GsonBuilder()
		.setPrettyPrinting()
		.disableHtmlEscaping()
		.create();
	private static final Yaml YAML = new Yaml();
	
	@Override
	public Collection[] getCollections() {
		return new Collection[] {};
	}

	@Override
	public String getCanonicalName() {
		return getClass().getSimpleName();
	}
	
	@Override
	public <T> T getConfig(Class<T> clazz) {
	
		// Migrate the configuration if it is still stored in the modules config.
		ModuleConfig moduleConfig = DefianceCore.getModuleConfig();
		if (moduleConfig.configs.containsKey(getCanonicalName()) && !moduleConfig.configs.get(getCanonicalName()).containsKey(MIGRATION_KEY))
			return migrateConfig(clazz);
		
		try {
			
			File jsonConfig = FileUtils.getModuleConfig(getCanonicalName() + JSON_EXTENSION);
			File yamlConfig = FileUtils.getModuleConfig(getCanonicalName() + YAML_EXTENSION);
			ConfigFormat preferred = moduleConfig.configFormat;
			
			// Try and load their preference if possible. If a config exists that isn't their
			// preference, use that instead if their preference isn't there.
			if (jsonConfig.isFile() && (preferred.equals(ConfigFormat.JSON) || !yamlConfig.isFile()))
				return getJsonConfig(clazz, jsonConfig);
			else if (yamlConfig.isFile() && (preferred.equals(ConfigFormat.YAML) || !jsonConfig.isFile()))
				return getYamlConfig(clazz, yamlConfig);

			// Write defaults to their preferred storage medium.
			T instance = clazz.newInstance();
			if (preferred.equals(ConfigFormat.JSON))
				saveJsonConfig(instance, jsonConfig);
			else
				saveYamlConfig(instance, yamlConfig);
			
			return instance;
			
		} catch (IOException e) {
			
			Bukkit.getLogger().warning(String.format("[Modules] An I/O error occurred while loading the config for module '%s'. Returning default config.", getCanonicalName()));
			e.printStackTrace();
			
			// Attempt to return default config
			try {
				return clazz.newInstance();
			} catch (InstantiationException | IllegalAccessException e1) {
				Bukkit.getLogger().severe(String.format("[Modules] Could not create default instance of config for module '%s'. Returning null instead.", getCanonicalName()));
				return null;
			}
			
		} catch (InstantiationException | IllegalAccessException e) {
			
			Bukkit.getLogger().warning(String.format("[Modules] The default config for module '%s' could not be instantiated! Returning null and printing stack trace (this could cause issues within the module!", getCanonicalName()));
			e.printStackTrace();
			return null;
			
		}
		
	}
	
	@Override
	public <T> boolean saveConfig(T instance) {
		
		try {
			
			ModuleConfig config = DefianceCore.getModuleConfig();
			if (config.configFormat.equals(ConfigFormat.JSON))
				saveJsonConfig(instance, FileUtils.getModuleConfig(getCanonicalName() + JSON_EXTENSION));
			else
				saveYamlConfig(instance, FileUtils.getModuleConfig(getCanonicalName() + YAML_EXTENSION));
		
			return true;
			
		} catch (IOException e) {
			
			Bukkit.getLogger().warning(String.format("[Modules] Failed to save config for module '%s'. Stack trace below.", getCanonicalName()));
			return false;
			
		}
		
	}
	
	/**
	 * Migrates a config from the modules config to its own file, stored
	 * in the preferred format defined in the modules config.
	 * 
	 * @param clazz Type of config
	 * @return Instance of config that was present in the modules.json
	 */
	private <T> T migrateConfig(Class<T> clazz) {
		
		ModuleConfig moduleConfig = DefianceCore.getModuleConfig();
		if (!moduleConfig.configs.containsKey(getCanonicalName()) || moduleConfig.configs.get(getCanonicalName()).containsKey(MIGRATION_KEY))
			throw new IllegalStateException("Config is already migrated or does not exist!");
		
		// Load the config as an instance of 'T' by converting it to a JsonObject from
		// the Map it is stored as, and getting Gson to parse this.
		T config = GSON.fromJson(GsonUtils.toJsonObject(moduleConfig.configs.get(getCanonicalName())), clazz);
		
		// Save the new config in the preferred format.
		try {
			if (moduleConfig.configFormat.equals(ConfigFormat.JSON))
				saveJsonConfig(config, FileUtils.getModuleConfig(getCanonicalName() + JSON_EXTENSION));
			else
				saveYamlConfig(config, FileUtils.getModuleConfig(getCanonicalName() + YAML_EXTENSION));
		} catch (IOException e) {
			Bukkit.getLogger().warning(String.format("[Modules] Failed to save config for module '%s' during migration; it will remain unmigrated until possible to save.", getCanonicalName()));
			e.printStackTrace();
			return config;
		}
		
		// Set the migrated flag in the module's config in modules.json to prevent future migration
		// and save the new file.
		moduleConfig.configs.get(getCanonicalName()).put(MIGRATION_KEY, new JsonPrimitive(true));
		moduleConfig.save(FileUtils.getSharedConfig("modules.json"));
		
		return config;
		
	}
	
	/**
	 * Loads a configuration file from disk in JSON format. The config
	 * will be serialized to the given type `clazz`. This method assumes
	 * the file exists; if it does not, an IllegalArgumentException will be
	 * thrown.
	 * 
	 * @param clazz Class of the config
	 * @param file File representing the config
	 * @return The serialized config instance
	 * @throws IOException If the config could not be parsed/read.
	 */
	protected static <T> T getJsonConfig(Class<T> clazz, File file) throws IOException {
		
		if (!file.isFile())
			throw new IllegalArgumentException("Yaml config file does not exist/is a directory.");
		
		return GSON.fromJson(new FileReader(file), clazz);
		
	}
	
	/**
	 * Loads a configuration file from disk in YAML format. The config
	 * will be serialized to the given type, similarly to Gson's serialization
	 * of JSON files.
	 * <br><br>
	 * Note that internally, the configuration will be converted
	 * to JSON, which could take slightly longer than directly parsing JSON files
	 * via {@link #getJsonConfig(Class, File)}. However, timing is often not an
	 * issue for this and so may be ignored if not of importance.
	 * 
	 * @param clazz Class of the config
	 * @param file File representing the config
	 * @return The serialized config instance
	 * @throws IOException If the file could not correctly be parsed.
	 */
	protected static <T> T getYamlConfig(Class<T> clazz, File file) throws IOException {
		
		if (!file.isFile())
			throw new IllegalArgumentException("Yaml config file does not exist/is a directory.");
		
		// SnakeYaml does not parse correctly in some situations, and
		// depends on empty constructors. Thus, it is easier to load it as
		// and Object, convert to JSON and parse.
		Object obj = YAML.load(new FileInputStream(file));
		T instance = GSON.fromJson(GSON.toJson(obj), clazz);

		return instance;
		
	}
	
	/**
	 * Saves a JSON configuration to the given file. This is primarily a convenience method.
	 * 
	 * @param instance Instance of config to save
	 * @param file File to save to
	 * @throws IOException If the file could not be written to
	 */
	protected static <T> void saveJsonConfig(T instance, File file) throws IOException {
		GSON.toJson(instance, new FileWriter(file));
	}
	
	/**
	 * Saves a YAML configuration to the given file. This is primarily a convenience method.
	 * 
	 * @param instance Instance of config to save
	 * @param file File to save to
	 * @throws IOException If the file could not be written to
	 */
	protected static <T> void saveYamlConfig(T instance, File file) throws IOException {
		YAML.dump(instance, new FileWriter(file));
	}
	
}
