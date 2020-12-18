package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.api.DamageEliteMob;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.collateralminecraftchanges.PlayerDeathMessageByEliteMob;
import com.magmaguy.elitemobs.combatsystem.displays.DamageDisplay;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class EliteMobDamagedByPlayerHandler implements Listener {

    public static boolean display = false;
    public static int damage = 0;

    /**
     * Player -> EliteMobs damage. Ignores vanilla armor that Elite Mobs are wearing as that is purely cosmetic.
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void eliteMobDamageByPlayer(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        EliteMobEntity eliteMobEntity = EntityTracker.getEliteMobEntity(event.getEntity());
        if (eliteMobEntity == null) return;
        Player player = null;
        if (event.getDamager() instanceof Player)
            player = (Player) event.getDamager();
        if ((event.getDamager() instanceof Arrow || event.getDamager() instanceof Trident) &&
                ((Projectile) event.getDamager()).getShooter() instanceof Player)
            player = (Player) ((Projectile) event.getDamager()).getShooter();
        if (player == null) return;

        //citizens
        if (player.hasMetadata("NPC")) {
            DamageEliteMob.lowDamage(eliteMobEntity);
            return;
        }

        //From this point on, the event damage is handled by Elite Mobs

        //if the damage source is custom , the damage is final
        if (CombatSystem.bypass) {
            double rawDamage = event.getDamage();
            CombatSystem.bypass = false;
            Bukkit.getServer().getPluginManager().callEvent(new EliteMobDamagedByPlayerEvent(eliteMobEntity, player, event, damage));
            //Deal with the mob getting killed
            if (player.getHealth() - rawDamage <= 0)
                PlayerDeathMessageByEliteMob.addDeadPlayer(player, PlayerDeathMessageByEliteMob.initializeDeathMessage(player, player));
            eliteMobEntity.damage(rawDamage);
            return;
        }

        double playerWeaponTier;
        //Melee attacks dealt with ranged weapons should have a tier of 0
        if (!(event.getDamager() instanceof Projectile) &&
                (player.getInventory().getItemInMainHand().getType().equals(Material.BOW) ||
                        player.getInventory().getItemInMainHand().getType().equals(Material.CROSSBOW)))
            playerWeaponTier = 0;
        else
            playerWeaponTier = ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getWeaponTier(true);
        double newDamage = finalDamageCalculator(playerWeaponTier, player, eliteMobEntity);

        if (event.getDamager() instanceof Arrow) {
            double arrowSpeedMultiplier = Math.sqrt(Math.pow(event.getDamager().getVelocity().getX(), 2) +
                    Math.pow(event.getDamager().getVelocity().getY(), 2) +
                    Math.pow(event.getDamager().getVelocity().getZ(), 2)) / 5;
            arrowSpeedMultiplier = (arrowSpeedMultiplier < 1) ? arrowSpeedMultiplier : 1;
            newDamage *= arrowSpeedMultiplier;
        }

        if (isCriticalHit(player)) {
            newDamage += newDamage * 0.5;
            DamageDisplay.isCriticalHit = true;
        }

        //event.setDamage(EntityDamageEvent.DamageModifier.BASE, event.getDamage());

        if (eliteMobEntity.getHealth() - newDamage < 0)
            newDamage = eliteMobEntity.getHealth();
        eliteMobEntity.addDamager(player, newDamage);

        playerHitCooldownHashMap.put(player, clock);

        Bukkit.getServer().getPluginManager().callEvent(new EliteMobDamagedByPlayerEvent(eliteMobEntity, player, event, newDamage));
        DamageDisplay.isCustomDamage = true;
        if (!event.isCancelled())
            eliteMobEntity.damage(newDamage);

    }

    /**
     * Calculates Player -> EliteMobs damage.
     *
     * @param player Player object
     * @return
     */
    private double finalDamageCalculator(double playerWeaponTier, Player player, EliteMobEntity eliteMobEntity) {

        double finalDamage = getCooledAttackStrength(player) *
                (playerWeaponTier + secondaryEnchantmentDamageIncrease(player, eliteMobEntity.getLivingEntity())) *
                MobCombatSettingsConfig.damageToEliteMultiplier;

        finalDamage = finalDamage < 1 ? 1 : finalDamage;

        return finalDamage;

    }

    /**
     * Calculates the cooldown debuff following vanilla Minecraft rules and then linearly applies a debuff to attacks
     *
     * @param player Player in cooldown
     * @return Debuff multiplier
     */
    private float getCooledAttackStrength(Player player) {
        if (!playerHitCooldownHashMap.containsKey(player)) return 1;
        float swingDelay = clock - playerHitCooldownHashMap.get(player);
        float cooldownPeriod = getCooldownPeriod(player);
        if (swingDelay > cooldownPeriod) return 1;
        return swingDelay / cooldownPeriod;
    }

    /**
     * Calculates the cooldown following vanilla rules
     *
     * @param player
     * @return
     */
    private float getCooldownPeriod(Player player) {
        return (float) (1.0D / player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).getValue() * 20.0D);
    }

    private static int clock = 0;

    public static void launchInternalClock() {
        new BukkitRunnable() {

            @Override
            public void run() {
                if (clock == Integer.MAX_VALUE) clock = 0;
                clock++;
            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 1, 1);
    }


    private double secondaryEnchantmentDamageIncrease(Player player, LivingEntity eliteMob) {

        if (eliteMob instanceof Spider || eliteMob instanceof Silverfish)
            return ElitePlayerInventory.playerInventories.get(player.getUniqueId()).mainhand.getDamageArthropodsLevel(player.getInventory().getItemInMainHand(), false);
        if (eliteMob instanceof Zombie || eliteMob instanceof Skeleton)
            return ElitePlayerInventory.playerInventories.get(player.getUniqueId()).mainhand.getDamageUndeadLevel(player.getInventory().getItemInMainHand(), false);

        return 0;

    }

    private boolean isCriticalHit(Player player) {
        double criticalStrike = ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getCritChance(false);
        criticalStrike += (GuildRank.critBonusValue(GuildRank.getGuildPrestigeRank(player), GuildRank.getActiveGuildRank(player)) / 100);
        return ThreadLocalRandom.current().nextDouble() < criticalStrike;
    }

    private final HashMap<Player, Integer> playerHitCooldownHashMap = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!(event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK) ||
                event.getAction().equals(Action.PHYSICAL))) return;
        playerHitCooldownHashMap.put(event.getPlayer(), clock);

    }

}
