package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.dungeons.WorldInstancedDungeonPackage;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class InstancedDungeonBrowser extends EliteMenu {
    private static final HashMap<Inventory, InstancedDungeonBrowser> inventories = new HashMap<>();

    private final List<Integer> difficultySlots = List.of(2, 4, 6, 0, 8, 1, 3, 5, 7);
    private final List<Integer> validSlots = new ArrayList<>(List.of(18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30,
            31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53));
    @Getter
    private final EMPackage emPackage;
    private List<DungeonInstance> instancesList;

    public InstancedDungeonBrowser(Player player, String instancedDungeonName) {
        EMPackage emPackage = EMPackage.getEmPackages().get(instancedDungeonName);
        this.emPackage = emPackage;
        if (!(emPackage instanceof WorldInstancedDungeonPackage)) {
            player.sendMessage("[EliteRabbit] Not a valid instanced dungeon!");
            return;
        }
        Set<DungeonInstance> dungeonInstances = new HashSet<>();
        DungeonInstance.getDungeonInstances().forEach(instance -> {
            if (instance.getDungeonPackagerConfigFields().getFilename().equals(instancedDungeonName))
                dungeonInstances.add(instance);
        });
        instancesList = new ArrayList<>(dungeonInstances);
        int slots;
        if (dungeonInstances.isEmpty()) slots = 9;
        else slots = 54;
        Inventory inventory = Bukkit.createInventory(player, slots);
        int difficultyCounter = 0;
        for (Map map : emPackage.getDungeonPackagerConfigFields().getDifficulties()) {
            List<String> description = new ArrayList<>();
            for (String string : DungeonsConfig.getInstancedDungeonDescription())
                description.add(string.replace("$dungeonName", emPackage.getDungeonPackagerConfigFields().getName()));
            inventory.setItem(difficultySlots.get(difficultyCounter), ItemStackGenerator.generateItemStack(Material.GREEN_STAINED_GLASS_PANE,
                    DungeonsConfig.getInstancedDungeonTitle().replace("$difficulty", map.get("name") + ""),
                    description));

            difficultyCounter++;
        }

        for (int i = 0; i < dungeonInstances.size(); i++) {
            ItemStack itemStack = null;
            if (instancesList.get(i).getState().equals(MatchInstance.InstancedRegionState.WAITING))
                itemStack = playerItem(instancesList.get(i));
            else if (DungeonsConfig.isAllowSpectatorsInInstancedContent())
                itemStack = spectatorItem(instancesList.get(i));
            if (itemStack != null)
                inventory.setItem(validSlots.get(i), itemStack);
        }
        player.openInventory(inventory);
        inventories.put(inventory, this);
    }

    private ItemStack spectatorItem(DungeonInstance dungeonInstance) {
        List<String> players = new ArrayList<>();
        players.add(ChatColorConverter.convert("&2Players:"));
        dungeonInstance.getPlayers().forEach(player -> players.add(player.getDisplayName()));
        return ItemStackGenerator.generateItemStack(
                Material.ORANGE_STAINED_GLASS_PANE,
                DungeonsConfig.getDungeonJoinAsSpectatorText().replace("$dungeonName", dungeonInstance.getDungeonPackagerConfigFields().getName()),
                players);
    }

    private ItemStack playerItem(DungeonInstance dungeonInstance) {
        List<String> players = new ArrayList<>();
        players.add(ChatColorConverter.convert("&2Players:"));
        dungeonInstance.getPlayers().forEach(player -> players.add(player.getDisplayName()));
        return ItemStackGenerator.generateItemStack(
                Material.GREEN_STAINED_GLASS_PANE,
                DungeonsConfig.getDungeonJoinAsPlayerText().replace("$dungeonName", dungeonInstance.getDungeonPackagerConfigFields().getName()),
                players);
    }

    public static class InstancedDungeonBrowserEvents implements Listener {
        @EventHandler
        public void onInventoryInteract(InventoryClickEvent event) {
            if (!isEliteMenu(event, inventories.keySet())) return;
            event.setCancelled(true);
            if (!isTopMenu(event)) return;
            if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) return;
            InstancedDungeonBrowser instancedDungeonBrowser = inventories.get(event.getInventory());
            //Case for creating a new instance
            if (instancedDungeonBrowser.difficultySlots.contains(event.getSlot())) {
                DungeonInstance.setupInstancedDungeon((Player) event.getWhoClicked(),
                        instancedDungeonBrowser.getEmPackage().getDungeonPackagerConfigFields().getFilename(),
                        (String) instancedDungeonBrowser.getEmPackage().getDungeonPackagerConfigFields().getDifficulties().get(instancedDungeonBrowser.difficultySlots.indexOf(event.getSlot())).get("name"));
            } else {
                DungeonInstance dungeonInstance = instancedDungeonBrowser.instancesList.get(instancedDungeonBrowser.validSlots.indexOf(event.getSlot()));
                switch (dungeonInstance.getState()) {
                    case ONGOING, STARTING -> {
                        if (DungeonsConfig.isAllowSpectatorsInInstancedContent())
                            dungeonInstance.addSpectator((Player) event.getWhoClicked(), false);
                    }
                    case WAITING -> dungeonInstance.addNewPlayer((Player) event.getWhoClicked());
                    case COMPLETED ->
                            event.getWhoClicked().sendMessage("[EliteRabbit] This match already ended! Can't join it!");
                }
                event.getWhoClicked().closeInventory();
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            inventories.remove(event.getInventory());
        }
    }
}