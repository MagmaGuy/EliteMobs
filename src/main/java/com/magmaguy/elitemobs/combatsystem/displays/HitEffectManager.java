package com.magmaguy.elitemobs.combatsystem.displays;

import com.magmaguy.easyminecraftgoals.internal.FakeText;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.utils.VisualDisplay;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

/** Owns the spawned entities and tick lifecycle of weak/resist hit effects. */
final class HitEffectManager {

    private static final List<HitEffect> activeEffects = new ArrayList<>();

    private HitEffectManager() {
    }

    static void createResistEffect(EliteEntity eliteEntity, Player player) {
        if (!sameValidWorld(eliteEntity, player)) return;
        try {
            activeEffects.add(new ResistEffect(eliteEntity, player));
        } catch (RuntimeException ignored) {
            // A cosmetic spawn failure must not affect combat.
        }
    }

    static void createWeakEffect(EliteEntity eliteEntity, Player player) {
        if (!sameValidWorld(eliteEntity, player)) return;
        WeakEffect effect = WeakEffect.create(eliteEntity, player);
        if (effect != null) activeEffects.add(effect);
    }

    static void update() {
        Iterator<HitEffect> iterator = activeEffects.iterator();
        while (iterator.hasNext()) {
            HitEffect effect = iterator.next();
            boolean keep = false;
            try {
                keep = effect.update();
            } catch (RuntimeException exception) {
                MetadataHandler.PLUGIN.getLogger().log(
                        Level.WARNING, "Discarding a failed combat hit effect", exception);
            }
            if (keep) continue;
            try {
                effect.cleanup();
            } catch (RuntimeException exception) {
                MetadataHandler.PLUGIN.getLogger().log(
                        Level.WARNING, "Failed to clean a combat hit effect", exception);
            } finally {
                iterator.remove();
            }
        }
    }

    static void shutdown() {
        activeEffects.forEach(effect -> {
            try {
                effect.cleanup();
            } catch (RuntimeException exception) {
                MetadataHandler.PLUGIN.getLogger().log(
                        Level.WARNING, "Failed to clean a combat hit effect during shutdown", exception);
            }
        });
        activeEffects.clear();
    }

    private static boolean sameValidWorld(EliteEntity eliteEntity, Player player) {
        if (eliteEntity == null || player == null || !eliteEntity.isValid() || !player.isValid()) return false;
        Location location = eliteEntity.getLocation();
        return location != null && location.getWorld() != null && location.getWorld().equals(player.getWorld());
    }

    private static Location resistLocation(Player player, EliteEntity eliteEntity) {
        Vector direction = player.getLocation().subtract(eliteEntity.getLocation())
                .toVector().normalize().multiply(1.5);
        Location location = eliteEntity.getLocation().add(direction);
        location.setDirection(direction);
        return location;
    }

    private interface HitEffect {
        boolean update();

        void cleanup();
    }

    private static final class ResistEffect implements HitEffect {
        private final EliteEntity eliteEntity;
        private final Player player;
        private final ArmorStand armorStand;
        private int ticks;
        private boolean cleaned;

        private ResistEffect(EliteEntity eliteEntity, Player player) {
            this.eliteEntity = eliteEntity;
            this.player = player;
            Location location = resistLocation(player, eliteEntity);
            if (location.getWorld() == null) throw new IllegalStateException("Resist effect has no world");
            ArmorStand spawnedArmorStand = location.getWorld().spawn(location, ArmorStand.class, stand -> {
                stand.setVisible(false);
                stand.setGravity(false);
                stand.setMarker(true);
                stand.setPersistent(false);
                stand.getEquipment().setItemInMainHand(new ItemStack(Material.SHIELD));
                stand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
                stand.setRightArmPose(new EulerAngle(Math.PI / 2d, Math.PI + Math.PI / 2d, Math.PI));
            });
            try {
                EntityTracker.registerVisualEffects(spawnedArmorStand);
            } catch (RuntimeException exception) {
                spawnedArmorStand.remove();
                throw exception;
            }
            armorStand = spawnedArmorStand;
        }

        @Override
        public boolean update() {
            if (ticks > 20 || !sameValidWorld(eliteEntity, player)) return false;
            try {
                armorStand.teleport(resistLocation(player, eliteEntity));
            } catch (IllegalArgumentException ignored) {
                // A transient non-finite direction only skips this frame.
            }
            ticks++;
            return true;
        }

        @Override
        public void cleanup() {
            if (cleaned) return;
            EntityTracker.unregister(armorStand, RemovalReason.EFFECT_TIMEOUT);
            cleaned = true;
        }
    }

    private static final class WeakEffect implements HitEffect {
        private final EliteEntity eliteEntity;
        private final Player player;
        private final FakeText[] displays;
        private final Location[] locations;
        private int ticks;

        private WeakEffect(EliteEntity eliteEntity, Player player, FakeText[] displays, Location[] locations) {
            this.eliteEntity = eliteEntity;
            this.player = player;
            this.displays = displays;
            this.locations = locations;
        }

        private static WeakEffect create(EliteEntity eliteEntity, Player player) {
            FakeText[] displays = new FakeText[2];
            Location[] locations = new Location[2];
            displays[0] = createDisplay(player, eliteEntity, -1, locations, 0);
            displays[1] = createDisplay(player, eliteEntity, 1, locations, 1);
            if (displays[0] != null && displays[1] != null)
                return new WeakEffect(eliteEntity, player, displays, locations);
            for (FakeText display : displays) if (display != null) display.remove();
            return null;
        }

        @Override
        public boolean update() {
            if (ticks > 10 || !sameValidWorld(eliteEntity, player)) return false;
            for (int index = 0; index < displays.length; index++) {
                Location newLocation = locations[index].add(eliteEntity.getLocation()
                        .subtract(locations[index]).toVector().normalize().multiply(.4));
                displays[index].teleport(newLocation);
            }
            ticks++;
            return true;
        }

        @Override
        public void cleanup() {
            for (FakeText display : displays) if (display != null) display.remove();
        }

        private static FakeText createDisplay(Player player, EliteEntity eliteEntity, int offset,
                                              Location[] locations, int index) {
            Vector direction = player.getLocation().clone().add(new Vector(0, 2, 0))
                    .subtract(eliteEntity.getLocation()).toVector().normalize().multiply(3.0)
                    .rotateAroundY(Math.PI / 8 * offset);
            Location location = eliteEntity.getLocation().add(direction);
            location.setDirection(direction.clone().multiply(-1));
            if (location.getWorld() == null) return null;

            locations[index] = location.clone();
            FakeText display = VisualDisplay.createStyledFakeText(
                    location, "&9&l✦", Color.fromARGB(0, 0, 0, 0), true, 1.0f);
            if (display != null) display.displayTo(player);
            return display;
        }
    }
}
