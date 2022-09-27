package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.WarningMessage;
import com.magmaguy.elitemobs.utils.shapes.Cylinder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

import static com.magmaguy.elitemobs.utils.MapListInterpreter.*;

public class ScriptActions {
    @Getter
    private final List<ScriptAction> scriptActions = new ArrayList();
    //This is used to reference actions when they are called by other actions
    private final EliteScript eliteScript;

    public ScriptActions(List<Map<?, ?>> values, String scriptName, EliteScript eliteScript) {
        this.eliteScript = eliteScript;
        for (Map<?, ?> entry : values) {
            ScriptAction scriptAction = new ScriptAction(entry, scriptName);
            scriptActions.add(scriptAction);
        }
    }

    public void runScripts(EliteEntity eliteEntity, LivingEntity directTarget) {
        for (ScriptAction scriptAction : scriptActions)
            scriptAction.runScript(eliteEntity, directTarget);
    }

    public boolean isValid() {
        return true;
    }

    private enum ActionType {
        TELEPORT,
        MESSAGE,
        POTION_EFFECT,
        EFFECT_ZONE,
        DAMAGE,
        RUN_SCRIPT,
        SET_ON_FIRE,
        VISUAL_FREEZE,
        PLACE_BLOCK,
        RUN_COMMAND_AS_PLAYER,
        RUN_COMMAND_AS_CONSOLE
    }

    private enum Target {
        NEARBY_PLAYERS,
        WORLD_PLAYERS,
        ALL_PLAYERS,
        DIRECT_TARGET,
        SELF,
        LOCATION,
        LOCATIONS
    }

    private enum Filter {
        PLAYER,
        ELITE,
        LIVING
    }

    private enum Shape {
        CYLINDER
    }

    private class ScriptAction {
        String scriptName;
        @Getter
        private ActionType actionType = null;
        private String location;
        private List<String> locations;
        @Getter
        private double range = 20;
        private int duration = 0;
        private Target target;
        private int wait = 0;
        private String message = null;
        private PotionEffectType potionEffectType;
        private int amplifier;
        private Shape shapeEnum = Shape.CYLINDER;
        private double radius = 5;
        private List<String> scripts;
        private List<String> behavior;
        private ScriptConditions scriptConditions = null;
        private int times = -1;
        private int repeatEvery = 0;
        private Filter filter = Filter.LIVING;
        private int height = 1;
        private ScriptParticles scriptParticles = null;
        private double multiplier = 1;
        private boolean track = false;
        private Material material = Material.TARGET;
        private String command = null;

        public ScriptAction(Map<?, ?> entry, String scriptName) {
            this.scriptName = scriptName;
            processMapList(entry);
        }

        private Location getLocation() {
            return ConfigurationLocation.serialize(location);
        }

        private void processMapList(Map<?, ?> entry) {
            for (Map.Entry entrySet : entry.entrySet()) {
                String key = (String) entrySet.getKey();
                processKeyAndValue(key, entrySet.getValue());
            }
        }

