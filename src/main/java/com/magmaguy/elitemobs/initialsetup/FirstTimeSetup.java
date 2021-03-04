package com.magmaguy.elitemobs.initialsetup;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class FirstTimeSetup implements Listener {
    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent event) {
        if (DefaultConfig.setupDone) return;
        if (!event.getPlayer().hasPermission("elitemobs.*")) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!event.getPlayer().isOnline()) return;
                event.getPlayer().sendMessage("----------------------------------------------------");
                event.getPlayer().sendMessage(ChatColorConverter.convert("&7[EliteMobs] &fFirst time setup message:"));
                event.getPlayer().sendMessage(ChatColorConverter.convert("&7Welcome to EliteMobs!" +
                        " &c&lIt looks like you haven't set up EliteMobs yet! &2To install EliteMobs, do &a/em setup &2!"));
                event.getPlayer().sendMessage(ChatColorConverter.convert("&7Need command help? &6&l/em help"));
                event.getPlayer().sendMessage(ChatColorConverter.convert("&7You can get support over at &9&n" + DiscordLinks.mainLink));
                TextComponent setupMessage = new TextComponent(ChatColorConverter.convert("&cDon't want to see this message again? "));
                TextComponent setupDone = new TextComponent(ChatColorConverter.convert("&c&nClick here!"));
                setupDone.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Never show this again!").create()));
                setupDone.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/elitemobs setup done"));
                setupMessage.addExtra(setupDone);
                event.getPlayer().spigot().sendMessage(setupMessage);
                event.getPlayer().sendMessage(ChatColorConverter.convert("&8&oThis message will only be sent admins!"));

                event.getPlayer().sendMessage("----------------------------------------------------");
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20 * 10);
    }
}
