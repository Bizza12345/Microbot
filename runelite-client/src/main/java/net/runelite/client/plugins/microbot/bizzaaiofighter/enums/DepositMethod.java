package net.runelite.client.plugins.microbot.bizzaaiofighter.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DepositMethod {
    DEPOSIT_ALL("Deposit All"),
    KEEP_UPKEEP("Keep Upkeep"),
    RANDOM("Random");

    private final String name;

    @Override
    public String toString() {
        return name;
    }
}
