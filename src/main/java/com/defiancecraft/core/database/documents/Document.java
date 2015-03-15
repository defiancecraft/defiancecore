package com.defiancecraft.core.database.documents;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.Binary;
import org.bson.types.ObjectId;

import com.mongodb.DBObject;
import com.mongodb.DBRef;

public class Document {

	public static final String FIELD_ID = "_id";
	protected DBObject dbo;
	
	public Document(DBObject obj) {
		
		this.dbo = obj;
		
	}
	
	/**
	 * Gets the _id field of this Document
	 * @return ObjectId
	 */
	public ObjectId getId() {
		
		return getObjectId(FIELD_ID);
		
	}
	
	/**
	 * Sets the _id field of this Document
	 * @param id ID to set to
	 */
	public void setId(ObjectId id) {
		
		this.dbo.put(FIELD_ID, id);
		
	}
	
	/**
	 * Gets the DBObject associated with 
	 * this document.
	 * @return DBObject
	 */
	public DBObject getDBO() {
		
		return this.dbo;
		
	}
	
	/**
	 * Shortcut for {@link #getString(String, String)}, with
	 * default as null
	 * 
	 * @see #getString(String, String)
	 */
	protected String getString(String field) {
		
		return getString(field, null);
		
	}
	
	/**
	 * Retrieves a String from the DBObject
	 * 
	 * @param field Field to get
	 * @param def Default value if not found
	 * @return The string, or `def` on failure
	 */
	protected String getString(String field, String def) {
		
		Object obj = dbo.get(field);
		
		if (obj instanceof String)
			return (String) obj;
		else if (obj != null)
			return obj.toString();
		
		return def;
		
	}
	
	/**
	 * Shortcut for {@link #getInt(String, int)}, with
	 * default as 0
	 * 
	 * @see #getInt(String, int)
	 */
	protected Integer getInt(String field) {
		
		return getInt(field, 0);
		
	}
	
	/**
	 * Retrieves an integer from the DBObject,
	 * converting Strings and Booleans to one
	 * if necessary.
	 * 
	 * @param field Field to get
	 * @param def Default value 
	 * @return The integer, or `def` on failure.
	 */
	protected Integer getInt(String field, int def) {
		
		Object obj = dbo.get(field);
		
		if (obj instanceof Number)
			return ((Number)obj).intValue();
		else if (obj instanceof Boolean)
			return ((Boolean)obj) ? 1 : 0;
		else if (obj instanceof String)
			try {
				return Integer.parseInt((String)obj);
			} catch (NumberFormatException e) {}
		
		return def;
		
	}
	
	/**
	 * Shortcut for {@link #getBoolean(String, boolean)}, with
	 * default as false
	 * 
	 * @see #getBoolean(String, boolean)
	 */
	protected Boolean getBoolean(String field) {
		
		return getBoolean(field, false);
		
	}
	
	/**
	 * Retrieves a boolean from the DBObject,
	 * converting from strings or numbers if necessary.
	 * 
	 * @param field Field to get
	 * @param def Default value
	 * @return The boolean, or `def` on failure
	 */
	protected Boolean getBoolean(String field, boolean def) {
		
		Object obj = dbo.get(field);
		
		if (obj instanceof Boolean)
			return (Boolean)obj;
		else if (obj instanceof Number)
			return ((Number)obj).intValue() == 1;
		else if (obj instanceof String)
			return ((String)obj).equalsIgnoreCase("true") || ((String)obj).equals("1");
		
		return def;
		
	}
	
	/**
	 * Shortcut for {@link #getObjectId(String, ObjectId)}, with
	 * default as null.
	 * 
	 * @see #getObjectId(String, ObjectId)
	 */
	protected ObjectId getObjectId(String field) {
		
		return getObjectId(field, null);
		
	}
	
	/**
	 * Retrieves an ObjectId from the DBObject
	 * 
	 * @param field Field to get
	 * @param def Default value if cannot convert to ObjectId
	 * @return ObjectId
	 */
	protected ObjectId getObjectId(String field, ObjectId def) {
		
		Object obj = dbo.get(field);
		
		try {
			
			if (obj instanceof String)
				return new ObjectId((String)obj);
			else if (obj instanceof ObjectId)
				return (ObjectId)obj;
			
		} catch (IllegalArgumentException e) {}
		
		return def;
		
	}

	/**
	 * Shortcut for {@link #getStringList(String, List)}, with
	 * default as null.
	 * 
	 * @see #getStringList(String, List)
	 */
	protected List<String> getStringList(String field) {
		
		return getStringList(field, null);
		
	}
	
	/**
	 * Retrieves a list of Strings from the DBObject
	 * 
	 * @param field Field to get
	 * @param def Default value if field is null
	 * @return List<String>, or `def` on failure
	 */
	protected List<String> getStringList(String field, List<String> def) {
		
		Object obj = dbo.get(field);
		List<String> list = new ArrayList<String>();
		
		if (obj == null || !(obj instanceof List<?>))
			return def;
		
		for (Object o : (List<?>)obj)
			list.add(o.toString());
			
		return list;
		
	}
	
