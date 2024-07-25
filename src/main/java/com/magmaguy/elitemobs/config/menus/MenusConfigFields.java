package com.magmaguy.elitemobs.config.menus;

import com.magmaguy.elitemobs.config.CustomConfigFields;

public class MenusConfigFields extends CustomConfigFields {

    public MenusConfigFields(String fileName, boolean isEnabled) {
        super(fileName, isEnabled);
    }

    @Override
    public void processConfigFields() {
        this.isEnabled = processBoolean("isEnabled", isEnabled, true, true);
        processAdditionalFields();
    }

    public void processAdditionalFields() {
    }

}
