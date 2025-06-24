package net.runelite.client.plugins.microbot.Bizza.utils;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.grandexchange.Rs2GrandExchange;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirement;

import java.util.ArrayList;
import java.util.List;

public class GEInteractionHelper {
    public static class Result {
        public final List<ItemRequirement> failed = new ArrayList<>();
        public boolean success() { return failed.isEmpty(); }
    }

    public static Result buyItems(List<ItemRequirement> items) {
        Result result = new Result();
        if (items == null || items.isEmpty()) {
            return result;
        }
        Microbot.log("GE helper: starting buy for " + items.size() + " items");
        if (!Rs2GrandExchange.isOpen()) {
            Rs2GrandExchange.openExchange();
            if (!Rs2GrandExchange.isOpen()) {
                result.failed.addAll(items);
                return result;
            }
        }

        for (ItemRequirement req : items) {
            String name = getName(req);
            int qty = req.getQuantity();
            try {
                Microbot.log("Buying " + name + " x" + qty);
                Rs2GrandExchange.buyItemAbove5Percent(name, qty, 5);
                // Wait until item appears, collect to bank later
                Rs2GrandExchange.collectToBank();
            } catch (Exception ex) {
                Microbot.log("GE helper failed to buy " + name + ": " + ex.getMessage());
                result.failed.add(req);
            }
        }

        // ensure everything goes to bank
        Rs2GrandExchange.collectToBank();
        if (!Rs2Bank.isOpen()) {
            Rs2Bank.openBank();
        }
        if (Rs2Bank.isOpen()) {
            Rs2Bank.depositAll();
        }
        return result;
    }

    private static String getName(ItemRequirement req) {
        return req.getName() != null ? req.getName() : String.valueOf(req.getId());
    }
}
