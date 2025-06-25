package net.runelite.client.plugins.microbot.Bizza.utils;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.grandexchange.Rs2GrandExchange;
import net.runelite.client.plugins.microbot.util.grandexchange.GrandExchangeSlots;
import org.apache.commons.lang3.tuple.Pair;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirement;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
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
            Microbot.log("GEHelper: no requirements to buy");
            return true;
        }

        if (!Rs2GrandExchange.isOpen()) {
            Microbot.status = "Opening Grand Exchange";
            Rs2GrandExchange.openExchange();
            sleepUntil(Rs2GrandExchange::isOpen, 5000);
        }

        Microbot.status = "Buying items at GE";

        // Make a copy to avoid modification issues
        List<ItemRequirement> pending = new ArrayList<>(requirements);
        for (ItemRequirement req : pending) {
            String name = req.getName();
            Microbot.status = "Buying " + name;
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
            Microbot.status = "Waiting on offer";
            // wait until a slot becomes free indicating the offer completed
            sleepUntil(() -> {
                Pair<GrandExchangeSlots, Integer> slotInfo = Rs2GrandExchange.getAvailableSlot();
                return slotInfo.getLeft() != null && Rs2GrandExchange.isSlotAvailable(slotInfo.getLeft());
            }, 3000);
        }
        Microbot.status = "Collecting items";
        Rs2GrandExchange.collectToBank();
        Microbot.log("Collecting GE items to bank");

        if (!Rs2Bank.isOpen()) {
            Rs2Bank.openBank();
            sleepUntil(Rs2Bank::isOpen, 5000);
        }
        Microbot.status = "Depositing";
        Rs2Bank.depositAll();
        Microbot.log("Deposited inventory after GE buy");
        Rs2Bank.closeBank();

        // Verify items were obtained
        for (ItemRequirement req : pending) {
            int id = req.getId();
            int owned = Rs2Bank.count(id) + Rs2Inventory.itemQuantity(id);
            if (owned < req.getQuantity()) {
                Microbot.log("Timeout: Did not obtain " + req.getName() + " after buy attempt.");
            }
        }

        Microbot.log("GEHelper finished buying items");
        Microbot.status = "GE buy complete";
        return true;
    }
}
