package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.dungeons.DynamicDungeonPackage;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.instanced.dungeons.DynamicDungeonInstance;
import com.magmaguy.elitemobs.skills.CombatLevelCalculator;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.ItemStackGenerator;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DynamicDungeonBrowser extends EliteMenu {
    private static final HashMap<Inventory, DynamicDungeonBrowser> inventories = new HashMap<>();

    public static void shutdown() {
        inventories.clear();
    }

    @Getter
    private final EMPackage emPackage;
    private final MenuType menuType;
    private final List<Integer> levelSlots = List.of(2, 4, 6, 0, 8, 1, 3, 5, 7);
    private final List<Integer> difficultySlots = List.of(2, 4, 6, 0, 8, 1, 3, 5, 7);
    private final List<Integer> validSlots = new ArrayList<>(List.of(18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30,
            31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53));
    private Integer selectedLevel = null;
    private List<DynamicDungeonInstance> instancesList = new ArrayList<>();

    public DynamicDungeonBrowser(Player player, String dynamicDungeonName) {
        this(player, dynamicDungeonName, MenuType.LEVEL_SELECTION, null);
    }

    private DynamicDungeonBrowser(Player player, String dynamicDungeonName, MenuType menuType, Integer selectedLevel) {
        EMPackage emPackage = EMPackage.getEmPackages().get(dynamicDungeonName);
        this.emPackage = emPackage;
        this.menuType = menuType;
        this.selectedLevel = selectedLevel;

        if (!(emPackage instanceof DynamicDungeonPackage)) {
            player.sendMessage("[EliteMobs] Not a valid dynamic dungeon!");
            return;
        }

        if (menuType == MenuType.LEVEL_SELECTION) {
            showLevelSelectionMenu(player, dynamicDungeonName);
        } else {
            showDifficultySelectionMenu(player, dynamicDungeonName);
        }
    }

    private void showLevelSelectionMenu(Player player, String dynamicDungeonName) {
        // Use the player's combat level as the base
        int baseLevel = Math.max(1, CombatLevelCalculator.calculateCombatLevel(player.getUniqueId()));

        int minLevel = Math.max(5, baseLevel - 5);
        int maxLevel = Math.min(200, baseLevel + 5);

        List<Integer> availableLevels = new ArrayList<>();
        for (int level = minLevel; level <= maxLevel; level += 5) {
            availableLevels.add(level);
        }

        // Find existing dynamic dungeon instances for this dungeon
        instancesList = DungeonInstance.getDungeonInstances().stream()
                .filter(instance -> instance instanceof DynamicDungeonInstance)
                .filter(instance -> instance.getContentPackagesConfigFields().getFilename().equals(dynamicDungeonName))
                .map(instance -> (DynamicDungeonInstance) instance)
                .collect(Collectors.toList());

        // Expand to 54 slots if there are existing instances to show
        int slots = instancesList.isEmpty() ? 9 : 54;
        Inventory inventory = Bukkit.createInventory(player, slots, ChatColorConverter.convert(DungeonsConfig.getDynamicDungeonLevelSelectionMenuTitle()));

        for (int i = 0; i < availableLevels.size() && i < levelSlots.size(); i++) {
            int level = availableLevels.get(i);
            List<String> description = new ArrayList<>();
            description.add(ChatColorConverter.convert(DungeonsConfig.getDynamicDungeonLevelSelectionClickToSelect().replace("$level", String.valueOf(level))));
            description.add(ChatColorConverter.convert(DungeonsConfig.getDynamicDungeonLevelSelectionMobsWillBeLevel().replace("$level", String.valueOf(level))));

            Material material;
            if (level == baseLevel) {
                material = Material.LIME_STAINED_GLASS_PANE;
                description.add(ChatColorConverter.convert(DungeonsConfig.getDynamicDungeonLevelSelectionRecommended()));
            } else if (level < baseLevel) {
                material = Material.YELLOW_STAINED_GLASS_PANE;
                description.add(ChatColorConverter.convert(DungeonsConfig.getDynamicDungeonLevelSelectionEasier()));
            } else {
                material = Material.ORANGE_STAINED_GLASS_PANE;
                description.add(ChatColorConverter.convert(DungeonsConfig.getDynamicDungeonLevelSelectionHarder()));
            }

            inventory.setItem(levelSlots.get(i), ItemStackGenerator.generateItemStack(
                    material,
                    ChatColorConverter.convert(DungeonsConfig.getDynamicDungeonLevelSelectionItemTitle().replace("$level", String.valueOf(level))),
                    description));
        }

        // Add existing instances to the lower slots
        for (int i = 0; i < instancesList.size() && i < validSlots.size(); i++) {
            DynamicDungeonInstance instance = instancesList.get(i);
            ItemStack itemStack = null;
            if (instance.getState().equals(MatchInstance.InstancedRegionState.WAITING))
                itemStack = playerItem(instance);
            else if (DungeonsConfig.isAllowSpectatorsInInstancedContent())
                itemStack = spectatorItem(instance);
            if (itemStack != null)
                inventory.setItem(validSlots.get(i), itemStack);
        }

        player.openInventory(inventory);
        inventories.put(inventory, this);
    }

    private ItemStack spectatorItem(DynamicDungeonInstance dungeonInstance) {
        List<String> lore = new ArrayList<>();
        lore.add(ChatColorConverter.convert("&7Level: &e" + dungeonInstance.getSelectedLevel()));
        lore.add(ChatColorConverter.convert("&2Players:"));
        dungeonInstance.getPlayers().forEach(player -> lore.add(ChatColorConverter.convert("&f- " + player.getDisplayName())));
        return ItemStackGenerator.generateItemStack(
                Material.ORANGE_STAINED_GLASS_PANE,
                DungeonsConfig.getDungeonJoinAsSpectatorText().replace("$dungeonName", dungeonInstance.getContentPackagesConfigFields().getName()),
                lore);
    }

    private ItemStack playerItem(DynamicDungeonInstance dungeonInstance) {
        List<String> lore = new ArrayList<>();
        lore.add(ChatColorConverter.convert("&7Level: &e" + dungeonInstance.getSelectedLevel()));
        lore.add(ChatColorConverter.convert("&2Players:"));
        dungeonInstance.getPlayers().forEach(player -> lore.add(ChatColorConverter.convert("&f- " + player.getDisplayName())));
        return ItemStackGenerator.generateItemStack(
                Material.GREEN_STAINED_GLASS_PANE,
                DungeonsConfig.getDungeonJoinAsPlayerText().replace("$dungeonName", dungeonInstance.getContentPackagesConfigFields().getName()),
                lore);
    }

    private void showDifficultySelectionMenu(Player player, String dynamicDungeonName) {
        Inventory inventory = Bukkit.createInventory(player, 9, ChatColorConverter.convert(DungeonsConfig.getDynamicDungeonDifficultySelectionMenuTitle()));

        int difficultyCounter = 0;
        for (Map map : emPackage.getContentPackagesConfigFields().getDifficulties()) {
            List<String> description = new ArrayList<>();
            for (String string : DungeonsConfig.getInstancedDungeonDescription())
                description.add(string.replace("$dungeonName", emPackage.getContentPackagesConfigFields().getName()));
            description.add(ChatColorConverter.convert(DungeonsConfig.getDynamicDungeonDifficultySelectionSelectedLevel().replace("$level", String.valueOf(selectedLevel))));

            inventory.setItem(difficultySlots.get(difficultyCounter), ItemStackGenerator.generateItemStack(
                    Material.GREEN_STAINED_GLASS_PANE,
                    DungeonsConfig.getInstancedDungeonTitle().replace("$difficulty", map.get("name") + ""),
                    description));

            difficultyCounter++;
        }

        player.openInventory(inventory);
        inventories.put(inventory, this);
    }

    private enum MenuType {
        LEVEL_SELECTION,
        DIFFICULTY_SELECTION
    }

    public static class DynamicDungeonBrowserEvents implements Listener {
        @EventHandler
        public void onInventoryInteract(InventoryClickEvent event) {
            if (!isEliteMenu(event, inventories.keySet())) return;
            event.setCancelled(true);
            if (!isTopMenu(event)) return;
            if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) return;

            DynamicDungeonBrowser browser = inventories.get(event.getInventory());
            Player player = (Player) event.getWhoClicked();
            event.getWhoClicked().closeInventory();

            if (browser.menuType == MenuType.LEVEL_SELECTION) {
                if (browser.levelSlots.contains(event.getSlot())) {
                    int levelIndex = browser.levelSlots.indexOf(event.getSlot());
                    // Use the player's combat level as the base
                    int baseLevel = Math.max(1, CombatLevelCalculator.calculateCombatLevel(player.getUniqueId()));
                    int minLevel = Math.max(5, baseLevel - 5);
                    int selectedLevel = minLevel + (levelIndex * 5);

                    new DynamicDungeonBrowser(player,
                            browser.getEmPackage().getContentPackagesConfigFields().getFilename(),
                            MenuType.DIFFICULTY_SELECTION,
                            selectedLevel);
                } else if (browser.validSlots.contains(event.getSlot())) {
                    // Player clicked on an existing instance to join
                    int instanceIndex = browser.validSlots.indexOf(event.getSlot());
                    if (instanceIndex < browser.instancesList.size()) {
                        DynamicDungeonInstance dungeonInstance = browser.instancesList.get(instanceIndex);
                        switch (dungeonInstance.getState()) {
                            case ONGOING, STARTING -> {
                                if (DungeonsConfig.isAllowSpectatorsInInstancedContent())
                                    dungeonInstance.addSpectator(player, false);
                            }
                            case WAITING -> dungeonInstance.addNewPlayer(player);
                            case COMPLETED, COMPLETED_VICTORY, COMPLETED_DEFEAT ->
                                    player.sendMessage("[EliteMobs] This match already ended! Can't join it!");
                        }
                    }
                }
            } else if (browser.menuType == MenuType.DIFFICULTY_SELECTION) {
                if (browser.difficultySlots.contains(event.getSlot())) {
                    String difficultyName = (String) browser.getEmPackage()
                            .getContentPackagesConfigFields()
                            .getDifficulties()
                            .get(browser.difficultySlots.indexOf(event.getSlot()))
                            .get("name");

                    DynamicDungeonInstance.setupDynamicDungeon(
                            player,
                            browser.getEmPackage().getContentPackagesConfigFields().getFilename(),
                            difficultyName,
                            browser.selectedLevel);
                }
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            inventories.remove(event.getInventory());
        }
    }
}
