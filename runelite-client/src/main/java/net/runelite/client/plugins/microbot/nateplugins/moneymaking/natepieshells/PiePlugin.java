package net.runelite.client.plugins.microbot.nateplugins.moneymaking.natepieshells;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.mouse.VirtualMouse;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;


@PluginDescriptor(
        name = PluginDescriptor.Nate +"Pie Shell Maker",
        description = "Nate's Pie Shell Maker",
        tags = {"MoneyMaking", "nate", "pies"},
        enabledByDefault = false
)
@Slf4j
public class PiePlugin extends Plugin {
    @Inject
    private PieConfig config;

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private PieOverlay pieOverlay;

    @Inject
    PieScript pieScript;

    @Provides
    PieConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(PieConfig.class);
    }


    @Override
    protected void startUp() throws AWTException {
        PieScript.totalPieShellsMade = 0;
        Microbot.status = "Starting Nate Pie Shell Maker";
        Microbot.log("PiePlugin.startUp() - Initializing plugin");
        Microbot.pauseAllScripts.compareAndSet(true, false);
        if (overlayManager != null) {
            overlayManager.add(pieOverlay);
        }
        Microbot.log("PiePlugin.startUp() - Running script");
        pieScript.run(config);
    }

    protected void shutDown() {
        Microbot.status = "Stopped Nate Pie Shell Maker";
        Microbot.log("PiePlugin.shutDown() - Shutting down plugin");
        pieScript.shutdown();
        overlayManager.remove(pieOverlay);
    }
}
