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

/** Destination selection retains its NPC source and revalidates proximity when clicked. */
final class TransportMenu implements Listener, AutoCloseable {
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
        Choice choice = new Choice(player.getUniqueId(), npc, routes);
        choice.inventory = Bukkit.createInventory(choice, ((routes.size() + 8) / 9) * 9, "Travel destinations");
        for (int slot = 0; slot < routes.size(); slot++) {
            TransportRoute route = module.files.get(routes.get(slot));
            ItemStack item = new ItemStack(Material.FEATHER);
            var meta = item.getItemMeta();
            meta.setDisplayName(ChatColorConverter.convert("&6" + route.name()));
            meta.setLore(List.of(ChatColorConverter.convert("&7Click to board")));
            item.setItemMeta(meta); choice.inventory.setItem(slot, item);
        }
        opened.add(choice.inventory); player.openInventory(choice.inventory);
    }
    @EventHandler public void click(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof Choice choice)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || !choice.player.equals(player.getUniqueId())) return;
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= choice.routes.size()) return;
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline() || player.getOpenInventory().getTopInventory() != choice.inventory) return;
            player.closeInventory();
            var villager = choice.npc.getVillager();
            if (villager == null || !villager.isValid() || villager.getWorld() != player.getWorld()
                    || villager.getLocation().distanceSquared(player.getLocation()) > 64) return;
            String route = choice.routes.get(slot);
            if (choice.npc.getNPCsConfigFields().getTransportRoutes().contains(route)) TransportModule.startRoute(player, route);
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
    private static final class Choice implements InventoryHolder {
        final UUID player; final NPCEntity npc; final List<String> routes;
        Inventory inventory;
        Choice(UUID player, NPCEntity npc, List<String> routes) { this.player = player; this.npc = npc; this.routes = routes; }
        @Override public Inventory getInventory() { return inventory; }
    }
}
