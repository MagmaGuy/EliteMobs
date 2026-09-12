package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.magmacore.enchantments.*;
import com.magmaguy.magmacore.scripting.ScriptHook;
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
    public static final ScriptHook HUNTER_BONUS = new ScriptHook("on_natural_spawn_bonus");
    private static final Set<ScriptHook> HOOKS = Set.of(THREAT_BONUS, CRITICAL_CHANCE, HUNTER_BONUS);
    private static final Set<ScriptHook> ALL_HOOKS = java.util.stream.Stream.concat(HOOKS.stream(),
            EnchantmentInputs.HOOKS.stream()).collect(java.util.stream.Collectors.toUnmodifiableSet());
    private static final Set<String> SHARED = Set.of("loud_strikes", "critical_strikes", "drilling", "ice_breaker", "summon_wolf", "summon_merchant", "flamethrower", "lightning", "hunter", "earthquake", "plasma_boots", "grappling_hook", "meteor_shower");
    /** EM acquisition probabilities and Soulbind ownership retain their existing configuration owner. */
    public static final Set<String> HOST_SETTINGS = Set.of("soulbind", "multicast", "blast_radius", "ignition");
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
            EnchantmentCatalog.initializeDefaults(plugin, directory, SHARED);
            Set<String> selectedNames = new HashSet<>(SHARED);
            var candidate = EnchantmentCatalog.load("elitemobs", directory, ALL_HOOKS, path -> {
                String stem = path.getFileName().toString().toLowerCase(Locale.ROOT).replaceFirst("\\.ya?ml$", "");
                if (HOST_SETTINGS.contains(stem)
                        || org.bukkit.enchantments.Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft(stem)) != null)
                    return false;
                selectedNames.add(stem);
                return true;
            });
            catalog = candidate;
            sharedNames = Set.copyOf(selectedNames);
        } catch (IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }

    public static void publish() {
        if (hosted == null)
            hosted = EnchantmentDefinitions.publishActions(MetadataHandler.PLUGIN, catalog, Set.of(EnchantmentActions.SOURCE_FACTS), HOOKS,
                    EnchantmentInputs.HOOKS, (operation, request) -> {
                        if (operation == EnchantmentProviders.Operation.EVALUATE
                                && EnchantmentActions.SOURCE_FACTS.equals(request.get("kind")))
                            return captureCombatFacts(request);
                        if (operation != EnchantmentProviders.Operation.EVALUATE
                                || !"attributed_damage".equals(request.get("kind"))) return Map.of("supported", false);
                        var input = EnchantmentActions.DamageInput.read(request);
                        return Map.of("applied", input != null && EnchantmentInputs.applyExplicitDamage(MetadataHandler.PLUGIN,
                                input.actor(), input.target(), () -> com.magmaguy.elitemobs.combatsystem.EnchantmentDamage.apply(
                                        input.attackId(), input.providerFacts(),
                                        input::applyDamage)));
                    });
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

    /** Natural spawning retains its existing player selection and world policy. */
    public static double huntingGearBonus(List<org.bukkit.entity.Player> players) {
        double bonus = 0;
        for (var player : players) {
            var inventory = com.magmaguy.elitemobs.playerdata.ElitePlayerInventory.playerInventories.get(player.getUniqueId());
            if (inventory != null) bonus += inventory.getHunterChance(true);
        }
        return bonus;
    }

    private static Map<String, Object> captureCombatFacts(Map<String, Object> request) {
        if (!request.keySet().equals(Set.of("kind", "actor", "equipment"))
                || !(request.get("actor") instanceof UUID actorId)
                || !(request.get("equipment") instanceof Map<?, ?> equipment))
            throw new IllegalArgumentException("Invalid EM source facts request");
        var player = org.bukkit.Bukkit.getPlayer(actorId);
        if (player == null || !player.isOnline()) throw new IllegalArgumentException("Missing source player");
        double threat = 0;
        int weaponLevel = 0;
        Map<String, Boolean> usableSlots = new LinkedHashMap<>();
        Map<String, Integer> equippedLevels = new TreeMap<>();
        for (var entry : equipment.entrySet()) {
            var slot = EnchantmentDefinition.Slot.valueOf((String) entry.getKey());
            if (!(entry.getValue() instanceof ItemStack item)) throw new IllegalArgumentException("Invalid source item");
            if (com.magmaguy.elitemobs.api.utils.EliteItemManager.isOnLastDamage(item)
                    || !com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment.isValidSoulbindUser(item.getItemMeta(), player)
                    || !com.magmaguy.elitemobs.items.GearRestrictionHandler.canEquip(player, item)) continue;
            usableSlots.put(slot.name(), true);
            var profile = EnchantmentItems.classify(item);
            for (var enchantment : EnchantmentItems.inspectCustom(item.getItemMeta()).entrySet()) {
                var definition = definition(enchantment.getKey());
                if (definition == null || enchantment.getValue() < 1
                        || !definition.validSlots().isEmpty() && !definition.validSlots().contains(slot)
                        || !definition.itemTypes().isEmpty() && !definition.itemTypes().contains(profile.type())) continue;
                equippedLevels.merge(enchantment.getKey(), enchantment.getValue(), Math::addExact);
            }
            if (slot == EnchantmentDefinition.Slot.MAINHAND)
                weaponLevel = com.magmaguy.elitemobs.playerdata.PlayerItem.readWeaponTier(player, item);
            threat += contribution(item, THREAT_BONUS, actorId, slot);
        }
        if (!Double.isFinite(threat)) throw new IllegalArgumentException("Invalid captured threat bonus");
        return Map.of("weapon_level", weaponLevel, "loud_strikes", threat,
                "usable_slots", usableSlots, "equipped_levels", equippedLevels);
    }

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
        if (levels.isEmpty()) return 0;
        var requests = new ArrayList<EnchantmentQueries.Query>();
        EnchantmentItemProfile profile;
        try { profile = EnchantmentItems.classify(item); }
        catch (IllegalArgumentException unavailable) { return 0; }
        for (var entry : new TreeMap<>(levels).entrySet()) {
            var definition = definition(entry.getKey());
            if (definition == null || entry.getValue() < 1
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
        if (!(raw instanceof Integer maximum) || maximum < 1)
            throw new IllegalArgumentException("Invalid procedural limit for " + id);
        return Map.of(id, java.util.concurrent.ThreadLocalRandom.current().nextInt(maximum) + 1);
    }
}
