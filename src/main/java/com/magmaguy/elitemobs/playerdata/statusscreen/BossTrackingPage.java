package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.SpigotMessage;
import com.magmaguy.elitemobs.utils.WarningMessage;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.*;

public class BossTrackingPage {
    private BossTrackingPage() {
    }

    protected static TextComponent[] bossTrackingPage(Player player) {

        TextComponent configTextComponent = new TextComponent();

        for (int i = 0; i < 3; i++) {

            if (PlayerStatusMenuConfig.getBossTrackerHoverLines()[i] == null) continue;

            TextComponent line = new TextComponent(PlayerStatusMenuConfig.getBossTrackerTextLines()[i] + "\n");

            if (!PlayerStatusMenuConfig.getBossTrackerHoverLines()[i].isEmpty())
                PlayerStatusScreen.setHoverText(line, PlayerStatusMenuConfig.getBossTrackerHoverLines()[i]);

            if (!PlayerStatusMenuConfig.getBossTrackerCommandLines()[i].isEmpty())
                line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, PlayerStatusMenuConfig.getBossTrackerCommandLines()[i]));

            configTextComponent.addExtra(line);
        }

        ArrayList<TextComponent> textComponents = new ArrayList<>();
        int counter = 0;

        //Fixes the issue where sometimes trackable bosses stay on the tracking page for no reason
        HashSet<CustomBossEntity> tempSet = new HashSet<>(CustomBossEntity.getTrackableCustomBosses());
        tempSet.forEach(customBossEntity -> {
            if (!customBossEntity.exists())
                CustomBossEntity.getTrackableCustomBosses().remove(customBossEntity);
        });

        for (CustomBossEntity customBossEntity : CustomBossEntity.getTrackableCustomBosses()) {
            try {
                textComponents.add(SpigotMessage.commandHoverMessage(
                        customBossEntity.getCustomBossBossBar().bossBarMessage(player, customBossEntity.getCustomBossesConfigFields().getLocationMessage()) + "\n",
                        PlayerStatusMenuConfig.getOnBossTrackHover(),
                        "/elitemobs trackcustomboss " + customBossEntity.getEliteUUID()));

                counter++;
            } catch (Exception ex) {
                new WarningMessage("Failed to correctly get elements for boss tracking page!");
                ex.printStackTrace();
            }
        }

        TextComponent[] textComponent;
        if (counter == 0) {
            textComponent = new TextComponent[1];
            textComponent[0] = configTextComponent;
        } else {
            textComponent = new TextComponent[(int) Math.floor(counter / 6D) + 1];
            int internalCounter = 0;
            textComponent[0] = configTextComponent;
            for (TextComponent text : textComponents) {
                int currentPage = (int) Math.floor(internalCounter / 6D);
                if (textComponent[currentPage] == null)
                    textComponent[currentPage] = new TextComponent();
                textComponent[currentPage].addExtra(text);
                internalCounter++;
            }
        }
        return textComponent;
    }

    protected static void bossTrackingPage(Player targetPlayer, Player requestingPlayer) {
        Inventory inventory = Bukkit.createInventory(requestingPlayer, 54, PlayerStatusMenuConfig.getBossTrackerChestMenuName());
        int counter = 0;
        BossTrackingPageEvents.bosses.clear();
        for (CustomBossEntity customBossEntity : CustomBossEntity.getTrackableCustomBosses()) {
            BossTrackingPageEvents.bosses.add(customBossEntity);
            inventory.setItem(counter, ItemStackGenerator.generateItemStack(Material.ZOMBIE_HEAD,
                    customBossEntity.getCustomBossBossBar().bossBarMessage(targetPlayer, customBossEntity.getCustomBossesConfigFields().getLocationMessage()),
                    Collections.singletonList(MobCombatSettingsConfig.getBossLocationMessage())));
            counter++;
        }
        inventory.setItem(53, PlayerStatusMenuConfig.getBackItem());
        requestingPlayer.openInventory(inventory);
        BossTrackingPageEvents.pageInventories.add(inventory);
    }

    public static class BossTrackingPageEvents implements Listener {
        private static final Set<Inventory> pageInventories = new HashSet<>();
        private static final List<CustomBossEntity> bosses = new ArrayList<>();

        @EventHandler(ignoreCancelled = true)
        public void onInventoryInteract(InventoryClickEvent event) {
            if (event.getSlot() < 0) return;
            Player player = ((Player) event.getWhoClicked()).getPlayer();
            if (!pageInventories.contains(event.getInventory())) return;
            event.setCancelled(true);
            if (bosses.size() - 1 >= event.getSlot()) {
                player.closeInventory();
                bosses.get(event.getSlot()).getCustomBossBossBar().addTrackingPlayer(player);
                return;
            }
            if (event.getSlot() == 53) {
                player.closeInventory();
                CoverPage.coverPage(player);
            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            pageInventories.remove(event.getInventory());
        }
    }
}
