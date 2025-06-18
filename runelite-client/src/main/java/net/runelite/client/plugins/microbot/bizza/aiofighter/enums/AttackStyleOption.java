package net.runelite.client.plugins.microbot.bizza.aiofighter.enums;

import lombok.Getter;
import net.runelite.api.Skill;

@Getter
public enum AttackStyleOption {
    DEFAULT("Default"),
    ACCURATE("Accurate", Skill.ATTACK),
    AGGRESSIVE("Aggressive", Skill.STRENGTH),
    DEFENSIVE("Defensive", Skill.DEFENCE),
    CONTROLLED("Controlled", Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE),
    RANGING("Ranging", Skill.RANGED),
    LONGRANGE("Longrange", Skill.RANGED, Skill.DEFENCE),
    CASTING("Casting", Skill.MAGIC),
    DEFENSIVE_CASTING("Defensive Casting", Skill.MAGIC, Skill.DEFENCE);

    private final String name;
    private final Skill[] skills;

    AttackStyleOption(String name, Skill... skills) {
        this.name = name;
        this.skills = skills;
    }

    @Override
    public String toString() {
        return name;
    }
}