        private void processKeyAndValue(String key, Object value) {
            switch (key.toLowerCase()) {
                case "location" -> location = parseString(key, value, scriptName);
                case "message" -> message = parseString(key, value, scriptName);
                case "range" -> range = parseDouble(key, value, scriptName);
                case "duration" -> duration = parseInteger(key, value, scriptName);
                case "wait" -> wait = parseInteger(key, value, scriptName);
                case "amplifier" -> amplifier = parseInteger(key, value, scriptName);
                case "target" -> target = parseEnum(key, value, Target.class, scriptName);
                case "action" -> actionType = parseEnum(key, value, ActionType.class, scriptName);
                case "potioneffecttype" ->
                        potionEffectType = PotionEffectType.getByKey(NamespacedKey.minecraft(((String) value).toLowerCase()));
                case "shape" -> shapeEnum = parseEnum(key, value, Shape.class, scriptName);
                case "radius" -> radius = parseDouble(key, value, scriptName);
                case "scripts" -> scripts = parseStringList(key, value, scriptName);
                case "behavior" -> behavior = parseStringList(key, value, scriptName);
                case "conditions" -> scriptConditions = new ScriptConditions((Map<?, ?>) value, scriptName);
                case "times" -> times = parseInteger(key, value, scriptName);
                case "repeatevery" -> repeatEvery = parseInteger(key, value, scriptName);
                case "filter" -> filter = parseEnum(key, value, Filter.class, scriptName);
                case "height" -> height = parseInteger(key, value, scriptName);
                case "particles" -> scriptParticles = new ScriptParticles((List<Map<?, ?>>) value, scriptName);
                case "multiplier" -> multiplier = parseDouble(key, value, scriptName);
                case "track" -> track = parseBoolean(key, value, scriptName);
                case "locations" -> locations = parseStringList(key, value, scriptName);
                case "material" -> material = parseEnum(key, value, Material.class, scriptName);
                case "command" -> command = parseString(key, value, scriptName);

                default -> new WarningMessage("Failed to read key " + key + " for script " + scriptName);
            }
        }

        public void runScript(EliteEntity eliteEntity, LivingEntity directTarget) {
            //Check if the script conditions of action are met
            if (scriptConditions != null && !scriptConditions.meetsConditions(eliteEntity, directTarget)) return;
            /*
            If the action doesn't track the player location, targetting locations get cached.
            This means that the effects will happen at the location obtained before the wait time elapsed.
            This is important for effects that have visual components for warnings.
            If you want an effect that tracks for a period of time and then stops tracking, make a script that does the
            first part, then make another script that waits for an amount of time and calls another script which does not track.
             */
            List<com.magmaguy.elitemobs.utils.shapes.Shape> shapes = new ArrayList<>();
            if (!track) {
                if (actionType.equals(ActionType.EFFECT_ZONE))
                    //Handle effect zone
                    shapes = getShapes(eliteEntity, directTarget);
            }
            //First wait for allotted amount of time
            List<com.magmaguy.elitemobs.utils.shapes.Shape> finalShapes = shapes;
            new BukkitRunnable() {
                @Override
                public void run() {

                    if (repeatEvery > 0)
                        //if it's a repeating task, run task repeatedly
                        new BukkitRunnable() {
                            int counter = 0;

                            @Override
                            public void run() {
                                counter++;
                                //If it has run for the allotted amount times, stop
                                if (times > 0 && counter > times) {
                                    cancel();
                                    return;
                                }
                                //Otherwise, run the condition
                                runActions(eliteEntity, directTarget, finalShapes);
                            }
                        }.runTaskTimer(MetadataHandler.PLUGIN, 0, repeatEvery);
                    else
                        //If it's not a repeating task, just run it normally
                        runActions(eliteEntity, directTarget, finalShapes);
                }
            }.runTaskLater(MetadataHandler.PLUGIN, wait);
        }

        private void runActions(EliteEntity eliteEntity, LivingEntity directTarget, List<com.magmaguy.elitemobs.utils.shapes.Shape> shapes) {
            //Different actions have completely different behavior
            switch (actionType) {
                case TELEPORT -> runTeleport(eliteEntity, directTarget);
                case MESSAGE -> runMessage(eliteEntity, directTarget);
                case POTION_EFFECT -> runPotionEffect(eliteEntity, directTarget);
                case EFFECT_ZONE -> runEffectZone(eliteEntity, directTarget, shapes);
                case DAMAGE -> runDamage(eliteEntity, directTarget);
                case SET_ON_FIRE -> runSetOnFire(eliteEntity, directTarget);
                case VISUAL_FREEZE -> runVisualFreeze(eliteEntity, directTarget);
                case PLACE_BLOCK -> runPlaceBlock(eliteEntity, directTarget);
                case RUN_COMMAND_AS_PLAYER -> runPlayerCommand(eliteEntity, directTarget);
                case RUN_COMMAND_AS_CONSOLE -> runConsoleCommand(eliteEntity, directTarget);
                default -> {
                    if (!actionType.equals(ActionType.RUN_SCRIPT))
                        new WarningMessage("Failed to determine action type " + actionType + " in script " + scriptName);
                }
            }
            runAdditionalScripts(eliteEntity, directTarget);
        }

