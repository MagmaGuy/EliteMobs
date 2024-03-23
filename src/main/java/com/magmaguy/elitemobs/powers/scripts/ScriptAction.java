package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteDamageEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.collateralminecraftchanges.LightningSpawnBypass;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.powers.meta.CustomSummonPower;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptActionBlueprint;
import com.magmaguy.elitemobs.powers.scripts.enums.ActionType;
import com.magmaguy.elitemobs.utils.WarningMessage;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class ScriptAction {

    @Getter
    private final ScriptActionBlueprint blueprint;
    private final ScriptTargets scriptTargets;
    private final ScriptConditions scriptConditions;
    private final ScriptParticles scriptParticles;
    @Getter
    private final Map<String, EliteScript> eliteScriptMap;
    private final EliteScript eliteScript;
    private ScriptTargets finalScriptTargets = null;


    public ScriptAction(ScriptActionBlueprint blueprint, Map<String, EliteScript> eliteScriptMap, EliteScript eliteScript) {
        this.blueprint = blueprint;
        this.scriptTargets = new ScriptTargets(blueprint.getScriptTargets(), eliteScript);
        if (blueprint.getFinalTarget() != null)
            finalScriptTargets = new ScriptTargets(blueprint.getFinalTarget(), eliteScript);
        this.scriptConditions = new ScriptConditions(blueprint.getConditionsBlueprint(), eliteScript, true);
        this.scriptParticles = new ScriptParticles(blueprint.getScriptParticlesBlueprint());
        this.eliteScriptMap = eliteScriptMap;
        this.eliteScript = eliteScript;
    }


    /**
     * Base case, runs based on actions, not called by other scripts
     *
     * @param eliteEntity  EliteEntity that runs the script
     * @param directTarget Direct target from the event that caused the script to run, if any
     * @param event        Event that caused the script to run
     */
    public void runScript(EliteEntity eliteEntity, LivingEntity directTarget, Event event) {
        if (blueprint.getActionType() == null) {
            new WarningMessage("Script " + blueprint.getScriptName() + " in file " + blueprint.getScriptFilename() + " does not have a valid action! Every action must define a valid action for the script to work.");
            return;
        }

        //Create script action data for this run of the script, this data may change on every run
        ScriptActionData scriptActionData = new ScriptActionData(eliteEntity, directTarget, scriptTargets, eliteScript.getScriptZone(), event);
        //Run script task
        scriptTask(scriptActionData);
    }


    /**
     * Called by other scripts, inherits targets from the previous scripts
     *
     * @param previousScriptActionData
     */
    public void runScript(ScriptActionData previousScriptActionData) {
        if (blueprint.getActionType() == null) {
            new WarningMessage("Script " + blueprint.getScriptName() + " in file " + blueprint.getScriptFilename() + " does not have a valid action! Every action must define a valid action for the script to work.");
            return;
        }

        //Create script action data for this run of the script, this data may change on every run
        ScriptActionData scriptActionData = new ScriptActionData(scriptTargets, eliteScript.getScriptZone(), previousScriptActionData);
        //Run script task
        scriptTask(scriptActionData);
    }

    /**
     * Called by scripts that have a landing parameter, i.e. projectiles, fireworks and landing blocks. Only gets called on landing!
     *
     * @param previousScriptActionData The script action data from the script before the landing script
     * @param landingLocation          The "landing" location. Might not necessarily be on a block, more of an end location.
     */
    public void runScript(ScriptActionData previousScriptActionData, Location landingLocation) {
        //This caches the tracking mostly for zones to start at the wait time. This matters if you are making zones
        //that go through a warning phase and then a damage phase.
        ScriptActionData scriptActionData = new ScriptActionData(scriptTargets, eliteScript.getScriptZone(), previousScriptActionData, landingLocation);

        //Run script task
        scriptTask(scriptActionData);
    }

    /**
     * The basic script task, used by all scripts
     *
     * @param scriptActionData The data for the script action
     */
    private void scriptTask(ScriptActionData scriptActionData) {
        //This caches the tracking mostly for zones to start at the wait time. This matters if you are making zones
        //that go through a warning phase and then a damage phase.
        scriptTargets.cacheTargets(scriptActionData);
        //First wait for allotted amount of time
        new BukkitRunnable() {
            @Override
            public void run() {
                if (blueprint.getRepeatEvery() > 0)
                    //if it's a repeating task, run task repeatedly
                    new BukkitRunnable() {
                        int counter = 0;

                        @Override
                        public void run() {
                            counter++;
                            //Check if the script conditions of action are met, since repeating actions might've stopped being valid
                            if (blueprint.getConditionsBlueprint() != null &&
                                    !scriptConditions.meetsActionConditions(scriptActionData)) {
                                cancel();
                                return;
                            }
                            //If it has run for the allotted amount times, stop
                            if (blueprint.getTimes() > 0 && counter > blueprint.getTimes()) {
                                cancel();
                                return;
                            }

                            //If a boss is dead and a script is set to repeat "forever", then the script should end when the boss dies
                            if (blueprint.getTimes() < 0 && !scriptActionData.getEliteEntity().isValid()){
                                cancel();
                                return;
                            }

                            //Otherwise, run the condition
                            runActions(scriptActionData);
                        }
                    }.runTaskTimer(MetadataHandler.PLUGIN, 0, blueprint.getRepeatEvery());
                else {
                    //Check for blocking conditions
                    if (blueprint.getConditionsBlueprint() != null &&
                            !scriptConditions.meetsActionConditions(scriptActionData))
                        return;
                    //If it's not a repeating task, just run it normally
                    runActions(scriptActionData);
                }
            }
        }.runTaskLater(MetadataHandler.PLUGIN, blueprint.getWait());
    }

    /**
     * Routes the action type to the action behavior.
     *
     * @param scriptActionData Data of the current action.
     */
    private void runActions(ScriptActionData scriptActionData) {
        //Different actions have completely different behavior
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
            case STRIKE_LIGHTNING -> runStrikeLighting(scriptActionData);
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
            default ->
                    new WarningMessage("Failed to determine action type " + blueprint.getActionType() + " in script " + blueprint.getScriptName() + " for file " + blueprint.getScriptFilename());
        }
        //Run script will have already run this
        if (!blueprint.getActionType().equals(ActionType.RUN_SCRIPT))
            runAdditionalScripts(scriptActionData);
    }

    protected Collection<LivingEntity> getTargets(ScriptActionData scriptActionData) {
        Collection<LivingEntity> livingTargets = scriptConditions.validateEntities(scriptActionData, scriptTargets.getTargetEntities(scriptActionData));
        scriptTargets.setAnonymousTargets(livingTargets.stream().toList());
        return livingTargets;
    }

    //Gets a list of locations
    protected Collection<Location> getLocationTargets(ScriptActionData scriptActionData) {
        Collection<Location> locationTargets = scriptConditions.validateLocations(scriptActionData, scriptTargets.getTargetLocations(scriptActionData));
        scriptTargets.setAnonymousTargets(locationTargets.stream().toList());
        return locationTargets;
    }

    //Teleports the targets
    private void runTeleport(ScriptActionData scriptActionData) {
        getTargets(scriptActionData).forEach(iteratedTarget -> {
            if (finalScriptTargets == null) {
                new WarningMessage("Failed to get teleport destination for script " + blueprint.getScriptName() + " because there is no set FinalTarget!");
                return;
            }
            List<Location> destinationLocations = new ArrayList<>(finalScriptTargets.getTargetLocations(scriptActionData));
            if (destinationLocations.isEmpty()) return;
            MatchInstance.MatchInstanceEvents.teleportBypass = true;
            iteratedTarget.teleport(destinationLocations.get(0));
        });
    }

    //Sends a message to the targets
    private void runMessage(ScriptActionData scriptActionData) {
        getTargets(scriptActionData).forEach(iteratedTarget -> iteratedTarget.sendMessage(ChatColorConverter.convert(blueprint.getSValue())));
    }

    private void runTitleMessage(ScriptActionData scriptActionData) {
        if (blueprint.getTitle().isEmpty() && blueprint.getSubtitle().isEmpty()) {
            new WarningMessage("TITLE_MESSAGE action does not have any titles or subtitles for script " + blueprint.getScriptName() + " in file " + blueprint.getScriptFilename());
            return;
        }
        getTargets(scriptActionData).forEach(iteratedTarget -> {
            if (!(iteratedTarget instanceof Player)) {
                new WarningMessage("TITLE_MESSAGE actions must target players! Problematic script: " + blueprint.getScriptFilename() + " in " + blueprint.getScriptFilename());
                return;
            }
            ((Player) iteratedTarget).sendTitle(blueprint.getTitle(), blueprint.getSubtitle(), blueprint.getFadeIn(), blueprint.getDuration(), blueprint.getFadeOut());
        });
    }

    private void runActionBarMessage(ScriptActionData scriptActionData) {
        if (blueprint.getSValue().isEmpty()) {
            new WarningMessage("ACTION_BAR_MESSAGE action does not have a sValue for script " + blueprint.getScriptName() + " in file " + blueprint.getScriptFilename());
            return;
        }
        getTargets(scriptActionData).forEach(iteratedTarget -> {
            if (!(iteratedTarget instanceof Player)) {
                new WarningMessage("ACTION_BAR_MESSAGE actions must target players! Problematic script: " + blueprint.getScriptFilename() + " in " + blueprint.getScriptFilename());
                return;
            }
            ((Player) iteratedTarget).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(blueprint.getSValue()));
        });
    }

    private void runBossBarMessage(ScriptActionData scriptActionData) {
        if (blueprint.getSValue().isEmpty()) {
            new WarningMessage("BOSS_BAR_MESSAGE action does not have a valid sValue for script " + blueprint.getScriptFilename() + " in file " + blueprint.getScriptFilename());
            return;
        }
        BossBar bossBar = Bukkit.createBossBar(blueprint.getSValue(), blueprint.getBarColor(), blueprint.getBarStyle());
        getTargets(scriptActionData).forEach(iteratedTarget -> {
            if (!(iteratedTarget instanceof Player)) {
                new WarningMessage("BOSS_BAR_MESSAGE actions must target players! Problematic script: " + blueprint.getScriptFilename() + " in " + blueprint.getScriptFilename());
                return;
            }
            bossBar.addPlayer((Player) iteratedTarget);
            if (blueprint.getDuration() > 0)
                Bukkit.getScheduler().scheduleSyncDelayedTask(MetadataHandler.PLUGIN, bossBar::removeAll, blueprint.getDuration());
        });
    }

    //Applies a potion effect to the target living entity
    private void runPotionEffect(ScriptActionData scriptActionData) {
        getTargets(scriptActionData).forEach(iteratedTarget -> iteratedTarget.addPotionEffect(new PotionEffect(blueprint.getPotionEffectType(), blueprint.getDuration(), blueprint.getAmplifier())));
    }

    //Runs any scripts in the scripts field. Respects wait time and repeating tasks
    private void runAdditionalScripts(ScriptActionData scriptActionData) {
        if (blueprint.getActionType().equals(ActionType.RUN_SCRIPT) && blueprint.getScripts().isEmpty())
            new WarningMessage("Did not find any scripts for action RUN_SCRIPT in script " + blueprint.getScriptName() + " in file " + blueprint.getScriptFilename());
        //This is a bit of a dirty hack but if there are no targets and an action called scripts then it is assumed that the script did not meet the conditions required to run and therefore additional scripts will also not run
        /*
        if (!blueprint.getActionType().equals(ActionType.RUN_SCRIPT) &&
                (scriptActionData.getScriptTargets().getAnonymousTargets() == null ||
                        scriptActionData.getScriptTargets().getAnonymousTargets().isEmpty()))
            return;

         */
        if (blueprint.getScripts() != null)
            if (!blueprint.isOnlyRunOneScript())
                blueprint.getScripts().forEach(iteratedScriptName -> {
                    EliteScript iteratedScript = eliteScriptMap.get(iteratedScriptName);
                    if (iteratedScript == null)
                        new WarningMessage("Failed to get script " + iteratedScriptName + " for script " + blueprint.getScriptName() + " in file " + blueprint.getScriptFilename());
                    else {
                        iteratedScript.check(scriptActionData.getEliteEntity(), scriptActionData.getDirectTarget(), scriptActionData);
                    }
                });
            else {
                String scriptName = blueprint.getScripts().get(ThreadLocalRandom.current().nextInt(blueprint.getScripts().size()));
                EliteScript randomizedScript = eliteScriptMap.get(scriptName);
                if (randomizedScript == null)
                    new WarningMessage("Failed to get script " + scriptName + " for script " + blueprint.getScriptName() + " in file " + blueprint.getScriptFilename());
                else
                    randomizedScript.check(scriptActionData.getEliteEntity(), scriptActionData.getDirectTarget(), scriptActionData);
            }
    }

    //Damages the target living entity
    private void runDamage(ScriptActionData scriptActionData) {
        getTargets(scriptActionData).forEach(targetEntity -> {
            if (targetEntity instanceof Player)
                PlayerDamagedByEliteMobEvent.PlayerDamagedByEliteMobEventFilter.setSpecialMultiplier(blueprint.getMultiplier());
            if (scriptActionData.getEliteEntity().getLivingEntity() != null)
                targetEntity.damage(blueprint.getAmount(), scriptActionData.getEliteEntity().getLivingEntity());
            else
                targetEntity.damage(blueprint.getAmount());
        });
    }

    //Sets a target on fire for the duration
    private void runSetOnFire(ScriptActionData scriptActionData) {
        getTargets(scriptActionData).forEach(iteratedTarget -> iteratedTarget.setFireTicks(blueprint.getDuration()));
    }

    //Applies the freeze visual effect from Minecraft. Requires a repeating task to keep reapplying
    private void runVisualFreeze(ScriptActionData scriptActionData) {
        if (VersionChecker.serverVersionOlderThan(17, 0)) return;
        getTargets(scriptActionData).forEach(iteratedTarget -> iteratedTarget.setFreezeTicks((int) (iteratedTarget.getFreezeTicks() + blueprint.getAmount())));
    }

    //Places a block, temporarily if the duration is defined
    private void runPlaceBlock(ScriptActionData scriptActionData) {
        getLocationTargets(scriptActionData).forEach(targetLocation -> {
            Block block = targetLocation.getBlock();
            if (blueprint.getDuration() > 0) {
                EntityTracker.addTemporaryBlock(block, blueprint.getDuration(), blueprint.getMaterial());
            } else
                block.setType(blueprint.getMaterial());
        });
    }

    //Runs a command as player
    private void runPlayerCommand(ScriptActionData scriptActionData) {
        getTargets(scriptActionData).forEach(targetEntity -> ((Player) targetEntity).performCommand(parseCommand(scriptActionData.getEliteEntity(), (Player) targetEntity)));
    }

    //Runs a command as console
    private void runConsoleCommand(ScriptActionData scriptActionData) {
        getTargets(scriptActionData).forEach(targetEntity -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parseCommand(scriptActionData.getEliteEntity(), (Player) targetEntity)));
    }

    //Command placeholders
    private String parseCommand(EliteEntity eliteEntity, Player player) {
        return blueprint.getSValue()
                .replace("$playerName", player.getName())
                .replace("$playerX", player.getLocation().getX() + "")
                .replace("$playerY", player.getLocation().getY() + "")
                .replace("$playerZ", player.getLocation().getZ() + "")
                .replace("$bossName", eliteEntity.getName())
                .replace("$bossX", eliteEntity.getLocation().getX() + "")
                .replace("$bossY", eliteEntity.getLocation().getY() + "")
                .replace("$bossZ", eliteEntity.getLocation().getZ() + "")
                .replace("$bossLevel", eliteEntity.getLevel() + "")
                .replace("$bossWorldName", eliteEntity.getLocation().getWorld().getName());
    }

    //Strikes visual lightning at the target location
    private void runStrikeLighting(ScriptActionData scriptActionData) {
        getLocationTargets(scriptActionData).forEach(LightningSpawnBypass::strikeLightingIgnoreProtections);
    }

    //Spawns a particle at the target location
    private void runSpawnParticle(ScriptActionData scriptActionData) {
        getLocationTargets(scriptActionData).forEach(targetLocation -> scriptParticles.visualize(scriptActionData, targetLocation, eliteScript));
    }

    //Sets mob AI
    private void runSetMobAI(ScriptActionData scriptActionData) {
        getTargets(scriptActionData).forEach(targetEntity -> {
            targetEntity.setAI(blueprint.getBValue());
            if (blueprint.getDuration() > 0)
                Bukkit.getScheduler().scheduleSyncDelayedTask(MetadataHandler.PLUGIN, () -> targetEntity.setAI(!blueprint.getBValue()), blueprint.getDuration());
        });
    }

    //Sets mob awareness
    private void runSetMobAware(ScriptActionData scriptActionData) {
        getTargets(scriptActionData).forEach(targetEntity -> {
            ((Mob) targetEntity).setAware(blueprint.getBValue());
            if (blueprint.getDuration() > 0)
                Bukkit.getScheduler().scheduleSyncDelayedTask(MetadataHandler.PLUGIN, () -> ((Mob) targetEntity).setAware(!blueprint.getBValue()), blueprint.getDuration());
        });

    }

    //Plays a sound at the target location
    private void runPlaySound(ScriptActionData scriptActionData) {
        getLocationTargets(scriptActionData).forEach(targetLocation -> targetLocation.getWorld().playSound(targetLocation, blueprint.getSValue(), blueprint.getVolume(), blueprint.getPitch()));
    }

    private void runPush(ScriptActionData scriptActionData) {
        getTargets(scriptActionData).forEach(targetEntity -> {
            if (blueprint.getScriptRelativeVectorBlueprint() != null) {
                if (blueprint.getBValue() != null && blueprint.getBValue())
                    targetEntity.setVelocity(targetEntity.getVelocity().add(new ScriptRelativeVector(blueprint.getScriptRelativeVectorBlueprint(), eliteScript, targetEntity.getLocation()).getVector(scriptActionData)));
                else
                    targetEntity.setVelocity(new ScriptRelativeVector(blueprint.getScriptRelativeVectorBlueprint(), eliteScript, targetEntity.getLocation()).getVector(scriptActionData));
            } else if (blueprint.getVValue() != null) {
                if (blueprint.getBValue() != null && blueprint.getBValue())
                    targetEntity.setVelocity(targetEntity.getVelocity().add(blueprint.getVValue()));
                else
                    targetEntity.setVelocity(blueprint.getVValue());
            }
        });
    }

    private void runSummonReinforcement(ScriptActionData scriptActionData) {
        getLocationTargets(scriptActionData).forEach(targetLocation -> CustomSummonPower.summonReinforcement(scriptActionData.getEliteEntity(), targetLocation, blueprint.getSValue(), blueprint.getDuration()));
    }

    private void runSpawnFireworks(ScriptActionData scriptActionData) {
        getLocationTargets(scriptActionData).forEach(targetLocation -> {
            Firework firework = targetLocation.getWorld().spawn(targetLocation, Firework.class);
            firework.setPersistent(false);
            FireworkMeta fireworkMeta = firework.getFireworkMeta();

            if (blueprint.getFireworkEffects().isEmpty()) {
                new WarningMessage("Tried to spawn fireworks for script " + eliteScript.getFileName() + " but no color for the fireworks was set! This part of the script will not run.");
                return;
            }

            if (blueprint.getFireworkEffectTypes() == null) {
                List<Color> colors = blueprint.getFireworkEffects().get(0).stream().map(ScriptActionBlueprint.FireworkColor::getColor).toList();
                //Single effect
                fireworkMeta.addEffect(FireworkEffect.builder()
                        .with(blueprint.getFireworkEffectType())
                        .withColor(colors)
                        .flicker(blueprint.isFlicker())
                        .trail(blueprint.isWithTrail())
                        .build());


            } else {
                int counter = 0;
                //Multiple effects
                for (FireworkEffect.Type fireworkEffectType : blueprint.getFireworkEffectTypes()) {
                    List<Color> colors;
                    if (blueprint.getFireworkEffects().size() - 1 >= counter)
                        colors = blueprint.getFireworkEffects().get(counter)
                                .stream().map(ScriptActionBlueprint.FireworkColor::getColor).toList();
                    else
                        colors = blueprint.getFireworkEffects().get(blueprint.getFireworkEffects().size() - 1)
                                .stream().map(ScriptActionBlueprint.FireworkColor::getColor).toList();

                    fireworkMeta.addEffect(FireworkEffect.builder()
                            .with(fireworkEffectType)
                            .withColor(colors)
                            .flicker(blueprint.isFlicker())
                            .trail(blueprint.isWithTrail())
                            .build());
                    counter++;
                }
            }

            fireworkMeta.setPower(blueprint.getPower());
            if (blueprint.getVValue() != null) {
                firework.setVelocity(blueprint.getVValue());
                firework.setShotAtAngle(true);
            }
            firework.setFireworkMeta(fireworkMeta);
        });
    }

    private void runMakeInvulnerable(ScriptActionData scriptActionData) {
        getTargets(scriptActionData).forEach(targetEntity -> {
            targetEntity.setInvulnerable(blueprint.isInvulnerable());
            if (blueprint.getDuration() > 0)
                Bukkit.getScheduler().scheduleSyncDelayedTask(MetadataHandler.PLUGIN, () -> targetEntity.setInvulnerable(!blueprint.isInvulnerable()), blueprint.getDuration());
        });
    }

    private void runTag(ScriptActionData scriptActionData) {
        getTargets(scriptActionData).forEach(targetEntity -> {
            EliteEntity bossEntity = EntityTracker.getEliteMobEntity(targetEntity);
            if (bossEntity != null)
                bossEntity.addTags(blueprint.getTags());
            if (targetEntity instanceof Player player && ElitePlayerInventory.getPlayer(player) != null)
                ElitePlayerInventory.getPlayer(player).addTags(blueprint.getTags());
            if (blueprint.getDuration() > 0) {
                if (bossEntity != null)
                    Bukkit.getScheduler().scheduleSyncDelayedTask(MetadataHandler.PLUGIN, () -> bossEntity.removeTags(blueprint.getTags()), blueprint.getDuration());
                if (targetEntity instanceof Player player && ElitePlayerInventory.getPlayer(player) != null)
                    Bukkit.getScheduler().scheduleSyncDelayedTask(MetadataHandler.PLUGIN, () -> ElitePlayerInventory.getPlayer(player).removeTags(blueprint.getTags()), blueprint.getDuration());
            }
        });
    }

    private void runUntag(ScriptActionData scriptActionData) {
        getTargets(scriptActionData).forEach(targetEntity -> {
            EliteEntity bossEntity = EntityTracker.getEliteMobEntity(targetEntity);
            if (bossEntity != null)
                bossEntity.removeTags(blueprint.getTags());
            if (targetEntity instanceof Player player && ElitePlayerInventory.getPlayer(player) != null)
                ElitePlayerInventory.getPlayer(player).removeTags(blueprint.getTags());
            if (blueprint.getDuration() > 0) {
                if (bossEntity != null)
                    Bukkit.getScheduler().scheduleSyncDelayedTask(MetadataHandler.PLUGIN, () -> bossEntity.addTags(blueprint.getTags()), blueprint.getDuration());
                if (targetEntity instanceof Player player && ElitePlayerInventory.getPlayer(player) != null)
                    Bukkit.getScheduler().scheduleSyncDelayedTask(MetadataHandler.PLUGIN, () -> ElitePlayerInventory.getPlayer(player).addTags(blueprint.getTags()), blueprint.getDuration());
            }
        });
    }

    private void runSetTime(ScriptActionData scriptActionData) {
        getLocationTargets(scriptActionData).forEach(targetEntity -> targetEntity.getWorld().setTime(blueprint.getTime()));
    }

    private void runSetWeather(ScriptActionData scriptActionData) {
        getTargets(scriptActionData).forEach(targetEntity -> {
            if (blueprint.getDuration() > 0) {
                switch (blueprint.getWeatherType()) {
                    case CLEAR -> targetEntity.getWorld().setClearWeatherDuration(blueprint.getDuration());
                    case PRECIPITATION -> {
                        targetEntity.getWorld().setClearWeatherDuration(0);
                        targetEntity.getWorld().setWeatherDuration(blueprint.getDuration());
                    }
                    case THUNDER -> targetEntity.getWorld().setThunderDuration(blueprint.getDuration());
                }
            } else {
                switch (blueprint.getWeatherType()) {
                    case CLEAR -> targetEntity.getWorld().setClearWeatherDuration(6000);
                    case PRECIPITATION -> {
                        targetEntity.getWorld().setClearWeatherDuration(0);
                        targetEntity.getWorld().setWeatherDuration(6000);
                    }
                    case THUNDER -> targetEntity.getWorld().setThunderDuration(6000);
                }
            }
        });
    }

    private void runPlayAnimation(ScriptActionData scriptActionData) {
        getTargets(scriptActionData).forEach(targetEntity -> {
            EliteEntity targetElite = EntityTracker.getEliteMobEntity(targetEntity);
            if (targetElite == null) return;
            if (!(targetElite instanceof CustomBossEntity customBossEntity)) return;
            if (customBossEntity.getCustomModel() == null) return;
            customBossEntity.getCustomModel().playAnimationByName(blueprint.getSValue());
        });
    }

    private void runSpawnFallingBlock(ScriptActionData scriptActionData) {
        Collection<Location> locationTargets = getLocationTargets(scriptActionData);
        locationTargets.forEach(targetLocation -> {
            FallingBlock fallingBlock = targetLocation.getWorld().spawnFallingBlock(targetLocation, blueprint.getMaterial(), (byte) 0);
            fallingBlock.setDropItem(false);
            fallingBlock.setHurtEntities(false);
            if (blueprint.getScriptRelativeVectorBlueprint() != null)
                fallingBlock.setVelocity(new ScriptRelativeVector(blueprint.getScriptRelativeVectorBlueprint(), eliteScript, fallingBlock.getLocation()).getVector(scriptActionData));
            else if (blueprint.getVValue() != null)
                fallingBlock.setVelocity(blueprint.getVValue());
            ScriptListener.fallingBlocks.put(fallingBlock, new FallingEntityDataPair(this, scriptActionData));
        });
    }

    private void runModifyDamage(ScriptActionData scriptActionData) {
        if (scriptActionData.getEvent() instanceof EliteDamageEvent eliteDamageEvent)
            eliteDamageEvent.setDamage(eliteDamageEvent.getDamage() * blueprint.getMultiplier());
    }

    private void runSummonEntity(ScriptActionData scriptActionData) {
        EntityType entityType;
        try {
            entityType = EntityType.valueOf(blueprint.getSValue());
        } catch (Exception ex) {
            new WarningMessage("Failed to get entity type for the projectile in the script " + getBlueprint().getScriptName() + " in the file " + blueprint.getScriptFilename());
            return;
        }

        getLocationTargets(scriptActionData).forEach(targetLocation -> {
            Vector velocity = new Vector(0, 0, 0);
            if (blueprint.getScriptRelativeVectorBlueprint() != null) {
                ScriptRelativeVector scriptRelativeVector = new ScriptRelativeVector(blueprint.getScriptRelativeVectorBlueprint(), eliteScript, targetLocation);
                velocity = scriptRelativeVector.getVector(scriptActionData);
            } else if (!blueprint.getVValue().isZero()){
                velocity = blueprint.getVValue();
            }
            Entity entity;
            if (scriptActionData.getEliteEntity().getLivingEntity() != null &&
                    Projectile.class.isAssignableFrom(entityType.getEntityClass())) {
                entity = scriptActionData.getEliteEntity().getLivingEntity().launchProjectile(entityType.getEntityClass().asSubclass(Projectile.class), velocity);
                ((Projectile) entity).setShooter(scriptActionData.getEliteEntity().getLivingEntity());
                if (entity instanceof Fireball fireball) fireball.setDirection(velocity);
            }
            else {
                entity = targetLocation.getWorld().spawn(targetLocation, entityType.getEntityClass());
                entity.setVelocity(velocity);
            }

            if (!blueprint.getLandingScripts().isEmpty()) {
                FallingEntityDataPair fallingEntityDataPair = new FallingEntityDataPair(this, scriptActionData);
                new BukkitRunnable() {
                    int counter = 0;

                    @Override
                    public void run() {
                        if (!entity.isValid() || entity.isOnGround()) {
                            ScriptListener.runEvent(fallingEntityDataPair, entity.getLocation());
                            cancel();
                            return;
                        }
                        counter++;
                        if (counter > 20 * 60 * 5)
                            cancel();
                    }
                }.runTaskTimer(MetadataHandler.PLUGIN, 1, 1);
            }
        });
    }
}