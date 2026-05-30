package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.utils.DebugMessage;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Prints a diagnostic snapshot of the player's effective combat stats and the
 * currently-loaded damage-multiplier config values to both chat and the server
 * log. Designed to answer the question "did my MobCombatSettings.yml edit
 * actually take effect?" without requiring a full combat encounter.
 * <p>
 * Output goes to both chat (so the player sees it immediately) and the server
 * log (so the same data can be shared via mclo.gs for remote diagnosis), with
 * any active /em debug session continuing to receive damage breakdowns.
 */
public class DebugInfoCommand extends AdvancedCommand {

    public DebugInfoCommand() {
        super(List.of("debug"));
        addLiteral("info");
        setUsage("/em debug info");
        setPermission("elitemobs.debug");
        setSenderType(SenderType.PLAYER);
        setDescription("Dumps player skill/gear levels and active damage multipliers for diagnosis.");
    }

    @Override
    public void execute(CommandData commandData) {
        Player player = commandData.getPlayerSender();
        StringBuilder log = new StringBuilder();
        log.append("[EM-DebugInfo:").append(player.getName()).append("] ");

        Logger.sendMessage(player, "&6═════ EM DEBUG INFO ═════");

        // Player-side stats: skill levels (these drive playerToElite and eliteToPlayer formulas).
        Logger.sendMessage(player, "&7Player skills:");
        log.append("skills={");
        boolean first = true;
        for (SkillType type : SkillType.values()) {
            int level = PlayerData.getSkillLevel(player.getUniqueId(), type);
            Logger.sendMessage(player, "&7  " + type.name() + ": &f" + level);
            if (!first) log.append(',');
            log.append(type.name()).append('=').append(level);
            first = false;
        }
        log.append('}');

        int combatLevel = PlayerData.getPlayerLevel(player.getUniqueId());
        Logger.sendMessage(player, "&7Combat level: &f" + combatLevel);
        log.append(" combatLevel=").append(combatLevel);

        // Active multipliers — show all 6 so the user can see exactly which paths are scaled.
        // These are the values currently in memory; if the user edited the YAML without
        // reloading they will see the OLD value here (a useful clue on its own).
        Logger.sendMessage(player, "&6── Active damage multipliers (in-memory) ──");
        Logger.sendMessage(player, "&7damageToEliteMobMultiplierV2:        &f"
                + String.format("%.3f", MobCombatSettingsConfig.getDamageToEliteMultiplier()));
        Logger.sendMessage(player, "&7damageToPlayerMultiplierV2:          &f"
                + String.format("%.3f", MobCombatSettingsConfig.getDamageToPlayerMultiplier()));
        Logger.sendMessage(player, "&7normalizedDamageToEliteMultiplier:   &f"
                + String.format("%.3f", MobCombatSettingsConfig.getNormalizedDamageToEliteMultiplier()));
        Logger.sendMessage(player, "&7normalizedDamageToPlayerMultiplier:  &f"
                + String.format("%.3f", MobCombatSettingsConfig.getNormalizedDamageToPlayerMultiplier()));
        Logger.sendMessage(player, "&7scaledDamageToEliteMultiplier:       &f"
                + String.format("%.3f", MobCombatSettingsConfig.getScaledDamageToEliteMultiplier()));
        Logger.sendMessage(player, "&7scaledDamageToPlayerMultiplier:      &f"
                + String.format("%.3f", MobCombatSettingsConfig.getScaledDamageToPlayerMultiplier()));

        log.append(String.format(
                " multipliers={defaultToElite=%.3f,defaultToPlayer=%.3f,normalizedToElite=%.3f,normalizedToPlayer=%.3f,scaledToElite=%.3f,scaledToPlayer=%.3f}",
                MobCombatSettingsConfig.getDamageToEliteMultiplier(),
                MobCombatSettingsConfig.getDamageToPlayerMultiplier(),
                MobCombatSettingsConfig.getNormalizedDamageToEliteMultiplier(),
                MobCombatSettingsConfig.getNormalizedDamageToPlayerMultiplier(),
                MobCombatSettingsConfig.getScaledDamageToEliteMultiplier(),
                MobCombatSettingsConfig.getScaledDamageToPlayerMultiplier()));

        boolean debugEnabled = DebugMessage.isDebugEnabled(player);
        Logger.sendMessage(player, "&7Per-hit debug breakdown: &f"
                + (debugEnabled ? "&aENABLED" : "&cdisabled (run /em debug)"));
        log.append(" debug=").append(debugEnabled);

        Logger.sendMessage(player, "&6═════════════════════════");

        // Mirror the whole snapshot as one structured log line — easy to grep,
        // and crucially survives upload to mclo.gs when the player shares their
        // server log instead of a chat screenshot.
        Logger.warn(log.toString());
    }
}
