package com.magmaguy.elitemobs.powers.specialpowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.utils.VisualDisplay;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ShieldWallSupport {

    private static final Map<UUID, ShieldWallState> states = new ConcurrentHashMap<>();

    private ShieldWallSupport() {
    }

    public static boolean isActive(EliteEntity eliteEntity) {
        ShieldWallState state = states.get(eliteEntity.getEliteUUID());
        return state != null && state.isActive;
    }

    public static boolean initialize(EliteEntity eliteEntity, int openingDirection) {
        ShieldWallState existing = states.get(eliteEntity.getEliteUUID());
        if (existing != null && existing.isActive) {
            return false;
        }
        ShieldWallState state = new ShieldWallState(eliteEntity);
        state.initialize(openingDirection);
        states.put(eliteEntity.getEliteUUID(), state);
        return true;
    }

    public static boolean preventDamage(Player player, EliteEntity eliteEntity, double damage) {
        ShieldWallState state = states.get(eliteEntity.getEliteUUID());
        return state != null && state.preventDamage(player, eliteEntity, damage);
    }

    public static void deactivate(EliteEntity eliteEntity) {
        ShieldWallState state = states.remove(eliteEntity.getEliteUUID());
        if (state != null) {
            state.deactivate();
        }
    }

    private enum Direction {
        NORTH,
        SOUTH,
        EAST,
        WEST
    }

    private static final class ShieldWallState {
        private final EliteEntity eliteEntity;
        private final Map<Direction, List<ArmorStand>> armorStands = new EnumMap<>(Direction.class);
        private final Map<Direction, Vector> armorStandOffBaseOffsets = new EnumMap<>(Direction.class);
        private double northHealthPool;
        private double southHealthPool;
        private double eastHealthPool;
        private double westHealthPool;
        private boolean isActive = false;
        private BukkitTask task = null;

        private ShieldWallState(EliteEntity eliteEntity) {
            this.eliteEntity = eliteEntity;
            offsetInitializer();
        }

        private boolean preventDamage(Player player, EliteEntity eliteEntity, double damage) {
            if (!Objects.equals(player.getLocation().getWorld(), eliteEntity.getLocation().getWorld())) return false;
            Vector direction = player.getLocation().subtract(eliteEntity.getLocation()).toVector().normalize();
            Direction damageDirection;
            if (Math.abs(direction.getX()) > Math.abs(direction.getZ())) {
                damageDirection = direction.getX() > 0 ? Direction.EAST : Direction.WEST;
            } else if (direction.getZ() > 0) {
                damageDirection = Direction.SOUTH;
            } else {
                damageDirection = Direction.NORTH;
            }

            switch (damageDirection) {
                case NORTH -> {
                    if (northHealthPool > 0) {
                        northHealthPool -= damage;
                        return true;
                    }
                }
                case SOUTH -> {
                    if (southHealthPool > 0) {
                        southHealthPool -= damage;
                        return true;
                    }
                }
                case EAST -> {
                    if (eastHealthPool > 0) {
                        eastHealthPool -= damage;
                        return true;
                    }
                }
                case WEST -> {
                    if (westHealthPool > 0) {
                        westHealthPool -= damage;
                        return true;
                    }
                }
            }
            return false;
        }

        private void initialize(int direction) {
            northHealthPool = eliteEntity.getLevel();
            southHealthPool = eliteEntity.getLevel();
            eastHealthPool = eliteEntity.getLevel();
            westHealthPool = eliteEntity.getLevel();
            switch (direction) {
                case 1 -> northHealthPool = 0;
                case 2 -> eastHealthPool = 0;
                case 3 -> southHealthPool = 0;
                case 4 -> westHealthPool = 0;
                default -> {
                }
            }

            for (int i = 1; i < 5; i++) {
                if (direction != i) {
                    armorStands.put(directionConverter(i), armorStandCreator(directionConverter(i), eliteEntity));
                }
            }

            armorStandTracker(eliteEntity);
            isActive = true;
        }

        private void deactivate() {
            isActive = false;
            if (task != null) {
                task.cancel();
                task = null;
            }
            for (List<ArmorStand> stands : armorStands.values()) {
                if (stands == null) continue;
                for (ArmorStand armorStand : stands) {
                    if (armorStand != null && armorStand.isValid()) {
                        EntityTracker.unregister(armorStand, RemovalReason.EFFECT_TIMEOUT);
                    }
                }
            }
            armorStands.clear();
        }

        private Direction directionConverter(int direction) {
            return switch (direction) {
                case 1 -> Direction.NORTH;
                case 2 -> Direction.EAST;
                case 3 -> Direction.SOUTH;
                default -> Direction.WEST;
            };
        }

        private void offsetInitializer() {
            armorStandOffBaseOffsets.put(Direction.NORTH, new Vector(0, 0, -1));
            armorStandOffBaseOffsets.put(Direction.EAST, new Vector(1, 0, 0));
            armorStandOffBaseOffsets.put(Direction.SOUTH, new Vector(0, 0, 1));
            armorStandOffBaseOffsets.put(Direction.WEST, new Vector(-1, 0, 0));
        }

        private Location getRealLocation(Direction direction, Location livingEntityLocation, int iteration) {
            Vector finalOffsetVector;
            if (direction == Direction.NORTH || direction == Direction.SOUTH) {
                finalOffsetVector = armorStandOffBaseOffsets.get(direction).clone().add(new Vector(iteration, 0, 0));
            } else {
                finalOffsetVector = armorStandOffBaseOffsets.get(direction).clone().add(new Vector(0, 0, iteration));
            }
            Location realLocation = livingEntityLocation.clone().add(finalOffsetVector);
            switch (direction) {
                case NORTH -> realLocation.setYaw(180);
                case SOUTH -> realLocation.setYaw(0f);
                case EAST -> realLocation.setYaw(-90);
                case WEST -> realLocation.setYaw(90);
            }
            realLocation.setPitch(0);
            return realLocation;
        }

        private List<ArmorStand> armorStandCreator(Direction direction, EliteEntity eliteEntity) {
            List<ArmorStand> armorStands = new ArrayList<>();
            for (int i = -1; i < 2; i++) {
                ArmorStand armorStand = VisualDisplay.generateTemporaryArmorStand(getRealLocation(direction, eliteEntity.getLivingEntity().getLocation(), i), "Barrier");
                armorStands.add(armorStand);
                armorStand.getEquipment().setItemInMainHand(new ItemStack(Material.SHIELD));
                armorStand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
                armorStand.setRightArmPose(new EulerAngle(Math.PI / 2d, Math.PI + Math.PI / 2d, Math.PI));
            }
            return armorStands;
        }

        private void armorStandTracker(EliteEntity eliteEntity) {
            task = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, () -> {
                if (!eliteEntity.isValid() || (northHealthPool == 0 && southHealthPool == 0 && eastHealthPool == 0 && westHealthPool == 0) || !isActive) {
                    deactivate();
                    states.remove(eliteEntity.getEliteUUID());
                    return;
                }
                int visualShieldsLeft = 0;
                for (Direction direction : armorStands.keySet()) {
                    for (int i = -1; i < 2; i++) {
                        ArmorStand armorStand = armorStands.get(direction).get(i + 1);
                        if (armorStand == null || !armorStand.isValid()) continue;
                        switch (direction) {
                            case NORTH -> {
                                if (0.33 * (i + 1) >= northHealthPool / (double) eliteEntity.getLevel()) {
                                    EntityTracker.unregister(armorStand, RemovalReason.EFFECT_TIMEOUT);
                                }
                            }
                            case SOUTH -> {
                                if (0.33 * (i + 1) >= southHealthPool / (double) eliteEntity.getLevel()) {
                                    EntityTracker.unregister(armorStand, RemovalReason.EFFECT_TIMEOUT);
                                }
                            }
                            case EAST -> {
                                if (0.33 * (i + 1) >= eastHealthPool / (double) eliteEntity.getLevel()) {
                                    EntityTracker.unregister(armorStand, RemovalReason.EFFECT_TIMEOUT);
                                }
                            }
                            case WEST -> {
                                if (0.33 * (i + 1) >= westHealthPool / (double) eliteEntity.getLevel()) {
                                    EntityTracker.unregister(armorStand, RemovalReason.EFFECT_TIMEOUT);
                                }
                            }
                        }

                        visualShieldsLeft++;
                        armorStand.teleport(getRealLocation(direction, eliteEntity.getLivingEntity().getLocation(), i));
                    }
                }
                if (visualShieldsLeft == 0) {
                    deactivate();
                    states.remove(eliteEntity.getEliteUUID());
                }
            }, 1, 1);
        }
    }
}
