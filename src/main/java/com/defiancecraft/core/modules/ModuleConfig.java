package com.defiancecraft.core.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.defiancecraft.core.util.JsonConfig;
import com.google.gson.JsonElement;

public class ModuleConfig extends JsonConfig {

	public List<String> enabledModules = new ArrayList<String>();
	public ConfigFormat configFormat = ConfigFormat.JSON;
	
	// Map of strings (module names) to maps of strings to JsonElements (JSON objects)
	public Map<String, Map<String, JsonElement>> configs = new HashMap<String, Map<String, JsonElement>>();
	
}
