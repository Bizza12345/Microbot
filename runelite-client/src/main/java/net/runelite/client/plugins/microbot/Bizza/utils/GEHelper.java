package net.runelite.client.plugins.microbot.Bizza.utils;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.grandexchange.Rs2GrandExchange;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirement;
import java.util.List;
import java.util.ArrayList;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

/**
 * Utility helper for Grand Exchange interactions.
 * Provides simple methods to purchase quest items and
 * handle collecting them to the bank.
 */
public class GEHelper {

    /**
     * Attempts to buy all provided item requirements using the Grand Exchange.
     * It will open the GE if required, place offers with a high price boost,
     * collect to bank and deposit any remaining inventory items.
     *
     * @param requirements list of ItemRequirement to purchase
     * @return true if helper finished without blocking actions
     */
    public static boolean buyRequirements(List<ItemRequirement> requirements) {
        if (requirements == null || requirements.isEmpty()) {
            return true;
        }

        if (!Rs2GrandExchange.isOpen()) {
            Rs2GrandExchange.openExchange();
            sleepUntil(Rs2GrandExchange::isOpen, 5000);
        }

        // Make a copy to avoid modification issues
        List<ItemRequirement> pending = new ArrayList<>(requirements);
        for (ItemRequirement req : pending) {
            String name = req.getName();
            Microbot.log("GEHelper buying " + name);
            boolean success = false;
            // Prefer the +X% button if available (use 99%), otherwise spam +5% five times
            if (Rs2GrandExchange.getPricePerItemButton_PlusXPercent() != null) {
                success = Rs2GrandExchange.buyItemAboveXPercent(name, req.getQuantity(), 99);
            } else {
                success = Rs2GrandExchange.buyItemAbove5Percent(name, req.getQuantity(), 5);
            }

            if (!success) {
                Microbot.log("Failed to place offer for " + name);
                continue;
            }
            Microbot.log("Offer placed for " + name);
            // Small wait for the item to complete
            sleepUntil(() -> Rs2GrandExchange.isSlotAvailable(Rs2GrandExchange.getAvailableSlot().getLeft()), 3000);
        }

        Rs2GrandExchange.collectToBank();
        Microbot.log("Collecting GE items to bank");

        if (!Rs2Bank.isOpen()) {
            Rs2Bank.openBank();
            sleepUntil(Rs2Bank::isOpen, 5000);
        }
        Rs2Bank.depositAll();
        Microbot.log("Deposited inventory after GE buy");
        Rs2Bank.closeBank();

        Microbot.log("GEHelper finished buying items");
        return true;
    }
}
