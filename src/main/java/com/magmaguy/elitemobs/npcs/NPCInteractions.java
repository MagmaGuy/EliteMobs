package com.magmaguy.elitemobs.npcs;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.adventurersguild.AdventurersGuildGUI;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class NPCInteractions implements Listener {

    public enum NPCInteractionType {
        GUILD_GREETER,
        CHAT,
        CUSTOM_SHOP,
        PROCEDURALLY_GENERATED_SHOP,
        ARENA,
        NONE
    }

    @EventHandler
    public void playerNPCInteract(PlayerInteractAtEntityEvent event) {

        NPCEntity npcEntity = EntityTracker.getNPCEntity(event.getRightClicked());
        if (npcEntity == null) return;

        event.setCancelled(true);

        Bukkit.getLogger().warning("Interacting");

        switch (npcEntity.getInteractionType()) {
            case GUILD_GREETER:
                Bukkit.getLogger().warning("Running");
                AdventurersGuildGUI.mainMenu(event.getPlayer());

                break;
            case CHAT:
                break;
            case CUSTOM_SHOP:
                break;
            case PROCEDURALLY_GENERATED_SHOP:
                break;
            case ARENA:
                break;
            case NONE:
                break;

        }

    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {

        if (!event.getInventory().getType().equals(InventoryType.MERCHANT)) return;

        for (NPCEntity npcEntity : EntityTracker.getNPCEntities())
            if (event.getInventory().getName().equals(npcEntity.getName())) {
                event.setCancelled(true);
                return;
            }

    }

}
