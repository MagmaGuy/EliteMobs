package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.EliteMobExitCombatEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.elitemobs.powers.meta.MinorPower;
import com.magmaguy.elitemobs.utils.VisualArmorStand;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class ShieldWall extends MinorPower {
    private final HashMap<Direction, List<ArmorStand>> armorStands = new HashMap<>();
    private final HashMap<Direction, Vector> armorStandOffBaseOffsets = new HashMap<>();
    private double northHealthPool;
    private double southHealthPool;
    private double eastHealthPool;
    private double westHealthPool;
    @Getter
    @Setter
    private boolean isActive = false;

    public ShieldWall() {
        super(PowersConfig.getPower("shield_wall.yml"));
        offsetInitializer();
    }

    public boolean preventDamage(Player player, EliteEntity eliteEntity, double damage) {
        if (!Objects.equals(player.getLocation().getWorld(), eliteEntity.getLocation().getWorld())) return false;
        Vector direction = player.getLocation().subtract(eliteEntity.getLocation()).toVector().normalize();
        Direction damageDirection;
        if (Math.abs(direction.getX()) > Math.abs(direction.getZ())) {
            if (direction.getX() > 0)
                damageDirection = Direction.EAST;
            else
                damageDirection = Direction.WEST;
        } else if (direction.getZ() > 0)
            damageDirection = Direction.SOUTH;
        else
            damageDirection = Direction.NORTH;

        switch (damageDirection) {
            case NORTH:
                if (northHealthPool > 0) {
                    northHealthPool -= damage;
                    return true;
                }
                break;
            case SOUTH:
                if (southHealthPool > 0) {
                    southHealthPool -= damage;
                    return true;
                }
                break;
            case EAST:
                if (eastHealthPool > 0) {
                    eastHealthPool -= damage;
                    return true;
                }
                break;
            case WEST:
            default:
                if (westHealthPool > 0) {
                    westHealthPool -= damage;
                    return true;
                }
        }
        return false;
    }

    public void initialize(EliteEntity eliteEntity) {
        if (ThreadLocalRandom.current().nextDouble() >= .1) return;
        doCooldown(eliteEntity);
        /*
        1 = north
        2 = east
        3 = south
        4 = west
         */
        int direction = ThreadLocalRandom.current().nextInt(1, 5);
        northHealthPool = eliteEntity.getLevel();
        southHealthPool = eliteEntity.getLevel();
        eastHealthPool = eliteEntity.getLevel();
        westHealthPool = eliteEntity.getLevel();
        switch (direction) {
            case 1:
                northHealthPool = 0;
                break;
            case 2:
                eastHealthPool = 0;
                break;
            case 3:
                southHealthPool = 0;
                break;
            case 4:
                westHealthPool = 0;
                break;
        }

        for (int i = 1; i < 5; i++)
            if (direction != i)
                armorStands.put(directionConverter(i), armorStandCreator(directionConverter(i), eliteEntity));

        armorStandTracker(eliteEntity);
        setActive(true);
    }

    private Direction directionConverter(int direction) {
        switch (direction) {
            case 1:
                return Direction.NORTH;
            case 2:
                return Direction.EAST;
            case 3:
                return Direction.SOUTH;
            case 4:
            default:
                return Direction.WEST;
        }
    }

    private void offsetInitializer() {
        for (int i = 1; i < 5; i++) {
            switch (i) {
                case 1:
                    armorStandOffBaseOffsets.put(directionConverter(i), new Vector(0, 0, -1));
                    break;
                case 2:
                    armorStandOffBaseOffsets.put(directionConverter(i), new Vector(1, 0, 0));
                    break;
                case 3:
                    armorStandOffBaseOffsets.put(directionConverter(i), new Vector(0, 0, 1));
                    break;
                case 4:
                default:
                    armorStandOffBaseOffsets.put(directionConverter(i), new Vector(-1, 0, 0));
                    break;
            }
        }
    }

    private Location getRealLocation(Direction direction, Location livingEntityLocation, int iteration) {
        Vector finalOffsetVector;
        if (direction.equals(Direction.NORTH) || direction.equals(Direction.SOUTH))
            finalOffsetVector = armorStandOffBaseOffsets.get(direction).clone().add(new Vector(iteration, 0, 0));
        else
            finalOffsetVector = armorStandOffBaseOffsets.get(direction).clone().add(new Vector(0, 0, iteration));
        Location realLocation = livingEntityLocation.clone().add(finalOffsetVector);
        switch (direction) {
            case NORTH:
                realLocation.setYaw(180);
                break;
            case SOUTH:
                realLocation.setYaw(0f);
                break;
            case EAST:
                realLocation.setYaw(-90);
                break;
            case WEST:
            default:
                realLocation.setYaw(90);
        }
        realLocation.setPitch(0);
        return realLocation;
    }

    private List<ArmorStand> armorStandCreator(Direction direction, EliteEntity eliteEntity) {
        List<ArmorStand> armorStands = new ArrayList<>();
        for (int i = -1; i < 2; i++) {
            ArmorStand armorStand = VisualArmorStand.VisualArmorStand(getRealLocation(direction, eliteEntity.getLivingEntity().getLocation(), i), "Barrier");
            armorStands.add(armorStand);
            armorStand.getEquipment().setItemInMainHand(new ItemStack(Material.SHIELD));
            armorStand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.setRightArmPose(new EulerAngle(Math.PI / 2d, Math.PI + Math.PI / 2d, Math.PI));
        }
        return armorStands;
    }

    private void armorStandTracker(EliteEntity eliteEntity) {
        Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, (task) -> {
            if (!eliteEntity.isValid() || (northHealthPool == 0 && southHealthPool == 0 && eastHealthPool == 0 && westHealthPool == 0) || !isActive) {
                task.cancel();
                setActive(false);

                for (List<ArmorStand> armorStands : armorStands.values())
                    if (armorStands != null)
                        for (ArmorStand armorStand : armorStands)
                            if (armorStand != null && armorStand.isValid())
                                EntityTracker.unregister(armorStand, RemovalReason.EFFECT_TIMEOUT);

                return;
            }
            int visualShieldsLeft = 0;
            for (Direction direction : armorStands.keySet()) {
                for (int i = -1; i < 2; i++) {
                    ArmorStand armorStand = armorStands.get(direction).get(i + 1);
                    if (armorStand == null || !armorStand.isValid()) continue;
                    switch (direction) {
                        case NORTH:
                            if (0.33 * (i + 1) >= northHealthPool / (double) eliteEntity.getLevel())
                                EntityTracker.unregister(armorStand, RemovalReason.EFFECT_TIMEOUT);
                            break;
                        case SOUTH:
                            if (0.33 * (i + 1) >= southHealthPool / (double) eliteEntity.getLevel())
                                EntityTracker.unregister(armorStand, RemovalReason.EFFECT_TIMEOUT);
                            break;
                        case EAST:
                            if (0.33 * (i + 1) >= eastHealthPool / (double) eliteEntity.getLevel())
                                EntityTracker.unregister(armorStand, RemovalReason.EFFECT_TIMEOUT);
                            break;
                        case WEST:
                        default:
                            if (0.33 * (i + 1) >= westHealthPool / (double) eliteEntity.getLevel())
                                EntityTracker.unregister(armorStand, RemovalReason.EFFECT_TIMEOUT);
                    }

                    visualShieldsLeft++;
                    armorStand.teleport(getRealLocation(direction, eliteEntity.getLivingEntity().getLocation(), i));
                }
            }
            if (visualShieldsLeft == 0) {
                task.cancel();
                setActive(false);
            }
        }, 1, 1);
    }

    private enum Direction {
        NORTH,
        SOUTH,
        EAST,
        WEST
    }

    public static class ShieldWallEvents implements Listener {
        @EventHandler
        public void onEliteHit(EliteMobDamagedByPlayerEvent event) {
            ElitePower elitePower = event.getEliteMobEntity().getPower(new ShieldWall());
            if (elitePower == null) return;
            ShieldWall shieldWall = (ShieldWall) elitePower;
            if (!shieldWall.isActive()) {
                if (!eventIsValid(event, elitePower)) return;
                shieldWall.initialize(event.getEliteMobEntity());
            } else {
                if (shieldWall.preventDamage(event.getPlayer(), event.getEliteMobEntity(), event.getDamage()))
                    event.setCancelled(true);
            }
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
        public void onCombatLeaveEvent(EliteMobExitCombatEvent event) {
            ElitePower elitePower = event.getEliteMobEntity().getPower(new ShieldWall());
            if (elitePower == null) return;
            ShieldWall shieldWall = (ShieldWall) elitePower;
            if (shieldWall.isActive) shieldWall.setActive(false);
        }
    }

}
