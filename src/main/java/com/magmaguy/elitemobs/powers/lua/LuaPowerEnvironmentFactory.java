package com.magmaguy.elitemobs.powers.lua;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;

public final class LuaPowerEnvironmentFactory {

    private LuaPowerEnvironmentFactory() {
    }

    public static Globals createGlobals() {
        Globals globals = JsePlatform.standardGlobals();
        globals.set("debug", LuaValue.NIL);
        globals.set("dofile", LuaValue.NIL);
        globals.set("io", LuaValue.NIL);
        globals.set("load", LuaValue.NIL);
        globals.set("loadfile", LuaValue.NIL);
        globals.set("luajava", LuaValue.NIL);
        globals.set("module", LuaValue.NIL);
        globals.set("os", LuaValue.NIL);
        globals.set("package", LuaValue.NIL);
        globals.set("require", LuaValue.NIL);

        LuaTable em = new LuaTable();
        em.set("create_location", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                LuaTable table = new LuaTable();
                table.set("x", args.checkdouble(1));
                table.set("y", args.checkdouble(2));
                table.set("z", args.checkdouble(3));
                if (args.narg() >= 4 && args.arg(4).isstring()) {
                    table.set("world", args.arg(4));
                }
                if (args.narg() >= 5 && args.arg(5).isnumber()) {
                    table.set("yaw", args.arg(5));
                }
                if (args.narg() >= 6 && args.arg(6).isnumber()) {
                    table.set("pitch", args.arg(6));
                }
                return table;
            }
        });
        em.set("location", em.get("create_location"));
        em.set("create_vector", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                LuaTable table = new LuaTable();
                table.set("x", args.checkdouble(1));
                table.set("y", args.checkdouble(2));
                table.set("z", args.checkdouble(3));
                return table;
            }
        });
        em.set("vector", em.get("create_vector"));

        LuaTable zone = new LuaTable();
        zone.set("create_sphere_zone", zoneBuilder("sphere", "radius"));
        zone.set("create_dome_zone", zoneBuilder("dome", "radius"));
        zone.set("create_cylinder_zone", zoneBuilder("cylinder", "radius", "height"));
        zone.set("create_cuboid_zone", zoneBuilder("cuboid", "x", "y", "z"));
        zone.set("create_cone_zone", zoneBuilder("cone", "length", "radius"));
        zone.set("create_static_ray_zone", zoneBuilder("static_ray", "length", "thickness"));
        zone.set("create_rotating_ray_zone", zoneBuilder("rotating_ray", "length", "point_radius", "animation_duration"));
        zone.set("create_translating_ray_zone", zoneBuilder("translating_ray", "length", "point_radius", "animation_duration"));
        zone.set("sphere", zone.get("create_sphere_zone"));
        zone.set("dome", zone.get("create_dome_zone"));
        zone.set("cylinder", zone.get("create_cylinder_zone"));
        zone.set("cuboid", zone.get("create_cuboid_zone"));
        zone.set("cone", zone.get("create_cone_zone"));
        zone.set("static_ray", zone.get("create_static_ray_zone"));
        zone.set("rotating_ray", zone.get("create_rotating_ray_zone"));
        zone.set("translating_ray", zone.get("create_translating_ray_zone"));
        em.set("zone", zone);

        globals.set("em", em);
        return globals;
    }

    private static LuaValue zoneBuilder(String kind, String... numericFields) {
        return new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                LuaTable table = new LuaTable();
                table.set("kind", kind);
                for (int index = 0; index < numericFields.length; index++) {
                    table.set(numericFields[index], args.checkdouble(index + 1));
                }
                return table;
            }
        };
    }
}
