package net.runelite.client.plugins.microbot.skeletonstate;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.antiban.enums.Activity;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class SkeletonStateScript extends Script {
    private SkeletonState state = SkeletonState.WALK_TO_GE;

    private static final int UNICORN_HORN_NOTED = 1487;
    private static final int KNIFE = 946;
    private static final int CHOCOLATE_BAR = 1973;
    private static final int CHOCOLATE_DUST = 1975;
    private static final int PESTLE_UNNOTED = 233;

    public String getStateName() {
        return state.name();
    }

    public boolean run(SkeletonStateConfig config) {
        Microbot.log("[MAIN LOOP] Starting main loop");
        state = SkeletonState.WALK_TO_GE;
        // Anti-ban setup
        Rs2Antiban.resetAntibanSettings();
        Rs2Antiban.antibanSetupTemplates.applyCraftingSetup();
        Rs2AntibanSettings.actionCooldownChance = 0.1;
        Rs2Antiban.activateAntiban();
        Rs2Antiban.setActivity(Activity.GRINDING_CHOCOLATE_BARS);

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn() || !super.run()) return;

                // Anti-ban per tick
                Rs2Antiban.actionCooldown();
                Rs2Antiban.takeMicroBreakByChance();

                switch (state) {
                    case WALK_TO_GE:
                        Microbot.log("[WALK_TO_GE] Walking to GE");
                        Rs2Walker.walkWithState(BankLocation.GRAND_EXCHANGE.getWorldPoint(), 10);
                        int dist = Rs2Player.getWorldLocation()
                                .distanceTo(BankLocation.GRAND_EXCHANGE.getWorldPoint());
                        Microbot.log("[WALK_TO_GE] Distance: " + dist);
                        if (dist < 30) {
                            state = SkeletonState.BANKING;
                        }
                        break;

                    case BANKING:
                        Microbot.log("[BANKING] Attempting to open bank");
                        Rs2Bank.preHover();
                        Rs2Bank.openBank();
                        sleepUntilOnClientThread(Rs2Bank::isOpen, 2000); //2 sec
                        //check the banks open
                        if (Rs2Bank.isOpen()) {
                            Microbot.log("[BANKING] Bank opened");
                            state = SkeletonState.WITHDRAW_ITEMS;
                        } else {
                            Microbot.log("[BANKING] Bank failed, retry");
                            Rs2Bank.openBank();
                        }
                        break;

                    case WITHDRAW_ITEMS:
                        Microbot.log("[WITHDRAW_ITEMS] Withdrawing items");
                        // If we've already got dust from previous run, deposit it first
                        if (config.taskType() == SkeletonStateConfig.TaskType.GRINDING
                                && config.grindType() == SkeletonStateConfig.GrindType.CHOCOLATE
                                && Rs2Inventory.count(CHOCOLATE_DUST) > 0) {
                            Microbot.log("[WITHDRAW_ITEMS] Depositing existing chocolate dust");
                            Rs2Bank.depositAll(CHOCOLATE_DUST);
                            sleepUntilOnClientThread(
                                    () -> Rs2Inventory.count(CHOCOLATE_DUST) == 0,
                                    5000
                            );
                        }
                        if (config.taskType() == SkeletonStateConfig.TaskType.GRINDING) {
                            if (config.grindType() == SkeletonStateConfig.GrindType.UNICORN) {
                                Rs2Bank.withdrawX(UNICORN_HORN_NOTED, config.grindQuantity());
                                sleepUntilOnClientThread(
                                        () -> Rs2Inventory.count(UNICORN_HORN_NOTED) > 0, 5000);
                                Rs2Bank.withdrawX(PESTLE_UNNOTED, 1);
                            } else {
                                Rs2Bank.withdrawItem(KNIFE);
                                Rs2Bank.withdrawX(CHOCOLATE_BAR, config.grindQuantity());
                                sleepUntilOnClientThread(
                                        () -> Rs2Inventory.count(CHOCOLATE_BAR) > 0, 5000);

                            }
                        } else {
                            Rs2Bank.withdrawX(config.firstItemId(), config.combineQuantity());
                            sleep(300, 600);
                            Rs2Bank.withdrawX(config.secondItemId(), config.combineQuantity());
                        }
                        Rs2Bank.closeBank();
                        state = SkeletonState.PROCESS_ITEMS;
                        break;

                    case PROCESS_ITEMS:
                        Microbot.log("[PROCESS_ITEMS] Processing items");
                        if (config.taskType() == SkeletonStateConfig.TaskType.GRINDING) {
                            if (config.grindType() == SkeletonStateConfig.GrindType.UNICORN) {
                                Rs2Inventory.combine(PESTLE_UNNOTED, UNICORN_HORN_NOTED);
                                Microbot.log("[PROCESS_ITEMS] Waiting for unicorn horn to disappear");
                                sleepUntil(
                                        () -> {
                                            int count = Rs2Inventory.count(UNICORN_HORN_NOTED);
                                            Microbot.log("[WAIT] Unicorn horns left = " + count);
                                            return count == 0;
                                        },
                                        15000
                                );
                            } else {
                                Rs2Inventory.combine(KNIFE, CHOCOLATE_BAR);
                                Rs2Antiban.takeMicroBreakByChance();
                                Rs2Antiban.actionCooldown();
                                Microbot.log("[PROCESS_ITEMS] Waiting for dust count ≥ " + config.grindQuantity());
                                AtomicLong lastCooldown = new AtomicLong();
                                sleepUntil(
                                        () -> {
                                            long now = System.currentTimeMillis();
                                            if (now - lastCooldown.get() > 5000) {

                                                Rs2Antiban.actionCooldown();
                                                //Rs2Antiban.takeMicroBreakByChance();
                                                //Microbot.log("antiban loop entered");
                                                lastCooldown.set(now);
                                            }
                                            int dust = Rs2Inventory.count(CHOCOLATE_DUST);
                                            //Microbot.log("[WAIT] Chocolate dust = " + dust + " / " + config.grindQuantity());
                                            return dust >= 27;
                                        },
                                        85000
                                );
                            }
                            Microbot.log("[PROCESS_ITEMS] Grinding complete, cycling");
                        } else {
                            Rs2Inventory.combine(config.firstItemId(), config.secondItemId());
                            Microbot.log("[PROCESS_ITEMS] Waiting for items to combine");
                            sleepUntil(
                                    () -> {
                                        int remaining = Rs2Inventory.count(config.firstItemId());
                                        Microbot.log("[WAIT] Items left = " + remaining);
                                        return remaining == 0;
                                    },
                                    15000
                            );
                            Microbot.log("[PROCESS_ITEMS] Combine complete, cycling");
                        }
                        state = SkeletonState.IDLE;
                        break;



                    case IDLE:
                        Microbot.log("[IDLE] Restarting cycle");
                        state = SkeletonState.WALK_TO_GE;
                        break;
                }
            } catch (Exception ex) {
                Microbot.log("[MAIN LOOP] Error: " + ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);

        return true;
    }
}
