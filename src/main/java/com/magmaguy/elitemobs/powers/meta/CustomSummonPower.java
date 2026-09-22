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
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEscapeMechanism;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.powers.specialpowers.EnderCrystalLightningRod;
import com.magmaguy.elitemobs.utils.DebugMessage;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.magmaguy.elitemobs.utils.MapListInterpreter.*;

public class CustomSummonPower extends ElitePower implements Listener {

    /**
     * Tag for the reinforcement-summoning diagnostic trail. Everything emitted under it goes through
     * {@link DebugMessage}, which is a no-op unless an admin has switched debug on with {@code /em debug}, so normal
     * servers see nothing. Grep a submitted log for this tag to follow one hit from the damage listener all the way to
     * the spawn call: it distinguishes "the ON_HIT listener never fired" from "a specific gate rejected the hit" from
     * "the summon ran and the spawn silently produced no entity".
     */
    private static final String DEBUG_PREFIX = "[CustomSummon] ";
    private final List<CustomBossReinforcement> customBossReinforcements = new ArrayList<>();
    private final CustomBossesConfigFields customBossesConfigFields;

    public CustomSummonPower(Object powerObject, CustomBossesConfigFields customBossesConfigFields) {
        super(PowersConfig.getPower("custom_summon.yml"));
        this.customBossesConfigFields = customBossesConfigFields;
        //This allows an arbitrary amount of reinforcements to be added at any point, class initialization just prepares the stage
        addEntry(powerObject, customBossesConfigFields.getFilename());
    }

    public static CustomBossEntity summonReinforcement(EliteEntity summoningEntity, Location spawnLocation, String reinforcementFilename, int duration) {
        CustomBossesConfigFields fields = CustomBossesConfig.getCustomBoss(reinforcementFilename);
        if (fields == null) {
            Logger.warn("Attempted to summon reinforcement " + reinforcementFilename + " which is not a valid reinforcement!");
            return null;
        }
        CustomBossEntity customBossEntity = new CustomBossEntity(fields);
        customBossEntity.setSummoningEntity(summoningEntity);
        if (summoningEntity instanceof CustomBossEntity summoner && summoner.isNormalizedCombat())
            customBossEntity.setNormalizedCombat();
        summoningEntity.addReinforcement(customBossEntity);
        if (duration > 0) CustomBossEscapeMechanism.startEscapeTicks(duration, customBossEntity);
        customBossEntity.spawn(spawnLocation, true);
        return customBossEntity;
    }

