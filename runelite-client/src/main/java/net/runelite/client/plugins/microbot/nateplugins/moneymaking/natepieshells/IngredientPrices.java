package net.runelite.client.plugins.microbot.nateplugins.moneymaking.natepieshells;

public class IngredientPrices {
    private final int pastryDough;
    private final int pieDish;

    public IngredientPrices(int pastryDough, int pieDish) {
        this.pastryDough = pastryDough;
        this.pieDish = pieDish;
    }

    public int pastryDough() {
        return pastryDough;
    }

    public int pieDish() {
        return pieDish;
    }
}
