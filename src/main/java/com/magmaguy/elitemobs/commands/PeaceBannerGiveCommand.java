package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.peacebanner.PeaceBannerItem;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.arguments.PlayerCommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class PeaceBannerGiveCommand extends AdvancedCommand {
    public PeaceBannerGiveCommand() {
        super(List.of("peacebanner"));
        addLiteral("give");
        addArgument("playerName", new PlayerCommandArgument());
        setUsage("/em peacebanner give <player>");
        setPermission("elitemobs.peacebanner.admin");
        setDescription("Gives a Peace Banner to the specified player.");
    }

    @Override
    public void execute(CommandData commandData) {
        Player player = Bukkit.getPlayer(commandData.getStringArgument("playerName"));
        if (player == null) {
            commandData.getCommandSender().sendMessage("Player not found.");
            return;
        }
        for (var overflow : player.getInventory().addItem(PeaceBannerItem.createPeaceBanner()).values()) {
            player.getWorld().dropItem(player.getLocation(), overflow);
        }
        commandData.getCommandSender().sendMessage("Gave a Peace Banner to " + player.getName() + ".");
    }
}
