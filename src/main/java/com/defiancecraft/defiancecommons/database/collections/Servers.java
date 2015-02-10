package com.defiancecraft.defiancecommons.database.collections;

import com.defiancecraft.defiancecommons.database.documents.DBServer;
import com.mongodb.BasicDBObject;

public class Servers extends Collection {

	public String getCollectionName() {
		return "servers";
	}
	
	public DBServer getByName(String name) {
		return new DBServer(getDBC().findOne(new BasicDBObject(DBServer.FIELD_NAME, name)));
	}

}
