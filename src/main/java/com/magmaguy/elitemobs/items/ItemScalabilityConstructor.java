package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructor;
import com.magmaguy.elitemobs.items.itemconstructor.ScallableItemObject;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ItemScalabilityConstructor {

    /*
    Keep in mind that all items in this class are passed through the ItemConstructor class upon being generated.
    As such, all data required to generate an item using the constructors there is stored here.
    This class merely tweaks the enchantments.
     */

    /*
    The following HashMap contains all dynamically scalable items.
    These items have a base ItemStack only used to pick a random item
    The List in the value has the following structure:
    [0] - String rawname
    [1] - Material material
    [2] - HashMap<Enchantment, Integer> enchantments
    [3] - HashMap<String, Integer> customEnchantments
    [4] - List<String> potionEffects
    The list is used as parameters to generate a new item. You may as well ignore the ItemStack.
     */
    public static List<ScallableItemObject> dynamicallyScalableItems = new ArrayList<>();

    /*
    The following HashMap contains all limited scalable items.
    The ItemStack is only used to pick a random item.
    The List in the value has the same fields as the previous one
    From a user perspective, the ItemStack represents the best version of that item that the plugin will generate.
    The Integer stores data pertaining to the highest item tier that this item can have.
    This will only accept
     */
    public static HashMap<Integer, List<ScallableItemObject>> limitedScalableItems = new HashMap<>();

    /*
    Fully dynamic scalable items can be of any item rank all the way up to the maximum valid item tier.
    As such, there is no sense in trying to sort this by tiers.
     */
    public static ItemStack constructDynamicItem(int itemTier) {

        ScallableItemObject scalableItemObject = dynamicallyScalableItems.get(ThreadLocalRandom.current().nextInt(dynamicallyScalableItems.size()) - 1);

        HashMap<Enchantment, Integer> newEnchantmentList = updateDynamicEnchantments(scalableItemObject.enchantments, itemTier);

        return ItemConstructor.constructItem(
                scalableItemObject.rawName,
                scalableItemObject.material,
                scalableItemObject.enchantments,
                scalableItemObject.customEnchantments,
                scalableItemObject.potionEffects,
                scalableItemObject.lore
        );

    }

    private static HashMap<Enchantment, Integer> updateDynamicEnchantments(HashMap<Enchantment, Integer> enchantmentsList,
                                                                           int itemTier) {

        HashMap<Enchantment, Integer> newEnchantmentList = (HashMap<Enchantment, Integer>) enchantmentsList.clone();

        /*
        Small limitation of this system, it doesn't take into account the material tier and just defaults to assume it's
        diamond (hence the -1 enchantment).
        It's such a small thing that I don't feel it's worth the hassle of adjusting it.
         */
        for (Enchantment enchantment : enchantmentsList.keySet())
            if (enchantment.equals(Enchantment.DAMAGE_ALL) ||
                    enchantment.equals(Enchantment.ARROW_DAMAGE) ||
                    enchantment.equals(Enchantment.PROTECTION_ENVIRONMENTAL))
                newEnchantmentList.put(enchantment, itemTier - 1);


        return newEnchantmentList;

    }

    /*
    Limited scalable items have the item in the config as the best possible item that the plugin will generate for that
    entry, and every other entry is just a nerfed version of that item. Basically an easy way to limit an item in a
    predictable way.
     */
    public static ItemStack constructLimiteItem(int ItemTier) {

    }



}
