package com.magmaguy.elitemobs.utils;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;

public class BookMaker {
    public static ItemStack generateBook(Player player, List<String> pages) {
        ItemStack writtenBook = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) writtenBook.getItemMeta();
        bookMeta.setTitle("EliteMobs Book");
        bookMeta.setAuthor("MagmaGuy");
        bookMeta.setPages(pages);
        writtenBook.setItemMeta(bookMeta);
        player.openBook(writtenBook);
        return writtenBook;
    }

    public static ItemStack generateBook(Player player, TextComponent[] pages) {
        ItemStack writtenBook = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) writtenBook.getItemMeta();
        bookMeta.setTitle("EliteMobs Book");
        bookMeta.setAuthor("MagmaGuy");

        /*
        for some reason the spigot api isn't respecting the whole 1 page per array element, so this converts it to a
        format that actually works
         */
        for (TextComponent textComponent : pages) {
            TextComponent[] stupid = new TextComponent[1];
            stupid[0] = textComponent;
            bookMeta.spigot().addPage(stupid);
        }
        writtenBook.setItemMeta(bookMeta);
        player.openBook(writtenBook);
        return writtenBook;
    }
}
