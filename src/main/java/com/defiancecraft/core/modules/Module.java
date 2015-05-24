package com.defiancecraft.core.modules;

import com.defiancecraft.core.database.collections.Collection;

/**
 * A module is defined as a plugin which utilizes DefianceCore
 * and adds functionality of some sort, rather than acting as
 *  a separate, independent plugin.
 * <br>
 * They are stored in the directory '&lt;shared folder&gt;/modules/'
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
	public <T> T getConfig(Class<T> clazz);

	/**
	 * Saves the configuration to the storage medium used by the
	 * module. This is usually JSON.
	 * 
	 * @param config An instance of the configuration to save
	 * @return Whether the config saved successfully.
	 */
	public <T> boolean saveConfig(T config);
	
}
