package com.magmaguy.elitemobs.advancedcombat.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.bukkit.util.VoxelShape;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlinkPlannerCollisionTest {

    @Test
    void traversesAFullBlockRiseUsingRealPlayerVolumeCollisionChecks() {
        CollisionWorld fixture = new CollisionWorld(
                (x, y, z) -> y == 0 || x >= 3 && y == 1,
                new Vector(.5D, 1D, .5D));

        BlinkPlanner.Plan plan = BlinkPlanner.plan(fixture.player(), 6D).orElseThrow();

        assertEquals(6D, plan.distance(), 1.0E-9D);
        assertEquals(6.5D, plan.destination().getX(), 1.0E-9D);
        assertEquals(2D, plan.destination().getY(), 1.0E-9D);
        assertTrue(SafeMovement.safeStandingPlayerVolume(fixture.player(), plan.destination()));
    }

    @Test
    void traversesAFullBlockDropUsingRealPlayerVolumeCollisionChecks() {
        CollisionWorld fixture = new CollisionWorld(
                (x, y, z) -> x < 3 ? y == 0 : y == -1,
                new Vector(.5D, 1D, .5D));

        BlinkPlanner.Plan plan = BlinkPlanner.plan(fixture.player(), 6D).orElseThrow();

        assertEquals(6D, plan.distance(), 1.0E-9D);
        assertEquals(6.5D, plan.destination().getX(), 1.0E-9D);
        assertEquals(0D, plan.destination().getY(), .075D);
        assertTrue(SafeMovement.safeStandingPlayerVolume(fixture.player(), plan.destination()));
    }

    @Test
    void stopsAtLastValidPointBeforeATwoBlockRise() {
        CollisionWorld fixture = new CollisionWorld(
                (x, y, z) -> y == 0 || x >= 3 && (y == 1 || y == 2),
                new Vector(.5D, 1D, .5D));

        BlinkPlanner.Plan plan = BlinkPlanner.plan(fixture.player(), 6D).orElseThrow();

        assertEquals(2D, plan.distance(), 1.0E-9D);
        assertEquals(2.5D, plan.destination().getX(), 1.0E-9D);
        assertEquals(1D, plan.destination().getY(), 1.0E-9D);
        assertTrue(SafeMovement.safeStandingPlayerVolume(fixture.player(), plan.destination()));
    }

    @Test
    void stopsAtLastSupportedPointBeforeATwoBlockCliff() {
        CollisionWorld fixture = new CollisionWorld(
                (x, y, z) -> x < 3 ? y == 0 : y == -2,
                new Vector(.5D, 1D, .5D));

        BlinkPlanner.Plan plan = BlinkPlanner.plan(fixture.player(), 6D).orElseThrow();

        assertEquals(2.75D, plan.distance(), 1.0E-9D);
        assertEquals(3.25D, plan.destination().getX(), 1.0E-9D);
        assertEquals(1D, plan.destination().getY(), 1.0E-9D);
        assertTrue(SafeMovement.safeStandingPlayerVolume(fixture.player(), plan.destination()));
    }

    @Test
    void stopsBeforeAWallEvenWhenTheGroundContinuesBehindIt() {
        CollisionWorld fixture = new CollisionWorld(
                (x, y, z) -> y == 0 || x == 3 && (y == 1 || y == 2),
                new Vector(.5D, 1D, .5D));

        BlinkPlanner.Plan plan = BlinkPlanner.plan(fixture.player(), 6D).orElseThrow();

        assertEquals(2D, plan.distance(), 1.0E-9D);
        assertEquals(2.5D, plan.destination().getX(), 1.0E-9D);
        assertEquals(1D, plan.destination().getY(), 1.0E-9D);
        assertTrue(SafeMovement.safeStandingPlayerVolume(fixture.player(), plan.destination()));
    }

    private static final class CollisionWorld {
        private static final VoxelShape EMPTY = shape(null);
        private static final VoxelShape FULL_BLOCK = shape(new BoundingBox(0, 0, 0, 1, 1, 1));

        private final SolidBlock solidBlock;
        private final Vector playerPosition;
        private final World world;
        private final WorldBorder border;
        private final Player player;

        private CollisionWorld(SolidBlock solidBlock, Vector playerPosition) {
            this.solidBlock = solidBlock;
            this.playerPosition = playerPosition.clone();
            this.world = proxy(World.class, this::worldCall);
            this.border = proxy(WorldBorder.class, this::borderCall);
            this.player = proxy(Player.class, this::playerCall);
        }

        Player player() {
            return player;
        }

        private Object worldCall(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "getBlockAt" -> block((int) args[0], (int) args[1], (int) args[2]);
                case "isChunkLoaded" -> true;
                case "getMinHeight" -> -64;
                case "getMaxHeight" -> 320;
                case "getWorldBorder" -> border;
                default -> defaultValue(method.getReturnType());
            };
        }

        private Object borderCall(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "isInside" -> true;
                case "getWorld" -> world;
                default -> defaultValue(method.getReturnType());
            };
        }

        private Object playerCall(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "getLocation" -> location(playerPosition.getY());
                case "getEyeLocation" -> location(playerPosition.getY() + 1.62D)
                        .setDirection(new Vector(1D, 0D, 0D));
                case "getBoundingBox" -> new BoundingBox(
                        playerPosition.getX() - .3D,
                        playerPosition.getY(),
                        playerPosition.getZ() - .3D,
                        playerPosition.getX() + .3D,
                        playerPosition.getY() + 1.8D,
                        playerPosition.getZ() + .3D);
                case "getWorld" -> world;
                case "getWorldBorder" -> border;
                default -> defaultValue(method.getReturnType());
            };
        }

        private Location location(double y) {
            return new Location(world, playerPosition.getX(), y, playerPosition.getZ(), -90F, 0F);
        }

        private Block block(int x, int y, int z) {
            boolean solid = solidBlock.test(x, y, z);
            return proxy(Block.class, (proxy, method, args) -> switch (method.getName()) {
                case "getCollisionShape" -> solid ? FULL_BLOCK : EMPTY;
                case "getType" -> solid ? Material.STONE : Material.AIR;
                case "isLiquid" -> false;
                default -> defaultValue(method.getReturnType());
            });
        }

        private static VoxelShape shape(BoundingBox box) {
            return new VoxelShape() {
                @Override
                public Collection<BoundingBox> getBoundingBoxes() {
                    return box == null ? java.util.List.of() : java.util.List.of(box);
                }

                @Override
                public boolean overlaps(BoundingBox other) {
                    return box != null && box.overlaps(other);
                }
            };
        }

        @SuppressWarnings("unchecked")
        private static <T> T proxy(Class<T> type, InvocationHandler handler) {
            return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                    (proxy, method, args) -> objectMethod(proxy, method, args, handler));
        }

        private static Object objectMethod(
                Object proxy,
                Method method,
                Object[] args,
                InvocationHandler handler) throws Throwable {
            return switch (method.getName()) {
                case "equals" -> proxy == args[0];
                case "hashCode" -> System.identityHashCode(proxy);
                case "toString" -> "CollisionFixture<" + proxy.getClass().getInterfaces()[0].getSimpleName() + ">";
                default -> handler.invoke(proxy, method, args);
            };
        }

        private static Object defaultValue(Class<?> type) {
            if (!type.isPrimitive()) return null;
            if (type == boolean.class) return false;
            if (type == char.class) return '\0';
            if (type == byte.class) return (byte) 0;
            if (type == short.class) return (short) 0;
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == float.class) return 0F;
            return 0D;
        }
    }

    @FunctionalInterface
    private interface SolidBlock {
        boolean test(int x, int y, int z);
    }
}
