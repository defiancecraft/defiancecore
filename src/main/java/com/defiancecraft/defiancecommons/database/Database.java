package com.defiancecraft.defiancecommons.database;

import java.io.File;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bson.types.ObjectId;

import com.archeinteractive.defiancetools.util.JsonConfig;
import com.defiancecraft.defiancecommons.DefianceCommons;
import com.defiancecraft.defiancecommons.database.collections.Collection;
import com.defiancecraft.defiancecommons.database.collections.Servers;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;

public class Database {

	private static DatabaseConfig config;
	private static MongoClient client;
	private static DB db;
	private static Map<Class<? extends Collection>, Collection> collections = new HashMap<Class<? extends Collection>, Collection>();
	
	/**
	 * Initializes the configuration and DB connection
	 * @param p The plugin
	 * @throws UnknownHostException
	 */
	public static void init(DefianceCommons p) throws MongoException, UnknownHostException {
		
		if (Database.config != null)
			return;
		
		Database.config = JsonConfig.load(new File(p.getDataFolder(), "db.json"), DatabaseConfig.class);
		reconnect();
		
	}
	
	/**
	 * Checks whether the database is connected
	 * @return Whether the database is connected
	 */
	public static boolean isConnected() {
		
		if (client == null || db == null)
			return false;
		
		// Attempt to get a list of DB names
		// (will throw if it can't)
		try {
			client.getDatabaseNames();
		} catch (MongoException e) {
			return false;
		}
		
		// Database must be connected
		return true;
		
	}
	
	/**
	 * Gets the server's ID in the "servers"
	 * collection, or returns null if the
	 * server ID is invalid.
	 * 
	 * @return The server ID
	 */
	public static ObjectId getServerId() {
		
		try {
			return new ObjectId(config.serverId);
		} catch (IllegalArgumentException e) {
			return null;			
		}
		
	}
	
	/**
	 * Gets the primary DB as specified in
	 * the configuration
	 * 
	 * @return A DB object, or null if Database was not initialized.
	 */
	public static DB getDB() {
		
		return db;
		
	}
	
	/**
	 * Establishes a connection with the database, closing
	 * exiting ones if necessary.
	 * 
	 * @throws UnknownHostException
	 */
	public static void reconnect() throws MongoException, UnknownHostException {
		
		if (client != null)
			disconnect();
		
		ServerAddress address = new ServerAddress(config.host, config.port);
		
		if (config.usesAuth) {
			MongoCredential credential = MongoCredential.createMongoCRCredential(config.username, config.database, config.password.toCharArray());
			client = new MongoClient(address, Arrays.asList(credential));
		} else {
			client = new MongoClient(address);
		}
		
		db = client.getDB(config.database);
		
		// Verify that the server exists in "servers" collection
		// Throw exception if it does not
		DBObject obj = db.getCollection("servers").findOne(new BasicDBObject("_id", Database.getServerId()));
		if (obj == null)
			throw new MongoException("Failed to verify server ID; is the server configured correctly?");
		
	}
	
	/**
	 * Destroys connection with database, if existent
	 */
	public static void disconnect() {
		
		if (client == null)
			return;
		
		client.close();
		client = null;
		db = null;
		
	}
	
	/**
	 * Gets the MongoClient instance
	 * @return MongoClient instance
	 */
	public static MongoClient getClient() {
		
		return client;
		
	}
	
	/**
	 * Registers a collection so that it can be
	 * retrieved using getCollection
	 * 
	 * @param c The collection instance to register
	 * @see Database#getCollection(Class<? extends Collection>)
	 */
	public static void registerCollection(Collection c) {
		
		collections.put(c.getClass(), c);
		
	}
	
	/**
	 * Gets a collection from the registered
	 * collections map
	 * 
	 * @param clazz The class of the collection
	 * @return A collection of type T
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Collection> T getCollection(Class<T> clazz) {
		
		return (T) collections.get(clazz);
		
	}
	
	// Register collections
	static {
		
		Database.registerCollection(new Servers());
		
	}
	//public static ExecutorService
	
}
