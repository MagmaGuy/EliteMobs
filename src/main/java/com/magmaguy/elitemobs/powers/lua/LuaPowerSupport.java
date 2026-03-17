package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.combatsystem.ScaledCombatRewardResolver;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.explosionregen.Explosion;
import com.magmaguy.elitemobs.items.ItemLootShower;
import com.magmaguy.elitemobs.items.LootTables;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.powers.ProjectileDamage;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.powers.scripts.ScriptAction;
import com.magmaguy.elitemobs.utils.GameClock;
import com.magmaguy.elitemobs.utils.shapes.*;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.*;

final class LuaPowerSupport {

    private final LuaPowerDefinition definition;
    private final EliteEntity eliteEntity;

    LuaPowerSupport(LuaPowerDefinition definition, EliteEntity eliteEntity) {
        this.definition = definition;
        this.eliteEntity = eliteEntity;
    }

    LuaTable toLocationTable(Location location) {
        LuaTable table = new LuaTable();
        if (location == null) return table;
        table.set("x", LuaValue.valueOf(location.getX()));
        table.set("y", LuaValue.valueOf(location.getY()));
        table.set("z", LuaValue.valueOf(location.getZ()));
        table.set("yaw", LuaValue.valueOf(location.getYaw()));
        table.set("pitch", LuaValue.valueOf(location.getPitch()));
        table.set("direction", toVectorTable(location.getDirection()));
        if (location.getWorld() != null) {
            table.set("world", LuaValue.valueOf(location.getWorld().getName()));
        }
        table.set("add", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                LuaTable target = args.arg1().istable() ? args.arg1().checktable() : table;
                int offsetIndex = args.arg1().raweq(table) ? 2 : 1;
                target.set("x", LuaValue.valueOf(target.get("x").optdouble(0) + args.optdouble(offsetIndex, 0)));
                target.set("y", LuaValue.valueOf(target.get("y").optdouble(0) + args.optdouble(offsetIndex + 1, 0)));
                target.set("z", LuaValue.valueOf(target.get("z").optdouble(0) + args.optdouble(offsetIndex + 2, 0)));
                return target;
            }
        });
        return table;
    }

    Location toLocation(LuaValue value) {
        if (value == null || value.isnil()) return eliteEntity.getLocation();
        if (!value.istable()) return eliteEntity.getLocation();
        LuaTable table = value.checktable();
        if (table.get("current_location").istable()) {
            table = table.get("current_location").checktable();
        }
        World world = eliteEntity.getLocation() == null ? null : eliteEntity.getLocation().getWorld();
        if (table.get("world").isstring()) {
            world = Bukkit.getWorld(table.get("world").tojstring());
        }
        if (world == null) return null;
        return new Location(
                world,
                table.get("x").optdouble(0),
                table.get("y").optdouble(0),
                table.get("z").optdouble(0),
                (float) table.get("yaw").optdouble(0),
                (float) table.get("pitch").optdouble(0));
    }

    LuaTable toVectorTable(Vector vector) {
        LuaTable table = new LuaTable();
        if (vector == null) {
            return table;
        }
        table.set("x", LuaValue.valueOf(vector.getX()));
        table.set("y", LuaValue.valueOf(vector.getY()));
        table.set("z", LuaValue.valueOf(vector.getZ()));
        return table;
    }

    Vector toVector(LuaValue value) {
        if (value == null || value.isnil() || !value.istable()) {
            return null;
        }
        LuaTable table = value.checktable();
        if (table.get("direction").istable()) {
            table = table.get("direction").checktable();
        }
        return new Vector(
                getIndexedOrNamedDouble(table, 1, "x"),
                getIndexedOrNamedDouble(table, 2, "y"),
                getIndexedOrNamedDouble(table, 3, "z"));
    }

    private double getIndexedOrNamedDouble(LuaTable table, int index, String key) {
        LuaValue named = table.get(key);
        if (named.isnumber()) {
            return named.todouble();
        }
        LuaValue indexed = table.get(index);
        if (indexed.isnumber()) {
            return indexed.todouble();
        }
        return 0;
    }

    float getFloat(Varargs args, int index, float fallback) {
        return args.narg() >= index && args.arg(index).isnumber() ? (float) args.arg(index).todouble() : fallback;
    }

    void playSound(Location location, String soundKey, float volume, float pitch) {
        if (location == null || location.getWorld() == null) return;
        location.getWorld().playSound(location, soundKey, volume, pitch);
    }

    void spawnParticle(Location location, String particleKey, int count) {
        if (location == null || location.getWorld() == null) return;
        try {
            Particle particle = Particle.valueOf(particleKey.toUpperCase(Locale.ROOT));
            location.getWorld().spawnParticle(particle, location, count);
        } catch (Exception exception) {
            Logger.warn("Unknown particle " + particleKey + " in Lua power " + definition.getFileName() + ".");
        }
    }

    void spawnParticle(Location location, LuaValue particleValue, int fallbackCount) {
        if (particleValue.isstring()) {
            spawnParticle(location, particleValue.tojstring(), fallbackCount);
            return;
        }
        if (location == null || location.getWorld() == null || !particleValue.istable()) {
            return;
        }
        LuaTable table = particleValue.checktable();
        String particleKey = table.get("particle").optjstring("");
        if (particleKey.isEmpty()) {
            return;
        }
        try {
            Particle particle = Particle.valueOf(particleKey.toUpperCase(Locale.ROOT));
            int amount = table.get("amount").optint(fallbackCount);
            double x = table.get("x").optdouble(0);
            double y = table.get("y").optdouble(0);
            double z = table.get("z").optdouble(0);
            double speed = table.get("speed").optdouble(0);
            if (particle.equals(Particle.DUST)) {
                location.getWorld().spawnParticle(particle, location, amount, x, y, z, speed,
                        new Particle.DustOptions(
                                Color.fromRGB(
                                        table.get("red").optint(255),
                                        table.get("green").optint(255),
                                        table.get("blue").optint(255)),
                                1));
            } else if (particle.equals(Particle.DUST_COLOR_TRANSITION)) {
                location.getWorld().spawnParticle(particle, location, amount, x, y, z, speed,
                        new Particle.DustTransition(
                                Color.fromRGB(
                                        table.get("red").optint(255),
                                        table.get("green").optint(255),
                                        table.get("blue").optint(255)),
                                Color.fromRGB(
                                        table.get("toRed").optint(table.get("to_red").optint(255)),
                                        table.get("toGreen").optint(table.get("to_green").optint(255)),
                                        table.get("toBlue").optint(table.get("to_blue").optint(255))),
                                1));
            } else {
                location.getWorld().spawnParticle(particle, location, amount, x, y, z, speed);
            }
        } catch (Exception exception) {
            Logger.warn("Unknown particle " + particleKey + " in Lua power " + definition.getFileName() + ".");
        }
    }

    void applyPotionEffect(LivingEntity livingEntity, String effectKey, int duration, int amplifier) {
        if (livingEntity == null || !livingEntity.isValid()) {
            return;
        }
        PotionEffectType potionEffectType = PotionEffectType.getByName(effectKey.toUpperCase(Locale.ROOT));
        if (potionEffectType == null) {
            Logger.warn("Unknown potion effect " + effectKey + " in Lua power " + definition.getFileName() + ".");
            return;
        }
        livingEntity.addPotionEffect(new PotionEffect(potionEffectType, duration, amplifier));
    }

    void placeTemporaryBlock(Location location, String materialKey, int duration, boolean requireAir) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        Material material = parseMaterial(materialKey);
        if (material == null) return;
        Block block = location.getBlock();
        if (requireAir && !block.getType().isAir()) {
            return;
        }
        if (duration > 0) {
            EntityTracker.addTemporaryBlock(block, duration, material);
        } else {
            block.setType(material);
        }
    }

    Material parseMaterial(String materialKey) {
        try {
            return Material.valueOf(materialKey.toUpperCase(Locale.ROOT));
        } catch (Exception exception) {
            Logger.warn("Unknown material " + materialKey + " in Lua power " + definition.getFileName() + ".");
            return null;
        }
    }

    Collection<LivingEntity> filterEntities(World world, String filter) {
        if (world == null) {
            return Collections.emptyList();
        }
        return switch (filter.toLowerCase(Locale.ROOT)) {
            case "player", "players" -> new ArrayList<>(world.getPlayers());
            case "elite", "elites" -> world.getLivingEntities().stream()
                    .filter(livingEntity -> EntityTracker.getEliteMobEntity(livingEntity) != null)
                    .toList();
            case "mob", "mobs" -> world.getLivingEntities().stream()
                    .filter(livingEntity -> !(livingEntity instanceof Player))
                    .toList();
            default -> new ArrayList<>(world.getLivingEntities());
        };
    }

    void applyVectorOptions(Vector vector, LuaTable options) {
        if (options.get("normalize").optboolean(false) && vector.lengthSquared() > 0) {
            vector.normalize();
        }
        vector.multiply(options.get("multiplier").optdouble(1.0));
        Vector offset = toVector(options.get("offset"));
        if (offset != null) {
            vector.add(offset);
        }
    }

    Vector rotateVector(Vector original, double pitchDegrees, double yawDegrees) {
        Vector rotated = original.clone();
        rotated.rotateAroundX(Math.toRadians(pitchDegrees));
        rotated.rotateAroundY(Math.toRadians(yawDegrees));
        return rotated;
    }

    <T extends Enum<T>> T parseEnum(String rawValue, Class<T> enumClass, T fallback) {
        try {
            return Enum.valueOf(enumClass, rawValue.toUpperCase(Locale.ROOT));
        } catch (Exception exception) {
            return fallback;
        }
    }

    void applyInvulnerable(LivingEntity livingEntity, boolean invulnerable, int duration) {
        livingEntity.setInvulnerable(invulnerable);
        if (livingEntity instanceof Player player) {
            if (invulnerable) {
                ScriptAction.getInvulnerablePlayers().add(player.getUniqueId());
            } else {
                ScriptAction.getInvulnerablePlayers().remove(player.getUniqueId());
            }
        }
        if (duration > 0) {
            UUID uuid = livingEntity.getUniqueId();
            GameClock.scheduleLater(duration, () -> {
                if (!livingEntity.isValid()) {
                    return;
                }
                livingEntity.setInvulnerable(!invulnerable);
                if (livingEntity instanceof Player) {
                    if (invulnerable) {
                        ScriptAction.getInvulnerablePlayers().remove(uuid);
                    } else {
                        ScriptAction.getInvulnerablePlayers().add(uuid);
                    }
                }
            });
        }
    }

    void applyTag(LivingEntity livingEntity, String tag, boolean add) {
        EliteEntity targetElite = EntityTracker.getEliteMobEntity(livingEntity);
        if (targetElite != null) {
            if (add) {
                targetElite.addTag(tag);
            } else {
                targetElite.removeTag(tag);
            }
        }
        if (livingEntity instanceof Player player) {
            ElitePlayerInventory inventory = ElitePlayerInventory.getPlayer(player);
            if (inventory != null) {
                if (add) {
                    inventory.addTags(List.of(tag));
                } else {
                    inventory.removeTags(List.of(tag));
                }
            }
        }
    }

    boolean hasTag(LivingEntity livingEntity, String tag) {
        EliteEntity targetElite = EntityTracker.getEliteMobEntity(livingEntity);
        if (targetElite != null && targetElite.hasTag(tag)) {
            return true;
        }
        if (livingEntity instanceof Player player) {
            ElitePlayerInventory inventory = ElitePlayerInventory.getPlayer(player);
            return inventory != null && inventory.hasTag(tag);
        }
        return false;
    }

    Firework spawnFirework(Location location, LuaTable spec) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        Firework firework = location.getWorld().spawn(location, Firework.class);
        firework.setPersistent(false);
        FireworkMeta meta = firework.getFireworkMeta();
        if (spec.get("effects").istable()) {
            LuaTable effects = spec.get("effects").checktable();
            LuaValue key = LuaValue.NIL;
            while (true) {
                Varargs next = effects.next(key);
                key = next.arg1();
                if (key.isnil()) {
                    break;
                }
                if (next.arg(2).istable()) {
                    meta.addEffect(buildFireworkEffect(next.arg(2).checktable()));
                }
            }
        } else {
            meta.addEffect(buildFireworkEffect(spec));
        }
        meta.setPower(spec.get("power").optint(1));
        firework.setFireworkMeta(meta);
        Vector velocity = toVector(spec.get("velocity"));
        if (velocity != null) {
            firework.setVelocity(velocity);
            firework.setShotAtAngle(spec.get("shot_at_angle").optboolean(true));
        }
        return firework;
    }

    ThrownPotion spawnSplashPotion(Location location, LuaTable spec) {
        if (location == null || location.getWorld() == null) {
            return null;
        }

        ItemStack itemStack = new ItemStack(Material.SPLASH_POTION);
        PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
        if (potionMeta == null) {
            return null;
        }

        if (spec.get("effects").istable()) {
            LuaTable effects = spec.get("effects").checktable();
            LuaValue key = LuaValue.NIL;
            while (true) {
                Varargs next = effects.next(key);
                key = next.arg1();
                if (key.isnil()) {
                    break;
                }
                if (!next.arg(2).istable()) {
                    continue;
                }
                LuaTable effectSpec = next.arg(2).checktable();
                String effectKey = effectSpec.get("type").optjstring("").toUpperCase(Locale.ROOT);
                PotionEffectType effectType = PotionEffectType.getByName(effectKey);
                if (effectType == null) {
                    Logger.warn("Unknown potion effect " + effectKey + " in Lua power " + definition.getFileName() + ".");
                    continue;
                }
                potionMeta.addCustomEffect(
                        new PotionEffect(
                                effectType,
                                effectSpec.get("duration").optint(0),
                                effectSpec.get("amplifier").optint(0)),
                        effectSpec.get("overwrite").optboolean(true));
            }
        }

        itemStack.setItemMeta(potionMeta);

        ThrownPotion thrownPotion = location.getWorld().spawn(location, ThrownPotion.class);
        thrownPotion.setItem(itemStack);

        Vector velocity = toVector(spec.get("velocity"));
        if (velocity != null) {
            thrownPotion.setVelocity(velocity);
        }
        return thrownPotion;
    }

    float getBlastResistance(Location location) {
        if (location == null || location.getWorld() == null) {
            return 0f;
        }
        return location.getBlock().getType().getBlastResistance();
    }

    void generateFakeExplosion(List<Location> blockLocations, Location explosionSourceLocation) {
        if (eliteEntity.getLivingEntity() == null || blockLocations == null || blockLocations.isEmpty()) {
            return;
        }
        List<Block> blocks = new ArrayList<>();
        for (Location blockLocation : blockLocations) {
            if (blockLocation == null || blockLocation.getWorld() == null) {
                continue;
            }
            blocks.add(blockLocation.getBlock());
        }
        PowersConfigFields powersConfigFields = PowersConfig.getPower(definition.getFileName());
        Explosion.generateFakeExplosion(blocks, eliteEntity.getLivingEntity(), powersConfigFields, explosionSourceLocation);
    }

    ProjectileDamage.FakeProjectile spawnGoldNuggetProjectile(Location location, Vector velocity) {
        if (location == null || velocity == null) {
            return null;
        }
        return ProjectileDamage.createGoldNuggetProjectile(location, velocity);
    }

    void runGoldNuggetDamage(List<ProjectileDamage.FakeProjectile> projectiles) {
        if (projectiles == null || projectiles.isEmpty()) {
            return;
        }
        ProjectileDamage.doGoldNuggetDamage(projectiles, eliteEntity);
    }

    void generatePlayerLoot(int times) {
        for (int i = 0; i < times; i++) {
            LootTables.generatePlayerLoot(eliteEntity);
        }
    }

    void dropBonusCoins(double coinMultiplier) {
        if (eliteEntity.getUnsyncedLivingEntity() == null) {
            return;
        }
        for (Player player : eliteEntity.getDamagers().keySet()) {
            if (player.hasMetadata("NPC") || !PlayerData.isInMemory(player.getUniqueId())) {
                continue;
            }
            if (eliteEntity.getDamagers().get(player) / eliteEntity.getMaxHealth() <= 0.1) {
                continue;
            }
            int rewardLevel = ScaledCombatRewardResolver.getRewardLevel(eliteEntity, player);
            new ItemLootShower(rewardLevel * coinMultiplier, rewardLevel, eliteEntity.getUnsyncedLivingEntity().getLocation(), player);
        }
    }

    private FireworkEffect buildFireworkEffect(LuaTable spec) {
        String typeName = spec.get("type").optjstring("BALL_LARGE");
        FireworkEffect.Type type = parseEnum(typeName, FireworkEffect.Type.class, FireworkEffect.Type.BALL_LARGE);
        FireworkEffect.Builder builder = FireworkEffect.builder()
                .with(type)
                .flicker(spec.get("flicker").optboolean(true))
                .trail(spec.get("trail").optboolean(true));
        List<Color> colors = parseColors(spec.get("colors"));
        if (!colors.isEmpty()) {
            builder.withColor(colors);
        }
        List<Color> fadeColors = parseColors(spec.get("fade_colors"));
        if (!fadeColors.isEmpty()) {
            builder.withFade(fadeColors);
        }
        return builder.build();
    }

    private List<Color> parseColors(LuaValue colorsValue) {
        List<Color> parsedColors = new ArrayList<>();
        if (!colorsValue.istable()) {
            return parsedColors;
        }
        LuaTable colors = colorsValue.checktable();
        LuaValue key = LuaValue.NIL;
        while (true) {
            Varargs next = colors.next(key);
            key = next.arg1();
            if (key.isnil()) {
                break;
            }
            parsedColors.add(parseColor(next.arg(2)));
        }
        return parsedColors;
    }

    Color parseColor(LuaValue value) {
        if (value.isstring()) {
            return switch (value.tojstring().toUpperCase(Locale.ROOT)) {
                case "WHITE" -> Color.WHITE;
                case "SILVER" -> Color.SILVER;
                case "GRAY" -> Color.GRAY;
                case "BLACK" -> Color.BLACK;
                case "RED" -> Color.RED;
                case "MAROON" -> Color.MAROON;
                case "YELLOW" -> Color.YELLOW;
                case "OLIVE" -> Color.OLIVE;
                case "LIME" -> Color.LIME;
                case "GREEN" -> Color.GREEN;
                case "AQUA" -> Color.AQUA;
                case "TEAL" -> Color.TEAL;
                case "BLUE" -> Color.BLUE;
                case "NAVY" -> Color.NAVY;
                case "FUCHSIA" -> Color.FUCHSIA;
                case "PURPLE" -> Color.PURPLE;
                case "ORANGE" -> Color.ORANGE;
                default -> Color.WHITE;
            };
        }
        if (value.istable()) {
            LuaTable table = value.checktable();
            return Color.fromRGB(table.get("red").optint(255), table.get("green").optint(255), table.get("blue").optint(255));
        }
        return Color.WHITE;
    }

    Entity resolveEntityReference(LuaValue value) {
        if (value == null || value.isnil() || !value.istable()) {
            return null;
        }
        LuaTable table = value.checktable();
        LuaValue uuidValue = table.get("uuid");
        if (!uuidValue.isstring()) {
            return null;
        }
        try {
            return Bukkit.getEntity(UUID.fromString(uuidValue.tojstring()));
        } catch (Exception exception) {
            return null;
        }
    }

    LivingEntity resolveLivingEntityReference(LuaValue value) {
        Entity entity = resolveEntityReference(value);
        return entity instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    Player resolvePlayerReference(LuaValue value) {
        Entity entity = resolveEntityReference(value);
        if (entity instanceof Player player) {
            return player;
        }
        if (value != null && value.istable()) {
            LuaValue uuidValue = value.checktable().get("uuid");
            if (uuidValue.isstring()) {
                try {
                    return Bukkit.getPlayer(UUID.fromString(uuidValue.tojstring()));
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }

    String resolveZoneFilter(LuaValue optionsValue) {
        if (optionsValue != null && optionsValue.istable()) {
            return optionsValue.checktable().get("filter").optjstring("living");
        }
        return "living";
    }

    boolean resolveZoneMode(LuaValue optionsValue) {
        if (optionsValue != null && optionsValue.istable()) {
            return "border".equalsIgnoreCase(optionsValue.checktable().get("mode").optjstring("full"));
        }
        return false;
    }

    String getString(LuaTable table, String... keys) {
        for (String key : keys) {
            LuaValue value = table.get(key);
            if (value.isstring()) {
                return value.tojstring();
            }
        }
        return "";
    }

    double getDouble(LuaTable table, double fallback, String... keys) {
        for (String key : keys) {
            LuaValue value = table.get(key);
            if (value.isnumber()) {
                return value.todouble();
            }
        }
        return fallback;
    }

    int getInt(LuaTable table, int fallback, String... keys) {
        for (String key : keys) {
            LuaValue value = table.get(key);
            if (value.isnumber()) {
                return value.toint();
            }
        }
        return fallback;
    }

    boolean getBoolean(LuaTable table, boolean fallback, String... keys) {
        for (String key : keys) {
            LuaValue value = table.get(key);
            if (value.isboolean()) {
                return value.toboolean();
            }
        }
        return fallback;
    }

    Shape createShape(LuaValue zoneDefinition) {
        if (!zoneDefinition.istable() || eliteEntity.getLocation() == null) return null;
        LuaTable table = zoneDefinition.checktable();
        String kind = getString(table, "kind", "type");
        Location center = resolveLocation(table.get("origin"));
        if (center == null) {
            center = eliteEntity.getLocation();
        }
        Location destination = resolveLocation(table.get("destination"));
        if (destination == null && center != null) {
            destination = center.clone().add(center.getDirection().normalize().multiply(getDouble(table, 0, "length")));
        }
        Location originEnd = resolveLocation(table.get("origin_end"));
        Location destinationEnd = resolveLocation(table.get("destination_end"));
        double radius = getDouble(table, 0, "radius");
        double borderRadius = getDouble(table, 1, "border_radius", "borderRadius");
        double height = getDouble(table, 0, "height");
        double pointRadius = getDouble(table, 0.5, "point_radius", "pointRadius", "thickness");
        double x = getDouble(table, 0, "x");
        double y = getDouble(table, 0, "y");
        double z = getDouble(table, 0, "z");
        double xBorder = getDouble(table, 1, "x_border", "xBorder");
        double yBorder = getDouble(table, 1, "y_border", "yBorder");
        double zBorder = getDouble(table, 1, "z_border", "zBorder");
        double pitchPreRotation = getDouble(table, 0, "pitch_pre_rotation", "pitchPreRotation");
        double yawPreRotation = getDouble(table, 0, "yaw_pre_rotation", "yawPreRotation");
        double pitchRotation = getDouble(table, 0, "pitch_rotation", "pitchRotation");
        double yawRotation = getDouble(table, 0, "yaw_rotation", "yawRotation");
        int animationDuration = getInt(table, 0, "animation_duration", "animationDuration");
        boolean ignoresSolidBlocks = getBoolean(table, true, "ignores_solid_blocks", "ignoresSolidBlocks");
        return switch (kind) {
            case "sphere" -> new Sphere(radius, center, borderRadius);
            case "dome" -> new Dome(radius, center, borderRadius);
            case "cylinder" -> new Cylinder(center, radius, height, borderRadius);
            case "cuboid" -> new Cuboid((float) x, (float) y, (float) z, (float) xBorder, (float) yBorder, (float) zBorder, center);
            case "cone" -> destination == null ? null : new Cone(center.clone(), destination, radius, borderRadius);
            case "static_ray" -> destination == null ? null : new StaticRay(ignoresSolidBlocks, pointRadius, center.clone(), destination);
            case "rotating_ray" -> destination == null ? null : new RotatingRay(
                    ignoresSolidBlocks,
                    pointRadius,
                    center.clone(),
                    destination,
                    pitchPreRotation,
                    yawPreRotation,
                    pitchRotation,
                    yawRotation,
                    animationDuration);
            case "translating_ray" -> destination == null ? null : new TranslatingRay(
                    ignoresSolidBlocks,
                    pointRadius,
                    center.clone(),
                    originEnd,
                    destination,
                    destinationEnd,
                    animationDuration);
            default -> null;
        };
    }

    LuaTable toParticleSpec(Varargs args) {
        LuaTable particle = new LuaTable();
        particle.set("particle", args.arg(2).isstring() ? args.arg(2) : args.arg(1));
        particle.set("amount", LuaValue.valueOf(args.narg() >= 3 ? args.arg(3).optint(1) : 1));
        particle.set("x", LuaValue.valueOf(args.narg() >= 4 ? args.arg(4).optdouble(0) : 0));
        particle.set("y", LuaValue.valueOf(args.narg() >= 5 ? args.arg(5).optdouble(0) : 0));
        particle.set("z", LuaValue.valueOf(args.narg() >= 6 ? args.arg(6).optdouble(0) : 0));
        particle.set("speed", LuaValue.valueOf(args.narg() >= 7 ? args.arg(7).optdouble(0) : 0));
        return particle;
    }

    private Location resolveLocation(LuaValue value) {
        return toLocation(value);
    }
}
