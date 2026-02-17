package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.ArmorSkillHealthBonus;
import com.magmaguy.elitemobs.skills.CombatLevelDisplay;
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
            Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getSkillPlayerNotFoundMessage().replace("$player", playerName));
            return;
        }

        if (level < 1) {
            Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getSkillLevelMinMessage());
            return;
        }

        if (level > 100) {
            Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getSkillLevelWarningMessage().replace("$level", String.valueOf(level)));
        }

        // Calculate the XP needed for the target level
        long targetXP = SkillXPCalculator.totalXPForLevel(level);

        // Set all skills
        for (SkillType skillType : SkillType.values()) {
            PlayerData.setSkillXP(targetPlayer.getUniqueId(), skillType, targetXP);
        }

        // Update combat level display
        CombatLevelDisplay.updateDisplay(targetPlayer);

        // Update armor health bonus (since armor skill was changed)
        ArmorSkillHealthBonus.updateHealthBonus(targetPlayer);

        Logger.sendMessage(commandData.getCommandSender(),
                CommandMessagesConfig.getSkillSetAllSuccessMessage()
                        .replace("$player", targetPlayer.getName())
                        .replace("$level", String.valueOf(level))
                        .replace("$xp", String.valueOf(targetXP)));
    }
}
