package com.defiancecraft.core.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class to assist with parsing arguments passed to a
 * command. Can parse different types of Argument defined
 * in the ArgumentParser.Argument enum and return in their
 * appropriate formats with the getter methods.
 */
public class ArgumentParser {
	
	private Argument[] arguments;
	private Matcher matcher;
	
	/**
	 * Constructs a new ArgumentParser for given input.
	 * 
	 * @param input String to parse
	 * @param arguments List of arguments, in order, to parse.
	 */
	public ArgumentParser(String input, Argument... arguments) {
		
		// List of regex patterns for arguments in groups (i.e. surrounded by brackets)
		List<String> patterns = new ArrayList<String>();
		for (Argument arg : arguments)
			patterns.add(String.format("(%s)", arg.getRegex()));
		
		// Create pattern to match all arguments.
		Pattern pattern = Pattern.compile("^" + String.join(" ", patterns) + "$");
		
		this.matcher   = pattern.matcher(input);
		this.arguments = arguments;
		
	}
	
	/**
	 * Checks whether the passed input matches passed
	 * argument types.
	 * 
	 * @return Whether the input is valid
	 */
	public boolean isValid() {
		return matcher.matches();
	}
	
	/**
	 * Retrieves a group from the matched input, or
	 * null if it does not matched. This works with
	 * the {@link Matcher#group(int)} method, so `group`
	 * is 1-indexed.
	 * 
	 * @param group Group number, 0 is the matched string.
	 * @throws IndexOutOfBoundsException Thrown if group exceeds argument length.
	 * @return The matched group, or null if it did not match or is empty.
	 */
	public String getGroup(int group) throws IndexOutOfBoundsException {

		// Attempt to match, and then attempt to get group,
		// returning null on either failure.
		String matched;
		return !matcher.matches()						? null    : 
			   (matched = matcher.group(group)) != null ? matched :
			   null;
	}
	
	/**
	 * Alias for {@link #getGroup(int)}
	 * @see #getGroup(int)
	 */
	public String getString(int group) throws IndexOutOfBoundsException {
		return getGroup(group);
	}
	
	/**
	 * Attempts to retrieve an integer from the
	 * matched input.
	 *
	 * @param group Group number, 1-indexed.
	 * @throws IndexOutOfBoundsException Thrown if group exceeds argument length.
	 * @return Integer, null if could not parse to int.
	 */
	public Integer getInt(int group) throws IndexOutOfBoundsException {
		
		if (arguments.length < group || group < 1) 
			throw new IndexOutOfBoundsException("Group is out of bounds of arguments");
		
		String matched = getGroup(group);
		if (matched == null)
			return null;
		
		switch (arguments[group - 1]) {
		case USERNAME:
		case WORD:
		case STRING:
		case INTEGER:
			try {
				return Integer.parseInt(matched);
			} catch (NumberFormatException e) { return null; }
		case DOUBLE:
			try {
				return Double.valueOf(matched).intValue();
			} catch (NumberFormatException e) { return null; }
		default:
			return null;
		}
		
	}

	/**
	 * Attempts to retrieve a double from the
	 * matched input.
	 * 
	 * @param group Group number, 1-indexed
	 * @throws IndexOutOfBoundsException Thrown if group exceeds argument length.
	 * @return Double, null if could not parse to double.
	 */
	public Double getDouble(int group) throws IndexOutOfBoundsException {
		
		if (arguments.length < group || group < 1)
			throw new IndexOutOfBoundsException("Group is out of bounds of arguments.");
		
		String matched = getGroup(group);
		if (matched == null)
			return null;
		
		switch (arguments[group - 1]) {
		case USERNAME:
		case WORD:
		case STRING:
		case DOUBLE:
			try {
				return Double.valueOf(matched);
			} catch (NumberFormatException e) { return null; }
		case INTEGER:
			try {
				return Integer.valueOf(matched).doubleValue();
			} catch (NumberFormatException e) { return null; }
		default:
			return null;
		}
		
	}
	
	/**
	 * An enum of Argument types that can be passed to a
	 * command and parsed.
	 */
	public enum Argument {

		/**
		 * Minecraft Username, limited to 16 alphanumeric or
		 * underscore characters.
		 */
		USERNAME("[a-zA-Z0-9_]{1,16}"),
		
		/**
		 * Integer - no floating points permitted.
		 */
		INTEGER("\\d+"),
		
		/**
		 * Double - floating points permitted, but optional.
		 */
		DOUBLE("\\d+(?:\\.\\d+)?"),
		
		/**
		 * Word - one or more characters excluding space.
		 */
		WORD("[^ ]+"),
		
		/**
		 * String - one or more characters including space.
		 */
		STRING(".+");
		
		private String regex;
		
		/**
		 * Constructs a required Argument type
		 * (i.e. not optional)
		 * 
		 * @param regex Regex of Argument type
		 */
		Argument(String regex) {
			this.regex = regex;
		}
		
		/**
		 * Gets the regular expression for this Argument type
		 * @return Regex pattern string
		 */
		public String getRegex() {
			return regex;
		}
		
	}
	
}
