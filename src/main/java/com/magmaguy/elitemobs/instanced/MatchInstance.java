package com.magmaguy.elitemobs.instanced;


import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.PlayerJoinArenaEvent;
import com.magmaguy.elitemobs.api.PlayerJoinDungeonEvent;
import com.magmaguy.elitemobs.api.PlayerLeaveArenaEvent;
import com.magmaguy.elitemobs.api.PlayerLeaveDungeonEvent;
import com.magmaguy.elitemobs.api.instanced.MatchDestroyEvent;
import com.magmaguy.elitemobs.api.instanced.MatchEndEvent;
import com.magmaguy.elitemobs.api.instanced.MatchInstantiateEvent;
import com.magmaguy.elitemobs.api.instanced.MatchJoinEvent;
import com.magmaguy.elitemobs.api.instanced.MatchLeaveEvent;
import com.magmaguy.elitemobs.api.instanced.MatchStartEvent;
import com.magmaguy.elitemobs.collateralminecraftchanges.AlternativeDurabilityLoss;
import com.magmaguy.elitemobs.config.ArenasConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.instanced.arena.ArenaInstance;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.magmacore.match.DeathPolicy;
import com.magmaguy.magmacore.match.LeaveReason;
import com.magmaguy.magmacore.match.Match;
import com.magmaguy.magmacore.match.MatchApi;
import com.magmaguy.magmacore.match.MatchCore;
import com.magmaguy.magmacore.match.MatchMessages;
import com.magmaguy.magmacore.match.MatchOutcome;
import com.magmaguy.magmacore.match.MatchPhase;
import com.magmaguy.magmacore.match.MatchPlayer;
import com.magmaguy.magmacore.match.MatchRole;
import com.magmaguy.magmacore.match.MatchSettings;
import com.magmaguy.magmacore.match.MatchSpace;
import com.magmaguy.magmacore.util.AttributeManager;
import com.magmaguy.magmacore.util.ChatColorConverter;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * EliteMobs' instance API, running on MagmaCore's match core. The core owns admission,
 * movement, rescue, death and teardown; this class keeps every member add-ons and EliteMobs'
 * own subclasses use, mirroring the core's state into the familiar fields and firing
 * EliteMobs' events at the moments they always fired.
 */
public abstract class MatchInstance extends Match {

    protected static final HashSet<MatchInstance> instances = new HashSet<>();
    @Getter
    protected final HashMap<Block, InstanceDeathLocation> deathBanners = new HashMap<>();
    @Getter
    protected final Map<Player, Location> previousPlayerLocations = new HashMap<>();
    @Getter
    protected HashSet<Player> players = new HashSet<>();
    protected HashMap<Player, Integer> playerLives = new HashMap<>();
    @Getter
    protected HashSet<Player> participants = new HashSet<>();
    @Getter
    protected final HashSet<UUID> startingParticipantIds = new HashSet<>();
    protected HashSet<Player> spectators = new HashSet<>();
    @Getter
    protected InstancedRegionState state = InstancedRegionState.WAITING;
    protected Location lobbyLocation = null;
    protected Location startLocation;
    protected Location exitLocation;
    protected int minPlayers;
    protected int maxPlayers;
    protected World world;
    @Getter
    protected String permission = null;
    @Getter
    protected boolean cancelled = false;
    @Getter
    protected boolean destroyingMatch = false;
    private boolean matchHasStarted = false;
    private boolean matchEndEventFired = false;
    private boolean matchDestroyEventFired = false;

    public MatchInstance(Location startLocation, Location exitLocation, int minPlayers, int maxPlayers) {
        this(new InstanceSpace(), startLocation, exitLocation, minPlayers, maxPlayers);
    }

    private MatchInstance(InstanceSpace space, Location startLocation, Location exitLocation,
                          int minPlayers, int maxPlayers) {
        super(settings(space, minPlayers, maxPlayers));
        space.owner = this;
        this.startLocation = startLocation;
        this.exitLocation = exitLocation;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        // MatchInstantiateEvent fires here, before subclass constructors run, as it always has.
        if (!open()) {
            cancelled = true;
            return;
        }
        instances.add(this);
    }

