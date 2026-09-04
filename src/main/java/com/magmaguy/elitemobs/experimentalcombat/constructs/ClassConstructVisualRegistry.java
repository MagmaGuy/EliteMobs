package com.magmaguy.elitemobs.experimentalcombat.constructs;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Code-owned physical-fantasy catalog for packet-only class constructs. */
public final class ClassConstructVisualRegistry {
    private static final double DEFAULT_VIEW_RANGE = 48D;

    private final Map<String, Plan> plans;

    private ClassConstructVisualRegistry(Map<String, Plan> plans) {
        this.plans = Collections.unmodifiableMap(new LinkedHashMap<>(plans));
    }

    public static ClassConstructVisualRegistry builtIns() {
        Map<String, Plan> plans = new LinkedHashMap<>();
        fixed(plans, "aegis.utility", "sanctuary_wall",
                block(-1, 0, 0, "minecraft:quartz_bricks"),
                block(0, 0, 0, "minecraft:quartz_bricks"),
                block(1, 0, 0, "minecraft:quartz_bricks"),
                block(-1, 1, 0, "minecraft:white_stained_glass"),
                block(0, 1, 0, "minecraft:white_stained_glass"),
                block(1, 1, 0, "minecraft:white_stained_glass"));
        fixed(plans, "bulwark.utility", "unyielding_ground",
                block(0, 0, 0, "minecraft:polished_blackstone_pressure_plate"),
                block(1, 0, 0, "minecraft:light_gray_carpet"),
                block(-1, 0, 0, "minecraft:light_gray_carpet"),
                block(0, 0, 1, "minecraft:light_gray_carpet"),
                block(0, 0, -1, "minecraft:light_gray_carpet"));
        fixed(plans, "shieldbearer.utility", "covering_wall",
                block(-1, 0, 0, "minecraft:blue_stained_glass"),
                block(0, 0, 0, "minecraft:blue_stained_glass"),
                block(1, 0, 0, "minecraft:blue_stained_glass"),
                block(-1, 1, 0, "minecraft:iron_bars"),
                block(0, 1, 0, "minecraft:iron_bars"),
                block(1, 1, 0, "minecraft:iron_bars"));
        fixed(plans, "marshal.signature", "battle_standard",
                block(0, 0, 0, "minecraft:blue_banner"));
        following(plans, "bannerlord.signature", "grand_standard",
                block(0, 1, 0, "minecraft:red_banner"));
        fixed(plans, "artillerist.utility", "quickload_cache",
                block(1, 0, 0, "minecraft:barrel"));
        fixed(plans, "pathfinder.utility", "guide_beacon",
                block(0, 0, 0, "minecraft:oak_fence"),
                block(0, 1, 0, "minecraft:soul_lantern"));
        fixed(plans, "saboteur.utility", "snare_beacon",
                block(0, 0, 0, "minecraft:tripwire"),
                block(1, 0, 0, "minecraft:tripwire"),
                block(-1, 0, 0, "minecraft:tripwire"),
                block(0, 0, 1, "minecraft:tripwire"),
                block(0, 0, -1, "minecraft:tripwire"));
        fixed(plans, "trapper.signature", "kill_zone",
                block(0, 0, 0, "minecraft:cobweb"),
                block(2, 0, 0, "minecraft:tripwire"),
                block(-2, 0, 0, "minecraft:tripwire"),
                block(0, 0, 2, "minecraft:tripwire"),
                block(0, 0, -2, "minecraft:tripwire"));
        fixed(plans, "cleric.utility", "sanctuary",
                block(0, 0, 0, "minecraft:white_candle"),
                block(1, 0, 0, "minecraft:white_carpet"),
                block(-1, 0, 0, "minecraft:white_carpet"),
                block(0, 0, 1, "minecraft:white_carpet"),
                block(0, 0, -1, "minecraft:white_carpet"));
        fixed(plans, "saint.utility", "hallowed_ground",
                block(0, 0, 0, "minecraft:yellow_candle"),
                block(2, 0, 0, "minecraft:white_candle"),
                block(-2, 0, 0, "minecraft:white_candle"),
                block(0, 0, 2, "minecraft:white_candle"),
                block(0, 0, -2, "minecraft:white_candle"));
        fixed(plans, "shaman.utility", "spirit_totem",
                block(0, 0, 0, "minecraft:dark_oak_fence"),
                block(0, 1, 0, "minecraft:soul_lantern"));
        fixed(plans, "lifewarden.utility", "sheltering_seed",
                block(0, 0, 0, "minecraft:mangrove_propagule"));
        fixed(plans, "grovekeeper.signature", "verdant_bloom",
                block(0, 0, 0, "minecraft:flowering_azalea"),
                block(2, 0, 0, "minecraft:moss_carpet"),
                block(-2, 0, 0, "minecraft:moss_carpet"),
                block(0, 0, 2, "minecraft:moss_carpet"),
                block(0, 0, -2, "minecraft:moss_carpet"));
        fixed(plans, "grovekeeper.utility", "thorn_ward",
                block(0, 0, 0, "minecraft:flowering_azalea"),
                block(2, 0, 0, "minecraft:sweet_berry_bush"),
                block(-2, 0, 0, "minecraft:sweet_berry_bush"),
                block(0, 0, 2, "minecraft:sweet_berry_bush"),
                block(0, 0, -2, "minecraft:sweet_berry_bush"));
        return new ClassConstructVisualRegistry(plans);
    }

    public Optional<Plan> find(String abilityId) {
        return Optional.ofNullable(plans.get(abilityId));
    }

    public Plan require(String abilityId) {
        return find(abilityId).orElseThrow(() ->
                new IllegalArgumentException("No packet construct for " + abilityId));
    }

    public Set<String> abilityIds() {
        return plans.keySet();
    }

    private static void fixed(
            Map<String, Plan> plans,
            String abilityId,
            String constructId,
            PacketConstructDefinition.BlockVisual... blocks) {
        add(plans, abilityId, constructId, AnchorMode.FIXED, blocks);
    }

    private static void following(
            Map<String, Plan> plans,
            String abilityId,
            String constructId,
            PacketConstructDefinition.BlockVisual... blocks) {
        add(plans, abilityId, constructId, AnchorMode.FOLLOW_CASTER, blocks);
    }

    private static void add(
            Map<String, Plan> plans,
            String abilityId,
            String constructId,
            AnchorMode anchorMode,
            PacketConstructDefinition.BlockVisual... blocks) {
        Plan previous = plans.putIfAbsent(abilityId, new Plan(
                new PacketConstructDefinition(
                        constructId, DEFAULT_VIEW_RANGE, java.util.List.of(blocks)),
                anchorMode));
        if (previous != null) throw new IllegalArgumentException(
                "Duplicate packet construct ability " + abilityId);
    }

    private static PacketConstructDefinition.BlockVisual block(
            int x,
            int y,
            int z,
            String data) {
        return new PacketConstructDefinition.BlockVisual(x, y, z, data);
    }

    public enum AnchorMode {
        FIXED,
        FOLLOW_CASTER
    }

    public record Plan(PacketConstructDefinition definition, AnchorMode anchorMode) {
        public Plan {
            Objects.requireNonNull(definition, "definition");
            Objects.requireNonNull(anchorMode, "anchorMode");
        }
    }
}
