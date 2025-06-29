package net.runelite.client.plugins.microbot.skeletonstate;

import com.google.inject.Provides;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Default + "Skeleton State",
        description = "Skeleton plugin with a basic state machine",
        tags = {"skeleton", "state", "microbot"},
        enabledByDefault = false
)
public class SkeletonStatePlugin extends Plugin {
    @Inject
    private SkeletonStateConfig config;

    @Provides
    SkeletonStateConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(SkeletonStateConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private SkeletonStateOverlay overlay;

    @Inject
    SkeletonStateScript script;

    String getScriptState() {
        return script != null ? script.getStateName() : "unknown";
    }

    @Override
    protected void startUp() throws AWTException {
        Microbot.log("SkeletonStatePlugin starting up");
        if (overlayManager != null) {
            overlayManager.add(overlay);
        }
        script.run(config);
    }

    protected void shutDown() {
        Microbot.log("SkeletonStatePlugin shutting down");
        script.shutdown();
        overlayManager.remove(overlay);
    }

    @Subscribe
    public void onGameTick(GameTick tick) {
        // Placeholder for script tick updates if needed
    }
}
