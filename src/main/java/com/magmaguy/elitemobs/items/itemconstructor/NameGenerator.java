package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.config.StaticItemNamesConfig;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Material;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class NameGenerator {

    private NameGenerator() {
    }

    public static String generateName(String rawName) {
        return ChatColorConverter.convert(rawName);
    }

    public static String generateName(Material material) {
        List<String> names = getNameListForMaterial(material);
        if (names == null || names.isEmpty()) {
            Logger.warn("Found unexpected material type in procedurally generated loot. Can't generate item name.");
            Logger.warn("Material name: " + material);
            return "";
        }
        return names.get(ThreadLocalRandom.current().nextInt(names.size()));
    }

    private static List<String> getNameListForMaterial(Material material) {
        switch (material) {
            case DIAMOND_SWORD:
            case GOLDEN_SWORD:
            case IRON_SWORD:
            case STONE_SWORD:
            case WOODEN_SWORD:
                return StaticItemNamesConfig.getSwordNames();
            case BOW:
                return StaticItemNamesConfig.getBowNames();
            case DIAMOND_PICKAXE:
            case GOLDEN_PICKAXE:
            case IRON_PICKAXE:
            case STONE_PICKAXE:
            case WOODEN_PICKAXE:
                return StaticItemNamesConfig.getPickaxeNames();
            case DIAMOND_SHOVEL:
            case GOLDEN_SHOVEL:
            case IRON_SHOVEL:
            case STONE_SHOVEL:
            case WOODEN_SHOVEL:
                return StaticItemNamesConfig.getShovelNames();
            case DIAMOND_HOE:
            case GOLDEN_HOE:
            case IRON_HOE:
            case STONE_HOE:
            case WOODEN_HOE:
                return StaticItemNamesConfig.getHoeNames();
            case DIAMOND_AXE:
            case GOLDEN_AXE:
            case IRON_AXE:
            case STONE_AXE:
            case WOODEN_AXE:
                return StaticItemNamesConfig.getAxeNames();
            case CHAINMAIL_HELMET:
            case DIAMOND_HELMET:
            case GOLDEN_HELMET:
            case IRON_HELMET:
            case LEATHER_HELMET:
            case TURTLE_HELMET:
                return StaticItemNamesConfig.getHelmetNames();
            case CHAINMAIL_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
            case IRON_CHESTPLATE:
            case LEATHER_CHESTPLATE:
                return StaticItemNamesConfig.getChestplateNames();
            case CHAINMAIL_LEGGINGS:
            case DIAMOND_LEGGINGS:
            case GOLDEN_LEGGINGS:
            case IRON_LEGGINGS:
            case LEATHER_LEGGINGS:
                return StaticItemNamesConfig.getLeggingsNames();
            case CHAINMAIL_BOOTS:
            case DIAMOND_BOOTS:
            case GOLDEN_BOOTS:
            case IRON_BOOTS:
            case LEATHER_BOOTS:
                return StaticItemNamesConfig.getBootsNames();
            case SHEARS:
                return StaticItemNamesConfig.getShearsNames();
            case FISHING_ROD:
                return StaticItemNamesConfig.getFishingRodNames();
            case SHIELD:
                return StaticItemNamesConfig.getShieldNames();
            case TRIDENT:
                return StaticItemNamesConfig.getTridentNames();
            case CROSSBOW:
                return StaticItemNamesConfig.getCrossbowNames();
            case MACE:
                return StaticItemNamesConfig.getMaceNames();
            case DIAMOND_SPEAR:
            case IRON_SPEAR:
            case GOLDEN_SPEAR:
            case STONE_SPEAR:
            case WOODEN_SPEAR:
            case COPPER_SPEAR:
            case NETHERITE_SPEAR:
                return StaticItemNamesConfig.getSpearNames();
            default:
                return null;
        }
    }

}
