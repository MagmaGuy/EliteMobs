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

package com.magmaguy.elitemobs;

import com.magmaguy.elitemobs.mobscanner.ValidAgressiveMobFilter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by MagmaGuy on 26/04/2017.
 */
public class MetadataHandler implements Listener {

    //if you're wondering why I thought this was needed, it's because I got tired of finding and replacing
    //individual metadata strings and stuff breaking due to it

    //plugin name
    public final static String ELITE_MOBS = "EliteMobs";

    //plugin getter
    public final static Plugin PLUGIN = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS);

    //plugin metadata
    public final static String ELITE_MOB_MD = "EliteMob";
    public final static String PASSIVE_ELITE_MOB_MD = "PassiveEliteMob";
    public final static String NATURAL_MOB_MD = "NaturalMob";
    public final static String MAJOR_POWER_AMOUNT_MD = "MajorPowerAmount";
    public final static String MINOR_POWER_AMOUNT_MD = "MinorPowerAmount";
    public final static String MAJOR_VISUAL_EFFECT_MD = "MajorVisualEffect";
    public final static String VISUAL_EFFECT_MD = "VisualEffect";
    public final static String CUSTOM_NAME = "CustomName";
    public final static String CUSTOM_ARMOR = "CustomArmor";
    public final static String CUSTOM_HEALTH = "CustomHealth";
    public final static String TAUNT_NAME = "Taunt_Name";
    public final static String CUSTOM_STACK = "Forbidden";
    public final static String CUSTOM_POWERS_MD = "Custom";
    public final static String EVENT_CREATURE = "EventCreature";

    //Major powers
    public final static String ZOMBIE_FRIENDS_MD = "ZombieFriends";
    public final static String ZOMBIE_NECRONOMICON_MD = "ZombieNecronomicon";
    public final static String ZOMBIE_TEAM_ROCKET_MD = "ZombieTeamRocket";
    public final static String ZOMBIE_PARENTS_MD = "ZombieParents";
    public final static String ZOMBIE_BLOAT_MD = "ZombieBloat";

    //Major powers human format
    public final static String ZOMBIE_FRIENDS_H = "ZombieFriends";
    public final static String ZOMBIE_NECRONOMICON_H = "ZombieNecronomicon";
    public final static String ZOMBIE_TEAM_ROCKET_H = "ZombieTeamRocket";
    public final static String ZOMBIE_PARENTS_H = "ZombieParents";
    public final static String ZOMBIE_BLOAT_H = "ZombieBloat";

    //Minor powers
    public final static String ATTACK_ARROW_MD = "AttackArrow";
    public final static String ATTACK_BLINDING_MD = "AttackBlinding";
    public final static String ATTACK_CONFUSING_MD = "AttackConfusing";
    public final static String ATTACK_FIRE_MD = "AttackFire";
    public final static String ATTACK_FIREBALL_MD = "AttackFireball";
    public final static String ATTACK_FREEZE_MD = "AttackFreeze";
    public final static String ATTACK_GRAVITY_MD = "AttackGravity";
    public final static String ATTACK_POISON_MD = "AttackPoison";
    public final static String ATTACK_PUSH_MD = "AttackPush";
    public final static String ATTACK_WEAKNESS_MD = "AttackWeakness";
    public final static String ATTACK_WEB_MD = "AttackWeb";
    public final static String ATTACK_WITHER_MD = "AttackWither";
    public final static String BONUS_LOOT_MD = "BonusLoot";
    public final static String DOUBLE_DAMAGE_MD = "DoubleDamage";
    public final static String DOUBLE_HEALTH_MD = "DoubleHealth";
    public final static String INVULNERABILITY_ARROW_MD = "InvulnerabilityArrow";
    public final static String INVULNERABILITY_FALL_DAMAGE_MD = "InvulnerabilityFallDamage";
    public final static String INVULNERABILITY_FIRE_MD = "InvulnerabilityFire";
    public final static String INVULNERABILITY_KNOCKBACK_MD = "InvulnerabilityKnockback";
    public final static String MOVEMENT_SPEED_MD = "MovementSpeed";
    public final static String INVISIBILITY_MD = "Invisibility";
    public final static String TAUNT_MD = "Taunt";

    //Minor powers human format
    public final static String ATTACK_ARROW_H = "Archer";
    public final static String ATTACK_BLINDING_H = "Blindness";
    public final static String ATTACK_CONFUSING_H = "Confusion";
    public final static String ATTACK_FIRE_H = "Pyromancer";
    public final static String ATTACK_FIREBALL_H = "Fireball";
    public final static String ATTACK_FREEZE_H = "Cryomancer";
    public final static String ATTACK_GRAVITY_H = "Levitation";
    public final static String ATTACK_POISON_H = "Poisonous";
    public final static String ATTACK_PUSH_H = "Knockback";
    public final static String ATTACK_WEAKNESS_H = "Exhausting";
    public final static String ATTACK_WEB_H = "Webbing";
    public final static String ATTACK_WITHER_H = "Withering";
    public final static String BONUS_LOOT_H = "Treasure";
    public final static String DOUBLE_DAMAGE_H = "Berserker";
    public final static String DOUBLE_HEALTH_H = "Tank";
    public final static String INVULNERABILITY_ARROW_H = "Arrowproof";
    public final static String INVULNERABILITY_FALL_DAMAGE_H = "Light";
    public final static String INVULNERABILITY_FIRE_H = "Fireproof";
    public final static String INVULNERABILITY_KNOCKBACK_H = "Heavy";
    public final static String MOVEMENT_SPEED_H = "Fast";
    public final static String INVISIBILITY_H = "Invisible";
    public final static String TAUNT_H = "Taunt";


    //powerEffects
    public final static String FROZEN = "Frozen";
    public final static String ZOMBIE_FRIENDS_ACTIVATED = "ZombieFriendsActivated";
    public final static String TEAM_ROCKET_MEMBER = "TeamRocketMember";
    public final static String TEAM_ROCKET_ACTIVATED = "TeamRocketActivated";
    public final static String ZOMBIE_PARENTS_ACTIVATED = "ZombieParentsActivated";
    public final static String ZOMBIE_CHANTING = "ZombieChanting";
    public final static String SHOOTING_ARROWS = "ShootingArrows";
    public final static String SHOOTING_FIREBALLS = "ShootingFireballs";

    //powerCooldowns
    public final static String FROZEN_COOLDOWN = "FrozenCooldown";
    public final static String ZOMBIE_BLOAT_COOLDOWN = "ZombieBloatCooldown";
    public final static String ATTACK_CONFUSING_COOLDOWN = "AttackConfusingCooldown";
    public final static String ATTACK_BLINDING_COOLDOWN = "AttackBlindingCooldown";
    public final static String ATTACK_FIRE_COOLDOWN = "AttackFireCooldown";
    public final static String ATTACK_GRAVITY_COOLDOWN = "AttackGravityCooldown";
    public final static String ATTACK_POISON_COOLDOWN = "AttackPoisonCooldown";
    public final static String ATTACK_PUSH_COOLDOWN = "AttackPushCooldown";
    public final static String ATTACK_WITHER_COOLDOWN = "AttackWitherCooldown";
    public final static String TREASURE_GOBLIN_RADIAL_GOLD_EXPLOSION_COOLDOWN = "TreasureGoblinRadialGoldExplosionCooldown";
    public final static String TREASURE_GOBLIN_GOLD_SHOTGUN_COOLDOWN = "TreasureGoblinGoldShotgunCooldown";
    public final static String ZOMBIE_KING_FLAMETHROWER_COOLDOWN = "ZombieKingFlamethrowerCooldown";
    public final static String ZOMBIE_KING_UNHOLY_SMITE_COOLDOWN = "ZombieKingUnholySmiteCooldown";
    public final static String ZOMBIE_KING_SUMMON_MINIONS_COOLDOWN = "ZombieKingSummonMinionsCooldown";

    //displays
    public final static String ARMOR_STAND_DISPLAY = "ArmorStandDisplay";

    //third party compatibility
    public final static String BETTERDROPS_COMPATIBILITY_MD = "betterdrops_ignore";
    public final static String VANISH_NO_PACKET = "vanished";

    //events
    public final static String PERSISTENT_ENTITY = "PersistentEntity";
    public final static String TREASURE_GOBLIN = "TreasureGoblin";
    public final static String THE_RETURNED = "TheReturned";
    public final static String ZOMBIE_KING = "ZombieKing";

    //player metadata
    public final static String KILLED_BY_ELITE_MOB = "KilledByEliteMob";
    public final static String USING_ZOMBIE_KING_AXE = "UsingZombieKingAxe";

    public static List<String> minorPowerList = new ArrayList<>(Arrays.asList(
            ATTACK_ARROW_MD,
            ATTACK_BLINDING_MD,
            ATTACK_CONFUSING_MD,
            ATTACK_FIRE_MD,
            ATTACK_FIREBALL_MD,
            ATTACK_FREEZE_MD,
            ATTACK_GRAVITY_MD,
            ATTACK_POISON_MD,
            ATTACK_PUSH_MD,
            ATTACK_WEAKNESS_MD,
            ATTACK_WEB_MD,
            ATTACK_WITHER_MD,
            BONUS_LOOT_MD,
            DOUBLE_DAMAGE_MD,
            DOUBLE_HEALTH_MD,
            INVULNERABILITY_ARROW_MD,
            INVULNERABILITY_FALL_DAMAGE_MD,
            INVULNERABILITY_FIRE_MD,
            INVULNERABILITY_KNOCKBACK_MD,
            MOVEMENT_SPEED_MD,
            INVISIBILITY_MD,
            TAUNT_MD
    ));
    public static List<String> majorPowerList = new ArrayList<>(Arrays.asList(
            ZOMBIE_FRIENDS_MD,
            ZOMBIE_NECRONOMICON_MD,
            ZOMBIE_TEAM_ROCKET_MD,
            ZOMBIE_PARENTS_MD,
            ZOMBIE_BLOAT_MD
    ));
    public static List<String> forMetadataList = new ArrayList<>(Arrays.asList( //add major and minor power lists
            ELITE_MOB_MD,
            PASSIVE_ELITE_MOB_MD,
            NATURAL_MOB_MD,
            MAJOR_POWER_AMOUNT_MD,
            MINOR_POWER_AMOUNT_MD,
            MAJOR_VISUAL_EFFECT_MD,
            VISUAL_EFFECT_MD,
            CUSTOM_NAME,
            CUSTOM_ARMOR,
            CUSTOM_HEALTH,
            TAUNT_NAME,
            CUSTOM_STACK,
            FROZEN,
            FROZEN_COOLDOWN,
            ZOMBIE_BLOAT_COOLDOWN,
            ATTACK_CONFUSING_COOLDOWN,
            ATTACK_BLINDING_COOLDOWN,
            ATTACK_FIRE_COOLDOWN,
            ATTACK_GRAVITY_COOLDOWN,
            ATTACK_POISON_COOLDOWN,
            ATTACK_WITHER_COOLDOWN,
            TREASURE_GOBLIN_RADIAL_GOLD_EXPLOSION_COOLDOWN,
            TREASURE_GOBLIN_GOLD_SHOTGUN_COOLDOWN,
            ZOMBIE_KING_FLAMETHROWER_COOLDOWN,
            ZOMBIE_KING_UNHOLY_SMITE_COOLDOWN,
            ZOMBIE_KING_SUMMON_MINIONS_COOLDOWN,
            ZOMBIE_FRIENDS_ACTIVATED,
            TEAM_ROCKET_ACTIVATED,
            TEAM_ROCKET_MEMBER,
            ZOMBIE_PARENTS_ACTIVATED,
            ZOMBIE_CHANTING,
            SHOOTING_ARROWS,
            SHOOTING_FIREBALLS,
            ARMOR_STAND_DISPLAY,
            KILLED_BY_ELITE_MOB,
            USING_ZOMBIE_KING_AXE
    ));
    public static List<String> powerListHumanFormat = new ArrayList<>(Arrays.asList( //add major and minor power lists
            //minor powers
            ATTACK_ARROW_H,
            ATTACK_BLINDING_H,
            ATTACK_CONFUSING_H,
            ATTACK_FIRE_H,
            ATTACK_FIREBALL_H,
            ATTACK_FREEZE_H,
            ATTACK_GRAVITY_H,
            ATTACK_POISON_H,
            ATTACK_PUSH_H,
            ATTACK_WEAKNESS_H,
            ATTACK_WEB_H,
            ATTACK_WITHER_H,
            BONUS_LOOT_H,
            DOUBLE_DAMAGE_H,
            DOUBLE_HEALTH_H,
            INVULNERABILITY_ARROW_H,
            INVULNERABILITY_FALL_DAMAGE_H,
            INVULNERABILITY_FIRE_H,
            INVULNERABILITY_KNOCKBACK_H,
            MOVEMENT_SPEED_H,
            INVISIBILITY_H,
            TAUNT_H,
            //major powers
            ZOMBIE_PARENTS_H,
            ZOMBIE_NECRONOMICON_H,
            ZOMBIE_TEAM_ROCKET_H,
            ZOMBIE_PARENTS_H,
            ZOMBIE_BLOAT_H
    ));

    public static List<String> metadataList() {

        List<String> metadataList = new ArrayList<>();

        metadataList.addAll(forMetadataList);

        metadataList.addAll(majorPowerList);

        metadataList.addAll(minorPowerList);


        return metadataList;

    }

    public static List<String> allPowersList() {

        List metadataList = Stream.of(minorPowerList, majorPowerList).flatMap(List::stream).collect(Collectors.toList());

        return metadataList;

    }

    public static String machineToHumanTranslator(String metadata) {

        switch (metadata) {

            case ZOMBIE_FRIENDS_MD:
                return ZOMBIE_FRIENDS_H;
            case ZOMBIE_NECRONOMICON_MD:
                return ZOMBIE_NECRONOMICON_H;
            case ZOMBIE_TEAM_ROCKET_MD:
                return ZOMBIE_TEAM_ROCKET_H;
            case ZOMBIE_PARENTS_MD:
                return ZOMBIE_PARENTS_H;
            case ZOMBIE_BLOAT_MD:
                return ZOMBIE_BLOAT_H;

            case ATTACK_ARROW_MD:
                return ATTACK_ARROW_H;
            case ATTACK_BLINDING_MD:
                return ATTACK_BLINDING_H;
            case ATTACK_CONFUSING_MD:
                return ATTACK_CONFUSING_H;
            case ATTACK_FIRE_MD:
                return ATTACK_FIRE_H;
            case ATTACK_FIREBALL_MD:
                return ATTACK_FIREBALL_H;
            case ATTACK_FREEZE_MD:
                return ATTACK_FREEZE_H;
            case ATTACK_GRAVITY_MD:
                return ATTACK_GRAVITY_H;
            case ATTACK_POISON_MD:
                return ATTACK_POISON_H;
            case ATTACK_PUSH_MD:
                return ATTACK_PUSH_H;
            case ATTACK_WEAKNESS_MD:
                return ATTACK_WEAKNESS_H;
            case ATTACK_WEB_MD:
                return ATTACK_WEB_H;
            case ATTACK_WITHER_MD:
                return ATTACK_WITHER_H;
            case DOUBLE_DAMAGE_MD:
                return DOUBLE_DAMAGE_H;
            case DOUBLE_HEALTH_MD:
                return DOUBLE_HEALTH_H;
            case BONUS_LOOT_MD:
                return BONUS_LOOT_H;
            case INVISIBILITY_MD:
                return INVISIBILITY_H;
            case INVULNERABILITY_ARROW_MD:
                return INVULNERABILITY_ARROW_H;
            case INVULNERABILITY_FALL_DAMAGE_MD:
                return INVULNERABILITY_FALL_DAMAGE_H;
            case INVULNERABILITY_FIRE_MD:
                return INVULNERABILITY_FIRE_H;
            case INVULNERABILITY_KNOCKBACK_MD:
                return INVULNERABILITY_KNOCKBACK_H;
            case MOVEMENT_SPEED_MD:
                return MOVEMENT_SPEED_H;
            case TAUNT_MD:
                return TAUNT_H;

            default:
                Bukkit.getLogger().info("Error: Problem with power name: " + metadata);
                return null;
        }

    }

    public static void flushMetadata(Entity entity) {

        if (entity.hasMetadata(EVENT_CREATURE)) {

            return;

        }

        for (String string : metadataList()) {

            if (entity.hasMetadata(string) && !string.equals(NATURAL_MOB_MD)) {


                if (!(entity instanceof IronGolem) && entity.hasMetadata(VISUAL_EFFECT_MD) ||
                        !(entity instanceof IronGolem) && entity.hasMetadata(MAJOR_VISUAL_EFFECT_MD) ||
                        (entity.hasMetadata(ELITE_MOB_MD) && ValidAgressiveMobFilter.ValidAgressiveMobFilter(entity)) ||
                        entity.hasMetadata(ARMOR_STAND_DISPLAY) && string.equals(ARMOR_STAND_DISPLAY)) {

                    /*
                    Take into account entities spawned by other plugins
                     */
//                    if (!entity.hasMetadata(ELITE_MOB_MD) ||
//                            entity.hasMetadata(ELITE_MOB_MD) && NameHandler.compareCustomAggressiveNameIgnoresCustomName(entity)) {

                        entity.remove();

//                    }


                }

                if (!(entity.hasMetadata(ZOMBIE_KING) || entity.hasMetadata(TREASURE_GOBLIN))) {

                    //todo: add improved flushing for permanent mobs

                } else {

                    entity.removeMetadata(string, PLUGIN);

                }


            }

            if (entity instanceof Player) {


                entity.removeMetadata(string, PLUGIN);

            }

        }

    }

    public void fullMetadataFlush(Entity entity) {

        if (entity.hasMetadata(EVENT_CREATURE)) {
            entity.removeMetadata(EVENT_CREATURE, PLUGIN);
            entity.remove();
        }

        flushMetadata(entity);

    }

    @EventHandler
    public void flushPlayerMetadataOnQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();

        for (String string : metadataList()) {

            if (player.hasMetadata(string)) {

                player.removeMetadata(string, PLUGIN);

            }

        }

    }


    @EventHandler
    public void flushPlayerMetadataOnDeath(PlayerDeathEvent event) {

        Player player = event.getEntity();

        for (String string : metadataList()) {

            if (player.hasMetadata(string)) {

                player.removeMetadata(string, PLUGIN);

            }

        }

    }

}
