package com.magmaguy.elitemobs.items.customenchantments;

public abstract class CustomEnchantment {

    public String name = setName();

    public abstract String setName();

    public String getName(){
        return name;
    }

}
