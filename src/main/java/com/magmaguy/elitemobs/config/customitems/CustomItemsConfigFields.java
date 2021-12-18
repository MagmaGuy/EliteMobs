package com.magmaguy.elitemobs.config.customitems;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.CustomConfigFieldsInterface;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class CustomItemsConfigFields extends CustomConfigFields implements CustomConfigFieldsInterface {

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
    //todo: implement
    private List<String> nbtTags = new ArrayList<>();

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
        this.material = processEnum("material", material, Material.WOODEN_SWORD, true);
        this.name = processString("name", name, "Default name", true);
        this.lore = processStringList("lore", lore, new ArrayList<>(), true);
        this.enchantments = processStringList("enchantments", enchantments, new ArrayList<>(), false);
        this.potionEffects = processStringList("potionEffects", potionEffects, new ArrayList<>(), false);
        this.dropWeight = processString("dropWeight", dropWeight, "dynamic", false);
        this.scalability = processEnum("scalability", scalability, CustomItem.Scalability.SCALABLE, false);
        this.itemType = processEnum("itemType", itemType, CustomItem.ItemType.CUSTOM, false);
        this.customModelID = processInt("customModelID", customModelID, -1, false);
    }
}
