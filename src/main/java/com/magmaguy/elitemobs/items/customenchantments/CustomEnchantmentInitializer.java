package com.magmaguy.elitemobs.items.customenchantments;

public class CustomEnchantmentInitializer {

//    Necessary for registering the config keys and item names, needs to run before items get constructed
    public static void initialize(){

        FlamethrowerEnchantment.initialize();
        HunterEnchantment.initialize();

    }

}
