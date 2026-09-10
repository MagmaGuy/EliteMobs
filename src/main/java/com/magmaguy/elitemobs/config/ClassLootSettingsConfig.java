package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.items.ClassLootProfile;
import com.magmaguy.elitemobs.items.ClassLootFamily;
import com.magmaguy.elitemobs.items.ClassLootPreferences;
import com.magmaguy.elitemobs.advancedcombat.content.BuiltInClassContent;
import com.magmaguy.magmacore.config.ConfigurationFile;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** The editable, corpus-derived baseline; item-specific names and lore belong to custom items. */
public class ClassLootSettingsConfig extends ConfigurationFile {
    public enum Difficulty { NORMAL, HARD, MYTHIC }
    public enum Rank { TRASH, MINIBOSS, BOSS }
    private record Key(Difficulty difficulty, Rank rank, ClassLootFamily family) {}
    private static Map<Key, ClassLootProfile> profiles = Map.of();
    private static Map<String, Difficulty> difficultyIds = Map.of();
    private static Map<Rank, Double> chances = Map.of();
    private static Difficulty defaultDifficulty = Difficulty.NORMAL;
    private static boolean enabled;
    private static double budgetFraction = .5;
    private static int minimumPrimaryLevel = 1;
    private static boolean classBiasEnabled;
    private static double relevantChance = .8;
    private static Map<ClassLootFamily.Category, Double> categoryWeights = Map.of();
    private static Map<String, ClassLootPreferences> classPreferences = Map.of();

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
        initializeSelection();
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
            for (Difficulty difficulty : Difficulty.values()) for (ClassLootFamily family : ClassLootFamily.values()) {
                String path = "profiles." + difficulty + "." + rank + "." + family;
                String primary = Objects.toString(fileConfiguration.get(path + ".primaryEnchantment"), "")
                        .toLowerCase(Locale.ROOT);
                if (!ClassLootProfile.supports(family, primary) || ClassLootProfile.nativeEnchantment(primary) == null) {
                    Logger.warn("Invalid class-loot primary at " + path + "; using bundled primary.");
                    primary = defaults.getString(path + ".primaryEnchantment").toLowerCase(Locale.ROOT);
                }
                parsed.put(new Key(difficulty, rank, family), new ClassLootProfile(primary,
                        rules(path + ".enchantments", family), rules(path + ".rareEnchantments", family),
                        potionEffects(path + ".potionEffects")));
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

    private java.util.List<ClassLootProfile.Rule> rules(String path, ClassLootFamily family) {
        var section = fileConfiguration.getConfigurationSection(path);
        var result = new ArrayList<ClassLootProfile.Rule>();
        if (section == null) return result;
        for (String name : section.getKeys(false)) {
            String key = name.toLowerCase(Locale.ROOT);
            if (!ClassLootProfile.supports(family, key)) {
                Logger.warn("Unsupported class-loot enchantment " + path + "." + name + "; skipped.");
                continue;
            }
            int level = (int) number(section, name + ".level", 1, 0, 100);
            double chance = number(section, name + ".chance", 0, 0, 1);
            result.add(new ClassLootProfile.Rule(key, level, chance));
        }
        return result;
    }

    private java.util.List<String> potionEffects(String path) {
        var result = new ArrayList<String>();
        for (String raw : fileConfiguration.getStringList(path)) {
            try {
                String[] fields = raw.split(",");
                if (fields.length != 4) throw new IllegalArgumentException("expected EFFECT,amplifier,target,method");
                for (int i = 0; i < fields.length; i++) fields[i] = fields[i].strip().toUpperCase(Locale.ROOT);
                fields[0] = LegacyValueConverter.parsePotionEffect(fields[0]);
                if (Registry.EFFECT.get(NamespacedKey.minecraft(fields[0].toLowerCase(Locale.ROOT))) == null)
                    throw new IllegalArgumentException("unknown potion effect");
                int amplifier = Integer.parseInt(fields[1]);
                if (amplifier < 0 || amplifier > 255) throw new IllegalArgumentException("amplifier must be 0..255");
                com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffect.Target.valueOf(fields[2]);
                com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffect.ApplicationMethod.valueOf(fields[3]);
                result.add(String.join(",", fields));
            } catch (RuntimeException exception) {
                Logger.warn("Invalid ClassLootSettings " + path + " entry '" + raw + "': " + exception.getMessage() + "; skipped.");
            }
        }
        return java.util.List.copyOf(result);
    }

    private void initializeSelection() {
        fileConfiguration.addDefault("selection.classBiasEnabled", true);
        fileConfiguration.addDefault("selection.relevantChance", .8);
        classBiasEnabled = fileConfiguration.getBoolean("selection.classBiasEnabled", true);
        relevantChance = number(fileConfiguration, "selection.relevantChance", .8, 0, 1);
        Map<ClassLootFamily.Category, Double> weights = new java.util.EnumMap<>(ClassLootFamily.Category.class);
        for (var category : ClassLootFamily.Category.values()) {
            double fallback = switch (category) { case WEAPONS -> 50; case ARMOR -> 45; case SHIELDS -> 5; };
            String path = "selection.categoryWeights." + category;
            fileConfiguration.addDefault(path, fallback);
            weights.put(category, number(fileConfiguration, path, fallback, 0, 10000));
        }
        if (weights.values().stream().allMatch(weight -> weight == 0))
            Logger.warn("ClassLootSettings: all category weights are zero; automatic equipment rolls are disabled.");
        categoryWeights = Map.copyOf(weights);
        Map<String, ClassLootPreferences> preferences = new HashMap<>();
        var catalog = BuiltInClassContent.catalog();
        for (var form : catalog.forms()) {
            var fallback = ClassLootPreferences.defaults(form.id());
            String path = "selection.classes." + form.id();
            fileConfiguration.addDefault(path + ".armor", fallback.armor().name());
            fileConfiguration.addDefault(path + ".shields", fallback.shields());
            ClassLootPreferences.Armor armor = fallback.armor();
            try { armor = ClassLootPreferences.Armor.valueOf(fileConfiguration.getString(path + ".armor").strip().toUpperCase(Locale.ROOT)); }
            catch (RuntimeException invalid) { Logger.warn("Invalid " + path + ".armor; expected DPS, TANK or BOTH. Using " + armor); }
            if (!fileConfiguration.isBoolean(path + ".shields"))
                Logger.warn("Invalid " + path + ".shields; expected true or false. Using " + fallback.shields());
            preferences.put(form.id(), new ClassLootPreferences(armor,
                    fileConfiguration.getBoolean(path + ".shields", fallback.shields())));
        }
        var configured = fileConfiguration.getConfigurationSection("selection.classes");
        if (configured != null) for (String id : configured.getKeys(false))
            if (catalog.find(id).isEmpty()) Logger.warn("Unknown class in ClassLootSettings selection.classes: " + id);
        classPreferences = Map.copyOf(preferences);
        fileConfiguration.setComments("selection", java.util.List.of(
                "Split relevant/off-class first, then choose a category by weight, then a family equally.",
                "Empty buckets fall back to available equipment. No active class uses unbiased category weights.",
                "Weapons reuse each specialization's weapon affinities. Armor/shield preferences are editable below."));
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
    public static ClassLootProfile profile(Difficulty difficulty, Rank rank, ClassLootFamily family) {
        return profiles.get(new Key(difficulty, rank, family));
    }
    public static double budgetFraction() { return budgetFraction; }
    public static int minimumPrimaryLevel() { return minimumPrimaryLevel; }
    public static boolean classBiasEnabled() { return classBiasEnabled; }
    public static double relevantChance() { return relevantChance; }
    public static double categoryWeight(ClassLootFamily.Category category) { return categoryWeights.getOrDefault(category, 0D); }
    public static ClassLootPreferences preferences(String classId) {
        return classPreferences.getOrDefault(classId, ClassLootPreferences.defaults(classId));
    }
}
