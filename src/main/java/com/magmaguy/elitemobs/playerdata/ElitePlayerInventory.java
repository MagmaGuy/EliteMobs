package com.magmaguy.elitemobs.playerdata;

import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffect;
import com.magmaguy.elitemobs.skills.CombatLevelCalculator;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class ElitePlayerInventory {

    public static HashMap<UUID, ElitePlayerInventory> playerInventories = new HashMap<>();
    public final PlayerItem helmet, chestplate, leggings, boots, mainhand, offhand;
    private final Player player;
    //Used by elite scripts
    private final HashSet<String> customMetadata = new HashSet<>();

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
        if (player == null) return null;

        ElitePlayerInventory elitePlayerInventory = playerInventories.get(player.getUniqueId());
        if (elitePlayerInventory != null) return elitePlayerInventory;

        if (!player.isOnline() || player.hasMetadata("NPC")) return null;
        return new ElitePlayerInventory(player);
    }

    public static void shutdown() {
        playerInventories.clear();
    }

    public static void initialize() {
        for (Player player : Bukkit.getOnlinePlayers())
            playerInventories.put(player.getUniqueId(), new ElitePlayerInventory(player));
    }

    public static ItemStack[] getHeldAndEquippedItems(PlayerInventory inventory) {
        return new ItemStack[]{
                inventory.getItemInMainHand(),
                inventory.getItemInOffHand(),
                inventory.getHelmet(),
                inventory.getChestplate(),
                inventory.getLeggings(),
                inventory.getBoots()
        };
    }

    public double getEliteDamage(boolean update) {
        double eliteDamage = helmet.getEliteDamage(player.getInventory().getHelmet(), update) +
                chestplate.getEliteDamage(player.getInventory().getChestplate(), update) +
                leggings.getEliteDamage(player.getInventory().getLeggings(), update) +
                boots.getEliteDamage(player.getInventory().getBoots(), update) +
                mainhand.getEliteDamage(player.getInventory().getItemInMainHand(), update) +
                offhand.getEliteDamage(player.getInventory().getItemInOffHand(), update);
        return eliteDamage;
    }

    public double getEliteEnchantmentDamage(boolean update) {
        double eliteEnchantmentDamage = helmet.getEliteEnchantmentDamage(player.getInventory().getHelmet(), update) +
                chestplate.getEliteEnchantmentDamage(player.getInventory().getChestplate(), update) +
                leggings.getEliteEnchantmentDamage(player.getInventory().getLeggings(), update) +
                boots.getEliteEnchantmentDamage(player.getInventory().getBoots(), update) +
                mainhand.getEliteEnchantmentDamage(player.getInventory().getItemInMainHand(), update) +
                offhand.getEliteEnchantmentDamage(player.getInventory().getItemInOffHand(), update);
        return eliteEnchantmentDamage;
    }

    public double getEliteDefense(boolean update) {
        double defense = helmet.getEliteDefense(player.getInventory().getHelmet(), update) +
                chestplate.getEliteDefense(player.getInventory().getChestplate(), update) +
                leggings.getEliteDefense(player.getInventory().getLeggings(), update) +
                boots.getEliteDefense(player.getInventory().getBoots(), update) +
                mainhand.getEliteDefense(player.getInventory().getItemInMainHand(), update) +
                offhand.getEliteDefense(player.getInventory().getItemInOffHand(), update);
        return defense;
    }

    public double getEliteProjectileProtection(boolean update) {
        double eliteProjectileProtection = (helmet.getProtectionProjectile(player.getInventory().getHelmet(), update) +
                chestplate.getProtectionProjectile(player.getInventory().getChestplate(), update) +
                leggings.getProtectionProjectile(player.getInventory().getLeggings(), update) +
                boots.getProtectionProjectile(player.getInventory().getBoots(), update) +
                mainhand.getProtectionProjectile(player.getInventory().getItemInMainHand(), update) +
                offhand.getProtectionProjectile(player.getInventory().getItemInOffHand(), update))
                / 6d;
        return eliteProjectileProtection;
    }

    public double getEliteBlastProtection(boolean update) {
        double eliteBlastProtection = (helmet.getBlastProtection(player.getInventory().getHelmet(), update) +
                chestplate.getBlastProtection(player.getInventory().getChestplate(), update) +
                leggings.getBlastProtection(player.getInventory().getLeggings(), update) +
                boots.getBlastProtection(player.getInventory().getBoots(), update) +
                mainhand.getBlastProtection(player.getInventory().getItemInMainHand(), update) +
                offhand.getBlastProtection(player.getInventory().getItemInOffHand(), update))
                / 6d;
        return eliteBlastProtection;
    }

    /**
     * Tier of the weapon in the main hand.
     *
     * @return Tier of the weapon in the main hand.
     */
    public int getWeaponLevel(boolean update) {
        int weaponLevel = mainhand.getTier(player.getInventory().getItemInMainHand(), update);
        return weaponLevel;
    }

    public int getFullPlayerTier(boolean update) {
        return CombatLevelCalculator.calculateCombatLevel(player.getUniqueId());
    }

    public int getNaturalMobSpawnLevel(boolean update) {
        if (player.getGameMode().equals(GameMode.SPECTATOR)) return 0;
        return CombatLevelCalculator.calculateCombatLevel(player.getUniqueId());
    }

    /**
     * Returns a list of all continuous potion effects in armor/weapons, may contain duplicate effects.
     *
     * @return ArrayList of all continuous potion effects.
     */
    public ArrayList<ElitePotionEffect> getContinuousPotionEffects(boolean update) {
        ArrayList<ElitePotionEffect> elitePotionEffects = new ArrayList<>();
        elitePotionEffects.addAll(helmet.getContinuousPotionEffects(player.getInventory().getHelmet(), update));
        elitePotionEffects.addAll(chestplate.getContinuousPotionEffects(player.getInventory().getChestplate(), update));
        elitePotionEffects.addAll(leggings.getContinuousPotionEffects(player.getInventory().getLeggings(), update));
        elitePotionEffects.addAll(boots.getContinuousPotionEffects(player.getInventory().getBoots(), update));
        elitePotionEffects.addAll(mainhand.getContinuousPotionEffects(player.getInventory().getItemInMainHand(), update));
        elitePotionEffects.addAll(offhand.getContinuousPotionEffects(player.getInventory().getItemInOffHand(), update));
        return elitePotionEffects;
    }

    /**
     * Returns a list of all on hit potion effects in armor/weapons, may contain duplicates.
     *
     * @return ArrayList of all onHit potion effects.
     */
    public ArrayList<ElitePotionEffect> getOnHitPotionEffects(boolean update) {
        ArrayList<ElitePotionEffect> elitePotionEffects = new ArrayList<>();
        elitePotionEffects.addAll(helmet.getOnHitPotionEffects(player.getInventory().getHelmet(), update));
        elitePotionEffects.addAll(chestplate.getOnHitPotionEffects(player.getInventory().getChestplate(), update));
        elitePotionEffects.addAll(leggings.getOnHitPotionEffects(player.getInventory().getLeggings(), update));
        elitePotionEffects.addAll(boots.getOnHitPotionEffects(player.getInventory().getBoots(), update));
        elitePotionEffects.addAll(mainhand.getOnHitPotionEffects(player.getInventory().getItemInMainHand(), update));
        elitePotionEffects.addAll(offhand.getOnHitPotionEffects(player.getInventory().getItemInOffHand(), update));
        return elitePotionEffects;
    }

    public double getCritChance(boolean update) {
        ItemStack[] activeItems = getHeldAndEquippedItems(player.getInventory());
        double critChance = mainhand.getCritChance(activeItems[0], update) +
                offhand.getCritChance(activeItems[1], update) +
                helmet.getCritChance(activeItems[2], update) +
                chestplate.getCritChance(activeItems[3], update) +
                leggings.getCritChance(activeItems[4], update) +
                boots.getCritChance(activeItems[5], update);
        // Cap the total. Chance is additive across six slots, so without a ceiling a player can
        // reach 100% and crit on every swing - at which point the crit IS the typical hit and
        // stops reading as a critical strike, while still paying out the full 1.5x multiplier.
        double cap = Math.max(0D, Math.min(1D, MobCombatSettingsConfig.getMaximumCriticalStrikeChance()));
        return Math.min(cap, critChance);
    }

    public double getLightningChance(boolean update) {
        double lightningChance = mainhand.getLightningChance(player.getInventory().getItemInMainHand(), update);
        return lightningChance;
    }

    public double getHunterChance(boolean update) {
        double hunterChance = helmet.getHunterChance(player.getInventory().getHelmet(), update) +
                chestplate.getHunterChance(player.getInventory().getChestplate(), update) +
                leggings.getHunterChance(player.getInventory().getLeggings(), update) +
                boots.getHunterChance(player.getInventory().getBoots(), update);
        return hunterChance;
    }

    public double getPlasmaBootsLevel(boolean update) {
        double plasmaBootsLevel = boots.getPlasmaBootsLevel(player.getInventory().getBoots(), update);
        return plasmaBootsLevel;
    }

    public double getEarthquakeLevel(boolean update) {
        //todo: should earthquake really apply for things other than the boots?
        double earthquakeLevel = helmet.getEarthquakeLevel(player.getInventory().getHelmet(), update) +
                chestplate.getEarthquakeLevel(player.getInventory().getChestplate(), update) +
                leggings.getEarthquakeLevel(player.getInventory().getLeggings(), update) +
                boots.getEarthquakeLevel(player.getInventory().getBoots(), update);
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

    public double getLoudStrikesBonusMultiplier(boolean update) {
        double loudStrikesBonusMultiplier = helmet.getLoudStrikesBonus(player.getInventory().getHelmet(), update) +
                chestplate.getLoudStrikesBonus(player.getInventory().getChestplate(), update) +
                leggings.getLoudStrikesBonus(player.getInventory().getLeggings(), update) +
                boots.getLoudStrikesBonus(player.getInventory().getBoots(), update) +
                mainhand.getLoudStrikesBonus(player.getInventory().getItemInMainHand(), update) +
                offhand.getLoudStrikesBonus(player.getInventory().getItemInOffHand(), update);
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
