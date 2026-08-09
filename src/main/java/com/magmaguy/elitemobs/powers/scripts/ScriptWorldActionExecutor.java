package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteDamageEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.collateralminecraftchanges.LightningSpawnBypass;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.pathfinding.Navigation;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.powers.meta.CustomSummonPower;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptActionBlueprint;
import com.magmaguy.elitemobs.powers.scripts.enums.ActionType;
import com.magmaguy.elitemobs.powers.scripts.enums.TargetType;
import com.magmaguy.magmacore.scripting.zones.Shape;
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

/** Executes world, entity-state, and visual script actions. */
final class ScriptWorldActionExecutor {

    private final ScriptAction action;

    ScriptWorldActionExecutor(ScriptAction action) {
        this.action = action;
    }

    /**
     * Strikes lightning at the target locations, ignoring protections.
     *
     * @param scriptActionData The data for the script action.
     */
    void runStrikeLightning(ScriptActionData scriptActionData) {
        action.getLocationTargets(scriptActionData).forEach(LightningSpawnBypass::strikeLightningIgnoreProtections);
    }

    /**
     * Spawns particles at the target locations.
     *
     * @param scriptActionData The data for the script action.
     */
    void runSpawnParticle(ScriptActionData scriptActionData) {
        boolean needsCentering = switch (scriptActionData.getTargetType()) {
            case ZONE_FULL, ZONE_BORDER, INHERIT_SCRIPT_ZONE_FULL, INHERIT_SCRIPT_ZONE_BORDER, LOCATION, LOCATIONS,
                    LANDING_LOCATION -> true;
            default -> false;
        };
        action.getLocationTargets(scriptActionData).forEach(location -> {
            Location targetLocation = needsCentering ? location.clone().add(0.5, 0, 0.5) : location;
            action.scriptParticles().visualize(scriptActionData, targetLocation, action.runtimeOwner());
        });
    }

    /**
     * Sets the AI state of the target entities.
     *
     * @param scriptActionData The data for the script action.
     */
    void runSetMobAI(ScriptActionData scriptActionData) {
        boolean aiEnabled = action.getBlueprint().getBValue();
        int duration = action.getBlueprint().getDuration().getValue();

        action.getTargets(scriptActionData).forEach(target -> TimedScriptStateManager.apply(
                target.getUniqueId(), "mob_ai", aiEnabled, duration,
                target::hasAI, target::setAI));
    }

    /**
     * Sets the awareness state of the target mobs.
     *
     * @param scriptActionData The data for the script action.
     */
    void runSetMobAware(ScriptActionData scriptActionData) {
        boolean aware = action.getBlueprint().getBValue();
        int duration = action.getBlueprint().getDuration().getValue();

        action.getTargets(scriptActionData).forEach(target -> {
            if (target instanceof Mob mob) {
                TimedScriptStateManager.apply(
                        mob.getUniqueId(), "mob_aware", aware, duration,
                        mob::isAware, mob::setAware);
            } else {
                Logger.warn("SET_MOB_AWARE action must target mobs! Problematic script: '" + action.getBlueprint().getScriptName() + "' in file '" + action.getBlueprint().getScriptFilename() + "'");
            }
        });
    }

    /**
     * Plays a sound at the target locations.
     *
     * @param scriptActionData The data for the script action.
     */
    void runPlaySound(ScriptActionData scriptActionData) {
        String sound = action.getBlueprint().getSValue();
        float volume = action.getBlueprint().getVolume().getValue();
        float pitch = action.getBlueprint().getPitch().getValue();
        action.getLocationTargets(scriptActionData).forEach(location -> {
            try {
                location.getWorld().playSound(location, sound, volume, pitch);
            } catch (Exception e) {
                Logger.warn("Failed to play sound '" + sound + "' at location '" + location + "' in script '" + action.getBlueprint().getScriptName() + "': " + e.getMessage());
            }
        });
    }

