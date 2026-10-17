package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.easyminecraftgoals.thirdparty.BedrockChecker;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.shaded.luaj.vm2.LuaTable;
import com.magmaguy.shaded.luaj.vm2.LuaValue;
import com.magmaguy.shaded.luaj.vm2.Varargs;
import com.magmaguy.shaded.luaj.vm2.lib.VarArgFunction;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.List;
import java.util.Locale;

/**
 * A script-owned item or block display, centered on its location. Bedrock clients cannot render display
 * entities, so while Bedrock players are in the world they see an armor stand wearing the same item instead.
 * Displays are never saved with the chunk; the owning script or its power's cleanup removes them.
 */
final class LuaDisplayVisual {
    // A small armor stand renders a worn block at about 0.31 scale, centered this far above its feet.
    private static final double SMALL_HEAD_OFFSET = 0.82, LARGE_HEAD_OFFSET = 1.64, SMALL_STAND_MAXIMUM_SCALE = 0.45;
    private final Display display;
    private final LuaPowerSupport support;
    private ArmorStand bedrockStand;
    private double standOffset;
    private Vector3f scale;

    private LuaDisplayVisual(Display display, LuaPowerSupport support, Vector3f scale) {
        this.display = display;
        this.support = support;
        this.scale = scale;
    }

