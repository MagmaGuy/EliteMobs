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
import org.bukkit.entity.*;
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
        ON_COMBAT_ENTER,
        ON_COMBAT_ENTER_PLACE_CRYSTAL
    }

    private class CustomBossReinforcement {
        private boolean isSummoned = false;
        private final SummonType summonType;
        public double summonChance;
        public String bossFileName = null;
        public Vector spawnLocationOffset;
        public EntityType entityType;
        public boolean isLightningRod;
        public boolean inheritAggro = false;
        public int amount = 1;
        public boolean inheritLevel = false;
        public boolean spawnNearby = false;

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

    public CustomSummonPower(String powerString) {
        super(PowersConfig.getPower("custom_summon.yml"));
        //This allows an arbitrary amount of reinforcements to be added at any point, class initialization just prepares the stage
        addEntry(powerString);
    }

    public void addEntry(String powerString) {
        /*
        valid formats:
        summonable:
        summonType=once/onHit/onCombatEnter/onCombatEnterPlaceCrystal:
            file=filename.yml:
            chance=double;
            location=x,y,z:
            lightningRod=true/false:
            inheritAggro=true/false:
            amount=int

        summon:once:filename.yml
        summon:onHit:%:filename.yml
        summon:onCombatEnter:x,y,z:filename.yml
        summon:onCombatEnterPlaceCrystal:x,y,z:boolean
         */

        //this is now considered to be legacy
        if (powerString.split(":")[0].equalsIgnoreCase("summon")) {
            if (powerString.split(":")[1].equalsIgnoreCase("once"))
                parseOnce(powerString);
            else if (powerString.split(":")[1].equalsIgnoreCase("onHit"))
                parseOnHit(powerString);
            else if (powerString.split(":")[1].equalsIgnoreCase("onCombatEnter"))
                parseOnCombatEnter(powerString);
            else if (powerString.split(":")[1].equalsIgnoreCase("onCombatEnterPlaceCrystal"))
                parseOnCombatEnterPlaceCrystal(powerString);
        }

        //this is the new recommended format for reinforcements
        if (powerString.split(":")[0].equalsIgnoreCase("summonable")) {
            SummonType summonType = null;
            String filename = null;
            Vector location = null;
            Double chance = null;
            boolean lightningRod = false;
            boolean inheritAggro = false;
            boolean inheritLevel = false;
            int amount = 1;
            boolean spawnNearby = false;
            for (String substring : powerString.split(":")) {
                switch (substring.split("=")[0].toLowerCase()) {
                    //this just tags it for parsing
                    case "summonable":
                        break;
                    case "summontype":
                        try {
                            summonType = SummonType.valueOf(getSubstringField(substring));
                        } catch (Exception ex) {
                            new WarningMessage("Failed to determine summon type from " + getSubstringField(substring));
                        }
                        break;
                    case "filename":
                        try {
                            filename = getSubstringField(substring);
                        } catch (Exception ex) {
                            new WarningMessage("Failed to determine filename from " + getSubstringField(substring));
                        }
                        break;
                    case "chance":
                        try {
                            chance = Double.parseDouble(getSubstringField(substring));
                        } catch (Exception ex) {
                            new WarningMessage("Failed to determine chance from " + getSubstringField(substring));
                        }
                        break;
                    case "location":
                        try {
                            String locationString = getSubstringField(substring);
                            location = new Vector(
                                    Double.parseDouble(locationString.split(",")[0]),
                                    Double.parseDouble(locationString.split(",")[1]),
                                    Double.parseDouble(locationString.split(",")[2]));
                        } catch (Exception ex) {
                            new WarningMessage("Failed to determine location from " + getSubstringField(substring));
                        }
                        break;
                    case "lightningrod":
                        try {
                            lightningRod = Boolean.parseBoolean(getSubstringField(substring));
                        } catch (Exception ex) {
                            new WarningMessage("Failed to determine lightningRod from " + getSubstringField(substring));
                        }
                        break;
                    case "inheritaggro":
                        try {
                            inheritAggro = Boolean.parseBoolean(getSubstringField(substring));
                        } catch (Exception ex) {
                            new WarningMessage("Failed to determine inheritAggro from " + getSubstringField(substring));
                        }
                        break;
                    case "amount":
                        try {
                            amount = Integer.parseInt(getSubstringField(substring));
                        } catch (Exception ex) {
                            new WarningMessage("Failed to determine inheritAggro from " + getSubstringField(substring));
                        }
                        break;
                    case "inheritlevel":
                        try {
                            inheritLevel = Boolean.parseBoolean(getSubstringField(substring));
                        } catch (Exception ex) {
                            new WarningMessage("Failed to determine inheritLevel from " + getSubstringField(substring));
                        }
                        break;
                    case "spawnnearby":
                        try {
                            spawnNearby = Boolean.parseBoolean(getSubstringField(substring));
                        } catch (Exception ex) {
                            new WarningMessage("Failed to determine spawnNearby from " + getSubstringField(substring));
                        }
                        break;
                    default:
                        new WarningMessage("Invalid boss reinforcement string for line " + powerString + " !");
                        new WarningMessage("Problematic entry: " + substring);
                }
            }

            if (summonType == null) {
                new WarningMessage("No summon type detected in " + powerString + " ! This reinforcement will not work.");
                return;
            }

            CustomBossReinforcement customBossReinforcement;
            switch (summonType) {
                case ONCE:
                    customBossReinforcement = doOnce(filename);
                    break;
                case ON_HIT:
                    customBossReinforcement = doOnHit(filename, chance);
                    break;
                case ON_COMBAT_ENTER:
                    customBossReinforcement = doOnCombatEnter(filename, location);
                    break;
                case ON_COMBAT_ENTER_PLACE_CRYSTAL:
                    customBossReinforcement = dOnCombatEnterPlaceCrystal(location, lightningRod);
                    break;
                default:
                    customBossReinforcement = null;
                    new WarningMessage("Failed to determine summon type for reinforcement " + powerString + " ! Contact the developer with this error!");
            }

            if (customBossReinforcement == null)
                return;

            customBossReinforcement.inheritAggro = inheritAggro;
            customBossReinforcement.amount = amount;
            customBossReinforcement.inheritLevel = inheritLevel;
            customBossReinforcement.spawnNearby = spawnNearby;

        }
    }


    private String getSubstringField(String string) {
        return string.split("=")[1];
    }

    //summon:once:filename.yml
    private void parseOnce(String powerString) {
        String[] strings = powerString.split(":");
        doOnce(strings[2]);
    }

    private CustomBossReinforcement doOnce(String filename) {
        CustomBossReinforcement customBossReinforcement = new CustomBossReinforcement(SummonType.ONCE, filename);
        if (CustomBossesConfig.getCustomBoss(customBossReinforcement.bossFileName) == null) {
            new WarningMessage("Reinforcement mob " + customBossReinforcement.bossFileName + " is not valid!");
            return null;
        }
        customBossReinforcements.add(customBossReinforcement);
        return customBossReinforcement;
    }

    //summon:onHit:%:filename.yml
    private void parseOnHit(String powerString) {
        String[] strings = powerString.split(":");
        doOnHit(strings[3], Double.parseDouble(strings[2]));
    }

    private CustomBossReinforcement doOnHit(String filename, double chance) {
        CustomBossReinforcement customBossReinforcement = new CustomBossReinforcement(SummonType.ON_HIT, filename);
        customBossReinforcement.setSummonChance(chance);
        if (CustomBossesConfig.getCustomBoss(customBossReinforcement.bossFileName) == null) {
            new WarningMessage("Reinforcement mob " + customBossReinforcement.bossFileName + " is not valid!");
            return customBossReinforcement;
        }
        customBossReinforcements.add(customBossReinforcement);
        return customBossReinforcement;
    }

    //summon:onCombatEnter:x,y,z:filename.yml
    private void parseOnCombatEnter(String powerString) {
        String[] strings = powerString.split(":");
        doOnCombatEnter(strings[3], new Vector(
                Double.parseDouble(strings[2].split(",")[0]),
                Double.parseDouble(strings[2].split(",")[1]),
                Double.parseDouble(strings[2].split(",")[2])));
    }

    private CustomBossReinforcement doOnCombatEnter(String filename, Vector location) {
        CustomBossReinforcement customBossReinforcement = new CustomBossReinforcement(SummonType.ON_COMBAT_ENTER, filename);
        customBossReinforcement.setSpawnLocationOffset(location);

        if (CustomBossesConfig.getCustomBoss(customBossReinforcement.bossFileName) == null) {
            new WarningMessage("Reinforcement mob " + customBossReinforcement.bossFileName + " is not valid!");
            return customBossReinforcement;
        }
        customBossReinforcements.add(customBossReinforcement);
        return customBossReinforcement;
    }

    //summon:onCombatEnterPlaceCrystal:x,y,z:boolean
    private void parseOnCombatEnterPlaceCrystal(String powerString) {
        String[] strings = powerString.split(":");
        dOnCombatEnterPlaceCrystal(new Vector(
                Double.parseDouble(strings[2].split(",")[0]),
                Double.parseDouble(strings[2].split(",")[1]),
                Double.parseDouble(strings[2].split(",")[2])), Boolean.parseBoolean(strings[3]));
    }

    private CustomBossReinforcement dOnCombatEnterPlaceCrystal(Vector location, boolean lightningRod) {
        CustomBossReinforcement customBossReinforcement = new CustomBossReinforcement(SummonType.ON_COMBAT_ENTER, EntityType.ENDER_CRYSTAL, lightningRod);
        customBossReinforcement.setSpawnLocationOffset(location);
        customBossReinforcements.add(customBossReinforcement);
        return customBossReinforcement;
    }

    private void onHitSummonReinforcement(EliteMobEntity spawningEntity) {
        for (CustomBossReinforcement customBossReinforcement : customBossReinforcements) {
            if (customBossReinforcement.summonType.equals(SummonType.ONCE) && !customBossReinforcement.isSummoned)
                summonReinforcement(spawningEntity, customBossReinforcement);

            if (customBossReinforcement.summonType.equals(SummonType.ON_HIT) && ThreadLocalRandom.current().nextDouble() < customBossReinforcement.summonChance)
                summonReinforcement(spawningEntity, customBossReinforcement);
        }
    }

    private void onCombatEnterSummonReinforcement(EliteMobEntity spawningEntity) {
        for (CustomBossReinforcement customBossReinforcement : customBossReinforcements) {
            if (!customBossReinforcement.summonType.equals(SummonType.ON_COMBAT_ENTER)) continue;
            if (customBossReinforcement.bossFileName != null) {
                summonReinforcement(spawningEntity, customBossReinforcement);
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

                if (entity instanceof Mob && !spawningEntity.getDamagers().isEmpty()) {
                    Player target = null;
                    double damageDealt = 0;
                    for (Player player : spawningEntity.getDamagers().keySet()) {
                        if (spawningEntity.getDamagers().get(player) < damageDealt) continue;
                        target = player;
                        damageDealt = spawningEntity.getDamagers().get(player);
                    }
                    if (target != null)
                        ((Mob) entity).setTarget(target);

                }


                if (customBossReinforcement.isLightningRod)
                    new EnderCrystalLightningRod(spawningEntity, (EnderCrystal) entity);

                customBossReinforcement.isSummoned = true;
                spawningEntity.nonEliteReinforcementEntities.add(entity);
            }
        }

    }

    private void summonReinforcement(EliteMobEntity eliteMobEntity, CustomBossReinforcement customBossReinforcement) {
        for (int i = 0; i < customBossReinforcement.amount; i++) {
            Location spawnLocation = eliteMobEntity.getLivingEntity().getLocation();
            if (customBossReinforcement.spawnLocationOffset != null)
                spawnLocation.clone().add(customBossReinforcement.spawnLocationOffset);
            if (customBossReinforcement.spawnNearby)
                for (int loc = 0; loc < 30; loc++){
                    Location randomLocation = spawnLocation.clone().add(new Vector(
                            ThreadLocalRandom.current().nextInt(-15, 15),
                            0,
                            ThreadLocalRandom.current().nextInt(-15, 15)));
                    randomLocation.setY(randomLocation.getWorld().getHighestBlockAt(randomLocation).getY());
                    if (randomLocation.getY() == -1) continue;
                    spawnLocation = randomLocation;
                    break;
                }

            CustomBossEntity customBossEntity;
            if (customBossReinforcement.inheritLevel)
                customBossEntity = CustomBossEntity.constructCustomBoss(customBossReinforcement.bossFileName, spawnLocation, eliteMobEntity.getLevel());
            else
                customBossEntity = CustomBossEntity.constructCustomBoss(customBossReinforcement.bossFileName, spawnLocation);
            customBossReinforcement.isSummoned = true;
            eliteMobEntity.eliteReinforcementEntities.add(customBossEntity);
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
