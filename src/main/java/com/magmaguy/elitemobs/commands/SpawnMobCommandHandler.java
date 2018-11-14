/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.events.mobs.*;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.mobpowers.ElitePower;
import com.magmaguy.elitemobs.mobpowers.defensivepowers.*;
import com.magmaguy.elitemobs.mobpowers.majorpowers.*;
import com.magmaguy.elitemobs.mobpowers.miscellaneouspowers.BonusLoot;
import com.magmaguy.elitemobs.mobpowers.miscellaneouspowers.MovementSpeed;
import com.magmaguy.elitemobs.mobpowers.miscellaneouspowers.Taunt;
import com.magmaguy.elitemobs.mobpowers.offensivepowers.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;

import static com.magmaguy.elitemobs.EliteMobs.validWorldList;

/**
 * Created by MagmaGuy on 01/05/2017.
 */
public class SpawnMobCommandHandler {

    public static void spawnMob(CommandSender commandSender, String[] args) {

        Location location = getLocation(commandSender, args);
        if (location == null) return;

        EntityType entityType = getEntityType(commandSender, args);
        if (entityType == null) return;

        Integer mobLevel = getLevel(commandSender, args);
        if (mobLevel == null) return;

        HashSet<ElitePower> mobPowers = getPowers(commandSender, args);

        LivingEntity eliteMob = (LivingEntity) location.getWorld().spawnEntity(location, entityType);
        EliteMobEntity eliteMobEntity = new EliteMobEntity(eliteMob, mobLevel, mobPowers);

    }

    private static Location getLocation(CommandSender commandSender, String args[]) {

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


    private static EntityType getEntityType(CommandSender commandSender, String args[]) {

        String entityInput = "";

        if (commandSender instanceof Player) {

            try {

                entityInput = args[1].toLowerCase();

            } catch (Exception e) {

                commandSender.sendMessage("[EliteMobs] Error trying to obtain mob type.");
                String validEntities = "";
                for (EntityType entities : EliteMobProperties.getValidMobTypes())
                    validEntities += entities + ", ";
                commandSender.sendMessage("[EliteMobs] Valid mob types:" + validEntities);

            }


        } else if (commandSender instanceof ConsoleCommandSender || commandSender instanceof BlockCommandSender) {


            try {

                entityInput = args[5].toLowerCase();

            } catch (Exception e) {

                commandSender.sendMessage("[EliteMobs] Error trying to obtain mob type.");
                String validEntities = "";
                for (EntityType entities : EliteMobProperties.getValidMobTypes())
                    validEntities += entities + ", ";
                commandSender.sendMessage("[EliteMobs] Valid mob types:" + validEntities);

            }

        }

        try {
            return EntityType.valueOf(entityInput.toUpperCase());
        } catch (Exception ex) {
            commandSender.sendMessage("[EliteMobs] Could not spawn mob type " + entityInput +
                    " If this is incorrect, please contact the dev.");
            return null;
        }

    }

    private static Integer getLevel(CommandSender commandSender, String args[]) {

        int mobLevel = 0;

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

    private static HashSet<ElitePower> getPowers(CommandSender commandSender, String args[]) {

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
                    case "zombieteamrocket":
                        elitePowers.add(new ZombieTeamRocket());
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
                    default:
                        commandSender.sendMessage(string + " is not a valid power.");
                        commandSender.sendMessage("Valid powers: attackarrow, attackblinding, attackconfusing," +
                                " attackfire, attackfireball, attackfreeze,attackgravity, attackpoison, attackpush, " +
                                "attackweakness, attackweb, attackwither, bonusloot, invisibility, invulnerabilityarrow," +
                                "invulnerabilityfalldamage, invulnerabilityfire, invulnerabilityknockback, movementspeed," +
                                "taunt, zombiefriends, zombienecronomicon, zombieteamrocket, zombieteamrocket, zombieparents," +
                                "zombiebloat, skeletontrackingarrow, skeletonpillar");
                        break;

                }

            }

        }

        return elitePowers;

    }

    public static void spawnBossMob(Player player, String[] args) {

        Location cursorLocation = player.getTargetBlock(null, 5).getLocation().add(new Vector(0.5, 2, 0.5));

        if (args[1].equalsIgnoreCase("treasuregoblin")) {

            Zombie zombie = (Zombie) cursorLocation.getWorld().spawnEntity(cursorLocation, EntityType.ZOMBIE);
            TreasureGoblin.createGoblin(zombie);
            player.sendMessage("Spawned treasure goblin");

        }

        if (args[1].equalsIgnoreCase("zombieking")) {

            Zombie zombie = (Zombie) cursorLocation.getWorld().spawnEntity(cursorLocation, EntityType.ZOMBIE);
            ZombieKing.spawnZombieKing(zombie);
            zombie.remove();
            player.sendMessage("Spawned zombie king");

        }

        if (args[1].equalsIgnoreCase("kraken")) {

            Squid squid = (Squid) cursorLocation.getWorld().spawnEntity(cursorLocation, EntityType.SQUID);
            Kraken.spawnKraken(squid.getLocation());
            squid.remove();
            player.sendMessage("Spawned Kraken");

        }

        if (args[1].equalsIgnoreCase("balrog")) {

            Silverfish balrog = (Silverfish) cursorLocation.getWorld().spawnEntity(cursorLocation, EntityType.SILVERFISH);
            Balrog.spawnBalrog(balrog.getLocation());
            balrog.remove();
            player.sendMessage("Spawned Balrog");

        }

        if (args[1].equalsIgnoreCase("fae")) {

            Vex fae = (Vex) cursorLocation.getWorld().spawnEntity(cursorLocation, EntityType.VEX);
            Fae.spawnFae(fae.getLocation());
            fae.remove();
            player.sendMessage("Spawned Fae");

        }

    }

}
