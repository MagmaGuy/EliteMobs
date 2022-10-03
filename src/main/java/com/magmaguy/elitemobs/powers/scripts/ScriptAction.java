package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.collateralminecraftchanges.LightningSpawnBypass;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.CustomSummonPower;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptActionBlueprint;
import com.magmaguy.elitemobs.powers.scripts.enums.ActionType;
import com.magmaguy.elitemobs.utils.WarningMessage;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ScriptAction {
    private final ScriptActionBlueprint blueprint;
    private ScriptTargets scriptTargets;
    private ScriptConditions scriptConditions;
    private ScriptParticles scriptParticles;
    private Map<String, EliteScript> eliteScriptMap;

    public ScriptAction(ScriptActionBlueprint blueprint, Map<String, EliteScript> eliteScriptMap, EliteScript eliteScript) {
        this.blueprint = blueprint;
        this.scriptTargets = new ScriptTargets(blueprint.getScriptTargets(), eliteScript);
        this.scriptConditions = new ScriptConditions(blueprint.getConditionsBlueprint());
        this.scriptParticles = new ScriptParticles(blueprint.getScriptParticlesBlueprint());
        this.eliteScriptMap = eliteScriptMap;
    }

    public void runScript(EliteEntity eliteEntity, LivingEntity directTarget) {
        //Check if the script conditions of action are met
        if (blueprint.getConditionsBlueprint() != null && !scriptConditions.meetsConditions(eliteEntity, directTarget))
            return;
        //This caches the tracking mostly for zones to start at the wait time. This matters if you are making zones
        //that go through a warning phase and then a damage phase.
        scriptTargets.cacheTargets(eliteEntity, directTarget, blueprint.getActionType());
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
                            //If it has run for the allotted amount times, stop
                            if (blueprint.getTimes() > 0 && counter > blueprint.getTimes()) {
                                cancel();
                                return;
                            }

                            //Otherwise, run the condition
                            runActions(eliteEntity, directTarget);
                        }
                    }.runTaskTimer(MetadataHandler.PLUGIN, 0, blueprint.getRepeatEvery());
                else
                    //If it's not a repeating task, just run it normally
                    runActions(eliteEntity, directTarget);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, blueprint.getWait());
    }

    private void runActions(EliteEntity eliteEntity, LivingEntity directTarget) {
        //Different actions have completely different behavior
        switch (blueprint.getActionType()) {
            case TELEPORT -> runTeleport(eliteEntity, directTarget);
            case MESSAGE -> runMessage(eliteEntity, directTarget);
            case ACTION_BAR_MESSAGE -> runActionBarMessage(eliteEntity, directTarget);
            case TITLE_MESSAGE -> runTitleMessage(eliteEntity, directTarget);
            case BOSS_BAR_MESSAGE -> runBossBarMessage(eliteEntity, directTarget);
            case POTION_EFFECT -> runPotionEffect(eliteEntity, directTarget);
            case DAMAGE -> runDamage(eliteEntity, directTarget);
            case SET_ON_FIRE -> runSetOnFire(eliteEntity, directTarget);
            case VISUAL_FREEZE -> runVisualFreeze(eliteEntity, directTarget);
            case PLACE_BLOCK -> runPlaceBlock(eliteEntity, directTarget);
            case RUN_COMMAND_AS_PLAYER -> runPlayerCommand(eliteEntity, directTarget);
            case RUN_COMMAND_AS_CONSOLE -> runConsoleCommand(eliteEntity, directTarget);
            case STRIKE_LIGHTNING -> runStrikeLighting(eliteEntity, directTarget);
            case SPAWN_PARTICLE -> runSpawnParticle(eliteEntity, directTarget);
            case SET_MOB_AI -> runSetMobAI(eliteEntity, directTarget);
            case SET_MOB_AWARE -> runSetMobAware(eliteEntity, directTarget);
            case PLAY_SOUND -> runPlaySound(eliteEntity, directTarget);
            case PUSH -> runPush(eliteEntity, directTarget);
            case SUMMON_REINFORCEMENT -> runSummonReinforcement(eliteEntity, directTarget);
            case RUN_SCRIPT -> runAdditionalScripts(eliteEntity, directTarget);
            case SPAWN_FIREWORKS -> runSpawnFireworks(eliteEntity, directTarget);
            case MAKE_INVULNERABLE -> runMakeInvulnerable(eliteEntity, directTarget);
            default ->
                    new WarningMessage("Failed to determine action type " + blueprint.getActionType() + " in script " + blueprint.getScriptName() + " for file " + blueprint.getScriptFilename());
        }
        //Run script will have already run this
        if (!blueprint.getActionType().equals(ActionType.RUN_SCRIPT))
            runAdditionalScripts(eliteEntity, directTarget);
    }

    protected Collection<? extends LivingEntity> getTargets(EliteEntity eliteEntity, LivingEntity directTarget) {
        return scriptConditions.validateEntities(eliteEntity, scriptTargets.getTargets(eliteEntity, directTarget));
    }

    //Gets a list of locations
    protected Collection<Location> getLocationTargets(EliteEntity eliteEntity, LivingEntity directTarget) {
        return scriptConditions.validateLocations(eliteEntity, scriptTargets.getZoneLocationTargets(eliteEntity, directTarget));
    }

    //Teleports the targets
    private void runTeleport(EliteEntity eliteEntity, LivingEntity directTarget) {
        getTargets(eliteEntity, directTarget).forEach(iteratedPlayer -> iteratedPlayer.teleport(blueprint.getLocation(eliteEntity)));
    }

    //Sends a message to the targets
    private void runMessage(EliteEntity eliteEntity, LivingEntity directTarget) {
        getTargets(eliteEntity, directTarget).forEach(iteratedTarget -> iteratedTarget.sendMessage(ChatColorConverter.convert(blueprint.getSValue())));
    }

    private void runTitleMessage(EliteEntity eliteEntity, LivingEntity directTarget) {
        if (blueprint.getTitle().isEmpty() && blueprint.getSubtitle().isEmpty()) {
            new WarningMessage("TITLE_MESSAGE action does not have any titles or subtitles for script " + blueprint.getScriptName() + " in file " + blueprint.getScriptFilename());
            return;
        }
        getTargets(eliteEntity, directTarget).forEach(iteratedTarget -> {
            if (!(iteratedTarget instanceof Player)) {
                new WarningMessage("TITLE_MESSAGE actions must target players! Problematic script: " + blueprint.getScriptFilename() + " in " + blueprint.getScriptFilename());
                return;
            }
            ((Player) iteratedTarget).sendTitle(blueprint.getTitle(), blueprint.getSubtitle(), blueprint.getFadeIn(), blueprint.getDuration(), blueprint.getFadeOut());
        });
    }

    private void runActionBarMessage(EliteEntity eliteEntity, LivingEntity directTarget) {
        if (blueprint.getSValue().isEmpty()) {
            new WarningMessage("ACTION_BAR_MESSAGE action does not have a sValue for script " + blueprint.getScriptName() + " in file " + blueprint.getScriptFilename());
            return;
        }
        getTargets(eliteEntity, directTarget).forEach(iteratedTarget -> {
            if (!(iteratedTarget instanceof Player)) {
                new WarningMessage("ACTION_BAR_MESSAGE actions must target players! Problematic script: " + blueprint.getScriptFilename() + " in " + blueprint.getScriptFilename());
                return;
            }
            ((Player) iteratedTarget).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(blueprint.getSValue()));
        });
    }

    private void runBossBarMessage(EliteEntity eliteEntity, LivingEntity directTarget) {
        if (blueprint.getSValue().isEmpty()) {
            new WarningMessage("BOSS_BAR_MESSAGE action does not have a valid sValue for script " + blueprint.getScriptFilename() + " in file " + blueprint.getScriptFilename());
            return;
        }
        BossBar bossBar = Bukkit.createBossBar(blueprint.getSValue(), blueprint.getBarColor(), blueprint.getBarStyle());
        getTargets(eliteEntity, directTarget).forEach(iteratedTarget -> {
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
    private void runPotionEffect(EliteEntity eliteEntity, LivingEntity directTarget) {
        getTargets(eliteEntity, directTarget).forEach(iteratedTarget -> iteratedTarget.addPotionEffect(new PotionEffect(blueprint.getPotionEffectType(), blueprint.getDuration(), blueprint.getAmplifier())));
    }

    //Runs any scripts in the scripts field. Respects wait time and repeating tasks
    private void runAdditionalScripts(EliteEntity eliteEntity, LivingEntity directTarget) {
        if (blueprint.getActionType().equals(ActionType.RUN_SCRIPT) && blueprint.getScripts().isEmpty())
            new WarningMessage("Did not find any scripts for action RUN_SCRIPT in script " + blueprint.getScriptName() + " in file " + blueprint.getScriptFilename());
        if (blueprint.getScripts() != null)
            blueprint.getScripts().forEach(iteratedScriptName -> {
                EliteScript iteratedScript = eliteScriptMap.get(iteratedScriptName);
                if (iteratedScript == null)
                    new WarningMessage("Failed to get script " + iteratedScriptName + " for script " + blueprint.getScriptName() + " in file " + blueprint.getScriptFilename());
                else {
                    iteratedScript.check(eliteEntity, directTarget);
                }
            });
    }

    //Damages the target living entity
    private void runDamage(EliteEntity eliteEntity, LivingEntity directTarget) {
        getTargets(eliteEntity, directTarget).forEach(targetEntity -> {
            if (targetEntity instanceof Player)
                PlayerDamagedByEliteMobEvent.PlayerDamagedByEliteMobEventFilter.setSpecialMultiplier(blueprint.getMultiplier());
            if (eliteEntity.getLivingEntity() != null)
                targetEntity.damage(blueprint.getAmount(), eliteEntity.getLivingEntity());
            else
                targetEntity.damage(blueprint.getAmount());
        });
    }

    //Sets a target on fire for the duration
    private void runSetOnFire(EliteEntity eliteEntity, LivingEntity directTarget) {
        getTargets(eliteEntity, directTarget).forEach(iteratedTarget -> iteratedTarget.setFireTicks(blueprint.getDuration()));
    }

    //Applies the freeze visual effect from Minecraft. Requires a repeating task to keep reapplying
    private void runVisualFreeze(EliteEntity eliteEntity, LivingEntity directTarget) {
        getTargets(eliteEntity, directTarget).forEach(iteratedTarget -> iteratedTarget.setFreezeTicks(141));
    }

    //Places a block, temporarily if the duration is defined
    private void runPlaceBlock(EliteEntity eliteEntity, LivingEntity livingEntity) {
        getLocationTargets(eliteEntity, livingEntity).forEach(targetLocation -> {
            Block block = targetLocation.getBlock();
            if (blueprint.getDuration() > 0) {
                EntityTracker.addTemporaryBlock(block, blueprint.getDuration(), blueprint.getMaterial());
            } else
                block.setType(blueprint.getMaterial());
        });
    }

    //Runs a command as player
    private void runPlayerCommand(EliteEntity eliteEntity, LivingEntity directTarget) {
        getTargets(eliteEntity, directTarget).forEach(targetEntity -> ((Player) targetEntity).performCommand(parseCommand(eliteEntity, (Player) directTarget)));
    }

    //Runs a command as console
    private void runConsoleCommand(EliteEntity eliteEntity, LivingEntity directTarget) {
        getTargets(eliteEntity, directTarget).forEach(targetEntity -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parseCommand(eliteEntity, (Player) directTarget)));
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
                .replace("$bossLevel", eliteEntity.getLevel() + "");
    }

    //Strikes visual lightning at the target location
    private void runStrikeLighting(EliteEntity eliteEntity, LivingEntity directTarget) {
        getLocationTargets(eliteEntity, directTarget).forEach(LightningSpawnBypass::strikeLightingIgnoreProtections);
    }

    //Spawns a particle at the target location
    private void runSpawnParticle(EliteEntity eliteEntity, LivingEntity directTarget) {
        getLocationTargets(eliteEntity, directTarget).forEach(targetLocation -> scriptParticles.visualize(targetLocation));
    }

    //Sets mob AI
    private void runSetMobAI(EliteEntity eliteEntity, LivingEntity directTarget) {
        getTargets(eliteEntity, directTarget).forEach(targetEntity -> targetEntity.setAI(blueprint.getBValue()));
    }

    //Sets mob awareness
    private void runSetMobAware(EliteEntity eliteEntity, LivingEntity directTarget) {
        getTargets(eliteEntity, directTarget).forEach(targetEntity -> ((Mob) targetEntity).setAware(blueprint.getBValue()));
    }

    //Plays a sound at the target location
    private void runPlaySound(EliteEntity eliteEntity, LivingEntity directTarget) {
        getLocationTargets(eliteEntity, directTarget).forEach(targetLocation -> targetLocation.getWorld().playSound(targetLocation, blueprint.getSValue(), 1f, 1f));
    }

    private void runPush(EliteEntity eliteEntity, LivingEntity livingEntity) {
        getTargets(eliteEntity, livingEntity).forEach(targetEntity -> targetEntity.setVelocity(blueprint.getVValue()));
    }

    private void runSummonReinforcement(EliteEntity eliteEntity, LivingEntity livingEntity) {
        getLocationTargets(eliteEntity, livingEntity).forEach(targetLocation -> CustomSummonPower.summonReinforcement(eliteEntity, targetLocation, blueprint.getSValue(), blueprint.getDuration()));
    }

    private void runSpawnFireworks(EliteEntity eliteEntity, LivingEntity livingEntity) {
        getLocationTargets(eliteEntity, livingEntity).forEach(targetLocation -> {
            Firework firework = targetLocation.getWorld().spawn(targetLocation, Firework.class);
            FireworkMeta fireworkMeta = firework.getFireworkMeta();
            for (List<ScriptActionBlueprint.FireworkColor> fireworkColors : blueprint.getFireworkEffects()) {
                List<Color> colors = fireworkColors.stream().map(ScriptActionBlueprint.FireworkColor::getColor).toList();
                fireworkMeta.addEffect(FireworkEffect.builder()
                        .with(blueprint.getFireworkEffectType())
                        .withColor(colors)
                        .flicker(blueprint.isFlicker())
                        .trail(blueprint.isWithTrail())
                        .build());
            }
            fireworkMeta.setPower(blueprint.getPower());
            if (blueprint.getVValue() != null)
                firework.setVelocity(blueprint.getVValue());
            firework.setFireworkMeta(fireworkMeta);
        });
    }

    private void runMakeInvulnerable(EliteEntity eliteEntity, LivingEntity livingEntity) {
        getTargets(eliteEntity, livingEntity).forEach(targetEntity -> {
            targetEntity.setInvulnerable(blueprint.isInvulnerable());
            if (blueprint.getDuration() > 0)
                Bukkit.getScheduler().scheduleSyncDelayedTask(MetadataHandler.PLUGIN, () -> targetEntity.setInvulnerable(!blueprint.isInvulnerable()), blueprint.getDuration());
        });
    }
}