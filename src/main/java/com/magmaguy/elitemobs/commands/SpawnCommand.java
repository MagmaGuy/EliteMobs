package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.SuperMobConstructor;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.mobconstructor.mobdata.passivemobs.SuperMobProperties;
import com.magmaguy.elitemobs.powers.ElitePower;
import com.magmaguy.elitemobs.powers.defensivepowers.*;
import com.magmaguy.elitemobs.powers.majorpowers.skeleton.SkeletonPillar;
import com.magmaguy.elitemobs.powers.majorpowers.skeleton.SkeletonTrackingArrow;
import com.magmaguy.elitemobs.powers.majorpowers.zombie.ZombieBloat;
import com.magmaguy.elitemobs.powers.majorpowers.zombie.ZombieFriends;
import com.magmaguy.elitemobs.powers.majorpowers.zombie.ZombieNecronomicon;
import com.magmaguy.elitemobs.powers.majorpowers.zombie.ZombieParents;
import com.magmaguy.elitemobs.powers.miscellaneouspowers.*;
import com.magmaguy.elitemobs.powers.offensivepowers.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.ArrayList;
import java.util.HashSet;

import static com.magmaguy.elitemobs.EliteMobs.validWorldList;

/**
 * Created by MagmaGuy on 01/05/2017.
 */
public class SpawnCommand {

    public static void spawnMob(CommandSender commandSender, String[] args) {

        Location location = getLocation(commandSender, args);
        if (location == null) return;

        String name = "";
        try {
            if (commandSender instanceof Player) {
                name = args[1].toLowerCase();
            } else if (commandSender instanceof ConsoleCommandSender || commandSender instanceof BlockCommandSender) {
                name = args[5].toLowerCase();
            }
        } catch (Exception e) {
            commandSender.sendMessage("[EliteMobs] Error trying to obtain mob type or name.");
            String validEntities = "";
            for (EntityType entities : EliteMobProperties.getValidMobTypes())
                validEntities += entities + ", ";
            commandSender.sendMessage("[EliteMobs] Valid mob types:" + validEntities);
            String validNames = "";
            for (String fileName : CustomBossesConfig.getCustomBosses().keySet())
                validNames += fileName + ", ";
            commandSender.sendMessage("[EliteMobs] Valid custom mob names: " + validNames);
        }

        boolean isCustomBoss = false;
        EntityType entityType = getEntityType(commandSender, name);

        if (entityType == null) {
            String configName = name;
            if (!configName.contains(".yml"))
                configName += ".yml";

            if (!CustomBossesConfig.getCustomBosses().containsKey(configName)) {
                commandSender.sendMessage("[EliteMobs] Error trying to obtain mob type or name.");
                String validEntities = "";
                for (EntityType entities : EliteMobProperties.getValidMobTypes())
                    validEntities += entities + ", ";
                commandSender.sendMessage("[EliteMobs] Valid mob types:" + validEntities);
                String validNames = "";
                for (String fileName : CustomBossesConfig.getCustomBosses().keySet())
                    validNames += fileName + ", ";
                commandSender.sendMessage("[EliteMobs] Valid custom mob names: " + validNames);
                return;
            } else
                isCustomBoss = true;
        }

        if (entityType != null)
            if (SuperMobProperties.isValidSuperMobType(entityType)) {
                spawnSuperMob(entityType, location);
                return;
            }

        Integer mobLevel = getLevel(commandSender, args);
        if (mobLevel == null) return;

        if (isCustomBoss) {
            String configName = name;
            if (!configName.contains(".yml"))
                configName += ".yml";
            CustomBossEntity.constructCustomBoss(configName, location, mobLevel);
            return;
        }

        HashSet<ElitePower> mobPowers = getPowers(commandSender, args);

        new EliteMobEntity(entityType, location, mobLevel, mobPowers, CreatureSpawnEvent.SpawnReason.CUSTOM);

    }

    private static Location getLocation(CommandSender commandSender, String[] args) {

        if (commandSender instanceof Player)
            return getLocation((Player) commandSender);


        if (commandSender instanceof ConsoleCommandSender || commandSender instanceof BlockCommandSender) {

            World world = null;

            try {

                for (World worldIterator : validWorldList)
                    if (worldIterator.getName().equals(args[1]))
                        world = worldIterator;

            } catch (Exception e) {

                commandSender.sendMessage("[EliteMobs] Invalid world name when summoning Elite Mob.");
                commandSender.sendMessage("[EliteMobs] Valid worlds:");
                for (World worldIterator : validWorldList)
                    commandSender.sendMessage(worldIterator.getName());
                commandSender.sendMessage("[EliteMobs] Is your world enabled in the ValidWorlds.yml config file?");
                return null;

            }

            double xCoord, yCoord, zCoord;

            try {

                xCoord = Double.parseDouble(args[2]);
                yCoord = Double.parseDouble(args[3]);
                zCoord = Double.parseDouble(args[4]);

            } catch (Exception e) {

                commandSender.sendMessage("[EliteMobs] Invalid X Y Z coordinates.");
                return null;

            }

            return new Location(world, xCoord, yCoord, zCoord);

        }

        return null;

    }

    private static Location getLocation(Player player) {
        return player.getTargetBlock(null, 30).getLocation().add(0.5, 1, 0.5);
    }


    private static EntityType getEntityType(CommandSender commandSender, String name) {

        try {
            return EntityType.valueOf(name.toUpperCase());
        } catch (Exception ex) {
            return null;
        }

    }

