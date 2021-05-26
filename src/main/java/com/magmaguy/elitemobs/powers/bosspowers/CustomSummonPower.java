package com.magmaguy.elitemobs.powers.bosspowers;

import com.magmaguy.elitemobs.CrashFix;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.EliteMobEnterCombatEvent;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.powers.ElitePower;
import com.magmaguy.elitemobs.powers.specialpowers.EnderCrystalLightningRod;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CustomSummonPower extends ElitePower implements Listener {

    public enum SummonType {
        ONCE,
        ON_HIT,
        ON_COMBAT_ENTER
    }

    private class CustomBossReinforcement {
        private boolean isSummoned = false;
        private SummonType summonType;
        public double summonChance;
        public String bossFileName = null;
        public Vector spawnLocationOffset;
        public EntityType entityType;
        public boolean isLightningRod;

        public CustomBossReinforcement(SummonType summonType, String bossFileName) {
            this.summonType = summonType;
            this.bossFileName = bossFileName;
        }

        public CustomBossReinforcement(SummonType summonType, EntityType entityType, boolean isLightningRod) {
            this.summonType = summonType;
            this.entityType = entityType;
            this.isLightningRod = isLightningRod;
        }

        public void setSummonChance(double summonChance) {
            this.summonChance = summonChance;
        }

        public void setSpawnLocationOffset(Vector vector) {
            this.spawnLocationOffset = vector;
        }
    }

    List<CustomBossReinforcement> customBossReinforcements = new ArrayList<>();

    public CustomSummonPower(List<String> powerStrings) {
        super(PowersConfig.getPower("custom_summon.yml"));
        /*
        valid formats:
        summon:once:filename.yml
        summon:onHit:%:filename.yml
        summon:onCombatEnter:x,y,z:filename.yml
        summon:onCombatEnterPlaceCrystal:x,y,z:boolean
         */
        for (String powerString : powerStrings) {
            if (powerString.split(":")[0].equalsIgnoreCase("summon"))
                if (powerString.split(":")[1].equalsIgnoreCase("once"))
                    parseOnce(powerString);
                else if (powerString.split(":")[1].equalsIgnoreCase("onHit"))
                    parseOnHit(powerString);
                else if (powerString.split(":")[1].equalsIgnoreCase("onCombatEnter"))
                    parseOnCombatEnter(powerString);
                else if (powerString.split(":")[1].equalsIgnoreCase("onCombatEnterPlaceCrystal"))
                    parseOnCombatEnterPlaceCrystal(powerString);
        }

    }

    //summon:once:filename.yml
    private void parseOnce(String powerString) {
        String[] strings = powerString.split(":");
        CustomBossReinforcement customBossReinforcement = new CustomBossReinforcement(SummonType.ONCE, strings[2]);
        if (CustomBossesConfig.getCustomBoss(customBossReinforcement.bossFileName) == null) {
            new WarningMessage("Reinforcement mob " + customBossReinforcement.bossFileName + " is not valid!");
            return;
        }
        customBossReinforcements.add(customBossReinforcement);
    }

    //summon:onHit:%:filename.yml
    private void parseOnHit(String powerString) {
        String[] strings = powerString.split(":");
        CustomBossReinforcement customBossReinforcement = new CustomBossReinforcement(SummonType.ON_HIT, strings[3]);
        customBossReinforcement.setSummonChance(Double.parseDouble(strings[2]));
        if (CustomBossesConfig.getCustomBoss(customBossReinforcement.bossFileName) == null) {
            new WarningMessage("Reinforcement mob " + customBossReinforcement.bossFileName + " is not valid!");
            return;
        }
        customBossReinforcements.add(customBossReinforcement);
    }

    //summon:onCombatEnter:x,y,z:filename.yml
    private void parseOnCombatEnter(String powerString) {
        String[] strings = powerString.split(":");
        CustomBossReinforcement customBossReinforcement = new CustomBossReinforcement(SummonType.ON_COMBAT_ENTER, strings[3]);
        Vector vector = new Vector(
                Double.parseDouble(strings[2].split(",")[0]),
                Double.parseDouble(strings[2].split(",")[1]),
                Double.parseDouble(strings[2].split(",")[2]));
        customBossReinforcement.setSpawnLocationOffset(vector);

        if (CustomBossesConfig.getCustomBoss(customBossReinforcement.bossFileName) == null) {
            new WarningMessage("Reinforcement mob " + customBossReinforcement.bossFileName + " is not valid!");
            return;
        }
        customBossReinforcements.add(customBossReinforcement);
    }

    //summon:onCombatEnterPlaceCrystal:x,y,z:boolean
    private void parseOnCombatEnterPlaceCrystal(String powerString) {
        String[] strings = powerString.split(":");
        CustomBossReinforcement customBossReinforcement = new CustomBossReinforcement(SummonType.ON_COMBAT_ENTER, EntityType.ENDER_CRYSTAL, Boolean.parseBoolean(strings[3]));
        Vector vector = new Vector(
                Double.parseDouble(strings[2].split(",")[0]),
                Double.parseDouble(strings[2].split(",")[1]),
                Double.parseDouble(strings[2].split(",")[2]));
        customBossReinforcement.setSpawnLocationOffset(vector);

        customBossReinforcements.add(customBossReinforcement);
    }

    private void onHitSummonReinforcement(EliteMobEntity spawningEntity) {
        for (CustomBossReinforcement customBossReinforcement : customBossReinforcements) {
            if (customBossReinforcement.summonType.equals(SummonType.ONCE) && !customBossReinforcement.isSummoned) {
                CustomBossEntity customBossEntity = CustomBossEntity.constructCustomBoss(customBossReinforcement.bossFileName, spawningEntity.getLivingEntity().getLocation(), spawningEntity.getLevel());
                customBossReinforcement.isSummoned = true;
                spawningEntity.eliteReinforcementEntities.add(customBossEntity);
            }

            if (customBossReinforcement.summonType.equals(SummonType.ON_HIT) && ThreadLocalRandom.current().nextDouble() < customBossReinforcement.summonChance) {
                CustomBossEntity customBossEntity = CustomBossEntity.constructCustomBoss(customBossReinforcement.bossFileName, spawningEntity.getLivingEntity().getLocation(), spawningEntity.getLevel());
                spawningEntity.eliteReinforcementEntities.add(customBossEntity);
            }
        }
    }

    private void onCombatEnterSummonReinforcement(EliteMobEntity spawningEntity) {
        for (CustomBossReinforcement customBossReinforcement : customBossReinforcements) {
            if (customBossReinforcement.summonType.equals(SummonType.ON_COMBAT_ENTER)) {
                if (customBossReinforcement.bossFileName != null) {
                    Location spawnLocation;

                    if (spawningEntity.regionalBossEntity != null)
                        spawnLocation = spawningEntity.regionalBossEntity.spawnLocation.clone().add(customBossReinforcement.spawnLocationOffset);
                    else
                        spawnLocation = spawningEntity.getLivingEntity().getLocation().clone().add(customBossReinforcement.spawnLocationOffset);
                    CustomBossEntity customBossEntity = CustomBossEntity.constructCustomBoss(customBossReinforcement.bossFileName, spawnLocation, spawningEntity.getLevel());
                    customBossReinforcement.isSummoned = true;
                    spawningEntity.eliteReinforcementEntities.add(customBossEntity);

                } else {

                    Location spawnLocation;

                    if (spawningEntity.regionalBossEntity != null)
                        spawnLocation = spawningEntity.regionalBossEntity.spawnLocation.clone().add(customBossReinforcement.spawnLocationOffset);
                    else
                        spawnLocation = spawningEntity.getLivingEntity().getLocation().clone().add(customBossReinforcement.spawnLocationOffset);

                    Entity entity = spawnLocation.getWorld().spawnEntity(spawnLocation, customBossReinforcement.entityType);
                    entity.getPersistentDataContainer().set(new NamespacedKey(MetadataHandler.PLUGIN, "eliteCrystal"), PersistentDataType.STRING, "eliteCrystal");
                    entity.setPersistent(false);
                    CrashFix.persistentTracker(entity);

                    if (customBossReinforcement.isLightningRod)
                        new EnderCrystalLightningRod(spawningEntity, (EnderCrystal) entity);

                    customBossReinforcement.isSummoned = true;
                    spawningEntity.nonEliteReinforcementEntities.add(entity);
                }
            }
        }
    }


    public static class CustomSummonPowerEvent implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void onHit(EliteMobDamagedByPlayerEvent event) {
            CustomSummonPower customSummonPower = (CustomSummonPower) event.getEliteMobEntity().getPower("custom_summon.yml");
            if (customSummonPower == null) return;
            if (!eventIsValid(event, customSummonPower)) return;
            customSummonPower.onHitSummonReinforcement(event.getEliteMobEntity());
        }

        @EventHandler(ignoreCancelled = true)
        public void onCombatEnter(EliteMobEnterCombatEvent event) {
            CustomSummonPower customSummonPower = (CustomSummonPower) event.getEliteMobEntity().getPower("custom_summon.yml");
            if (customSummonPower == null) return;
            customSummonPower.onCombatEnterSummonReinforcement(event.getEliteMobEntity());
        }
    }

}
