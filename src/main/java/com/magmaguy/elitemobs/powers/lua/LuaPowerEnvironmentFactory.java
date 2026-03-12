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
                LuaTable table = createLocationTable(args.checkdouble(1), args.checkdouble(2), args.checkdouble(3));
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

        LuaTable zone = new LuaTable();
        zone.set("create_sphere_zone", zoneBuilder("sphere", "set_center", null, "radius"));
        zone.set("create_dome_zone", zoneBuilder("dome", "set_center", null, "radius"));
        zone.set("create_cylinder_zone", zoneBuilder("cylinder", "set_center", null, "radius", "height"));
        zone.set("create_cuboid_zone", zoneBuilder("cuboid", "set_center", null, "x", "y", "z"));
        zone.set("create_cone_zone", zoneBuilder("cone", "set_origin", "set_destination", "length", "radius"));
        zone.set("create_static_ray_zone", zoneBuilder("static_ray", "set_origin", "set_destination", "length", "thickness"));
        zone.set("create_rotating_ray_zone", zoneBuilder("rotating_ray", "set_origin", "set_destination", "length", "point_radius", "animation_duration"));
        zone.set("create_translating_ray_zone", zoneBuilder("translating_ray", "set_origin", "set_destination", "length", "point_radius", "animation_duration"));
        em.set("zone", zone);

        globals.set("em", em);
        return globals;
    }

    private static LuaValue zoneBuilder(String kind, String originMethod, String destinationMethod, String... numericFields) {
        return new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                LuaTable table = new LuaTable();
                table.set("kind", kind);
                for (int index = 0; index < numericFields.length; index++) {
                    table.set(numericFields[index], args.checkdouble(index + 1));
                }
                table.set(originMethod, tableMethod(table, methodArgs -> {
                    table.set("origin", methodArgs.arg1());
                    return table;
                }));
                if (destinationMethod != null) {
                    table.set(destinationMethod, tableMethod(table, methodArgs -> {
                        table.set("destination", methodArgs.arg1());
                        return table;
                    }));
                }
                return table;
            }
        };
    }

    private static LuaTable createLocationTable(double x, double y, double z) {
        LuaTable table = new LuaTable();
        table.set("x", x);
        table.set("y", y);
        table.set("z", z);
        table.set("add", tableMethod(table, args -> {
            table.set("x", LuaValue.valueOf(table.get("x").optdouble(0) + args.optdouble(1, 0)));
            table.set("y", LuaValue.valueOf(table.get("y").optdouble(0) + args.optdouble(2, 0)));
            table.set("z", LuaValue.valueOf(table.get("z").optdouble(0) + args.optdouble(3, 0)));
            return table;
        }));
        return table;
    }

    private static LuaValue tableMethod(LuaTable owner, LuaCallback callback) {
        return new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return callback.invoke(stripMethodSelf(args, owner));
            }
        };
    }

    private static Varargs stripMethodSelf(Varargs args, LuaTable owner) {
        if (args.narg() == 0 || !args.arg1().raweq(owner)) {
            return args;
        }
        LuaValue[] stripped = new LuaValue[Math.max(0, args.narg() - 1)];
        for (int index = 2; index <= args.narg(); index++) {
            stripped[index - 2] = args.arg(index);
        }
        return LuaValue.varargsOf(stripped);
    }

    @FunctionalInterface
    private interface LuaCallback {
        Varargs invoke(Varargs args);
    }
}
