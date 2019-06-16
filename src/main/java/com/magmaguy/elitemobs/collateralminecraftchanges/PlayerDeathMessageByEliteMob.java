package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;

public class PlayerDeathMessageByEliteMob implements Listener {

    private static HashMap<Player, String> deadPlayerList = new HashMap<>();

    public static void addDeadPlayer(Player player, String deathMessage) {
        deadPlayerList.put(player, deathMessage);
    }

    private static boolean isDeadPlayer(Player player) {
        return deadPlayerList.containsKey(player);
    }

    private static void removeDeadPlayer(Player player) {
        deadPlayerList.remove(player);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        if (isDeadPlayer(event.getEntity())) {
            event.setDeathMessage(deadPlayerList.get(event.getEntity()));
            removeDeadPlayer(event.getEntity());
        }

    }

    public static String intializeDeathMessage(Player player, LivingEntity livingEntity) {

        String deathMessage;

        /*
        Deal with players dying from Elite Mobs
        */

        switch (livingEntity.getType()) {

            case BLAZE:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.BLAZE_DEATH_MESSAGE, player, livingEntity);
                break;
            case CREEPER:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.CREEPER_DEATH_MESSAGE, player, livingEntity);
                break;
            case ENDERMAN:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.ENDERMAN_DEATH_MESSAGE, player, livingEntity);
                break;
            case ENDERMITE:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.ENDERMITE_DEATH_MESSAGE, player, livingEntity);
                break;
            case IRON_GOLEM:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.IRONGOLEM_DEATH_MESSAGE, player, livingEntity);
                break;
            case POLAR_BEAR:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.POLARBEAR_DEATH_MESSAGE, player, livingEntity);
                break;
            case SILVERFISH:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.SILVERFISH_DEATH_MESSAGE, player, livingEntity);
                break;
            case SKELETON:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.SKELETON_DEATH_MESSAGE, player, livingEntity);
                break;
            case SPIDER:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.SPIDER_DEATH_MESSAGE, player, livingEntity);
                break;
            case WITCH:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.WITCH_DEATH_MESSAGE, player, livingEntity);
                break;
            case ZOMBIE:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.ZOMBIE_DEATH_MESSAGE, player, livingEntity);
                break;
            case HUSK:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.HUSK_DEATH_MESSAGE, player, livingEntity);
                break;
            case PIG_ZOMBIE:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.ZOMBIE_PIGMAN_DEATH_MESSAGE, player, livingEntity);
                break;
            case STRAY:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.STRAY_DEATH_MESSAGE, player, livingEntity);
                break;
            case WITHER_SKELETON:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.WITHER_SKELETON_DEATH_MESSAGE, player, livingEntity);
                break;
            default:
                deathMessage = deathMessageSender(MobCombatSettingsConfig.DEFAULT_DEATH_MESSAGE, player, livingEntity);
                break;
        }

        return deathMessage;

    }

    private static String deathMessageSender(String configNode, Player player, LivingEntity livingEntity) {

        String deathMessage = ConfigValues.mobCombatSettingsConfig.getString(configNode);
        deathMessage = deathMessagePlaceholderConversion(deathMessage, player, livingEntity);

        return deathMessage;

    }

    private static String deathMessagePlaceholderConversion(String deathMessage, Player player, LivingEntity livingEntity) {

        String livingEntityName = EntityTracker.getEliteMobEntity(livingEntity).getName();

        deathMessage = deathMessage.replace("$player", player.getDisplayName());
        deathMessage = deathMessage.replace("$entity", livingEntityName);
        deathMessage = ChatColorConverter.convert("&7" + deathMessage);

        return deathMessage;

    }

}
