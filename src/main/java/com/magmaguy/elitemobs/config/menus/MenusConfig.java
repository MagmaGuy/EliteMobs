package com.magmaguy.elitemobs.config.menus;

import com.magmaguy.elitemobs.config.CustomConfig;

import java.util.HashMap;

public class MenusConfig extends CustomConfig {

    private static HashMap<String, MenusConfigFields> menusConfigFields = new HashMap<>();

    public MenusConfig(){
        super("menus", "com.magmaguy.elitemobs.config.menus.premade", MenusConfigFields.class);
        menusConfigFields = new HashMap<>();
        for (String key : super.getCustomConfigFieldsHashMap().keySet())
            menusConfigFields.put(key, (MenusConfigFields) super.getCustomConfigFieldsHashMap().get(key));
    }

    public static HashMap<String, MenusConfigFields> getCustomQuests(){
        return menusConfigFields;
    }

}
