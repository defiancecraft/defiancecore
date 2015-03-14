package com.defiancecraft.core.modules;

import java.util.Map;

import org.bukkit.Bukkit;

import com.defiancecraft.core.DefianceCore;
import com.defiancecraft.core.database.collections.Collection;
import com.defiancecraft.core.util.FileUtils;
import com.defiancecraft.core.util.GsonUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * A module is defined as a plugin which utilizes DefianceCore
 * and adds functionality of some sort, rather than acting as
 *  a separate, independent plugin.
 * <br>
 * They are stored in the directory '<shared folder>/modules/'
 * and have the extension '.dc.jar'.
 * <br>
 * Modules currently serve the purpose of allowing for easy
 * disabling/enabling of modular functionality that can be
 * shared across servers.
 */
public interface Module {

	/**
	 * Gets a list of collections to register for this module.
	 * @return List of Collection instances
	 */
	public Collection[] getCollections();
	
	/**
	 * Gets the canonical name of this module to be used in
	 * config naming, for example.
	 * 
	 * @return Canonical name of module
	 */
	public String getCanonicalName();

	/**
	 * Loads the configuration from its appropriate object in
	 * the ModuleConfig (modules do not have their own files,
	 * but rather trees in modules.json)
	 * 
	 * @param clazz Class representing the config to load.
	 * @return The configuration, or null if `clazz` was invalid.
	 */
	public default <T> T getConfig(Class<T> clazz) {
		
		ModuleConfig config = DefianceCore.getModuleConfig();
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		
		if (config.configs.containsKey(getCanonicalName())) {
			
			// Return parsed config, if it exists in config list.
			Map<String, JsonElement> objMap = config.configs.get(getCanonicalName());
			JsonObject obj = GsonUtils.toJsonObject(objMap);
			
			return gson.fromJson(obj, clazz);
			
		} else {
			
			// Put default config in configs as a JsonObject, and return.
			try {
				
				T t = clazz.newInstance();
				
				// Convert to JSON string, then parse as JsonObject... then to
				// a Map<String, JsonElement>... wat.
				// This is to avoid having "members": { ... } in the config of
				// a module; it should simply be key-value pairs, whereas JsonObjects
				// add a "members" field, which is a Map<String, JsonElement>.
				Map<String, JsonElement> obj = GsonUtils.toMap(new JsonParser()
					.parse(gson.toJson(t))
					.getAsJsonObject());
				
				config.configs.put(getCanonicalName(), obj);
				config.save(FileUtils.getSharedConfig("modules.json"));
				DefianceCore.reloadModuleConfig();
				
				return t;
				
			} catch (InstantiationException | IllegalAccessException e) {
				
				Bukkit.getLogger().warning(String.format("%s while trying to load config of module; message: %s", e.getClass().getSimpleName(), e.getMessage()));
				return null;
			}
			
			
		}
		
	}
	
}
