package com.bgsoftware.superiorskyblock.module.mongodb.bridge;

import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridgeMode;
import com.bgsoftware.superiorskyblock.api.data.DatabaseFilter;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.module.mongodb.MongoDBClient;
import com.bgsoftware.superiorskyblock.module.mongodb.threading.DatabaseExecutor;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.DeleteManyModel;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.WriteModel;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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
        MongoCollection<Document> collection = MongoDBClient.getCollection(collectionName);
        try (MongoCursor<Document> cursor = collection.find().cursor()) {
            while (cursor.hasNext()) {
                resultConsumer.accept(cursor.next());
            }
        }
    }

    @Override
    public void batchOperations(boolean batchOperations) {
        if (batchOperations) {
            this.batchOperations = new HashMap<>();
        } else if (this.batchOperations != null) {
            Map<MongoCollection<Document>, List<WriteModel<Document>>> batchOperationsCopy = this.batchOperations;
            this.batchOperations = null;
            DatabaseExecutor.execute(() -> batchOperationsCopy.forEach(MongoCollection::bulkWrite));
        }
    }

    @Override
    public void updateObject(String collectionName, DatabaseFilter filter, Pair<String, Object>[] columns) {
        if (databaseBridgeMode != DatabaseBridgeMode.SAVE_DATA)
            return;

        DatabaseExecutor.execute(() -> {
            MongoCollection<Document> collection = MongoDBClient.getCollection(collectionName);
            Bson query = buildFilter(filter);
            Document updateOperation = new Document("$set", buildColumns(columns));

            if (this.batchOperations != null) {
                this.batchOperations.computeIfAbsent(collection, c -> new ArrayList<>())
                        .add(new UpdateManyModel<>(query, updateOperation));
            } else {
                collection.updateMany(query, updateOperation);
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
            List<String> filteredFields = MongoDBClient.getCachedIndex(collectionName);
            if (filteredFields == null) {
                _insertObject(collection, buildColumns(columns));
            } else {
                List<Bson> filters = new LinkedList<>();

                for (Pair<String, Object> column : columns) {
                    if (filteredFields.contains(column.getKey())) {
                        filters.add(Filters.eq(column.getKey(), column.getValue()));
                    }
                }

                _replaceObject(collection, buildFiltersFromList(filters), buildColumns(columns));
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
                        .add(new DeleteManyModel<>(query));
            } else {
                collection.deleteMany(query);
            }
        });
    }

    @Override
    public void loadObject(String collectionName, DatabaseFilter filter, Consumer<Map<String, Object>> resultConsumer) {
        MongoCollection<Document> collection = MongoDBClient.getCollection(collectionName);
        try (MongoCursor<Document> cursor = collection.find(buildFilter(filter)).cursor()) {
            while (cursor.hasNext()) {
                resultConsumer.accept(cursor.next());
            }
        }
    }

    @Override
    public void setDatabaseBridgeMode(DatabaseBridgeMode databaseBridgeMode) {
        this.databaseBridgeMode = databaseBridgeMode;
    }

    @Override
    public DatabaseBridgeMode getDatabaseBridgeMode() {
        return this.databaseBridgeMode;
    }

    private void _replaceObject(MongoCollection<Document> collection, Bson filter, Document columns) {
        if (this.batchOperations != null) {
            this.batchOperations.computeIfAbsent(collection, c -> new ArrayList<>())
                    .add(new ReplaceOneModel<>(filter, columns, new ReplaceOptions().upsert(true)));
        } else {
            collection.replaceOne(filter, columns, new ReplaceOptions().upsert(true));
        }
    }

    private void _insertObject(MongoCollection<Document> collection, Document columns) {
        if (this.batchOperations != null) {
            this.batchOperations.computeIfAbsent(collection, c -> new ArrayList<>())
                    .add(new InsertOneModel<>(columns));
        } else {
            collection.insertOne(columns);
        }
    }

    private static Bson buildFilter(@Nullable DatabaseFilter filter) {
        if (filter == null || filter.getFilters().isEmpty())
            return Filters.empty();

        List<Bson> filters = new LinkedList<>();

        filter.getFilters().forEach(columnFilter ->
                filters.add(Filters.eq(columnFilter.getKey(), columnFilter.getValue())));

        return buildFiltersFromList(filters);
    }

    private static Bson buildFiltersFromList(List<Bson> filters) {
        return filters.isEmpty() ? Filters.empty() : filters.size() == 1 ? filters.get(0) : Filters.and(filters);
    }

    private static Document buildColumns(Pair<String, Object>[] columns) {
        Document document = new Document();

        if (columns != null) {
            for (Pair<String, Object> column : columns) {
                document.put(column.getKey(), column.getValue());
            }
        }

        return document;
    }

}
