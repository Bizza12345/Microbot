package net.runelite.client.plugins.microbot.bizzaaiofighter.combat;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.bizzaaiofighter.BizzaAIOFighterConfig;
import net.runelite.client.plugins.microbot.bizzaaiofighter.BizzaAIOFighterPlugin;
import net.runelite.client.plugins.microbot.bizzaaiofighter.enums.State;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import java.util.concurrent.TimeUnit;

public class SafeSpot extends Script {

    public WorldPoint currentSafeSpot = null;
    private boolean messageShown = false;

public boolean run(BizzaAIOFighterConfig config) {
    mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
        try {
            if (BizzaAIOFighterPlugin.getState().equals(State.BANKING) || BizzaAIOFighterPlugin.getState().equals(State.WALKING)) return;
            if (!Microbot.isLoggedIn() || !super.run() || !config.toggleSafeSpot() || Rs2Player.isMoving()) return;

            currentSafeSpot = config.safeSpot();
            if (isDefaultSafeSpot(currentSafeSpot)) {
                if(!messageShown){
                    Microbot.showMessage("Please set a safespot location");
                    messageShown = true;
                }
                return;
            }

			messageShown = false;

			if (!isPlayerAtSafeSpot(currentSafeSpot)) {
				Rs2Walker.walkFastCanvas(currentSafeSpot);
				Microbot.pauseAllScripts = true;
				sleepUntil(() -> isPlayerAtSafeSpot(currentSafeSpot));
				Microbot.pauseAllScripts = false;
			}


        } catch (Exception ex) {
            Microbot.logStackTrace(this.getClass().getSimpleName(), ex);
        }
    }, 0, 600, TimeUnit.MILLISECONDS);
    return true;
}

private boolean isDefaultSafeSpot(WorldPoint safeSpot) {
    return safeSpot.getX() == 0 && safeSpot.getY() == 0;
}

private boolean isPlayerAtSafeSpot(WorldPoint safeSpot) {
    return safeSpot.equals(Microbot.getClient().getLocalPlayer().getWorldLocation());
}

    @Override
    public void shutdown() {
        super.shutdown();
    }
}
