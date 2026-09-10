package com.magmaguy.elitemobs.experimentalcombat;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ExperimentalCombatConfig;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.SpigotMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.nio.file.Path;

/** Reminds administrators about the opt-in test until that administrator dismisses it. */
public final class ExperimentalCombatSuggestion implements Listener {
    private static final long REMINDER_DELAY_TICKS = 100L;
    private static ExperimentalCombatSuggestion instance;

    private final ExperimentalCombatSuggestionState state;

    public ExperimentalCombatSuggestion() {
        ExperimentalCombatSuggestionState loaded = null;
        Path stateFile = MetadataHandler.PLUGIN.getDataFolder().toPath()
                .resolve(".state")
                .resolve("experimental-combat-suggestion.yml");
        try {
            loaded = new ExperimentalCombatSuggestionState(stateFile);
        } catch (IOException exception) {
            Logger.warn("Could not read [Alpha] Advanced Combat System suggestion dismissals: "
                    + exception.getMessage());
        }
        state = loaded;
        instance = this;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!shouldSuggest(player)) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !shouldSuggest(player)) return;
                sendSuggestion(player);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, REMINDER_DELAY_TICKS);
    }

    public static boolean dismiss(Player player) {
        if (instance == null || instance.state == null) return false;
        try {
            instance.state.dismiss(player.getUniqueId());
            return true;
        } catch (IOException exception) {
            Logger.warn("Could not save the [Alpha] Advanced Combat System suggestion dismissal: "
                    + exception.getMessage());
            return false;
        }
    }

    private boolean shouldSuggest(Player player) {
        return !ExperimentalCombatConfig.isEnabled()
                && player.hasPermission("elitemobs.experimentalcombat.admin")
                && (state == null || !state.isDismissed(player.getUniqueId()));
    }

    private static void sendSuggestion(Player player) {
        Logger.sendSimpleMessage(player, "<g:#9B59FF:#FFB347>[Alpha] Advanced Combat System needs testers</g>");
        Logger.sendSimpleMessage(player,
                "&7Enable it in &fExperimental Combat.yml &7to test classes and the new dungeon combat model.");
        Logger.sendSimpleMessage(player,
                "&cTest the unchanged defaults and send feedback. Community feedback will decide whether this feature evolves or is removed.");
        player.spigot().sendMessage(
                SpigotMessage.hoverLinkMessage(
                        "<g:#6EE7B7:#22C55E>Open the testing community</g>",
                        "&7Open the EliteMobs Discord",
                        DiscordLinks.mainLink),
                SpigotMessage.simpleMessage(" &8• "),
                SpigotMessage.commandHoverMessage(
                        "<g:#EF4444:#F97316>Dismiss this reminder</g>",
                        "&7Stop showing this tester reminder to you",
                        "/em experimentalcombat dismiss"));
    }
}
