package com.magmaguy.elitemobs.items.customloottable;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.InitializeConfig;
import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.menus.LootMenu;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.parties.Party;
import com.magmaguy.elitemobs.parties.PartyManager;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.SpigotMessage;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Need/greed loot pool used by instanced dungeon drops and normal Elite gear earned by a party.
 */
public class SharedLootTable {
    private static final int DURATION_SECONDS = 60;
    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    @Getter
    private static final HashMap<EliteEntity, SharedLootTable> sharedLootTables = new HashMap<>();
    private static final Map<UUID, SharedLootTable> partyLootTables = new HashMap<>();

    public static void shutdown() {
        List<SharedLootTable> tables = new ArrayList<>(sharedLootTables.values());
        for (SharedLootTable table : partyLootTables.values())
            if (!tables.contains(table)) tables.add(table);
        tables.forEach(SharedLootTable::closeWithoutDistribution);
        sharedLootTables.clear();
        partyLootTables.clear();
    }

    /**
     * Adds an item to the active party vote. Returns false when fewer than two party members are eligible nearby,
     * allowing the caller to fall back to the normal personal drop path.
     */
    public static boolean addPartyLoot(EliteEntity eliteEntity, Player contributor, ItemStack itemStack) {
        if (itemStack == null || !PartyManager.shouldUsePartyLoot(contributor, eliteEntity)) return false;
        Party party = PartyManager.getParty(contributor.getUniqueId());
        if (party == null) return false;
        List<Player> eligiblePlayers = new ArrayList<>(PartyManager.getNearbyMembers(party, eliteEntity.getLocation()));
        if (eliteEntity instanceof InstancedBossEntity instancedBossEntity)
            eligiblePlayers.removeIf(instancedBossEntity.getLockoutPlayers()::contains);
        if (eligiblePlayers.size() < 2) return false;

        SharedLootTable table = partyLootTables.get(party.getId());
        boolean created = table == null || table.closed;
        if (created) {
            table = new SharedLootTable(eliteEntity, eligiblePlayers, party.getId());
            partyLootTables.put(party.getId(), table);
        }
        List<Player> newParticipants = table.addParticipants(eligiblePlayers);
        table.addLoot(itemStack, eligiblePlayers);
        if (!created && !newParticipants.isEmpty()) table.messagePlayersLater(newParticipants);
        return true;
    }

    public static void onPlayerQuit(UUID playerId) {
        java.util.HashSet<SharedLootTable> tables = new java.util.HashSet<>(sharedLootTables.values());
        tables.addAll(partyLootTables.values());
        tables.forEach(table -> table.removeParticipant(playerId));
    }

    /** Removes a leaver only from their former party vote, preserving unrelated dungeon votes. */
    public static void onPartyMemberLeave(UUID partyId, UUID playerId) {
        SharedLootTable table = partyLootTables.get(partyId);
        if (table != null) table.removeParticipant(playerId);
    }

    private final List<LootRollEntry> loot = new ArrayList<>();
    private final EliteEntity eliteEntity;
    private final UUID partyId;
    private final Location fallbackDropLocation;
    private final long hardDeadlineNanos;
    private final LinkedHashMap<UUID, Player> participants = new LinkedHashMap<>();
    private final Map<UUID, LootMenu> lootMenus = new HashMap<>();
    private final HashMap<UUID, PlayerTable> playerTables = new HashMap<>();
    private long lastLootAddedNanos;
    private BukkitTask distributionTask;
    private boolean closed;

    /** Creates the original dungeon-wide vote pool from the elite's combat contributors. */
    public SharedLootTable(EliteEntity eliteEntity) {
        this(eliteEntity, eligibleDungeonParticipants(eliteEntity), null);
        sharedLootTables.put(eliteEntity, this);
    }

    private static Collection<Player> eligibleDungeonParticipants(EliteEntity eliteEntity) {
        List<Player> eligible = new ArrayList<>(eliteEntity.getDamagers().keySet());
        if (eliteEntity instanceof InstancedBossEntity instancedBossEntity)
            eligible.removeIf(instancedBossEntity.getLockoutPlayers()::contains);
        return eligible;
    }