    private static MatchSettings settings(InstanceSpace space, int minPlayers, int maxPlayers) {
        MatchCore.enable(MetadataHandler.PLUGIN);
        int max = Math.max(1, maxPlayers);
        return MatchSettings.builder()
                .space(space)
                .players(Math.max(0, Math.min(minPlayers, max)), max)
                .countdownSeconds(3)
                .bypassPermission("elitemobs.*")
                .withinTeleportPermission("elitemobs.instanced.teleport.within")
                // Instances reset between runs; dungeons and trials retire themselves when done,
                // and every subclass schedules its own teardown after the match ends.
                .reusable(true)
                .destroyAfterEnd(false)
                .death(DungeonsConfig.isAllowSpectatorsInInstancedContent()
                        ? DeathPolicy.spectateAndRevive(3, InstanceDeathLocation::marker)
                        : DeathPolicy.eliminate())
                .messages(MatchMessages.builder()
                        .notAccepting(ArenasConfig.getArenasOngoingMessage())
                        .full(ArenasConfig.getArenaFullMessage())
                        .noPermission(null)
                        .alreadyInMatch(null)
                        .joinedMessage(ArenasConfig.getArenaJoinPlayerMessage())
                        .joinedTitle(ArenasConfig.getJoinPlayerTitle())
                        .joinedSubtitle(ArenasConfig.getJoinPlayerSubtitle())
                        .spectatorMessage(ArenasConfig.getArenaJoinSpectatorMessage())
                        .spectatorTitle(ArenasConfig.getJoinSpectatorTitle())
                        .spectatorSubtitle(ArenasConfig.getJoinSpectatorSubtitle())
                        .notEnoughPlayers(ArenasConfig.getNotEnoughPlayersMessage())
                        .startingTitle(ArenasConfig.getStartingTitle())
                        .startingSubtitle(ArenasConfig.getStartingSubtitle())
                        .waitingHint(ArenasConfig.getArenaStartHintMessage())
                        .build())
                .api(EliteMatchApi.INSTANCE)
                .build();
    }

    public static void shutdown() {
        HashSet<MatchInstance> cloneInstance = new HashSet<>(instances);
        cloneInstance.forEach(matchInstance -> {
            matchInstance.cancelScheduledTasks();
            matchInstance.destroyMatch();
            matchInstance.retire();
        });
        instances.clear();
        MatchCore.shutdown();
    }

    public static MatchInstance getPlayerInstance(Player player) {
        for (MatchInstance matchInstance : instances)
            for (Player iteratedPlayer : matchInstance.players)
                if (iteratedPlayer.equals(player)) return matchInstance;
        return null;
    }

    public static MatchInstance getSpectatorInstance(Player player) {
        for (MatchInstance matchInstance : instances)
            for (Player iteratedPlayer : matchInstance.spectators)
                if (iteratedPlayer.equals(player)) return matchInstance;
        return null;
    }

    public static MatchInstance getAnyPlayerInstance(Player player) {
        for (MatchInstance matchInstance : instances) {
            for (Player iteratedPlayer : matchInstance.players)
                if (iteratedPlayer.equals(player)) return matchInstance;
            for (Player iteratedPlayer : matchInstance.spectators)
                if (iteratedPlayer.equals(player)) return matchInstance;
        }
        return null;
    }

    /**
     * True when this match object is a corpse: cancelled at construction, or already evicted
     * from {@link #instances}. Dungeon instances leave that registry the moment teardown begins
     * but stay referenced by the dungeon browser while their world awaits deletion, and a
     * destroyed instance reads WAITING, so state alone cannot tell a fresh lobby from a dead one.
     */
    public boolean isDefunct() {
        return cancelled || !instances.contains(this);
    }

    /**
     * The single admission predicate. The dungeon browsers and the match core both consult it,
     * so a menu never offers an entry that admission then handles differently.
     */
    public boolean isAcceptingNewPlayers() {
        return !isDefunct() && !destroyingMatch && state == InstancedRegionState.WAITING;
    }

    /** Admitted players whose entry task has reached the lobby, including the start countdown. */
    public final boolean isWaitingPlayer(Player player) {
        return player != null && player.isOnline() && !player.isDead()
                && !isDefunct() && !destroyingMatch
                && (state == InstancedRegionState.WAITING || state == InstancedRegionState.STARTING)
                && PlayerData.getMatchInstance(player) == this
                && players.contains(player) && !spectators.contains(player)
                && playerLives.containsKey(player)
                && player.getWorld().equals(lobbyLocation == null ? world : lobbyLocation.getWorld());
    }

