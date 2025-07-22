package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteDamageEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.collateralminecraftchanges.LightningSpawnBypass;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.pathfinding.Navigation;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.powers.meta.CustomSummonPower;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptActionBlueprint;
import com.magmaguy.elitemobs.powers.scripts.enums.ActionType;
import com.magmaguy.magmacore.util.AttributeManager;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles the execution of script actions for EliteMobs.
 * This class interprets the script blueprints and executes the corresponding actions.
 */
public class ScriptAction {

    /**
     * A thread-safe set of players who are currently invulnerable due to scripts.
     */
    @Getter
    private static final Set<Player> invulnerablePlayers = ConcurrentHashMap.newKeySet();

    @Getter
    private final ScriptActionBlueprint blueprint;
    private final ScriptTargets scriptTargets;
    private final ScriptConditions scriptConditions;
    private final ScriptParticles scriptParticles;
    @Getter
    private final Map<String, EliteScript> eliteScriptMap;
    private final EliteScript eliteScript;
    private final ScriptTargets finalScriptTargets;

    /**
     * Constructs a new ScriptAction with the given blueprint, script map, and elite script.
     *
     * @param blueprint      The blueprint defining this action.
     * @param eliteScriptMap Map of script names to EliteScript instances.
     * @param eliteScript    The elite script associated with this action.
     */
    public ScriptAction(ScriptActionBlueprint blueprint, Map<String, EliteScript> eliteScriptMap, EliteScript eliteScript) {
        this.blueprint = blueprint;
        this.scriptTargets = new ScriptTargets(blueprint.getScriptTargets(), eliteScript);
        this.finalScriptTargets = blueprint.getFinalTarget() != null
                ? new ScriptTargets(blueprint.getFinalTarget(), eliteScript)
                : null;
        this.scriptConditions = new ScriptConditions(blueprint.getConditionsBlueprint(), eliteScript, true);
        this.scriptParticles = new ScriptParticles(blueprint.getScriptParticlesBlueprint());
        this.eliteScriptMap = eliteScriptMap;
        this.eliteScript = eliteScript;
    }

    private static final ThreadLocal<Integer> scriptDamageDepth = ThreadLocal.withInitial(() -> 0);

    /**
     * Executes the script action based on the provided EliteEntity, target, and event.
     *
     * @param eliteEntity  The EliteEntity executing the script.
     * @param directTarget The direct target from the event that triggered the script.
     * @param event        The event that caused the script to run.
     */
    public void runScript(EliteEntity eliteEntity, LivingEntity directTarget, Event event) {
        if (blueprint.getActionType() == null) {
            Logger.warn("Script '" + blueprint.getScriptName() + "' in file '" + blueprint.getScriptFilename()
                    + "' does not have a valid action! Every action must define a valid action for the script to work.");
            return;
        }

        ScriptActionData scriptActionData = new ScriptActionData(eliteEntity, directTarget, scriptTargets, eliteScript.getScriptZone(), event);
        scriptTask(scriptActionData);
    }

    /**
     * Executes the script action using data from a previous script action.
     *
     * @param previousScriptActionData The data from the previous script action.
     */
    public void runScript(ScriptActionData previousScriptActionData) {
        if (blueprint.getActionType() == null) {
            Logger.warn("Script '" + blueprint.getScriptName() + "' in file '" + blueprint.getScriptFilename()
                    + "' does not have a valid action! Every action must define a valid action for the script to work.");
            return;
        }

        ScriptActionData scriptActionData = new ScriptActionData(scriptTargets, eliteScript.getScriptZone(), previousScriptActionData);
        scriptTask(scriptActionData);
    }

    /**
     * Executes the script action upon landing, using data from a previous script action.
     *
     * @param previousScriptActionData The data from the previous script action.
     * @param landingLocation          The location where the action should be executed.
     */
    public void runScript(ScriptActionData previousScriptActionData, Location landingLocation) {
        ScriptActionData scriptActionData = new ScriptActionData(scriptTargets, eliteScript.getScriptZone(), previousScriptActionData, landingLocation);
        scriptTask(scriptActionData);
    }

