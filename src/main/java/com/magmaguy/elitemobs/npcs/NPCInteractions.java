package com.magmaguy.elitemobs.npcs;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.PlayerPreTeleportEvent;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.menus.*;
import com.magmaguy.elitemobs.menus.gambling.BettingMenu;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.QuestInteractionHandler;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.Logger;
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
import java.util.Set;
import java.util.UUID;

public class NPCInteractions implements Listener {

    private static final Set<UUID> cooldowns = new HashSet<>();

    public static void shutdown() {
        cooldowns.clear();
    }

    public static void handleNPCInteraction(Player player, NPCEntity npcEntity) {
        UUID playerUUID = player.getUniqueId();
        if (cooldowns.contains(playerUUID)) return;
        cooldowns.add(playerUUID);
        Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> cooldowns.remove(playerUUID), 1);

        switch (npcEntity.getNPCsConfigFields().getInteractionType()) {
            case GUILD_GREETER:
                if (player.hasPermission("elitemobs.skill.npc")) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            SkillBonusMenu.openWeaponSelectMenu(player);
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 1);
                }
                break;
            case CHAT:
                npcEntity.sayDialog(player);
                break;
            case CUSTOM_SHOP:
                if (player.hasPermission("elitemobs.shop.custom.npc"))
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            CustomShopMenu.customShopInitializer(player);
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 1);

                break;
            case PROCEDURALLY_GENERATED_SHOP:
                if (player.hasPermission("elitemobs.shop.dynamic.npc"))
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            ProceduralShopMenu.shopInitializer(player);
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 1);
                break;
            case QUEST_GIVER:
                if (player.hasPermission("elitemobs.quest.npc"))
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            QuestInteractionHandler.processDynamicQuests(player, npcEntity);
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 1);
                break;
            case CUSTOM_QUEST_GIVER:
                QuestInteractionHandler.processNPCQuests(player, npcEntity);
                break;
            case BAR:
                player.sendMessage("[EliteMobs] This feature is coming soon!");
                break;
            case SELL:
                if (player.hasPermission("elitemobs.shop.sell.npc"))
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            SellMenu sellMenu = new SellMenu();
                            sellMenu.constructSellMenu(player);
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 1);
                break;
            case TELEPORT_BACK:
                if (player.hasPermission("elitemobs.back.npc")) {
                    Location previousLocation = PlayerData.getBackTeleportLocation(player);
                    if (previousLocation == null) {
                        if (npcEntity.npCsConfigFields.noPreviousLocationMessage != null)
                            player.sendMessage(ChatColorConverter.convert(npcEntity.npCsConfigFields.noPreviousLocationMessage));
                    } else
                        PlayerPreTeleportEvent.teleportPlayer(player, previousLocation);
                }
                break;
            case SCRAPPER:
                if (player.hasPermission("elitemobs.scrap.npc")) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            ScrapperMenu scrapperMenu = new ScrapperMenu();
                            scrapperMenu.constructScrapMenu(player);
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 1);
                }
                break;
            case REPAIRMAN:
                if (player.hasPermission("elitemobs.repair.npc")) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            RepairMenu repairMenu = new RepairMenu();
                            repairMenu.constructRepairMenu(player);
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 1);
                }
                break;
            case UNBINDER:
                if (player.hasPermission("elitemobs.unbind.npc")) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            UnbindMenu unbindMenu = new UnbindMenu();
                            unbindMenu.constructUnbinderMenu(player);
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 1);
                }
                break;
            case ARENA_MASTER:
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        ArenaMenu arenaMenu = new ArenaMenu();
                        arenaMenu.constructArenaMenu(player, npcEntity.getNPCsConfigFields().getArenaFilename());
                    }
                }.runTaskLater(MetadataHandler.PLUGIN, 1);
                break;
            case NONE:
            default:
                break;
            case COMMAND:
                if (npcEntity.getNPCsConfigFields().getCommand() == null) {
                    Logger.warn("Failed to run NPC command because none is configured for " + npcEntity.getNPCsConfigFields().getFilename());
                    return;
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.performCommand(npcEntity.getNPCsConfigFields().getCommand());
                    }
                }.runTaskLater(MetadataHandler.PLUGIN, 1);
                break;
            case ENHANCER:
            case REFINER:
            case SMELTER:
                player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &cThis feature has been replaced! This NPC should be removed by an admin as soon as possible."));
                if (player.isOp() || player.hasPermission("elitemobs.*")) {
                    player.sendMessage(ChatColorConverter.convert("&2To remove this NPC, use the command &6/em remove &2and hit the NPC!"));
                }
                break;
            case ENCHANTER:
                if (player.hasPermission("elitemobs.enchant.npc"))
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            new ItemEnchantmentMenu(player);
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 1);
                break;
            case SCROLL_APPLIER:
                if (player.hasPermission("elitemobs.scroll.npc"))
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            new EliteScrollMenu(player);
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 1);
                break;
            case ARROW_SHOP:
                if (player.hasPermission("elitemobs.shop.arrow.npc"))
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            ArrowShopMenu.openArrowShop(player);
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 1);
                break;
            case GAMBLING_BLACKJACK:
                if (player.hasPermission("elitemobs.gambling.npc"))
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            BettingMenu.openBettingMenu(player, BettingMenu.GameType.BLACKJACK);
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 1);
                break;
            case GAMBLING_COINFLIP:
                if (player.hasPermission("elitemobs.gambling.npc"))
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            BettingMenu.openBettingMenu(player, BettingMenu.GameType.COIN_FLIP);
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 1);
                break;
            case GAMBLING_SLOTS:
                if (player.hasPermission("elitemobs.gambling.npc"))
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            BettingMenu.openBettingMenu(player, BettingMenu.GameType.SLOTS);
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 1);
                break;
            case GAMBLING_HIGHERLOWER:
                if (player.hasPermission("elitemobs.gambling.npc"))
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            BettingMenu.openBettingMenu(player, BettingMenu.GameType.HIGHER_LOWER);
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 1);
                break;
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerNPCInteract(PlayerInteractAtEntityEvent event) {
        if (event.isCancelled()) return;
        NPCEntity npcEntity = EntityTracker.getNPCEntity(event.getRightClicked());
        if (npcEntity == null) return;
        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType().equals(Material.NAME_TAG)) {
            event.setCancelled(true);
            player.sendMessage("[EliteMobs] You can't rename NPCs using name tags!");
            return;
        }
        event.setCancelled(true);
        handleNPCInteraction(player, npcEntity);
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
        COMMAND,
        ENCHANTER,
        SCROLL_APPLIER,
        ARROW_SHOP,
        GAMBLING_BLACKJACK,
        GAMBLING_COINFLIP,
        GAMBLING_SLOTS,
        GAMBLING_HIGHERLOWER
    }


}
