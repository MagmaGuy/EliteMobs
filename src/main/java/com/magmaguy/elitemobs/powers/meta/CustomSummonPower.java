package com.magmaguy.elitemobs.powers.meta;

import com.magmaguy.elitemobs.CrashFix;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.EliteMobEnterCombatEvent;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.customspawns.CustomSpawnConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.mobconstructor.CustomSpawn;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.powers.specialpowers.EnderCrystalLightningRod;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CustomSummonPower extends ElitePower implements Listener {

    private List<CustomBossReinforcement> customBossReinforcements = new ArrayList<>();

    public CustomSummonPower(String powerString) {
        super(PowersConfig.getPower("custom_summon.yml"));
        //This allows an arbitrary amount of reinforcements to be added at any point, class initialization just prepares the stage
        addEntry(powerString);
    }

    public static BukkitTask summonGlobalReinforcement(CustomBossReinforcement customBossReinforcement, CustomBossEntity summoningEntity) {
        if (customBossReinforcement.customSpawn == null || customBossReinforcement.customSpawn.isEmpty()) {
            new WarningMessage("Reinforcement for boss " + summoningEntity.getCustomBossesConfigFields().getFilename() + " has an incorrectly configured global reinforcement for " + customBossReinforcement.bossFileName);
            return null;
        }
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (summoningEntity.getGlobalReinforcementsCount() > 30 * summoningEntity.getSpawnLocation().getWorld().getPlayers().size())
                    return;
                for (int i = 0; i < customBossReinforcement.amount; i++) {
                    CustomBossEntity customBossEntity = CustomBossEntity.createCustomBossEntity(customBossReinforcement.bossFileName);
                    if (customBossEntity == null) {
                        new WarningMessage("Failed to spawn reinforcement because boss " + customBossReinforcement.bossFileName + " was invalid! Does the file exist? Is it configured correctly?");
                        return;
                    }
                    CustomSpawn customSpawn = new CustomSpawn(customBossReinforcement.customSpawn, customBossEntity);
                    customSpawn.setWorld(summoningEntity.getSpawnLocation().getWorld());
                    customSpawn.queueSpawn();
                    summoningEntity.addGlobalReinforcement(customBossEntity);
                    customBossReinforcement.isSummoned = true;
                    customBossEntity.setSummoningEntity(summoningEntity);
                }
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20L * 10);
    }

    public List<CustomBossReinforcement> getCustomBossReinforcements() {
        return customBossReinforcements;
    }

    public void addEntry(String powerString) {
        /*
        valid formats:
        summonable:
        summonType=ONCE/ON_HIT/ON_COMBAT_ENTER/ON_COMBAT_ENTER_PLACE_CRYSTAL/GLOBAL/ON_DEATH:
            filename=filename.yml:
            chance=double;
            location=x,y,z:
            lightningRod=true/false:
            inheritAggro=true/false:
            amount=int:
            customSpawn=filename.yml

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
            String customSpawn = "";
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
                    case "customspawn":
                        if (CustomSpawnConfig.customSpawnConfig.getCustomEvent(getSubstringField(substring)) == null)
                            new WarningMessage("Failed to determine Custom Spawn file for filename " + substring);
                        else
                            customSpawn = getSubstringField(substring);
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
                case ON_DEATH:
                    customBossReinforcement = doOnDeath(filename);
                    break;
                case ON_COMBAT_ENTER:
                    customBossReinforcement = doOnCombatEnter(filename);
                    break;
                case ON_COMBAT_ENTER_PLACE_CRYSTAL:
                    customBossReinforcement = doOnCombatEnterPlaceCrystal(location, lightningRod);
                    break;
                case GLOBAL:
                    customBossReinforcement = doGlobalSummonReinforcement(filename);
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
            customBossReinforcement.customSpawn = customSpawn;
            customBossReinforcement.summonChance = chance;
            customBossReinforcement.setSpawnLocationOffset(location);

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

    private CustomBossReinforcement doOnDeath(String filename) {
        CustomBossReinforcement customBossReinforcement = new CustomBossReinforcement(SummonType.ON_DEATH, filename);
        if (CustomBossesConfig.getCustomBoss(customBossReinforcement.bossFileName) == null) {
            new WarningMessage("Reinforcement mob " + customBossReinforcement.bossFileName + " is not valid!");
            return null;
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

    private CustomBossReinforcement doOnCombatEnter(String filename, Vector vector) {
        CustomBossReinforcement customBossReinforcement = new CustomBossReinforcement(SummonType.ON_COMBAT_ENTER, filename);
        customBossReinforcement.setSpawnLocationOffset(vector);
        return doOnCombatEnter(filename);
    }

    private CustomBossReinforcement doOnCombatEnter(String filename) {
        CustomBossReinforcement customBossReinforcement = new CustomBossReinforcement(SummonType.ON_COMBAT_ENTER, filename);

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
        doOnCombatEnterPlaceCrystal(new Vector(
                Double.parseDouble(strings[2].split(",")[0]),
                Double.parseDouble(strings[2].split(",")[1]),
                Double.parseDouble(strings[2].split(",")[2])), Boolean.parseBoolean(strings[3]));
    }

    private CustomBossReinforcement doOnCombatEnterPlaceCrystal(Vector location, boolean lightningRod) {
        CustomBossReinforcement customBossReinforcement = new CustomBossReinforcement(SummonType.ON_COMBAT_ENTER, EntityType.ENDER_CRYSTAL, lightningRod);
        customBossReinforcement.setSpawnLocationOffset(location);
        customBossReinforcements.add(customBossReinforcement);
        return customBossReinforcement;
    }

    private CustomBossReinforcement doGlobalSummonReinforcement(String filename) {
        CustomBossReinforcement customBossReinforcement = new CustomBossReinforcement(SummonType.GLOBAL, filename);
        CustomBossesConfigFields customBossesConfigFields = CustomBossesConfig.getCustomBoss(customBossReinforcement.bossFileName);
        if (customBossesConfigFields == null) {
            new WarningMessage("Reinforcement mob " + customBossReinforcement.bossFileName + " is not valid!");
            return null;
        }
        customBossReinforcement.entityType = customBossesConfigFields.getEntityType();
        customBossReinforcements.add(customBossReinforcement);
        return customBossReinforcement;
    }

    private void onHitSummonReinforcement(EliteEntity spawningEntity) {
        for (CustomBossReinforcement customBossReinforcement : customBossReinforcements) {
            if (customBossReinforcement.summonType.equals(SummonType.ONCE) && !customBossReinforcement.isSummoned)
                summonReinforcement(spawningEntity, customBossReinforcement);

            if (customBossReinforcement.summonType.equals(SummonType.ON_HIT))
                summonReinforcement(spawningEntity, customBossReinforcement);
        }
    }

    private void onCombatEnterSummonReinforcement(EliteEntity spawningEntity) {
        for (CustomBossReinforcement customBossReinforcement : customBossReinforcements) {
            if (!customBossReinforcement.summonType.equals(SummonType.ON_COMBAT_ENTER)) continue;
            if (customBossReinforcement.bossFileName != null) {
                summonReinforcement(spawningEntity, customBossReinforcement);
            } else {

                Location spawnLocation = getFinalSpawnLocation(spawningEntity, customBossReinforcement.spawnLocationOffset);

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
                spawningEntity.addReinforcement(entity);
            }
        }

    }

    private void onDeathSummonReinforcement(EliteEntity spawningEntity) {
        for (CustomBossReinforcement customBossReinforcement : customBossReinforcements)
            if (customBossReinforcement.summonType.equals(SummonType.ON_DEATH))
                summonReinforcement(spawningEntity, customBossReinforcement);
    }

    private void summonReinforcement(EliteEntity eliteEntity, CustomBossReinforcement customBossReinforcement) {
        if (customBossReinforcement.summonChance != null && ThreadLocalRandom.current().nextDouble() > customBossReinforcement.summonChance)
            return;
        for (int i = 0; i < customBossReinforcement.amount; i++) {
            Location spawnLocation = eliteEntity.getLocation();
            if (customBossReinforcement.spawnLocationOffset != null)
                spawnLocation = getFinalSpawnLocation(eliteEntity, customBossReinforcement.spawnLocationOffset);
            if (customBossReinforcement.spawnNearby)
                for (int loc = 0; loc < 30; loc++) {
                    Location randomLocation = spawnLocation.clone().add(new Vector(
                            ThreadLocalRandom.current().nextInt(-15, 15),
                            0,
                            ThreadLocalRandom.current().nextInt(-15, 15)));
                    randomLocation.setY(CustomSpawn.getHighestValidBlock(randomLocation, 256));
                    if (randomLocation.getY() == -100) continue;
                    spawnLocation = randomLocation;
                    break;
                }

            if (CustomBossesConfig.getCustomBoss(customBossReinforcement.bossFileName).isRegionalBoss()) {
                RegionalBossEntity regionalBossEntity = RegionalBossEntity.createTemporaryRegionalBossEntity(customBossReinforcement.bossFileName, spawnLocation);
                if (regionalBossEntity == null) {
                    new WarningMessage("Failed to spawn reinforcement for " + eliteEntity.getName() + " because boss " + customBossReinforcement.bossFileName + " was invalid! Does the file exist? Is it configured correctly?");
                    return;
                }
                if (customBossReinforcement.inheritLevel)
                    regionalBossEntity.setLevel(eliteEntity.getLevel());
                if (!customBossReinforcement.summonType.equals(SummonType.ON_DEATH))
                    eliteEntity.addReinforcement(regionalBossEntity);
                customBossReinforcement.isSummoned = true;
                regionalBossEntity.setSummoningEntity(eliteEntity);
                regionalBossEntity.initialize();
            } else {
                CustomBossEntity customBossEntity = CustomBossEntity.createCustomBossEntity(customBossReinforcement.bossFileName);
                if (customBossEntity == null) {
                    new WarningMessage("Failed to spawn reinforcement for " + eliteEntity.getName() + " because boss " + customBossReinforcement.bossFileName + " was invalid! Does the file exist? Is it configured correctly?");
                    return;
                }
                customBossEntity.setSpawnLocation(spawnLocation);
                customBossEntity.setBypassesProtections(eliteEntity.getBypassesProtections());
                if (customBossReinforcement.inheritLevel)
                    customBossEntity.setLevel(eliteEntity.getLevel());
                customBossEntity.spawn(false);
                if (customBossEntity.getLivingEntity() != null)
                    customBossEntity.getLivingEntity().setVelocity(new Vector(ThreadLocalRandom.current().nextDouble(0.2), 0.2, ThreadLocalRandom.current().nextDouble(0.2)));
                if (!customBossReinforcement.summonType.equals(SummonType.ON_DEATH))
                    eliteEntity.addReinforcement(customBossEntity);
                customBossReinforcement.isSummoned = true;
                customBossEntity.setSummoningEntity(eliteEntity);
            }
        }
    }

    private Location getFinalSpawnLocation(EliteEntity summoningEntity, Vector spawnLocationOffset) {
        Location finalSpawnLocation;
        if (summoningEntity instanceof RegionalBossEntity)
            finalSpawnLocation = ((RegionalBossEntity) summoningEntity).getSpawnLocation().clone().add(spawnLocationOffset);
        else if (summoningEntity == null)
            finalSpawnLocation = null;
        else
            finalSpawnLocation = summoningEntity.getLocation().add(spawnLocationOffset);
        if (summoningEntity instanceof CustomBossEntity && ((CustomBossEntity) summoningEntity).getMinidungeon() != null)
            if (summoningEntity instanceof RegionalBossEntity)
                return ((CustomBossEntity) summoningEntity).getMinidungeon().getRotatedFinalLocation(((RegionalBossEntity) summoningEntity).getSpawnLocation(), spawnLocationOffset);
            else
                return ((CustomBossEntity) summoningEntity).getMinidungeon().getRotatedFinalLocation((summoningEntity).getLocation(), spawnLocationOffset);

        else return finalSpawnLocation;
    }

    public enum SummonType {
        ONCE,
        ON_HIT,
        ON_COMBAT_ENTER,
        ON_DEATH,
        ON_COMBAT_ENTER_PLACE_CRYSTAL,
        GLOBAL
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

        @EventHandler
        public void onDeath(EliteMobDeathEvent event) {
            CustomSummonPower customSummonPower = (CustomSummonPower) event.getEliteEntity().getPower("custom_summon.yml");
            if (customSummonPower == null) return;
            customSummonPower.onDeathSummonReinforcement(event.getEliteEntity());
        }
    }

    public class CustomBossReinforcement {
        public final SummonType summonType;
        public Double summonChance;
        public String bossFileName = null;
        public Vector spawnLocationOffset;
        public EntityType entityType;
        public boolean isLightningRod;
        public boolean inheritAggro = false;
        public int amount = 1;
        public boolean inheritLevel = false;
        public boolean spawnNearby = false;
        public String customSpawn;
        private boolean isSummoned = false;

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

}