        private Collection<? extends LivingEntity> getTargets(EliteEntity eliteEntity, LivingEntity directTarget) {
            Location eliteEntityLocation = eliteEntity.getLocation();
            return switch (target) {
                case ALL_PLAYERS -> Bukkit.getOnlinePlayers();
                case WORLD_PLAYERS -> eliteEntityLocation.getWorld().getPlayers();
                case NEARBY_PLAYERS ->
                        eliteEntityLocation.getWorld().getNearbyEntities(eliteEntityLocation, range, range, range, (entity -> entity.getType() == EntityType.PLAYER)).stream().map(entity -> (Player) entity).collect(Collectors.toSet());
                case DIRECT_TARGET -> Collections.singletonList(directTarget);
                case SELF -> Collections.singletonList(eliteEntity.getLivingEntity());
                default -> new ArrayList<>();
            };
        }

        private Collection<Location> getLocationsFromTargets(EliteEntity eliteEntity, LivingEntity directTarget) {
            Location eliteEntityLocation = eliteEntity.getLocation();
            return switch (target) {
                case ALL_PLAYERS ->
                        Bukkit.getOnlinePlayers().stream().map(Player::getLocation).collect(Collectors.toSet());
                case WORLD_PLAYERS ->
                        eliteEntityLocation.getWorld().getPlayers().stream().map(Player::getLocation).collect(Collectors.toSet());
                case NEARBY_PLAYERS ->
                        eliteEntityLocation.getWorld().getNearbyEntities(eliteEntityLocation, range, range, range, (entity -> entity.getType() == EntityType.PLAYER)).stream().map(Entity::getLocation).collect(Collectors.toSet());
                case DIRECT_TARGET -> Collections.singletonList(directTarget.getLocation());
                case SELF -> Collections.singletonList(eliteEntity.getLocation());
                default -> null;
            };
        }

        /**
         * Obtains the target locations for a script. Some scripts require locations instead of living entities, and this
         * method obtains those locations from the potential targets.
         *
         * @param eliteEntity  Elite Entity running the script
         * @param directTarget Direct target
         * @return Validated location for the script behavior
         */
        private Collection<Location> getTargetLocations(EliteEntity eliteEntity, LivingEntity directTarget) {
            Collection<Location> targetLocations = getLocationsFromTargets(eliteEntity, directTarget);
            if (targetLocations == null) {
                targetLocations = new ArrayList<>();
                switch (target) {
                    case LOCATION:
                        return getValidLocations(eliteEntity, List.of(getLocation()));
                    case LOCATIONS:
                        Collection<Location> finalTargetLocations = targetLocations;
                        this.locations.forEach(string -> finalTargetLocations.add(ConfigurationLocation.serialize(string)));
                        return getValidLocations(eliteEntity, finalTargetLocations);
                }
            } else {
                return getValidLocations(eliteEntity, targetLocations);
            }
            return null;
        }

        /**
         * Checks the validity of a location based on the requirements set by the script conditions
         *
         * @param eliteEntity       Elite Entity running the script
         * @param originalLocations Original set of locations
         * @return Validated locations
         */
        private Collection<Location> getValidLocations(EliteEntity eliteEntity, Collection<Location> originalLocations) {
            if (scriptConditions == null) return originalLocations;
            List<Location> parsedLocations = new ArrayList<>();
            for (Location iteratedLocation : originalLocations)
                if (scriptConditions.meetsConditions(eliteEntity, iteratedLocation))
                    parsedLocations.add(iteratedLocation);
            return parsedLocations;
        }

