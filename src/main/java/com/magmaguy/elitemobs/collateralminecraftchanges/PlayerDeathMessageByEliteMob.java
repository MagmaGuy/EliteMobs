package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.playerdata.PlayerStatsTracker;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class PlayerDeathMessageByEliteMob implements Listener {

    private static final HashMap<Player, String> deadPlayerList = new HashMap<>();

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
            PlayerStatsTracker.registerPlayerDeath(event.getEntity());
            removeDeadPlayer(event.getEntity());
        }

    }

    public static String initializeDeathMessage(Player player, LivingEntity livingEntity) {

        String deathMessage = "";

        if (MobPropertiesConfig.getMobProperties().containsKey(livingEntity.getType()))
            deathMessage = deathMessageSender
                    (MobPropertiesConfig.getMobProperties().get(livingEntity.getType()).getDeathMessages()
                                    .get(ThreadLocalRandom.current().nextInt(MobPropertiesConfig.getMobProperties().get(livingEntity.getType()).getDeathMessages().size()))
                            , player, livingEntity);

        return deathMessage;

    }

    private static String deathMessageSender(String deathMessage, Player player, LivingEntity livingEntity) {

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
