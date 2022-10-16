package com.magmaguy.elitemobs.playerdata;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.combatsystem.CombatSystem;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.customenchantments.*;
import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffect;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class PlayerItem {

    public EquipmentSlot equipmentSlot;
    public Player player;
    public ItemStack itemStack = null;
    public int itemTier = 0;
    public ArrayList<ElitePotionEffect> continuousPotionEffects = new ArrayList<>();
    public ArrayList<ElitePotionEffect> onHitPotionEffects = new ArrayList<>();
    public int damageArthropodsLevel = 0;
    public int damageUndeadLevel = 0;
    public int thornsLevel = 0;
    private double plasmaBootsLevel = 0;
    private double critChance = 0;
    private double hunterChance = 0;
    private double lightningChance = 0;
    private double earthquakeLevel = 0;
    private double eliteDamageReduction = 0;
    private double protectionProjectile = 0;
    private double eliteDamage = 0;
    private double blastProtection = 0;
    private double loudStrikesBonus = 0;

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
        if (equipmentSlot.equals(EquipmentSlot.MAINHAND)) {
            this.itemTier = (int) Math.round(EliteItemManager.getWeaponLevel(itemStack));
            this.eliteDamage = EliteItemManager.getEliteDamageFromEliteAttributes(itemStack);
        } else
            this.itemTier = (int) Math.round(EliteItemManager.getArmorLevel(itemStack));
        this.continuousPotionEffects = ItemTagger.getPotionEffects(itemStack.getItemMeta(), ItemTagger.continuousPotionEffectKey);
        this.onHitPotionEffects = ItemTagger.getPotionEffects(itemStack.getItemMeta(), ItemTagger.onHitPotionEffectKey);

        //Enchantments are global, any inventory slot will add to the total of any enchantment
        this.eliteDamageReduction = EliteItemManager.getEliteDefense(itemStack) + EliteItemManager.getBonusEliteDefense(itemStack);
        this.protectionProjectile = ItemTagger.getEnchantment(itemStack.getItemMeta(), Enchantment.PROTECTION_PROJECTILE.getKey());
        this.blastProtection = ItemTagger.getEnchantment(itemStack.getItemMeta(), Enchantment.PROTECTION_EXPLOSIONS.getKey());
        this.damageArthropodsLevel = ItemTagger.getEnchantment(itemStack.getItemMeta(), Enchantment.DAMAGE_ARTHROPODS.getKey());
        this.damageUndeadLevel = ItemTagger.getEnchantment(itemStack.getItemMeta(), Enchantment.DAMAGE_UNDEAD.getKey());
        this.critChance = ItemTagger.getEnchantment(itemStack.getItemMeta(), new NamespacedKey(MetadataHandler.PLUGIN, CriticalStrikesEnchantment.key)) / 10D;
        this.lightningChance = Math.pow(ItemTagger.getEnchantment(itemStack.getItemMeta(), new NamespacedKey(MetadataHandler.PLUGIN, LightningEnchantment.key)), 2) / 1000D;
        this.plasmaBootsLevel = ItemTagger.getEnchantment(itemStack.getItemMeta(), new NamespacedKey(MetadataHandler.PLUGIN, PlasmaBootsEnchantment.key));
        this.hunterChance = ItemTagger.getEnchantment(itemStack.getItemMeta(), new NamespacedKey(MetadataHandler.PLUGIN, HunterEnchantment.key)) * EnchantmentsConfig.getEnchantment("hunter.yml").getFileConfiguration().getDouble("hunterSpawnBonus");
        this.earthquakeLevel = ItemTagger.getEnchantment(itemStack.getItemMeta(), new NamespacedKey(MetadataHandler.PLUGIN, EarthquakeEnchantment.key));
        this.thornsLevel = ItemTagger.getEnchantment(itemStack.getItemMeta(), Enchantment.THORNS.getKey());
        this.loudStrikesBonus = ItemTagger.getEnchantment(itemStack.getItemMeta(), new NamespacedKey(MetadataHandler.PLUGIN, LoudStrikesEnchantment.key)) / 3d;
        eliteDamage += EliteItemManager.getEliteDamageFromEnchantments(itemStack);

        //Level sync for instanced dungeons - limits the max level of the item
        if (PlayerData.getMatchInstance(player) != null &&
                PlayerData.getMatchInstance(player) instanceof DungeonInstance dungeonInstance &&
                dungeonInstance.getLevelSync() > 0) {
            //Recalculate damage
            if (equipmentSlot.equals(EquipmentSlot.MAINHAND)) {
                double syncedDPS = CombatSystem.DPS_PER_LEVEL * dungeonInstance.getLevelSync();
                double currentDPS = EliteItemManager.getDPS(EliteItemManager.getBaseDamage(itemStack) + EliteItemManager.getEliteDamageFromEliteAttributes(itemStack), EliteItemManager.getAttackSpeed(itemStack));
                if (currentDPS > syncedDPS)
                    this.eliteDamage = syncedDPS * (1 / EliteItemManager.getAttackSpeed(itemStack)) + EliteItemManager.getEliteDamageFromEnchantments(itemStack) - EliteItemManager.getBaseDamage(itemStack);
            }
            this.eliteDamageReduction = Math.min(dungeonInstance.getLevelSync(), EliteItemManager.getEliteDefense(itemStack)) / 4d +
                    EliteItemManager.getBonusEliteDefense(itemStack);
        }

        return true;

    }

    private boolean fillNullItem() {
        itemStack = null;
        continuousPotionEffects = new ArrayList<>();
        onHitPotionEffects = new ArrayList<>();
        damageArthropodsLevel = 0;
        damageUndeadLevel = 0;
        thornsLevel = 0;
        plasmaBootsLevel = 0;
        critChance = 0;
        hunterChance = 0;
        lightningChance = 0;
        earthquakeLevel = 0;
        eliteDamageReduction = 0;
        protectionProjectile = 0;
        eliteDamage = 0;
        blastProtection = 0;
        loudStrikesBonus = 0;
        return true;
    }

    public int getTier(ItemStack itemStack, boolean update) {
        if (update) fullUpdate(itemStack);
        return this.itemTier;
    }

    public double getEliteDamage(ItemStack itemStack, boolean update) {
        if (update) fullUpdate(itemStack);
        return this.eliteDamage;
    }

    public double getEliteDefense(ItemStack itemStack, boolean update) {
        if (update) fullUpdate(itemStack);
        return this.eliteDamageReduction;
    }

    public double getProtectionProjectile(ItemStack itemStack, boolean update) {
        if (update) fullUpdate(itemStack);
        return this.protectionProjectile;
    }

    public double getBlastProtection(ItemStack itemStack, boolean update) {
        if (update) fullUpdate(itemStack);
        return this.blastProtection;
    }

    public ArrayList<ElitePotionEffect> getContinuousPotionEffects(ItemStack itemStack, boolean update) {
        if (update) fullUpdate(itemStack);
        return this.continuousPotionEffects;
    }

    public ArrayList<ElitePotionEffect> getOnHitPotionEffects(ItemStack itemStack, boolean update) {
        if (update) fullUpdate(itemStack);
        return this.onHitPotionEffects;
    }

    public int getDamageArthropodsLevel(ItemStack itemStack, boolean update) {
        if (update) fullUpdate(itemStack);
        return this.damageArthropodsLevel;
    }

    public int getDamageUndeadLevel(ItemStack itemStack, boolean update) {
        if (update) fullUpdate(itemStack);
        return this.damageUndeadLevel;
    }

    public double getCritChance(ItemStack itemStack, boolean update) {
        if (update) fullUpdate(itemStack);
        return this.critChance;
    }

    public double getHunterChance(ItemStack itemStack, boolean update) {
        if (update) fullUpdate(itemStack);
        return this.hunterChance;
    }

    public double getLightningChance(ItemStack itemStack, boolean update) {
        if (update) fullUpdate(itemStack);
        return this.lightningChance;
    }

    public double getPlasmaBootsLevel(ItemStack itemStack, boolean update) {
        if (update) fullUpdate(itemStack);
        return this.plasmaBootsLevel;
    }

    public double getEarthquakeLevel(ItemStack itemStack, boolean update) {
        if (update) fullUpdate(itemStack);
        return this.earthquakeLevel;
    }

    public double getLoudStrikesBonus(ItemStack itemStack, boolean update) {
        if (update) fullUpdate(itemStack);
        return this.loudStrikesBonus;
    }

    public enum EquipmentSlot {
        HELMET,
        CHESTPLATE,
        LEGGINGS,
        BOOTS,
        MAINHAND,
        OFFHAND
    }

}
