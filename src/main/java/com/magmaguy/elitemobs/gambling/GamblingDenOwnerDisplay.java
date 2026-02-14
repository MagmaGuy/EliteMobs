package com.magmaguy.elitemobs.gambling;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.GamblingConfig;
import com.magmaguy.elitemobs.economy.GamblingEconomyHandler;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages the "House Earnings" display above the Gambling Den Owner NPC.
 * Updates periodically to show current house profits/losses.
 */
public class GamblingDenOwnerDisplay {

    private static final Map<UUID, TextDisplay> earningsDisplays = new HashMap<>();
    private static BukkitTask updateTask;

    /**
     * Initializes the display updater task.
     */
    public static void initialize() {
        // Update display every second
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateAllDisplays();
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 20L, 20L);
    }

    /**
     * Creates the house earnings display above a Gambling Den Owner NPC.
     *
     * @param npcEntity The NPC entity
     */
    public static void createDisplay(NPCEntity npcEntity) {
        if (npcEntity == null || npcEntity.getVillager() == null) return;
        if (!npcEntity.getNPCsConfigFields().getFilename().equals("gambling_den_owner.yml")) return;

        // Check if display already exists
        if (earningsDisplays.containsKey(npcEntity.getUuid())) return;

        // Create TextDisplay above the NPC - Minecraft handles visibility automatically
        TextDisplay display = npcEntity.getVillager().getWorld().spawn(
                npcEntity.getVillager().getLocation().add(0, 2.8, 0),
                TextDisplay.class,
                textDisplay -> {
                    textDisplay.setText(getDisplayText());
                    textDisplay.setBillboard(Display.Billboard.CENTER);
                    textDisplay.setShadowed(false);
                    textDisplay.setPersistent(false);
                }
        );

        earningsDisplays.put(npcEntity.getUuid(), display);
        EntityTracker.registerVisualEffects(display);
    }

    /**
     * Removes the display for an NPC.
     *
     * @param npcUUID The NPC's UUID
     */
    public static void removeDisplay(UUID npcUUID) {
        TextDisplay display = earningsDisplays.remove(npcUUID);
        if (display != null && display.isValid()) {
            display.remove();
        }
    }

    /**
     * Updates all house earnings displays.
     */
    private static void updateAllDisplays() {
        String displayText = getDisplayText();

        earningsDisplays.entrySet().removeIf(entry -> {
            TextDisplay display = entry.getValue();
            if (display == null || !display.isValid()) {
                return true;
            }
            display.setText(displayText);
            return false;
        });
    }

    /**
     * Gets the formatted display text for house earnings.
     *
     * @return The formatted text
     */
    private static String getDisplayText() {
        return ChatColorConverter.convert(GamblingConfig.getHouseEarningsLabel() + GamblingEconomyHandler.getFormattedHouseEarnings());
    }

    /**
     * Shuts down the display system.
     */
    public static void shutdown() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }

        // Remove all displays
        for (TextDisplay display : earningsDisplays.values()) {
            if (display != null && display.isValid()) {
                display.remove();
            }
        }
        earningsDisplays.clear();
    }
}
