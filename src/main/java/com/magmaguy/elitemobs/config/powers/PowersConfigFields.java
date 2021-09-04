package com.magmaguy.elitemobs.config.powers;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

public class PowersConfigFields extends CustomConfigFields {

    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String effect;
    @Getter
    @Setter
    private int powerCooldown = 0;
    @Getter
    @Setter
    private int globalCooldown = 0;

    public PowersConfigFields(String fileName,
                              boolean isEnabled,
                              String name,
                              String material) {
        super(fileName, isEnabled);
        this.name = name;
        this.effect = material;
    }

    public PowersConfigFields(String fileName,
                              boolean isEnabled,
                              String name,
                              String material,
                              int powerCooldown,
                              int globalCooldown) {
        super(fileName, isEnabled);
        this.name = name;
        this.effect = material;
        this.powerCooldown = powerCooldown;
        this.globalCooldown = globalCooldown;
    }

    public PowersConfigFields(String fileName,
                              boolean isEnabled) {
        super(fileName, isEnabled);
    }

    @Override
    public void processConfigFields() {
        this.isEnabled = processBoolean("isEnabled", isEnabled, true, true);
        this.name = processString("name", name, null, true);
        this.effect = processString("effect", effect, null, false);
        this.powerCooldown = processInt("powerCooldown", powerCooldown, 0, false);
        this.globalCooldown = processInt("globalCooldown", globalCooldown, 0, false);
        processAdditionalFields();
    }

    public void processAdditionalFields(){}

}
