package com.defiancecraft.core.api;

import java.util.UUID;

import com.defiancecraft.core.database.Database;
import com.defiancecraft.core.database.collections.Users;
import com.defiancecraft.core.database.documents.DBUser;
import com.defiancecraft.core.util.UUIDUtils;
import com.defiancecraft.core.util.UUIDUtils.UUIDResponse;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

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
	private DBObject generateQuery() {
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
	 * @return Whether the group was added
	 */
	public boolean addGroup(String group) {
		
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
	 * @return Whether the group was removed
	 */
	public boolean removeGroup(String group) {
		
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
	 * @return Whether the prefix was set
	 */
	public boolean setPrefix(String prefix) {
			
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
	 * @return Whether the suffix was set
	 */
	public boolean setSuffix(String suffix) {
		
		suffix = suffix == null ? "" : suffix; // Prevent NPEs on BasicDBObject
		
		DBObject query = generateQuery();
		DBObject data  = new BasicDBObject(
			suffix.isEmpty() ? "$unset" : "$set",
			new BasicDBObject(DBUser.FIELD_CUSTOM_SUFFIX, suffix)
		);
		
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
	 * @return User object representing found user, or null
	 * 		   if their username could not be resolved.
	 */
	public static User findByNameOrCreate(String name) {
		
		Users users = Database.getCollection(Users.class);
		
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
	 * @return User object, or null
	 */
	public static User findByName(String name) {
		
		Users users = Database.getCollection(Users.class);
		
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
		return User.findByUUID(uuidRes.getUUID());
		
	}
	
	/**
	 * Attempts to find a user by their UUID, returning
	 * null if they were not found in the DB.
	 * 
	 * @param uuid UUID of user to find
	 * @return User object, or null
	 */
	public static User findByUUID(UUID uuid) {
		
		Users users = Database.getCollection(Users.class);
		DBUser user = users.getByUUID(uuid);
		
		return user != null ? new User(user) : null;
		
	}
	
}
