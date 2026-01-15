package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.combatsystem.CombatSystem;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.ProceduralItemGenerationSettingsConfig;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.bukkit.Material.*;

public class MaterialGenerator {

    private static final ArrayList<Material> validProceduralMaterials = new ArrayList();

    public static Material generateMaterial(Material material) {

        return material;

    }

    /**
     * Generates a random material from all valid procedural materials, ignoring tier restrictions.
     * Used by the skill-based shop system to first pick a material, then determine level based on player's skill.
     *
     * @return A random valid material, or null if none available
     */
    public static Material generateRandomMaterial() {
        if (validProceduralMaterials.isEmpty()) initializeValidProceduralMaterials();
        if (validProceduralMaterials.isEmpty()) return null;
        return validProceduralMaterials.get(ThreadLocalRandom.current().nextInt(validProceduralMaterials.size()));
    }

    public static Material generateMaterial(double itemTier) {

        List<Material> localValidMaterials = (List<Material>) validProceduralMaterials.clone();

        if (localValidMaterials.isEmpty()) initializeValidProceduralMaterials();

        if (itemTier < CombatSystem.DIAMOND_TIER_LEVEL + ItemSettingsConfig.getMinimumProcedurallyGeneratedDiamondLootLevelPlusSeven())
            localValidMaterials.remove(TRIDENT);

        if (itemTier < CombatSystem.DIAMOND_TIER_LEVEL + ItemSettingsConfig.getMinimumProcedurallyGeneratedDiamondLootLevelPlusSeven()) {
            localValidMaterials.remove(DIAMOND_AXE);
            localValidMaterials.remove(DIAMOND_HORSE_ARMOR);
            localValidMaterials.remove(DIAMOND_CHESTPLATE);
            localValidMaterials.remove(DIAMOND_HELMET);
            localValidMaterials.remove(DIAMOND_HOE);
            localValidMaterials.remove(DIAMOND_LEGGINGS);
            localValidMaterials.remove(DIAMOND_PICKAXE);
            localValidMaterials.remove(DIAMOND_SHOVEL);
            localValidMaterials.remove(DIAMOND_SWORD);
            localValidMaterials.remove(DIAMOND_BOOTS);
        }

        if (itemTier < CombatSystem.IRON_TIER_LEVEL) {

            localValidMaterials.remove(IRON_AXE);
            localValidMaterials.remove(IRON_HORSE_ARMOR);
            localValidMaterials.remove(IRON_BOOTS);
            localValidMaterials.remove(IRON_CHESTPLATE);
            localValidMaterials.remove(IRON_HELMET);
            localValidMaterials.remove(IRON_HOE);
            localValidMaterials.remove(IRON_LEGGINGS);
            localValidMaterials.remove(IRON_PICKAXE);
            localValidMaterials.remove(IRON_SHOVEL);
            localValidMaterials.remove(IRON_SWORD);
            localValidMaterials.remove(BOW);
            localValidMaterials.remove(CROSSBOW);
            localValidMaterials.remove(TURTLE_HELMET);
        }

        if (itemTier < CombatSystem.STONE_CHAIN_TIER_LEVEL) {

            localValidMaterials.remove(CHAINMAIL_BOOTS);
            localValidMaterials.remove(CHAINMAIL_CHESTPLATE);
            localValidMaterials.remove(CHAINMAIL_HELMET);
            localValidMaterials.remove(CHAINMAIL_LEGGINGS);
            localValidMaterials.remove(STONE_SWORD);
            localValidMaterials.remove(STONE_HOE);
            localValidMaterials.remove(STONE_SHOVEL);
            localValidMaterials.remove(STONE_PICKAXE);
            localValidMaterials.remove(STONE_AXE);

        }

        if (itemTier < CombatSystem.GOLD_WOOD_LEATHER_TIER_LEVEL) {
            localValidMaterials.remove(GOLDEN_BOOTS);
            localValidMaterials.remove(GOLDEN_CHESTPLATE);
            localValidMaterials.remove(GOLDEN_HELMET);
            localValidMaterials.remove(GOLDEN_LEGGINGS);
            localValidMaterials.remove(GOLDEN_SWORD);
            localValidMaterials.remove(GOLDEN_HOE);
            localValidMaterials.remove(GOLDEN_SHOVEL);
            localValidMaterials.remove(GOLDEN_PICKAXE);
            localValidMaterials.remove(GOLDEN_AXE);
            localValidMaterials.remove(WOODEN_SWORD);
            localValidMaterials.remove(WOODEN_AXE);
            localValidMaterials.remove(WOODEN_HOE);
        }

        if (localValidMaterials.isEmpty()) return null;

        return localValidMaterials.get(ThreadLocalRandom.current().nextInt(localValidMaterials.size()));

    }

    public static void initializeValidProceduralMaterials() {

        validProceduralMaterials.clear();

        if (ProceduralItemGenerationSettingsConfig.getValidMaterials().isEmpty()) {
            ProceduralItemGenerationSettingsConfig.getInstance().cacheMaterials();
            if (ProceduralItemGenerationSettingsConfig.getValidMaterials().isEmpty()) {
                Logger.warn("No valid materials detected for the procedural item settings. If you are trying to disable" +
                        " them, use the 'dropProcedurallyGeneratedItems' option instead. Warn the developer.");
                return;
            }
        }

        for (String string : ProceduralItemGenerationSettingsConfig.getValidMaterials()) {
            try {
                validProceduralMaterials.add(getMaterial(string));
            } catch (Exception e) {
                Logger.info("Invalid material type detected: " + string);
            }
        }

    }

}
