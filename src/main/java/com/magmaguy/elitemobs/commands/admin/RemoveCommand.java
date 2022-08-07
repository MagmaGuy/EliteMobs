package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.dungeons.SchematicDungeonPackage;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RemoveCommand {
    private static final Set<UUID> removingPlayers = new HashSet<>();

    private RemoveCommand() {
    }

    public static void remove(Player player) {
        if (removingPlayers.contains(player.getUniqueId())) {
            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &aYou are no longer removing elites!"));
            removingPlayers.remove(player.getUniqueId());
        } else {
            removingPlayers.add(player.getUniqueId());
            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &cYou are now removing elites when you punch them! Run &a/em remove &cagain to stop removing elites!"));
        }
    }

    public static class RemoveCommandEvents implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void removeEliteEntity(EntityDamageByEntityEvent event) {
            if (!removingPlayers.contains(event.getDamager().getUniqueId())) return;
            EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getEntity());
            if (eliteEntity == null) return;
            if (eliteEntity instanceof RegionalBossEntity)
                event.getDamager().sendMessage(ChatColorConverter.convert(
                        "&8[EliteMobs] &cRemoved a spawn location for boss " +
                                ((RegionalBossEntity) eliteEntity).getCustomBossesConfigFields().getFilename()));
            if (eliteEntity instanceof RegionalBossEntity &&
                    ((CustomBossEntity) eliteEntity).getEmPackage() != null &&
                    ((CustomBossEntity) eliteEntity).getEmPackage().getDungeonPackagerConfigFields().getDungeonLocationType()
                            .equals(DungeonPackagerConfigFields.DungeonLocationType.SCHEMATIC))
                ((SchematicDungeonPackage) ((CustomBossEntity) eliteEntity).getEmPackage()).removeBoss((RegionalBossEntity) eliteEntity);
            eliteEntity.remove(RemovalReason.REMOVE_COMMAND);
            event.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void removeNPC(EntityDamageByEntityEvent event) {
            if (!removingPlayers.contains(event.getDamager().getUniqueId())) return;
            NPCEntity npcEntity = EntityTracker.getNPCEntity(event.getEntity());
            if (npcEntity == null) return;
            npcEntity.remove(RemovalReason.REMOVE_COMMAND);
            event.setCancelled(true);
        }
    }

}

