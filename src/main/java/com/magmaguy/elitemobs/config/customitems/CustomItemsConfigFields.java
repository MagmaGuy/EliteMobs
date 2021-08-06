package com.magmaguy.elitemobs.config.customitems;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.CustomConfigFieldsInterface;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class CustomItemsConfigFields extends CustomConfigFields implements CustomConfigFieldsInterface {

    private Material material = Material.WOODEN_SWORD;
    private String name = "Defaults name";
    private List<String> lore = new ArrayList<>();
    private List<String> enchantments = new ArrayList<>();
    private List<String> potionEffects = new ArrayList<>();
    private String dropWeight = "dynamic";
    private CustomItem.Scalability scalability = CustomItem.Scalability.scalable;
    private CustomItem.ItemType itemType = CustomItem.ItemType.custom;
    private Integer customModelID = -1;

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

    @Override
    public void generateConfigDefaults() {

        fileConfiguration.addDefault("isEnabled", isEnabled);
        fileConfiguration.addDefault("material", material.toString());
        fileConfiguration.addDefault("name", name);
        fileConfiguration.addDefault("lore", lore);
        fileConfiguration.addDefault("enchantments", enchantments);
        fileConfiguration.addDefault("potionEffects", potionEffects);
        fileConfiguration.addDefault("dropWeight", dropWeight);
        fileConfiguration.addDefault("scalability", scalability.toString());
        fileConfiguration.addDefault("itemType", itemType.toString());
        if (customModelID > 0)
            fileConfiguration.addDefault("customModelID", customModelID);
    }

    /**
     * Generates config defaults to be used by CustomBossesConfig
     */
    @Override
    public void processConfigFields() {
        this.isEnabled = processBoolean("isEnabled", isEnabled);
        this.material = processEnum("material", material);
        this.name = processString("name", name);
        this.lore = processStringList("lore", lore);
        this.enchantments = processStringList("enchantments", enchantments);
        this.potionEffects = processStringList("potionEffects", potionEffects);
        this.dropWeight = processString("dropWeight", dropWeight);
        this.scalability = processEnum("scalability", scalability);
        this.itemType = processEnum("itemType", itemType);
        this.customModelID = processInt("customModelID", customModelID);
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public Material getMaterial() {
        return material;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public List<String> getEnchantments() {
        return enchantments;
    }

    public void setEnchantments(List<String> enchantments) {
        this.enchantments = enchantments;
    }

    public List<String> getPotionEffects() {
        return potionEffects;
    }

    public void setPotionEffects(List<String> potionEffects) {
        this.potionEffects = potionEffects;
    }

    public String getDropWeight() {
        return dropWeight;
    }

    public void setDropWeight(String dropWeight) {
        this.dropWeight = dropWeight;
    }

    public CustomItem.Scalability getScalability() {
        return scalability;
    }

    public void setScalability(CustomItem.Scalability scalability) {
        this.scalability = scalability;
    }

    public CustomItem.ItemType getItemType() {
        return itemType;
    }

    public void setItemType(CustomItem.ItemType itemType) {
        this.itemType = itemType;
    }

    public Integer getCustomModelID() {
        return this.customModelID;
    }

    public void setCustomModelID(Integer customModelID) {
        this.customModelID = customModelID;
    }
}
