package com.bgsoftware.superiorskyblock.module.mongodb;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public final class MongoDBInitializer {

    public static void initDatabase(SuperiorSkyblock plugin) {
        if (!containsGrid()) {
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
        MongoDBClient.createIndex("islands", "uuid");
        MongoDBClient.createIndex("islands_banks", "island");
        MongoDBClient.createIndex("islands_bans", "island", "player");
        MongoDBClient.createIndex("islands_block_limits", "island", "block");
        MongoDBClient.createIndex("islands_chests", "island", "`index`");
        MongoDBClient.createIndex("islands_custom_data", "island");
        MongoDBClient.createIndex("islands_effects", "island", "effect_type");
        MongoDBClient.createIndex("islands_entity_limits", "island", "entity");
        MongoDBClient.createIndex("islands_flags", "island", "name");
        MongoDBClient.createIndex("islands_generators", "island", "environment", "block");
        MongoDBClient.createIndex("islands_homes", "island", "environment");
        MongoDBClient.createIndex("islands_members", "island", "player");
        MongoDBClient.createIndex("islands_missions", "island", "name");
        MongoDBClient.createIndex("islands_player_permissions", "island", "player", "permission");
        MongoDBClient.createIndex("islands_ratings", "island", "player");
        MongoDBClient.createIndex("islands_role_limits", "island", "role");
        MongoDBClient.createIndex("islands_role_permissions", "island", "permission");
        MongoDBClient.createIndex("islands_settings", "island");
        MongoDBClient.createIndex("islands_upgrades", "island", "upgrade");
        MongoDBClient.createIndex("islands_visitor_homes", "island", "environment");
        MongoDBClient.createIndex("islands_visitors", "island", "player");
        MongoDBClient.createIndex("islands_warp_categories", "island", "name");
        MongoDBClient.createIndex("islands_warps", "island", "name");
        MongoDBClient.createIndex("players", "uuid");
        MongoDBClient.createIndex("players_custom_data", "player");
        MongoDBClient.createIndex("players_missions", "player", "name");
        MongoDBClient.createIndex("players_settings", "player");
        MongoDBClient.createIndex("stacked_blocks", "location");
    }


}
