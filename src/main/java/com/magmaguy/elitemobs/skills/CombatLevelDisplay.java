package com.magmaguy.elitemobs.skills;

import com.magmaguy.easyminecraftgoals.NMSManager;
import com.magmaguy.easyminecraftgoals.internal.FakeText;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.combatsystem.combattag.PlayerCombatState;
import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.experimentalcombat.CombatHealthFormatter;
import com.magmaguy.elitemobs.thirdparty.geyser.GeyserDetector;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages combat level text displays above players using packet-based FakeText.
 * <p>
 * The display shows the player's combat level calculated from their skills.
 * Uses FakeText from EasyMinecraftGoals for packet-based display that mounts
 * on players - the client handles positioning automatically.
 */
public class CombatLevelDisplay implements Listener {

    private static final float DEFAULT_Y_TRANSLATION = 0.5f;
    private static final float BEDROCK_Y_TRANSLATION_BONUS = 1.0f;
    private static final Map<UUID, PlayerDisplay> playerDisplays = new ConcurrentHashMap<>();
    private static PlayerCombatState combatState;
    private static BukkitTask refreshTask;

    public static void setCombatState(PlayerCombatState state) {
        combatState = state;
    }

    private static final class PlayerDisplay {
        private final FakeText display;
        private String text;
        private boolean healthVisible;

        private PlayerDisplay(FakeText display, String text, boolean healthVisible) {
            this.display = display;
            this.text = text;
            this.healthVisible = healthVisible;
        }
    }

    private static boolean shouldShowHealth(Player player) {
        return !player.isDead() && combatState != null
                && combatState.isInCombat(player.getUniqueId());
    }

    private static String renderText(Player player, boolean showHealth) {
        String identity = PlayerIdentityLabelRenderer.render(player.getUniqueId());
        if (!showHealth) return identity;
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        double current = player.getHealth();
        double maximum = maxHealth == null ? current : maxHealth.getValue();
        return "&c❤ " + CombatHealthFormatter.format(current) + "/"
                + CombatHealthFormatter.format(maximum) + "\n&r" + identity;
    }

    /**
     * Creates a combat level display for a player.
     *
     * @param player The player to create the display for
     */
    public static void createDisplay(Player player) {
        if (!shouldRender(player)) {
            removeDisplay(player);
            return;
        }
        if (NMSManager.getAdapter() == null) return;

        // Remove existing display if present
        removeDisplay(player);

        // Create the FakeText display at the player's location (will be mounted)
        boolean showHealth = shouldShowHealth(player);
        String text = renderText(player, showHealth);
        FakeText fakeText = NMSManager.getAdapter().fakeTextBuilder()
                .text(ChatColorConverter.convert(text))
                .billboard(Display.Billboard.CENTER)
                .shadow(true)
                .seeThrough(false)
                .translation(0, getDisplayHeight(player), 0)
                .viewerFilter(viewer -> canSeeNameTag(player, viewer))
                .build(player.getLocation());

        playerDisplays.put(player.getUniqueId(), new PlayerDisplay(fakeText, text, showHealth));

        // Attach to the player - this mounts and registers with the global tracker
        // which handles visibility, world changes, respawns, etc. automatically
        fakeText.attachTo(player);
    }

    /**
     * Mirrors name-tag visibility exposed by the Spigot API. Packet-only changes made by other plugins
     * are not represented by Player or Scoreboard state and therefore cannot be detected here.
     */
    private static boolean canSeeNameTag(Player player, Player viewer) {
        if (!viewer.canSee(player)) return false;

        Scoreboard scoreboard = viewer.getScoreboard();
        Team playerTeam = scoreboard.getEntryTeam(player.getName());
        if (playerTeam == null) return true;

        Team viewerTeam = scoreboard.getEntryTeam(viewer.getName());
        boolean sameTeam = playerTeam.equals(viewerTeam);
        return switch (playerTeam.getOption(Team.Option.NAME_TAG_VISIBILITY)) {
            case ALWAYS -> true;
            case NEVER -> false;
            case FOR_OWN_TEAM -> sameTeam;
            case FOR_OTHER_TEAMS -> !sameTeam;
        };
    }

