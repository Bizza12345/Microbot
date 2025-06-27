package net.runelite.client.plugins.microbot.tanner.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.runelite.client.plugins.microbot.tanner.HideMapping;

@Getter
@RequiredArgsConstructor
public enum HideType {
    LEATHER("Leather", "cow hide", "soft leather"),
    HARD_LEATHER("Hard leather", "cow hide", "hard d'hide"),
    GREEN("Green dragon leather", "green dragonhide", "green d'hide"),
    BLUE("Blue dragon leather", "blue dragonhide", "blue d'hide"),
    RED("Red dragon leather", "red dragonhide", "red d'hide"),
    BLACK("Black dragon leather", "black dragonhide", "black d'hide");

    private static final Map<HideType, HideMapping> HIDE_MAP =
            Stream.of(values()).collect(Collectors.toMap(h -> h, h ->
                    new HideMapping(h.itemName, h.name),
                    (a, b) -> a,
                    () -> new EnumMap<>(HideType.class)));

    private final String name;
    private final String itemName;
    private final String widgetName;

    public static HideMapping getMapping(HideType type) {
        return HIDE_MAP.get(type);
    }

    public String getRawHide() {
        return itemName;
    }

    public String getTannedHide() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
