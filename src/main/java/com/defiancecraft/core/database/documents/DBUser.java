package com.defiancecraft.core.database.documents;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.defiancecraft.core.DefianceCore;
import com.defiancecraft.core.database.UniqueField;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class DBUser extends Document {

	@UniqueField
	public static final String FIELD_UUID = "uuid";
	public static final String FIELD_NAME = "name";
	public static final String FIELD_GROUPS = "groups";
	public static final String FIELD_CUSTOM_PREFIX = "custom_prefix";
	public static final String FIELD_CUSTOM_SUFFIX = "custom_suffix";
	public static final String FIELD_BALANCE = "balance";
	
	public DBUser(DBObject obj) {
		super(obj);
	}
	
	public DBUser(UUID uuid, String name, Date time) {
		super(new BasicDBObject());
		getDBO().put(FIELD_UUID, uuid.toString());
		getDBO().put(FIELD_NAME, name);
		getDBO().put(FIELD_GROUPS, DefianceCore
					.getPermissionManager()
					.getConfig()
					.defaultGroups);
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
	
	public String getCustomPrefix() {
		return getString(FIELD_CUSTOM_PREFIX);
	}
	
	public String getCustomSuffix() {
		return getString(FIELD_CUSTOM_SUFFIX);
	}
	
	public double getBalance() {
		return getDouble(FIELD_BALANCE);
	}
	
}
