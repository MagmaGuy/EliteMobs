package com.magmaguy.elitemobs.items.customenchantments;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.events.BossSpecialAttackDamage;
import com.magmaguy.elitemobs.utils.CooldownHandler;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class FlamethrowerEnchantment extends CustomEnchantment implements Listener {

    public String setKey() {
        return "FLAMETHROWER";
    }

    private static ArrayList<Player> playersUsingFlamethrower = new ArrayList<>();

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();

        if (!(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR)))
            return;

        if (playersUsingFlamethrower.contains(player)) return;

        ItemStack zombieKingAxe = player.getInventory().getItemInMainHand();

        if (hasCustomEnchantment(zombieKingAxe)) {

            if (zombieKingAxe.getDurability() + 4 > zombieKingAxe.getType().getMaxDurability()) {
                return;
            } else zombieKingAxe.setDurability((short) (zombieKingAxe.getDurability() + 4));


            doFlamethrowerPhase1(player, player.getTargetBlock(null, 30).getLocation().clone().add(0.5, 1, 0.5));
            CooldownHandler.initialize(playersUsingFlamethrower, player, 3 * 60);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5 * 20, 20));

        }

    }

    private void doFlamethrowerPhase1(Player player, Location targetLocation) {

        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {

                doParticleEffect(player, targetLocation, Particle.SMOKE_NORMAL);
                counter++;

                if (counter < 20) return;
                doFlamethrowerPhase2(player, targetLocation);
                cancel();

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    private void doParticleEffect(Player player, Location target, Particle particle) {
        Vector directionVector = target.clone().subtract(player.getLocation()).toVector().normalize();
        for (int i = 0; i < 5; i++) {
            player.getWorld().spawnParticle(
                    particle,
                    player.getEyeLocation().clone().add(directionVector.getX(), -0.5, directionVector.getZ()),
                    0,
                    (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.1 + directionVector.getX(),
                    (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.1 + directionVector.getY(),
                    (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.1 + directionVector.getZ(),
                    ThreadLocalRandom.current().nextDouble() + 0.05);
        }
    }

    /**
     * Damage phase
     *
     * @param player
     */
    private void doFlamethrowerPhase2(Player player, Location target) {
        List<Location> damagePoints = generateDamagePoints(player, target);
        new BukkitRunnable() {
            int timer = 0;

            @Override
            public void run() {
                doParticleEffect(player, target, Particle.FLAME);
                doDamage(damagePoints, player);
                timer++;
                if (timer < 20 * 3) return;
                doFlamethrowerPhase3(player, target);
                cancel();
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    private static List<Location> generateDamagePoints(Player player, Location fixedPlayerLocation) {
        List<Location> locations = new ArrayList<>();
        Location eliteMobLocation = player.getLocation().clone();
        Vector toPlayerVector = fixedPlayerLocation.clone().subtract(eliteMobLocation).toVector().normalize().multiply(0.5);
        for (int i = 0; i < 40; i++)
            locations.add(eliteMobLocation.add(toPlayerVector).clone());
        return locations;
    }

    private static void doDamage(List<Location> locations, Player player) {
        for (Location location : locations)
            for (Entity entity : location.getWorld().getNearbyEntities(location, 0.5, 0.5, 0.5))
                if (entity instanceof LivingEntity) {
                    if (entity.equals(player))
                        continue;
                    BossSpecialAttackDamage.dealSpecialDamage(player, (LivingEntity) entity, 1);
                }

    }

    /**
     * Cooldown phase
     *
     * @param player
     */
    private void doFlamethrowerPhase3(Player player, Location fixedPlayerLocation) {
        new BukkitRunnable() {
            int timer = 0;

            @Override
            public void run() {
                timer++;
                doParticleEffect(player, fixedPlayerLocation, Particle.SMOKE_NORMAL);
                if (timer < 20) return;
                cancel();
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }


}
