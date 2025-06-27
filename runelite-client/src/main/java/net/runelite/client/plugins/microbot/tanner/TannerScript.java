package net.runelite.client.plugins.microbot.tanner;

import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.tanner.enums.Location;
import net.runelite.client.plugins.microbot.tanner.enums.HideType;
import net.runelite.client.plugins.microbot.tanner.enums.TannerState;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.microbot.util.Global;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class TannerScript extends Script {

    public static double version = 1.0;

    private final WorldPoint tannerLocation = new WorldPoint(3276, 3192, 0);

    private final AtomicBoolean interactThreadRunning = new AtomicBoolean(false);

    private TannerState state = TannerState.BANKING;
    private TannerState previousState = null;

    /**
     * Continuously attempt to trade with Ellis until the tanning interface appears
     */
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
                }, 12000, 200);
            } finally {
                interactThreadRunning.set(false);
            }
            return true;
        });
    }

    private List<HideType> parseHideList(TannerConfig config) {
        return Arrays.stream(config.HIDE_LIST().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toUpperCase().replace(' ', '_'))
                .map(name -> {
                    try {
                        return HideType.valueOf(name);
                    } catch (IllegalArgumentException ex) {
                        Microbot.log("Invalid hide type in list: " + name);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
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
        List<HideType> hideTypes = parseHideList(config);
        if (hideTypes.isEmpty()) {
            hideTypes = Collections.singletonList(config.HIDE_TYPE());
        }
        HideType activeHide = hideTypes.stream()
                .filter(h -> Rs2Inventory.hasItem(h.getItemName()))
                .findFirst()
                .orElse(hideTypes.get(0));

        boolean hasHides = Rs2Inventory.hasItem(activeHide.getItemName());
        boolean hasMoney = Rs2Inventory.hasItem(995);
        boolean hasStamina = Rs2Inventory.hasItem("stamina");
        NPC tanner = Rs2Npc.getNpc(NpcID.ELLIS);
        boolean tannerVisible = tanner != null && Rs2Camera.isTileOnScreen(tanner.getLocalLocation());
        boolean bankVisible = Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(BankLocation.AL_KHARID.getWorldPoint()) < 5;
        boolean hasRunEnergy = Microbot.getClient().getEnergy() > 4000;

        if (hasRunEnergy) Rs2Player.toggleRunEnergy(true);

        previousState = state;
        state = determineState(hasHides, hasMoney, hasRunEnergy, hasStamina, bankVisible, tannerVisible);
        if (state != previousState) {
            Microbot.log("State changed to " + state);
        }

        Microbot.log("hasHides=" + hasHides + ", hasMoney=" + hasMoney + ", staminaInInv=" + hasStamina);
        Microbot.log("tannerVisible=" + tannerVisible + ", bankVisible=" + bankVisible);
        Microbot.log("runEnergy=" + hasRunEnergy);

        switch (state) {
            case WALK_TO_BANK:
                Microbot.status = "Walking to Bank";
                Microbot.log("Walking to bank");
                Rs2Walker.walkTo(BankLocation.AL_KHARID.getWorldPoint());
                break;
            case BANKING:
                Microbot.status = "Banking";
                Microbot.log("Managing bank");
                if (!Rs2Bank.isOpen()) {
                    Rs2Bank.openBank();
                    break;
                }
                if (!hasMoney) {
                    Microbot.log("Withdrawing coins");
                    Rs2Bank.withdrawAll(false, "Coins");
                }
                if (!hasHides || !hasRunEnergy) {
                    Microbot.log("Depositing items");
                    Rs2Bank.depositAll(activeHide.getName());
                    Rs2Bank.depositAll("vial");
                    if (!hasRunEnergy && !hasStamina) {
                        Microbot.log("Withdrawing stamina potion");
                        Rs2Bank.withdrawItem("Stamina potion(4)");
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
                break;
            case WALK_TO_TANNER:
                Microbot.status = "Walking to Tanner";
                Microbot.log("Walking to tanner");
                Microbot.getClientThread().runOnSeperateThread(() -> {
                    Rs2Walker.walkTo(tannerLocation);
                    return true;
                });
                startInteractionThread();
                break;
            case TRADING:
                Microbot.status = "Trading";
                Microbot.log("Interacting with Ellis");
                if (Rs2Widget.hasWidget("What hides would you like tanning?")) {
                    Widget widget = Rs2Widget.findWidget(activeHide.getWidgetName());
                    if (widget != null) {
                        Microbot.log("Selecting hide option " + activeHide.getWidgetName());
                        Microbot.showMessage("needs to be reworked to specificy all option");
                        //Rs2Widget.clickWidget(widget.getId(), "all");
                        sleepUntil(() -> Rs2Inventory.hasItem(activeHide.getItemName()));
                    }
                } else {
                    if (Rs2Npc.interact(NpcID.ELLIS, "trade")) {
                        sleepUntil(() -> Rs2Widget.hasWidget("What hides would you like tanning?"));
                    }
                    startInteractionThread();
                }
                break;
        }
    }

    private TannerState determineState(boolean hasHides, boolean hasMoney, boolean hasRunEnergy,
                                       boolean hasStamina, boolean bankVisible, boolean tannerVisible) {
        if (!hasHides || !hasMoney || (!hasRunEnergy && !hasStamina)) {
            return bankVisible ? TannerState.BANKING : TannerState.WALK_TO_BANK;
        }
        if (tannerVisible) {
            return TannerState.TRADING;
        }
        return TannerState.WALK_TO_TANNER;
    }
}
