package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class ScriptActions {
    @Getter
    private final List<ScriptAction> scriptActions = new ArrayList();

    public ScriptActions(List<Map<?, ?>> values, String scriptName) {
        for (Map<?, ?> entry : values)
            scriptActions.add(new ScriptAction(entry, scriptName));
    }

    public void runScripts(EliteEntity eliteEntity, Player player) {
        for (ScriptAction scriptAction : scriptActions)
            scriptAction.runScript(eliteEntity, player);
    }

    public boolean isValid() {
        return true;
    }

    private enum ActionType {
        TELEPORT,
        MESSAGE,
        POTION_EFFECT
    }

    private enum Target {
        NEARBY_PLAYERS,
        WORLD_PLAYERS,
        ALL_PLAYERS,
        PLAYER,
        SELF
    }

    private class ScriptAction {
        String scriptName;
        @Getter
        private ActionType actionType = null;
        private String location;
        @Getter
        private double range = 20;
        private int duration = 0;
        private Target target;
        private int wait = 0;
        private String message = null;
        private PotionEffectType potionEffectType;
        private int amplifier;

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
                case "location" -> location = parseString(key, value);
                case "message" -> message = parseString(key, value);
                case "range" -> range = parseDouble(key, value);
                case "duration" -> duration = parseInteger(key, value);
                case "wait" -> wait = parseInteger(key, value);
                case "amplifier" -> amplifier = parseInteger(key, value);
                case "target" -> target = parseEnum(key, value, Target.class);
                case "action" -> actionType = parseEnum(key, value, ActionType.class);
                case "potioneffecttype" ->
                        potionEffectType = PotionEffectType.getByKey(NamespacedKey.minecraft(((String) value).toLowerCase()));
                default -> new WarningMessage("Failed to read key " + key + " for script " + scriptName);
            }
        }

        private void parsingErrorMessage(String key, Object value) {
            new WarningMessage("Failed to read value " + value + " for key " + key + " in script " + scriptName);
        }

        private String parseString(String key, Object value) {
            return (String) value;
        }

        private Integer parseInteger(String key, Object value) {
            try {
                return (Integer) value;
            } catch (Exception ex) {
                parsingErrorMessage(key, value);
                return null;
            }
        }


        private Double parseDouble(String key, Object value) {
            try {
                return (Double) value;
            } catch (Exception ex) {
                parsingErrorMessage(key, value);
                return null;
            }
        }

        private <T extends Enum<T>> T parseEnum(String key, Object value, Class<T> enumClass) {
            try {
                return Enum.valueOf(enumClass, (String) value);
            } catch (Exception ex) {
                parsingErrorMessage(key, value);
            }
            return null;
        }


        public void runScript(EliteEntity eliteEntity, Player player) {
            //First wait for allotted amount of time
            new BukkitRunnable() {
                @Override
                public void run() {
                    //Different actions have completely different behavior
                    switch (actionType) {
                        case TELEPORT -> runTeleport(eliteEntity, player);
                        case MESSAGE -> runMessage(eliteEntity, player);
                        case POTION_EFFECT -> runPotionEffect(eliteEntity, player);
                        default ->
                                new WarningMessage("Failed to determine action type " + actionType + " in script " + scriptName);
                    }
                }
            }.runTaskLater(MetadataHandler.PLUGIN, wait);
        }

        private Collection<? extends LivingEntity> getTargets(EliteEntity eliteEntity, Player player) {
            Location eliteEntityLocation = eliteEntity.getLocation();
            return switch (target) {
                case ALL_PLAYERS -> Bukkit.getOnlinePlayers();
                case WORLD_PLAYERS -> eliteEntityLocation.getWorld().getPlayers();
                case NEARBY_PLAYERS ->
                        eliteEntityLocation.getWorld().getNearbyEntities(eliteEntityLocation, range, range, range, (entity -> entity.getType() == EntityType.PLAYER)).stream().map(entity -> (Player) entity).collect(Collectors.toSet());
                case PLAYER -> Collections.singletonList(player);
                case SELF -> Collections.singletonList(eliteEntity.getLivingEntity());
                default -> null;
            };
        }

        private void runTeleport(EliteEntity eliteEntity, Player player) {
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
            getTargets(eliteEntity, player).forEach(iteratedPlayer -> iteratedPlayer.teleport(finalLocation));
        }


        private void runMessage(EliteEntity eliteEntity, Player player) {
            getTargets(eliteEntity, player).forEach(iteratedPlayer -> iteratedPlayer.sendMessage(ChatColorConverter.convert(message)));
        }

        private void runPotionEffect(EliteEntity eliteEntity, Player player) {
            getTargets(eliteEntity, player).forEach(iteratedPlayer -> iteratedPlayer.addPotionEffect(new PotionEffect(potionEffectType, duration, amplifier)));
        }
    }
}
