package com.magmaguy.elitemobs.events.actionevents;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardFlagChecker;
import com.magmaguy.elitemobs.config.events.premade.BalrogEventConfig;
import com.magmaguy.elitemobs.events.mobs.sharedeventproperties.ActionDynamicBossLevelConstructor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public class MiningEvent implements Listener {

    @EventHandler
    public void onMine(BlockBreakEvent event) {

        if (event.isCancelled()) return;
        if (!BalrogEventConfig.isEnabled) return;
        if (!EliteMobs.validWorldList.contains(event.getPlayer().getWorld())) return;
        if (EliteMobs.worldguardIsEnabled &&
                !WorldGuardFlagChecker.checkFlag(event.getPlayer().getLocation(), WorldGuardCompatibility.getEliteMobsEventsFlag()))
            return;
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE || event.getPlayer().getGameMode() == GameMode.SPECTATOR)
            return;
        if (!event.getPlayer().hasPermission("elitemobs.events.balrog")) return;
        if (event.getPlayer().getInventory().getItemInMainHand().hasItemMeta() &&
                event.getPlayer().getInventory().getItemInMainHand().getEnchantments().containsKey(Enchantment.SILK_TOUCH))
            return;
        if (!(event.getBlock().getType().equals(Material.DIAMOND_ORE) || event.getBlock().getType().equals(Material.IRON_ORE) ||
                event.getBlock().getType().equals(Material.COAL_ORE) || event.getBlock().getType().equals(Material.REDSTONE_ORE) ||
                event.getBlock().getType().equals(Material.LAPIS_ORE) || event.getBlock().getType().equals(Material.GOLD_ORE)))
            return;
        if (ThreadLocalRandom.current().nextDouble() > BalrogEventConfig.chanceOnMine)
            return;

        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {

                if (i > 20 * 3) {
                    CustomBossEntity.constructCustomBoss("balrog.yml", event.getBlock().getLocation(),
                            ActionDynamicBossLevelConstructor.determineDynamicBossLevel(event.getBlock().getLocation()));
                    cancel();
                    return;
                }

                event.getBlock().getLocation().getWorld().spawnParticle(Particle.SMOKE_LARGE, event.getBlock().getLocation(), 4, 0.1, 0.1, 0.1, 0.05);
                event.getBlock().getLocation().getWorld().spawnParticle(Particle.FLAME, event.getBlock().getLocation(), 2, 0.1, 0.1, 0.1, 0.05);

                i++;

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

}
