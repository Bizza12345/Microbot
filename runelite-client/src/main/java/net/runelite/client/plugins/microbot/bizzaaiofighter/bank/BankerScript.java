package net.runelite.client.plugins.microbot.bizzaaiofighter.bank;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.bizzaaiofighter.BizzaAIOFighterConfig;
import net.runelite.client.plugins.microbot.bizzaaiofighter.BizzaAIOFighterPlugin;
import net.runelite.client.plugins.microbot.bizzaaiofighter.constants.Constants;
import net.runelite.client.plugins.microbot.bizzaaiofighter.enums.State;
import net.runelite.client.plugins.microbot.bizzaaiofighter.enums.DepositMethod;
import net.runelite.client.plugins.microbot.util.Rs2InventorySetup;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.misc.Rs2Food;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

enum ItemToKeep {
    TELEPORT(Constants.TELEPORT_IDS, BizzaAIOFighterConfig::ignoreTeleport, BizzaAIOFighterConfig::staminaValue),
    STAMINA(Constants.STAMINA_POTION_IDS, BizzaAIOFighterConfig::useStamina, BizzaAIOFighterConfig::staminaValue),
    PRAYER(Constants.PRAYER_RESTORE_POTION_IDS, BizzaAIOFighterConfig::usePrayer, BizzaAIOFighterConfig::prayerValue),
    FOOD(Rs2Food.getIds(), BizzaAIOFighterConfig::useFood, BizzaAIOFighterConfig::foodValue),
    ANTIPOISON(Constants.ANTI_POISON_POTION_IDS, BizzaAIOFighterConfig::useAntipoison, BizzaAIOFighterConfig::antipoisonValue),
    ANTIFIRE(Constants.ANTI_FIRE_POTION_IDS, BizzaAIOFighterConfig::useAntifire, BizzaAIOFighterConfig::antifireValue),
    COMBAT(Constants.STRENGTH_POTION_IDS, BizzaAIOFighterConfig::useCombat, BizzaAIOFighterConfig::combatValue),
    RESTORE(Constants.RESTORE_POTION_IDS, BizzaAIOFighterConfig::useRestore, BizzaAIOFighterConfig::restoreValue);

    @Getter
    private final List<Integer> ids;
    private final Function<BizzaAIOFighterConfig, Boolean> useConfig;
    private final Function<BizzaAIOFighterConfig, Integer> valueConfig;

    ItemToKeep(Set<Integer> ids, Function<BizzaAIOFighterConfig, Boolean> useConfig, Function<BizzaAIOFighterConfig, Integer> valueConfig) {
        this.ids = new ArrayList<>(ids);
        this.useConfig = useConfig;
        this.valueConfig = valueConfig;
    }

    public boolean isEnabled(BizzaAIOFighterConfig config) {
        return useConfig.apply(config);
    }

    public int getValue(BizzaAIOFighterConfig config) {
        return valueConfig.apply(config);
    }
}

@Slf4j
public class BankerScript extends Script {
    BizzaAIOFighterConfig config;


    boolean initialized = false;

    public boolean run(BizzaAIOFighterConfig config) {
        this.config = config;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (config.bank() && needsBanking()) {
                    if (config.eatFoodForSpace())
                        if (Rs2Player.eatAt(100))
                            return;

                    if(handleBanking()){
                        BizzaAIOFighterPlugin.setState(State.IDLE);
                    }
                } else if (!needsBanking() && config.centerLocation().distanceTo(Rs2Player.getWorldLocation()) > config.attackRadius() && !Objects.equals(config.centerLocation(), new WorldPoint(0, 0, 0))) {
                    BizzaAIOFighterPlugin.setState(State.WALKING);
                    if (Rs2Walker.walkTo(config.centerLocation())) {
                        BizzaAIOFighterPlugin.setState(State.IDLE);
                    }
                }
            } catch (Exception ex) {
                Microbot.logStackTrace(this.getClass().getSimpleName(), ex);
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }

    public boolean needsBanking() {
        return (isUpkeepItemDepleted(config) && config.bank()) || (Rs2Inventory.getEmptySlots() <= config.minFreeSlots() && config.bank());
    }

