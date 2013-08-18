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

import java.io.IOException;
import java.lang.reflect.Method;

import org.testng.annotations.BeforeMethod;

import com.mongodb.DB;
import com.mongodb.MongoClient;

/**
 * Base class for TestNG tests that need a pre-populated MongoDB.  Before
 * each test method, checks for the MongoData annotation and uses it to
 * populate the database.
 */
public class MongoTestNG  {
	
	MongoTestDriver mongoTestDriver=new MongoTestDriver();
	
	/**
	 * Runs before each method and sets up the database based upon the annotation.
	 */
	@BeforeMethod
	public void setupTestNGMongoDb(Method method) throws IOException {
		mongoTestDriver.loadData(method);
	}

	/** gets the currently active mongoClient */
	public MongoClient getMongoClient() {
		return mongoTestDriver.getMongoClient();
	}
	
	/** gets the currently active mongoDB */
	public DB getMongoDB() {
		return mongoTestDriver.getMongoDB();
	}
}
