package net.runelite.client.plugins.microbot.bizzaaiofighter.enums;

public enum HardAttackStyle {
    NONE("None"),
    ACCURATE("Accurate"),
    AGGRESSIVE("Aggressive"),
    DEFENSIVE("Defensive"),
    CONTROLLED("Controlled"),
    RANGING("Ranging"),
    LONGRANGE("Longrange"),
    CASTING("Casting"),
    DEFENSIVE_CASTING("Defensive Casting");

    private final String name;

    HardAttackStyle(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
