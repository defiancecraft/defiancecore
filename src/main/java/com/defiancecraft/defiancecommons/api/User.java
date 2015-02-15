package com.defiancecraft.defiancecommons.api;

import org.bson.types.ObjectId;

import com.defiancecraft.defiancecommons.database.Database;
import com.defiancecraft.defiancecommons.database.collections.Users;
import com.defiancecraft.defiancecommons.database.documents.DBUser;
import com.defiancecraft.defiancecommons.util.UUIDUtils;
import com.defiancecraft.defiancecommons.util.UUIDUtils.UUIDResponse;
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
		
		ObjectId id = dbu.getId();
		DBObject query = new BasicDBObject(DBUser.FIELD_ID, id);
		DBObject data  = new BasicDBObject("$addToSet", new BasicDBObject(DBUser.FIELD_GROUPS, group));
		
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
		user = users.getByUUID(uuidRes.getUUID());
		if (user != null)
			return new User(user);
		
		// Plan C: Create new user with their UUID and name
		user = new DBUser(uuidRes.getUUID(), name);
		users.createUser(user);
		return new User(user);
		
	}
	
}
