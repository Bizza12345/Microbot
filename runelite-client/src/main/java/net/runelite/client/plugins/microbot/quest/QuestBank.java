package net.runelite.client.plugins.microbot.quest;

import net.runelite.api.ItemID;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;

/**
 * Helper for quest-related banking operations.
 */
public class QuestBank
{
    /**
     * Ensure a minimum amount of coins is present in the inventory. If the
     * inventory does not contain the required amount, the bank will be opened
     * and all coins will be withdrawn.
     *
     * @param amount minimal amount of coins needed
     */
    public static void ensureCoinsAvailable(int amount)
    {
        if (Rs2Inventory.count(ItemID.COINS_995) >= amount)
        {
            return;
        }

        if (!Rs2Bank.isOpen())
        {
            Rs2Bank.openBank();
        }

        if (Rs2Bank.isOpen())
        {
            Rs2Bank.withdrawAll("Coins");
        }
    }
}
