package net.runelite.client.plugins.microbot.tanner;

import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.tanner.enums.Location;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.microbot.util.Global;
import net.runelite.client.plugins.microbot.tanner.enums.HideType;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TannerScript extends Script {

    public static double version = 1.0;

    WorldPoint tannerLocation = new WorldPoint(3276, 3192, 0);

    private final AtomicBoolean interactThreadRunning = new AtomicBoolean(false);

    private void startInteractionThread() {
        if (interactThreadRunning.get()) {
            return;
        }
        interactThreadRunning.set(true);
        Microbot.log("Starting NPC interaction watcher thread");
        Microbot.getClientThread().runOnSeperateThread(() -> {
            try {
                Global.sleepUntil(() -> Rs2Widget.hasWidget("What hides would you like tanning?"), () -> {
                    NPC ellis = Rs2Npc.getNpc(NpcID.ELLIS);
                    if (ellis != null && Rs2Camera.isTileOnScreen(ellis.getLocalLocation())) {
                        Microbot.log("Watcher: clicking trade on Ellis");
                        Rs2Npc.interact(ellis, "trade");
                    }
                }, 10000, 200);
            } finally {
                interactThreadRunning.set(false);
            }
            return true;
        });
    }


    public boolean run(TannerConfig config) {
        Microbot.log("Starting Tanner script");
        Microbot.status = "Starting";
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;

                if (config.LOCATION() == Location.AL_KHARID)
                    tanInAlkharid(config);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }

    public void tanInAlkharid(TannerConfig config) {
        Microbot.status = "Tanning";
        Microbot.log("Processing tanning routine");

        List<HideType> hideTypes = parseHideList(config);
        if (hideTypes.isEmpty()) {
            hideTypes = Collections.singletonList(config.HIDE_TYPE());
        }
        Microbot.log.debug("Parsed hide list: " + hideTypes);

        HideType activeHide = hideTypes.stream()
                .filter(h -> Rs2Inventory.hasItem(h.getItemName()) || Rs2Inventory.hasItem(h.getName()))
                .findFirst()
                .orElse(hideTypes.get(0));
        Microbot.log.debug("Active hide selected: " + activeHide);

        boolean hasHides = Rs2Inventory.hasItem(activeHide.getItemName());
        boolean hasMoney = Rs2Inventory.hasItem(995);
        boolean hasStamina = Rs2Inventory.hasItem("stamina");
        Microbot.log.debug("Inventory has raw hides? " + hasHides);
        NPC tanner = Rs2Npc.getNpc(NpcID.ELLIS);
        boolean isTannerVisibleOnScreen = tanner != null && Rs2Camera.isTileOnScreen(tanner.getLocalLocation());
        boolean isBankVisible = Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(BankLocation.AL_KHARID.getWorldPoint()) < 5;
        boolean hasRunEnergy = Microbot.getClient().getEnergy() > 4000;
        Microbot.log("hasHides=" + hasHides + ", hasMoney=" + hasMoney + ", staminaInInv=" + hasStamina);
        Microbot.log("tannerVisible=" + isTannerVisibleOnScreen + ", bankVisible=" + isBankVisible);
        Microbot.log("runEnergy=" + hasRunEnergy);
        if (hasRunEnergy) Rs2Player.toggleRunEnergy(true);
        if (isBankVisible) {
            if ((!hasRunEnergy && !hasStamina) || !hasMoney || !hasHides) {
                Microbot.log.debug("Banking needed: withdraw supplies or hides");
                Microbot.status = "Opening bank";
                Microbot.log("Opening bank");
                Rs2Bank.openBank();
            }

            if (Rs2Bank.isOpen()) {
                Microbot.status = "Banking";
                Microbot.log("Managing bank");

                if (!hasMoney) {
                    Microbot.log("Withdrawing coins");
                    Rs2Bank.withdrawAll(false,"Coins");
                }

                if (!hasHides || !hasRunEnergy) {
                    Microbot.log("Depositing items");
                    Rs2Bank.depositAll(activeHide.getName());
                    Rs2Bank.depositAll("vial");
                    if (!hasRunEnergy && !hasStamina) {
                        Microbot.log("Withdrawing stamina potion");
                        Rs2Bank.withdrawItem( "Stamina potion(4)");
                    }
                    if (!Rs2Bank.hasItem(activeHide.getItemName())) {
                        Rs2Bank.closeBank();
                        Rs2Player.logout();
                        shutdown();
                        return;
                    }
                    Microbot.log("Withdrawing hides");
                    Rs2Bank.withdrawAll(false, activeHide.getItemName());
                }
            }
        }
        if (hasHides && !isTannerVisibleOnScreen) {
            Microbot.status = "Walking to Tanner";
            Microbot.log("Walking to tanner");
            Microbot.getClientThread().runOnSeperateThread(() -> {
                Rs2Walker.walkTo(tannerLocation);
                return true;
            });
            startInteractionThread();
        }

        if (hasHides && isTannerVisibleOnScreen) {
            if (!Rs2Widget.hasWidget("What hides would you like tanning?")) {
                Microbot.status = "Interacting";
                Microbot.log("Attempting to interact with Ellis");
                startInteractionThread();
            }

            if (Rs2Widget.hasWidget("What hides would you like tanning?")) {
                Widget widget = Rs2Widget.findWidget((activeHide.getWidgetName()));
                if (widget != null) {
                    // TODO: needs to be reworked to specificy all option
                    Microbot.showMessage("needs to be reworked to specificy all option");
                    //Rs2Widget.clickWidget(widget.getId(), "all");
                    sleepUntil(() -> Rs2Inventory.hasItem(activeHide.getItemName()));
                }
            }
        }

        if (!hasHides && !isBankVisible) {
            Microbot.status = "Walking to Bank";
            Microbot.log("Walking to bank");
            Rs2Walker.walkTo(BankLocation.AL_KHARID.getWorldPoint());
        }
    }

    private List<HideType> parseHideList(TannerConfig config) {
        List<HideType> types = new ArrayList<>();
        if (config.HIDE_TYPE() != null) {
            types.add(config.HIDE_TYPE());
        }
        return types;
    }
}
