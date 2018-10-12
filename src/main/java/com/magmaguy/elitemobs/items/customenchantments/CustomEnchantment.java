package com.magmaguy.elitemobs.items.customenchantments;

public abstract class CustomEnchantment {

    /*
    Code here has been maintained as static because the key value is frequently accessed and stores a static value
    setKey() and setName() need to be rewritten in every class
     */

    /*
    Key is used to hide the enchantment in the lore to later ID the item, is independent from name to avoid issues with
    config changes invalidating old enchantment names
     */
    public static String key = setKey();

    public static String setKey() {
        return "placeholder key";
    }

    public static String getKey() {
        return key;
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
