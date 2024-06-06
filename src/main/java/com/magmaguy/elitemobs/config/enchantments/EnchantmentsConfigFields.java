package com.magmaguy.elitemobs.config.enchantments;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import lombok.Getter;
import org.bukkit.enchantments.Enchantment;

import java.util.Locale;

public class EnchantmentsConfigFields extends CustomConfigFields {
    @Getter
    private String name = "name";
    @Getter
    private int maxLevel = 5;
    @Getter
    private double value = 1;
    @Getter
    private Enchantment enchantment = null;
    @Getter
    private boolean isEnabledForProcedurallyGeneratedItems = false;
    @Getter
    private int maxEnchantmentLevel;

    public EnchantmentsConfigFields(String filename,
                                    boolean isEnabled,
                                    String name,
                                    int maxLevel,
                                    double value,
                                    boolean isEnabledForProcedurallyGeneratedItems,
                                    int maxEnchantmentLevel){
        super(filename, isEnabled);
        this.isEnabled = isEnabled;
        this.name = name;
        this.maxLevel = maxLevel;
        this.value = value;
        this.isEnabledForProcedurallyGeneratedItems = isEnabledForProcedurallyGeneratedItems;
        this.maxEnchantmentLevel = maxEnchantmentLevel;
    }

    @Override
    public void processConfigFields() {
        this.isEnabled = processBoolean("isEnabled", isEnabled, true, true);
        this.name = translatable(filename, "name", processString("name", name, "name", true));
        this.maxLevel = processInt("maxLevelV2", maxLevel, 1, true);
        String cleanName = this.filename.replace(".yml", "").toUpperCase(Locale.ROOT);
        try {
            this.enchantment = Enchantment.getByName(cleanName);
        } catch (Exception ex) {
            this.enchantment = null;
        }
        this.value = processDouble("value", value, 1, true);
        this.isEnabledForProcedurallyGeneratedItems = processBoolean("isEnabledForProcedurallyGeneratedItems", isEnabledForProcedurallyGeneratedItems, false, true);
        this.maxEnchantmentLevel = processInt("maxEnchantmentLevel", maxEnchantmentLevel, 1, false);
        processAdditionalFields();
    }

    public void processAdditionalFields() {
    }

}