        private List<com.magmaguy.elitemobs.utils.shapes.Shape> getShapes(EliteEntity eliteEntity, LivingEntity directTarget) {
            List<com.magmaguy.elitemobs.utils.shapes.Shape> shapes = new ArrayList<>();
            getTargets(eliteEntity, directTarget).forEach(instancedTarget -> {
                if (instancedTarget != null) shapes.add(generateShape(instancedTarget.getLocation()));
            });
            return shapes;
        }

        private Collection<? extends LivingEntity> getZoneTargets(Location center) {
            com.magmaguy.elitemobs.utils.shapes.Shape shape = generateShape(center);
            if (shape == null) return new ArrayList<>();
            return getEntitiesInArea(center, shape);
        }

        private Collection<? extends LivingEntity> getZoneTargets(com.magmaguy.elitemobs.utils.shapes.Shape shape) {
            return getEntitiesInArea(shape.getCenter(), shape);
        }

        private Collection<? extends LivingEntity> getEntitiesInArea(Location center, com.magmaguy.elitemobs.utils.shapes.Shape shape) {
            //Get entities in the world
            Collection<? extends LivingEntity> livingEntities;
            if (filter == null) livingEntities = filterByLiving(center);
            else
                switch (filter) {
                    case PLAYER:
                        livingEntities = filterByPlayer(center);
                        break;
                    case ELITE:
                        livingEntities = filterByElite(center);
                        break;
                    case LIVING:
                        livingEntities = filterByLiving(center);
                        break;
                    default:
                        return new ArrayList<>();
                }

            //Check if entities are in the relevant area
            com.magmaguy.elitemobs.utils.shapes.Shape finalShape = shape;
            livingEntities.removeIf(livingEntity -> !finalShape.contains(livingEntity.getLocation()));
            return livingEntities;
        }

        private com.magmaguy.elitemobs.utils.shapes.Shape generateShape(Location center) {
            if (shapeEnum == Shape.CYLINDER) {
                return new Cylinder(center, radius, height);
            }
            return null;
        }

        private Collection<? extends LivingEntity> filterByPlayer(Location center) {
            return center.getWorld().getPlayers();
        }

        private Collection<? extends LivingEntity> filterByElite(Location center) {
            Collection<LivingEntity> entities = new ArrayList<>();
            center.getWorld().getEntities().forEach(entity -> {
                EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(entity);
                if (eliteEntity != null)
                    entities.add((LivingEntity) entity);
            });
            return entities;
        }

        private Collection<? extends LivingEntity> filterByLiving(Location center) {
            Collection<LivingEntity> entities = new ArrayList<>();
            center.getWorld().getEntities().forEach(entity -> {
                if (entity instanceof LivingEntity livingEntity)
                    entities.add(livingEntity);
            });
            return entities;
        }


        private void runTeleport(EliteEntity eliteEntity, LivingEntity directTarget) {
            Location finalLocation = getLocation();
            if (finalLocation == null) {
                new WarningMessage("Failed to get valid location for string " + location + " in script " + scriptName);
                return;
            }
            if (finalLocation.getWorld() == null) {
                if (location.split(",")[0].equalsIgnoreCase("same_as_boss"))
                    finalLocation.setWorld(eliteEntity.getLocation().getWorld());
                else
                    new WarningMessage("Could not determine world for teleport in script " + scriptName);
            }
            getTargets(eliteEntity, directTarget).forEach(iteratedPlayer -> iteratedPlayer.teleport(finalLocation));
        }


        private void runMessage(EliteEntity eliteEntity, LivingEntity directTarget) {
            getTargets(eliteEntity, directTarget).forEach(iteratedTarget -> iteratedTarget.sendMessage(ChatColorConverter.convert(message)));
        }

