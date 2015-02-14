package com.defiancecraft.defiancecommons.database.documents;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.defiancecraft.defiancecommons.DefianceCommons;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class DBUser extends Document {

	public static final String FIELD_UUID = "uuid";
	public static final String FIELD_NAME = "name";
	public static final String FIELD_LAST_UPDATED = "last_updated";
	public static final String FIELD_GROUPS = "groups";
	
	public DBUser(DBObject obj) {
		super(obj);
	}
	
	public DBUser(UUID uuid, String name, Date time) {
		super(new BasicDBObject());
		getDBO().put(FIELD_UUID, uuid.toString());
		getDBO().put(FIELD_NAME, name);
		getDBO().put(FIELD_GROUPS, DefianceCommons
					.getPermissionManager()
					.getConfig()
					.defaultGroups);
		getDBO().put(FIELD_LAST_UPDATED, time);
	}
	
	public DBUser(UUID uuid, String name) {
		this(uuid, name, new Date(System.currentTimeMillis()));
	}
	
	public UUID getUUID() {
		return UUID.fromString(getString(FIELD_UUID)); 
	}

	public String getName() {
		return getString(FIELD_NAME);
	}
	
	public List<String> getGroups() {
		return getStringList(FIELD_GROUPS);
	}

	public void addGroup(String group) {
		
		List<String> groups = getStringList(FIELD_GROUPS, new ArrayList<String>());
		if (!groups.contains(group))
			groups.add(group);
		getDBO().put(FIELD_GROUPS, groups);
		
	}
	
}
