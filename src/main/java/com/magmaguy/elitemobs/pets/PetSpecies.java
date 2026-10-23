package com.magmaguy.elitemobs.pets;

import com.magmaguy.elitemobs.api.mind.EliteMindBodyLocomotion;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * What a custom boss file's {@code pet:} section says about a pet species: how it is tamed, bred and raised, how it
 * floats and follows, where it spawns wild and how it touches the world. The custom boss itself supplies the body,
 * the model, the name, the level and the drops, as for any other boss.
 */
public final class PetSpecies {
    public record Variant(String model, double weight) {
    }

    public record WildSpawn(boolean enabled, String customSpawn, double chance, int cap) {
    }

    /** Contact sting: wild pets sting players, owned pets sting hostile mobs near their owner. */
    public record Sting(boolean wild, boolean pets, PotionEffectType effect, int durationTicks, int amplifier, double damage) {
    }

    private final String id;
    private final CustomBossesConfigFields boss;
    private final List<Material> tamingFood;
    private final List<Material> breedingFood;
    private final double tameChance;
    private final String babyModel;
    private final double babyScale;
    private final int babyGrowthTicks;
    private final int breedingCooldownTicks;
    private final int loveTicks;
    private final List<Variant> variants;
    private final double followDistance;
    private final double leashDistance;
    private final double teleportDistance;
    private final double roamRadius;
    private final double hoverHeight;
    private final double hoverVariance;
    private final double speed;
    private final boolean canStay;
    private final WildSpawn wildSpawn;
    private final Sting sting;
    private final boolean glow;
    private final EliteMindBodyLocomotion locomotion;

