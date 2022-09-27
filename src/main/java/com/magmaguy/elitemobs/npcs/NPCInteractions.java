package com.magmaguy.elitemobs.npcs;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.adventurersguild.GuildRankMenuHandler;
import com.magmaguy.elitemobs.api.PlayerPreTeleportEvent;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.menus.*;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.QuestInteractionHandler;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

public class NPCInteractions implements Listener {

    private static final HashSet<Player> cooldowns = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerNPCInteract(PlayerInteractAtEntityEvent event) {

        if (cooldowns.contains(event.getPlayer())) return;
        cooldowns.add(event.getPlayer());
        Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> cooldowns.remove(event.getPlayer()), 1);
        if (event.isCancelled()) return;

        NPCEntity npcEntity = EntityTracker.getNPCEntity(event.getRightClicked());
        if (npcEntity == null) return;
        if (event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.NAME_TAG)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("[EliteMobs] You can't rename NPCs using name tags!");
            return;
        }

        event.setCancelled(true);

        switch (npcEntity.getNPCsConfigFields().getInteractionType()) {
            case GUILD_GREETER:
                if (event.getPlayer().hasPermission("elitemobs.rank.npc")) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
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
                if (event.getPlayer().hasPermission("elitemobs.quest.npc"))
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            QuestInteractionHandler.processDynamicQuests(event.getPlayer(), npcEntity);
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 1);
                break;
            case CUSTOM_QUEST_GIVER:
                QuestInteractionHandler.processNPCQuests(event.getPlayer(), npcEntity);
                break;
            case BAR:
                event.getPlayer().sendMessage("[EliteMobs] This feature is coming soon!");
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
                    Location previousLocation = PlayerData.getBackTeleportLocation(event.getPlayer());
                    if (previousLocation == null) {
                        if (npcEntity.npCsConfigFields.noPreviousLocationMessage != null)
                            event.getPlayer().sendMessage(ChatColorConverter.convert(npcEntity.npCsConfigFields.noPreviousLocationMessage));
                    } else
                        PlayerPreTeleportEvent.teleportPlayer(event.getPlayer(), previousLocation);
                }
                break;
            case SCRAPPER:
                if (event.getPlayer().hasPermission("elitemobs.scrap.npc")) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            ScrapperMenu scrapperMenu = new ScrapperMenu();
                            scrapperMenu.constructScrapMenu(event.getPlayer());
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 1);
                }
                break;
            case SMELTER:
                if (event.getPlayer().hasPermission("elitemobs.smelt.npc")) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            SmeltMenu smeltMenu = new SmeltMenu();
                            smeltMenu.constructSmeltMenu(event.getPlayer());
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 1);
                }
                break;
            case REPAIRMAN:
                if (event.getPlayer().hasPermission("elitemobs.repair.npc")) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            RepairMenu repairMenu = new RepairMenu();
                            repairMenu.constructRepairMenu(event.getPlayer());
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 1);
                }
                break;
            case ENHANCER:
                event.getPlayer().sendMessage("[EliteMobs] This feature is coming back soon!");
                break;
                /*
                if (event.getPlayer().hasPermission("elitemobs.enhancer.npc")) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            EnhancementMenu enhancementMenu = new EnhancementMenu();
                            enhancementMenu.constructEnhancementMenu(event.getPlayer());
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 1);
                }
                break;

                 */
            case REFINER:
                if (event.getPlayer().hasPermission("elitemobs.refiner.npc")) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            RefinerMenu refinerMenu = new RefinerMenu();
                            refinerMenu.constructRefinerMenu(event.getPlayer());
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 1);
                }
                break;
            case UNBINDER:
                if (event.getPlayer().hasPermission("elitemobs.unbind.npc")) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            UnbindMenu unbindMenu = new UnbindMenu();
                            unbindMenu.constructUnbinderMenu(event.getPlayer());
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 1);
                }
                break;
            case ARENA_MASTER:
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        ArenaMenu arenaMenu = new ArenaMenu();
                        arenaMenu.constructArenaMenu(event.getPlayer(), npcEntity.getNPCsConfigFields().getArenaFilename());
                    }
                }.runTaskLater(MetadataHandler.PLUGIN, 1);
                break;
            case NONE:
            default:
                break;
            case COMMAND:
                if (npcEntity.getNPCsConfigFields().getCommand() == null) {
                    new WarningMessage("Failed to run NPC command because none is configured for " + npcEntity.getNPCsConfigFields().getFilename());
                    return;
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        event.getPlayer().performCommand(npcEntity.getNPCsConfigFields().getCommand());
                    }
                }.runTaskLater(MetadataHandler.PLUGIN, 1);
                break;

        }

    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {

        if (!event.getInventory().getType().equals(InventoryType.MERCHANT)) return;

        for (NPCEntity npcEntity : EntityTracker.getNpcEntities().values())
            if (event.getView().getTitle().equals(npcEntity.getNPCsConfigFields().getName())) {
                event.setCancelled(true);
                return;
            }

    }

    public enum NPCInteractionType {
        GUILD_GREETER,
        CHAT,
        CUSTOM_SHOP,
        PROCEDURALLY_GENERATED_SHOP,
        BAR,
        ARENA,
        QUEST_GIVER,
        CUSTOM_QUEST_GIVER,
        NONE,
        SELL,
        TELEPORT_BACK,
        SCRAPPER,
        SMELTER,
        REPAIRMAN,
        ENHANCER,
        REFINER,
        UNBINDER,
        ARENA_MASTER,
        COMMAND
    }


}
