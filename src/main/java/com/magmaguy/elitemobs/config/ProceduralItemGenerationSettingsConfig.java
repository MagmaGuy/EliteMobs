package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.items.itemconstructor.MaterialGenerator;
import com.magmaguy.magmacore.config.ConfigurationFile;
import lombok.Getter;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class ProceduralItemGenerationSettingsConfig extends ConfigurationFile {
    @Getter
    private static final List<String> validMaterials = new ArrayList<>();
    @Getter
    private
    static boolean doProceduralItemDrops;
    @Getter
    private static double customEnchantmentChance;
    @Getter
    private static ProceduralItemGenerationSettingsConfig instance;

    public ProceduralItemGenerationSettingsConfig() {
        super("ProceduralItemGenerationSettings.yml");
    }

    private void addMaterial(Material material) {
        String key = "validMaterials." + material.name();
        if (!fileConfiguration.contains(key))
            fileConfiguration.set(key, true);
    }

    public void cacheMaterials() {
        validMaterials.clear();
        var section = fileConfiguration.getConfigurationSection("validMaterials");
        if (section == null) return;
        for (String material : section.getKeys(false))
            if (section.getBoolean(material))
                validMaterials.add(material);
    }


    @Override
    public void initializeValues() {
        instance = this;

        doProceduralItemDrops = ConfigurationEngine.setBoolean(fileConfiguration, "dropProcedurallyGeneratedItems", true);
        customEnchantmentChance = ConfigurationEngine.setDouble(fileConfiguration, "customEnchantmentsChance", 0.5);

        addMaterial(Material.DIAMOND_HELMET);
        addMaterial(Material.DIAMOND_CHESTPLATE);
        addMaterial(Material.DIAMOND_LEGGINGS);
        addMaterial(Material.DIAMOND_BOOTS);

        addMaterial(Material.DIAMOND_SWORD);
        addMaterial(Material.DIAMOND_AXE);
        addMaterial(Material.DIAMOND_HOE);

        addMaterial(Material.IRON_HELMET);
        addMaterial(Material.IRON_CHESTPLATE);
        addMaterial(Material.IRON_LEGGINGS);
        addMaterial(Material.IRON_BOOTS);

        addMaterial(Material.IRON_SWORD);
        addMaterial(Material.IRON_AXE);
        addMaterial(Material.IRON_HOE);

        addMaterial(Material.GOLDEN_HELMET);
        addMaterial(Material.GOLDEN_CHESTPLATE);
        addMaterial(Material.GOLDEN_LEGGINGS);
        addMaterial(Material.GOLDEN_BOOTS);

        addMaterial(Material.GOLDEN_SWORD);
        addMaterial(Material.GOLDEN_AXE);
        addMaterial(Material.GOLDEN_HOE);

        addMaterial(Material.CHAINMAIL_HELMET);
        addMaterial(Material.CHAINMAIL_CHESTPLATE);
        addMaterial(Material.CHAINMAIL_LEGGINGS);
        addMaterial(Material.CHAINMAIL_BOOTS);


        addMaterial(Material.LEATHER_HELMET);
        addMaterial(Material.LEATHER_CHESTPLATE);
        addMaterial(Material.LEATHER_LEGGINGS);
        addMaterial(Material.LEATHER_BOOTS);


        addMaterial(Material.STONE_SWORD);
        addMaterial(Material.STONE_AXE);
        addMaterial(Material.STONE_HOE);

        addMaterial(Material.WOODEN_SWORD);
        addMaterial(Material.WOODEN_AXE);
        addMaterial(Material.WOODEN_HOE);

        addMaterial(Material.SHIELD);
        addMaterial(Material.TURTLE_HELMET);
        addMaterial(Material.TRIDENT);
        addMaterial(Material.BOW);
        addMaterial(Material.CROSSBOW);

        // MACE (1.21+) - EliteMobs already requires 1.21+, so no version check needed
        try {
            addMaterial(Material.MACE);
        } catch (NoSuchFieldError e) {
            // MACE doesn't exist pre-1.21
        }

        // SPEARS (1.21.11+) - tiered like swords
        try {
            addMaterial(Material.DIAMOND_SPEAR);
            addMaterial(Material.IRON_SPEAR);
            addMaterial(Material.GOLDEN_SPEAR);
            addMaterial(Material.STONE_SPEAR);
            addMaterial(Material.WOODEN_SPEAR);
            addMaterial(Material.COPPER_SPEAR);
            addMaterial(Material.NETHERITE_SPEAR);
        } catch (NoSuchFieldError e) {
            // SPEAR doesn't exist pre-1.21.11
        }

        cacheMaterials();

        MaterialGenerator.initializeValidProceduralMaterials();
    }
}