    public boolean withdrawUpkeepItems(BizzaAIOFighterConfig config) {
        if (config.useInventorySetup()) {
            Rs2InventorySetup inventorySetup = new Rs2InventorySetup(config.inventorySetup().getName(), mainScheduledFuture);
            if (!inventorySetup.doesEquipmentMatch()) {
                inventorySetup.loadEquipment();
            }
            inventorySetup.loadInventory();
            return true;
        }

        for (ItemToKeep item : ItemToKeep.values()) {
            if (item.isEnabled(config)) {
                int count = item.getIds().stream().mapToInt(Rs2Inventory::count).sum();
                log.info("Item: {} Count: {}", item.name(), count);
                if (count < item.getValue(config)) {
                    log.info("Withdrawing {} {}(s)", item.getValue(config) - count, item.name());
                    if (item.name().equals("FOOD")) {
                        for (Rs2Food food : Arrays.stream(Rs2Food.values()).sorted(Comparator.comparingInt(Rs2Food::getHeal).reversed()).collect(Collectors.toList())) {
                            log.info("Checking bank for food: {}", food.getName());
                            if (Rs2Bank.hasBankItem(food.getId(), item.getValue(config) - count)) {
                                Rs2Bank.withdrawX(true, food.getId(), item.getValue(config) - count);
                                break;
                            }
                        }
                    } else {
                        ArrayList<Integer> ids = new ArrayList<>(item.getIds());
                        Collections.reverse(ids);
                        for (int id : ids) {
                            log.info("Checking bank for item: {}", id);
                            if (Rs2Bank.hasBankItem(id, item.getValue(config) - count)) {
                                Rs2Bank.withdrawX(true, id, item.getValue(config) - count);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return !isUpkeepItemDepleted(config);
    }

    public boolean depositAllExcept(BizzaAIOFighterConfig config) {
        List<Integer> ids = Arrays.stream(ItemToKeep.values())
                .filter(item -> item.isEnabled(config))
                .flatMap(item -> item.getIds().stream())
                .collect(Collectors.toList());

        int attempts = 0;
        while (attempts < 3 && hasDepositableItems(ids)) {
            Rs2Bank.depositAllExcept(ids.toArray(new Integer[0]));
            Rs2Inventory.waitForInventoryChanges(1200);
            attempts++;
        }

        return Rs2Bank.isOpen();
    }

    private void depositAllWithRetry() {
        int attempts = 0;
        while (attempts < 3 && !Rs2Inventory.isEmpty()) {
            Rs2Bank.depositAll();
            Rs2Inventory.waitForInventoryChanges(1200);
            attempts++;
        }
    }

    private boolean hasDepositableItems(List<Integer> keepIds) {
        return Rs2Inventory.items().stream()
                .map(Rs2ItemModel::getId)
                .anyMatch(id -> !keepIds.contains(id));
    }

    public boolean isUpkeepItemDepleted(BizzaAIOFighterConfig config) {
        return Arrays.stream(ItemToKeep.values())
                .filter(item -> item != ItemToKeep.TELEPORT && item.isEnabled(config))
                .anyMatch(item -> item.getIds().stream().mapToInt(Rs2Inventory::count).sum() == 0);
    }

    public boolean goToBank() {
        return Rs2Walker.walkTo(Rs2Bank.getNearestBank().getWorldPoint(), 8);
    }

    public boolean handleBanking() {
        BizzaAIOFighterPlugin.setState(State.BANKING);
        Rs2Prayer.disableAllPrayers();
        if (Rs2Bank.walkToBankAndUseBank()) {
            switch (config.depositMethod()) {
                case DEPOSIT_ALL:
                    depositAllWithRetry();
                    break;
                case RANDOM:
                    if (new Random().nextBoolean()) {
                        depositAllWithRetry();
                    } else {
                        depositAllExcept(config);
                    }
                    break;
                case KEEP_UPKEEP:
                default:
                    depositAllExcept(config);
                    break;
            }
            withdrawUpkeepItems(config);
            Rs2Bank.closeBank();
        }
        return !needsBanking();
    }


    public void shutdown() {
        super.shutdown();
        // reset the initialized flag
        initialized = false;

    }
}
