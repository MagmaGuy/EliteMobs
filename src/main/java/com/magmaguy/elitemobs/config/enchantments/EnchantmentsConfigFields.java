package com.magmaguy.elitemobs.config.enchantments;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import lombok.Getter;
import org.bukkit.enchantments.Enchantment;

public class EnchantmentsConfigFields extends CustomConfigFields {
    @Getter
    private String name = "name";
    @Getter
    private int maxLevel = 5;
    @Getter
    private double value = 1;
    @Getter
    private Enchantment enchantment = Enchantment.DAMAGE_ALL;

    public EnchantmentsConfigFields(String filename,
                                    boolean isEnabled,
                                    String name,
                                    int maxLevel,
                                    double value) {
        super(filename, isEnabled);
        this.isEnabled = isEnabled;
        this.name = name;
        this.maxLevel = maxLevel;
        this.value = value;
    }

    @Override
    public void processConfigFields() {
        this.isEnabled = processBoolean("isEnabled", isEnabled, true, true);
        this.name = processString("name", name, "name", true);
        this.maxLevel = processInt("maxLevel", maxLevel, 1, true);
        String cleanName = this.filename.replace(".yml", "").toUpperCase();
        try {
            this.enchantment = Enchantment.getByName(cleanName);
        } catch (Exception ex) {
            this.enchantment = null;
        }
        this.value = processDouble("value", value, 1, true);
        processAdditionalFields();
    }

    public void processAdditionalFields(){}

}
