package com.magmaguy.elitemobs.config.customitems;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.LegacyValueConverter;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class CustomItemsConfigFields extends CustomConfigFields {

    @Getter
    @Setter
    private Material material = Material.WOODEN_SWORD;
    @Getter
    @Setter
    private String name = "Default name";
    @Getter
    @Setter
    private List<String> lore = new ArrayList<>();
    @Getter
    @Setter
    private List<String> enchantments = new ArrayList<>();
    @Getter
    @Setter
    private List<String> potionEffects = new ArrayList<>();
    @Getter
    @Setter
    private String dropWeight = "dynamic";
    @Getter
    @Setter
    private CustomItem.Scalability scalability = CustomItem.Scalability.SCALABLE;
    @Getter
    @Setter
    private CustomItem.ItemType itemType = CustomItem.ItemType.CUSTOM;
    @Getter
    @Setter
    private Integer customModelID = -1;
    @Getter
    @Setter
    private String permission = "";
    @Getter
    @Setter
    private int level = 0;
    @Getter
    @Setter
    private boolean soulbound = true;

    public CustomItemsConfigFields(String fileName,
                                   boolean isEnabled,
                                   Material material,
                                   String name,
                                   List<String> lore) {
        super(fileName, isEnabled);
        this.material = material;
        this.name = name;
        this.lore = lore;
    }

    public CustomItemsConfigFields(String fileName,
                                   boolean isEnabled) {
        super(fileName, isEnabled);
    }

    /**
     * Generates config defaults to be used by CustomBossesConfig
     */
    @Override
    public void processConfigFields() {
        this.isEnabled = processBoolean("isEnabled", isEnabled, true, true);
        this.material = processEnum("material", material, Material.WOODEN_SWORD, Material.class, true);
        this.name = translatable(filename, "name", processString("name", name, "Default name", true));
        this.lore = translatable(filename, "lore", processStringList("lore", lore, new ArrayList<>(), true));
        this.enchantments = processStringList("enchantments", enchantments, null, false);
        this.potionEffects = processStringList("potionEffects", potionEffects, null, false);
        this.dropWeight = processString("dropWeight", dropWeight, "dynamic", false);
        this.scalability = processEnum("scalability", scalability, CustomItem.Scalability.SCALABLE, CustomItem.Scalability.class, false);
        this.itemType = processEnum("itemType", itemType, CustomItem.ItemType.CUSTOM, CustomItem.ItemType.class, false);
        this.customModelID = processInt("customModelID", customModelID, -1, false);
        this.permission = processString("permission", permission, "", false);
        this.level = processInt("level", level, 0, false);
        this.soulbound = processBoolean("soulbound", soulbound, true, false);
        updatePostProcessor();
    }

    private void updatePostProcessor() {
        List<String> newEnchantments = new ArrayList<>();
        for (String enchantment : enchantments) {
            if (!enchantment.contains(",")) {
                Logger.warn("Invalid format for enchantment in file " + filename + " for enchantment " + enchantment + " : missing ',' for valid level after the enchantment name");
                continue;
            }
            String[] split = enchantment.split(",");
            String result = LegacyValueConverter.parseEnchantment(split[0]);
            if (result.equals(split[0])) newEnchantments.add(enchantment);
            else newEnchantments.add(result + "," + split[1]);
        }
        enchantments = newEnchantments;

        List<String> newPotionEffects = new ArrayList<>();
        for (String potionEffect : potionEffects) {
            if (!potionEffect.contains(",")) {
                Logger.warn("Invalid format for potion effect in file " + filename + " for potion effect " + potionEffect + " : missing ',' for valid level after the potion effect name");
                continue;
            }
            String[] split = potionEffect.split(",");
            String result = LegacyValueConverter.parsePotionEffect(split[0]);
            if (result.equals(split[0])) newPotionEffects.add(potionEffect);
            else {
                StringBuilder newString = new StringBuilder();
                newString.append(result + ",");

                for (int i = 1; i < split.length; i++) {
                    newString.append(split[i]);
                    if (i != split.length - 1)
                        newString.append(",");
                }
                newPotionEffects.add(newString.toString());
            }
        }
        potionEffects = newPotionEffects;

        potionEffects.forEach(potionEffect -> {
            if (potionEffect.contains("INSTANT_DAMAGE"))
                Logger.warn("Item " + filename + " contains HARM/INSTANT_DAMAGE potion effect, which heals undead mobs (Minecraft vanilla mechanic) and often times confuses players and admins. It is recommended you switch this potion effect with something else, like STRENGTH if you want more damage.");
        });
    }
}
