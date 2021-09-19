package com.bgsoftware.superiorskyblock.module.mongodb.bridge;

import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.factory.DatabaseBridgeFactory;
import com.bgsoftware.superiorskyblock.api.handlers.GridManager;
import com.bgsoftware.superiorskyblock.api.handlers.StackedBlocksManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import javax.annotation.Nullable;

public final class MongoDatabaseBridgeFactory implements DatabaseBridgeFactory {

    @Override
    public DatabaseBridge createIslandsDatabaseBridge(@Nullable Island island, DatabaseBridge databaseBridge) {
        return new MongoDatabaseBridge();
    }

    @Override
    public DatabaseBridge createPlayersDatabaseBridge(@Nullable SuperiorPlayer superiorPlayer, DatabaseBridge databaseBridge) {
        return new MongoDatabaseBridge();
    }

    @Override
    public DatabaseBridge createGridDatabaseBridge(@Nullable GridManager gridManager, DatabaseBridge databaseBridge) {
        return new MongoDatabaseBridge();
    }

    @Override
    public DatabaseBridge createStackedBlocksDatabaseBridge(@Nullable StackedBlocksManager stackedBlocksManager, DatabaseBridge databaseBridge) {
        return new MongoDatabaseBridge();
    }

}
