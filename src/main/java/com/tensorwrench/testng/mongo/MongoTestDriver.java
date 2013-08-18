/*
Copyright 2013 TensorWrench, LLC 

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.
*/
package com.tensorwrench.testng.mongo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.util.List;

import org.bson.BSONObject;
import org.testng.Assert;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.mongodb.util.JSON;

/**
 * The MongoTestDriver inspects a test method for the MongoData annotation and sets up
 * the database with the referenced data.  The database is cleared at the beginning of
 * each test, before the test data is loaded.  
 * <p>
 * Data files should be on the classpath for the test
 * and are JSON files that contain an object with one or or more fields.  Each field name is
 * the name of a collection, and the value is an array of values inserted into that collection.
 * <p>
 * <code> { "collectionName" : [ ], "collectionName2" : [] }</code>
 * <p>
 * This class is intended to be used by a test runner-specific interceptor.
 * <p>TODO: <ol>
 * <li> Skip clear and load if database isn't modified since last test and same data. (via readOnly option on MongoData?)
 * <li> Separate out the test driver, make a JUnit implementation.
 * <li> Let each test have a separate database and not clear it, allowing for post-test inspection of the results.
 * </ol>
 */
public class MongoTestDriver {
	protected MongoClient mongoClient;
	protected DB mongoDB;
	protected String dbName="integrationTestDB";
	
	public MongoTestDriver() {
		try {
			mongoClient=new MongoClient();
			mongoDB=mongoClient.getDB(dbName);
			mongoDB.setWriteConcern(WriteConcern.ACKNOWLEDGED);
		} catch (UnknownHostException e) {
			Assert.fail("Failed to create Mongo Client", e);
		}
	}

	/** Reference to the mongo client currently in use */
	public MongoClient getMongoClient() {
		return mongoClient;
	}
	
	/** Reference to the currently active database */
	public DB getMongoDB() {
		return mongoDB;
	}
	
	/** Finds the MongoData annotation for the given method and sets up the database if present.
	 *  This looks for the annotation on the method, then the class that declared the method, and
	 *  finally the ancestor classes of the method. 
	*/
	public void loadData(Method method) throws IOException {
		MongoData data=findDataAnnotation(method);
		if(data != null) {
			loadData(data.value());
		}
	}

	/**
	 * Opens the given file (from the classpath) and loads the data contained within.
	 * @throws IOException
	 */
	public void loadData(String file) throws IOException {
		// pave the database
		mongoDB.dropDatabase();

		// load the data
		InputStream in=getClass().getResourceAsStream(file);
		if(in==null) {
			Assert.fail("MongoDB data file not found on classpath: " + file);
		}
		importStream(mongoDB,in);
	}
	
	
	/**
	 * Reads JSON from the input stream to populate the database.
	 * @param db
	 * @param in
	 * @throws IOException
	 */
	private void importStream(DB db, InputStream in) throws IOException {
		BufferedReader reader=new BufferedReader(new InputStreamReader(in));
		StringBuilder sb=new StringBuilder();
		String read = reader.readLine();
		while(read != null) {
	    sb.append(read);
	    read =reader.readLine();
		}
		
		BSONObject obj=(BSONObject) JSON.parse(sb.toString());
		for(String collection:obj.keySet()) {
			@SuppressWarnings("unchecked")
			List<DBObject> docs=(List<DBObject>) obj.get(collection);
			
			DBCollection col=db.getCollection(collection);
			for(DBObject o: docs) {
				col.save(o);
			}
		}
	}
	
	/** Searches the method, declaring class, and superclasses for a MongoData annotation */
	protected MongoData findDataAnnotation(Method m) {
		if(m == null) {
			return null;
		}
		MongoData data=m.getAnnotation(MongoData.class);
		
		// not on method, check class hierarchy
		Class<?> c=m.getDeclaringClass();
		while(c != null && data ==null) {
			data=c.getAnnotation(MongoData.class);
			c=c.getSuperclass();
		}
		
		return data;
	}
}
