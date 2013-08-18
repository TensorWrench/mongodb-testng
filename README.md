MongoTestNG is a base class used for doing setup and teardown of MongoDB database before unit tests
that use TestNG.  The @MongoData("/file.json") annotation designates a test as using MongoDB.

Data files should be on the classpath for the test and are JSON files that contain an object with one or or more fields.  Each field name is
the name of a collection, and the value is an array of values inserted into that collection.

    { 
      "collectionName" : [ ], 
      "collectionName2" : [] 
    }

This class is intended to be used by a test runner-specific interceptor.

TODO:
*  Skip clear and load if database isn't modified since last test and same data. (via readOnly option on MongoData?)
*  Separate out the test driver, make a JUnit implementation.
*  Let each test have a separate database and not clear it, allowing for post-test inspection of the results.