    /**
     * Applies a velocity to the target entities.
     *
     * @param scriptActionData The data for the script action.
     */
    void runPush(ScriptActionData scriptActionData) {
        Vector velocity = action.getBlueprint().getScriptRelativeVectorBlueprint() != null
                ? new ScriptRelativeVector(action.getBlueprint().getScriptRelativeVectorBlueprint(), action.runtimeOwner(), scriptActionData.getEliteEntity().getLocation()).getVector(scriptActionData)
                : action.getBlueprint().getVValue();

        // Ensure velocity is finite, otherwise default to zero vector
        if (velocity == null || !isFiniteVector(velocity)) {
            velocity = new Vector(0, 0, 0);
        }

        boolean additive = action.getBlueprint().getBValue() != null && action.getBlueprint().getBValue();

        // Delay the push by one tick to avoid interference with other events.
        Vector localFinalVelocity = velocity;
        new BukkitRunnable() {
            @Override
            public void run() {
                Vector finalVelocity = localFinalVelocity;
                action.getTargets(scriptActionData).forEach(target -> {
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
    void runSummonReinforcement(ScriptActionData scriptActionData) {
        action.getLocationTargets(scriptActionData).forEach(location -> {
            CustomBossEntity customBossEntity = CustomSummonPower.summonReinforcement(scriptActionData.getEliteEntity(), location, action.getBlueprint().getSValue(), action.getBlueprint().getDuration().getValue());
            if (customBossEntity != null && customBossEntity.getLivingEntity() != null) {
                Vector velocity = action.getBlueprint().getScriptRelativeVectorBlueprint() != null
                        ? new ScriptRelativeVector(action.getBlueprint().getScriptRelativeVectorBlueprint(), action.runtimeOwner(), customBossEntity.getLivingEntity().getLocation()).getVector(scriptActionData)
                        : action.getBlueprint().getVValue();
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
    void runSpawnFireworks(ScriptActionData scriptActionData) {
        if (action.getBlueprint().getFireworkEffects().isEmpty()) {
            Logger.warn("No colors set for fireworks in script '" + action.getBlueprint().getScriptName() + "' in file '" + action.getBlueprint().getScriptFilename() + "'");
            return;
        }

        action.getLocationTargets(scriptActionData).forEach(location -> {
            try {
                Firework firework = location.getWorld().spawn(location, Firework.class);
                firework.setPersistent(false);
                FireworkMeta fireworkMeta = firework.getFireworkMeta();

                List<FireworkEffect.Type> types = action.getBlueprint().getFireworkEffectTypes();
                List<List<Color>> colorsList = action.getBlueprint().getFireworkEffects().stream()
                        .map(colors -> colors.stream().map(ScriptActionBlueprint.FireworkColor::getColor).toList())
                        .toList();

                if (types == null || types.isEmpty()) {
                    types = List.of(action.getBlueprint().getFireworkEffectType());
                }

                for (int i = 0; i < types.size(); i++) {
                    FireworkEffect.Type type = types.get(i);
                    List<Color> colors = i < colorsList.size() ? colorsList.get(i) : colorsList.get(colorsList.size() - 1);
                    FireworkEffect effect = FireworkEffect.builder()
                            .with(type)
                            .withColor(colors)
                            .flicker(action.getBlueprint().isFlicker())
                            .trail(action.getBlueprint().isWithTrail())
                            .build();
                    fireworkMeta.addEffect(effect);
                }

                fireworkMeta.setPower(action.getBlueprint().getPower().getValue());

                if (action.getBlueprint().getVValue() != null) {
                    firework.setVelocity(action.getBlueprint().getVValue());
                    firework.setShotAtAngle(true);
                }

                firework.setFireworkMeta(fireworkMeta);
            } catch (Exception e) {
                Logger.warn("Failed to spawn fireworks at location '" + location + "' in script '" + action.getBlueprint().getScriptName() + "': " + e.getMessage());
            }
        });
    }

    /**
     * Makes the target entities invulnerable for a specified duration.
     *
     * @param scriptActionData The data for the script action.
     */
    void runMakeInvulnerable(ScriptActionData scriptActionData) {
        boolean invulnerable = action.getBlueprint().isInvulnerable();
        int duration = action.getBlueprint().getDuration().getValue();

        action.getTargets(scriptActionData).forEach(target -> {
            if (target instanceof Player player) {
                UUID playerId = player.getUniqueId();
                TimedScriptStateManager.apply(
                        playerId, "invulnerable", new PlayerInvulnerability(
                                invulnerable, invulnerable), duration,
                        () -> new PlayerInvulnerability(
                                player.isInvulnerable(),
                                ScriptAction.getInvulnerablePlayers().contains(playerId)),
                        state -> {
                            player.setInvulnerable(state.invulnerable());
                            if (state.scriptOwned()) ScriptAction.getInvulnerablePlayers().add(playerId);
                            else ScriptAction.getInvulnerablePlayers().remove(playerId);
                        });
            } else {
                TimedScriptStateManager.apply(
                        target.getUniqueId(), "invulnerable", invulnerable, duration,
                        target::isInvulnerable, target::setInvulnerable);
            }
        });
    }

    /**
     * Adds tags to the target entities and players.
     *
     * @param scriptActionData The data for the script action.
     */
    void runTag(ScriptActionData scriptActionData) {
        List<String> tags = action.getBlueprint().getTags();
        int duration = action.getBlueprint().getDuration().getValue();

        action.getTargets(scriptActionData).forEach(target -> {
            EliteEntity bossEntity = EntityTracker.getEliteMobEntity(target);
            if (bossEntity != null) {
                for (String tag : tags) {
                    TimedScriptStateManager.apply(
                            target.getUniqueId(), "elite_tag:" + tag, true, duration,
                            () -> bossEntity.getTags().contains(tag),
                            present -> setEliteTag(bossEntity, tag, present));
                }
            }
            if (target instanceof Player player) {
                ElitePlayerInventory playerInventory = ElitePlayerInventory.getPlayer(player);
                if (playerInventory != null) {
                    for (String tag : tags) {
                        TimedScriptStateManager.apply(
                                player.getUniqueId(), "player_tag:" + tag, true, duration,
                                () -> playerInventory.getTags().contains(tag),
                                present -> setPlayerTag(playerInventory, tag, present));
                    }
                }
            }
        });
    }

    /**
     * Removes tags from the target entities and players.
     *
     * @param scriptActionData The data for the script action.
     */
    void runUntag(ScriptActionData scriptActionData) {
        List<String> tags = action.getBlueprint().getTags();
        int duration = action.getBlueprint().getDuration().getValue();

        action.getTargets(scriptActionData).forEach(target -> {
            EliteEntity bossEntity = EntityTracker.getEliteMobEntity(target);
            if (bossEntity != null) {
                for (String tag : tags) {
                    TimedScriptStateManager.apply(
                            target.getUniqueId(), "elite_tag:" + tag, false, duration,
                            () -> bossEntity.getTags().contains(tag),
                            present -> setEliteTag(bossEntity, tag, present));
                }
            }
            if (target instanceof Player player) {
                ElitePlayerInventory playerInventory = ElitePlayerInventory.getPlayer(player);
                if (playerInventory != null) {
                    for (String tag : tags) {
                        TimedScriptStateManager.apply(
                                player.getUniqueId(), "player_tag:" + tag, false, duration,
                                () -> playerInventory.getTags().contains(tag),
                                present -> setPlayerTag(playerInventory, tag, present));
                    }
                }
            }
        });
    }

    /**
     * Sets the time in the worlds of the target locations.
     *
     * @param scriptActionData The data for the script action.
     */
    void runSetTime(ScriptActionData scriptActionData) {
        long time = action.getBlueprint().getTime().getValue();
        action.getLocationTargets(scriptActionData).forEach(location -> {
            try {
                location.getWorld().setTime(time);
            } catch (Exception e) {
                Logger.warn("Failed to set time in world '" + location.getWorld().getName() + "' in script '" + action.getBlueprint().getScriptName() + "': " + e.getMessage());
            }
        });
    }

    /**
     * Sets the weather in the worlds of the target entities.
     *
     * @param scriptActionData The data for the script action.
     */
    void runSetWeather(ScriptActionData scriptActionData) {
        int duration = action.getBlueprint().getDuration().getValue();
        action.getTargets(scriptActionData).forEach(target -> {
            World world = target.getWorld();
            try {
                switch (action.getBlueprint().getWeatherType()) {
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
                        if (duration > 0) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    world.setStorm(false);
                                    world.setThundering(false);
                                }
                            }.runTaskLater(MetadataHandler.PLUGIN, duration + 1);
                        }
                    }
                }
            } catch (Exception e) {
                Logger.warn("Failed to set weather in world '" + world.getName() + "' in script '" + action.getBlueprint().getScriptName() + "': " + e.getMessage());
            }
        });
    }

    private static void setEliteTag(EliteEntity eliteEntity, String tag, boolean present) {
        if (present) eliteEntity.addTags(List.of(tag));
        else eliteEntity.removeTags(List.of(tag));
    }

    private static void setPlayerTag(ElitePlayerInventory inventory, String tag, boolean present) {
        if (present) inventory.addTags(List.of(tag));
        else inventory.removeTags(List.of(tag));
    }

    private record PlayerInvulnerability(boolean invulnerable, boolean scriptOwned) {
    }

}
