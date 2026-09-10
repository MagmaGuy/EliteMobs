package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.magmacore.enchantments.*;
import com.magmaguy.magmacore.scripting.ScriptHook;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** EM-owned definitions and numeric contributions. Native overflow remains in EnchantmentsConfig. */
public final class EliteEnchantmentCatalog {
    public static final String LOUD_STRIKES = "elitemobs:loud_strikes";
    public static final String CRITICAL_STRIKES = "elitemobs:critical_strikes";
    public static final ScriptHook THREAT_BONUS = new ScriptHook("on_threat_bonus");
    public static final ScriptHook CRITICAL_CHANCE = new ScriptHook("on_critical_chance");
    private static final Set<ScriptHook> HOOKS = Set.of(THREAT_BONUS, CRITICAL_CHANCE);
    private static final Set<String> SHARED = Set.of("loud_strikes", "critical_strikes");
    // Removed as each remaining effect is moved to this provider. Soulbind is an ownership setting.
    private static final Set<String> REMAINING_CONFIGS = Set.of("hunter", "lightning", "flamethrower", "drilling",
            "ice_breaker", "earthquake", "plasma_boots", "grappling_hook", "meteor_shower",
            "summon_merchant", "summon_wolf", "soulbind", "multicast", "blast_radius", "ignition");
    private static volatile EnchantmentCatalog catalog;
    private static volatile Set<String> sharedNames = SHARED;
    private static EnchantmentDefinitions.HostedCatalog hosted;
    private static long revision;

    private EliteEnchantmentCatalog() { }

    public static boolean ownsFilename(String filename) {
        String stem = filename.toLowerCase(Locale.ROOT).replaceFirst("\\.ya?ml$", "");
        return sharedNames.contains(stem);
    }

    /** Called before the legacy/native configuration owner writes its defaults. */
    public static void prepare() {
        var plugin = MetadataHandler.PLUGIN;
        Path directory = plugin.getDataFolder().toPath().resolve("enchantments");
        try {
            if (Files.notExists(directory)) {
                Files.createDirectories(directory);
                for (String name : SHARED) {
                    plugin.saveResource("enchantments/" + name + ".yml", false);
                    plugin.saveResource("enchantments/" + name + ".lua", false);
                }
            }
            Set<String> selectedNames = new HashSet<>(SHARED);
            var candidate = EnchantmentCatalog.load("elitemobs", directory, HOOKS, path -> {
                String stem = path.getFileName().toString().toLowerCase(Locale.ROOT).replaceFirst("\\.ya?ml$", "");
                if (REMAINING_CONFIGS.contains(stem)
                        || org.bukkit.enchantments.Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft(stem)) != null)
                    return false;
                try {
                    var yaml = new YamlConfiguration();
                    yaml.load(path.toFile());
                    if (!yaml.contains("script") && yaml.contains("maxLevelV2")) {
                        plugin.getLogger().warning("Skipping retired enchantment configuration " + path
                                + "; supply the current YAML/Lua definition. No content was converted.");
                        return false;
                    }
                    selectedNames.add(stem);
                    return true;
                } catch (Exception failure) {
                    throw new UncheckedIOException(new IOException("Cannot read " + path, failure));
                }
            });
            catalog = candidate;
            sharedNames = Set.copyOf(selectedNames);
        } catch (IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }

    public static void publish() {
        if (hosted == null)
            hosted = EnchantmentDefinitions.publishQueries(MetadataHandler.PLUGIN, catalog, Set.of(), HOOKS,
                    (operation, request) -> Map.of("supported", false));
        else hosted.reload(catalog);
        revision++;
    }

    public static void close() {
        if (hosted != null) hosted.close();
        hosted = null;
        catalog = null;
        revision++;
    }

    public static long revision() { return revision; }

    public static EnchantmentDefinition definition(String id) {
        var current = catalog;
        return current == null ? null : current.definitions().get(id);
    }

    public static String text(String id, String parameter, String fallback) {
        var definition = definition(id);
        Object value = definition == null ? null : definition.parameters().get(parameter);
        return value instanceof String text ? text : fallback;
    }

    public static double contribution(ItemStack item, ScriptHook hook, UUID actor, EnchantmentDefinition.Slot slot) {
        if (item == null || !item.hasItemMeta() || com.magmaguy.elitemobs.api.utils.EliteItemManager.isOnLastDamage(item)) return 0;
        Map<String, Integer> levels;
        try { levels = EnchantmentItems.inspectCustom(item.getItemMeta()); }
        catch (IllegalArgumentException corrupt) { return 0; }
        var requests = new ArrayList<EnchantmentQueries.Query>();
        var profile = EnchantmentItems.classify(item);
        for (var entry : new TreeMap<>(levels).entrySet()) {
            var definition = definition(entry.getKey());
            if (definition == null || entry.getValue() > definition.maxLevel()
                    || !definition.validSlots().isEmpty() && !definition.validSlots().contains(slot)
                    || !definition.itemTypes().isEmpty() && !definition.itemTypes().contains(profile.type())
                    || !definition.attackKinds().isEmpty() && Collections.disjoint(definition.attackKinds(), profile.attackKinds())) continue;
            var resolved = EnchantmentDefinitions.resolve(entry.getKey());
            if (resolved == null || !resolved.available()) continue;
            requests.add(new EnchantmentQueries.Query(resolved.provider(), entry.getKey(), hook,
                    entry.getValue(), Map.of("slot", slot.name()), actor, null));
        }
        if (requests.isEmpty()) return 0;
        var result = EnchantmentQueries.evaluate(requests, new EnchantmentQueries.Limits(64, 50_000_000L, 250_000L));
        if (result.status() != EnchantmentQueries.Status.OK) return 0;
        double total = 0;
        for (var contribution : result.contributions()) {
            if (contribution.value() == null) continue;
            double value = contribution.value();
            if (!Double.isFinite(value) || value < 0) throw new IllegalArgumentException("Invalid EM contribution from " + contribution.id());
            total += value;
        }
        if (!Double.isFinite(total)) throw new IllegalArgumentException("EM contribution overflow");
        return total;
    }

    public static Map<String, Integer> procedural(String id) {
        var definition = definition(id);
        if (definition == null || !Boolean.TRUE.equals(definition.parameters().get("procedural"))) return Map.of();
        Object raw = definition.parameters().get("procedural_max_level");
        if (!(raw instanceof Integer maximum) || maximum < 1 || maximum > definition.maxLevel())
            throw new IllegalArgumentException("Invalid procedural limit for " + id);
        return Map.of(id, java.util.concurrent.ThreadLocalRandom.current().nextInt(maximum) + 1);
    }
}
