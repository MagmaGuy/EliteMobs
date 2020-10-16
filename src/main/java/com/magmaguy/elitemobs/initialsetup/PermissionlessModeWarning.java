package com.magmaguy.elitemobs.initialsetup;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand;
import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;


public class PermissionlessModeWarning implements Listener {

    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent event) {
        if (DefaultConfig.setupDone) return;
        if (!event.getPlayer().isOp()) return;
        event.getPlayer().sendMessage("----------------------------------------------------");
        event.getPlayer().sendMessage(ChatColorConverter.convert("&7[EliteMobs] First time setup message:"));
        String guildWorldStatus;
        if (AdventurersGuildCommand.defineTeleportLocation() == null)
            guildWorldStatus = "not completed!";
        else
            guildWorldStatus = "completed!";
        event.getPlayer().sendMessage("Your current Adventurer's Guild world setup status is " + guildWorldStatus);
        if (AdventurersGuildConfig.guildWorldLocation == null) {
            TextComponent guildWorldSetup = new TextComponent(ChatColorConverter.convert("&9Click here to learn more about the Adventurer's Guild World and how to set it up!"));
            guildWorldSetup.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/MagmaGuy/EliteMobs/wiki/%5BGuide%5D-Adventurer's-Guild-World"));
            event.getPlayer().spigot().sendMessage(guildWorldSetup);
        }
        event.getPlayer().sendMessage(ChatColorConverter.convert("&2By default, EliteMobs uses the recommended " +
                "settings for allowing players to access commands and features. &cAre you using a permissions plugin for that instead?"));
        TextComponent yes = new TextComponent(ChatColorConverter.convert("&aYes"));
        TextComponent no = new TextComponent(ChatColorConverter.convert("&cNo"));
        yes.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Use a permissions plugin").create()));
        no.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Let EliteMobs use recommended command and feature access").create()));
        yes.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/elitemobs usepermissions"));
        no.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/elitemobs dontusepermissions"));
        TextComponent optionsMessage = new TextComponent();
        optionsMessage.addExtra(yes);
        optionsMessage.addExtra(ChatColorConverter.convert(" &f/ "));
        optionsMessage.addExtra(no);
        event.getPlayer().spigot().sendMessage(optionsMessage);
        event.getPlayer().sendMessage(ChatColorConverter.convert("&7This message will only be sent to OPs"));
        event.getPlayer().sendMessage("----------------------------------------------------");
        event.getPlayer().sendMessage(ChatColor.GREEN + "A video on how to set EliteMobs up is available here: " + ChatColor.DARK_BLUE + "https://youtu.be/2u71JCyGj-E");
        event.getPlayer().sendMessage(ChatColor.RED + "Need help?  https://discord.gg/9f5QSka");

    }

}