    private PetSpecies(CustomBossesConfigFields boss, ConfigurationSection pet) {
        this.boss = boss;
        String filename = boss.getFilename();
        this.id = pet.getString("species", filename.replace(".yml", "")).toLowerCase(Locale.ROOT);
        this.tamingFood = materials(pet.getStringList("tamingFood"), filename, "tamingFood");
        this.breedingFood = materials(pet.getStringList("breedingFood"), filename, "breedingFood");
        // Wolves and cats tame on a third of feeds; the same default keeps taming a small hunt for food.
        this.tameChance = pet.getDouble("tameChance", 1D / 3D);
        this.babyModel = pet.getString("babyModel", null);
        this.babyScale = pet.getDouble("babyScale", 0.5D);
        // Minecraft babies grow up in 20 minutes and parents breed again after 5.
        this.babyGrowthTicks = (int) (pet.getDouble("babyGrowthMinutes", 20D) * 1200D);
        this.breedingCooldownTicks = (int) (pet.getDouble("breedingCooldownMinutes", 5D) * 1200D);
        this.loveTicks = pet.getInt("loveTicks", 600);
        this.variants = variants(pet.getStringList("variants"), filename);
        ConfigurationSection follow = section(pet, "follow");
        this.followDistance = follow.getDouble("distance", 5D);
        this.leashDistance = follow.getDouble("leashDistance", 12D);
        this.teleportDistance = follow.getDouble("teleportDistance", 24D);
        this.roamRadius = follow.getDouble("roamRadius", 5D);
        this.speed = follow.getDouble("speed", 1D);
        this.canStay = follow.getBoolean("stay", true);
        ConfigurationSection floating = section(pet, "float");
        this.hoverHeight = floating.getDouble("hoverHeight", 1.5D);
        this.hoverVariance = floating.getDouble("hoverVariance", 0.75D);
        ConfigurationSection wild = section(pet, "wildSpawn");
        this.wildSpawn = new WildSpawn(wild.getBoolean("enabled", true), wild.getString("customSpawn", null),
                wild.getDouble("chance", 1D), wild.getInt("cap", 4));
        ConfigurationSection stingSection = section(pet, "sting");
        PotionEffectType effect = effect(stingSection.getString("effect", "SLOWNESS"), filename);
        this.sting = new Sting(stingSection.getBoolean("wild", true), stingSection.getBoolean("pets", true), effect,
                stingSection.getInt("durationTicks", 60), stingSection.getInt("amplifier", 0), stingSection.getDouble("damage", 0D));
        this.glow = pet.getBoolean("glow", true);
        EliteMindBodyLocomotion movement;
        try {
            movement = EliteMindBodyLocomotion.valueOf(pet.getString("locomotion", "FLYING").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            Logger.warn("Unknown pet.locomotion in " + filename + "; using FLYING");
            movement = EliteMindBodyLocomotion.FLYING;
        }
        this.locomotion = movement;
    }

    /** The species a boss file defines in its {@code pet:} section, or null when it has none. */
    public static PetSpecies parse(CustomBossesConfigFields boss, ConfigurationSection pet) {
        if (pet == null) return null;
        try {
            return new PetSpecies(boss, pet);
        } catch (RuntimeException failure) {
            Logger.warn("Could not read the pet section of " + boss.getFilename() + ": " + failure.getMessage());
            return null;
        }
    }

    private static ConfigurationSection section(ConfigurationSection parent, String key) {
        ConfigurationSection section = parent.getConfigurationSection(key);
        return section != null ? section : parent.createSection(key);
    }

    private static List<Material> materials(List<String> names, String filename, String key) {
        List<Material> materials = new ArrayList<>();
        for (String name : names) {
            Material material = Material.matchMaterial(name);
            if (material == null) Logger.warn("Unknown item " + name + " in pet." + key + " of " + filename);
            else materials.add(material);
        }
        return List.copyOf(materials);
    }

    private static PotionEffectType effect(String name, String filename) {
        PotionEffectType effect = PotionEffectType.getByName(name.toUpperCase(Locale.ROOT));
        if (effect == null) Logger.warn("Unknown potion effect " + name + " in pet.sting.effect of " + filename);
        return effect;
    }

    /** {@code model} or {@code model:weight}; the boss's own model is the only variant when none are listed. */
    private static List<Variant> variants(List<String> entries, String filename) {
        List<Variant> variants = new ArrayList<>();
        for (String entry : entries) {
            String[] parts = entry.split(":");
            double weight = 1D;
            if (parts.length > 1) {
                try {
                    weight = Double.parseDouble(parts[1]);
                } catch (NumberFormatException ignored) {
                    Logger.warn("Bad variant weight in " + entry + " of " + filename + "; using 1");
                }
            }
            variants.add(new Variant(parts[0].trim(), Math.max(0D, weight)));
        }
        return List.copyOf(variants);
    }

    public String id() {
        return id;
    }

    public CustomBossesConfigFields boss() {
        return boss;
    }

    public List<Material> tamingFood() {
        return tamingFood;
    }

    public List<Material> breedingFood() {
        return breedingFood;
    }

    public boolean isTamingFood(ItemStack item) {
        return item != null && tamingFood.contains(item.getType());
    }

    public boolean isBreedingFood(ItemStack item) {
        return item != null && breedingFood.contains(item.getType());
    }

    public double tameChance() {
        return tameChance;
    }

    public String babyModel() {
        return babyModel;
    }

    public double babyScale() {
        return babyScale;
    }

    public int babyGrowthTicks() {
        return babyGrowthTicks;
    }

    public int breedingCooldownTicks() {
        return breedingCooldownTicks;
    }

    public int loveTicks() {
        return loveTicks;
    }

    public List<Variant> variants() {
        return variants;
    }

    /** A weighted variant model for a new pet, or the boss's model when the species lists none. */
    public String pickVariant(Random random) {
        double total = variants.stream().mapToDouble(Variant::weight).sum();
        if (variants.isEmpty() || total <= 0D) return boss.getCustomModel();
        double roll = random.nextDouble() * total;
        for (Variant variant : variants) {
            roll -= variant.weight();
            if (roll <= 0D) return variant.model();
        }
        return variants.get(variants.size() - 1).model();
    }

    public double followDistance() {
        return followDistance;
    }

    public double leashDistance() {
        return leashDistance;
    }

    public double teleportDistance() {
        return teleportDistance;
    }

    public double roamRadius() {
        return roamRadius;
    }

    public double hoverHeight() {
        return hoverHeight;
    }

    public double hoverVariance() {
        return hoverVariance;
    }

    public double speed() {
        return speed;
    }

    public boolean canStay() {
        return canStay;
    }

    public WildSpawn wildSpawn() {
        return wildSpawn;
    }

    public Sting sting() {
        return sting;
    }

    public boolean glow() {
        return glow;
    }

    public EliteMindBodyLocomotion locomotion() {
        return locomotion;
    }
}
