package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.ItemsProceduralSettingsConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class NameGenerator {

    public static String generateName(String rawName) {

        return ChatColorConverter.convert(rawName);

    }

    private static ArrayList<String> nouns = (ArrayList<String>) ConfigValues.itemsProceduralSettingsConfig.getList("Valid nouns");
    private static ArrayList<String> adjectives = (ArrayList<String>) ConfigValues.itemsProceduralSettingsConfig.getList("Valid adjectives");
    private static ArrayList<String> verbs = (ArrayList<String>) ConfigValues.itemsProceduralSettingsConfig.getList("Valid verbs");
    private static ArrayList<String> verbers = (ArrayList<String>) ConfigValues.itemsProceduralSettingsConfig.getList("Valid verb-er (noun)");

    public static String generateName(Material material) {

        int nounConstructorSelector = ThreadLocalRandom.current().nextInt(ConfigValues.itemsProceduralSettingsConfig.getList(ItemsProceduralSettingsConfig.ITEM_NAME_FORMAT).size());

        return ((String) ConfigValues.itemsProceduralSettingsConfig.getList(ItemsProceduralSettingsConfig.ITEM_NAME_FORMAT)
                .get(nounConstructorSelector))
                .replace("$noun", nouns.get(ThreadLocalRandom.current().nextInt(nouns.size())))
                .replace("$verb-er", verbers.get(ThreadLocalRandom.current().nextInt(verbers.size())))
                .replace("$verb", verbs.get(ThreadLocalRandom.current().nextInt(verbs.size())))
                .replace("$itemType", materialStringParser(material))
                .replace("$adjective", adjectives.get(ThreadLocalRandom.current().nextInt(adjectives.size())));

    }

    private static String materialStringParser(Material material) {

        switch (material) {
            case DIAMOND_SWORD:
            case GOLDEN_SWORD:
            case IRON_SWORD:
            case STONE_SWORD:
            case WOODEN_SWORD:
                return ConfigValues.itemsProceduralSettingsConfig.getString(ItemsProceduralSettingsConfig.SWORD_MATERIAL_NAME);
            case BOW:
                return ConfigValues.itemsProceduralSettingsConfig.getString(ItemsProceduralSettingsConfig.BOW_MATERIAL_NAME);
            case DIAMOND_PICKAXE:
            case GOLDEN_PICKAXE:
            case IRON_PICKAXE:
            case STONE_PICKAXE:
            case WOODEN_PICKAXE:
                return ConfigValues.itemsProceduralSettingsConfig.getString(ItemsProceduralSettingsConfig.PICKAXE_MATERIAL_NAME);
            case DIAMOND_SHOVEL:
            case GOLDEN_SHOVEL:
            case IRON_SHOVEL:
            case STONE_SHOVEL:
            case WOODEN_SHOVEL:
                return ConfigValues.itemsProceduralSettingsConfig.getString(ItemsProceduralSettingsConfig.SPADE_MATERIAL_NAME);
            case DIAMOND_HOE:
            case GOLDEN_HOE:
            case IRON_HOE:
            case STONE_HOE:
            case WOODEN_HOE:
                return ConfigValues.itemsProceduralSettingsConfig.getString(ItemsProceduralSettingsConfig.HOE_MATERIAL_NAME);
            case DIAMOND_AXE:
            case GOLDEN_AXE:
            case IRON_AXE:
            case STONE_AXE:
            case WOODEN_AXE:
                return ConfigValues.itemsProceduralSettingsConfig.getString(ItemsProceduralSettingsConfig.AXE_MATERIAL_NAME);
            case CHAINMAIL_HELMET:
            case DIAMOND_HELMET:
            case GOLDEN_HELMET:
            case IRON_HELMET:
            case LEATHER_HELMET:
                return ConfigValues.itemsProceduralSettingsConfig.getString(ItemsProceduralSettingsConfig.HELMET_MATERIAL_NAME);
            case CHAINMAIL_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
            case IRON_CHESTPLATE:
            case LEATHER_CHESTPLATE:
                return ConfigValues.itemsProceduralSettingsConfig.getString(ItemsProceduralSettingsConfig.CHESTPLATE_MATERIAL_NAME);
            case CHAINMAIL_LEGGINGS:
            case DIAMOND_LEGGINGS:
            case GOLDEN_LEGGINGS:
            case IRON_LEGGINGS:
            case LEATHER_LEGGINGS:
                return ConfigValues.itemsProceduralSettingsConfig.getString(ItemsProceduralSettingsConfig.LEGGINGS_MATERIAL_NAME);
            case CHAINMAIL_BOOTS:
            case DIAMOND_BOOTS:
            case GOLDEN_BOOTS:
            case IRON_BOOTS:
            case LEATHER_BOOTS:
                return ConfigValues.itemsProceduralSettingsConfig.getString(ItemsProceduralSettingsConfig.BOOTS_MATERIAL_NAME);
            case SHEARS:
                return ConfigValues.itemsProceduralSettingsConfig.getString(ItemsProceduralSettingsConfig.SHEARS_MATERIAL_NAME);
            case FISHING_ROD:
                return ConfigValues.itemsProceduralSettingsConfig.getString(ItemsProceduralSettingsConfig.FISHING_ROD_MATERIAL_NAME);
            case SHIELD:
                return ConfigValues.itemsProceduralSettingsConfig.getString(ItemsProceduralSettingsConfig.SHIELD_MATERIAL_NAME);
        }


        Bukkit.getLogger().warning("[EliteMobs] Found unexpected material type in procedurally generated loot. Can't generate item type name.");
        Bukkit.getLogger().warning("[EliteMobs] Material name: " + material.toString());
        return "";

    }

}
