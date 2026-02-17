package com.magmaguy.elitemobs.commands.setup;

import com.magmaguy.elitemobs.commands.ReloadCommand;
import com.magmaguy.elitemobs.config.*;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import com.magmaguy.magmacore.menus.FirstTimeSetupMenu;
import com.magmaguy.magmacore.menus.MenuButton;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.SpigotMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class EliteFirstTimeSetupMenu {

    public static void createMenu(Player player) {
        new FirstTimeSetupMenu(
                player,
                InitializeConfig.getMenuTitle(),
                InitializeConfig.getMenuSubtitle(),
                createInfoItem(),
                List.of(createRecommendedItem(), createNothingItem(), createContentOnlyItem()));
    }

    private static MenuButton createInfoItem() {
        return new MenuButton(ItemStackGenerator.generateSkullItemStack(
                "magmaguy",
                InitializeConfig.getInfoButtonName(),
                List.of(
                        InitializeConfig.getInfoButtonLore1(),
                        InitializeConfig.getInfoButtonLore2(),
                        InitializeConfig.getInfoButtonLore3()))) {
            @Override
            public void onClick(Player player) {
                player.closeInventory();
                Logger.sendSimpleMessage(player, InitializeConfig.getSeparatorLine());
                sendLink(player, InitializeConfig.getInfoButtonClickSetupLink(),
                        InitializeConfig.getContentLinkDisplay(), InitializeConfig.getContentLinkHover(),
                        "https://nightbreak.io/plugin/elitemobs/#setup");
                // "Browse content: /em setup or nightbreak.io/elitemobs"
                player.spigot().sendMessage(
                        SpigotMessage.simpleMessage(InitializeConfig.getInfoButtonClickContentBrowse()),
                        SpigotMessage.commandHoverMessage(
                                InitializeConfig.getEmSetupDisplay(),
                                InitializeConfig.getEmSetupHover(),
                                "/em setup"),
                        SpigotMessage.simpleMessage(InitializeConfig.getInfoButtonClickContentBrowseSuffix()),
                        SpigotMessage.hoverLinkMessage(
                                InitializeConfig.getContentLinkDisplay(),
                                InitializeConfig.getContentLinkHover(),
                                "https://nightbreak.io/plugin/elitemobs/"));
                sendLink(player, InitializeConfig.getInfoButtonClickDiscord(),
                        InitializeConfig.getDiscordLinkDisplay(), InitializeConfig.getDiscordLinkHover(),
                        DiscordLinks.mainLink);
                Logger.sendSimpleMessage(player, InitializeConfig.getSeparatorLine());
            }
        };
    }

    private static MenuButton createRecommendedItem() {
        return new MenuButton(ItemStackGenerator.generateItemStack(
                Material.GREEN_STAINED_GLASS_PANE,
                InitializeConfig.getRecommendedPresetName(),
                List.of(InitializeConfig.getRecommendedPresetLore1(),
                        InitializeConfig.getRecommendedPresetLore2(),
                        InitializeConfig.getRecommendedPresetLore3(),
                        InitializeConfig.getRecommendedPresetLore4(),
                        InitializeConfig.getRecommendedPresetLore5()))) {
            @Override
            public void onClick(Player player) {
                player.closeInventory();
                DefaultConfig.toggleSetupDone(true);
                MobCombatSettingsConfig.toggleNaturalMobSpawning(true);
                sendPostClickMessages(player,
                        InitializeConfig.getRecommendedPresetClickConfirm(),
                        InitializeConfig.getRecommendedPresetClickNightbreakLogin(),
                        InitializeConfig.getRecommendedPresetClickDownloadAll(),
                        InitializeConfig.getRecommendedPresetClickDownloadAllSuffix(),
                        InitializeConfig.getRecommendedPresetClickSetupGuide(),
                        InitializeConfig.getRecommendedPresetClickDiscord(),
                        InitializeConfig.getRecommendedPresetClickWiki());
                ReloadCommand.reload(player);
            }
        };
    }

    private static MenuButton createContentOnlyItem() {
        return new MenuButton(ItemStackGenerator.generateItemStack(
                Material.YELLOW_STAINED_GLASS_PANE,
                InitializeConfig.getContentOnlyPresetName(),
                List.of(InitializeConfig.getContentOnlyPresetLore1(),
                        InitializeConfig.getContentOnlyPresetLore2(),
                        InitializeConfig.getContentOnlyPresetLore3(),
                        InitializeConfig.getContentOnlyPresetLore4(),
                        InitializeConfig.getContentOnlyPresetLore5()))) {
            @Override
            public void onClick(Player player) {
                player.closeInventory();
                DefaultConfig.toggleSetupDone(true);
                MobCombatSettingsConfig.toggleNaturalMobSpawning(false);
                EventsConfig.setEventsEnabled(false);
                sendPostClickMessages(player,
                        InitializeConfig.getContentOnlyPresetClickConfirm(),
                        InitializeConfig.getContentOnlyPresetClickNightbreakLogin(),
                        InitializeConfig.getContentOnlyPresetClickDownloadAll(),
                        InitializeConfig.getContentOnlyPresetClickDownloadAllSuffix(),
                        InitializeConfig.getContentOnlyPresetClickSetupGuide(),
                        InitializeConfig.getContentOnlyPresetClickDiscord(),
                        InitializeConfig.getContentOnlyPresetClickWiki());
                ReloadCommand.reload(player);
            }
        };
    }

    private static MenuButton createNothingItem() {
        List<String> lore = new ArrayList<>(List.of(
                InitializeConfig.getNothingPresetLore1(),
                InitializeConfig.getNothingPresetLore2(),
                InitializeConfig.getNothingPresetLore3(),
                InitializeConfig.getNothingPresetLore4(),
                InitializeConfig.getNothingPresetLore5()));
        String lore6 = InitializeConfig.getNothingPresetLore6();
        if (lore6 != null && !lore6.isEmpty()) lore.add(lore6);

        return new MenuButton(ItemStackGenerator.generateItemStack(
                Material.RED_STAINED_GLASS_PANE,
                InitializeConfig.getNothingPresetName(),
                lore)) {
            @Override
            public void onClick(Player player) {
                player.closeInventory();
                DefaultConfig.toggleSetupDone(true);
                MobCombatSettingsConfig.toggleNaturalMobSpawning(false);
                SkillsConfig.toggleSkillSystem(false);
                ItemSettingsConfig.toggleEliteMobsLoot(false);
                Logger.sendSimpleMessage(player, InitializeConfig.getSeparatorLine());
                sendText(player, InitializeConfig.getNothingPresetClickConfirm());
                sendText(player, InitializeConfig.getNothingPresetClickWarning());
                sendText(player, InitializeConfig.getNothingPresetClickBetterStructures());
                sendLink(player, InitializeConfig.getNothingPresetClickSetupGuide(),
                        InitializeConfig.getDiscordLinkDisplay(), InitializeConfig.getDiscordLinkHover(),
                        DiscordLinks.mainLink);
                Logger.sendSimpleMessage(player, InitializeConfig.getSeparatorLine());
                ReloadCommand.reload(player);
            }
        };
    }

    private static void sendPostClickMessages(Player player, String confirm, String nightbreakLoginPrefix,
                                               String downloadAllPrefix, String downloadAllSuffix,
                                               String browseContentPrefix,
                                               String manualInstallPrefix, String discordPrefix) {
        Logger.sendSimpleMessage(player, InitializeConfig.getSeparatorLine());
        sendText(player, confirm);
        sendLink(player, nightbreakLoginPrefix,
                InitializeConfig.getAccountLinkDisplay(), InitializeConfig.getAccountLinkHover(),
                "https://nightbreak.io/account/");
        // "Step 2: Install & manage content in-game with /em setup!"
        sendCommand(player, downloadAllPrefix, downloadAllSuffix,
                InitializeConfig.getEmSetupDisplay(),
                InitializeConfig.getEmSetupHover(),
                "/em setup");
        sendLink(player, browseContentPrefix,
                InitializeConfig.getContentLinkDisplay(), InitializeConfig.getContentLinkHover(),
                "https://nightbreak.io/plugin/elitemobs/");
        sendLink(player, manualInstallPrefix,
                InitializeConfig.getWikiLinkDisplay(), InitializeConfig.getWikiLinkHover(),
                "https://wiki.nightbreak.io/EliteMobs/setup/");
        sendLink(player, discordPrefix,
                InitializeConfig.getDiscordLinkDisplay(), InitializeConfig.getDiscordLinkHover(),
                DiscordLinks.mainLink);
        Logger.sendSimpleMessage(player, InitializeConfig.getSeparatorLine());
    }

    private static void sendText(Player player, String msg) {
        if (msg != null && !msg.isEmpty())
            Logger.sendSimpleMessage(player, msg);
    }

    private static void sendLink(Player player, String prefix, String display, String hover, String url) {
        if (prefix == null || prefix.isEmpty()) return;
        player.spigot().sendMessage(
                SpigotMessage.simpleMessage(prefix),
                SpigotMessage.hoverLinkMessage(display, hover, url));
    }

    private static void sendCommand(Player player, String prefix, String suffix,
                                     String display, String hover, String command) {
        if (prefix == null || prefix.isEmpty()) return;
        player.spigot().sendMessage(
                SpigotMessage.simpleMessage(prefix),
                SpigotMessage.commandHoverMessage(display, hover, command),
                SpigotMessage.simpleMessage(suffix));
    }
}
