package com.magmaguy.elitemobs.items.customenchantments;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;
import com.magmaguy.elitemobs.items.ItemTagger;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;

public abstract class CustomEnchantment {

    @Getter
    private static final HashMap<String, CustomEnchantment> customEnchantmentMap = new HashMap();
    public final String key;
    private final boolean dynamic;
    private final EnchantmentsConfigFields enchantmentsConfigFields;
    private final Enchantment originalEnchantment = null;

    public CustomEnchantment(String key, boolean dynamic) {
        this.key = key;
        this.dynamic = dynamic;
        this.enchantmentsConfigFields = EnchantmentsConfig.getEnchantment(key + ".yml");
        customEnchantmentMap.put(enchantmentsConfigFields.getFilename(), this);
    }

    public static void initializeCustomEnchantments() {
        new FlamethrowerEnchantment();
        new PlasmaBootsEnchantment();
        new HunterEnchantment();
        new CriticalStrikesEnchantment();
        new DrillingEnchantment();
        new IceBreakerEnchantment();
        new MeteorShowerEnchantment();
        new SummonMerchantEnchantment();
        new SummonWolfEnchantment();
        new UnbindEnchantment();
        new LightningEnchantment();
        new GrapplingHookEnchantment();
        new EarthquakeEnchantment();
        new LoudStrikesEnchantment();
        new RepairEnchantment();
        new LuckySourceEnchantment();
        new EnchantedSourceEnchantment();
    }

    public static boolean isCustomEnchantment(String enchantmentFilename) {
        for (CustomEnchantment customEnchantment : customEnchantmentMap.values())
            if (customEnchantment.getKey().equalsIgnoreCase(enchantmentFilename))
                return true;
        return false;
    }

    /*
    Check an itemstack has this enchantment
     */
    public static boolean hasCustomEnchantment(ItemStack itemStack, String key) {
        if (itemStack == null || itemStack.getType().equals(Material.AIR)) return false;
        if (!itemStack.hasItemMeta()) return false;
        if (!itemStack.getItemMeta().hasLore()) return false;
        return hasCustomEnchantment(itemStack.getItemMeta(), key);
    }

    public static boolean hasCustomEnchantment(ItemMeta itemMeta, String key) {
        return ItemTagger.getEnchantment(itemMeta, new NamespacedKey(MetadataHandler.PLUGIN, key)) > 0;
    }

    /*
    Get a custom enchantment's level
     */
    public static int getCustomEnchantmentLevel(ItemStack itemStack, String key) {
        if (itemStack == null) return 0;
        return ItemTagger.getEnchantment(itemStack.getItemMeta(), new NamespacedKey(MetadataHandler.PLUGIN, key));
    }

    public String getKey() {
        return key;
    }

    public EnchantmentsConfigFields getEnchantmentsConfigFields() {
        return enchantmentsConfigFields;
    }

}
