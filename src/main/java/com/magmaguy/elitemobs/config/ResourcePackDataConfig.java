package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ResourcePackDataConfig {

    @Getter
    public static boolean eliteMobsResourcePackEnabled;
    @Getter
    public static boolean displayCustomMenuUnicodes;
    @Getter
    public static boolean forceResourcePack;
    @Getter
    public static String resourcePackPrompt;
    @Getter
    public static String resourcePackLink;
    @Getter
    public static byte[] SHA1 = null;
    private static File file;
    private static FileConfiguration fileConfiguration;

    private ResourcePackDataConfig() {
    }

    public static void initializeConfig() {
        file = ConfigurationEngine.fileCreator("resource_pack_config.yml");
        fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
        eliteMobsResourcePackEnabled = ConfigurationEngine.setBoolean(fileConfiguration, "eliteMobsResourcePackEnabled", false);
        forceResourcePack = ConfigurationEngine.setBoolean(fileConfiguration, "forceResourcePack", false);
        resourcePackPrompt = ConfigurationEngine.setString(file, fileConfiguration, "resourcePackPrompt", "[EliteMobs] The use of the resource pack is highly recommended!", true);
        displayCustomMenuUnicodes = eliteMobsResourcePackEnabled;
        resourcePackLink = ConfigurationEngine.setString(file, fileConfiguration, "resourcePackLink", "https://www.magmaguy.com/downloads/elitemobs_resource_pack.zip", false);
        updateSHA1();
        if (DefaultConfig.isMenuUnicodeFormatting()) displayCustomMenuUnicodes = true;
        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }

    public static void updateSHA1() {
        if (!eliteMobsResourcePackEnabled || resourcePackLink == null) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                File tempFile = new File("elitemobs_resource_pack.zip");
                try {
                    FileUtils.copyURLToFile(new URL(resourcePackLink), tempFile);
                } catch (IOException e) {
                    new WarningMessage("[EliteMobs] Failed to get resource pack from  https://www.magmaguy.com/downloads/elitemobs_resource_pack.zip ! This might mean the server is down, in which case you will have to host the resource pack on your own! (2)");
                    return;
                }
                try {
                    SHA1 = ConfigurationExporter.sha1CodeByteArray(tempFile);
                } catch (Exception e) {
                    new WarningMessage("[EliteMobs] Failed to generate your SHA1 key! You will have to do this manually, though this might mean there is a serious problem with the resource pack.");
                }
            }
        }.runTaskAsynchronously(MetadataHandler.PLUGIN);
    }

    public static void toggleEliteMobsResourcePackStatus(boolean status) {
        fileConfiguration.set("eliteMobsResourcePackEnabled", status);
        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
        eliteMobsResourcePackEnabled = status;
        displayCustomMenuUnicodes = eliteMobsResourcePackEnabled;
        if (status) {
            updateSHA1();
            Bukkit.getOnlinePlayers().forEach(ResourcePackDataConfig::sendResourcePack);
        }
    }

    public static void toggleForceResourcePack(boolean status){
        fileConfiguration.set("forceResourcePack", status);
        forceResourcePack = status;
    }

    public static void sendResourcePack(Player player) {
        player.setResourcePack(resourcePackLink, SHA1, resourcePackPrompt, forceResourcePack);
    }

    public static class ResourcePackDataConfigEvents implements Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerLogin(PlayerJoinEvent event) {
            if (!eliteMobsResourcePackEnabled) return;
            if (SHA1 == null) return;
            sendResourcePack(event.getPlayer());
        }
    }
}