    /** Null when the options name no valid item or block. */
    static LuaDisplayVisual spawn(Location location, LuaTable options, LuaPowerSupport support) {
        Vector3f scale = scale(options.get("scale"), support);
        if (scale == null) return null;
        String itemName = options.get("item").optjstring(null);
        String blockName = options.get("block").optjstring(null);
        if ((itemName == null) == (blockName == null)) return null;
        Display display;
        ItemStack worn;
        if (itemName != null) {
            Material material = support.parseMaterial(itemName);
            if (material == null || !material.isItem() || material.isAir()) return null;
            ItemStack shown = new ItemStack(material);
            worn = shown;
            display = location.getWorld().spawn(location, ItemDisplay.class, spawned -> {
                spawned.setItemStack(shown);
                spawned.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.NONE);
                configure(spawned, options, scale);
            });
        } else {
            BlockData data = blockData(blockName, support);
            if (data == null) return null;
            worn = data.getMaterial().isItem() ? new ItemStack(data.getMaterial()) : null;
            display = location.getWorld().spawn(location, BlockDisplay.class, spawned -> {
                spawned.setBlock(data);
                configure(spawned, options, scale);
            });
        }
        LuaDisplayVisual visual = new LuaDisplayVisual(display, support, scale);
        if (worn != null && options.get("bedrock_fallback").optboolean(true)) visual.addBedrockStand(location, worn);
        return visual;
    }

    private static void configure(Display display, LuaTable options, Vector3f scale) {
        display.setPersistent(false);
        display.setTransformation(transformation(display, scale));
        display.setTeleportDuration(Math.max(0, Math.min(59, options.get("teleport_duration").optint(0))));
        String billboard = options.get("billboard").optjstring(null);
        if (billboard != null) {
            try { display.setBillboard(Display.Billboard.valueOf(billboard.toUpperCase(Locale.ROOT))); }
            catch (IllegalArgumentException unknown) { /* FIXED stays; an unknown name is not fatal to the power */ }
        }
        if (options.get("glowing").optboolean(false)) display.setGlowing(true);
        if (!options.get("brightness").isnil()) {
            int light = Math.max(0, Math.min(15, options.get("brightness").checkint()));
            display.setBrightness(new Display.Brightness(light, light));
        }
    }

    /** Block displays draw from their corner; shift them so every display is centered on its location. */
    private static Transformation transformation(Display display, Vector3f scale) {
        Vector3f translation = display instanceof BlockDisplay ? new Vector3f(scale).mul(-0.5f) : new Vector3f();
        return new Transformation(translation, new AxisAngle4f(), scale, new AxisAngle4f());
    }

    private void addBedrockStand(Location location, ItemStack worn) {
        List<Player> bedrockViewers = location.getWorld().getPlayers().stream().filter(BedrockChecker::isBedrock).toList();
        if (bedrockViewers.isEmpty()) return;
        boolean small = Math.max(scale.x, Math.max(scale.y, scale.z)) <= SMALL_STAND_MAXIMUM_SCALE;
        standOffset = small ? SMALL_HEAD_OFFSET : LARGE_HEAD_OFFSET;
        bedrockStand = location.getWorld().spawn(location.clone().subtract(0, standOffset, 0), ArmorStand.class, stand -> {
            stand.setVisibleByDefault(false);
            stand.setPersistent(false);
            stand.setInvisible(true);
            stand.setMarker(true);
            stand.setSmall(small);
            stand.setBasePlate(false);
            stand.setGravity(false);
            stand.setSilent(true);
            stand.setInvulnerable(true);
            if (stand.getEquipment() != null) stand.getEquipment().setItem(EquipmentSlot.HEAD, worn);
        });
        for (Player viewer : bedrockViewers) {
            viewer.showEntity(MetadataHandler.PLUGIN, bedrockStand);
            viewer.hideEntity(MetadataHandler.PLUGIN, display);
        }
    }

    boolean isValid() { return display.isValid(); }

    void teleport(Location destination) {
        display.teleport(destination);
        if (bedrockStand != null && bedrockStand.isValid())
            bedrockStand.teleport(destination.clone().subtract(0, standOffset, 0));
    }

    void remove() {
        display.remove();
        if (bedrockStand != null) bedrockStand.remove();
    }

    LuaTable table(Runnable onRemove) {
        LuaTable table = new LuaTable();
        table.set("uuid", LuaValue.valueOf(display.getUniqueId().toString()));
        table.set("entity_type", LuaValue.valueOf(display.getType().name()));
        table.set("is_valid", method(table, args -> LuaValue.valueOf(display.isValid())));
        table.set("is_alive", method(table, args -> LuaValue.valueOf(display.isValid())));
        table.set("get_location", method(table, args -> support.toLocationTable(display.getLocation())));
        table.set("teleport_to_location", method(table, args -> {
            Location destination = support.toLocation(args.arg1());
            if (destination != null && display.isValid()) teleport(destination);
            return LuaValue.NIL;
        }));
        table.set("remove", method(table, args -> {
            remove();
            onRemove.run();
            return LuaValue.NIL;
        }));
        table.set("set_scale", method(table, args -> {
            Vector3f changed = scale(args.arg1(), support);
            if (changed != null) {
                scale = changed;
                display.setTransformation(transformation(display, scale));
            }
            return LuaValue.NIL;
        }));
        table.set("set_item", method(table, args -> {
            Material material = support.parseMaterial(args.checkjstring(1));
            if (material == null || !material.isItem() || !(display instanceof ItemDisplay itemDisplay)) return LuaValue.FALSE;
            itemDisplay.setItemStack(new ItemStack(material));
            if (bedrockStand != null && bedrockStand.getEquipment() != null)
                bedrockStand.getEquipment().setItem(EquipmentSlot.HEAD, new ItemStack(material));
            return LuaValue.TRUE;
        }));
        table.set("set_block", method(table, args -> {
            BlockData data = blockData(args.checkjstring(1), support);
            if (data == null || !(display instanceof BlockDisplay blockDisplay)) return LuaValue.FALSE;
            blockDisplay.setBlock(data);
            if (bedrockStand != null && bedrockStand.getEquipment() != null && data.getMaterial().isItem())
                bedrockStand.getEquipment().setItem(EquipmentSlot.HEAD, new ItemStack(data.getMaterial()));
            return LuaValue.TRUE;
        }));
        table.set("set_glowing", method(table, args -> {
            display.setGlowing(args.checkboolean(1));
            return LuaValue.NIL;
        }));
        table.set("set_brightness", method(table, args -> {
            if (args.arg1().isnil()) display.setBrightness(null);
            else {
                int light = Math.max(0, Math.min(15, args.checkint(1)));
                display.setBrightness(new Display.Brightness(light, light));
            }
            return LuaValue.NIL;
        }));
        table.set("set_teleport_duration", method(table, args -> {
            display.setTeleportDuration(Math.max(0, Math.min(59, args.checkint(1))));
            return LuaValue.NIL;
        }));
        return table;
    }

    /** A number scales every axis; a vector table scales each axis. Null for anything else. */
    private static Vector3f scale(LuaValue value, LuaPowerSupport support) {
        if (value.isnil()) return new Vector3f(1f);
        if (value.isnumber()) {
            float uniform = (float) value.todouble();
            return uniform > 0 ? new Vector3f(uniform) : null;
        }
        var vector = support.toVector(value);
        if (vector == null || vector.getX() <= 0 || vector.getY() <= 0 || vector.getZ() <= 0) return null;
        return new Vector3f((float) vector.getX(), (float) vector.getY(), (float) vector.getZ());
    }

    /** Accepts a material name or full block data such as {@code lantern[hanging=true]}. */
    private static BlockData blockData(String name, LuaPowerSupport support) {
        try {
            String key = name.toLowerCase(Locale.ROOT);
            return Bukkit.createBlockData(key.contains(":") || key.contains("[") ? key : "minecraft:" + key);
        } catch (IllegalArgumentException invalid) {
            Material material = support.parseMaterial(name);
            return material == null || !material.isBlock() ? null : material.createBlockData();
        }
    }

    private VarArgFunction method(LuaTable owner, LuaCallback callback) {
        return new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (args.narg() == 0 || !args.arg1().raweq(owner)) return callback.invoke(args);
                LuaValue[] stripped = new LuaValue[args.narg() - 1];
                for (int index = 2; index <= args.narg(); index++) stripped[index - 2] = args.arg(index);
                return callback.invoke(LuaValue.varargsOf(stripped));
            }
        };
    }

    @FunctionalInterface
    private interface LuaCallback {
        Varargs invoke(Varargs args);
    }
}
