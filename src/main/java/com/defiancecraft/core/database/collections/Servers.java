package com.defiancecraft.core.database.collections;

import com.defiancecraft.core.database.documents.DBServer;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class Servers extends Collection {

	public String getCollectionName() {
		return "servers";
	}
	
	/**
	 * Finds a DBServer based on query; uses this
	 * rather than generics solution due to performance.
	 * 
	 * Speed tests which tested use of generics vs this
	 * showed ~0.2s taken off the time to retrieve 5000
	 * documents on a local MongoDB server. 
	 * 
	 * @param query Query to find object
	 * @return DBServer or null
	 */
	public DBServer findOne(DBObject query) {
		DBObject obj = getDBC().findOne(query);
		return obj == null ? null : new DBServer(obj);
	}
	
	/**
	 * Gets a server by name
	 * 
	 * @param name Name of server
	 * @return DBServer or null
	 */
	public DBServer getByName(String name) {
		return findOne(new BasicDBObject(DBServer.FIELD_NAME, name));
	}

}
