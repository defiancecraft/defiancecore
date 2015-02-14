package com.defiancecraft.defiancecommons.permissions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A class representing metadata to
 * prevent frequent queries to the database.
 * This metadata should generally be used by
 * the chat so that every message doesn't send
 * a query.
 */
public class PermissionMetadata {
	
	public String prefix;
	public String suffix;
	
	private static Gson gson = new GsonBuilder()
		.disableHtmlEscaping()
		.create();
	
	public PermissionMetadata(String prefix, String suffix) {
		
		this.prefix = prefix;
		this.suffix = suffix;
		
	}
	
	public String getPrefix() { return prefix; }
	public String getSuffix() { return suffix; }
	
	/**
	 * Serializes data to a string so that it can
	 * be stored in metadata without ClassLoader conflicts
	 * 
	 * @param input PermissionMetadata object
	 * @return JSON String
	 */
	public static String serialize(PermissionMetadata input) {
		return gson.toJson(input);
	}
	
	/**
	 * Deserializes data so that it can be converted
	 * to a PermissionMetadata without ClassLoader conflicts
	 * 
	 * @param input JSON String
	 * @return PermissionMetadata object
	 */
	public static PermissionMetadata deserialize(String input) {
		try {
			return gson.fromJson(input, PermissionMetadata.class);
		} catch (Throwable t) { return null; }
	}

}