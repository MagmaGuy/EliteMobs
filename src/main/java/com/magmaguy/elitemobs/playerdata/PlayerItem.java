package com.magmaguy.elitemobs.playerdata;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.ItemTierFinder;
import com.magmaguy.elitemobs.items.customenchantments.CriticalStrikesEnchantment;
import com.magmaguy.elitemobs.items.customenchantments.HunterEnchantment;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffect;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class PlayerItem {

    public enum EquipmentSlot {
        HELMET,
        CHESTPLATE,
        LEGGINGS,
        BOOTS,
        MAINHAND,
        OFFHAND
    }

    public EquipmentSlot equipmentSlot;
    public Player player;
    public ItemStack itemStack = null;
    public int itemTier = 0;
    public ArrayList<ElitePotionEffect> continuousPotionEffects = new ArrayList<>();
    public ArrayList<ElitePotionEffect> onHitPotionEffects = new ArrayList<>();

    public int damageArthropodsLevel = 0;
    public int damageUndeadLevel = 0;
    public int thornsLevel = 0;
    private double critChance = 0;
    private double hunterChance = 0;


    /**
     * Stores an instance of the custom EliteMobs values of what a player is wearing. This is used to reduce the amount
     * of checks done by EliteMobs during combat and for passive potion effect applications. It should (largely) only update
     * when necessary.
     *
     * @param itemStack     ItemStack in the equipment slot. Does not update.
     * @param equipmentSlot Player's equipment slot. This is used to quickly access weapons and armor for the combat system. Updates when the ItemStack changes.
     * @param player        Player associated to the gear. Does not update.
     */
    public PlayerItem(ItemStack itemStack, EquipmentSlot equipmentSlot, Player player) {
        this.equipmentSlot = equipmentSlot; //equipment slot never updates
        this.player = player;
        fullUpdate(itemStack);
    }

    private boolean fullUpdate(ItemStack itemStack) {

        //case when both are null
        if (itemStack == null && this.itemStack == null)
            return false;

        //case when it became null
        if (itemStack == null)
            return fillNullItem();

        //case when it's the same item as before - best performance
        if (itemStack.isSimilar(this.itemStack))
            return false;

        if (EnchantmentsConfig.getEnchantment(SoulbindEnchantment.key + ".yml").isEnabled())
            if (!SoulbindEnchantment.isValidSoulbindUser(itemStack.getItemMeta(), player)) {
                player.getWorld().dropItem(player.getLocation(), itemStack);
                itemStack.setAmount(0);
                itemStack = new ItemStack(Material.AIR);
            }

        //case when the item changed during runtime to another valid ItemStack
        this.itemStack = itemStack;
        if (equipmentSlot.equals(EquipmentSlot.MAINHAND))
            this.itemTier = ItemTierFinder.mainHandCombatParser(itemStack);
        else
            this.itemTier = ItemTierFinder.findBattleTier(itemStack);

        this.continuousPotionEffects = ItemTagger.getPotionEffects(itemStack.getItemMeta(), ItemTagger.continuousPotionEffectKey);
        this.onHitPotionEffects = ItemTagger.getPotionEffects(itemStack.getItemMeta(), ItemTagger.onHitPotionEffectKey);

        if (equipmentSlot.equals(EquipmentSlot.MAINHAND)) {
            this.damageArthropodsLevel = ItemTagger.getEnchantment(itemStack.getItemMeta(), Enchantment.DAMAGE_ARTHROPODS.getKey());
            this.damageUndeadLevel = ItemTagger.getEnchantment(itemStack.getItemMeta(), Enchantment.DAMAGE_UNDEAD.getKey());
            this.critChance = ItemTagger.getEnchantment(itemStack.getItemMeta(), new NamespacedKey(MetadataHandler.PLUGIN, CriticalStrikesEnchantment.key)) / 10D;
        }

        this.hunterChance = ItemTagger.getEnchantment(itemStack.getItemMeta(), new NamespacedKey(MetadataHandler.PLUGIN, HunterEnchantment.key)) * EnchantmentsConfig.getEnchantment("hunter.yml").getFileConfiguration().getDouble("hunterSpawnBonus");

        checkArmorSpecificFeatures();

        return true;

    }

    private boolean fillNullItem() {
        this.itemStack = ItemStackGenerator.generateItemStack(Material.AIR);
        this.itemTier = 0;
        this.continuousPotionEffects = new ArrayList<>();
        this.onHitPotionEffects = new ArrayList<>();
        this.damageArthropodsLevel = 0;
        this.damageUndeadLevel = 0;
        return true;
    }

    private void checkArmorSpecificFeatures() {
        if (!(equipmentSlot.equals(EquipmentSlot.HELMET) ||
                equipmentSlot.equals(EquipmentSlot.CHESTPLATE) ||
                equipmentSlot.equals(EquipmentSlot.LEGGINGS) ||
                equipmentSlot.equals(EquipmentSlot.BOOTS))) return;
        this.thornsLevel = ItemTagger.getEnchantment(itemStack.getItemMeta(), Enchantment.THORNS.getKey());
    }

    public int getTier(ItemStack itemStack, boolean update) {
        if (update)
            fullUpdate(itemStack);
        return this.itemTier;
    }

    public ArrayList<ElitePotionEffect> getContinuousPotionEffects(ItemStack itemStack, boolean update) {
        if (update)
            fullUpdate(itemStack);
        return this.continuousPotionEffects;
    }

    public ArrayList<ElitePotionEffect> getOnHitPotionEffects(ItemStack itemStack, boolean update) {
        if (update)
            fullUpdate(itemStack);
        return this.onHitPotionEffects;
    }

    public int getDamageArthropodsLevel(ItemStack itemStack, boolean update) {
        if (update)
            fullUpdate(itemStack);
        return this.damageArthropodsLevel;
    }

    public int getDamageUndeadLevel(ItemStack itemStack, boolean update) {
        if (update)
            fullUpdate(itemStack);
        return this.damageUndeadLevel;
    }

    public double getCritChance(ItemStack itemStack, boolean update) {
        if (update)
            fullUpdate(itemStack);
        return this.critChance;
    }

    public double getHunterChance(ItemStack itemStack, boolean update) {
        if (update)
            fullUpdate(itemStack);
        return this.hunterChance;
    }

}
