package wormhole;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.utils.ChunkLocationChecker;
import com.magmaguy.elitemobs.utils.ChunkVectorizer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class WormholeEntry {
    private static HashMap<Integer, List<WormholeEntry>> wormholeEntries = new HashMap<>();

    @Getter
    private final Location location;
    @Getter
    private final Wormhole wormhole;
    @Getter
    private final String locationText;
    @Getter
    @Setter
    private BukkitTask wormholeTask;
    @Getter
    @Setter
    private ArmorStand text;

    public WormholeEntry(Location location, Wormhole wormhole, String locationText) {
        this.location = location;
        this.wormhole = wormhole;
        this.locationText = locationText;
        if (location == null || location.getWorld() == null) return;
        int chunkInt = ChunkVectorizer.hash(location.getChunk());
        if (wormholeEntries.get(chunkInt) == null)
            wormholeEntries.put(chunkInt, new ArrayList<>(Arrays.asList(this)));
        else {
            List<WormholeEntry> wEs = wormholeEntries.get(chunkInt);
            wEs.add(this);
            wormholeEntries.put(chunkInt, wEs);
        }
        if (ChunkLocationChecker.locationIsLoaded(location))
            chunkLoad();
    }

    public void chunkLoad() {
        initializeTextDisplay();
        wormholeTask = WormholeTask.startWormholeTask(this);
    }

    public void chunkUnload() {
        stop();
    }

    public void stop() {
        if (wormholeTask != null)
            wormholeTask.cancel();
    }

    private void initializeTextDisplay() {
        if (locationText == null) return;
        if (location == null || location.getWorld() == null) return;
        text = location.getWorld().spawn(
                location.clone().add(new Vector(0, 1.2, 0).multiply(wormhole.getWormholeConfigFields().getSizeMultiplier())),
                ArmorStand.class, new Consumer<ArmorStand>() {
                    @Override
                    public void accept(ArmorStand armorStand) {
                        armorStand.setCustomName(ChatColorConverter.convert(locationText));
                        armorStand.setCustomNameVisible(true);
                        armorStand.setMarker(true);
                        armorStand.setVisible(false);
                        armorStand.setGravity(false);
                        armorStand.setPersistent(false);
                    }
                });
        EntityTracker.registerVisualEffects(text);
    }

    public static class WormholeEntryEvent implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
        public void chunkLoadEvent(ChunkLoadEvent event) {
            int chunk = ChunkVectorizer.hash(event.getChunk());
            if (!wormholeEntries.containsKey(chunk)) return;
            for (WormholeEntry wormholeEntry : wormholeEntries.get(chunk))
                wormholeEntry.chunkLoad();
        }
    }

}