    protected boolean isAcceptingSpectator(Player player, boolean wasPlayer) {
        return true;
    }

    /**
     * True only when both the active participant and a prospective combat target remain inside
     * this ongoing instance. This is the public policy seam for mechanics that must not reach
     * across an instance boundary.
     */
    public final boolean authorizesCombatTarget(Player player, Location targetLocation) {
        return player != null
                && targetLocation != null
                && targetLocation.getWorld() != null
                && state == InstancedRegionState.ONGOING
                && players.contains(player)
                && world != null
                && world.equals(player.getWorld())
                && world.equals(targetLocation.getWorld())
                && isInRegion(player.getLocation())
                && isInRegion(targetLocation);
    }

    /** Returns this participant's remaining dungeon/arena lives, or null when no counter is active. */
    public Integer getRemainingLives(Player player) {
        return playerLives.get(player);
    }

    /** True for a player currently waiting in this match's spectator/downed state. */
    public boolean isSpectator(Player player) {
        return spectators.contains(player);
    }

    public boolean addNewPlayer(Player player) {
        return InstancePlayerManager.addNewPlayer(player, this);
    }

    public void removePlayer(Player player) {
        leave(player, LeaveReason.QUIT);
    }

    /** Participant exits are independent of the boundary used to eject intruders. */
    protected Location participantExitLocation(Player player) {
        return exitLocation;
    }

    protected Location previousLocationOrExit(Player player) {
        Location previous = previousPlayerLocations.get(player);
        if (previous == null || previous.getWorld() == null
                || Bukkit.getWorld(previous.getWorld().getUID()) != previous.getWorld()) return exitLocation;
        return previous.clone();
    }

    public void playerDeath(Player player) {
        handleDeath(player);
    }

    public void revivePlayer(Player player, InstanceDeathLocation deathLocation) {
        releaseReviveBanner(player, true);
    }

    /** Spectating after death is the core's job; this admits outsiders who want to watch. */
    public void addSpectator(Player player, boolean wasPlayer) {
        if (!wasPlayer) admitSpectator(player);
    }

    public void removeSpectator(Player player) {
        leave(player, LeaveReason.QUIT);
    }

    public void removeAnyKind(Player player) {
        leave(player, LeaveReason.QUIT);
    }

    public void countdownMatch() {
        if (state != InstancedRegionState.WAITING) return;
        start();
        if (getPhase() == MatchPhase.STARTING) state = InstancedRegionState.STARTING;
    }

    /** Subclasses cancel their own tasks here; the match core owns the watchdog and countdown. */
    protected void cancelScheduledTasks() {
    }

    protected void announce(String message) {
        participants.forEach(player -> player.sendMessage(ChatColorConverter.convert(message)));
    }

    protected abstract boolean isInRegion(Location location);

    /**
     * Whether non-participants are kept from teleporting into this instance during the phase.
     * Arenas and trials share their world, so only a running match closes its door; dungeons
     * own their world and override this.
     */
    protected boolean guardsEntryDuring(MatchPhase phase) {
        return phase == MatchPhase.STARTING || phase == MatchPhase.ONGOING;
    }

    protected void startMatch() {
        matchHasStarted = true;
        matchEndEventFired = false;
        matchDestroyEventFired = false;
        state = InstancedRegionState.ONGOING;
        startingParticipantIds.clear();
        startingParticipantIds.addAll(getStartingRoster());
        new EventCaller(new MatchStartEvent(this));
    }

    /*
    This is useful for extending behavior down the line like the enchanted dungeon loot
     */
    protected void victory() {
        endWith(MatchOutcome.VICTORY, InstancedRegionState.COMPLETED_VICTORY);
    }

    protected void defeat() {
        endWith(MatchOutcome.DEFEAT, InstancedRegionState.COMPLETED_DEFEAT);
    }

    private void endWith(MatchOutcome outcome, InstancedRegionState endState) {
        if (getPhase() == MatchPhase.ENDED || isDestroyed()) return;
        state = endState;
        end(outcome);
        endMatch();
    }

