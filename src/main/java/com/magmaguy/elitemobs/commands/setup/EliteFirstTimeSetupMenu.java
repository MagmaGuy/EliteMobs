package com.magmaguy.elitemobs.commands.setup;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.EventsConfig;
import com.magmaguy.magmacore.menus.AdvancedMenuItem;
import com.magmaguy.magmacore.menus.FirstTimeSetupMenu;
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
                List.of(createFullInstallItem(), createMinimumInstallItem()));
    }

    private static AdvancedMenuItem createInfoItem() {
        class archetype extends AdvancedMenuItem {
            public archetype() {
                super(ItemStackGenerator.generateSkullItemStack(
                        "magmaguy",
                        "&2Welcome to EliteMobs!",
                        List.of(
                                "&9Click to get a link to the full setup guide!",
                                "&2You can find server presets below for commonly used",
                                "&2settings. &cWe highly recommend you use the",
                                "&arecommended full install!")));
            }

            @Override
            public void run(Player player) {
                player.closeInventory();
                Logger.sendSimpleMessage(player, "&8&m-----------------------------------------------------");
                Logger.sendSimpleMessage(player, "&2See the full setup here: &9&nhttps://nightbreak.io/plugin/elitemobs/#setup");
                Logger.sendSimpleMessage(player, "&2Check the available content through &6/em setup &2!");
                Logger.sendSimpleMessage(player, "&2Support & discussion Discord: &9&nhttps://discord.gg/eSxvPbWYy4");
                Logger.sendSimpleMessage(player, "&8&m-----------------------------------------------------");
            }
        }
        return new archetype();
    }

    private static AdvancedMenuItem createFullInstallItem() {
        class fullArchetype extends AdvancedMenuItem {
            public fullArchetype() {
                super(ItemStackGenerator.generateItemStack(
                        Material.GREEN_STAINED_GLASS_PANE,
                        "&2Full install &a- Recommended!",
                        List.of("&aUse all features from EliteMobs - Events, ",
                                "&aCustom Items and the recommended built-in",
                                "&aMMORPG progression system!",
                                "&6Note that these systems are built to be highly",
                                "&6compatible with survival-like servers, even those",
                                "&6running a lot of other plugins that do similar things!",
                                "&9Click to select this option!")));
            }

            @Override
            public void run(Player player) {
                player.closeInventory();
                DefaultConfig.toggleSetupDone(true);
                EventsConfig.setEventsEnabled(true);
                Logger.sendSimpleMessage(player, "&8&m-----------------------------------------------------");
                Logger.sendSimpleMessage(player, "&2You are now using the &arecommended installation&2!");
                Logger.sendSimpleMessage(player, "&6Want to download premade dungeons, arenas, events & more?");
                Logger.sendSimpleMessage(player, "&2See what you can install and how here: &9&nhttps://nightbreak.io/plugin/elitemobs/#setup");
                Logger.sendSimpleMessage(player, "&aCheck the status of installed content through &3/em setup &a!");
                Logger.sendSimpleMessage(player, "&2Support & discussion Discord: &9&nhttps://discord.gg/eSxvPbWYy4");
                Logger.sendSimpleMessage(player, "&8&m-----------------------------------------------------");
            }
        }
        return new fullArchetype();
    }

    private static AdvancedMenuItem createMinimumInstallItem() {
        class fullArchetype extends AdvancedMenuItem {
            public fullArchetype() {
                super(ItemStackGenerator.generateItemStack(
                        Material.GREEN_STAINED_GLASS_PANE,
                        "&cMinimum install &c- Not recommended!",
                        List.of("&cDisables default events and custom items, but does not",
                                "&cdisable EliteMob's self-contained MMORPG system!",
                                "&eThis option is sometimes used by those who want the &cEliteMobs",
                                "&efeatures required to run &cBetterStructures and nothing else.",
                                "&9Click to select this option!")));
            }

            @Override
            public void run(Player player) {
                player.closeInventory();
                DefaultConfig.toggleSetupDone(true);
                EventsConfig.setEventsEnabled(false);
                Logger.sendSimpleMessage(player, "&8&m-----------------------------------------------------");
                Logger.sendSimpleMessage(player, "&2You are now using the &cnot recommended minimum installation&2!");
                Logger.sendSimpleMessage(player, "&6Want to download premade dungeons, arenas, events & more?");
                Logger.sendSimpleMessage(player, "&2See what you can install and how here: &9&nhttps://nightbreak.io/plugin/elitemobs/#setup");
                Logger.sendSimpleMessage(player, "&aCheck the status of installed content through &3/em setup &a!");
                Logger.sendSimpleMessage(player, "&2Support & discussion Discord: &9&nhttps://discord.gg/eSxvPbWYy4");
                Logger.sendSimpleMessage(player, "&8&m-----------------------------------------------------");
            }
        }
        return new fullArchetype();
    }
}
