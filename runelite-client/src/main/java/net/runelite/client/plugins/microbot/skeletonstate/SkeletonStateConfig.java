package net.runelite.client.plugins.microbot.skeletonstate;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("skeletonstate")
public interface SkeletonStateConfig extends Config {

    @ConfigSection(
            name = "General Settings",
            description = "General script configuration",
            position = 0
    )
    String general = "General Settings";

    @ConfigItem(
            keyName = "taskType",
            name = "Processing Method",
            description = "Select the type of processing task",
            position = 1,
            section = general
    )
    default TaskType taskType() {
        return TaskType.COMBINING;
    }

    enum TaskType {
        COMBINING,
        GRINDING
    }

    @ConfigSection(
            name = "Combining Settings",
            description = "Settings for combining (e.g., potion making)",
            position = 2
    )
    String combineSection = "Combining Settings";

    @ConfigItem(
            keyName = "firstItemId",
            name = "First Item ID",
            description = "The item ID of the first item to combine",
            section = combineSection,
            position = 1
    )
    default int firstItemId() {
        return 2347;
    }

    @ConfigItem(
            keyName = "secondItemId",
            name = "Second Item ID",
            description = "The item ID of the second item to combine",
            section = combineSection,
            position = 2
    )
    default int secondItemId() {
        return 2351;
    }

    @ConfigItem(
            keyName = "combineQuantity",
            name = "Combine Quantity",
            description = "How many of each item to withdraw and combine",
            section = combineSection,
            position = 3
    )
    default int combineQuantity() {
        return 14;
    }

    @ConfigSection(
            name = "Grinding Settings",
            description = "Settings for grinding tasks",
            position = 3
    )
    String grindSection = "Grinding Settings";

    @ConfigItem(
            keyName = "grindType",
            name = "Material to Grind",
            description = "Choose between Unicorn Horn or Chocolate Bar",
            section = grindSection,
            position = 1
    )
    default GrindType grindType() {
        return GrindType.UNICORN;
    }

    enum GrindType {
        UNICORN,
        CHOCOLATE
    }

    @ConfigItem(
            keyName = "grindQuantity",
            name = "Grind Quantity",
            description = "How many items to withdraw and grind",
            section = grindSection,
            position = 2
    )
    default int grindQuantity() {
        return 27;
    }
}
