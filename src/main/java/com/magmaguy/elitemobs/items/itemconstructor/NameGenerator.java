package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.ProceduralItemGenerationSettingsConfig;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class NameGenerator {
    private static final List<String> nouns = ProceduralItemGenerationSettingsConfig.getNouns();
    private static final List<String> adjectives = ProceduralItemGenerationSettingsConfig.getAdjectives();
    private static final List<String> verbs = ProceduralItemGenerationSettingsConfig.getVerbs();
    private static final List<String> verbers = ProceduralItemGenerationSettingsConfig.getVerbers();

    private NameGenerator() {
    }

    public static String generateName(String rawName) {
        return ChatColorConverter.convert(rawName);
    }

    public static String generateName(Material material) {

        int nounConstructorSelector = ThreadLocalRandom.current().nextInt(ProceduralItemGenerationSettingsConfig.getNameFormat().size());

        return (ProceduralItemGenerationSettingsConfig.getNameFormat().get(nounConstructorSelector))
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
                return ProceduralItemGenerationSettingsConfig.getSwordName();
            case BOW:
                return ProceduralItemGenerationSettingsConfig.getBowName();
            case DIAMOND_PICKAXE:
            case GOLDEN_PICKAXE:
            case IRON_PICKAXE:
            case STONE_PICKAXE:
            case WOODEN_PICKAXE:
                return ProceduralItemGenerationSettingsConfig.getPickaxeName();
            case DIAMOND_SHOVEL:
            case GOLDEN_SHOVEL:
            case IRON_SHOVEL:
            case STONE_SHOVEL:
            case WOODEN_SHOVEL:
                return ProceduralItemGenerationSettingsConfig.getSpadeName();
            case DIAMOND_HOE:
            case GOLDEN_HOE:
            case IRON_HOE:
            case STONE_HOE:
            case WOODEN_HOE:
                return ProceduralItemGenerationSettingsConfig.getHoeName();
            case DIAMOND_AXE:
            case GOLDEN_AXE:
            case IRON_AXE:
            case STONE_AXE:
            case WOODEN_AXE:
                return ProceduralItemGenerationSettingsConfig.getAxeName();
            case CHAINMAIL_HELMET:
            case DIAMOND_HELMET:
            case GOLDEN_HELMET:
            case IRON_HELMET:
            case LEATHER_HELMET:
            case TURTLE_HELMET:
                return ProceduralItemGenerationSettingsConfig.getHelmetName();
            case CHAINMAIL_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
            case IRON_CHESTPLATE:
            case LEATHER_CHESTPLATE:
                return ProceduralItemGenerationSettingsConfig.getChestplateName();
            case CHAINMAIL_LEGGINGS:
            case DIAMOND_LEGGINGS:
            case GOLDEN_LEGGINGS:
            case IRON_LEGGINGS:
            case LEATHER_LEGGINGS:
                return ProceduralItemGenerationSettingsConfig.getLeggingsName();
            case CHAINMAIL_BOOTS:
            case DIAMOND_BOOTS:
            case GOLDEN_BOOTS:
            case IRON_BOOTS:
            case LEATHER_BOOTS:
                return ProceduralItemGenerationSettingsConfig.getBootsName();
            case SHEARS:
                return ProceduralItemGenerationSettingsConfig.getShearsName();
            case FISHING_ROD:
                return ProceduralItemGenerationSettingsConfig.getFishingRodName();
            case SHIELD:
                return ProceduralItemGenerationSettingsConfig.getShieldName();
            case TRIDENT:
                return ProceduralItemGenerationSettingsConfig.getTridentName();
            case CROSSBOW:
                return ProceduralItemGenerationSettingsConfig.getCrossbowName();
        }

        if (ProceduralItemGenerationSettingsConfig.getFileConfiguration().getString("materialNames." + material.toString().toLowerCase(Locale.ROOT)) != null)
            return ProceduralItemGenerationSettingsConfig.getFileConfiguration().getString("materialNames." + material.toString().toLowerCase(Locale.ROOT));

        Bukkit.getLogger().warning("[EliteMobs] Found unexpected material type in procedurally generated loot. Can't generate item type name.");
        Bukkit.getLogger().warning("[EliteMobs] Material name: " + material);
        new WarningMessage("If you're trying to set a non-default item type, you need to add the name format like this under materialNames: " + material.toString().toLowerCase(Locale.ROOT) + ": Name");
        return "";

    }

}
