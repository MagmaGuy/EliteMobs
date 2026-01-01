package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.arguments.PlayerCommandArgument;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class SkillCheckCommand extends AdvancedCommand {
    public SkillCheckCommand() {
        super(List.of("skill"));
        addLiteral("check");
        addArgument("player", new PlayerCommandArgument());
        setUsage("/em skill check <player>");
        setPermission("elitemobs.skill.check");
        setDescription("Displays a player's skill levels.");
    }

    @Override
    public void execute(CommandData commandData) {
        String playerName = commandData.getStringArgument("player");

        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer == null) {
            Logger.sendMessage(commandData.getCommandSender(), "&cPlayer not found: " + playerName);
            return;
        }

        Logger.sendMessage(commandData.getCommandSender(), "&6=== " + targetPlayer.getName() + "'s Skills ===");

        for (SkillType skillType : SkillType.values()) {
            long xp = PlayerData.getSkillXP(targetPlayer.getUniqueId(), skillType);
            int level = SkillXPCalculator.levelFromTotalXP(xp);
            long xpProgress = SkillXPCalculator.xpProgressInCurrentLevel(xp);
            long xpNeeded = SkillXPCalculator.xpToNextLevel(level);
            double progress = SkillXPCalculator.levelProgress(xp) * 100;

            Logger.sendMessage(commandData.getCommandSender(),
                    String.format("&7%s: &aLevel %d &7(%s/%s XP, %.1f%%)",
                            skillType.getDisplayName(),
                            level,
                            formatNumber(xpProgress),
                            formatNumber(xpNeeded),
                            progress));
        }
    }

    private String formatNumber(long number) {
        if (number >= 1_000_000) {
            return String.format("%.1fM", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format("%.1fK", number / 1_000.0);
        }
        return String.valueOf(number);
    }
}
