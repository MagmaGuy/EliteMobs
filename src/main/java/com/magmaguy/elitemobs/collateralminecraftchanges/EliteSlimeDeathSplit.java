package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardSpawnEventBypasser;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

public class EliteSlimeDeathSplit implements Listener {
    @EventHandler
    public void EliteMobDeathEvent(EliteMobDeathEvent event) {
        if (!event.getEntity().getType().equals(EntityType.SLIME) &&
                !event.getEntity().getType().equals(EntityType.MAGMA_CUBE)) return;
        Slime slime = (Slime) event.getEntity();
        int size = slime.getSize() / 2;
        if (size < 1) return;
        slime.setSize(1);
        for (int i = 0; i < ThreadLocalRandom.current().nextInt(2) + 2; i++) {
            WorldGuardSpawnEventBypasser.forceSpawn();
            Slime newSlime;
            if (event.getEntity().getType() == EntityType.SLIME)
                newSlime = (Slime) slime.getLocation().getWorld().spawnEntity(slime.getLocation(), EntityType.SLIME);
            else
                newSlime = (MagmaCube) slime.getLocation().getWorld().spawnEntity(slime.getLocation(), EntityType.MAGMA_CUBE);
            newSlime.setSize(size);
            EliteEntity eliteEntity = new EliteEntity();
            eliteEntity.setLevel(event.getEliteEntity().getLevel());
            eliteEntity.setLivingEntity(newSlime, CreatureSpawnEvent.SpawnReason.SLIME_SPLIT);
            EliteMobProperties.getPluginData(EntityType.SLIME);
            eliteEntity.setDamageMultiplier(eliteEntity.getDamageMultiplier() / 2D);
            eliteEntity.setHealthMultiplier(eliteEntity.getHealthMultiplier() / 2D);
            eliteEntity.setVanillaLoot(event.getEliteEntity().isVanillaLoot());
            eliteEntity.setEliteLoot(event.getEliteEntity().isEliteLoot());
            eliteEntity.setRandomLoot(event.getEliteEntity().isRandomLoot());

            //refreshes the max health
            eliteEntity.setMaxHealth();
            HashSet<PowersConfigFields> powersConfigFields = new HashSet<>();
            event.getEliteEntity().getElitePowers().forEach(elitePower -> {
                if (elitePower.getPowersConfigFields() instanceof PowersConfigFields powersConfigFields1)
                    powersConfigFields.add(powersConfigFields1);
            });
            eliteEntity.applyPowers(powersConfigFields);
        }
    }
}
