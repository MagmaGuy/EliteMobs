package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.playerdata.PlayerStatsTracker;
import com.magmaguy.magmacore.util.ChatColorConverter;
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

    public static String initializeDeathMessage(Player player, EliteEntity eliteEntity) {

        String deathMessage = "";

        if (eliteEntity instanceof CustomBossEntity customBossEntity && customBossEntity.getCustomBossesConfigFields().getOnKillMessage() != null && !customBossEntity.getCustomBossesConfigFields().getOnKillMessage().isEmpty())
            return deathMessageSender(customBossEntity.getCustomBossesConfigFields().getOnKillMessage().replace("$player", player.getDisplayName()), player, customBossEntity.getLivingEntity());

        if (MobPropertiesConfig.getMobProperties().containsKey(eliteEntity.getLivingEntity().getType()))
            deathMessage = deathMessageSender
                    (MobPropertiesConfig.getMobProperties().get(eliteEntity.getLivingEntity().getType()).getDeathMessages()
                                    .get(ThreadLocalRandom.current().nextInt(MobPropertiesConfig.getMobProperties().get(eliteEntity.getLivingEntity().getType()).getDeathMessages().size()))
                            , player, eliteEntity.getLivingEntity());

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

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        if (isDeadPlayer(event.getEntity())) {
            event.setDeathMessage(deadPlayerList.get(event.getEntity()));
            PlayerStatsTracker.registerPlayerDeath(event.getEntity());
            removeDeadPlayer(event.getEntity());
        }

    }

}
