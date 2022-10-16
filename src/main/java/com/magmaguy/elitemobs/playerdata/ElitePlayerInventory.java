package com.magmaguy.elitemobs.playerdata;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.items.MobTierCalculator;
import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffect;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class ElitePlayerInventory {

    public static HashMap<UUID, ElitePlayerInventory> playerInventories = new HashMap<>();
    public final PlayerItem helmet, chestplate, leggings, boots, mainhand, offhand;
    private final Player player;
    private boolean isUpdateLock = false;
    private boolean getWeaponCooldown = false;
    //Used by elite scripts
    private HashSet<String> customMetadata = new HashSet<>();

    /**
     * Object of the player's inventory for EliteMobs.
     * For performance reasons, values are cached and updated only when strictly necessary.
     * Stores already parsed logic for weapon and armor tiers, as well as potion effects associated to weapons.
     *
     * @param player Inventory th
     */
    public ElitePlayerInventory(Player player) {
        this.player = player;
        this.helmet = new PlayerItem(player.getInventory().getHelmet(), PlayerItem.EquipmentSlot.HELMET, player);
        this.chestplate = new PlayerItem(player.getInventory().getChestplate(), PlayerItem.EquipmentSlot.CHESTPLATE, player);
        this.leggings = new PlayerItem(player.getInventory().getLeggings(), PlayerItem.EquipmentSlot.LEGGINGS, player);
        this.boots = new PlayerItem(player.getInventory().getBoots(), PlayerItem.EquipmentSlot.BOOTS, player);
        this.mainhand = new PlayerItem(player.getInventory().getItemInMainHand(), PlayerItem.EquipmentSlot.MAINHAND, player);
        this.offhand = new PlayerItem(player.getInventory().getItemInOffHand(), PlayerItem.EquipmentSlot.OFFHAND, player);
        playerInventories.put(player.getUniqueId(), this);
    }

    public static ElitePlayerInventory getPlayer(Player player) {
        return playerInventories.get(player.getUniqueId());
    }

    public static void initialize() {
        for (Player player : Bukkit.getOnlinePlayers())
            playerInventories.put(player.getUniqueId(), new ElitePlayerInventory(player));
    }

    /**
     * An average of all equipped armor tiers!
     *
     * @return Average of all armor tiers
     */
    public double getArmorLevel(boolean update) {
        if (isUpdateLock) update = false;

        double armorLevel = (helmet.getTier(player.getInventory().getHelmet(), update) +
                chestplate.getTier(player.getInventory().getChestplate(), update) +
                leggings.getTier(player.getInventory().getLeggings(), update) +
                boots.getTier(player.getInventory().getBoots(), update) +
                mainhand.getTier(player.getInventory().getItemInMainHand(), update) +
                offhand.getTier(player.getInventory().getItemInOffHand(), update))
                / 4D;
        updateLock();
        return armorLevel;
    }

    public double getEliteDamage(boolean update) {
        if (isUpdateLock) update = false;
        double eliteDamage = helmet.getEliteDamage(player.getInventory().getHelmet(), update) +
                chestplate.getEliteDamage(player.getInventory().getChestplate(), update) +
                leggings.getEliteDamage(player.getInventory().getLeggings(), update) +
                boots.getEliteDamage(player.getInventory().getBoots(), update) +
                mainhand.getEliteDamage(player.getInventory().getItemInMainHand(), update) +
                offhand.getEliteDamage(player.getInventory().getItemInOffHand(), update);
        updateLock();
        return eliteDamage;
    }

    public double getEliteDefense(boolean update) {
        if (isUpdateLock) update = false;
        double defense = helmet.getEliteDefense(player.getInventory().getHelmet(), update) +
                chestplate.getEliteDefense(player.getInventory().getChestplate(), update) +
                leggings.getEliteDefense(player.getInventory().getLeggings(), update) +
                boots.getEliteDefense(player.getInventory().getBoots(), update) +
                mainhand.getEliteDefense(player.getInventory().getItemInMainHand(), update) +
                mainhand.getEliteDefense(player.getInventory().getItemInOffHand(), update);
        updateLock();
        return defense;
    }

    public double getEliteProjectileProtection(boolean update) {
        if (isUpdateLock) update = false;

        double eliteProjectileProtection = (helmet.getProtectionProjectile(player.getInventory().getHelmet(), update) +
                chestplate.getProtectionProjectile(player.getInventory().getChestplate(), update) +
                leggings.getProtectionProjectile(player.getInventory().getLeggings(), update) +
                boots.getProtectionProjectile(player.getInventory().getBoots(), update) +
                mainhand.getProtectionProjectile(player.getInventory().getItemInMainHand(), update) +
                offhand.getProtectionProjectile(player.getInventory().getItemInOffHand(), update))
                / 4d;
        updateLock();
        return eliteProjectileProtection;
    }

    public double getEliteBlastProtection(boolean update) {
        if (isUpdateLock) update = false;
        double eliteBlastProtection = (helmet.getBlastProtection(player.getInventory().getHelmet(), update) +
                chestplate.getBlastProtection(player.getInventory().getChestplate(), update) +
                leggings.getBlastProtection(player.getInventory().getLeggings(), update) +
                boots.getBlastProtection(player.getInventory().getBoots(), update) +
                mainhand.getBlastProtection(player.getInventory().getItemInMainHand(), update) +
                offhand.getBlastProtection(player.getInventory().getItemInOffHand(), update))
                / 4d;
        updateLock();
        return eliteBlastProtection;
    }

    private boolean updateLock() {
        isUpdateLock = true;
        Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> isUpdateLock = false, 1);
        return true;
    }

    /**
     * Tier of the weapon in the main hand.
     *
     * @return Tier of the weapon in the main hand.
     */
    public int getWeaponLevel(boolean update) {
        if (isUpdateLock) update = false;
        int weaponLevel = mainhand.getTier(player.getInventory().getItemInMainHand(), update);
        updateLock();
        return weaponLevel;
    }

    public int getFullPlayerTier(boolean update) {
        if (isUpdateLock) update = false;
        int fullPlayerTier = (int) ((helmet.getTier(player.getInventory().getHelmet(), update) +
                chestplate.getTier(player.getInventory().getChestplate(), update) +
                leggings.getTier(player.getInventory().getLeggings(), update) +
                boots.getTier(player.getInventory().getBoots(), update) +
                mainhand.getTier(player.getInventory().getItemInMainHand(), update))
                / 5D);
        updateLock();
        return fullPlayerTier;
    }

    public int getNaturalMobSpawnLevel(boolean update) {
        if (isUpdateLock) update = false;
        if (player.getGameMode().equals(GameMode.SPECTATOR)) return 0;
        int naturalMobSpawnLevel = (int) ((helmet.getTier(player.getInventory().getHelmet(), update) +
                chestplate.getTier(player.getInventory().getChestplate(), update) +
                leggings.getTier(player.getInventory().getLeggings(), update) +
                boots.getTier(player.getInventory().getBoots(), update) +
                mainhand.getTier(player.getInventory().getItemInMainHand(), update))
                / 5D * MobTierCalculator.PER_TIER_LEVEL_INCREASE);
        updateLock();
        return naturalMobSpawnLevel;
    }

    /**
     * Returns a list of all continuous potion effects in armor/weapons, may contain duplicate effects.
     *
     * @return ArrayList of all continuous potion effects.
     */
    public ArrayList<ElitePotionEffect> getContinuousPotionEffects(boolean update) {
        if (isUpdateLock) update = false;
        ArrayList<ElitePotionEffect> elitePotionEffects = new ArrayList<>();
        elitePotionEffects.addAll(helmet.getContinuousPotionEffects(player.getInventory().getHelmet(), update));
        elitePotionEffects.addAll(chestplate.getContinuousPotionEffects(player.getInventory().getChestplate(), update));
        elitePotionEffects.addAll(leggings.getContinuousPotionEffects(player.getInventory().getLeggings(), update));
        elitePotionEffects.addAll(boots.getContinuousPotionEffects(player.getInventory().getBoots(), update));
        elitePotionEffects.addAll(mainhand.getContinuousPotionEffects(player.getInventory().getItemInMainHand(), update));
        elitePotionEffects.addAll(offhand.getContinuousPotionEffects(player.getInventory().getItemInOffHand(), update));
        updateLock();
        return elitePotionEffects;
    }

    /**
     * Returns a list of all on hit potion effects in armor/weapons, may contain duplicates.
     *
     * @return ArrayList of all onHit potion effects.
     */
    public ArrayList<ElitePotionEffect> getOnHitPotionEffects(boolean update) {
        if (isUpdateLock) update = false;
        ArrayList<ElitePotionEffect> elitePotionEffects = new ArrayList<>();
        elitePotionEffects.addAll(helmet.getOnHitPotionEffects(player.getInventory().getHelmet(), update));
        elitePotionEffects.addAll(chestplate.getOnHitPotionEffects(player.getInventory().getChestplate(), update));
        elitePotionEffects.addAll(leggings.getOnHitPotionEffects(player.getInventory().getLeggings(), update));
        elitePotionEffects.addAll(boots.getOnHitPotionEffects(player.getInventory().getBoots(), update));
        elitePotionEffects.addAll(mainhand.getOnHitPotionEffects(player.getInventory().getItemInMainHand(), update));
        elitePotionEffects.addAll(offhand.getOnHitPotionEffects(player.getInventory().getItemInOffHand(), update));
        updateLock();
        return elitePotionEffects;
    }

    public double getCritChance(boolean update) {
        if (isUpdateLock) update = false;
        double critChance = mainhand.getCritChance(player.getInventory().getItemInMainHand(), update);
        updateLock();
        return critChance;
    }

    public double getLightningChance(boolean update) {
        if (isUpdateLock) update = false;
        double lightningChance = mainhand.getLightningChance(player.getInventory().getItemInMainHand(), update);
        updateLock();
        return lightningChance;
    }

    public double getHunterChance(boolean update) {
        if (isUpdateLock) update = false;
        double hunterChance = helmet.getHunterChance(player.getInventory().getHelmet(), update) +
                chestplate.getHunterChance(player.getInventory().getChestplate(), update) +
                leggings.getHunterChance(player.getInventory().getLeggings(), update) +
                boots.getHunterChance(player.getInventory().getBoots(), update);
        updateLock();
        return hunterChance;
    }

    public double getPlasmaBootsLevel(boolean update) {
        if (isUpdateLock) update = false;
        double plasmaBootsLevel = boots.getPlasmaBootsLevel(player.getInventory().getBoots(), update);
        updateLock();
        return plasmaBootsLevel;
    }

    public double getEarthquakeLevel(boolean update) {
        //todo: should earthquake really apply for things other than the boots?
        if (isUpdateLock) update = false;
        double earthquakeLevel = helmet.getEarthquakeLevel(player.getInventory().getHelmet(), update) +
                chestplate.getEarthquakeLevel(player.getInventory().getChestplate(), update) +
                leggings.getEarthquakeLevel(player.getInventory().getLeggings(), update) +
                boots.getEarthquakeLevel(player.getInventory().getBoots(), update);
        updateLock();
        return earthquakeLevel;
    }

    /**
     * Outputs the base damage of the player given the current gear. Does not take secondary enchantments into account,
     * such as Smite
     *
     * @return Base damage value
     */
    public double baseDamage() {
        if (getWeaponLevel(true) == 0)
            return 1;
        return getWeaponLevel(false);
    }

    /**
     * Outputs the base damage reduction given the current gear. Does not take secondary enchantments into accounts,
     * such as Blast Protection
     *
     * @return Base damage reduction value
     */
    public double baseDamageReduction() {
        return getArmorLevel(true);
    }

    public double getLoudStrikesBonusMultiplier(boolean update) {
        if (isUpdateLock) update = false;
        double loudStrikesBonusMultiplier = helmet.getLoudStrikesBonus(player.getInventory().getHelmet(), update) +
                chestplate.getLoudStrikesBonus(player.getInventory().getChestplate(), update) +
                leggings.getLoudStrikesBonus(player.getInventory().getLeggings(), update) +
                boots.getLoudStrikesBonus(player.getInventory().getBoots(), update) +
                mainhand.getLoudStrikesBonus(player.getInventory().getItemInMainHand(), update) +
                offhand.getLoudStrikesBonus(player.getInventory().getItemInOffHand(), update);
        updateLock();
        return loudStrikesBonusMultiplier;
    }

    public boolean hasTag(String string) {
        return customMetadata.contains(string);
    }

    public HashSet<String> getTags() {
        return customMetadata;
    }

    public void addTags(List<String> string) {
        customMetadata.addAll(string);
    }

    public void removeTags(List<String> string) {
        customMetadata.removeAll(string);
    }

    public static class ElitePlayerInventoryEvents implements Listener {
        @EventHandler
        public void onPlayerLogin(PlayerLoginEvent event) {
            playerInventories.put(event.getPlayer().getUniqueId(), new ElitePlayerInventory(event.getPlayer()));
        }

        @EventHandler
        public void onPlayerLogout(PlayerQuitEvent event) {
            playerInventories.remove(event.getPlayer().getUniqueId());
        }
    }
}
