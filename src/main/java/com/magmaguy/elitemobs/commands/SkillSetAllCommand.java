package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.arguments.IntegerCommandArgument;
import com.magmaguy.magmacore.command.arguments.PlayerCommandArgument;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class SkillSetAllCommand extends AdvancedCommand {
    public SkillSetAllCommand() {
        super(List.of("skill"));
        addLiteral("setAll");
        addArgument("player", new PlayerCommandArgument());
        addArgument("level", new IntegerCommandArgument("<level>"));
        setUsage("/em skill setAll <player> <level>");
        setPermission("elitemobs.skill.admin");
        setDescription("Sets all of a player's skills to the specified level.");
    }

    @Override
    public void execute(CommandData commandData) {
        String playerName = commandData.getStringArgument("player");
        int level = commandData.getIntegerArgument("level");

        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer == null) {
            Logger.sendMessage(commandData.getCommandSender(), "&cPlayer not found: " + playerName);
            return;
        }

        if (level < 1) {
            Logger.sendMessage(commandData.getCommandSender(), "&cLevel must be at least 1!");
            return;
        }

        if (level > 100) {
            Logger.sendMessage(commandData.getCommandSender(), "&eWarning: Level " + level + " is above the soft cap of 100.");
        }

        // Calculate the XP needed for the target level
        long targetXP = SkillXPCalculator.totalXPForLevel(level);

        // Set all skills
        for (SkillType skillType : SkillType.values()) {
            PlayerData.setSkillXP(targetPlayer.getUniqueId(), skillType, targetXP);
        }

        Logger.sendMessage(commandData.getCommandSender(),
                "&aSet all of " + targetPlayer.getName() + "'s skills to level " + level);
    }
}