        private void runPotionEffect(EliteEntity eliteEntity, LivingEntity directTarget) {
            getTargets(eliteEntity, directTarget).forEach(iteratedTarget -> iteratedTarget.addPotionEffect(new PotionEffect(potionEffectType, duration, amplifier)));
        }

        private void runEffectZone(EliteEntity eliteEntity, LivingEntity directTarget, List<com.magmaguy.elitemobs.utils.shapes.Shape> shapes) {
            //Run zone behavior
            if (behavior != null && !behavior.isEmpty())
                if (!shapes.isEmpty()) {
                    shapes.forEach(cachedShape -> getZoneTargets(cachedShape).forEach(livingEntity -> behavior.forEach(key -> eliteScript.eliteScriptMap.get(key).check(eliteEntity, livingEntity))));
                } else
                    getTargetLocations(eliteEntity, directTarget).forEach(location -> getZoneTargets(location).forEach(livingEntity -> behavior.forEach(key -> eliteScript.eliteScriptMap.get(key).check(eliteEntity, livingEntity))));
            //Run zone visuals
            if (scriptParticles != null)
                if (!shapes.isEmpty())
                    shapes.forEach(shape -> scriptParticles.visualize(shape));
                else
                    getTargetLocations(eliteEntity, directTarget).forEach(location -> scriptParticles.visualize(generateShape(location)));
        }

        private void runAdditionalScripts(EliteEntity eliteEntity, LivingEntity directTarget) {
            if (scripts != null)
                scripts.forEach(scriptName -> eliteScript.eliteScriptMap.get(scriptName).check(eliteEntity, directTarget));
        }

        private void runDamage(EliteEntity eliteEntity, LivingEntity directTarget) {
            PlayerDamagedByEliteMobEvent.PlayerDamagedByEliteMobEventFilter.setSpecialMultiplier(multiplier);
            directTarget.damage(multiplier, eliteEntity.getLivingEntity());
        }

        private void runSetOnFire(EliteEntity eliteEntity, LivingEntity directTarget) {
            getTargets(eliteEntity, directTarget).forEach(iteratedTarget -> iteratedTarget.setFireTicks(duration));
        }

        private void runVisualFreeze(EliteEntity eliteEntity, LivingEntity directTarget) {
            getTargets(eliteEntity, directTarget).forEach(iteratedTarget -> iteratedTarget.setFreezeTicks(141));
        }

        private void runPlaceBlock(EliteEntity eliteEntity, LivingEntity livingEntity) {
            getTargetLocations(eliteEntity, livingEntity).forEach(targetLocation -> {
                Block block = targetLocation.getBlock();
                if (duration > 0) {
                    EntityTracker.addTemporaryBlock(block, duration, material);
                } else
                    block.setType(material);
            });
        }

        private void runPlayerCommand(EliteEntity eliteEntity, LivingEntity livingEntity) {
            getTargets(eliteEntity, livingEntity).forEach(targetEntity -> ((Player) targetEntity).performCommand(parseCommand(eliteEntity, (Player) livingEntity)));
        }

        private void runConsoleCommand(EliteEntity eliteEntity, LivingEntity livingEntity) {
            getTargets(eliteEntity, livingEntity).forEach(targetEntity -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parseCommand(eliteEntity, (Player) livingEntity)));
        }

        private String parseCommand(EliteEntity eliteEntity, Player player) {
            return command
                    .replace("$playerName", player.getName())
                    .replace("$playerX", player.getLocation().getX() + "")
                    .replace("$playerY", player.getLocation().getY() + "")
                    .replace("$playerZ", player.getLocation().getZ() + "")
                    .replace("$bossName", eliteEntity.getName())
                    .replace("$bossX", eliteEntity.getLocation().getX() + "")
                    .replace("$bossY", eliteEntity.getLocation().getY() + "")
                    .replace("$bossZ", eliteEntity.getLocation().getZ() + "")
                    .replace("$bossLevel", eliteEntity.getLevel() + "");
        }
    }
}
