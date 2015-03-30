package com.defiancecraft.core.api;

import java.util.UUID;

import org.bukkit.Bukkit;

import com.defiancecraft.core.database.Database;
import com.defiancecraft.core.database.collections.Users;
import com.defiancecraft.core.database.documents.DBUser;
import com.defiancecraft.core.util.UUIDUtils;
import com.defiancecraft.core.util.UUIDUtils.UUIDResponse;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

/**
 * This class represents a User model, and presents
 * a high-level way of interacting with the database.
 * 
 * Not to be confused with DBUser, which is a Document
 * and does not make any modifications to the database
 * per se, but rather to the local object. A DBUser can
 * later be saved, but saving is not always the best
 * option as this will overwrite documents; sometimes,
 * updates are a better options. This class provides a
 * layer of abstraction which makes use of these operations.
 */
public class User {

	private DBUser dbu;
	
	User(DBUser dbu) {
		this.dbu = dbu;
	}
	
	/**
	 * Generates a query object to be used in
	 * updates
	 * 
	 * @return DBObject
	 */
	public DBObject generateQuery() {
		return new BasicDBObject(DBUser.FIELD_ID, dbu.getId());
	}
	
	/**
	 * Gets the DBUser associated with this
	 * user.
	 * 
	 * @return DBUser
	 */
	public DBUser getDBU() {
		
		return this.dbu;
		
	}
	
	/**
	 * Adds a group to this user by performing an update on
	 * the database. Will return false if no users matched
	 * the query (queries by ID of the user's DBUser)
	 * 
	 * @param group The group to add
	 * @throws MongoException Thrown if a database error occurs
	 * @return Whether the group was added
	 */
	public boolean addGroup(String group) throws MongoException {
		
		DBObject query = generateQuery();
		DBObject data  = new BasicDBObject("$addToSet", new BasicDBObject(DBUser.FIELD_GROUPS, group));
		
		return Database.getCollection(Users.class).update(query, data).getN() > 0;
		
	}
	
	/**
	 * Removes a group from this user by performing an update
	 * on the database. Will return false if no users matched
	 * the query (queries by ID of the user's DBUser)
	 * 
	 * @param group The group to remove
	 * @throws MongoException Thrown if a database error occurs
	 * @return Whether the group was removed
	 */
	public boolean removeGroup(String group) throws MongoException {
		
		DBObject query = generateQuery();
		DBObject data  = new BasicDBObject("$pull", new BasicDBObject(DBUser.FIELD_GROUPS, group));
		
		return Database.getCollection(Users.class).updateMulti(query, data).getN() > 0;
		
	}
	
	/**
	 * Sets the user's custom prefix to the specified prefix
	 * by performing an update on the database. If `prefix` is
	 * null or empty, their custom prefix will be unset.
	 * 
	 * @param prefix Prefix to set (can be null)
	 * @throws MongoException Thrown if a database error occurs
	 * @return Whether the prefix was set
	 */
	public boolean setPrefix(String prefix) throws MongoException {
			
		prefix = prefix == null ? "" : prefix; // Prevent NPEs on BasicDBObject
		
		DBObject query = generateQuery();
		DBObject data  = new BasicDBObject( // Change $set to $unset if prefix is empty
			prefix.isEmpty() ? "$unset" : "$set",
			new BasicDBObject(DBUser.FIELD_CUSTOM_PREFIX, prefix)
		);
		
		return Database.getCollection(Users.class).update(query, data).getN() > 0;
		
	}
	
	/**
	 * Sets the user's custom suffix to the specified suffix
	 * by performing an update on the database. If `suffix` is
	 * null or empty, their custom suffix will be unset.
	 * 
	 * @param suffix Suffix to set (can be null)
	 * @throws MongoException Thrown if a database error occurs
	 * @return Whether the suffix was set
	 */
	public boolean setSuffix(String suffix) throws MongoException {
		
		suffix = suffix == null ? "" : suffix; // Prevent NPEs on BasicDBObject
		
		DBObject query = generateQuery();
		DBObject data  = new BasicDBObject(
			suffix.isEmpty() ? "$unset" : "$set",
			new BasicDBObject(DBUser.FIELD_CUSTOM_SUFFIX, suffix)
		);
		
		return Database.getCollection(Users.class).update(query, data).getN() > 0;
		
	}
	
	/**
	 * Sets the user's balance by performing an update on the
	 * database.
	 * 
	 * @param balance New balance of user
	 * @throws MongoException Thrown if a database error occurs
	 * @return Whether the user's balance was set
	 */
	public boolean setBalance(double balance) throws MongoException {
		
		DBObject query = generateQuery();
		DBObject data  = new BasicDBObject("$set", new BasicDBObject(DBUser.FIELD_BALANCE, balance));
		
		return Database.getCollection(Users.class).update(query, data).getN() > 0;
		
	}
	
	/**
	 * Sets the name of a player by performing an update on the
	 * database.
	 * 
	 * @param name New name of player
	 * @return Whether the user's name was set successfully
	 * @throws MongoException Thrown if a database error occurs
	 */
	public boolean setName(String name) throws MongoException {
		
		DBObject query = generateQuery();
		DBObject data  = new BasicDBObject("$set", new BasicDBObject(DBUser.FIELD_NAME, name));
		
		return Database.getCollection(Users.class).update(query, data).getN() > 0;
		
	}
	
