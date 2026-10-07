package com.magmaguy.elitemobs.transport;

import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.plugin.Plugin;
import java.util.*;
import java.util.function.Supplier;

/** Destination menus retain their source and revalidate proximity when clicked. */
final class TransportMenu implements Listener, AutoCloseable {
    private static final double REACH = 8;
    private final TransportModule module;
    private final Plugin plugin;
    private final Set<Inventory> opened = new HashSet<>();

    TransportMenu(TransportModule module, Plugin plugin) {
        this.module = module; this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    void open(Player player, NPCEntity npc) {
        List<String> routes = npc.getNPCsConfigFields().getTransportRoutes().stream()
                .filter(id -> module.files.get(id) != null).distinct().toList();
        if (routes.isEmpty() || routes.size() > 54) {
            TransportModule.message(player, "This attendant needs between 1 and 54 configured destinations."); return;
        }
        List<Option> options = routes.stream().map(id -> new Option("&6" + module.files.get(id).name(), "&7Click to board",
                Material.FEATHER, () -> {
                    if (npc.getNPCsConfigFields().getTransportRoutes().contains(id)) TransportModule.startRoute(player, id);
                }, null)).toList();
        show(player, "Travel destinations", npcAnchor(npc), options);
    }

    void openNetwork(Player player, Supplier<Location> anchor, TransportNetwork network, String fromNode) {
        TransportNode from = network.nodes().get(fromNode);
        if (from == null) { TransportModule.message(player, "This flight point is not part of " + network.id() + "."); return; }
        if (!from.unlockedFor(player)) { TransportModule.message(player, lockedMessage(from)); return; }
        List<TransportNode> destinations = network.reachableDestinations(fromNode);
        if (destinations.isEmpty()) { TransportModule.message(player, "No flights leave from here yet."); return; }
        List<Option> options = new ArrayList<>();
        for (TransportNode destination : destinations.subList(0, Math.min(54, destinations.size()))) {
            boolean unlocked = destination.unlockedFor(player);
            options.add(new Option((unlocked ? "&6" : "&8") + destination.name(),
                    unlocked ? "&7Click to fly" : lockedMessage(destination),
                    unlocked ? Material.FEATHER : Material.GRAY_DYE,
                    unlocked ? () -> module.startFlight(player, network.id(), fromNode, destination.id()) : null,
                    unlocked ? null : lockedMessage(destination)));
        }
        show(player, "Flight paths", anchor, options);
    }

    static String lockedMessage(TransportNode node) {
        return node.lockedHint() == null ? "&7You have not unlocked this flight point." : node.lockedHint();
    }

    static Supplier<Location> npcAnchor(NPCEntity npc) {
        return () -> {
            var villager = npc.getVillager();
            return villager == null || !villager.isValid() ? null : villager.getLocation();
        };
    }

    private void show(Player player, String title, Supplier<Location> anchor, List<Option> options) {
        Choice choice = new Choice(player.getUniqueId(), anchor, options);
        choice.inventory = Bukkit.createInventory(choice, Math.max(9, ((options.size() + 8) / 9) * 9), title);
        for (int slot = 0; slot < options.size(); slot++) {
            Option option = options.get(slot);
            ItemStack item = new ItemStack(option.icon());
            var meta = item.getItemMeta();
            meta.setDisplayName(ChatColorConverter.convert(option.label()));
            meta.setLore(List.of(ChatColorConverter.convert(option.lore())));
            item.setItemMeta(meta);
            choice.inventory.setItem(slot, item);
        }
        opened.add(choice.inventory);
        player.openInventory(choice.inventory);
    }

    @EventHandler public void click(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof Choice choice)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || !choice.player.equals(player.getUniqueId())) return;
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= choice.options.size()) return;
        Option option = choice.options.get(slot);
        if (option.action() == null) { if (option.refusal() != null) TransportModule.message(player, option.refusal()); return; }
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline() || player.getOpenInventory().getTopInventory() != choice.inventory) return;
            player.closeInventory();
            Location anchor = choice.anchor.get();
            if (anchor == null || anchor.getWorld() != player.getWorld()
                    || anchor.distanceSquared(player.getLocation()) > REACH * REACH) return;
            option.action().run();
        });
    }

    @EventHandler public void drag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof Choice) event.setCancelled(true);
    }

    @EventHandler public void inventoryClosed(InventoryCloseEvent event) { opened.remove(event.getInventory()); }

    @Override public void close() {
        for (Inventory inventory : List.copyOf(opened))
            for (var viewer : List.copyOf(inventory.getViewers())) viewer.closeInventory();
        opened.clear(); HandlerList.unregisterAll(this);
    }

    /** A null action keeps the menu open and explains the refusal instead. */
    private record Option(String label, String lore, Material icon, Runnable action, String refusal) { }

    private static final class Choice implements InventoryHolder {
        final UUID player; final Supplier<Location> anchor; final List<Option> options;
        Inventory inventory;
        Choice(UUID player, Supplier<Location> anchor, List<Option> options) { this.player = player; this.anchor = anchor; this.options = options; }
        @Override public Inventory getInventory() { return inventory; }
    }
}
