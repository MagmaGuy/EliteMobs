package com.magmaguy.elitemobs.config.customitems;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.LegacyValueConverter;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.items.ItemConsumables;
import com.magmaguy.elitemobs.skills.SkillType;
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
    private com.magmaguy.elitemobs.items.ClassLootFamily classLootFamily;
    @Getter
    @Setter
    private String customModelID = null;
    @Getter
    @Setter
    private String equipmentModelID = null;
    @Getter
    @Setter
    private String scriptedItem = null;
    @Getter
    @Setter
    private SkillType weaponType = null;
    @Getter
    @Setter
    private String fmmItemModel = null;
    @Getter
    @Setter
    private boolean proceduralEnchantments = false;
    @Getter
    @Setter
    private String permission = "";
    @Getter
    @Setter
    private int level = 0;
    @Getter
    @Setter
    private boolean soulbound = true;
    @Getter
    @Setter
    private boolean showSource = true;
    @Getter
    @Setter
    private ItemConsumables.Definition consumable;

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
        Object authoredMaterial = fileConfiguration.get("material");
        Object authoredName = fileConfiguration.get("name");
        Object authoredLore = fileConfiguration.get("lore");
        this.isEnabled = processBoolean("isEnabled", isEnabled, true, true);
        // Materials for future versions (e.g. spears) may not exist yet â€” skip silently
        if (configHas("material")) {
            String rawMaterial = fileConfiguration.getString("material");
            if (rawMaterial != null && rawMaterial.toUpperCase(java.util.Locale.ROOT).endsWith("_SPEAR")
                    && com.magmaguy.elitemobs.versionnotifier.VersionChecker.serverVersionOlderThan(21, 11)) {
                this.material = Material.WOODEN_SWORD;
                this.isEnabled = false;
            } else {
                this.material = processEnum("material", material, Material.WOODEN_SWORD, Material.class, true);
            }
        } else {
            this.material = processEnum("material", material, Material.WOODEN_SWORD, Material.class, true);
        }
        this.name = translatable(filename, "name", processString("name", name, "Default name", true));
        this.lore = translatable(filename, "lore", processStringList("lore", lore, new ArrayList<>(), true));
        this.enchantments = processStringList("enchantments", enchantments, null, false);
        this.potionEffects = processStringList("potionEffects", potionEffects, null, false);
        this.dropWeight = processString("dropWeight", dropWeight, "dynamic", false);
        this.scalability = processEnum("scalability", scalability, CustomItem.Scalability.SCALABLE, CustomItem.Scalability.class, false);
        this.itemType = processEnum("itemType", itemType, CustomItem.ItemType.CUSTOM, CustomItem.ItemType.class, false);
        this.customModelID = processString("customModelV2", customModelID, null, false);
        this.equipmentModelID = processString("equipmentModelID", equipmentModelID, null, false);
        this.scriptedItem = processString("scriptedItem", scriptedItem, null, false);
        this.weaponType = processEnum("weaponType", weaponType, null, SkillType.class, false);
        if (weaponType != null && weaponType != SkillType.STAVES && weaponType != SkillType.WANDS) {
            Logger.warn("Item " + filename + ": weaponType accepts STAVES or WANDS; other weapons use their material.");
            weaponType = null;
        }
        this.fmmItemModel = processString("fmmItemModel", fmmItemModel, null, false);
        this.proceduralEnchantments = processBoolean("proceduralEnchantments", proceduralEnchantments, false, false);
        this.permission = processString("permission", permission, "", false);
        this.level = processInt("level", level, 0, false);
        this.soulbound = processBoolean("soulbound", soulbound, true, false);
        this.showSource = processBoolean("showSource", showSource, showSource, false);
        classLootFamily = null;
        if (itemType == CustomItem.ItemType.CLASS_LOOT) {
            try {
                if (!(authoredMaterial instanceof String) || !(authoredName instanceof String text) || text.isBlank()
                        || !(authoredLore instanceof List<?> lines)
                        || !lines.stream().allMatch(String.class::isInstance))
                    throw new IllegalArgumentException("material, name and lore must be valid item fields (lore: [] is allowed)");
                if (Material.getMaterial(fileConfiguration.getString("material").toUpperCase(java.util.Locale.ROOT)) == null)
                    throw new IllegalArgumentException("material is unavailable on this server");
                classLootFamily = com.magmaguy.elitemobs.items.ClassLootFamily.resolve(material, weaponType,
                        fileConfiguration.getString("classLootFamily"));
            } catch (IllegalArgumentException invalid) {
                this.isEnabled = false;
                Logger.warn("Invalid CLASS_LOOT item " + filename + ": " + invalid.getMessage() + "; item disabled.");
            }
        }
        processConsumable();
        updatePostProcessor();
    }

    private void processConsumable() {
        try {
            if (configHas("consumable") && !fileConfiguration.isConfigurationSection("consumable"))
                throw new IllegalArgumentException("consumable must contain type and optional repair tier");
            var section = fileConfiguration.getConfigurationSection("consumable");
            if (section != null && !java.util.Set.of("type", "tier").containsAll(section.getKeys(false)))
                throw new IllegalArgumentException("Unknown consumable field");
            String type = processString("consumable.type", consumable == null ? null
                    : consumable.type().name().toLowerCase(java.util.Locale.ROOT), null, false);
            if (type == null) {
                if (section != null) throw new IllegalArgumentException("consumable.type is required");
                consumable = null;
                return;
            }
            if (material == null || material.isAir() || !material.isItem())
                throw new IllegalArgumentException("Consumables require a valid item material");
            var parsed = ItemConsumables.Type.valueOf(type.toUpperCase(java.util.Locale.ROOT));
            if (configHas("consumable.tier") && !(fileConfiguration.get("consumable.tier") instanceof Integer))
                throw new IllegalArgumentException("consumable.tier must be an integer");
            if (parsed != ItemConsumables.Type.REPAIR_SCRAP && configHas("consumable.tier"))
                throw new IllegalArgumentException("Only repair_scrap accepts consumable.tier");
            int tier = parsed == ItemConsumables.Type.REPAIR_SCRAP
                    ? processInt("consumable.tier", consumable == null ? 0 : consumable.tier(), 0, false) : 0;
            consumable = new ItemConsumables.Definition(parsed, tier);
        } catch (IllegalArgumentException invalid) {
            consumable = null;
            isEnabled = false;
            Logger.warn("Item " + filename + " disabled: " + invalid.getMessage());
        }
    }

    private void updatePostProcessor() {
        List<String> newEnchantments = new ArrayList<>();
        for (String enchantment : enchantments) {
            if (!enchantment.contains(",")) {
                if (enchantment.contains(":") && !enchantment.startsWith("minecraft:")) isEnabled = false;
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
