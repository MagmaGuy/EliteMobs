package com.magmaguy.elitemobs.npcs;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.adventurersguild.GuildRankMenuHandler;
import com.magmaguy.elitemobs.api.PlayerPreTeleportEvent;
import com.magmaguy.elitemobs.api.PlayerTeleportEvent;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.menus.CustomShopMenu;
import com.magmaguy.elitemobs.menus.ProceduralShopMenu;
import com.magmaguy.elitemobs.menus.SellMenu;
import com.magmaguy.elitemobs.quests.QuestsMenu;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class NPCInteractions implements Listener {

    public enum NPCInteractionType {
        GUILD_GREETER,
        CHAT,
        CUSTOM_SHOP,
        PROCEDURALLY_GENERATED_SHOP,
        BAR,
        ARENA,
        QUEST_GIVER,
        NONE,
        SELL,
        TELEPORT_BACK
    }

    @EventHandler
    public void playerNPCInteract(PlayerInteractAtEntityEvent event) {

        NPCEntity npcEntity = EntityTracker.getNPCEntity(event.getRightClicked());
        if (npcEntity == null) return;
        if (event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.NAME_TAG)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("[EliteMobs] You can't rename NPCs using name tags!");
            return;
        }
        if (npcEntity.getIsSleeping()) return;

        event.setCancelled(true);

        switch (npcEntity.getInteractionType()) {
            case GUILD_GREETER:
                if (event.getPlayer().hasPermission("elitemobs.rank.npc")) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            GuildRankMenuHandler guildRankMenuHandler = new GuildRankMenuHandler();
                            GuildRankMenuHandler.initializeGuildRankMenu(event.getPlayer());
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 1);
                }
                break;
            case CHAT:
                npcEntity.sayDialog(event.getPlayer());
                break;
            case CUSTOM_SHOP:
                if (event.getPlayer().hasPermission("elitemobs.customshop.npc"))
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            CustomShopMenu.customShopInitializer(event.getPlayer());
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 1);

                break;
            case PROCEDURALLY_GENERATED_SHOP:
                if (event.getPlayer().hasPermission("elitemobs.shop.npc"))
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            ProceduralShopMenu.shopInitializer(event.getPlayer());
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 1);
                break;
            case QUEST_GIVER:
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        QuestsMenu.initializeQuestsMenu(event.getPlayer());
                    }
                }.runTaskLater(MetadataHandler.PLUGIN, 1);
                break;
            case BAR:
                break;
            case ARENA:
                break;
            case SELL:
                if (event.getPlayer().hasPermission("elitemobs.shop.npc"))
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            SellMenu sellMenu = new SellMenu();
                            sellMenu.constructSellMenu(event.getPlayer());
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 1);
                break;
            case TELEPORT_BACK:
                if (event.getPlayer().hasPermission("elitemobs.back.npc")) {
                    Location previousLocation = PlayerTeleportEvent.previousLocations.get(event.getPlayer());
                    if (previousLocation == null) {
                        if (npcEntity.npCsConfigFields.noPreviousLocationMessage != null)
                            event.getPlayer().sendMessage(ChatColorConverter.convert(npcEntity.npCsConfigFields.noPreviousLocationMessage));
                    } else
                        PlayerPreTeleportEvent.teleportPlayer(event.getPlayer(), previousLocation);
                }
                break;
            case NONE:
            default:
                break;

        }

    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {

        if (!event.getInventory().getType().equals(InventoryType.MERCHANT)) return;

        for (NPCEntity npcEntity : EntityTracker.getNPCEntities().values())
            if (event.getView().getTitle().equals(npcEntity.getName())) {
                event.setCancelled(true);
                return;
            }

    }


}
