package com.defiancecraft.core.database.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.defiancecraft.core.database.Database;
import com.defiancecraft.core.database.documents.DBUser;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class Users extends Collection {

	/**
	 * List of users being asynchronously created
	 * (to prevent duplicate creation operations)
	 */
	private volatile List<UUID> creating = new ArrayList<UUID>();
	
	public String getCollectionName() {
		return "users";
	}
	
	/**
	 * @see Servers#findOne(DBObject)
	 */
	public DBUser findOne(DBObject query) {
		DBObject obj = getDBC().findOne(query);
		return obj == null ? null : new DBUser(obj);
	}
	
	/**
	 * Finds a user by UUID
	 * 
	 * @param u UUID
	 * @return DBUser or null
	 */
	public DBUser getByUUID(UUID u) {
		return findOne(new BasicDBObject(DBUser.FIELD_UUID, u.toString()));
	}
	
	/**
	 * Finds a user by name.
	 * 
	 * Note: this method is
	 * used for Buycraft support. Currently, there
	 * is no support for UUIDs in Buycraft. If this
	 * changes in the future, this method will become
	 * obsolete and methods which use it should be
	 * updated accordingly.
	 * 
	 * @param name Player's username
	 * @return DBUser or null
	 */
	public DBUser getByName(String name) {
		return findOne(new BasicDBObject(DBUser.FIELD_NAME, name));
	}
	
	/**
	 * Gets a user, or creates one _asynchronously_
	 * if they do not exist. Does not wait for create
	 * action to complete if it needs to create a user.
	 * 
	 * @param p Player to get
	 * @return DBUser
	 */
	public DBUser getUserOrCreate(Player p) {
		
		UUID uuid = p.getUniqueId();
		DBUser user = getByUUID(uuid);
		if (user != null)
			return user;
		
		user = new DBUser(uuid, p.getName());
		
		// Create final equivalent for Runnable
		final DBUser newUser = user;
		
		if (!creating.contains(uuid)) {
			creating.add(uuid);
			Database.getExecutorService().submit(() -> {
				this.save(newUser);
				this.creating.remove(uuid);
			});
		}
		
		return user;
		
	}
	
	/**
	 * Saves a user to the database, replacing
	 * users with the same UUIID.
	 * 
	 * @param user User to save
	 */
	public void createUser(DBUser user) {
		
		DBUser existing = getByUUID(user.getUUID());
		if (existing != null)
			user.setId(existing.getId());
		
		Database.getExecutorService().submit(() -> {
			this.save(user);
		});
		
	}

}
