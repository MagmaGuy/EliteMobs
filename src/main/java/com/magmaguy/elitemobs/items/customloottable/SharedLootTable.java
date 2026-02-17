package com.magmaguy.elitemobs.items.customloottable;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.InitializeConfig;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.menus.LootMenu;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.SpigotMessage;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SharedLootTable {
    @Getter
    private static final HashMap<EliteEntity, SharedLootTable> sharedLootTables = new HashMap<>();

    public static void shutdown() {
        sharedLootTables.clear();
    }

    private final int durationInSeconds = 60;
    @Getter
    private final List<ItemStack> loot = new ArrayList<>();
    private final EliteEntity eliteEntity;
    private final List<Player> damagers;
    private final List<LootMenu> lootMenus = new ArrayList<>();
    private final HashMap<Player, PlayerTable> playerTables = new HashMap<>();

    public SharedLootTable(EliteEntity eliteEntity) {
        this.eliteEntity = eliteEntity;

        this.damagers = eliteEntity.getDamagers().keySet().stream().toList();
        sharedLootTables.put(eliteEntity, this);
        damagers.forEach(damager -> lootMenus.add(new LootMenu(damager, this, getPlayerTable(damager))));
        if (damagers.size() > 1)
            Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, this::messagePlayers, 1);
        endLoot();
    }

    private void messagePlayers() {
        damagers.forEach(player -> {
            Logger.sendSimpleMessage(player, CommandMessagesConfig.getLootVoteSeparator());
            player.spigot().sendMessage(
                    SpigotMessage.simpleMessage(CommandMessagesConfig.getLootVoteMessage()),
                    SpigotMessage.commandHoverMessage(
                            InitializeConfig.getEmLootDisplay(),
                            InitializeConfig.getEmLootHover(),
                            "/em loot"),
                    SpigotMessage.simpleMessage(CommandMessagesConfig.getLootVoteMessageSuffix()
                            .replace("$count", String.valueOf(loot.size()))));
            Logger.sendSimpleMessage(player, CommandMessagesConfig.getLootVoteSeparator());
        });
    }

    public void addLoot(ItemStack itemStack) {
        loot.add(itemStack);
    }

    private void endLoot() {
        if (damagers.size() < 2) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    distribute();
                }
            }.runTaskLater(MetadataHandler.PLUGIN, 1);
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                distribute();
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20L * durationInSeconds);
    }

    private void distribute() {
        for (ItemStack lootInstance : loot) {
            List<Player> needPlayers = new ArrayList<>();
            for (PlayerTable playerTable : playerTables.values())
                for (ItemStack neededLoot : playerTable.getNeedItems()) {
                    if (neededLoot.isSimilar(lootInstance)) {
                        needPlayers.add(playerTable.player);
                    }
                }
            if (needPlayers.isEmpty()) {
                rollLoot(lootInstance, damagers);
            } else rollLoot(lootInstance, needPlayers);
        }
        sharedLootTables.remove(eliteEntity);
        lootMenus.forEach(LootMenu::removeMenu);
    }

    private void rollLoot(ItemStack item, List<Player> players) {
        Player player = players.get(ThreadLocalRandom.current().nextInt(players.size()));
        SoulbindEnchantment.addEnchantment(item, player);
        new EliteItemLore(item, false);
        int newLevel = CustomItem.limitItemLevel(player, EliteItemManager.getRoundedItemLevel(item));
        EliteItemManager.setEliteLevel(item, newLevel);
        HashMap<Integer, ItemStack> pendingItems = player.getInventory().addItem(item);
        if (!pendingItems.isEmpty()) player.getWorld().dropItemNaturally(player.getLocation(), item);
        players.forEach(thisPlayer -> thisPlayer.sendMessage(
                player.getDisplayName() + ChatColor.GREEN + " received " + item.getItemMeta().getDisplayName() + " !"));
    }

    public PlayerTable getPlayerTable(Player player) {
        PlayerTable playerTable = playerTables.get(player);
        if (playerTable == null) playerTable = new PlayerTable(player, this);
        return playerTable;
    }

    public class PlayerTable {
        @Getter
        private final List<ItemStack> needItems = new ArrayList<>();
        private final Player player;
        private final SharedLootTable sharedLootTable;

        public PlayerTable(Player player, SharedLootTable sharedLootTable) {
            this.player = player;
            this.sharedLootTable = sharedLootTable;
            playerTables.put(player, this);
        }

        public void addNeed(ItemStack itemStack) {
            needItems.add(itemStack);
            sharedLootTable.damagers.forEach(damager -> damager.sendMessage(ChatColorConverter.convert(
                    CommandMessagesConfig.getLootNeedMessage()
                            .replace("$player", player.getDisplayName())
                            .replace("$item", itemStack.getItemMeta().getDisplayName()))));
        }

        public void removeNeed(ItemStack itemStack) {
            needItems.remove(itemStack);
            sharedLootTable.damagers.forEach(damager -> damager.sendMessage(ChatColorConverter.convert(
                    CommandMessagesConfig.getLootGreedMessage()
                            .replace("$player", player.getDisplayName())
                            .replace("$item", itemStack.getItemMeta().getDisplayName()))));
        }
    }
}
