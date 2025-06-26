package net.runelite.client.plugins.microbot.nateplugins.moneymaking.natepieshells;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("PieMaking")
public interface PieConfig extends Config {

    @ConfigItem(
            keyName = "enableGEBuying",
            name = "Enable GE Buying",
            description = "Buy materials from the Grand Exchange when running low",
            position = 0
    )
    default boolean enableGEBuying() {
        return false;
    }
}
