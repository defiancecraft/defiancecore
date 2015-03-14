package com.defiancecraft.core.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GsonUtils {

	/**
	 * Converts a Map<String, JsonElement> to a JsonObject
	 * 
	 * @param map Map to convert
	 * @return JsonObject
	 */
	public static JsonObject toJsonObject(Map<String, JsonElement> map) {
		
		JsonObject ret = new JsonObject();
		for (Entry<String, JsonElement> entry : map.entrySet())
			ret.add(entry.getKey(), entry.getValue());
		
		return ret;
		
	}
	
	/**
	 * Converts a JsonObject to a Map<String, JsonElement>
	 * 
	 * @param obj JsonObject to convert
	 * @return Map<String, JsonElement>
	 */
	public static Map<String, JsonElement> toMap(JsonObject obj) {
		
		Map<String, JsonElement> ret = new LinkedHashMap<String, JsonElement>();
		for (Entry<String, JsonElement> entry : obj.entrySet())
			ret.put(entry.getKey(), entry.getValue());
		
		return ret;
		
	}
	
}
