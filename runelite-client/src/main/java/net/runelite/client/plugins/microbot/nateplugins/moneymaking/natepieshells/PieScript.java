package net.runelite.client.plugins.microbot.nateplugins.moneymaking.natepieshells;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.api.ItemID;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.grandexchange.Rs2GrandExchange;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.concurrent.TimeUnit;

public class PieScript extends Script {

    public static double version = 1.2;
    public static int totalPieShellsMade = 0;

    private enum State {
        MAKING_SHELLS,
        SELLING_SHELLS,
        BUYING_SUPPLIES
    }

    private State state = State.MAKING_SHELLS;

    private IngredientPrices ingredientPrices;
    private PieConfig config;

    public boolean run(PieConfig config) {
        this.config = config;
        Microbot.status = "Starting Nate Pie Shell Maker";
        Microbot.log("PieScript.run() - Script started");
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!super.run()) {
                    Microbot.log("PieScript.run() - super.run() returned false");
                    return;
                }
                if (!Microbot.isLoggedIn()) {
                    Microbot.log("PieScript.run() - Client not logged in");
                    return;
                }

                switch (state) {
                    case MAKING_SHELLS:
                        if (Rs2Inventory.count("pie dish") > 0 && Rs2Inventory.count("pastry dough") > 0) {
                            Microbot.status = "Combining pie dish with pastry dough";
                            Microbot.log("PieScript.run() - Combining pie dishes with pastry dough");
                            Rs2Inventory.combine("pie dish", "pastry dough");
                            sleepUntilOnClientThread(() -> Rs2Widget.getWidget(17694734) != null);
                            keyPress('1');
                            sleepUntilOnClientThread(() -> !Rs2Inventory.hasItem("pie dish"), 25000);

                            totalPieShellsMade += 14;   // rough example, but you get the point
                            Microbot.log("PieScript.run() - Completed making pie shells. Total so far: " + totalPieShellsMade);
                        } else {
                            Microbot.log("PieScript.run() - Inventory missing items, banking");
                            bank();
                        }
                        break;
                    case SELLING_SHELLS:
                    case BUYING_SUPPLIES:
                        handleGrandExchange();
                        break;
                }
            } catch (Exception ex) {
                Microbot.logStackTrace(this.getClass().getSimpleName(), ex);
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    private void bank(){
        Microbot.status = "Banking";
        Microbot.log("PieScript.bank() - Opening bank");
        Rs2Bank.openBank();
        if (Rs2Bank.isOpen()) {
            Microbot.log("PieScript.bank() - Bank opened");
            Rs2Bank.depositAll();

            if (Rs2Bank.hasItem("pie shell")) {
                state = State.SELLING_SHELLS;
                Rs2Bank.closeBank();
                sleepUntilOnClientThread(() -> !Rs2Bank.isOpen());
                return;
            }

            int dishCount = Rs2Bank.count("pie dish");
            int doughCount = Rs2Bank.count("pastry dough");
            if (dishCount > 0 && doughCount > 0) {
                int amount = Math.min(14, Math.min(dishCount, doughCount));
                Microbot.log("PieScript.bank() - Withdrawing materials (" + amount + ")");
                Rs2Bank.withdrawX(true, "pie dish", amount);
                int finalAmount = amount;
                sleepUntilOnClientThread(() -> Rs2Inventory.itemQuantity("pie dish") >= finalAmount);
                Rs2Bank.withdrawX(true, "pastry dough", amount);
                sleepUntilOnClientThread(() -> Rs2Inventory.itemQuantity("pastry dough") >= finalAmount);
            } else {
                Microbot.log("PieScript.bank() - Out of materials");
                if (config.enableGEBuying()) {
                    state = State.BUYING_SUPPLIES;
                } else {
                    Microbot.getNotifier().notify("Run out of Materials");
                    shutdown();
                }
            }
        }
        Rs2Bank.closeBank();
        Microbot.log("PieScript.bank() - Closing bank");
        sleepUntilOnClientThread(() -> !Rs2Bank.isOpen());
    }

    private void handleGrandExchange() {
        Microbot.log("GE: Starting handleGrandExchange");
        if (Rs2Bank.hasItem("pie shell")) {
            Microbot.log("GE: Withdrawing pie shells from bank");
            Rs2Bank.withdrawAll(true, "pie shell");
            sleepUntilOnClientThread(() -> Rs2Inventory.hasItem("pie shell"));
        }

        if (Rs2Inventory.hasItem("pie shell")) {
            Microbot.log("GE: Selling existing pie shells");
            Rs2GrandExchange.walkToGrandExchange();
            Rs2GrandExchange.openExchange();
            while (Rs2Inventory.hasItem("pie shell")) {
                Microbot.log("GE: Selling pie shell stack" );
                Rs2GrandExchange.sellItemUnder5Percent("pie shell");
                sleepUntilOnClientThread(Rs2GrandExchange::hasFinishedSellingOffers);
            }
            Rs2GrandExchange.collectToBank();
            Rs2GrandExchange.closeExchange();
            Rs2Bank.walkToBank();
            Rs2Bank.openBank();
            Microbot.log("GE: Depositing proceeds and collected coins");
            Rs2Bank.depositAll();
        }

        int coins = Rs2Inventory.count(ItemID.COINS_995) + Rs2Bank.count(ItemID.COINS_995);
        Microbot.log("GE: Total coins available " + coins);
        if (coins <= 0) {
            Microbot.log("GE: No coins available, exiting GE handler");
            return;
        }

        int doughPrice = Rs2GrandExchange.getPrice(ItemID.PASTRY_DOUGH);
        int dishPrice = Rs2GrandExchange.getPrice(ItemID.PIE_DISH);

        Microbot.log("GE: Initial prices - dough: " + doughPrice + " dish: " + dishPrice);


        if (Rs2Bank.count(ItemID.COINS_995) > 0) {
            Microbot.log("GE: Withdrawing coins from bank");
            Rs2Bank.withdrawAll(ItemID.COINS_995);
            sleepUntilOnClientThread(() -> Rs2Inventory.count(ItemID.COINS_995) > 0);
        }

        Rs2GrandExchange.walkToGrandExchange();
        Rs2GrandExchange.openExchange();

        // Purchase one of each item at +99% to discover the actively traded price

        Microbot.log("GE: Buying one pastry dough at +99% to determine price");
        if (Rs2GrandExchange.buyItemAboveXPercent("pastry dough", 1, 99)) {
            sleepUntilOnClientThread(Rs2GrandExchange::hasFinishedBuyingOffers);
            doughPrice = Rs2GrandExchange.getLastBoughtPrice(ItemID.PASTRY_DOUGH);
            Microbot.log("GE: Determined dough price " + doughPrice);
            Rs2GrandExchange.collectToBank();
        }

        Microbot.log("GE: Buying one pie dish at +99% to determine price");
        if (Rs2GrandExchange.buyItemAboveXPercent("pie dish", 1, 99)) {
            sleepUntilOnClientThread(Rs2GrandExchange::hasFinishedBuyingOffers);
            dishPrice = Rs2GrandExchange.getLastBoughtPrice(ItemID.PIE_DISH);
            Microbot.log("GE: Determined dish price " + dishPrice);

            Rs2GrandExchange.collectToBank();
        }

        ingredientPrices = new IngredientPrices(doughPrice, dishPrice);

        // Update available coins after test purchases
        coins = Rs2Inventory.count(ItemID.COINS_995) + Rs2Bank.count(ItemID.COINS_995);
        int setCost = doughPrice + dishPrice;
        int setsAffordable = coins / setCost;
        int quantity = Math.min(setsAffordable * 14, 196) - 1; // subtract the test item

        Microbot.log("GE: Coins after test buys " + coins + ", can afford " + setsAffordable + " sets, buying quantity " + quantity);
        if (quantity <= 0) {
            Microbot.log("GE: Quantity <= 0, exiting GE handler");
            return;
        }

        Microbot.log("GE: Buying remaining pastry dough at price " + doughPrice);
        Rs2GrandExchange.buyItem("pastry dough", doughPrice, quantity);
        Microbot.log("GE: Buying remaining pie dishes at price " + dishPrice);

        Rs2GrandExchange.buyItem("pie dish", dishPrice, quantity);
        Rs2GrandExchange.collectToBank();
        Rs2GrandExchange.closeExchange();
        Rs2Bank.walkToBank();
        Rs2Bank.openBank();
        Microbot.log("GE: Finished GE transactions, returning to shell making");
        state = State.MAKING_SHELLS;
    }
}
