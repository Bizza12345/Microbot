package net.runelite.client.plugins.microbot.skeletonstate;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class SkeletonStateOverlay extends OverlayPanel {
    private final SkeletonStatePlugin plugin;

    @Inject
    SkeletonStateOverlay(SkeletonStatePlugin plugin) {
        super(plugin);
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        panelComponent.setPreferredSize(new Dimension(200, 100));
        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Skeleton State")
                .color(Color.GREEN)
                .build());
        panelComponent.getChildren().add(LineComponent.builder()
                .left(Microbot.status)
                .build());
        panelComponent.getChildren().add(LineComponent.builder()
                .left("State: " + plugin.getScriptState())
                .build());
        return super.render(graphics);
    }
}
