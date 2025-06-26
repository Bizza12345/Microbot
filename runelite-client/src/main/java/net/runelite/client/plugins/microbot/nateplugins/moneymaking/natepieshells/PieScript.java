package net.runelite.client.plugins.microbot.nateplugins.moneymaking.natepieshells;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.concurrent.TimeUnit;

public class PieScript extends Script {

    public static double version = 1.2;
    public static int totalPieShellsMade = 0;

    public boolean run(PieConfig config) {
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
                if (Rs2Inventory.count("pie dish") > 0 && (Rs2Inventory.count("pastry dough") > 0)) {
                    Microbot.status = "Combining pie dish with pastry dough";
                    Microbot.log("PieScript.run() - Combining pie dishes with pastry dough");
                    Rs2Inventory.combine("pie dish", "pastry dough");
                    sleepUntilOnClientThread(() -> Rs2Widget.getWidget(17694734) != null);
                    keyPress('1');
                    sleepUntilOnClientThread(() -> !Rs2Inventory.hasItem("pie dish"),25000);

                    totalPieShellsMade += 14;   // rough example, but you get the point
                    Microbot.log("PieScript.run() - Completed making pie shells. Total so far: " + totalPieShellsMade);
                    return;
                } else {
                    Microbot.log("PieScript.run() - Inventory missing items, banking");
                    bank();
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
        if(Rs2Bank.isOpen()){
            Microbot.log("PieScript.bank() - Bank opened");
            Rs2Bank.depositAll();
            if(Rs2Bank.hasItem("pie dish") &&  Rs2Bank.hasItem("pastry dough")) {
                Microbot.log("PieScript.bank() - Withdrawing materials");
                Rs2Bank.withdrawX(true, "pie dish", 14);
                sleepUntilOnClientThread(() -> Rs2Inventory.hasItem("pie dish"));
                Rs2Bank.withdrawX(true, "pastry dough", 14);
                sleepUntilOnClientThread(() -> Rs2Inventory.hasItem("pastry dough"));
            } else {
                Microbot.log("PieScript.bank() - Out of materials");
                Microbot.getNotifier().notify("Run out of Materials");
                shutdown();
            }
        }
        Rs2Bank.closeBank();
        Microbot.log("PieScript.bank() - Closing bank");
        sleepUntilOnClientThread(() -> !Rs2Bank.isOpen());
    }
}
