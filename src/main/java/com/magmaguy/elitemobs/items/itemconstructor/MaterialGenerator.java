package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.combatsystem.CombatSystem;
import com.magmaguy.elitemobs.config.ProceduralItemGenerationSettingsConfig;
import com.magmaguy.elitemobs.utils.VersionChecker;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.bukkit.Material.*;

public class MaterialGenerator {

    public static Material generateMaterial(Material material) {

        return material;

    }

    public static Material generateMaterial(double itemTier) {

        List<Material> localValidMaterials = (List<Material>) validProceduralMaterials.clone();

        if (localValidMaterials.isEmpty()) initializeValidProceduralMaterials();


        if (itemTier < CombatSystem.TRIDENT_TIER_LEVEL)
            localValidMaterials.remove(TRIDENT);

        if (VersionChecker.currentVersionIsUnder(16, 0) && itemTier < CombatSystem.NETHERITE_TIER_LEVEL)

            if (itemTier < CombatSystem.DIAMOND_TIER_LEVEL) {
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

        int index = ThreadLocalRandom.current().nextInt(localValidMaterials.size());

        Material material = localValidMaterials.get(index);

        return material;

    }

    private static final ArrayList<Material> validProceduralMaterials = new ArrayList();

    public static void initializeValidProceduralMaterials() {

        if (ProceduralItemGenerationSettingsConfig.validMaterials.isEmpty()) {
            ProceduralItemGenerationSettingsConfig.cacheMaterials();
            if (ProceduralItemGenerationSettingsConfig.validMaterials.isEmpty()) {
                new WarningMessage("No valid materials detected for the procedural item settings. If you are trying to disable" +
                        " them, use the 'dropProcedurallyGeneratedItems' option instead. Warn the developer.");
                return;
            }
        }

        for (String string : ProceduralItemGenerationSettingsConfig.validMaterials) {
            try {
                validProceduralMaterials.add(getMaterial(string));
            } catch (Exception e) {
                Bukkit.getLogger().info("Invalid material type detected: " + string);
            }
        }

    }

}