	/**
	 * Shortcut for {@link #getDate(String, Date)}, with
	 * default as null.
	 * 
	 * @see #getDate(String, Date)
	 */
	protected Date getDate(String field) {
		
		return getDate(field, null);
		
	}
	
	/**
	 * Retrieves a Date object from the DBObject
	 * 
	 * @param field Field to get
	 * @param def Default value if field is null
	 * @return Date, or `def` on failure
	 */
	protected Date getDate(String field, Date def) {
		
		Object obj = dbo.get(field);
		
		if (obj != null && obj instanceof Date)
			return (Date)obj;
		
		return def;
		
	}
	
	/**
	 * Shortcut for {@link #getDouble(String, Double)}, with
	 * default as 0.
	 * 
	 * @see #getDouble(String, Double)
	 */
	protected Double getDouble(String field) {
		return getDouble(field, 0d);
	}
	
	/**
	 * Retrieves a double from the DBObject
	 * 
	 * @param field Field to get
	 * @param def Default value if field is null or cannot convert to double
	 * @return Double, or `def` on failure
	 */
	protected Double getDouble(String field, Double def) {
		
		Object obj = dbo.get(field);
		
		if (obj instanceof Number)
			return ((Number)obj).doubleValue();
		else if (obj instanceof Boolean)
			return ((Boolean)obj) ? 1d : 0d;
		else if (obj instanceof String)
			try {
				return Double.parseDouble((String)obj);
			} catch (NumberFormatException e) {}
		
		return def;
		
	}
	
	/**
	 * Shortcut for {@link #getDBRef(String, DBRef)}, with
	 * default as null.
	 * 
	 * @see #getDBRef(String, DBRef)
	 */
	protected DBRef getDBRef(String field) {
		
		return getDBRef(field, null);
		
	}
	
	/**
	 * Retrieves a DBRef from the DBObject
	 * 
	 * @param field Field to get
	 * @param def Default value if field is null
	 * @return DBRef, or `def` on failure
	 */
	protected DBRef getDBRef(String field, DBRef def) {
		
		Object obj = dbo.get(field);
		
		if (obj instanceof DBRef)
			return (DBRef)obj;
		
		return def;
		
	}
	
	/**
	 * Shortcut for {@link #getDBObject(String, DBObject)}, with
	 * default as null.
	 * 
	 * @see #getDBObject(String, DBObject)
	 */
	protected DBObject getDBObject(String field) {
		
		return getDBObject(field, null);
		
	}
	
	/**
	 * Retrieves a DBObject (embedded document) from the DBObject
	 * 
	 * @param field Field to get
	 * @param def Default value if field is null
	 * @return DBObject, or `def` on failure
	 */
	protected DBObject getDBObject(String field, DBObject def) {
		
		Object obj = dbo.get(field);
		
		if (obj instanceof DBObject)
			return (DBObject)obj;
		
		return def;
		
	}
	
	/**
	 * Shortcut for {@link #getDBObjectList(String, List<DBObject>)}, with
	 * default as null.
	 * 
	 * @see #getDBObjectList(String, List<DBObject>)
	 */
	protected List<DBObject> getDBObjectList(String field) {
		
		return getDBObjectList(field, new ArrayList<DBObject>());
		
	}
	
	/**
	 * Retrieves a list of DBObjects (embedded documents) from the DBObject
	 * 
	 * @param field Field to get
	 * @param def Default value if field is null
	 * @return List<DBObject>, or `def` on failure
	 */
	protected List<DBObject> getDBObjectList(String field, List<DBObject> def) {
		
		Object obj = dbo.get(field);
		List<DBObject> list = new ArrayList<DBObject>();
		
		if (obj == null || !(obj instanceof List<?>))
			return def;
		
		for (Object o : (List<?>)obj)
			if (o instanceof DBObject)
				list.add((DBObject) o);
		
		return list;
		
	}

	/**
	 * Shortcut for {@link #getByte(String, byte)}, with
	 * default as null.
	 * 
	 * @see #getByte(String, byte)
	 */
	protected byte getByte(String field) {
		
		return getByte(field, (byte)0);
		
	}
	
	/**
	 * Retrieves a byte value from the DBObject
	 * 
	 * @param field Field to get
	 * @param def Default value if field is null
	 * @return byte, or `def` on failure
	 */
	protected byte getByte(String field, byte def) {
		
		Object obj = dbo.get(field);
		
		if (obj instanceof Number)
			return ((Number)obj).byteValue();
		else if (obj instanceof String)
			try {
				return Byte.parseByte((String)obj);
			} catch (NumberFormatException e) {}
		
		return def;
		
	}
	
	/**
	 * Shortcut for {@link #getByteArray(String, byte[])}, with
	 * default as null.
	 * 
	 * @see #getByteArray(String, byte[])
	 */
	protected byte[] getByteArray(String field) {
		
		return getByteArray(field, new byte[]{});
		
	}
	
	/**
	 * Retrieves a byte array from the DBObject
	 * 
	 * @param field Field to get
	 * @param def Default value if field is null
	 * @return byte[], or `def` on failure
	 */
	protected byte[] getByteArray(String field, byte[] def) {
		
		Object obj = dbo.get(field);
		
		if (obj instanceof Binary)
			return ((Binary)obj).getData();
		
		return def;
		
	}
	
}
