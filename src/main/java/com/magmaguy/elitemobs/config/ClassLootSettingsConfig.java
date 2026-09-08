package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.items.ClassLootProfile;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.magmacore.config.ConfigurationFile;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** The editable, corpus-derived baseline; item-specific names and lore remain in each boss. */
public class ClassLootSettingsConfig extends ConfigurationFile {
    public enum Difficulty { NORMAL, HARD, MYTHIC }
    public enum Rank { TRASH, MINIBOSS, BOSS }
    private record Key(Difficulty difficulty, Rank rank, SkillType skill) {}
    private static Map<Key, ClassLootProfile> profiles = Map.of();
    private static Map<String, Difficulty> difficultyIds = Map.of();
    private static Map<Rank, Double> chances = Map.of();
    private static Difficulty defaultDifficulty = Difficulty.NORMAL;
    private static boolean enabled;
    private static double budgetFraction = .5;
    private static int minimumPrimaryLevel = 1;

    public ClassLootSettingsConfig() { super("ClassLootSettings.yml"); }

    @Override public void initializeValues() {
        YamlConfiguration defaults;
        try (var reader = new InputStreamReader(Objects.requireNonNull(
                getClass().getResourceAsStream("/classloot/defaults.yml")), StandardCharsets.UTF_8)) {
            defaults = YamlConfiguration.loadConfiguration(reader);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not load bundled class-loot profiles", exception);
        }
        fileConfiguration.setDefaults(defaults);
        fileConfiguration.options().copyDefaults(true);
        enabled = fileConfiguration.getBoolean("enabled", true);
        defaultDifficulty = difficulty(fileConfiguration.getString("defaultDifficulty"), Difficulty.NORMAL);
        budgetFraction = number(fileConfiguration, "enchantmentBudgetFraction", .5, 0, 1);
        minimumPrimaryLevel = (int) number(fileConfiguration, "minimumPrimaryLevel", 1, 0, 100);
        Map<String, Difficulty> ids = new HashMap<>();
        var idSection = fileConfiguration.getConfigurationSection("difficultyIds");
        if (idSection != null) for (String id : idSection.getKeys(false))
            ids.put(id, difficulty(idSection.getString(id), defaultDifficulty));
        difficultyIds = Map.copyOf(ids);
        Map<Rank, Double> dropChances = new HashMap<>();
        Map<Key, ClassLootProfile> parsed = new HashMap<>();
        for (Rank rank : Rank.values()) {
            dropChances.put(rank, number(fileConfiguration, "dropChance." + rank,
                    defaults.getDouble("dropChance." + rank), 0, 1));
            for (Difficulty difficulty : Difficulty.values()) for (SkillType skill : SkillType.values()) {
                if (skill == SkillType.ARMOR) continue;
                String path = "profiles." + difficulty + "." + rank + "." + skill;
                String primary = Objects.toString(fileConfiguration.get(path + ".primaryEnchantment"), "")
                        .toLowerCase(Locale.ROOT);
                if (!ClassLootProfile.supports(skill, primary) || ClassLootProfile.nativeEnchantment(primary) == null) {
                    Logger.warn("Invalid class-loot primary at " + path + "; using bundled primary.");
                    primary = defaults.getString(path + ".primaryEnchantment").toLowerCase(Locale.ROOT);
                }
                parsed.put(new Key(difficulty, rank, skill), new ClassLootProfile(primary,
                        rules(path + ".enchantments", skill), rules(path + ".rareEnchantments", skill)));
            }
        }
        profiles = Map.copyOf(parsed);
        chances = Map.copyOf(dropChances);
        Logger.info("Loaded " + profiles.size() + " class-loot profiles (" + (enabled ? "enabled" : "disabled") + ").");
        fileConfiguration.setComments("enchantmentBudgetFraction", java.util.List.of(
                "Native enchantment units drawn without replacement: ceil(total levels * fraction).",
                "0.5 matches existing scalable DLC loot. Custom enchantment levels are not budgeted."));
        fileConfiguration.setComments("minimumPrimaryLevel", java.util.List.of(
                "Reserve this many primary levels inside the budget, bounded by its configured ceiling.",
                "Disabled enchantments and zero budgets remain disabled."));
    }

    private java.util.List<ClassLootProfile.Rule> rules(String path, SkillType skill) {
        var section = fileConfiguration.getConfigurationSection(path);
        var result = new ArrayList<ClassLootProfile.Rule>();
        if (section == null) return result;
        for (String name : section.getKeys(false)) {
            String key = name.toLowerCase(Locale.ROOT);
            if (!ClassLootProfile.supports(skill, key)) {
                Logger.warn("Unsupported class-loot enchantment " + path + "." + name + "; skipped.");
                continue;
            }
            int level = (int) number(section, name + ".level", 1, 0, 100);
            double chance = number(section, name + ".chance", 0, 0, 1);
            result.add(new ClassLootProfile.Rule(key, level, chance));
        }
        return result;
    }

    private static double number(ConfigurationSection section, String path, double fallback, double min, double max) {
        Object raw = section.get(path);
        if (!(raw instanceof Number number) || !Double.isFinite(number.doubleValue())
                || number.doubleValue() < min || number.doubleValue() > max) {
            Logger.warn("Invalid ClassLootSettings value at " + section.getCurrentPath() + "." + path + "; using " + fallback);
            return fallback;
        }
        return number.doubleValue();
    }

    public static Difficulty difficulty(String value, Difficulty fallback) {
        try { return Difficulty.valueOf(value.toUpperCase(Locale.ROOT)); }
        catch (RuntimeException ignored) { return fallback; }
    }

    // Profile maps permit admin-added compatible enchantments and difficulty IDs; preserve those nodes.
    @Override public void saveDefaults() { ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file); }
    public static boolean enabled() { return enabled; }
    public static Difficulty defaultDifficulty() { return defaultDifficulty; }
    public static Difficulty forDifficultyId(Object id) { return difficultyIds.getOrDefault(String.valueOf(id), defaultDifficulty); }
    public static Difficulty forDifficultyId(Object id, Object resolvedId) {
        // Explicit administrator mappings take precedence over package naming conventions.
        return difficultyIds.getOrDefault(String.valueOf(id), forDifficultyId(resolvedId));
    }
    public static double dropChance(Rank rank) { return chances.getOrDefault(rank, 0D); }
    public static ClassLootProfile profile(Difficulty difficulty, Rank rank, SkillType skill) {
        return profiles.get(new Key(difficulty, rank, skill));
    }
    public static double budgetFraction() { return budgetFraction; }
    public static int minimumPrimaryLevel() { return minimumPrimaryLevel; }
}
