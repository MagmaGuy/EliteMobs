package com.magmaguy.elitemobs.skills;

import com.magmaguy.easyminecraftgoals.NMSManager;
import com.magmaguy.easyminecraftgoals.internal.FakeText;
import com.magmaguy.easyminecraftgoals.internal.FollowingText;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.combatsystem.combattag.PlayerCombatState;
import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.advancedcombat.CombatHealthFormatter;
import com.magmaguy.easyminecraftgoals.thirdparty.BedrockChecker;
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
 * Uses client-positioned passengers when available, with following text for occupied players.
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
        private final FollowingText tracking;
        private String text;

        private PlayerDisplay(FakeText display, FollowingText tracking, String text) {
            this.display = display;
            this.tracking = tracking;
            this.text = text;
        }

        private boolean isPassenger() {
            return tracking == null;
        }

        private boolean isValid(Player player) {
            return tracking != null ? tracking.isValid()
                    : display.isAutoTracked() && player.equals(display.getVehicle());
        }

        private void close() {
            if (tracking != null) {
                tracking.close();
            } else {
                display.detach();
                display.remove();
            }
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

        // The bottom of this label sits above the native name; extra lines grow upward.
        boolean showHealth = shouldShowHealth(player);
        String text = renderText(player, showHealth);
        boolean passenger = shouldUsePassenger(player);
        FakeText fakeText = NMSManager.getAdapter().fakeTextBuilder()
                .text(ChatColorConverter.convert(text))
                .billboard(Display.Billboard.CENTER)
                .shadow(true)
                .seeThrough(false)
                .translation(0, passenger ? getDisplayHeight(player) : 0, 0)
                .viewerFilter(viewer -> canSeeNameTag(player, viewer))
                .build(player.getLocation());

        FollowingText tracking = null;
        if (passenger) {
            fakeText.attachTo(player);
        } else {
            tracking = new FollowingText(fakeText, player,
                    () -> player.getLocation().add(0, player.getHeight() + getDisplayHeight(player), 0),
                    viewer -> canSeeNameTag(player, viewer));
        }
        playerDisplays.put(player.getUniqueId(), new PlayerDisplay(fakeText, tracking, text));
    }

    private static boolean shouldUsePassenger(Player player) {
        // Our text is packet-only, so it never appears in Bukkit's passenger list.
        return DefaultConfig.isUsePassengerCombatLevelDisplay() && player.getPassengers().isEmpty();
    }

    /**
     * Mirrors name-tag visibility exposed by the Spigot API. Packet-only changes made by other plugins
     * are not represented by Player or Scoreboard state and therefore cannot be detected here.
     */
    private static boolean canSeeNameTag(Player player, Player viewer) {
        if (!viewer.canSee(player) || player.isInvisible()) return false;
        if (player.isSneaking() && viewer.getLocation().distanceSquared(player.getLocation()) >= 32D * 32D)
            return false;

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
        return DEFAULT_Y_TRANSLATION + (BedrockChecker.isBedrock(player) ? BEDROCK_Y_TRANSLATION_BONUS : 0f);
    }

    /**
     * Removes the combat level display from a player.
     *
     * @param player The player to remove the display from
     */
    public static void removeDisplay(Player player) {
        PlayerDisplay display = playerDisplays.remove(player.getUniqueId());
        if (display != null) {
            display.close();
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
        if (display != null && display.isValid(player)
                && display.isPassenger() == shouldUsePassenger(player)) {
            boolean showHealth = shouldShowHealth(player);
            String text = renderText(player, showHealth);
            if (!text.equals(display.text)) {
                display.display.setText(ChatColorConverter.convert(text));
                display.text = text;
            }
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
            display.close();
        }
        playerDisplays.clear();
    }

    /**
     * Initializes displays for all online players.
     * Called on plugin startup.
     */
    public static void initialize() {
        shutdown();
        // Reconcile missing or invalid labels after joins, respawns and world changes.
        refreshTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) updateDisplay(player);
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
        if (event.useBed() != Event.Result.ALLOW
                && event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) return;
        removeDisplay(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        // Old packet entities belong to the previous world. The refresh creates fresh ones.
        removeDisplay(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        removeDisplay(event.getPlayer());
    }

    private static boolean shouldRender(Player player) {
        if (!player.isOnline() || player.isDead() || player.isSleeping()) return false;
        if (PlayerIdentityLabelRenderer.hasClassLabel(player.getUniqueId())) return true;
        return SkillsConfig.isSkillSystemEnabled()
                && SkillsConfig.isShowCombatLevelDisplay()
                && !SkillsConfig.isWorldExcludedFromSkills(player);
    }
}