    /**
     * Schedules and executes the script action, handling delays and repeats.
     *
     * @param scriptActionData The data for the script action.
     */
    private void scriptTask(ScriptActionData scriptActionData) {
        scriptTargets.cacheTargets(scriptActionData);
        if (finalScriptTargets != null) {
            finalScriptTargets.cacheTargets(scriptActionData);
        }

        if (blueprint.getWait().getValue() > 0) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    runScriptTask(scriptActionData);
                }
            }.runTaskLater(MetadataHandler.PLUGIN, blueprint.getWait().getValue());
        } else {
            runScriptTask(scriptActionData);
        }
    }

    /**
     * Executes the script action, handling repeating tasks if necessary.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runScriptTask(ScriptActionData scriptActionData) {
        if (blueprint.getRepeatEvery().getValue() > 0) {
            // If it's a repeating task, schedule it accordingly.
            new BukkitRunnable() {
                int counter = 0;

                @Override
                public void run() {
                    counter++;
                    if (blueprint.getConditionsBlueprint() != null
                            && !scriptConditions.meetsActionConditions(scriptActionData)) {
                        cancel();
                        return;
                    }

                    if (blueprint.getTimes().getValue() > 0 && counter > blueprint.getTimes().getValue()) {
                        cancel();
                        return;
                    }

                    if (blueprint.getTimes().getValue() < 0 && !scriptActionData.getEliteEntity().isValid()) {
                        cancel();
                        return;
                    }

                    runActions(scriptActionData);
                }
            }.runTaskTimer(MetadataHandler.PLUGIN, 0, blueprint.getRepeatEvery().getValue());
        } else {
            if (blueprint.getConditionsBlueprint() != null
                    && !scriptConditions.meetsActionConditions(scriptActionData)) {
                return;
            }
            runActions(scriptActionData);
        }
    }

    /**
     * Routes the action type to the corresponding action execution method.
     *
     * @param scriptActionData The data for the current action.
     */
    private void runActions(ScriptActionData scriptActionData) {
        if (blueprint.getActionType() == null) {
            Logger.warn("Failed to determine action type in script '"
                    + blueprint.getScriptName() + "' for file '" + blueprint.getScriptFilename() + "'");
            return;
        }

        switch (blueprint.getActionType()) {
            case TELEPORT -> runTeleport(scriptActionData);
            case MESSAGE -> runMessage(scriptActionData);
            case ACTION_BAR_MESSAGE -> runActionBarMessage(scriptActionData);
            case TITLE_MESSAGE -> runTitleMessage(scriptActionData);
            case BOSS_BAR_MESSAGE -> runBossBarMessage(scriptActionData);
            case POTION_EFFECT -> runPotionEffect(scriptActionData);
            case DAMAGE -> runDamage(scriptActionData);
            case SET_ON_FIRE -> runSetOnFire(scriptActionData);
            case VISUAL_FREEZE -> runVisualFreeze(scriptActionData);
            case PLACE_BLOCK -> runPlaceBlock(scriptActionData);
            case RUN_COMMAND_AS_PLAYER -> runPlayerCommand(scriptActionData);
            case RUN_COMMAND_AS_CONSOLE -> runConsoleCommand(scriptActionData);
            case STRIKE_LIGHTNING -> runStrikeLightning(scriptActionData);
            case SPAWN_PARTICLE -> runSpawnParticle(scriptActionData);
            case SET_MOB_AI -> runSetMobAI(scriptActionData);
            case SET_MOB_AWARE -> runSetMobAware(scriptActionData);
            case PLAY_SOUND -> runPlaySound(scriptActionData);
            case PUSH -> runPush(scriptActionData);
            case SUMMON_REINFORCEMENT -> runSummonReinforcement(scriptActionData);
            case RUN_SCRIPT -> runAdditionalScripts(scriptActionData);
            case SPAWN_FIREWORKS -> runSpawnFireworks(scriptActionData);
            case MAKE_INVULNERABLE -> runMakeInvulnerable(scriptActionData);
            case TAG -> runTag(scriptActionData);
            case UNTAG -> runUntag(scriptActionData);
            case SET_TIME -> runSetTime(scriptActionData);
            case SET_WEATHER -> runSetWeather(scriptActionData);
            case PLAY_ANIMATION -> runPlayAnimation(scriptActionData);
            case SPAWN_FALLING_BLOCK -> runSpawnFallingBlock(scriptActionData);
            case MODIFY_DAMAGE -> runModifyDamage(scriptActionData);
            case SUMMON_ENTITY -> runSummonEntity(scriptActionData);
            case NAVIGATE -> runNavigate(scriptActionData);
            case SCALE -> runScale(scriptActionData);
            case SET_FACING -> setFacing(scriptActionData);
            case HEAL -> runHeal(scriptActionData);
            default -> Logger.warn("Unknown action type '"
                    + blueprint.getActionType() + "' in script '"
                    + blueprint.getScriptName() + "' for file '" + blueprint.getScriptFilename() + "'");
        }

        if (!blueprint.getActionType().equals(ActionType.RUN_SCRIPT)) {
            runAdditionalScripts(scriptActionData);
        }
    }

    /**
     * Retrieves and validates the target living entities based on the script conditions.
     *
     * @param scriptActionData The data for the script action.
     * @return A collection of validated living entities.
     */
    protected Collection<LivingEntity> getTargets(ScriptActionData scriptActionData) {
        Collection<LivingEntity> livingTargets = scriptConditions.validateEntities(scriptActionData, scriptTargets.getTargetEntities(scriptActionData));
        scriptTargets.setAnonymousTargets(new ArrayList<>(livingTargets));
        if (blueprint.isDebug()) {
            livingTargets.forEach(livingTarget -> Logger.showLocation(livingTarget.getLocation()));
        }
        return livingTargets;
    }

    /**
     * Retrieves and validates the target locations based on the script conditions.
     *
     * @param scriptActionData The data for the script action.
     * @return A collection of validated locations.
     */
    protected Collection<Location> getLocationTargets(ScriptActionData scriptActionData) {
        Collection<Location> locationTargets = scriptConditions.validateLocations(scriptActionData, scriptTargets.getTargetLocations(scriptActionData));
        scriptTargets.setAnonymousTargets(new ArrayList<>(locationTargets));
        if (blueprint.isDebug()) {
            locationTargets.forEach(Logger::showLocation);
        }
        return locationTargets;
    }

    /**
     * Retrieves and validates the final target locations based on the script conditions.
     *
     * @param scriptActionData The data for the script action.
     * @return A collection of validated final locations.
     */
    protected Collection<Location> getFinalLocationTargets(ScriptActionData scriptActionData) {
        if (finalScriptTargets == null) {
            return Collections.emptyList();
        }
        Collection<Location> locationTargets = scriptConditions.validateLocations(scriptActionData, finalScriptTargets.getTargetLocations(scriptActionData));
        finalScriptTargets.setAnonymousTargets(new ArrayList<>(locationTargets));
        if (blueprint.isDebug()) {
            locationTargets.forEach(Logger::showLocation);
        }
        return locationTargets;
    }

    /**
     * Teleports targets to the destination specified in the final script targets.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runTeleport(ScriptActionData scriptActionData) {
        Collection<LivingEntity> targets = getTargets(scriptActionData);
        Collection<Location> destinations = getFinalLocationTargets(scriptActionData);

        if (destinations.isEmpty()) {
            Logger.warn("Failed to get teleport destination for script '" + blueprint.getScriptName() + "' because there is no set FinalTarget!");
            return;
        }

        Location destination = destinations.iterator().next();

        targets.forEach(target -> {
            try {
                MatchInstance.MatchInstanceEvents.teleportBypass = true;
                target.teleport(destination);
            } catch (Exception e) {
                Logger.warn("Failed to teleport entity '" + target.getName() + "' in script '" + blueprint.getScriptName() + "': " + e.getMessage());
            } finally {
                MatchInstance.MatchInstanceEvents.teleportBypass = false;
            }
        });
    }

    /**
     * Sends a chat message to the target entities.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runMessage(ScriptActionData scriptActionData) {
        String message = ChatColorConverter.convert(blueprint.getSValue());
        getTargets(scriptActionData).forEach(target -> target.sendMessage(message));
    }

    /**
     * Sends a title message to the target players.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runTitleMessage(ScriptActionData scriptActionData) {
        if (blueprint.getTitle().isEmpty() && blueprint.getSubtitle().isEmpty()) {
            Logger.warn("TITLE_MESSAGE action does not have any titles or subtitles for script '" + blueprint.getScriptName() + "' in file '" + blueprint.getScriptFilename() + "'");
            return;
        }
        getTargets(scriptActionData).forEach(target -> {
            if (target instanceof Player player) {
                player.sendTitle(blueprint.getTitle(), blueprint.getSubtitle(), blueprint.getFadeIn().getValue(), blueprint.getDuration().getValue(), blueprint.getFadeOut().getValue());
            } else {
                Logger.warn("TITLE_MESSAGE actions must target players! Problematic script: '" + blueprint.getScriptName() + "' in file '" + blueprint.getScriptFilename() + "'");
            }
        });
    }

    /**
     * Sends an action bar message to the target players.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runActionBarMessage(ScriptActionData scriptActionData) {
        if (blueprint.getSValue().isEmpty()) {
            Logger.warn("ACTION_BAR_MESSAGE action does not have a sValue for script '" + blueprint.getScriptName() + "' in file '" + blueprint.getScriptFilename() + "'");
            return;
        }
        String message = blueprint.getSValue();
        getTargets(scriptActionData).forEach(target -> {
            if (target instanceof Player player) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
            } else {
                Logger.warn("ACTION_BAR_MESSAGE actions must target players! Problematic script: '" + blueprint.getScriptName() + "' in file '" + blueprint.getScriptFilename() + "'");
            }
        });
    }

    /**
     * Displays a boss bar message to the target players.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runBossBarMessage(ScriptActionData scriptActionData) {
        if (blueprint.getSValue().isEmpty()) {
            Logger.warn("BOSS_BAR_MESSAGE action does not have a valid sValue for script '" + blueprint.getScriptName() + "' in file '" + blueprint.getScriptFilename() + "'");
            return;
        }
        BossBar bossBar = Bukkit.createBossBar(blueprint.getSValue(), blueprint.getBarColor(), blueprint.getBarStyle());
        getTargets(scriptActionData).forEach(target -> {
            if (target instanceof Player player) {
                bossBar.addPlayer(player);
                if (blueprint.getDuration().getValue() > 0) {
                    Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, bossBar::removeAll, blueprint.getDuration().getValue());
                }
            } else {
                Logger.warn("BOSS_BAR_MESSAGE actions must target players! Problematic script: '" + blueprint.getScriptName() + "' in file '" + blueprint.getScriptFilename() + "'");
            }
        });
    }

    /**
     * Applies a potion effect to the target entities.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runPotionEffect(ScriptActionData scriptActionData) {
        PotionEffect effect = new PotionEffect(blueprint.getPotionEffectType(), blueprint.getDuration().getValue(), blueprint.getAmplifier().getValue());
        getTargets(scriptActionData).forEach(target -> {
            if (target.isValid()) {
                target.addPotionEffect(effect);
            }
        });
    }

    /**
     * Runs additional scripts specified in the blueprint.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runAdditionalScripts(ScriptActionData scriptActionData) {
        if (blueprint.getScripts() == null || blueprint.getScripts().isEmpty()) {
            if (blueprint.getActionType().equals(ActionType.RUN_SCRIPT)) {
                Logger.warn("No scripts found to run in RUN_SCRIPT action in script '" + blueprint.getScriptName() + "' in file '" + blueprint.getScriptFilename() + "'");
            }
            return;
        }

        if (blueprint.isOnlyRunOneScript()) {
            String scriptName = blueprint.getScripts().get(ThreadLocalRandom.current().nextInt(blueprint.getScripts().size()));
            EliteScript script = eliteScriptMap.get(scriptName);
            if (script != null) {
                script.check(scriptActionData.getEliteEntity(), scriptActionData.getDirectTarget(), scriptActionData);
            } else {
                Logger.warn("Failed to find script '" + scriptName + "' for script '" + blueprint.getScriptName() + "' in file '" + blueprint.getScriptFilename() + "'");
            }
        } else {
            for (String scriptName : blueprint.getScripts()) {
                EliteScript script = eliteScriptMap.get(scriptName);
                if (script != null) {
                    script.check(scriptActionData.getEliteEntity(), scriptActionData.getDirectTarget(), scriptActionData);
                } else {
                    Logger.warn("Failed to find script '" + scriptName + "' for script '" + blueprint.getScriptName() + "' in file '" + blueprint.getScriptFilename() + "'");
                }
            }
        }
    }
    private static final int MAX_SCRIPT_DAMAGE_DEPTH = 3; // Allow up to 3 levels of script damage

    /**
     * Resets the state of all invulnerable players by removing their invulnerability.
     */
    public static void shutdown() {
        invulnerablePlayers.forEach(player -> player.setInvulnerable(false));
        invulnerablePlayers.clear();
        scriptDamageDepth.remove();
    }

    /**
     * Damages the target entities with depth limiting to prevent infinite recursion.
     */
    private void runDamage(ScriptActionData scriptActionData) {
        // Check current recursion depth
        int currentDepth = scriptDamageDepth.get();
        if (currentDepth >= MAX_SCRIPT_DAMAGE_DEPTH) {
            Logger.warn("Maximum script damage depth (" + MAX_SCRIPT_DAMAGE_DEPTH +
                    ") reached in script '" + blueprint.getScriptName() +
                    "' in file '" + blueprint.getScriptFilename() +
                    "'. Preventing infinite recursion.");
            return;
        }

        double damageAmount = blueprint.getAmount().getValue();
        double multiplier = blueprint.getMultiplier().getValue();

        try {
            // Increment depth before processing
            scriptDamageDepth.set(currentDepth + 1);

            getTargets(scriptActionData).forEach(target -> {
                if (target instanceof Player) {
                    PlayerDamagedByEliteMobEvent.PlayerDamagedByEliteMobEventFilter.setSpecialMultiplier(multiplier);

                    if (scriptActionData.getEliteEntity().getLivingEntity() != null) {
                        target.damage(damageAmount, scriptActionData.getEliteEntity().getLivingEntity());
                    } else {
                        target.damage(damageAmount);
                    }
                } else {
                    if (scriptActionData.getEliteEntity().getLivingEntity() != null) {
                        target.damage(damageAmount, scriptActionData.getEliteEntity().getLivingEntity());
                    } else {
                        target.damage(damageAmount);
                    }
                }
            });
        } finally {
            // Always restore the previous depth
            scriptDamageDepth.set(currentDepth);

            // Clean up ThreadLocal if we're back to base level
            if (currentDepth == 0) {
                scriptDamageDepth.remove();
            }
        }
    }

    /**
     * Sets the target entities on fire for a specified duration.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runSetOnFire(ScriptActionData scriptActionData) {
        int duration = blueprint.getDuration().getValue();
        getTargets(scriptActionData).forEach(target -> target.setFireTicks(duration));
    }

    /**
     * Applies a visual freeze effect to the target entities.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runVisualFreeze(ScriptActionData scriptActionData) {
        int freezeTicks = blueprint.getAmount().getValue().intValue();
        getTargets(scriptActionData).forEach(target -> target.setFreezeTicks(target.getFreezeTicks() + freezeTicks));
    }

    /**
     * Places a block at the target locations, optionally temporarily.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runPlaceBlock(ScriptActionData scriptActionData) {
        getLocationTargets(scriptActionData).forEach(location -> {
            Block block = location.getBlock();
            if (blueprint.getDuration().getValue() > 0) {
                EntityTracker.addTemporaryBlock(block, blueprint.getDuration().getValue(), blueprint.getMaterial());
            } else {
                block.setType(blueprint.getMaterial());
            }
        });
    }

    /**
     * Runs a command as the target players.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runPlayerCommand(ScriptActionData scriptActionData) {
        String command = parseCommand(scriptActionData.getEliteEntity(), blueprint.getSValue());
        getTargets(scriptActionData).forEach(target -> {
            if (target instanceof Player player) {
                player.performCommand(command);
            } else {
                Logger.warn("RUN_COMMAND_AS_PLAYER action must target players! Problematic script: '" + blueprint.getScriptName() + "' in file '" + blueprint.getScriptFilename() + "'");
            }
        });
    }

    /**
     * Runs a command as the console.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runConsoleCommand(ScriptActionData scriptActionData) {
        String command = parseCommand(scriptActionData.getEliteEntity(), blueprint.getSValue());
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    /**
     * Parses command placeholders with actual values.
     *
     * @param eliteEntity     The elite entity executing the command.
     * @param commandTemplate The command template with placeholders.
     * @return The parsed command.
     */
    private String parseCommand(EliteEntity eliteEntity, String commandTemplate) {
        Player player = (eliteEntity.getLivingEntity() instanceof Player) ? (Player) eliteEntity.getLivingEntity() : null;
        String playerName = player != null ? player.getName() : "Unknown";
        Location playerLocation = player != null ? player.getLocation() : new Location(null, 0, 0, 0);

        return commandTemplate
                .replace("$playerName", playerName)
                .replace("$playerX", String.valueOf(playerLocation.getX()))
                .replace("$playerY", String.valueOf(playerLocation.getY()))
                .replace("$playerZ", String.valueOf(playerLocation.getZ()))
                .replace("$bossName", eliteEntity.getName())
                .replace("$bossX", String.valueOf(eliteEntity.getLocation().getX()))
                .replace("$bossY", String.valueOf(eliteEntity.getLocation().getY()))
                .replace("$bossZ", String.valueOf(eliteEntity.getLocation().getZ()))
                .replace("$bossLevel", String.valueOf(eliteEntity.getLevel()))
                .replace("$bossWorldName", eliteEntity.getLocation().getWorld().getName());
    }

    /**
     * Strikes lightning at the target locations, ignoring protections.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runStrikeLightning(ScriptActionData scriptActionData) {
        getLocationTargets(scriptActionData).forEach(LightningSpawnBypass::strikeLightningIgnoreProtections);
    }

    /**
     * Spawns particles at the target locations.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runSpawnParticle(ScriptActionData scriptActionData) {
        boolean needsCentering = switch (scriptActionData.getTargetType()) {
            case ZONE_FULL, ZONE_BORDER, INHERIT_SCRIPT_ZONE_FULL, INHERIT_SCRIPT_ZONE_BORDER, LOCATION, LOCATIONS,
                    LANDING_LOCATION -> true;
            default -> false;
        };
        getLocationTargets(scriptActionData).forEach(location -> {
            Location targetLocation = needsCentering ? location.clone().add(0.5, 0, 0.5) : location;
            scriptParticles.visualize(scriptActionData, targetLocation, eliteScript);
        });
    }

    /**
     * Sets the AI state of the target entities.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runSetMobAI(ScriptActionData scriptActionData) {
        boolean aiEnabled = blueprint.getBValue();
        int duration = blueprint.getDuration().getValue();

        getTargets(scriptActionData).forEach(target -> {
            target.setAI(aiEnabled);
            if (duration > 0) {
                Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> target.setAI(!aiEnabled), duration);
            }
        });
    }

    /**
     * Sets the awareness state of the target mobs.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runSetMobAware(ScriptActionData scriptActionData) {
        boolean aware = blueprint.getBValue();
        int duration = blueprint.getDuration().getValue();

        getTargets(scriptActionData).forEach(target -> {
            if (target instanceof Mob mob) {
                mob.setAware(aware);
                if (duration > 0) {
                    Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> mob.setAware(!aware), duration);
                }
            } else {
                Logger.warn("SET_MOB_AWARE action must target mobs! Problematic script: '" + blueprint.getScriptName() + "' in file '" + blueprint.getScriptFilename() + "'");
            }
        });
    }

    /**
     * Plays a sound at the target locations.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runPlaySound(ScriptActionData scriptActionData) {
        String sound = blueprint.getSValue();
        float volume = blueprint.getVolume().getValue();
        float pitch = blueprint.getPitch().getValue();
        getLocationTargets(scriptActionData).forEach(location -> {
            try {
                location.getWorld().playSound(location, sound, volume, pitch);
            } catch (Exception e) {
                Logger.warn("Failed to play sound '" + sound + "' at location '" + location + "' in script '" + blueprint.getScriptName() + "': " + e.getMessage());
            }
        });
    }

    /**
     * Applies a velocity to the target entities.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runPush(ScriptActionData scriptActionData) {
        Vector velocity = blueprint.getScriptRelativeVectorBlueprint() != null
                ? new ScriptRelativeVector(blueprint.getScriptRelativeVectorBlueprint(), eliteScript, scriptActionData.getEliteEntity().getLocation()).getVector(scriptActionData)
                : blueprint.getVValue();

        // Ensure velocity is finite, otherwise default to zero vector
        if (velocity == null || !isFiniteVector(velocity)) {
            velocity = new Vector(0, 0, 0);
        }

        boolean additive = blueprint.getBValue() != null && blueprint.getBValue();

        // Delay the push by one tick to avoid interference with other events.
        Vector localFinalVelocity = velocity;
        new BukkitRunnable() {
            @Override
            public void run() {
                Vector finalVelocity = localFinalVelocity;
                getTargets(scriptActionData).forEach(target -> {
                    if (additive) {
                        target.setVelocity(target.getVelocity().add(finalVelocity));
                    } else {
                        target.setVelocity(finalVelocity);
                    }
                });
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 1);
    }

    /**
     * Checks if a vector has only finite values.
     *
     * @param vector The vector to check.
     * @return true if all components are finite, false otherwise.
     */
    private boolean isFiniteVector(Vector vector) {
        return Double.isFinite(vector.getX()) && Double.isFinite(vector.getY()) && Double.isFinite(vector.getZ());
    }

    /**
     * Summons reinforcements at the target locations.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runSummonReinforcement(ScriptActionData scriptActionData) {
        getLocationTargets(scriptActionData).forEach(location -> {
            CustomBossEntity customBossEntity = CustomSummonPower.summonReinforcement(scriptActionData.getEliteEntity(), location, blueprint.getSValue(), blueprint.getDuration().getValue());
            if (customBossEntity != null && customBossEntity.getLivingEntity() != null) {
                Vector velocity = blueprint.getScriptRelativeVectorBlueprint() != null
                        ? new ScriptRelativeVector(blueprint.getScriptRelativeVectorBlueprint(), eliteScript, customBossEntity.getLivingEntity().getLocation()).getVector(scriptActionData)
                        : blueprint.getVValue();
                if (velocity != null) {
                    customBossEntity.getLivingEntity().setVelocity(velocity);
                }
            }
        });
    }

    /**
     * Spawns fireworks at the target locations.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runSpawnFireworks(ScriptActionData scriptActionData) {
        if (blueprint.getFireworkEffects().isEmpty()) {
            Logger.warn("No colors set for fireworks in script '" + blueprint.getScriptName() + "' in file '" + blueprint.getScriptFilename() + "'");
            return;
        }

        getLocationTargets(scriptActionData).forEach(location -> {
            try {
                Firework firework = location.getWorld().spawn(location, Firework.class);
                firework.setPersistent(false);
                FireworkMeta fireworkMeta = firework.getFireworkMeta();

                List<FireworkEffect.Type> types = blueprint.getFireworkEffectTypes();
                List<List<Color>> colorsList = blueprint.getFireworkEffects().stream()
                        .map(colors -> colors.stream().map(ScriptActionBlueprint.FireworkColor::getColor).toList())
                        .toList();

                if (types == null || types.isEmpty()) {
                    types = List.of(blueprint.getFireworkEffectType());
                }

                for (int i = 0; i < types.size(); i++) {
                    FireworkEffect.Type type = types.get(i);
                    List<Color> colors = i < colorsList.size() ? colorsList.get(i) : colorsList.get(colorsList.size() - 1);
                    FireworkEffect effect = FireworkEffect.builder()
                            .with(type)
                            .withColor(colors)
                            .flicker(blueprint.isFlicker())
                            .trail(blueprint.isWithTrail())
                            .build();
                    fireworkMeta.addEffect(effect);
                }

                fireworkMeta.setPower(blueprint.getPower().getValue());

                if (blueprint.getVValue() != null) {
                    firework.setVelocity(blueprint.getVValue());
                    firework.setShotAtAngle(true);
                }

                firework.setFireworkMeta(fireworkMeta);
            } catch (Exception e) {
                Logger.warn("Failed to spawn fireworks at location '" + location + "' in script '" + blueprint.getScriptName() + "': " + e.getMessage());
            }
        });
    }

    /**
     * Makes the target entities invulnerable for a specified duration.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runMakeInvulnerable(ScriptActionData scriptActionData) {
        boolean invulnerable = blueprint.isInvulnerable();
        int duration = blueprint.getDuration().getValue();

        getTargets(scriptActionData).forEach(target -> {
            target.setInvulnerable(invulnerable);
            if (target instanceof Player player) {
                if (invulnerable) {
                    invulnerablePlayers.add(player);
                } else {
                    invulnerablePlayers.remove(player);
                }
            }
            if (duration > 0) {
                Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> {
                    target.setInvulnerable(!invulnerable);
                    if (target instanceof Player player) {
                        if (invulnerable) {
                            invulnerablePlayers.remove(player);
                        } else {
                            invulnerablePlayers.add(player);
                        }
                    }
                }, duration);
            }
        });
    }

    /**
     * Adds tags to the target entities and players.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runTag(ScriptActionData scriptActionData) {
        List<String> tags = blueprint.getTags();
        int duration = blueprint.getDuration().getValue();

        getTargets(scriptActionData).forEach(target -> {
            EliteEntity bossEntity = EntityTracker.getEliteMobEntity(target);
            if (bossEntity != null) {
                bossEntity.addTags(tags);
            }
            if (target instanceof Player player) {
                ElitePlayerInventory playerInventory = ElitePlayerInventory.getPlayer(player);
                if (playerInventory != null) {
                    playerInventory.addTags(tags);
                }
            }
            if (duration > 0) {
                Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> {
                    if (bossEntity != null) {
                        bossEntity.removeTags(tags);
                    }
                    if (target instanceof Player player) {
                        ElitePlayerInventory playerInventory = ElitePlayerInventory.getPlayer(player);
                        if (playerInventory != null) {
                            playerInventory.removeTags(tags);
                        }
                    }
                }, duration);
            }
        });
    }

    /**
     * Removes tags from the target entities and players.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runUntag(ScriptActionData scriptActionData) {
        List<String> tags = blueprint.getTags();
        int duration = blueprint.getDuration().getValue();

        getTargets(scriptActionData).forEach(target -> {
            EliteEntity bossEntity = EntityTracker.getEliteMobEntity(target);
            if (bossEntity != null) {
                bossEntity.removeTags(tags);
            }
            if (target instanceof Player player) {
                ElitePlayerInventory playerInventory = ElitePlayerInventory.getPlayer(player);
                if (playerInventory != null) {
                    playerInventory.removeTags(tags);
                }
            }
            if (duration > 0) {
                Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> {
                    if (bossEntity != null) {
                        bossEntity.addTags(tags);
                    }
                    if (target instanceof Player player) {
                        ElitePlayerInventory playerInventory = ElitePlayerInventory.getPlayer(player);
                        if (playerInventory != null) {
                            playerInventory.addTags(tags);
                        }
                    }
                }, duration);
            }
        });
    }

    /**
     * Sets the time in the worlds of the target locations.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runSetTime(ScriptActionData scriptActionData) {
        long time = blueprint.getTime().getValue();
        getLocationTargets(scriptActionData).forEach(location -> {
            try {
                location.getWorld().setTime(time);
            } catch (Exception e) {
                Logger.warn("Failed to set time in world '" + location.getWorld().getName() + "' in script '" + blueprint.getScriptName() + "': " + e.getMessage());
            }
        });
    }

    /**
     * Sets the weather in the worlds of the target entities.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runSetWeather(ScriptActionData scriptActionData) {
        int duration = blueprint.getDuration().getValue();
        getTargets(scriptActionData).forEach(target -> {
            World world = target.getWorld();
            try {
                switch (blueprint.getWeatherType()) {
                    case CLEAR -> {
                        world.setStorm(false);
                        world.setThundering(false);
                        world.setWeatherDuration(duration > 0 ? duration : 6000);
                    }
                    case PRECIPITATION -> {
                        world.setStorm(true);
                        world.setThundering(false);
                        world.setWeatherDuration(duration > 0 ? duration : 6000);
                        if (duration > 0) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    world.setStorm(false);
                                }
                            }.runTaskLater(MetadataHandler.PLUGIN, duration + 1);
                        }
                    }
                    case THUNDER -> {
                        world.setStorm(true);
                        world.setThundering(true);
                        world.setThunderDuration(duration > 0 ? duration : 6000);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                world.setStorm(false);
                                world.setThundering(false);
                            }
                        }.runTaskLater(MetadataHandler.PLUGIN, duration + 1);
                    }
                }
            } catch (Exception e) {
                Logger.warn("Failed to set weather in world '" + world.getName() + "' in script '" + blueprint.getScriptName() + "': " + e.getMessage());
            }
        });
    }

    /**
     * Plays an animation on the target custom boss entities.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runPlayAnimation(ScriptActionData scriptActionData) {
        getTargets(scriptActionData).forEach(target -> {
            EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(target);
            if (eliteEntity instanceof CustomBossEntity customBossEntity && customBossEntity.getCustomModel() != null) {
                customBossEntity.getCustomModel().playAnimationByName(blueprint.getSValue());
            }
        });
    }

    /**
     * Spawns falling blocks at the target locations.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runSpawnFallingBlock(ScriptActionData scriptActionData) {
        getLocationTargets(scriptActionData).forEach(location -> {
            try {
                FallingBlock fallingBlock = location.getWorld().spawnFallingBlock(location, blueprint.getMaterial().createBlockData());
                fallingBlock.setDropItem(false);
                fallingBlock.setHurtEntities(false);

                Vector velocity = blueprint.getScriptRelativeVectorBlueprint() != null
                        ? new ScriptRelativeVector(blueprint.getScriptRelativeVectorBlueprint(), eliteScript, location).getVector(scriptActionData)
                        : blueprint.getVValue();

                if (velocity != null) {
                    fallingBlock.setVelocity(velocity);
                }

                ScriptListener.fallingBlocks.put(fallingBlock, new FallingEntityDataPair(this, scriptActionData));
            } catch (Exception e) {
                Logger.warn("Failed to spawn falling block at location '" + location + "' in script '" + blueprint.getScriptName() + "': " + e.getMessage());
            }
        });
    }

    /**
     * Modifies the damage of an EliteDamageEvent.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runModifyDamage(ScriptActionData scriptActionData) {
        if (scriptActionData.getEvent() instanceof EliteDamageEvent eliteDamageEvent) {
            double newDamage = eliteDamageEvent.getDamage() * blueprint.getMultiplier().getValue();
            eliteDamageEvent.setDamage(newDamage);
        }
    }

    /**
     * Summons entities at the target locations.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runSummonEntity(ScriptActionData scriptActionData) {
        EntityType entityType;
        try {
            entityType = EntityType.valueOf(blueprint.getSValue().toUpperCase());
        } catch (IllegalArgumentException e) {
            Logger.warn("Invalid entity type '" + blueprint.getSValue() + "' in script '" + blueprint.getScriptName() + "' in file '" + blueprint.getScriptFilename() + "'");
            return;
        }

        getLocationTargets(scriptActionData).forEach(location -> {
            Vector velocity = blueprint.getScriptRelativeVectorBlueprint() != null
                    ? new ScriptRelativeVector(blueprint.getScriptRelativeVectorBlueprint(), eliteScript, location).getVector(scriptActionData)
                    : blueprint.getVValue();

            try {
                Entity entity;
                if (scriptActionData.getEliteEntity().getLivingEntity() != null && Projectile.class.isAssignableFrom(entityType.getEntityClass())) {
                    entity = scriptActionData.getEliteEntity().getLivingEntity().launchProjectile(entityType.getEntityClass().asSubclass(Projectile.class), velocity);
                    ((Projectile) entity).setShooter(scriptActionData.getEliteEntity().getLivingEntity());
                    if (entity instanceof Fireball fireball) {
                        fireball.setDirection(velocity);
                    }
                } else {
                    entity = location.getWorld().spawn(location, entityType.getEntityClass());
                    if (velocity != null) {
                        entity.setVelocity(velocity);
                    }
                }

                if (!blueprint.getLandingScripts().isEmpty()) {
                    FallingEntityDataPair dataPair = new FallingEntityDataPair(this, scriptActionData);
                    new BukkitRunnable() {
                        final int maxTicks = 20 * 60 * 5;
                        int counter = 0;

                        @Override
                        public void run() {
                            if (!entity.isValid() || entity.isOnGround() || counter > maxTicks) {
                                ScriptListener.runEvent(dataPair, entity.getLocation());
                                cancel();
                            }
                            counter++;
                        }
                    }.runTaskTimer(MetadataHandler.PLUGIN, 1, 1);
                }
            } catch (Exception e) {
                Logger.warn("Failed to summon entity at location '" + location + "' in script '" + blueprint.getScriptName() + "': " + e.getMessage());
            }
        });
    }

    /**
     * Navigates the target custom boss entities to a destination.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runNavigate(ScriptActionData scriptActionData) {
        Collection<LivingEntity> targets = getTargets(scriptActionData);
        Collection<Location> destinations = getFinalLocationTargets(scriptActionData);

        if (destinations.isEmpty()) {
            Logger.warn("Failed to get navigation destination for script '" + blueprint.getScriptName() + "' because there is no set FinalTarget!");
            return;
        }

        Location destination = destinations.iterator().next();
        double speed = blueprint.getVelocity().getValue();
        boolean avoidObstacles = blueprint.getBValue();
        int duration = blueprint.getDuration().getValue();

        targets.forEach(target -> {
            EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(target);
            if (eliteEntity instanceof CustomBossEntity customBossEntity) {
                Navigation.navigateTo(customBossEntity, speed, destination, avoidObstacles, duration);
            }
        });
    }

    /**
     * Scales the target entities for a specified duration.
     *
     * @param scriptActionData The data for the script action.
     */
    private void runScale(ScriptActionData scriptActionData) {
        double scaleValue = blueprint.getScale().getValue();
        int duration = blueprint.getDuration().getValue();

        getTargets(scriptActionData).forEach(target -> {
            AttributeInstance attribute = AttributeManager.getAttributeInstance(target, "generic_scale");
            if (attribute != null) {
                attribute.setBaseValue(scaleValue);
                if (duration > 0) {
                    Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> attribute.setBaseValue(1.0), duration);
                }
            }
        });
    }

    private void setFacing(ScriptActionData scriptActionData) {
        Vector direction = blueprint.getScriptRelativeVectorBlueprint() != null
                ? new ScriptRelativeVector(blueprint.getScriptRelativeVectorBlueprint(), eliteScript, scriptActionData.getEliteEntity().getLocation()).getVector(scriptActionData)
                : blueprint.getVValue();

        if (direction == null) {
            Logger.warn("Tried to set direction in " + getBlueprint().getScriptFilename() + " but no configuration for vvalue or relative vector are set in " + blueprint.getScriptName() + " !");
            return;
        }

        getTargets(scriptActionData).forEach(target -> {
            Location location = target.getLocation();
            location.setDirection(direction);
            target.teleport(location);
        });
    }


    private void runHeal(ScriptActionData scriptActionData) {
        double healAmount = blueprint.getAmount().getValue();
        getTargets(scriptActionData).forEach(target -> {
            EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(target);
            if (eliteEntity != null) {
                // your existing Elite‐only heal
                eliteEntity.heal(healAmount);
            } else {
                // fallback: clamp using your AttributeManager
                double maxHp = AttributeManager.getAttributeBaseValue(target, "generic_max_health");
                double newHp  = Math.min(target.getHealth() + healAmount, maxHp);
                target.setHealth(newHp);
            }
        });
    }

}
