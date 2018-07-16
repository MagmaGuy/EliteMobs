package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.ItemsProceduralSettingsConfig;
import com.magmaguy.elitemobs.mobcustomizer.DamageAdjuster;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.bukkit.Material.*;

public class MaterialGenerator {

    public static Material generateMaterial(Material material){

        return material;

    }

    private static List<Material> validMaterials = new ArrayList<>();

    public static Material generateMaterial(double itemTier){

        if (validMaterials.isEmpty()) intializeValidProceduralMaterials();

        if (itemTier < DamageAdjuster.DIAMOND_TIER_LEVEL) {

            validMaterials.remove(DIAMOND);
            validMaterials.remove(DIAMOND_AXE);
            validMaterials.remove(DIAMOND_BARDING);
            validMaterials.remove(DIAMOND_BLOCK);
            validMaterials.remove(DIAMOND_CHESTPLATE);
            validMaterials.remove(DIAMOND_HELMET);
            validMaterials.remove(DIAMOND_HOE);
            validMaterials.remove(DIAMOND_LEGGINGS);
            validMaterials.remove(DIAMOND_ORE);
            validMaterials.remove(DIAMOND_PICKAXE);
            validMaterials.remove(DIAMOND_SPADE);
            validMaterials.remove(DIAMOND_SWORD);

        }

        if (itemTier < DamageAdjuster.IRON_TIER_LEVEL) {

            validMaterials.remove(IRON_AXE);
            validMaterials.remove(IRON_BARDING);
            validMaterials.remove(IRON_BLOCK);
            validMaterials.remove(IRON_BOOTS);
            validMaterials.remove(IRON_CHESTPLATE);
            validMaterials.remove(IRON_HELMET);
            validMaterials.remove(IRON_HOE);
            validMaterials.remove(IRON_INGOT);
            validMaterials.remove(IRON_LEGGINGS);
            validMaterials.remove(IRON_NUGGET);
            validMaterials.remove(IRON_ORE);
            validMaterials.remove(IRON_PICKAXE);
            validMaterials.remove(IRON_SPADE);
            validMaterials.remove(IRON_SWORD);
            validMaterials.remove(IRON_BOOTS);

        }

        if (itemTier < DamageAdjuster.STONE_CHAIN_TIER_LEVEL) {

            validMaterials.remove(CHAINMAIL_BOOTS);
            validMaterials.remove(CHAINMAIL_CHESTPLATE);
            validMaterials.remove(CHAINMAIL_HELMET);
            validMaterials.remove(CHAINMAIL_LEGGINGS);
            validMaterials.remove(STONE_SWORD);
            validMaterials.remove(STONE_HOE);
            validMaterials.remove(STONE_SPADE);
            validMaterials.remove(STONE_PICKAXE);
            validMaterials.remove(STONE_AXE);

        }

        int index = ThreadLocalRandom.current().nextInt(validMaterials.size());

        Material material = validMaterials.get(index);

        return material;

    }

    private static void intializeValidProceduralMaterials(){

        validMaterials = new ArrayList<>();

        for (Object object : ConfigValues.itemsProceduralSettingsConfig.getList(ItemsProceduralSettingsConfig.PROCEDURAL_ITEM_VALID_MATERIALS)) {

            try {

                Material parsedMaterial = getMaterial(object.toString());
                validMaterials.add(parsedMaterial);

            } catch (Exception e) {

                Bukkit.getLogger().info("Invalid material type detected: " + object.toString());

            }


        }

    }

}