    public static BukkitTask summonGlobalReinforcement(CustomBossReinforcement customBossReinforcement, CustomBossEntity summoningEntity) {
        if (customBossReinforcement.customSpawn == null || customBossReinforcement.customSpawn.isEmpty()) {
            Logger.warn("Reinforcement for boss " + summoningEntity.getCustomBossesConfigFields().getFilename() + " has an incorrectly configured global reinforcement for " + customBossReinforcement.bossFileName);
            return null;
        }
        return new BukkitRunnable() {
            @Override
            public void run() {
                Location source = summoningEntity.getSpawnLocation();
                if (source == null || source.getWorld() == null) return;
                int maximumReinforcements = 30 * source.getWorld().getPlayers().size();
                for (int i = 0; i < customBossReinforcement.amount; i++) {
                    if (summoningEntity.getGlobalReinforcementsCount() >= maximumReinforcements) return;
                    CustomBossEntity customBossEntity = CustomBossEntity.createCustomBossEntity(customBossReinforcement.bossFileName);
                    if (customBossEntity == null) {
                        Logger.warn("Failed to spawn reinforcement because boss " + customBossReinforcement.bossFileName + " was invalid! Does the file exist? Is it configured correctly?");
                        return;
                    }
                    if (summoningEntity.isNormalizedCombat())
                        customBossEntity.setNormalizedCombat();
                    CustomSpawn customSpawn = new CustomSpawn(customBossReinforcement.customSpawn, customBossEntity);
                    //Case if the spawn fails
                    if (customSpawn.getCustomSpawnConfigFields() == null) {
                        customBossEntity.remove(com.magmaguy.elitemobs.api.internal.RemovalReason.REINFORCEMENT_CULL);
                        continue;
                    }
                    customSpawn.setWorld(source.getWorld());
                    summoningEntity.addGlobalReinforcement(customBossEntity);
                    customBossEntity.setSummoningEntity(summoningEntity);
                    customSpawn.queueSpawn();
                    customBossReinforcement.isSummoned = true;
                }
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20L * 10);
    }

    public List<CustomBossReinforcement> getCustomBossReinforcements() {
        return customBossReinforcements;
    }

    public void addEntry(Object powerEntry, String filename) {
        int previousSize = customBossReinforcements.size();
        try {
            if (powerEntry instanceof String legacy) processNewFormat(normalizeLegacy(legacy), filename);
            else if (powerEntry instanceof Map<?, ?> map) processNewFormat((Map<String, ?>) map, filename);
            else throw new IllegalArgumentException("Expected a summon declaration");
        } catch (RuntimeException invalidDefinition) {
            customBossReinforcements.subList(previousSize, customBossReinforcements.size()).clear();
            Logger.warn("Invalid reinforcement in " + filename + ": " + invalidDefinition.getMessage());
        }
    }

    private void processNewFormat(Map<String, ?> map, String configFilename) {
        SummonType summonType = null;
        String filename = null;
        Vector location = null;
        Double chance = 1D;
        boolean lightningRod = false;
        boolean inheritAggro = false;
        boolean inheritLevel = false;
        String customSpawn = "";
        int amount = 1;
        boolean spawnNearby = false;
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            switch (entry.getKey().toLowerCase(Locale.ROOT)) {
                //this just tags it for parsing
                case "summonable":
                    break;
                case "summontype":
                    summonType = parseEnum(entry.getKey(), entry.getValue(), SummonType.class, customBossesConfigFields.getFilename());
                    break;
                case "filename":
                    filename = parseString(entry.getKey(), entry.getValue(), customBossesConfigFields.getFilename());
                    break;
                case "chance":
                    chance = parseDouble(entry.getKey(), entry.getValue(), customBossesConfigFields.getFilename());
                    break;
                case "location":
                    String locationString = parseString(entry.getKey(), entry.getValue(), customBossesConfigFields.getFilename());
                    try {
                        location = new Vector(
                                Double.parseDouble(locationString.split(",")[0]),
                                Double.parseDouble(locationString.split(",")[1]),
                                Double.parseDouble(locationString.split(",")[2]));
                        location.checkFinite();
                    } catch (RuntimeException ex) {
                        throw new IllegalArgumentException("Invalid reinforcement location: " + locationString, ex);
                    }
                    break;
                case "lightningrod":
                    lightningRod = parseBoolean(entry.getKey(), entry.getValue(), customBossesConfigFields.getFilename());
                    break;
                case "inheritaggro":
                    inheritAggro = parseBoolean(entry.getKey(), entry.getValue(), customBossesConfigFields.getFilename());
                    break;
                case "amount":
                    amount = parseInteger(entry.getKey(), entry.getValue(), customBossesConfigFields.getFilename());
                    break;
                case "inheritlevel":
                    inheritLevel = parseBoolean(entry.getKey(), entry.getValue(), customBossesConfigFields.getFilename());
                    break;
                case "spawnnearby":
                    spawnNearby = parseBoolean(entry.getKey(), entry.getValue(), customBossesConfigFields.getFilename());
                    break;
                case "customspawn":
                    if (CustomSpawnConfig.getCustomEvent(parseString(entry.getKey(), entry.getValue(), customBossesConfigFields.getFilename())) == null)
                        Logger.warn("Failed to determine Custom Spawn file for filename " + entry.getValue());
                    else {
                        customSpawn = parseString(entry.getKey(), entry.getValue(), customBossesConfigFields.getFilename());
                    }
                    break;
                case "difficultyid":
                    //Valid key, but it is not consumed here: ElitePowerParser#parsePowers reads difficultyID and
                    //drops the whole reinforcement entry before it ever reaches this parser when the instanced
                    //dungeon's active difficulty doesn't match. Anything that gets here has already passed that
                    //filter, so the key just has to be recognized instead of being reported as invalid.
                    break;
                default:
                    Logger.warn("Invalid boss reinforcement!");
                    Logger.warn("Problematic entry: " + entry.getValue());
            }
        }

        if (summonType == null) {
            Logger.warn("No summon type detected in " + customBossesConfigFields.getFilename() + " ! This reinforcement will not work.");
            return;
        }

        if (chance == null || !Double.isFinite(chance))
            throw new IllegalArgumentException("Invalid summon chance");
        if (summonType != SummonType.ON_COMBAT_ENTER_PLACE_CRYSTAL && (filename == null || filename.isBlank()))
            throw new IllegalArgumentException("Missing reinforcement filename");
        if (summonType == SummonType.ON_COMBAT_ENTER_PLACE_CRYSTAL && location == null)
            throw new IllegalArgumentException("Missing crystal location");

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
                Logger.warn("Failed to determine summon type for reinforcement in " + customBossesConfigFields.getFilename() + " ! Contact the developer with this error!");
        }