    protected void endMatch() {
        if (getPhase() != MatchPhase.ENDED && !isDestroyed()) {
            if (state != InstancedRegionState.COMPLETED_VICTORY && state != InstancedRegionState.COMPLETED_DEFEAT)
                state = InstancedRegionState.COMPLETED;
            end(MatchOutcome.NEUTRAL);
        }
        if (matchHasStarted && !matchEndEventFired) {
            matchEndEventFired = true;
            new EventCaller(new MatchEndEvent(this));
        }
    }

    /** Removes everyone and returns the instance to WAITING. Dungeons and trials then retire it. */
    protected void destroyMatch() {
        if (destroyingMatch) return;
        destroyingMatch = true;
        try {
            destroy();
        } finally {
            destroyingMatch = false;
        }
    }

    /** Takes the instance out of EliteMobs' registry and destroys its match for good. */
    protected final void retireInstance() {
        instances.remove(this);
        retire();
    }

    protected boolean hasMatchDestroyEventFired() {
        return matchDestroyEventFired;
    }

    protected InstanceDeathLocation getDeathLocationByPlayer(Player player) {
        for (InstanceDeathLocation deathLocation : deathBanners.values())
            if (deathLocation.getDeadPlayer().equals(player))
                return deathLocation;
        return null;
    }

    boolean releaseBanner(Player dead, boolean revive) {
        return releaseReviveBanner(dead, revive);
    }

    // Match core hooks.

    @Override
    protected MatchPlayer createPlayer(Player player) {
        EliteMatchPlayer participant = new EliteMatchPlayer(player, this);
        // Registered before any join feedback, as EliteMobs always has: a whole party is visible
        // in the instance before the first member's join event.
        participants.add(player);
        players.add(player);
        previousPlayerLocations.put(player, participant.getPreviousLocation());
        PlayerData.setMatchInstance(player, this);
        return participant;
    }

    @Override
    protected void onAdmissionRolledBack(MatchPlayer participant) {
        Player player = participant.getPlayer();
        forget(player);
        previousPlayerLocations.remove(player);
    }

    @Override
    protected boolean acceptsPlayers() {
        return isAcceptingNewPlayers();
    }

    @Override
    protected boolean acceptsSpectator(Player player) {
        return !isDefunct() && !destroyingMatch && isAcceptingSpectator(player, false);
    }

    @Override
    protected String requiredPermission() {
        return permission;
    }

    @Override
    protected Location entryDestination(MatchPlayer participant) {
        return state == InstancedRegionState.WAITING && lobbyLocation != null ? lobbyLocation : startLocation;
    }

    @Override
    protected Location startDestination() {
        return startLocation;
    }

    @Override
    protected Location exitDestination(MatchPlayer participant) {
        Player player = participant.getPlayer();
        // A death has always sent the player back where they came from, before the exit.
        if (participant.getLeaveReason() == LeaveReason.DIED) {
            Location previous = previousPlayerLocations.get(player);
            if (previous != null && previous.getWorld() != null
                    && Bukkit.getWorld(previous.getWorld().getUID()) == previous.getWorld()) return previous.clone();
            return exitLocation;
        }
        return participantExitLocation(player);
    }

    @Override
    protected Location intruderDestination(Player player) {
        if (exitLocation != null) return exitLocation;
        Location back = PlayerData.getBackTeleportLocation(player);
        if (back != null) return back;
        Location spawn = DefaultConfig.getDefaultSpawnLocation();
        return spawn != null && spawn.getWorld() != null ? spawn : null;
    }

    @Override
    protected void onStart() {
        state = InstancedRegionState.ONGOING;
        startMatch();
    }

    /** Automatic endings go through the overridable methods EliteMobs subclasses intercept. */
    @Override
    protected void requestEnd(MatchOutcome outcome) {
        switch (outcome) {
            case VICTORY -> victory();
            case DEFEAT -> defeat();
            case NEUTRAL -> endMatch();
        }
    }

    @Override
    protected void onDeath(MatchPlayer participant) {
        Player player = participant.getPlayer();
        AlternativeDurabilityLoss.doDurabilityLoss(player);
        AttributeManager.setAttribute(player, "generic_max_health",
                AttributeManager.getAttributeBaseValue(player, "generic_max_health"));
    }

    @Override
    protected void onSpectating(MatchPlayer participant) {
        players.remove(participant.getPlayer());
        spectators.add(participant.getPlayer());
    }

