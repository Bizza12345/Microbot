package net.runelite.client.plugins.microbot.skeletonstate;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;

import java.util.concurrent.TimeUnit;

public class SkeletonStateScript extends Script {
    private SkeletonState state = SkeletonState.IDLE;

    public boolean run(SkeletonStateConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;

                if (hasStateChanged()) {
                    state = updateState();
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
        return true;
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
        super.shutdown();
    }
}
