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

import org.testng.Assert;
import org.testng.annotations.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.tensorwrench.testng.mongo.MongoData;
import com.tensorwrench.testng.mongo.MongoTestNG;

import static org.testng.Assert.*;

@Test
public class MongoTestTest extends MongoTestNG {
	
	public void createsClient() throws Exception {
		assertNotNull(getMongoDB());
		assertNotNull(getMongoClient());
	}
	
	
	@Test(expectedExceptions={AssertionError.class}, expectedExceptionsMessageRegExp="MongoDB data file not found on classpath.*")
	public void failsOnMissingDatafile() throws Exception{
		mongoTestDriver.loadData("/doesNotExist.json");
	}
	
	@MongoData("/mongoTest.json")
	public void loadsData() throws Exception {
		DBCollection col=getMongoDB().getCollection("documents");
		DBCursor cursor=col.find();
		Assert.assertEquals(cursor.count(),2);
	}
	
	@MongoData("/mongoTest.json")
	public void loadsMultipleCollectoins() throws Exception {
		DBCollection principalCol=getMongoDB().getCollection("documents");
		DBCollection otherCol=getMongoDB().getCollection("others");

		DBCursor otherCursor=otherCol.find();
		DBCursor principalCursor=principalCol.find();
		
		Assert.assertEquals(otherCursor.count(),5);
		Assert.assertEquals(principalCursor.count(),2);
	}
	
	@MongoData("/mongoTest.json")
	public void wipesDataPart1() throws Exception {
		DBCollection col=getMongoDB().getCollection("documents");
		col.insert(new BasicDBObject("foo","bar"));

		DBCursor cursor=col.find();
		Assert.assertEquals(cursor.count(),3);
	}
	
	@MongoData("/mongoTest.json") @Test(dependsOnMethods={"wipesDataPart1"})
	public void wipesDataPart2() throws Exception {
		DBCollection col=getMongoDB().getCollection("documents");
		DBCursor cursor=col.find();
		Assert.assertEquals(cursor.count(),2);
	}
	
}
