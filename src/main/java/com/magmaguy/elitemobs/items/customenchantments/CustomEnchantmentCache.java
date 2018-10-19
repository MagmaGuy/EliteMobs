package com.magmaguy.elitemobs.items.customenchantments;

public class CustomEnchantmentCache {

    /*
    In order to implement use inheritance the custom enchantments can't be static
    However, accessing the data stored in them benefits from static access, as ultimately the custom enchantments just
    store a bunch on unchanging values
    Hence, static access to these basically final values is provided in this class.
    It also prevents issues related with having to re-initialize the values every time an instance of the method is invoked.
    There's probably a better way of doing this, but I don't know it. If you do, let me know.
     */

    public static FlamethrowerEnchantment flamethrowerEnchantment = new FlamethrowerEnchantment();
    public static HunterEnchantment hunterEnchantment = new HunterEnchantment();

//    Necessary for registering the config keys and item names, needs to run before items get constructed
    public static void initialize(){

        flamethrowerEnchantment.initialize();
        hunterEnchantment.initialize();

    }

}