    private SharedLootTable(EliteEntity eliteEntity, Collection<Player> initialParticipants, UUID partyId) {
        this.eliteEntity = eliteEntity;
        this.partyId = partyId;
        this.fallbackDropLocation = eliteEntity.getLocation() == null ? null : eliteEntity.getLocation().clone();
        this.lastLootAddedNanos = System.nanoTime();
        this.hardDeadlineNanos = partyId == null
                ? Long.MAX_VALUE
                : lastLootAddedNanos + PartyConfig.getLootVoteMaximumLifetimeSeconds() * NANOS_PER_SECOND;
        addParticipants(initialParticipants);
        if (initialParticipants.size() > 1) messagePlayersLater(initialParticipants);
        if (initialParticipants.size() < 2)
            distributionTask = Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, this::distribute, 1L);
        else
            scheduleDistribution(DURATION_SECONDS * 20L);
    }

    public List<LootRollEntry> getLoot(Player player) {
        UUID playerId = player.getUniqueId();
        List<LootRollEntry> visibleLoot = new ArrayList<>();
        for (LootRollEntry entry : loot) {
            if (entry.eligiblePlayers().contains(playerId)) visibleLoot.add(entry);
        }
        return visibleLoot;
    }

    /** Backward-compatible view of every item in this roll session. */
    public List<ItemStack> getLoot() {
        return loot.stream().map(LootRollEntry::itemStack).toList();
    }

    public void addLoot(ItemStack itemStack) {
        addLoot(itemStack, participants.values());
    }

    private void addLoot(ItemStack itemStack, Collection<Player> eligiblePlayers) {
        if (itemStack == null || closed) return;
        loot.add(new LootRollEntry(
                UUID.randomUUID(),
                itemStack,
                eligiblePlayers.stream().map(Player::getUniqueId).collect(Collectors.toUnmodifiableSet())));
        lastLootAddedNanos = System.nanoTime();
    }

    private List<Player> addParticipants(Collection<Player> players) {
        List<Player> added = new ArrayList<>();
        for (Player player : players) {
            if (player == null || participants.containsKey(player.getUniqueId())) continue;
            participants.put(player.getUniqueId(), player);
            PlayerTable playerTable = getPlayerTable(player);
            LootMenu lootMenu = new LootMenu(player, this, playerTable);
            lootMenus.put(player.getUniqueId(), lootMenu);
            added.add(player);
        }
        return added;
    }

    private void removeParticipant(UUID playerId) {
        participants.remove(playerId);
        playerTables.remove(playerId);
        LootMenu lootMenu = lootMenus.remove(playerId);
        if (lootMenu != null) lootMenu.removeMenu();
    }

    private void messagePlayersLater(Collection<Player> players) {
        List<UUID> playerIds = players.stream().map(Player::getUniqueId).toList();
        Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> messagePlayers(playerIds), 1L);
    }

    private void messagePlayers(Collection<UUID> playerIds) {
        if (closed) return;
        for (UUID playerId : playerIds) {
            Player player = activeParticipant(playerId);
            if (player == null) continue;
            int lootCount = getLoot(player).size();
            if (lootCount == 0) continue;
            Logger.sendSimpleMessage(player, CommandMessagesConfig.getLootVoteSeparator());
            player.spigot().sendMessage(
                    SpigotMessage.simpleMessage(CommandMessagesConfig.getLootVoteMessage()),
                    SpigotMessage.commandHoverMessage(
                            InitializeConfig.getEmLootDisplay(),
                            InitializeConfig.getEmLootHover(),
                            "/em loot"),
                    SpigotMessage.simpleMessage(CommandMessagesConfig.getLootVoteMessageSuffix()
                            .replace("$count", String.valueOf(lootCount))));
            Logger.sendSimpleMessage(player, CommandMessagesConfig.getLootVoteSeparator());
        }
    }

    private void scheduleDistribution(long ticks) {
        if (closed) return;
        if (distributionTask != null) distributionTask.cancel();
        distributionTask = new BukkitRunnable() {
            @Override
            public void run() {
                distributionTask = null;
                if (closed) return;
                long inactivityDeadline = lastLootAddedNanos + DURATION_SECONDS * NANOS_PER_SECOND;
                long deadline = Math.min(inactivityDeadline, hardDeadlineNanos);
                long remainingNanos = deadline - System.nanoTime();
                if (remainingNanos > 0L) {
                    long remainingTicks = Math.max(1L, (remainingNanos + 49_999_999L) / 50_000_000L);
                    scheduleDistribution(remainingTicks);
                    return;
                }
                distribute();
            }
        }.runTaskLater(MetadataHandler.PLUGIN, ticks);
    }

    private void distribute() {
        if (closed) return;
        closed = true;
        cancelDistributionTask();
        try {
            for (LootRollEntry entry : loot) {
                List<Player> eligiblePlayers = activeParticipants(entry.eligiblePlayers());
                List<Player> needPlayers = new ArrayList<>();
                for (Player player : eligiblePlayers) {
                    PlayerTable playerTable = playerTables.get(player.getUniqueId());
                    if (playerTable == null) continue;
                    if (playerTable.needs(entry.id())) needPlayers.add(player);
                }
                // Players who do not choose Need stay in the Greed pool by default.
                rollLoot(entry.itemStack(), needPlayers.isEmpty() ? eligiblePlayers : needPlayers);
            }
        } finally {
            removeFromRegistry();
            cleanupMenus();
        }
    }

    private void rollLoot(ItemStack item, List<Player> players) {
        if (players.isEmpty()) {
            if (fallbackDropLocation != null && fallbackDropLocation.getWorld() != null) {
                org.bukkit.World loadedWorld = Bukkit.getWorld(fallbackDropLocation.getWorld().getUID());
                if (loadedWorld != null) loadedWorld.dropItemNaturally(fallbackDropLocation, item);
            }
            return;
        }
        Player winner = players.get(ThreadLocalRandom.current().nextInt(players.size()));
        SoulbindEnchantment.addEnchantment(item, winner);
        new EliteItemLore(item, false);
        Map<Integer, ItemStack> pendingItems = winner.getInventory().addItem(item);
        pendingItems.values().forEach(leftover -> winner.getWorld().dropItemNaturally(winner.getLocation(), leftover));
        String itemName = item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                ? item.getItemMeta().getDisplayName()
                : item.getType().toString().replace('_', ' ');
        for (Player participant : activeParticipants(null))
            participant.sendMessage(winner.getDisplayName() + ChatColor.GREEN + " received " + itemName + " !");
    }

    private List<Player> activeParticipants(Set<UUID> eligiblePlayerIds) {
        List<Player> active = new ArrayList<>();
        for (UUID playerId : participants.keySet()) {
            if (eligiblePlayerIds != null && !eligiblePlayerIds.contains(playerId)) continue;
            Player player = activeParticipant(playerId);
            if (player != null) active.add(player);
        }
        return active;
    }

    private Player activeParticipant(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline()) return null;
        return player;
    }

    private void removeFromRegistry() {
        if (partyId == null) sharedLootTables.remove(eliteEntity, this);
        else partyLootTables.remove(partyId, this);
    }

    private void cleanupMenus() {
        new ArrayList<>(lootMenus.values()).forEach(LootMenu::removeMenu);
        lootMenus.clear();
        playerTables.clear();
    }

    private void closeWithoutDistribution() {
        if (closed) return;
        closed = true;
        cancelDistributionTask();
        cleanupMenus();
    }

    private void cancelDistributionTask() {
        if (distributionTask == null) return;
        distributionTask.cancel();
        distributionTask = null;
    }

    public PlayerTable getPlayerTable(Player player) {
        return playerTables.computeIfAbsent(player.getUniqueId(), ignored -> new PlayerTable(player));
    }

    public class PlayerTable {
        private final Set<UUID> needEntries = new java.util.HashSet<>();
        private final Player player;

        private PlayerTable(Player player) {
            this.player = player;
        }

        public boolean needs(UUID entryId) {
            return needEntries.contains(entryId);
        }

        /** Backward-compatible item view for integrations which inspected the old need list. */
        public List<ItemStack> getNeedItems() {
            return loot.stream()
                    .filter(entry -> needEntries.contains(entry.id()))
                    .map(LootRollEntry::itemStack)
                    .toList();
        }

        public void setNeed(LootRollEntry entry, boolean need) {
            if (entry == null || !entry.eligiblePlayers().contains(player.getUniqueId())) return;
            if (need) needEntries.add(entry.id());
            else needEntries.remove(entry.id());
            activeParticipants(null).forEach(participant -> participant.sendMessage(ChatColorConverter.convert(
                    (need ? CommandMessagesConfig.getLootNeedMessage() : CommandMessagesConfig.getLootGreedMessage())
                            .replace("$player", player.getDisplayName())
                            .replace("$item", displayName(entry.itemStack())))));
        }

        public void addNeed(ItemStack itemStack) {
            loot.stream()
                    .filter(entry -> entry.eligiblePlayers().contains(player.getUniqueId()))
                    .filter(entry -> entry.itemStack().isSimilar(itemStack))
                    .filter(entry -> !needs(entry.id()))
                    .findFirst()
                    .ifPresent(entry -> setNeed(entry, true));
        }

        public void removeNeed(ItemStack itemStack) {
            loot.stream()
                    .filter(entry -> needs(entry.id()))
                    .filter(entry -> entry.itemStack().isSimilar(itemStack))
                    .findFirst()
                    .ifPresent(entry -> setNeed(entry, false));
        }

        private String displayName(ItemStack itemStack) {
            return itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()
                    ? itemStack.getItemMeta().getDisplayName()
                    : itemStack.getType().toString().replace('_', ' ');
        }
    }

    public record LootRollEntry(UUID id, ItemStack itemStack, Set<UUID> eligiblePlayers) {
    }
}
