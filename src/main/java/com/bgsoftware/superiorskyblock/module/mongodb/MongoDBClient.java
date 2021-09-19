package com.bgsoftware.superiorskyblock.module.mongodb;

import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public final class MongoDBClient {

    private static MongoClient mongoClient;
    private static MongoDatabase database;

    private MongoDBClient() {

    }

    public static void connect(String url, String databaseName) {
        mongoClient = new MongoClient(new MongoClientURI(url));
        mongoClient.startSession(); // Makes sure connection is valid.
        database = mongoClient.getDatabase(databaseName);
    }

    public static void close() {
        mongoClient.close();
    }

    public static MongoCollection<Document> getCollection(String collectionName) {
        return database.getCollection(collectionName);
    }

    public static void createCollection(String collectionName) {
        database.createCollection(collectionName);
    }

}
