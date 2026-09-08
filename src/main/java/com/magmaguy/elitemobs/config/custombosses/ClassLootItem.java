package com.magmaguy.elitemobs.config.custombosses;

import java.util.List;
import java.util.Objects;

/** Authored presentation for one weapon in a boss's baseline loot pool. */
public record ClassLootItem(String name, List<String> lore) {
    public static final ClassLootItem DEFAULT = new ClassLootItem("&6$boss's $weapon", List.of());

    public ClassLootItem {
        name = Objects.requireNonNull(name, "name");
        lore = List.copyOf(lore);
    }
}
