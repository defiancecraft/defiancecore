package com.defiancecraft.defiancecommons.util;

import java.util.regex.Matcher;

public class RegexUtils {

	/**
	 * Gets a group from a matched string, returning
	 * an empty string if the operation fails.
	 * 
	 * @param index Index of group
	 * @param m Matcher object
	 * @return Matched group, or ""
	 */
	public static String getGroup(int index, Matcher m) {
		
		if (!m.matches())
			return "";
		
		try {
			return m.group(index);
		} catch (Exception e) { /*[DEBUG]*/e.printStackTrace();/*[/DEBUG]*/ }
		
		return "";
		
	}
	
}
