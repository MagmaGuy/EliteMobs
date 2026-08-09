package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.config.translations.TranslationsConfig;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * EliteMobs' translatable extension of MagmaCore's configuration engine.
 * <p>
 * <b>Registering a translation must never mutate the configuration</b>
 * Between 10.0.1 and 10.7.x {@link #setString} and {@link #setList} ran {@code fileConfiguration.set(key, null)} on
 * non-English servers, so translated text would only be served from {@code translations/<language>.csv}. The identical
 * strip in {@code CustomConfigFields.translatable} caused real, permanent data loss once MagmaCore's
 * {@code CustomConfig#initialize(File)} started calling {@code ConfigurationEngine#fileSaverCustomValues} after parsing
 * (MagmaCore 2db8d8e, 2026-06-21): the in-memory deletion became a disk write, and the first boot on a translated
 * server erased text out of every downloaded content package.
 * <p>
 * The strip here was assumed to be survivable because these callers pass a Java-side {@code defaultValue} that is
 * recreated on every boot. That was true of 914 of the 916 translatable call sites and false of the two that matter:
 * <ul>
 *     <li>{@code CustomBossesConfigFields#processConfigFields} registers {@code onKillMessage} with a {@code null}
 *     default, and every content-package boss reaches it through {@code CustomConfig#initialize(File)} - the save path
 *     that writes back to the user's YAML. There is no Java-side source for that text, so the strip deleted it
 *     permanently; the next boot read {@code null} and returned {@code null}.</li>
 *     <li>{@link #setItemStack} registers {@code <key>.lore} with a {@code null} default because its own
 *     {@code addDefault} calls used to be English-gated, leaving the lore of every menu button with no fallback
 *     outside the CSV.</li>
 * </ul>
 * Both are fixed by not mutating: registration is now identical on English and translated servers, and the source text
 * stays in the YAML exactly as it does in English. Keeping the key cannot clobber a translation - reconciliation in
 * {@code TranslationsConfigFields} only ever writes the {@code en} column, never the language column - and the value
 * these methods return is still the translated one.
 * <p>
 * <strong>Do not reintroduce the strip.</strong> Anything that removes a key here is one MagmaCore save-path change
 * away from deleting user content again.
 */
public class ConfigurationEngine extends com.magmaguy.magmacore.config.ConfigurationEngine {

    public static String setString(File file, FileConfiguration fileConfiguration, String key, String defaultValue, boolean translatable) {
        fileConfiguration.addDefault(key, defaultValue);
        if (translatable)
            return TranslationsConfig.add(file.getName(), key, fileConfiguration.getString(key));
        else
            return ChatColorConverter.convert(fileConfiguration.getString(key));
    }

    public static String setString(List<String> comments, File file, FileConfiguration fileConfiguration, String key, String defaultValue, boolean translatable) {
        String value = setString(file, fileConfiguration, key, defaultValue, translatable);
        //Comments used to be skipped on translated servers because the key they annotate had just been deleted.
        setComments(fileConfiguration, key, comments);
        return value;
    }

    @SuppressWarnings("unchecked")
    public static List setList(File file, FileConfiguration fileConfiguration, String key, List defaultValue, boolean translatable) {
        fileConfiguration.addDefault(key, defaultValue);
        if (translatable)
            return TranslationsConfig.add(file.getName(), key, (List<String>) fileConfiguration.getList(key));
        else
            return fileConfiguration.getList(key);
    }

    public static List setList(List<String> comment, File file, FileConfiguration fileConfiguration, String key, List defaultValue, boolean translatable) {
        List value = setList(file, fileConfiguration, key, defaultValue, translatable);
        //Comments used to be skipped on translated servers because the key they annotate had just been deleted.
        setComments(fileConfiguration, key, comment);
        return value;
    }

    public static ItemStack setItemStack(File file, FileConfiguration fileConfiguration, String key, ItemStack itemStack, boolean translatable) {
        fileConfiguration.addDefault(key + ".material", itemStack.getType().toString());
        //The name and lore defaults used to be registered only in English, which is what left setList below with a
        //null default and the lore with no source outside the CSV. setString / setList register them either way.
        if (itemStack.getType().equals(Material.PLAYER_HEAD))
            fileConfiguration.addDefault(key + ".owner", ((SkullMeta) itemStack.getItemMeta()).getOwner());
        Material material;
        try {
            material = Material.valueOf(fileConfiguration.getString(key + ".material"));
        } catch (Exception ex) {
            Logger.warn("Material type " + fileConfiguration.getString(key + ".material") + " is not valid! Correct it to make a valid item.");
            return null;
        }
        String name = "";
        try {
            name = setString(file, fileConfiguration, key + ".name", itemStack.getItemMeta().getDisplayName(), true);
        } catch (Exception ex) {
            Logger.warn("Item name " + fileConfiguration.getString(key + ".name") + " is not valid! Correct it to make a valid item.");
        }
        List<String> lore = new ArrayList<>();
        try {
            lore = setList(file, fileConfiguration, key + ".lore",
                    itemStack.hasItemMeta() && itemStack.getItemMeta().hasLore() ? itemStack.getItemMeta().getLore() : null,
                    true);
        } catch (Exception ex) {
            Logger.warn("Item lore " + fileConfiguration.getString(key + ".lore") + " is not valid! Correct it to make a valid item.");
        }
        ItemStack fileItemStack = ItemStackGenerator.generateItemStack(material, name, lore);
        if (material == Material.PLAYER_HEAD) {
            String owner = fileConfiguration.getString(key + ".owner");
            if (owner != null && !owner.isBlank()) {
                SkullMeta skullMeta = (SkullMeta) fileItemStack.getItemMeta();
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(owner));
                fileItemStack.setItemMeta(skullMeta);
            }
        }
        return fileItemStack;
    }

}
