package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.config.ClassLootSettingsConfig;
import com.magmaguy.elitemobs.experimentalcombat.ExperimentalCombatModule;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.experimentalcombat.progression.ProfileSnapshot;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/** Computes the exact distribution used both by death rolls and administrator previews. */
public final class ClassLootSelection {
    private ClassLootSelection() {}

    public static ClassFormDefinition activeClass(Player player) {
        if (player == null || !ExperimentalCombatModule.isInitialized()) return null;
        var module = ExperimentalCombatModule.get();
        return module.profile(player.getUniqueId()).flatMap(ProfileSnapshot::optionalActiveLineage)
                .map(active -> module.catalog().require(active.activeFormId())).orElse(null);
    }

    public static Map<ClassLootFamily, Double> probabilities(List<ClassLootFamily> available, ClassFormDefinition form) {
        List<ClassLootFamily> enabled = available.stream()
                .filter(family -> ClassLootSettingsConfig.categoryWeight(family.category()) > 0).toList();
        Map<ClassLootFamily, Double> result = new EnumMap<>(ClassLootFamily.class);
        if (form == null || !ClassLootSettingsConfig.classBiasEnabled()) {
            addBucket(result, enabled, 1);
            return Map.copyOf(result);
        }
        var preference = ClassLootSettingsConfig.preferences(form.id());
        var relevant = enabled.stream().filter(family -> preference.relevant(form, family)).toList();
        var other = enabled.stream().filter(family -> !preference.relevant(form, family)).toList();
        double relevantChance = other.isEmpty() ? 1 : relevant.isEmpty() ? 0 : ClassLootSettingsConfig.relevantChance();
        addBucket(result, relevant, relevantChance);
        addBucket(result, other, 1 - relevantChance);
        return Map.copyOf(result);
    }

    private static void addBucket(Map<ClassLootFamily, Double> result, List<ClassLootFamily> families, double probability) {
        if (families.isEmpty() || probability <= 0) return;
        Map<ClassLootFamily.Category, Long> counts = new EnumMap<>(ClassLootFamily.Category.class);
        families.forEach(family -> counts.merge(family.category(), 1L, Long::sum));
        double total = counts.keySet().stream().mapToDouble(ClassLootSettingsConfig::categoryWeight).sum();
        for (ClassLootFamily family : families)
            result.put(family, probability * ClassLootSettingsConfig.categoryWeight(family.category())
                    / total / counts.get(family.category()));
    }

    public static ClassLootFamily select(List<ClassLootFamily> available, Player player) {
        var probabilities = probabilities(available, activeClass(player));
        double roll = ThreadLocalRandom.current().nextDouble();
        ClassLootFamily last = null;
        for (ClassLootFamily family : ClassLootFamily.values()) {
            double chance = probabilities.getOrDefault(family, 0D);
            if (chance <= 0) continue;
            last = family;
            roll -= chance;
            if (roll < 0) return family;
        }
        return last;
    }
}
