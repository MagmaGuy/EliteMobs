package com.magmaguy.elitemobs.experimentalcombat.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.damage.DamageSource;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DivineSteedManagerTest {

    @Test
    void summonsALiteralArmoredHorseAndMountsItsOwner() {
        SteedFixture fixture = new SteedFixture();
        DivineSteedManager manager = new DivineSteedManager(fixture.plugin());

        Horse summoned = manager.summon(
                fixture.player(), 100, DivineSteedManager.RideListener.NOOP).orElseThrow();

        assertSame(fixture.horse(), summoned);
        assertEquals(Horse.class, fixture.spawnedType());
        assertFalse(fixture.persistent());
        assertTrue(fixture.removeWhenFarAway());
        assertTrue(fixture.adult());
        assertTrue(fixture.tamed());
        assertSame(fixture.player(), fixture.owner());
        assertEquals(Horse.Color.WHITE, fixture.color());
        assertEquals(Horse.Style.NONE, fixture.style());
        assertEquals(Material.SADDLE, fixture.saddle().getType());
        assertEquals(Material.GOLDEN_HORSE_ARMOR, fixture.armor().getType());
        assertEquals(List.of(fixture.player()), fixture.passengers());
        assertSame(fixture.horse(), fixture.vehicle());
    }

    @Test
    void expiresAfterFiveSecondsAndRemovesEveryTemporaryItem() {
        SteedFixture fixture = new SteedFixture();
        DivineSteedManager manager = new DivineSteedManager(fixture.plugin());
        int[] pulses = {0};
        int[] endings = {0};
        manager.summon(fixture.player(), 100, new DivineSteedManager.RideListener() {
            @Override
            public void onTick(Horse horse) {
                pulses[0]++;
            }

            @Override
            public void onEnd(Horse horse) {
                endings[0]++;
            }
        }).orElseThrow();

        fixture.pulse(99);
        assertTrue(fixture.horseValid());
        assertSame(fixture.horse(), fixture.vehicle());

        fixture.pulse(1);
        assertFalse(fixture.horseValid());
        assertEquals(Material.AIR, fixture.saddle().getType());
        assertEquals(Material.AIR, fixture.armor().getType());
        assertEquals(100, pulses[0]);
        assertEquals(1, endings[0]);
    }

    @Test
    void dismountImmediatelyEndsTheRide() {
        SteedFixture fixture = new SteedFixture();
        DivineSteedManager manager = new DivineSteedManager(fixture.plugin());
        manager.summon(fixture.player(), 100, DivineSteedManager.RideListener.NOOP).orElseThrow();

        manager.onVehicleExit(new VehicleExitEvent(fixture.horse(), fixture.player()));

        assertFalse(fixture.horseValid());
        assertEquals(Material.AIR, fixture.saddle().getType());
        assertEquals(Material.AIR, fixture.armor().getType());
    }

    @Test
    void casterDeactivationRemovesTheRide() {
        SteedFixture fixture = new SteedFixture();
        DivineSteedManager manager = new DivineSteedManager(fixture.plugin());
        manager.summon(fixture.player(), 100, DivineSteedManager.RideListener.NOOP).orElseThrow();

        manager.deactivate(fixture.player());

        assertFalse(fixture.horseValid());
        assertEquals(Material.AIR, fixture.saddle().getType());
        assertEquals(Material.AIR, fixture.armor().getType());
    }

    @Test
    void deathClearsAllDropsAndTemporaryEquipment() {
        SteedFixture fixture = new SteedFixture();
        DivineSteedManager manager = new DivineSteedManager(fixture.plugin());
        manager.summon(fixture.player(), 100, DivineSteedManager.RideListener.NOOP).orElseThrow();
        DamageSource damageSource = SteedFixture.proxy(
                DamageSource.class,
                (proxy, method, args) -> SteedFixture.defaultValue(method.getReturnType()));
        List<ItemStack> drops = new ArrayList<>(List.of(
                new ItemStack(Material.LEATHER),
                new ItemStack(Material.SADDLE),
                new ItemStack(Material.GOLDEN_HORSE_ARMOR)));
        EntityDeathEvent death = new EntityDeathEvent(fixture.horse(), damageSource, drops, 7);

        manager.onDeath(death);

        assertTrue(death.getDrops().isEmpty());
        assertEquals(0, death.getDroppedExp());
        assertEquals(Material.AIR, fixture.saddle().getType());
        assertEquals(Material.AIR, fixture.armor().getType());
        assertTrue(fixture.passengers().isEmpty());
    }

    @Test
    void temporaryEquipmentCannotBeTakenThroughTheHorseInventory() {
        SteedFixture fixture = new SteedFixture();
        DivineSteedManager manager = new DivineSteedManager(fixture.plugin());
        manager.summon(fixture.player(), 100, DivineSteedManager.RideListener.NOOP).orElseThrow();
        InventoryView view = SteedFixture.proxy(InventoryView.class, (proxy, method, args) -> switch (method.getName()) {
            case "getTopInventory", "getBottomInventory" -> fixture.inventory();
            case "getPlayer" -> fixture.player();
            default -> SteedFixture.defaultValue(method.getReturnType());
        });
        InventoryOpenEvent event = new InventoryOpenEvent(view);

        manager.onInventoryOpen(event);

        assertTrue(event.isCancelled());
    }

    @Test
    void durationExpiryWaitsBrieflyForAControlledLanding() {
        SteedFixture fixture = new SteedFixture();
        fixture.setHorseOnGround(false);
        DivineSteedManager manager = new DivineSteedManager(fixture.plugin());
        manager.summon(fixture.player(), 1, DivineSteedManager.RideListener.NOOP).orElseThrow();

        fixture.pulse(19);
        assertTrue(fixture.horseValid());

        fixture.pulse(1);
        assertFalse(fixture.horseValid());
    }

    @Test
    void shutdownStillRemovesTheSteedWhenAnEndCallbackFails() {
        SteedFixture fixture = new SteedFixture();
        DivineSteedManager manager = new DivineSteedManager(fixture.plugin());
        manager.summon(fixture.player(), 100, new DivineSteedManager.RideListener() {
            @Override
            public void onTick(Horse horse) {
            }

            @Override
            public void onEnd(Horse horse) {
                throw new IllegalStateException("test callback failure");
            }
        }).orElseThrow();

        assertDoesNotThrow(manager::close);
        assertFalse(fixture.horseValid());
        assertTrue(fixture.taskCancelled());
    }

    @Test
    void failedMountingRemovesTheSpawnWithoutCommittingARide() {
        SteedFixture fixture = new SteedFixture();
        fixture.setMountSucceeds(false);
        DivineSteedManager manager = new DivineSteedManager(fixture.plugin());

        assertTrue(manager.summon(
                fixture.player(), 100, DivineSteedManager.RideListener.NOOP).isEmpty());
        assertFalse(fixture.horseValid());
        assertEquals(Material.AIR, fixture.saddle().getType());
        assertEquals(Material.AIR, fixture.armor().getType());
    }

    private static final class SteedFixture {
        private final UUID playerId = UUID.randomUUID();
        private final UUID horseId = UUID.randomUUID();
        private final List<Entity> passengers = new ArrayList<>();
        private final BukkitTask task;
        private final BukkitScheduler scheduler;
        private final PluginManager pluginManager;
        private final Server server;
        private final Plugin plugin;
        private final World world;
        private final HorseInventory inventory;
        private final Player player;
        private final Horse horse;
        private Runnable pulse;
        private Class<?> spawnedType;
        private boolean taskCancelled;
        private boolean persistent = true;
        private boolean removeWhenFarAway;
        private boolean adult;
        private boolean tamed;
        private boolean horseValid = true;
        private boolean horseDead;
        private boolean horseOnGround = true;
        private boolean mountSucceeds = true;
        private Horse.Color color;
        private Horse.Style style;
        private Player owner;
        private Entity vehicle;
        private ItemStack saddle;
        private ItemStack armor;

        private SteedFixture() {
            task = proxy(BukkitTask.class, this::taskCall);
            scheduler = proxy(BukkitScheduler.class, this::schedulerCall);
            pluginManager = proxy(PluginManager.class, this::pluginManagerCall);
            server = proxy(Server.class, this::serverCall);
            plugin = proxy(Plugin.class, this::pluginCall);
            inventory = proxy(HorseInventory.class, this::inventoryCall);
            world = proxy(World.class, this::worldCall);
            player = proxy(Player.class, this::playerCall);
            horse = proxy(Horse.class, this::horseCall);
        }

        Plugin plugin() {
            return plugin;
        }

        Player player() {
            return player;
        }

        Horse horse() {
            return horse;
        }

        HorseInventory inventory() {
            return inventory;
        }

        Class<?> spawnedType() {
            return spawnedType;
        }

        boolean persistent() {
            return persistent;
        }

        boolean removeWhenFarAway() {
            return removeWhenFarAway;
        }

        boolean adult() {
            return adult;
        }

        boolean tamed() {
            return tamed;
        }

        Horse.Color color() {
            return color;
        }

        Horse.Style style() {
            return style;
        }

        Player owner() {
            return owner;
        }

        Entity vehicle() {
            return vehicle;
        }

        ItemStack saddle() {
            return saddle;
        }

        ItemStack armor() {
            return armor;
        }

        boolean horseValid() {
            return horseValid;
        }

        boolean taskCancelled() {
            return taskCancelled;
        }

        void pulse(int times) {
            for (int index = 0; index < times; index++) pulse.run();
        }

        void setHorseOnGround(boolean horseOnGround) {
            this.horseOnGround = horseOnGround;
        }

        void setMountSucceeds(boolean mountSucceeds) {
            this.mountSucceeds = mountSucceeds;
        }

        List<Entity> passengers() {
            return List.copyOf(passengers);
        }

        private Object taskCall(Object proxy, Method method, Object[] args) {
            if (method.getName().equals("cancel")) {
                taskCancelled = true;
                return null;
            }
            if (method.getName().equals("isCancelled")) return taskCancelled;
            return defaultValue(method.getReturnType());
        }

        private Object schedulerCall(Object proxy, Method method, Object[] args) {
            if (method.getName().equals("runTaskTimer") && args[1] instanceof Runnable runnable) {
                pulse = runnable;
                return task;
            }
            return defaultValue(method.getReturnType());
        }

        private Object pluginManagerCall(Object proxy, Method method, Object[] args) {
            return defaultValue(method.getReturnType());
        }

        private Object serverCall(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "getScheduler" -> scheduler;
                case "getPluginManager" -> pluginManager;
                default -> defaultValue(method.getReturnType());
            };
        }

        private Object pluginCall(Object proxy, Method method, Object[] args) {
            if (method.getName().equals("getServer")) return server;
            return defaultValue(method.getReturnType());
        }

        private Object worldCall(Object proxy, Method method, Object[] args) {
            if (method.getName().equals("spawn") && args.length >= 2 && args[1] instanceof Class<?> type) {
                spawnedType = type;
                return horse;
            }
            return defaultValue(method.getReturnType());
        }

        private Object playerCall(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "getUniqueId" -> playerId;
                case "getWorld" -> world;
                case "getLocation" -> new Location(world, 10.5D, 64D, 20.5D, 35F, 0F);
                case "getVehicle" -> vehicle;
                case "isInsideVehicle" -> vehicle != null;
                case "isOnline", "isValid" -> true;
                case "isDead" -> false;
                default -> defaultValue(method.getReturnType());
            };
        }

        private Object horseCall(Object proxy, Method method, Object[] args) {
            switch (method.getName()) {
                case "getUniqueId":
                    return horseId;
                case "getWorld":
                    return world;
                case "getLocation":
                    return new Location(world, 10.5D, 64D, 20.5D, 35F, 0F);
                case "getInventory":
                    return inventory;
                case "getMaxDomestication":
                    return 100;
                case "setPersistent":
                    persistent = (boolean) args[0];
                    return null;
                case "setRemoveWhenFarAway":
                    removeWhenFarAway = (boolean) args[0];
                    return null;
                case "setAdult":
                    adult = true;
                    return null;
                case "setTamed":
                    tamed = (boolean) args[0];
                    return null;
                case "setOwner":
                    owner = (Player) args[0];
                    return null;
                case "setColor":
                    color = (Horse.Color) args[0];
                    return null;
                case "setStyle":
                    style = (Horse.Style) args[0];
                    return null;
                case "getPassengers":
                    return List.copyOf(passengers);
                case "addPassenger":
                    if (!mountSucceeds) return false;
                    passengers.add((Entity) args[0]);
                    vehicle = horse;
                    return true;
                case "removePassenger":
                    boolean removed = passengers.remove(args[0]);
                    if (removed) vehicle = null;
                    return removed;
                case "eject":
                    boolean hadPassenger = !passengers.isEmpty();
                    passengers.clear();
                    vehicle = null;
                    return hadPassenger;
                case "isValid":
                    return horseValid;
                case "isDead":
                    return horseDead;
                case "isOnGround":
                    return horseOnGround;
                case "remove":
                    horseValid = false;
                    passengers.clear();
                    vehicle = null;
                    return null;
                default:
                    return defaultValue(method.getReturnType());
            }
        }

        private Object inventoryCall(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "getHolder" -> horse;
                case "setSaddle" -> {
                    saddle = (ItemStack) args[0];
                    yield null;
                }
                case "getSaddle" -> saddle;
                case "setArmor" -> {
                    armor = (ItemStack) args[0];
                    yield null;
                }
                case "getArmor" -> armor;
                default -> defaultValue(method.getReturnType());
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
                case "toString" -> "SteedFixture<" + proxy.getClass().getInterfaces()[0].getSimpleName() + ">";
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
}
