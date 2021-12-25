package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.playerdata.statusscreen.PlayerStatusScreen;
import com.magmaguy.elitemobs.utils.BookMaker;
import com.magmaguy.elitemobs.utils.SpigotMessage;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class DebugScreen {

    public static void open(Player player, String argument) {
        if (Bukkit.getPlayer(argument) != null)
            new PlayerStatusScreen(player, Bukkit.getPlayer(argument));
        else openBossScreen(player, argument);
    }

    private static void openBossScreen(Player player, String argument) {

        TextComponent[] pages = new TextComponent[100];

        int counter = 0;
        for (EliteEntity eliteEntity : EntityTracker.getEliteMobEntities().values())
            if (!(eliteEntity instanceof RegionalBossEntity))
                if (eliteEntity instanceof CustomBossEntity) {
                    TextComponent textComponent = generateEntry((CustomBossEntity) eliteEntity, argument, player);
                    if (textComponent == null) continue;
                    pages[counter] = textComponent;
                    counter++;
                }

        for (RegionalBossEntity regionalBossEntity : RegionalBossEntity.getRegionalBossEntities()) {
            TextComponent textComponent = generateEntry(regionalBossEntity, argument, player);
            if (textComponent == null) continue;
            pages[counter] = textComponent;
            counter++;
        }

        BookMaker.generateBook(player, pages);

    }

    private static TextComponent generateEntry(CustomBossEntity customBossEntity, String argument, Player player) {
        if (!customBossEntity.getCustomBossesConfigFields().getFilename().contains(argument) &&
                !customBossEntity.getCustomBossesConfigFields().getName().toLowerCase().contains(argument.toLowerCase()))
            return null;
        TextComponent page = new TextComponent();
        page.addExtra(customBossEntity.getCustomBossesConfigFields().getFilename() + "\n");
        page.addExtra("Name: " + ChatColorConverter.convert(customBossEntity.getCustomBossesConfigFields().getName()) + ChatColor.BLACK + "\n");
        page.addExtra("Level: " + customBossEntity.getCustomBossesConfigFields().getLevel() + "\n");
        if (customBossEntity.getLivingEntity() != null) {
            page.addExtra("Is Alive (MC): " + !customBossEntity.getLivingEntity().isDead() + "\n");
            page.addExtra(SpigotMessage.commandHoverMessage(ChatColor.BLUE + "XYZ: " + "\n",
                    customBossEntity.getLocation().getBlockX() + ", " +
                            customBossEntity.getLocation().getBlockY() + ", " +
                            customBossEntity.getLocation().getBlockZ() + "\n" +
                            ChatColor.BLUE + "Click to teleport! (if alive)",
                    "/em debugtp " + customBossEntity.getEliteUUID().toString()));
            page.addExtra("Has AI: " + !customBossEntity.getLivingEntity().hasAI() + "\n");
        } else
            page.addExtra("Is Alive (MC): false\n");
        if (customBossEntity.getLocation() != null && player.getWorld().equals(customBossEntity.getLocation().getWorld()))
            page.addExtra(SpigotMessage.hoverMessage(ChatColor.BLUE + "Spawn distance",
                    "Spawn distance: X=" + (int) (player.getLocation().getX() - customBossEntity.getSpawnLocation().getX())
                            + " | Y=" + (int) (player.getLocation().getY() - customBossEntity.getSpawnLocation().getY()) +
                            " | Z=" + (int) (player.getLocation().getZ() - customBossEntity.getSpawnLocation().getZ()) + "\n"));
        page.addExtra("Is Persistent: " + customBossEntity.getCustomBossesConfigFields().isPersistent() + "\n");
        if (customBossEntity instanceof RegionalBossEntity) {
            page.addExtra("Is Respawning: " + ((RegionalBossEntity) customBossEntity).isRespawning() + "\n");
        }

        page.addExtra(SpigotMessage.commandHoverMessage(ChatColor.BLUE + "Boss trace!",
                "Remember, it requires debug mode to be on! This is used for advanced debugging, ask on discord if you want to know more about it.",
                "/elitemobs trace " + customBossEntity.getEliteUUID().toString()));
        return page;
    }

}