    private static float getDisplayHeight(Player player) {
        return DEFAULT_Y_TRANSLATION + (GeyserDetector.bedrockPlayer(player) ? BEDROCK_Y_TRANSLATION_BONUS : 0f);
    }

    /**
     * Removes the combat level display from a player.
     *
     * @param player The player to remove the display from
     */
    public static void removeDisplay(Player player) {
        PlayerDisplay display = playerDisplays.remove(player.getUniqueId());
        if (display != null) {
            display.display.detach(); // Unregisters from tracker and hides from all viewers
        }
    }

    /**
     * Updates the combat level display text for a player.
     *
     * @param player The player to update the display for
     */
    public static void updateDisplay(Player player) {
        if (!shouldRender(player)) {
            removeDisplay(player);
            return;
        }

        PlayerDisplay display = playerDisplays.get(player.getUniqueId());
        if (display != null) {
            boolean showHealth = shouldShowHealth(player);
            String text = renderText(player, showHealth);
            if (!text.equals(display.text)) {
                display.display.setText(ChatColorConverter.convert(text));
                display.text = text;
            }
            display.healthVisible = showHealth;
        } else {
            // Recreate if missing
            createDisplay(player);
        }
    }

    /**
     * Updates the display for a player by UUID.
     *
     * @param playerUUID The player's UUID
     */
    public static void updateDisplay(UUID playerUUID) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null && player.isOnline()) {
            updateDisplay(player);
        }
    }

    /**
     * Cleans up all displays.
     * Called on plugin shutdown.
     */
    public static void shutdown() {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
        combatState = null;
        for (PlayerDisplay display : playerDisplays.values()) {
            display.display.detach();
        }
        playerDisplays.clear();
    }

    /**
     * Initializes displays for all online players.
     * Called on plugin startup.
     */
    public static void initialize() {
        shutdown();
        // Delay initialization to ensure players are fully loaded
        refreshTask = new BukkitRunnable() {
            private boolean initialized;

            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!initialized) {
                        createDisplay(player);
                        continue;
                    }
                    PlayerDisplay display = playerDisplays.get(player.getUniqueId());
                    if (display == null) {
                        // Bed, world-change and respawn handlers own recreation after
                        // passenger synchronization; the refresh must not race them.
                        continue;
                    }
                    if (display.healthVisible || shouldShowHealth(player)) updateDisplay(player);
                }
                initialized = true;
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 20L, 5L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Remove the quitting player's display
        removeDisplay(event.getPlayer());
        // The global tracker handles hiding other displays from the quitting player
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        // When a player sleeps, the server re-syncs passenger data which doesn't include
        // our packet-only entity, causing it to detach and float. Remove it preemptively.
        if (event.useBed() != Event.Result.ALLOW
                && event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) return;
        removeDisplay(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        if (!shouldRender(player)) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;
                createDisplay(player);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 5L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        // Same passenger-resync issue as bed entry: the dimension-change handshake
        // makes the server re-broadcast the player's passenger list without our
        // packet-only entity, leaving the display detached in the destination
        // world. Tear it down preemptively and rebuild after the handshake
        // settles. The MagmaCore tracker tries to remount on its own one tick
        // later, but that races with the server's own sync, which can land last
        // and wipe the mount.
        Player player = event.getPlayer();
        if (!shouldRender(player)) {
            removeDisplay(player);
            return;
        }
        removeDisplay(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;
                createDisplay(player);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // Same passenger-resync class of issue as bed/world-change. On respawn
        // the client tears down its entity tracking, but server-side the packet
        // entity is still flagged visible to the player, so MagmaCore's tracker
        // doesn't resend a spawn packet — and its follow-up remount references
        // an entity ID the client no longer knows about. Tear it down here and
        // rebuild so a fresh spawn + mount goes out.
        Player player = event.getPlayer();
        if (!shouldRender(player)) {
            removeDisplay(player);
            return;
        }
        removeDisplay(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;
                createDisplay(player);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20L);
    }

    private static boolean shouldRender(Player player) {
        if (PlayerIdentityLabelRenderer.hasClassLabel(player.getUniqueId())) return true;
        return SkillsConfig.isSkillSystemEnabled()
                && SkillsConfig.isShowCombatLevelDisplay()
                && !SkillsConfig.isWorldExcludedFromSkills(player);
    }
}
