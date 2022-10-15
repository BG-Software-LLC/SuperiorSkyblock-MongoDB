package com.bgsoftware.superiorskyblock.module.mongodb.bridge;

import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridgeMode;
import com.bgsoftware.superiorskyblock.api.data.DatabaseFilter;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.module.mongodb.MongoDBClient;
import com.bgsoftware.superiorskyblock.module.mongodb.threading.DatabaseExecutor;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class MongoDatabaseBridge implements DatabaseBridge {

    private DatabaseBridgeMode databaseBridgeMode = DatabaseBridgeMode.IDLE;
    private Map<MongoCollection<Document>, List<WriteModel<Document>>> batchOperations;

    public MongoDatabaseBridge() {
    }

    @Override
    public void loadAllObjects(String collectionName, Consumer<Map<String, Object>> resultConsumer) {
        DatabaseExecutor.execute(() -> {
            MongoCollection<Document> collection = MongoDBClient.getCollection(collectionName);
            MongoCursor<Document> cursor = collection.find().cursor();
            while (cursor.hasNext()) {
                resultConsumer.accept(cursor.next());
            }
        });
    }

    @Override
    public void batchOperations(boolean batchOperations) {
        if (batchOperations) {
            this.batchOperations = new HashMap<>();
        } else if (this.batchOperations != null) {
            DatabaseExecutor.execute(() -> {
                this.batchOperations.forEach(MongoCollection::bulkWrite);
                this.batchOperations = null;
            });
        }
    }

    @Override
    public void updateObject(String collectionName, DatabaseFilter filter, Pair<String, Object>[] columns) {
        if (databaseBridgeMode != DatabaseBridgeMode.SAVE_DATA)
            return;

        DatabaseExecutor.execute(() -> {
            MongoCollection<Document> collection = MongoDBClient.getCollection(collectionName);
            Bson query = buildFilter(filter);
            Document document = new Document().append("$set", buildDocument(columns));

            if (this.batchOperations != null) {
                this.batchOperations.computeIfAbsent(collection, c -> new ArrayList<>())
                        .add(new UpdateOneModel<>(query, document));
            } else {
                collection.updateOne(query, document);
            }
        });
    }

    @SafeVarargs
    @Override
    public final void insertObject(String collectionName, Pair<String, Object>... columns) {
        if (databaseBridgeMode != DatabaseBridgeMode.SAVE_DATA)
            return;

        DatabaseExecutor.execute(() -> {
            MongoCollection<Document> collection = MongoDBClient.getCollection(collectionName);
            MongoCursor<Document> cursor = collection.listIndexes().cursor();
            if (cursor.hasNext()) {
                _updateObject(collection, cursor.next(), buildDocument(columns));
            } else {
                _insertObject(collection, buildDocument(columns));
            }
        });
    }

    @Override
    public void deleteObject(String collectionName, DatabaseFilter filter) {
        if (databaseBridgeMode != DatabaseBridgeMode.SAVE_DATA)
            return;

        DatabaseExecutor.execute(() -> {
            MongoCollection<Document> collection = MongoDBClient.getCollection(collectionName);

            Bson query = buildFilter(filter);

            if (this.batchOperations != null) {
                this.batchOperations.computeIfAbsent(collection, c -> new ArrayList<>())
                        .add(new DeleteOneModel<>(query));
            } else {
                collection.deleteOne(query);
            }
        });
    }

    @Override
    public void loadObject(String collectionName, DatabaseFilter filter, Consumer<Map<String, Object>> resultConsumer) {
        DatabaseExecutor.execute(() -> {
            MongoCollection<Document> collection = MongoDBClient.getCollection(collectionName);
            MongoCursor<Document> cursor = collection.find(buildFilter(filter)).cursor();
            while (cursor.hasNext()) {
                resultConsumer.accept(cursor.next());
            }
        });
    }

    @Override
    public void setDatabaseBridgeMode(DatabaseBridgeMode databaseBridgeMode) {
        this.databaseBridgeMode = databaseBridgeMode;
    }

    @Override
    public DatabaseBridgeMode getDatabaseBridgeMode() {
        return this.databaseBridgeMode;
    }

    private void _updateObject(MongoCollection<Document> collection, Document filter, Document columns) {
        if (this.batchOperations != null) {
            this.batchOperations.computeIfAbsent(collection, c -> new ArrayList<>())
                    .add(new UpdateOneModel<>(filter, columns, new UpdateOptions().upsert(true)));
        } else {
            collection.updateOne(filter, columns, new UpdateOptions().upsert(true));
        }
    }

    private void _insertObject(MongoCollection<Document> collection, Document columns) {
        Document document = new Document().append("$set", columns);

        if (this.batchOperations != null) {
            this.batchOperations.computeIfAbsent(collection, c -> new ArrayList<>())
                    .add(new InsertOneModel<>(document));
        } else {
            collection.insertOne(document);
        }
    }

    private static BasicDBObject buildFilter(DatabaseFilter filter) {
        BasicDBObject query = new BasicDBObject();

        if (filter != null) {
            filter.getFilters().forEach(columnFilter -> query.append(columnFilter.getKey(), columnFilter.getValue()));
        }

        return query;
    }

    private static Document buildDocument(Pair<String, Object>[] columns) {
        Document document = new Document();

        if (columns != null) {
            for (Pair<String, Object> column : columns) {
                document.append(column.getKey(), column.getValue());
            }
        }

        return document;
    }

}