    @Override
    protected void onRevive(MatchPlayer participant) {
        Player player = participant.getPlayer();
        spectators.remove(player);
        players.add(player);
        playerLives.put(player, participant.getLives());
    }

    @Override
    protected void onReset() {
        state = InstancedRegionState.WAITING;
        startingParticipantIds.clear();
        matchHasStarted = false;
    }

    private void forget(Player player) {
        participants.remove(player);
        players.remove(player);
        spectators.remove(player);
        playerLives.remove(player);
        if (PlayerData.getMatchInstance(player) == this) PlayerData.setMatchInstance(player, null);
    }

    private void fireTypedJoinEvent(Player player) {
        if (this instanceof ArenaInstance arenaInstance)
            new EventCaller(new PlayerJoinArenaEvent(arenaInstance, player));
        else if (this instanceof DungeonInstance dungeonInstance)
            new EventCaller(new PlayerJoinDungeonEvent(dungeonInstance, player));
    }

    private void fireLeaveEvents(Player player) {
        new EventCaller(new MatchLeaveEvent(this, player));
        if (this instanceof ArenaInstance arenaInstance)
            new EventCaller(new PlayerLeaveArenaEvent(arenaInstance, player));
        else if (this instanceof DungeonInstance dungeonInstance)
            new EventCaller(new PlayerLeaveDungeonEvent(dungeonInstance, player));
    }

    public enum InstancedRegionState {
        WAITING, STARTING, ONGOING, COMPLETED, COMPLETED_VICTORY, COMPLETED_DEFEAT
    }

    /**
     * Kept so existing registrations compile. Quits, lethal damage, revive banners and the
     * teleport guard are now handled by MagmaCore's match core for every instance.
     */
    public static class MatchInstanceEvents implements Listener {
    }

    /** The instance's region as the match core sees it. EliteMobs retires its own worlds. */
    private static final class InstanceSpace implements MatchSpace {
        private MatchInstance owner;

        @Override
        public boolean contains(Location location) {
            if (owner == null || location == null || location.getWorld() == null || owner.world == null) return false;
            try {
                return owner.isInRegion(location);
            } catch (RuntimeException cleanedUp) {
                // Dungeon cleanup clears the start location isInRegion reads.
                return false;
            }
        }

        @Override
        public Collection<World> worlds() {
            return owner == null || owner.world == null ? List.of() : List.of(owner.world);
        }

        @Override
        public boolean guardsEntryDuring(MatchPhase phase) {
            return owner != null && owner.guardsEntryDuring(phase);
        }

        @Override
        public void teardown() {
        }
    }

    /** Fires EliteMobs' API events from the match core's callbacks. */
    private static final class EliteMatchApi implements MatchApi {
        private static final EliteMatchApi INSTANCE = new EliteMatchApi();

        @Override
        public boolean instantiateAttempt(Match match) {
            MatchInstantiateEvent event = new MatchInstantiateEvent((MatchInstance) match);
            new EventCaller(event);
            return !event.isCancelled();
        }

        @Override
        public boolean joinAttempt(Match match, Player player) {
            if (!PlayerData.isInMemory(player.getUniqueId())) return false;
            MatchJoinEvent event = new MatchJoinEvent((MatchInstance) match, player);
            new EventCaller(event);
            return !event.isCancelled();
        }

        @Override
        public void joined(Match match, MatchPlayer participant) {
            MatchInstance instance = (MatchInstance) match;
            Player player = participant.getPlayer();
            if (participant.getRole() == MatchRole.SPECTATOR) {
                instance.players.remove(player);
                instance.spectators.add(player);
            }
            instance.fireTypedJoinEvent(player);
        }

        @Override
        public void left(Match match, MatchPlayer participant, LeaveReason reason) {
            MatchInstance instance = (MatchInstance) match;
            Player player = participant.getPlayer();
            boolean wasParticipant = instance.participants.contains(player);
            instance.forget(player);
            if (wasParticipant) instance.fireLeaveEvents(player);
        }

        @Override
        public void destroyed(Match match) {
            MatchInstance instance = (MatchInstance) match;
            if (instance.matchDestroyEventFired) return;
            instance.matchDestroyEventFired = true;
            new EventCaller(new MatchDestroyEvent(instance));
        }
    }
}
