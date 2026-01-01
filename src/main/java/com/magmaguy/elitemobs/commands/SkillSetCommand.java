package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.arguments.IntegerCommandArgument;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.magmacore.command.arguments.PlayerCommandArgument;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class SkillSetCommand extends AdvancedCommand {
    public SkillSetCommand() {
        super(List.of("skill"));
        addLiteral("set");
        addArgument("player", new PlayerCommandArgument());
        addArgument("skillType", new ListStringCommandArgument(
                Arrays.stream(SkillType.values()).map(Enum::name).toList(),
                "<skillType>"));
        addArgument("level", new IntegerCommandArgument("<level>"));
        setUsage("/em skill set <player> <skillType> <level>");
        setPermission("elitemobs.skill.admin");
        setDescription("Sets a player's skill level for a specific skill type.");
    }

    @Override
    public void execute(CommandData commandData) {
        String playerName = commandData.getStringArgument("player");
        String skillTypeName = commandData.getStringArgument("skillType");
        int level = commandData.getIntegerArgument("level");

        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer == null) {
            Logger.sendMessage(commandData.getCommandSender(), "&cPlayer not found: " + playerName);
            return;
        }

        SkillType skillType;
        try {
            skillType = SkillType.valueOf(skillTypeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            Logger.sendMessage(commandData.getCommandSender(), "&cInvalid skill type: " + skillTypeName);
            Logger.sendMessage(commandData.getCommandSender(), "&7Valid types: " +
                    String.join(", ", Arrays.stream(SkillType.values()).map(Enum::name).toList()));
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

        // Set the player's skill XP
        PlayerData.setSkillXP(targetPlayer.getUniqueId(), skillType, targetXP);

        Logger.sendMessage(commandData.getCommandSender(),
                "&aSet " + targetPlayer.getName() + "'s " + skillType.getDisplayName() +
                        " skill to level " + level + " (" + targetXP + " XP)");
    }
}
