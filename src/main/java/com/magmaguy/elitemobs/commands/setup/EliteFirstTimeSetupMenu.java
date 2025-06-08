package com.magmaguy.elitemobs.commands.setup;

import com.magmaguy.elitemobs.commands.ReloadCommand;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.EventsConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.magmacore.menus.FirstTimeSetupMenu;
import com.magmaguy.magmacore.menus.MenuButton;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class EliteFirstTimeSetupMenu {

    public static void createMenu(Player player) {
        new FirstTimeSetupMenu(
                player,
                "&2EliteMobs",
                "&6Dungeons, events, arenas & more!",
                createInfoItem(),
                List.of(createFullInstallItem(), createVirtuallyDisabledItem(), createMinimumInstallItem()));
    }

    private static MenuButton createInfoItem() {
        return new MenuButton(ItemStackGenerator.generateSkullItemStack(
                "magmaguy",
                "&2Welcome to EliteMobs!",
                List.of(
                        "&9Click to get a link to the full setup guide!",
                        "&2You can find server presets below for commonly used",
                        "&2settings. &cWe highly recommend you use the",
                        "&arecommended full install!"))) {
            @Override
            public void onClick(Player player) {
                player.closeInventory();
                Logger.sendSimpleMessage(player, "&8&m-----------------------------------------------------");
                Logger.sendSimpleMessage(player, "&2See the full setup here: &9&nhttps://nightbreak.io/plugin/elitemobs/#setup");
                Logger.sendSimpleMessage(player, "&2Check the available content through &6/em setup &2!");
                Logger.sendSimpleMessage(player, "&2Support & discussion Discord: &9&nhttps://discord.gg/eSxvPbWYy4");
                Logger.sendSimpleMessage(player, "&8&m-----------------------------------------------------");
            }
        };
    }

    private static MenuButton createFullInstallItem() {
        return new MenuButton(ItemStackGenerator.generateItemStack(
                Material.GREEN_STAINED_GLASS_PANE,
                "&2Full install &a- Recommended!",
                List.of("&aUse all features from EliteMobs - Events, ",
                        "&aCustom Items and the recommended built-in",
                        "&aMMORPG progression system!",
                        "&6Note that these systems are built to be highly",
                        "&6compatible with survival-like servers, even those",
                        "&6running a lot of other plugins that do similar things!",
                        "&9Click to select this option!"))) {
            @Override
            public void onClick(Player player) {
                player.closeInventory();
                DefaultConfig.toggleSetupDone(true);
                EventsConfig.setEventsEnabled(true);
                MobCombatSettingsConfig.toggleNaturalMobSpawning(true);
                Logger.sendSimpleMessage(player, "&8&m-----------------------------------------------------");
                Logger.sendSimpleMessage(player, "&2You are now using the &arecommended installation&2!");
                Logger.sendSimpleMessage(player, "&6Want to download premade dungeons, arenas, events & more?");
                Logger.sendSimpleMessage(player, "&2See what you can install and how here: &9&nhttps://nightbreak.io/plugin/elitemobs/#setup");
                Logger.sendSimpleMessage(player, "&aCheck the status of installed content through &3/em setup &a!");
                Logger.sendSimpleMessage(player, "&2Support & discussion Discord: &9&nhttps://discord.gg/eSxvPbWYy4");
                Logger.sendSimpleMessage(player, "&8&m-----------------------------------------------------");
                ReloadCommand.reload(player);
            }
        };
    }

    private static MenuButton createMinimumInstallItem() {
        return new MenuButton(ItemStackGenerator.generateItemStack(
                Material.GREEN_STAINED_GLASS_PANE,
                "&cMinimum install &c- Not recommended!",
                List.of("&cDisables default events and custom items, but does not",
                        "&cdisable EliteMob's self-contained MMORPG system!",
                        "&eThis option is sometimes used by those who want the &cEliteMobs",
                        "&efeatures required to run &cBetterStructures and nothing else.",
                        "&9Click to select this option!"))) {
            @Override
            public void onClick(Player player) {
                player.closeInventory();
                DefaultConfig.toggleSetupDone(true);
                EventsConfig.setEventsEnabled(false);
                MobCombatSettingsConfig.toggleNaturalMobSpawning(true);
                Logger.sendSimpleMessage(player, "&8&m-----------------------------------------------------");
                Logger.sendSimpleMessage(player, "&2You are now using the &cnot recommended minimum installation&2!");
                Logger.sendSimpleMessage(player, "&6Want to download premade dungeons, arenas, events & more?");
                Logger.sendSimpleMessage(player, "&2See what you can install and how here: &9&nhttps://nightbreak.io/plugin/elitemobs/#setup");
                Logger.sendSimpleMessage(player, "&aCheck the status of installed content through &3/em setup &a!");
                Logger.sendSimpleMessage(player, "&2Support & discussion Discord: &9&nhttps://discord.gg/eSxvPbWYy4");
                Logger.sendSimpleMessage(player, "&8&m-----------------------------------------------------");
                ReloadCommand.reload(player);
            }
        };
    }

    private static MenuButton createVirtuallyDisabledItem() {
        return new MenuButton(ItemStackGenerator.generateItemStack(
                Material.GREEN_STAINED_GLASS_PANE,
                "&cVirtually disabled install &c- Not recommended!",
                List.of("&cDisables default events, custom items and",
                        "&cnaturally spawning elites, but does not",
                        "&cdisable EliteMob's self-contained MMORPG system!",
                        "&eThis option should only be used by those who",
                        "&eknow exactly what they are doing, as it disables all defaults!",
                        "&9Click to select this option!"))) {
            @Override
            public void onClick(Player player) {
                player.closeInventory();
                DefaultConfig.toggleSetupDone(true);
                EventsConfig.setEventsEnabled(false);
                MobCombatSettingsConfig.toggleNaturalMobSpawning(false);
                Logger.sendSimpleMessage(player, "&8&m-----------------------------------------------------");
                Logger.sendSimpleMessage(player, "&2You are now using the &cvirtually disabled installation&2!");
                Logger.sendSimpleMessage(player, "&6Unless you add something, nothing related to EliteMobs will spawn!");
                Logger.sendSimpleMessage(player, "&6Want to download premade dungeons, arenas, events & more?");
                Logger.sendSimpleMessage(player, "&2See what you can install and how here: &9&nhttps://nightbreak.io/plugin/elitemobs/#setup");
                Logger.sendSimpleMessage(player, "&aCheck the status of installed content through &3/em setup &a!");
                Logger.sendSimpleMessage(player, "&2Support & discussion Discord: &9&nhttps://discord.gg/eSxvPbWYy4");
                Logger.sendSimpleMessage(player, "&8&m-----------------------------------------------------");
                ReloadCommand.reload(player);
            }
        };
    }
}
