package net.runelite.client.plugins.microbot.bizzaaiofighter.loot;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.bizzaaiofighter.BizzaAIOFighterConfig;
import net.runelite.client.plugins.microbot.bizzaaiofighter.BizzaAIOFighterPlugin;
import net.runelite.client.plugins.microbot.bizzaaiofighter.enums.DefaultLooterStyle;
import net.runelite.client.plugins.microbot.bizzaaiofighter.enums.State;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.grounditem.LootingParameters;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;

import java.util.concurrent.TimeUnit;

@Slf4j
public class LootScript extends Script {
    int minFreeSlots = 0;

    public LootScript() {

    }


    public boolean run(BizzaAIOFighterConfig config) {

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                minFreeSlots = config.bank() ? config.minFreeSlots() : 0;
                if (!super.run()) return;
                if (!Microbot.isLoggedIn()) return;
                if (BizzaAIOFighterPlugin.getState().equals(State.BANKING) || BizzaAIOFighterPlugin.getState().equals(State.WALKING)) return;
                if (Rs2Inventory.isFull() || Rs2Inventory.getEmptySlots() <= minFreeSlots || (Rs2Combat.inCombat() && !config.toggleForceLoot()))
                    return;



                if (!config.toggleLootItems()) return;
                if (config.looterStyle().equals(DefaultLooterStyle.MIXED) || config.looterStyle().equals(DefaultLooterStyle.ITEM_LIST)) {
                    lootItemsOnName(config);
                }

                if (config.looterStyle().equals(DefaultLooterStyle.GE_PRICE_RANGE) || config.looterStyle().equals(DefaultLooterStyle.MIXED)) {
                    lootItemsByValue(config);
                }
                lootBones(config);
                lootAshes(config);
                lootRunes(config);
                lootCoins(config);
                lootUntradeableItems(config);
                lootArrows(config);

            } catch(Exception ex) {
                Microbot.log("Looterscript: " + ex.getMessage());
            }

        }, 0, 200, TimeUnit.MILLISECONDS);
        return true;
    }

    private void lootArrows(BizzaAIOFighterConfig config) {
        if (config.toggleLootArrows()) {
            LootingParameters arrowParams = new LootingParameters(
                    config.attackRadius(),
                    1,
                    10,
                    minFreeSlots,
                    config.toggleDelayedLooting(),
                    config.toggleOnlyLootMyItems(),
                    "arrow"
            );
            if (Rs2GroundItem.lootItemsBasedOnNames(arrowParams)) {
                Microbot.pauseAllScripts = false;
            }
        }
    }

    private void lootBones(BizzaAIOFighterConfig config) {
        if (config.toggleBuryBones()) {
            LootingParameters bonesParams = new LootingParameters(
                    config.attackRadius(),
                    1,
                    1,
                    minFreeSlots,
                    config.toggleDelayedLooting(),
                    config.toggleOnlyLootMyItems(),
                    "bones"
            );
            if (Rs2GroundItem.lootItemsBasedOnNames(bonesParams)) {
                Microbot.pauseAllScripts = false;
            }
        }
    }

    private void lootAshes(BizzaAIOFighterConfig config) {
        if (config.toggleScatter()) {
            LootingParameters ashesParams = new LootingParameters(
                    config.attackRadius(),
                    1,
                    1,
                    minFreeSlots,
                    config.toggleDelayedLooting(),
                    config.toggleOnlyLootMyItems(),
                    " ashes"
            );
            if (Rs2GroundItem.lootItemsBasedOnNames(ashesParams)) {
                Microbot.pauseAllScripts = false;
            }
        }
    }

    // loot runes
    private void lootRunes(BizzaAIOFighterConfig config) {
        if (config.toggleLootRunes()) {
            LootingParameters runesParams = new LootingParameters(
                    config.attackRadius(),
                    1,
                    1,
                    minFreeSlots,
                    config.toggleDelayedLooting(),
                    config.toggleOnlyLootMyItems(),
                    " rune"
            );
            if (Rs2GroundItem.lootItemsBasedOnNames(runesParams)) {
                Microbot.pauseAllScripts = false;
            }
        }
    }

    // loot coins
    private void lootCoins(BizzaAIOFighterConfig config) {
        if (config.toggleLootCoins()) {
            LootingParameters coinsParams = new LootingParameters(
                    config.attackRadius(),
                    1,
                    1,
                    minFreeSlots,
                    config.toggleDelayedLooting(),
                    config.toggleOnlyLootMyItems(),
                    "coins"
            );
            if (Rs2GroundItem.lootCoins(coinsParams)) {
                Microbot.pauseAllScripts = false;
            }
        }
    }

    // loot untreadable items
    private void lootUntradeableItems(BizzaAIOFighterConfig config) {
        if (config.toggleLootUntradables()) {
            LootingParameters untradeableItemsParams = new LootingParameters(
                    config.attackRadius(),
                    1,
                    1,
                    minFreeSlots,
                    config.toggleDelayedLooting(),
                    config.toggleOnlyLootMyItems(),
                    "untradeable"
            );
            if (Rs2GroundItem.lootUntradables(untradeableItemsParams)) {
                Microbot.pauseAllScripts = false;
            }
        }
    }

    private void lootItemsByValue(BizzaAIOFighterConfig config) {
        LootingParameters valueParams = new LootingParameters(
                config.minPriceOfItemsToLoot(),
                config.maxPriceOfItemsToLoot(),
                config.attackRadius(),
                1,
                minFreeSlots,
                config.toggleDelayedLooting(),
                config.toggleOnlyLootMyItems()
        );
        if (Rs2GroundItem.lootItemBasedOnValue(valueParams)) {
            Microbot.pauseAllScripts = false;
        }
    }

    private void lootItemsOnName(BizzaAIOFighterConfig config) {
        LootingParameters valueParams = new LootingParameters(
                config.attackRadius(),
                1,
                1,
                minFreeSlots,
                config.toggleDelayedLooting(),
                config.toggleOnlyLootMyItems(),
                config.listOfItemsToLoot().trim().split(",")
        );
        if (Rs2GroundItem.lootItemsBasedOnNames(valueParams)) {
            Microbot.pauseAllScripts = false;
        }
    }

    public void shutdown() {
        super.shutdown();
    }
}
