package com.magmaguy.elitemobs.config.customitems;

import com.magmaguy.magmacore.config.CustomConfig;

import java.util.HashMap;

public class CustomItemsConfig extends CustomConfig {

    private static HashMap<String, CustomItemsConfigFields> customItems = new HashMap<>();

    public CustomItemsConfig() {
        super("customitems", "com.magmaguy.elitemobs.config.customitems.premade", CustomItemsConfigFields.class);
        customItems = new HashMap<>();
        for (String key : super.getCustomConfigFieldsHashMap().keySet())
            customItems.put(key, (CustomItemsConfigFields) super.getCustomConfigFieldsHashMap().get(key));
    }

    public static HashMap<String, CustomItemsConfigFields> getCustomItems() {
        return customItems;
    }

}