        if (summonType != SummonType.ON_COMBAT_ENTER_PLACE_CRYSTAL)
            if (customBossReinforcement == null ||
                    customBossReinforcement.bossFileName == null ||
                    CustomBossesConfig.getCustomBoss(customBossReinforcement.bossFileName) == null) {
                Logger.warn("Could not get filename for reinforcement in file " + configFilename);
                return;
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

    /** Normalize the existing legacy formats without rewriting the author's source during actor construction. */
    private static Map<String, Object> normalizeLegacy(String declaration) {
        String[] parts = declaration.split(":", -1);
        Map<String, Object> fields = new LinkedHashMap<>();
        if (parts[0].equalsIgnoreCase("summon")) {
            if (parts.length < 3) throw new IllegalArgumentException("Incomplete summon declaration");
            switch (parts[1].toLowerCase(Locale.ROOT)) {
                case "once" -> {
                    requireParts(parts, 3);
                    fields.put("summonType", "ONCE");
                    fields.put("filename", parts[2]);
                }
                case "onhit" -> {
                    requireParts(parts, 4);
                    fields.put("summonType", "ON_HIT");
                    fields.put("chance", parts[2]);
                    fields.put("filename", parts[3]);
                }
                case "oncombatenter" -> {
                    requireParts(parts, 4);
                    fields.put("summonType", "ON_COMBAT_ENTER");
                    fields.put("location", parts[2]);
                    fields.put("filename", parts[3]);
                }
                case "oncombatenterplacecrystal" -> {
                    requireParts(parts, 4);
                    fields.put("summonType", "ON_COMBAT_ENTER_PLACE_CRYSTAL");
                    fields.put("location", parts[2]);
                    fields.put("lightningRod", parts[3]);
                }
                default -> throw new IllegalArgumentException("Unknown summon type: " + parts[1]);
            }
        } else if (parts[0].equalsIgnoreCase("summonable")) {
            for (int i = 1; i < parts.length; i++) {
                String[] field = parts[i].split("=", 2);
                if (field.length != 2 || field[1].isBlank())
                    throw new IllegalArgumentException("Incomplete summon field: " + parts[i]);
                String key = field[0].toLowerCase(Locale.ROOT);
                if (fields.putIfAbsent(key, field[1]) != null)
                    throw new IllegalArgumentException("Duplicate summon field: " + key);
            }
        } else throw new IllegalArgumentException("Unknown summon declaration: " + parts[0]);
        return fields;
    }

    private static void requireParts(String[] parts, int expected) {
        if (parts.length != expected || Arrays.stream(parts).anyMatch(String::isBlank))
            throw new IllegalArgumentException("Incomplete or extra summon fields");
    }

    private CustomBossReinforcement doOnce(String filename) {
        CustomBossReinforcement customBossReinforcement = new CustomBossReinforcement(SummonType.ONCE, filename);
        if (CustomBossesConfig.getCustomBoss(customBossReinforcement.bossFileName) == null) {
            Logger.warn("Reinforcement mob " + customBossReinforcement.bossFileName + " is not valid! Filename: " + filename);
            return null;
        }
        customBossReinforcements.add(customBossReinforcement);
        return customBossReinforcement;
    }

    private CustomBossReinforcement doOnHit(String filename, double chance) {
        CustomBossReinforcement customBossReinforcement = new CustomBossReinforcement(SummonType.ON_HIT, filename);
        customBossReinforcement.setSummonChance(chance);
        if (CustomBossesConfig.getCustomBoss(customBossReinforcement.bossFileName) == null) {
            Logger.warn("Reinforcement mob " + customBossReinforcement.bossFileName + " is not valid! Filename: " + filename);
            return customBossReinforcement;
        }
        customBossReinforcements.add(customBossReinforcement);
        return customBossReinforcement;
    }

    private CustomBossReinforcement doOnDeath(String filename) {
        CustomBossReinforcement customBossReinforcement = new CustomBossReinforcement(SummonType.ON_DEATH, filename);
        if (CustomBossesConfig.getCustomBoss(customBossReinforcement.bossFileName) == null) {
            Logger.warn("Reinforcement mob " + customBossReinforcement.bossFileName + " is not valid! Filename: " + filename);
            return null;
        }
        customBossReinforcements.add(customBossReinforcement);
        return customBossReinforcement;
    }

    private CustomBossReinforcement doOnCombatEnter(String filename) {
        CustomBossReinforcement customBossReinforcement = new CustomBossReinforcement(SummonType.ON_COMBAT_ENTER, filename);

        if (CustomBossesConfig.getCustomBoss(customBossReinforcement.bossFileName) == null) {
            Logger.warn("Reinforcement mob " + customBossReinforcement.bossFileName + " is not valid! Filename: " + filename);
            return customBossReinforcement;
        }
        customBossReinforcements.add(customBossReinforcement);
        return customBossReinforcement;
    }

    private CustomBossReinforcement doOnCombatEnterPlaceCrystal(Vector location, boolean lightningRod) {
        CustomBossReinforcement customBossReinforcement = new CustomBossReinforcement(SummonType.ON_COMBAT_ENTER, EntityType.END_CRYSTAL, lightningRod);
        customBossReinforcement.setSpawnLocationOffset(location);
        customBossReinforcements.add(customBossReinforcement);
        return customBossReinforcement;
    }

    private CustomBossReinforcement doGlobalSummonReinforcement(String filename) {
        CustomBossReinforcement customBossReinforcement = new CustomBossReinforcement(SummonType.GLOBAL, filename);
        CustomBossesConfigFields customBossesConfigFields = CustomBossesConfig.getCustomBoss(customBossReinforcement.bossFileName);
        if (customBossesConfigFields == null) {
            Logger.warn("Reinforcement mob " + customBossReinforcement.bossFileName + " is not valid! Filename: " + filename);
            return null;
        }
        customBossReinforcement.entityType = customBossesConfigFields.getEntityType();
        customBossReinforcements.add(customBossReinforcement);
        return customBossReinforcement;
    }

    private void onHitSummonReinforcement(EliteEntity spawningEntity) {
        if (DebugMessage.isAnyDebugEnabled())
            DebugMessage.log(DEBUG_PREFIX + "onHitSummonReinforcement reached for " + customBossesConfigFields.getFilename()
                    + " with " + customBossReinforcements.size() + " configured reinforcement entries");
        for (CustomBossReinforcement customBossReinforcement : customBossReinforcements) {
            //Logged before the dispatch below so isSummoned still reflects the state that decided the outcome
            if (DebugMessage.isAnyDebugEnabled()) {
                if (!customBossReinforcement.summonType.equals(SummonType.ON_HIT)
                        && !customBossReinforcement.summonType.equals(SummonType.ONCE))
                    DebugMessage.log(DEBUG_PREFIX + "skipped entry " + customBossReinforcement.bossFileName
                            + " : summon type is " + customBossReinforcement.summonType + " , not ON_HIT / ONCE");
                else if (customBossReinforcement.summonType.equals(SummonType.ONCE) && customBossReinforcement.isSummoned)
                    DebugMessage.log(DEBUG_PREFIX + "skipped entry " + customBossReinforcement.bossFileName
                            + " : summon type ONCE and it has already been summoned");
            }

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
        if (DebugMessage.isAnyDebugEnabled())
            DebugMessage.log(DEBUG_PREFIX + "summonReinforcement reached for " + customBossesConfigFields.getFilename()
                    + " -> " + customBossReinforcement.bossFileName
                    + " (type=" + customBossReinforcement.summonType
                    + ", amount=" + customBossReinforcement.amount
                    + ", chance=" + customBossReinforcement.summonChance
                    + ", summonerWorld=" + describeWorld(eliteEntity.getLocation()) + ")");
        if (customBossReinforcement.summonChance != null && ThreadLocalRandom.current().nextDouble() > customBossReinforcement.summonChance) {
            if (DebugMessage.isAnyDebugEnabled())
                DebugMessage.log(DEBUG_PREFIX + "aborted: summonChance roll failed for " + customBossReinforcement.bossFileName);
            return;
        }
        for (int i = 0; i < customBossReinforcement.amount; i++) {
            Location spawnLocation = eliteEntity.getLocation();
            if (customBossReinforcement.spawnLocationOffset != null) {
                spawnLocation = getFinalSpawnLocation(eliteEntity, customBossReinforcement.spawnLocationOffset);
            }
            if (customBossReinforcement.spawnNearby)
                for (int loc = 0; loc < 30; loc++) {
                    Location randomLocation = spawnLocation.clone().add(new Vector(
                            ThreadLocalRandom.current().nextInt(-15, 15),
                            0,
                            ThreadLocalRandom.current().nextInt(-15, 15)));
                    int height = CustomSpawn.getHighestValidBlock(randomLocation, randomLocation.getWorld().getMaxHeight() - 2);
                    if (height == Integer.MIN_VALUE) continue;
                    randomLocation.setY(height);
                    spawnLocation = randomLocation;
                    break;
                }

            if (DebugMessage.isAnyDebugEnabled())
                DebugMessage.log(DEBUG_PREFIX + "resolved spawn location for " + customBossReinforcement.bossFileName
                        + " : " + describeLocation(spawnLocation));

            if (CustomBossesConfig.getCustomBoss(customBossReinforcement.bossFileName).isRegionalBoss()) {
                RegionalBossEntity regionalBossEntity = RegionalBossEntity.createTemporaryRegionalBossEntity(customBossReinforcement.bossFileName, spawnLocation);
                if (regionalBossEntity == null) {
                    Logger.warn("Failed to spawn reinforcement for " + eliteEntity.getName() + " because boss " + customBossReinforcement.bossFileName + " was invalid! Does the file exist? Is it configured correctly?");
                    return;
                }
                if (eliteEntity instanceof CustomBossEntity summoner && summoner.isNormalizedCombat())
                    regionalBossEntity.setNormalizedCombat();
                if (customBossReinforcement.inheritLevel)
                    regionalBossEntity.setLevel(eliteEntity.getLevel());
                if (!customBossReinforcement.summonType.equals(SummonType.ON_DEATH))
                    eliteEntity.addReinforcement(regionalBossEntity);
                customBossReinforcement.isSummoned = true;
                regionalBossEntity.setSummoningEntity(eliteEntity);
                if (customBossReinforcement.inheritAggro)
                    regionalBossEntity.inheritAggroFrom(eliteEntity);
                regionalBossEntity.initialize();
                if (DebugMessage.isAnyDebugEnabled())
                    DebugMessage.log(DEBUG_PREFIX + "queued regional reinforcement " + customBossReinforcement.bossFileName
                            + " (regional bosses spawn through a delayed task, so a living entity is not expected yet)");
            } else {
                CustomBossEntity customBossEntity = CustomBossEntity.createCustomBossEntity(customBossReinforcement.bossFileName);
                if (customBossEntity == null) {
                    Logger.warn("Failed to spawn reinforcement for " + eliteEntity.getName() + " because boss " + customBossReinforcement.bossFileName + " was invalid! Does the file exist? Is it configured correctly?");
                    return;
                }
                if (eliteEntity instanceof CustomBossEntity summoner && summoner.isNormalizedCombat())
                    customBossEntity.setNormalizedCombat();
                customBossEntity.setSpawnLocation(spawnLocation);
                customBossEntity.setBypassesProtections(eliteEntity.getBypassesProtections());
                if (customBossReinforcement.inheritLevel)
                    customBossEntity.setLevel(eliteEntity.getLevel());
                customBossEntity.spawn(false);
                //Distinguishes "spawn was never attempted" from "spawn ran and silently produced nothing", which is
                //what a protection plugin, an unloaded chunk or a rejected entity type looks like from out here.
                if (DebugMessage.isAnyDebugEnabled())
                    DebugMessage.log(DEBUG_PREFIX + "spawn() returned for " + customBossReinforcement.bossFileName
                            + " , livingEntity=" + (customBossEntity.getLivingEntity() == null ? "null (spawn failed)" : "present"));
                if (customBossEntity.getLivingEntity() != null)
                    customBossEntity.getLivingEntity().setVelocity(new Vector(ThreadLocalRandom.current().nextDouble(0.2), 0.2, ThreadLocalRandom.current().nextDouble(0.2)));
                if (!customBossReinforcement.summonType.equals(SummonType.ON_DEATH))
                    eliteEntity.addReinforcement(customBossEntity);
                customBossReinforcement.isSummoned = true;
                customBossEntity.setSummoningEntity(eliteEntity);
                if (customBossReinforcement.inheritAggro)
                    customBossEntity.inheritAggroFrom(eliteEntity);
            }
        }
    }

    /**
     * Instanced dungeon worlds are the case this diagnostic exists for, so the world name is always worth printing.
     */
    private static String describeWorld(Location location) {
        if (location == null) return "unknown world (null location)";
        if (location.getWorld() == null) return "unknown world (null world)";
        return location.getWorld().getName();
    }

    private static String describeLocation(Location location) {
        if (location == null) return "null";
        return describeWorld(location) + " " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    private Location getFinalSpawnLocation(EliteEntity summoningEntity, Vector spawnLocationOffset) {
        Location finalSpawnLocation;
        if (summoningEntity instanceof RegionalBossEntity)
            finalSpawnLocation = summoningEntity.getSpawnLocation().add(spawnLocationOffset);
        else if (summoningEntity == null)
            finalSpawnLocation = null;
        else
            finalSpawnLocation = summoningEntity.getLocation().add(spawnLocationOffset);
        return finalSpawnLocation;
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
            if (!eventIsValid(event, customSummonPower, true)) {
                logOnHitGateRejection(event);
                return;
            }
            if (event.getDamage() < 3) {
                if (DebugMessage.isDebugEnabled(event.getPlayer()))
                    logOnHitGate(event, "rejected: damage " + event.getDamage() + " is below the hard-coded 3 damage floor");
                return;
            }
            logOnHitGate(event, "all gates passed, dispatching ON_HIT reinforcements");
            customSummonPower.onHitSummonReinforcement(event.getEliteMobEntity());
        }

        /**
         * Reports which of {@code eventIsValid}'s conditions rejected the hit. The gates are evaluated in the same
         * order as {@link ElitePower#eventIsValid(EliteMobDamagedByPlayerEvent, ElitePower, boolean)} so the reported
         * reason is the one that actually short-circuited, not merely a condition that also happens to be true.
         */
        private static void logOnHitGateRejection(EliteMobDamagedByPlayerEvent event) {
            if (!DebugMessage.isDebugEnabled(event.getPlayer())) return;
            EliteEntity eliteEntity = event.getEliteMobEntity();
            if (event.isCancelled()) {
                logOnHitGate(event, "rejected: the damage event was cancelled");
                return;
            }
            LivingEntity livingEntity = eliteEntity.getLivingEntity();
            if (livingEntity == null) {
                logOnHitGate(event, "rejected: the elite has no living entity");
                return;
            }
            if (!eliteEntity.isAIActive()) {
                logOnHitGate(event, "rejected: the living entity has AI disabled");
                return;
            }
            logOnHitGate(event, "rejected: eventIsValid returned false but no individual gate reproduced it");
        }

        private static void logOnHitGate(EliteMobDamagedByPlayerEvent event, String outcome) {
            if (!DebugMessage.isDebugEnabled(event.getPlayer())) return;
            EliteEntity eliteEntity = event.getEliteMobEntity();
            DebugMessage.log(event.getPlayer(), DEBUG_PREFIX + "ON_HIT gate for "
                    + (eliteEntity instanceof CustomBossEntity customBossEntity
                    ? customBossEntity.getCustomBossesConfigFields().getFilename()
                    : eliteEntity.getName())
                    + " in " + describeWorld(eliteEntity.getLocation()) + " : " + outcome);
        }

        /**
         * The main {@link #onHit} handler is registered with {@code ignoreCancelled = true}, so a cancelled damage
         * event never reaches it and cannot report itself as the reason nothing was summoned. This MONITOR-priority
         * handler sees cancelled events and closes that blind spot, which is the difference between "the listener
         * never fired" and "a gate rejected".
         */
        @EventHandler(priority = EventPriority.MONITOR)
        public void onHitCancellationDiagnostic(EliteMobDamagedByPlayerEvent event) {
            if (!DebugMessage.isAnyDebugEnabled()) return;
            if (!event.isCancelled()) return;
            if (event.getEliteMobEntity().getPower("custom_summon.yml") == null) return;
            logOnHitGate(event, "rejected before the summon listener ran: the damage event was cancelled");
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
