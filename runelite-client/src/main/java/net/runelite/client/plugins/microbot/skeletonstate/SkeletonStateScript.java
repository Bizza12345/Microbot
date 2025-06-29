package net.runelite.client.plugins.microbot.skeletonstate;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;

import java.util.concurrent.*;

public class SkeletonStateScript extends Script {
    private SkeletonState state = SkeletonState.IDLE;

    /** Secondary executor for concurrent tasks */
    private final ScheduledExecutorService workerService = Executors.newScheduledThreadPool(2);

    /** Example secondary scheduled task */
    private ScheduledFuture<?> secondaryTask;

    public boolean run(SkeletonStateConfig config) {
        Microbot.log("Starting SkeletonStateScript main loop");
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;

                if (hasStateChanged()) {
                    SkeletonState newState = updateState();
                    Microbot.log("State changed: " + state + " -> " + newState);
                    state = newState;
                }

                switch (state) {
                    case FETCHING:
                        Microbot.status = "Fetching";
                        // TODO: fetching logic
                        break;
                    case PROCESSING:
                        Microbot.status = "Processing";
                        // TODO: processing logic
                        break;
                    default:
                        Microbot.status = "Idle";
                        // TODO: idle logic
                        break;
                }
            } catch (Exception ex) {
                Microbot.log(ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);

        secondaryTask = workerService.scheduleWithFixedDelay(() -> {
            if (!Microbot.isLoggedIn() || mainScheduledFuture.isCancelled()) {
                return;
            }
            Microbot.log("Secondary task tick");
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    /**
     * Submit an asynchronous action on the worker service
     * @param task runnable to execute
     */
    public void submitAsyncTask(Runnable task) {
        workerService.submit(() -> {
            Microbot.log("Async task started");
            try {
                task.run();
            } catch (Exception ex) {
                Microbot.log("Async task error: " + ex.getMessage());
            }
            Microbot.log("Async task finished");
        });
    }

    private boolean hasStateChanged() {
        SkeletonState newState = updateState();
        return state != newState;
    }

    private SkeletonState updateState() {
        // TODO: determine state based on conditions
        return SkeletonState.IDLE;
    }

    @Override
    public void shutdown() {
        if (secondaryTask != null && !secondaryTask.isCancelled()) {
            secondaryTask.cancel(true);
        }
        workerService.shutdownNow();
        super.shutdown();
    }
}
