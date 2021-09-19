package com.bgsoftware.superiorskyblock.module.mongodb;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;

public final class MongoDBInitializer {

    public static void initDatabase(SuperiorSkyblock plugin) {
        if(!containsGrid()) {
            MongoCollection<Document> collection = MongoDBClient.getCollection("grid");
            String worldName = plugin.getSettings().getWorlds().getWorldName();
            collection.insertOne(new Document()
                    .append("last_island", worldName + ", 0, 100, 0")
                    .append("max_island_size", plugin.getSettings().getMaxIslandSize())
                    .append("world", worldName)
            );
        }

        createIndexes();
    }

    private static boolean containsGrid() {
        MongoCollection<Document> collection = MongoDBClient.getCollection("grid");
        return collection.find().cursor().hasNext();
    }

    private static void createIndexes() {
        createIndex("islands_bans", "island", "player");
        createIndex("islands_block_limits", "island", "block");
        createIndex("islands_chests", "island", "`index`");
        createIndex("islands_effects", "island", "effect_type");
        createIndex("islands_entity_limits", "island", "entity");
        createIndex("islands_flags", "island", "name");
        createIndex("islands_generators", "island", "environment", "block");
        createIndex("islands_homes", "island", "environment");
        createIndex("islands_members", "island", "player");
        createIndex("islands_missions", "island", "name");
        createIndex("islands_player_permissions", "island", "player", "permission");
        createIndex("islands_ratings", "island", "player");
        createIndex("islands_role_limits", "island", "role");
        createIndex("islands_role_permissions", "island", "permission");
        createIndex("islands_upgrades", "island", "upgrade");
        createIndex("islands_visitor_homes", "island", "environment");
        createIndex("islands_visitors", "island", "player");
        createIndex("islands_warp_categories", "island", "name");
        createIndex("islands_warps", "island", "name");
        createIndex("players_missions", "player", "name");
    }

    private static void createIndex(String collectionName, String... fieldNames) {
        MongoCollection<Document> collection = MongoDBClient.getCollection(collectionName);
        collection.createIndex(Indexes.ascending(fieldNames), new IndexOptions().unique(true));
    }

}