	/**
	 * Finds a user by their name, checking database first,
	 * then resorting to UUID lookup (via Mojang), and then
	 * just creating the user, if they are still not found.
	 * 
	 * Worst case scenario, this method will do two database
	 * queries and one database insert.
	 * 
	 * @param name Name of user
	 * @throws MongoException Thrown if a database error occurs
	 * @return User object representing found user, or null
	 * 		   if their username could not be resolved.
	 */
	@SuppressWarnings("deprecation")
	public static User findByNameOrCreate(String name) throws MongoException {
		
		Users users = Database.getCollection(Users.class);
		
		// In fact, try and find the Bukkit user and get their motherfucking UUID.
		// This shit will save a lot of time.
		if (Bukkit.getPlayerExact(name) != null) {
			DBUser user = users.getByUUID(Bukkit.getPlayerExact(name).getUniqueId());
			if (user != null)
				return new User(user);
		}
		
		// Plan A: Try and find them by name
		DBUser user = users.getByName(name);
		
		if (user != null)
			return new User(user);
		
		// Get their UUID from Mojang, or return null
		// if it wasn't found or some other error
		// (see UUIDUtils#getUUID(String, long))
		UUIDResponse uuidRes = UUIDUtils.getUUID(name, System.currentTimeMillis() / 1000);
		if (uuidRes == null
				|| uuidRes.name == null
				|| uuidRes.name.isEmpty()
				|| uuidRes.id == null
				|| uuidRes.id.isEmpty())
			return null;
		
		// Plan B: Try and find them by their UUID
		user = users.getByUUID(uuidRes.getUUID());
		if (user != null)
			return new User(user);
		
		// Plan C: Create new user with their UUID and name
		user = new DBUser(uuidRes.getUUID(), name);
		users.createUser(user);
		
		return new User(user);
		
	}
	
	/**
	 * Finds a user by their name, checking database first,
	 * then resorting to UUID lookup (via Mojang). This method
	 * will not create the user if they are not found at this
	 * point, unlike {@link #findByNameOrCreate(String)}, but
	 * rather return null.
	 * 
	 * @param name Name of user to lookup
	 * @throws MongoException Thrown if a database error occurs
	 * @return User object, or null
	 */
	 public static User findByName(String name) throws MongoException {
		
		return User.findByName(name, UUIDUtils.DEFAULT_MAX_ATTEMPTS);
		
	}
	 
	// TODO doc
	@SuppressWarnings("deprecation")
	public static User findByName(String name, int maxAttempts) throws MongoException {
		
		Users users = Database.getCollection(Users.class);
		
		// In fact, try and find the Bukkit user and get their motherfucking UUID.
		// This shit will save a lot of time.
		if (Bukkit.getPlayerExact(name) != null) {
			DBUser user = users.getByUUID(Bukkit.getPlayerExact(name).getUniqueId());
			if (user != null)
				return new User(user);
		}
		
		// Plan A: Try and find them by name
		DBUser user = users.getByName(name);
		
		if (user != null)
			return new User(user);
		
		// Get their UUID from Mojang, or return null
		// if it wasn't found or some other error
		// (see UUIDUtils#getUUID(String, long))
		UUIDResponse uuidRes = UUIDUtils.getUUID(name, System.currentTimeMillis() / 1000, maxAttempts);
		if (uuidRes == null
				|| uuidRes.name == null
				|| uuidRes.name.isEmpty()
				|| uuidRes.id == null
				|| uuidRes.id.isEmpty())
			return null;
		
		// Plan B: Try and find them by their UUID
		return User.findByUUID(uuidRes.getUUID());
		
	}
	
	/**
	 * Attempts to find a user by their UUID, returning
	 * null if they were not found in the DB.
	 * 
	 * @param uuid UUID of user to find
	 * @throws MongoException Thrown if a database error occurs
	 * @return User object, or null
	 */
	public static User findByUUID(UUID uuid) throws MongoException {
		
		Users users = Database.getCollection(Users.class);
		DBUser user = users.getByUUID(uuid);
		
		return user != null ? new User(user) : null;
		
	}
	
	/**
	 * Attempts to find a user by their UUID, creating
	 * one using the given name and UUID if they were
	 * not found in the DB.
	 * 
	 * @param uuid UUID of user to find
	 * @param name Name, for creation if they are not found
	 * @return User object
	 */
	public static User findByUUIDOrCreate(UUID uuid, String name) {
		
		Users users = Database.getCollection(Users.class);
		DBUser user = users.getByUUID(uuid);
		
		if (user == null) {
			user = new DBUser(uuid, name);
			users.createUser(user);
		}
		
		return new User(user);
		
	}
	
	/**
	 * Checks whether a user exists - will return false
	 * on failure.
	 * 
	 * @param name Name of user
	 * @return Whether they exist.
	 * @deprecated {@link #exists(UUID)} is preferred due to UUID migration.
	 */
	@Deprecated
	public static boolean exists(String name) {
		
		Users users = Database.getCollection(Users.class);
		DBUser user;
		
		try {
			user = users.getByName(name);
		} catch (MongoException e) { return false; }
		
		return user != null;
		
	}
	
	/**
	 * Checks whether a user exists - will return false
	 * on failure.
	 * 
	 * @param uuid UUID of user
	 * @return Whether they exist.
	 */
	public static boolean exists(UUID uuid) {
	
		Users users = Database.getCollection(Users.class);
		DBUser user;
		
		try {
			user = users.getByUUID(uuid);
		} catch (MongoException e) { return false; }
		
		return user != null;
		
	}
	
}
