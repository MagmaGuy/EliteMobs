package com.magmaguy.elitemobs.utils;

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
}