    private static Integer getLevel(CommandSender commandSender, String[] args) {

        int mobLevel = 0;

        if (args.length < 3) {
            commandSender.sendMessage("[EliteMobs] Missing Elite Mob level.");
            commandSender.sendMessage("[EliteMobs] Command syntax: /em spawn [mobType/mobName] [level]");
            return null;
        }

        if (commandSender instanceof Player) {

            try {

                mobLevel = Integer.valueOf(args[2]);

            } catch (Error error) {

                commandSender.sendMessage("Error assigning mob level to Elite Mob. Mob level must be above 0.");

            }


        } else if (commandSender instanceof ConsoleCommandSender || commandSender instanceof BlockCommandSender) {

            try {

                mobLevel = Integer.valueOf(args[6]);

            } catch (Error error) {

                commandSender.sendMessage("Error assigning mob level to Elite Mob. Mob level must be above 0.");

            }

        }

        if (mobLevel < 1)
            mobLevel = 1;

        return mobLevel;

    }

    private static HashSet<ElitePower> getPowers(CommandSender commandSender, String[] args) {

        ArrayList<String> mobPowers = new ArrayList();

        if (commandSender instanceof Player) {

            if (args.length > 3) {

                int index = 0;

                for (String arg : args) {

                    //mob powers start after arg 2
                    if (index > 2)
                        mobPowers.add(arg);

                    index++;

                }

            }

        } else if (commandSender instanceof ConsoleCommandSender || commandSender instanceof BlockCommandSender) {

            if (args.length > 6) {

                int index = 0;

                for (String arg : args) {

                    //mob powers start after arg 6
                    if (index > 6)
                        mobPowers.add(arg);

                    index++;

                }

            }

        }

        return getPowers(mobPowers, commandSender);

    }

    private static HashSet<ElitePower> getPowers(ArrayList<String> mobPowers, CommandSender commandSender) {

        HashSet<ElitePower> elitePowers = new HashSet<>();

        if (mobPowers.size() > 0) {

            for (String string : mobPowers) {

                switch (string.toLowerCase()) {
                    //major powers
                    case "zombiefriends":
                        elitePowers.add(new ZombieFriends());
                        break;
                    case "zombienecronomicon":
                        elitePowers.add(new ZombieNecronomicon());
                        break;
                    case "zombieparents":
                        elitePowers.add(new ZombieParents());
                        break;
                    case "zombiebloat":
                        elitePowers.add(new ZombieBloat());
                        break;
                    case "skeletontrackingarrow":
                        elitePowers.add(new SkeletonTrackingArrow());
                        break;
                    case "skeletonpillar":
                        elitePowers.add(new SkeletonPillar());
                        break;
                    //minor powers
                    case "attackarrow":
                        elitePowers.add(new AttackArrow());
                        break;
                    case "attackblinding":
                        elitePowers.add(new AttackBlinding());
                        break;
                    case "attackconfusing":
                        elitePowers.add(new AttackConfusing());
                        break;
                    case "attackfire":
                        elitePowers.add(new AttackConfusing());
                        break;
                    case "attackfireball":
                        elitePowers.add(new AttackFireball());
                        break;
                    case "attackfreeze":
                        elitePowers.add(new AttackFreeze());
                        break;
                    case "attackgravity":
                        elitePowers.add(new AttackGravity());
                        break;
                    case "attackpoison":
                        elitePowers.add(new AttackPoison());
                        break;
                    case "attackpush":
                        elitePowers.add(new AttackPush());
                        break;
                    case "attackweakness":
                        elitePowers.add(new AttackWeakness());
                        break;
                    case "attackweb":
                        elitePowers.add(new AttackWeb());
                        break;
                    case "attackwither":
                        elitePowers.add(new AttackWither());
                        break;
                    case "bonusloot":
                        elitePowers.add(new BonusLoot());
                        break;
                    case "invisibility":
                        elitePowers.add(new Invisibility());
                        break;
                    case "invulnerabilityarrow":
                        elitePowers.add(new InvulnerabilityArrow());
                        break;
                    case "invulnerabilityfalldamage":
                        elitePowers.add(new InvulnerabilityFallDamage());
                        break;
                    case "invulnerabilityfire":
                        elitePowers.add(new InvulnerabilityFire());
                        break;
                    case "invulnerabilityknockback":
                        elitePowers.add(new InvulnerabilityKnockback());
                        break;
                    case "movementspeed":
                        elitePowers.add(new MovementSpeed());
                        break;
                    case "taunt":
                        elitePowers.add(new Taunt());
                        break;
                    case "corpse":
                        elitePowers.add(new Corpse());
                        break;
                    case "moonwalk":
                        elitePowers.add(new MoonWalk());
                        break;
                    case "implosion":
                        elitePowers.add(new Implosion());
                        break;
                    case "attackvacuum":
                        elitePowers.add(new AttackVacuum());
                        break;
                    default:
                        commandSender.sendMessage(string + " is not a valid power.");
                        commandSender.sendMessage("Valid powers: attackarrow, attackblinding, attackconfusing," +
                                " attackfire, attackfireball, attackfreeze,attackgravity, attackpoison, attackpush, " +
                                "attackweakness, attackweb, attackwither, bonusloot, invisibility, invulnerabilityarrow," +
                                "invulnerabilityfalldamage, invulnerabilityfire, invulnerabilityknockback, movementspeed," +
                                "taunt, zombiefriends, zombienecronomicon, zombieteamrocket, zombieteamrocket, zombieparents," +
                                "zombiebloat, skeletontrackingarrow, skeletonpillar, corpse, moonwalk, implosion");
                        break;

                }

            }

        }

        return elitePowers;

    }

    private static void spawnSuperMob(EntityType entityType, Location location) {
        SuperMobConstructor.constructSuperMob((LivingEntity) location.getWorld().spawnEntity(location, entityType));
    }

}
