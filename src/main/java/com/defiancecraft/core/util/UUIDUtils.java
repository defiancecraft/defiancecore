package com.defiancecraft.core.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import org.bukkit.Bukkit;

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class UUIDUtils {

	private static final String URL_UUID = "https://api.mojang.com/users/profiles/minecraft/%s?at=%d";
	private static final int CONNECT_TIMEOUT = 8000; // Socket connection timeout in milliseconds
	public static final int DEFAULT_MAX_ATTEMPTS = 5;

	private static long lastAttempt = System.currentTimeMillis();
	private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();
	
	/**
	 * Attempts to resolve a UUID from a username
	 * at the specified time by contacting Mojang.
	 * Note that this does not run in a separate
	 * thread - this should be handled by the caller.
	 * 
	 * Will return null if:
	 * - `timestamp` is invalid
	 * - Could not find username
	 * - There is an IOException
	 * - Mojang server is unreachable after `DEFAULT_MAX_ATTEMPTS` attempts.
	 * 
	 * @param username Username of user to resolve
	 * @param timestamp Unix timestamp (without milliseconds)
	 * @return UUIDResponse object, or null
	 */
	public static UUIDResponse getUUID(String username, long timestamp) {
	
		return getUUID(username, timestamp, DEFAULT_MAX_ATTEMPTS);
		
	}
	
	/**
	 * Attempts to resolve a UUID from a username
	 * at the specified time by contacting Mojang.
	 * Note that this does not run in a separate
	 * thread - this should be handled by the caller.
	 * 
	 * Will return null if:
	 * - `timestamp` is invalid
	 * - Could not find username
	 * - There is an IOException
	 * - Mojang server is unreachable after `maxAttempts` attempts.
	 * 
	 * @param username Username of user to resolve
	 * @param timestamp Unix timestamp (without milliseconds)
	 * @param maxAttempts The number of times to attempt before giving up.
	 * @return UUIDResponse object, or null
	 */
	public static UUIDResponse getUUID(String username, long timestamp, int maxAttempts) {
		
		return getUUID(username, timestamp, 0, maxAttempts);
		
	}
	
	/**
	 * Private method called recursively to stop
	 * after `MAX_ATTEMPTS` attempts.
	 * 
	 * @see #getUUID(String, long)
	 */
	@SuppressWarnings("deprecation")
	private static UUIDResponse getUUID(String username, long timestamp, int attempts, int maxAttempts) {
		
		try {
			
			// Attempt to get Bukkit player
			if (Bukkit.getPlayerExact(username) != null) {
				return new UUIDUtils.UUIDResponse(Bukkit.getPlayerExact(username).getUniqueId().toString(), username);
			}
			
			String urlStr = String.format(URL_UUID, username, timestamp);
			URL url = new URL(urlStr);
			
			// Rate limiting :Z
			if (System.currentTimeMillis() - lastAttempt < 1000) {
				try {
					Thread.sleep(1000 - (System.currentTimeMillis() - lastAttempt) + 100);
				} catch (InterruptedException e) {}
			}

			UUIDUtils.lastAttempt = System.currentTimeMillis();
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(CONNECT_TIMEOUT);
			conn.connect();
			
			// Check if server responded with anything
			// notable. Otherwise, ignore and carry on.
			switch (conn.getResponseCode()) {
				case 204: return null; // 204 No Content - Player not found
				case 400: return null; // 400 Bad Request - timestamp invalid
				case 496: {
					Bukkit.getLogger().warning("Being rate limited by Mojang servers, retrying in 1 second.");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {}
					return getUUID(username, timestamp, attempts);
				}
				default: break;
			}
			
			InputStream in = conn.getInputStream();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			
			// Read response
			ByteStreams.copy(in, out);
			String json = out.toString();
			
			// Return null if empty response
			if (json == null) 
				return null;
			
			return gson.fromJson(json, UUIDResponse.class);
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (SocketTimeoutException e) {
			
			// Retry until MAX_ATTEMPTS is reached.
			if (attempts < maxAttempts) {
				
				Bukkit.getLogger().warning("Failed to reach Mojang servers, retrying in 5 seconds. Attempt #" + (++attempts));
				
				// Sleep, or upon failure, ignore.
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {}
				
				return getUUID(username, timestamp, attempts);
				
			} else {
				
				Bukkit.getLogger().severe("Could not reach Mojang servers after %d attempts; UUID for '" + username + "' not resolved.");
				return null;
				
			}
			
		} catch (IOException e) {
			
			Bukkit.getLogger().severe("IOException while attempting to resolve UUID for '" + username + "'. UUID not resolved. Stack trace below.");
			e.printStackTrace();
			return null;
			
		} catch (Exception e) {
			
			Bukkit.getLogger().severe("[CRITICAL ERROR] Exception while attempting to resolve UUID for '" + username + "'! UUID not resolved. Stack trace below.");
			e.printStackTrace();
			return null;
			
		}
		
		return null;
		
	}
	
	/**
	 * Converts a 32 character UUID string to
	 * a 36 character UUID string (i.e. with
	 * hyphens).
	 * 
	 * @param str UUID string to convert
	 * @return UUID, or null if `str` was invalid.
	 */
	public static UUID toUUID(String str) {
		
		str = str.toLowerCase();
		if (str.matches("^[a-f0-9]{8}-([a-f0-9]{4}-){3}[a-f0-9]{12}$"))
			return UUID.fromString(str);
		else if (str.matches("^[a-f0-9]{32}$"))
			return UUID.fromString(
				str.replaceAll("^([a-f0-9]{8})([a-f0-9]{4})([a-f0-9]{4})([a-f0-9]{4})([a-f0-9]{12})$", "$1-$2-$3-$4-$5")
			);
		else
			return null;
		
	}
	
	/**
	 * A class representing a response from
	 * Mojang for a UUID request.
	 */
	public static class UUIDResponse {
		
		public String id, name, error, errorMessage;
		
		public UUIDResponse(String id, String name) {
			this.id = id;
			this.name = name;
		}
		
		/**
		 * Gets the UUID
		 * @return UUID
		 */
		public UUID getUUID() {
			return UUIDUtils.toUUID(id);
		}
	}
	
}
