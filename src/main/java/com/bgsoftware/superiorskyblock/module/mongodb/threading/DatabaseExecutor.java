package com.bgsoftware.superiorskyblock.module.mongodb.threading;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class DatabaseExecutor {

    private static final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
            .setNameFormat("SuperiorSkyblock MongoDB Thread").build());

    private static boolean shutdown = false;

    public static void execute(Runnable runnable) {
        if(shutdown)
            return;

        if(isDatabaseThread()) {
            runnable.run();
        }
        else {
            databaseExecutor.execute(runnable);
        }
    }

    public static void shutdown() {
        try {
            shutdown = true;
            shutdownAndAwaitTermination();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static boolean isDatabaseThread() {
        return Thread.currentThread().getName().contains("SuperiorSkyblock MongoDB Thread");
    }

    private static void shutdownAndAwaitTermination() {
        databaseExecutor.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!databaseExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                databaseExecutor.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!databaseExecutor.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            databaseExecutor.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

}
