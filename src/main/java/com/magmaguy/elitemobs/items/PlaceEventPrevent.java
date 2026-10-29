package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class PlaceEventPrevent implements Listener {

    // Materials whose vanilla right click throws the item or spends it on a block, so an elite item made from one
    // would be gone. Names rather than constants, since some only exist on newer servers.
    // Eating, drinking, weapons, shields, armor and tools keep their vanilla use.
    private static final Set<String> SPENT_ON_USE = Set.of(
            "ENDER_EYE", "ENDER_PEARL", "SNOWBALL", "EGG", "BLUE_EGG", "BROWN_EGG", "EXPERIENCE_BOTTLE",
            "SPLASH_POTION", "LINGERING_POTION", "WIND_CHARGE", "FIRE_CHARGE", "BONE_MEAL",
            "END_CRYSTAL", "ARMOR_STAND", "ITEM_FRAME", "GLOW_ITEM_FRAME", "PAINTING");

    @EventHandler
    public void onPlaceForbiddenItem(PlayerInteractEvent event) {

        if (!ItemSettingsConfig.isPreventCustomItemPlacement())
            return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return;

        ItemStack item = event.getItem();
        if (item == null) return;

        boolean placesBlock = event.isBlockInHand() && action == Action.RIGHT_CLICK_BLOCK;
        if (!placesBlock && !isSpentOnUse(item.getType())) return;

        if (item.getItemMeta() == null || !ItemTagger.isEliteItem(item)) return;

        if (placesBlock) event.setCancelled(true);
        // Deny only the item's own use, so doors, chests and right-click powers still work.
        else event.setUseItemInHand(Event.Result.DENY);

    }

    private static boolean isSpentOnUse(Material material) {
        String name = material.name();
        return SPENT_ON_USE.contains(name) || name.endsWith("_SPAWN_EGG");
    }

}
