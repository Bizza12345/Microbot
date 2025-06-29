package net.runelite.client.plugins.microbot.skeletonstate;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import java.util.concurrent.*;

public class SkeletonStateScript extends Script {
    private SkeletonState state = SkeletonState.IDLE;
    private boolean bankTaskStarted = false;

    /** Secondary executor for concurrent tasks */
    private final ScheduledExecutorService workerService = Executors.newScheduledThreadPool(2);

    /** Example secondary scheduled task */
    private ScheduledFuture<?> secondaryTask;

    public String getStateName() {
        return state.name();
    }

    public boolean run(SkeletonStateConfig config) {
        Microbot.log("Starting SkeletonStateScript main loop");
        state = SkeletonState.WALK_TO_GE;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;

                switch (state) {
                    case WALK_TO_GE:
                        Microbot.status = "Walking to GE";
                        Rs2Walker.walkWithState(BankLocation.GRAND_EXCHANGE.getWorldPoint(), 10);
                        int distance = Rs2Player.getWorldLocation().distanceTo(BankLocation.GRAND_EXCHANGE.getWorldPoint());
                        if (distance < 30 && !bankTaskStarted) {
                            bankTaskStarted = true;
                            submitAsyncTask(() -> {
                                Microbot.log("Hovering banker");
                                Rs2Bank.preHover();
                                sleep(200, 400);
                                Microbot.log("Attempting to open bank");
                                Rs2Bank.openBank();
                            });
                        }
                        if (Rs2Bank.isOpen()) {
                            Microbot.log("Bank opened, switching to idle");
                            state = SkeletonState.IDLE;
                        }
                        break;
                    default:
                        Microbot.status = "Idle";
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


    @Override
    public void shutdown() {
        if (secondaryTask != null && !secondaryTask.isCancelled()) {
            secondaryTask.cancel(true);
        }
        workerService.shutdownNow();
        super.shutdown();
    }
}
