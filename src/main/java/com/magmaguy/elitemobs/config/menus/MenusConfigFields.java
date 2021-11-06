package com.magmaguy.elitemobs.config.menus;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.CustomConfigFieldsInterface;

public class MenusConfigFields extends CustomConfigFields implements CustomConfigFieldsInterface {

    public MenusConfigFields(String fileName, boolean isEnabled) {
        super(fileName, isEnabled);
    }

    @Override
    public void processConfigFields() {
        this.isEnabled = processBoolean("isEnabled", isEnabled, true, true);
        processAdditionalFields();
    }

    public void processAdditionalFields(){}

}
