package com.defiancecraft.core.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GsonUtils {

	/**
	 * Converts a Map&lt;String, JsonElement&gt; to a JsonObject
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
	 * Converts a JsonObject to a Map&lt;String, JsonElement&gt;
	 * 
	 * @param obj JsonObject to convert
	 * @return Map
	 */
	public static Map<String, JsonElement> toMap(JsonObject obj) {
		
		Map<String, JsonElement> ret = new LinkedHashMap<String, JsonElement>();
		for (Entry<String, JsonElement> entry : obj.entrySet())
			ret.put(entry.getKey(), entry.getValue());
		
		return ret;
		
	}
	
}
