package com.magmaguy.elitemobs.playerdata;

import com.magmaguy.elitemobs.items.MobTierCalculator;
import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffect;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ElitePlayerInventory {

    public static void initialize() {
        for (Player player : Bukkit.getOnlinePlayers())
            playerInventories.put(player.getUniqueId(), new ElitePlayerInventory(player));
    }

    public static HashMap<UUID, ElitePlayerInventory> playerInventories = new HashMap<>();

    public final PlayerItem helmet, chestplate, leggings, boots, mainhand, offhand;
    private final Player player;

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

    /**
     * An average of all equipped armor tiers!
     *
     * @return Average of all armor tiers
     */
    public double getArmorTier(boolean update) {
        return (helmet.getTier(player.getInventory().getHelmet(), update) +
                chestplate.getTier(player.getInventory().getChestplate(), update) +
                leggings.getTier(player.getInventory().getLeggings(), update) +
                boots.getTier(player.getInventory().getBoots(), update))
                / 4D;
    }

    /**
     * Tier of the weapon in the main hand.
     *
     * @return Tier of the weapon in the main hand.
     */
    public int getWeaponTier(boolean update) {
        return mainhand.getTier(player.getInventory().getItemInMainHand(), update);
    }

    public int getFullPlayerTier(boolean update) {
        return (int) ((helmet.getTier(player.getInventory().getHelmet(), update) +
                chestplate.getTier(player.getInventory().getChestplate(), update) +
                leggings.getTier(player.getInventory().getLeggings(), update) +
                boots.getTier(player.getInventory().getBoots(), update) +
                mainhand.getTier(player.getInventory().getItemInMainHand(), update))
                / 5D);
    }

    public int getNaturalMobSpawnLevel(boolean update) {
        return (int) ((helmet.getTier(player.getInventory().getHelmet(), update) +
                chestplate.getTier(player.getInventory().getChestplate(), update) +
                leggings.getTier(player.getInventory().getLeggings(), update) +
                boots.getTier(player.getInventory().getBoots(), update) +
                mainhand.getTier(player.getInventory().getItemInMainHand(), update))
                / 5D * MobTierCalculator.PER_TIER_LEVEL_INCREASE);
    }

    /**
     * Returns a list of all continuous potion effects in armor/weapons, may contain duplicate effects.
     *
     * @return ArrayList of all continuous potion effects.
     */
    public ArrayList<ElitePotionEffect> getContinuousPotionEffects(boolean update) {
        ArrayList<ElitePotionEffect> elitePotionEffects = new ArrayList<>();
        if (player.getInventory().getHelmet() != null)
            elitePotionEffects.addAll(helmet.getContinuousPotionEffects(player.getInventory().getHelmet(), update));
        if (player.getInventory().getChestplate() != null)
            elitePotionEffects.addAll(chestplate.getContinuousPotionEffects(player.getInventory().getChestplate(), update));
        if (player.getInventory().getLeggings() != null)
            elitePotionEffects.addAll(leggings.getContinuousPotionEffects(player.getInventory().getLeggings(), update));
        if (player.getInventory().getBoots() != null)
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
        return mainhand.getCritChance(player.getInventory().getItemInMainHand(), update);
    }

    public double getHunterChance(boolean update) {
        return helmet.getHunterChance(player.getInventory().getHelmet(), update) +
                chestplate.getHunterChance(player.getInventory().getChestplate(), update) +
                leggings.getHunterChance(player.getInventory().getLeggings(), update) +
                boots.getHunterChance(player.getInventory().getBoots(), update);
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

    /**
     * Outputs the base damage of the player given the current gear. Does not take secondary enchantments into account,
     * such as Smite
     *
     * @return Base damage value
     */
    public double baseDamage() {
        if (getWeaponTier(true) == 0)
            return 1;
        return getWeaponTier(false);
    }

    /**
     * Outputs the base damage reduction given the current gear. Does not take secondary enchantments into accounts,
     * such as Blast Protection
     *
     * @return Base damage reduction value
     */
    public double baseDamageReduction() {
        return getArmorTier(true);
    }

    /**
     * Outputs the total level of the thorns enchantment across all armor
     *
     * @return Total thorns enchantment level
     */
    public int getThornsLevel() {
        return helmet.thornsLevel + chestplate.thornsLevel + leggings.thornsLevel + boots.thornsLevel;
    }
}
