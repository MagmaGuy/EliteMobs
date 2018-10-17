package com.magmaguy.elitemobs.items.customenchantments;

import java.util.ArrayList;
import java.util.List;

public abstract class CustomEnchantment {

    /*
    Code here has been maintained as static because the key value is frequently accessed and stores a static value
    setKey() and setName() need to be rewritten in every class
     */

    public static void initialize(){

        setKey();
        setName();

    }

    /*
    Key is used to hide the enchantment in the lore to later ID the item, is independent from name to avoid issues with
    config changes invalidating old enchantment names
    It's also used as the value to write in for the configuration file
     */
    public static String key = setKey();
    public static List<String> keyList = new ArrayList<>();

    public static String setKey() {
        return "placeholder key";
    }

    public static String getKey() {
        return key;
    }

    public static void addKey(String key) {
        keyList.add(key);
    }

    /*
    Name is taken from config settings
     */
    public static String name = setName();

    public static String setName() {
        return "placeholder name";
    }

    public static String getName() {
        return name;
    }

    /*
    Get the enchantment string for assembling configurations
     */
    public static String assembleConfigString(int enchantmentLevel){
        return key + "," + enchantmentLevel;
    }

}
