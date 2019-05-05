package com.magmaguy.elitemobs.items;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ShareItem {

    public static void showOnChat(Player player) {
        if (player.getInventory().getItemInMainHand().getType().equals(Material.AIR)) return;
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (!itemStack.hasItemMeta()) return;
        if (!itemStack.getItemMeta().hasDisplayName()) return;
        if (!itemStack.getItemMeta().hasLore()) return;

        String stringList = itemStack.getItemMeta().getDisplayName();
        String name = itemStack.getItemMeta().getDisplayName();

        if (itemStack.getItemMeta().hasLore())
            for (String loreString : itemStack.getItemMeta().getLore())
                stringList += "\n" + loreString;

        TextComponent interactiveMessage = new TextComponent(player.getDisplayName() + ": " + name);
        interactiveMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(stringList).create()));

        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers())
            onlinePlayer.spigot().sendMessage(interactiveMessage);
    }

}
