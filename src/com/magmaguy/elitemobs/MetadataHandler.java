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
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by MagmaGuy on 26/04/2017.
 */
public class MetadataHandler {

    //if you're wondering why I thought this was needed, it's because I got tired of finding and replacing
    //individual metadata strings and stuff breaking due to it

    //plugin name
    public final static String ELITE_MOBS = "EliteMobs";
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
    public final static String FORBIDDEN_MD = "Forbidden";
    public final static String CUSTOM_POWERS_MD = "Custom";
    //Major powers
    public final static String ZOMBIE_FRIENDS_MD = "ZombieFriends";
    public final static String ZOMBIE_NECRONOMICON_MD = "ZombieNecronomicon";
    public final static String ZOMBIE_TEAM_ROCKET_MD = "ZombieTeamRocket";
    public final static String ZOMBIE_PARENTS_MD= "ZombieParents";
    //Major powers human format
    public final static String ZOMBIE_FRIENDS_H = "ZombieFriends";
    public final static String ZOMBIE_NECRONOMICON_H = "ZombieNecronomicon";
    public final static String ZOMBIE_TEAM_ROCKET_H = "ZombieTeamRocket";
    public final static String ZOMBIE_PARENTS_H= "ZombieParents";
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
    public final static String FROZEN_COOLDOWN = "FrozenCooldown";
    public final static String ZOMBIE_FRIENDS_ACTIVATED = "ZombieFriendsActivated";
    public final static String TEAM_ROCKET_MEMBER= "TeamRocketMember";
    public final static String TEAM_ROCKET_ACTIVATED = "TeamRocketActivated";
    public final static String ZOMBIE_PARENTS_ACTIVATED = "ZombieParentsActivated";
    public final static String ZOMBIE_CHANTING = "ZombieChanting";
    public final static String SHOOTING_ARROWS = "ShootingArrows";
    public final static String SHOOTING_FIREBALLS = "ShootingFireballs";

    //third party compatibility
    public final static String BETTERDROPS_COMPATIBILITY_MD = "betterdrops_ignore";


    Plugin plugin = Bukkit.getPluginManager().getPlugin(ELITE_MOBS);

    public static List<String> metadataList() {

        List<String> metadataList = new ArrayList<>();

        metadataList.add(ELITE_MOB_MD);
        metadataList.add(PASSIVE_ELITE_MOB_MD);
        metadataList.add(NATURAL_MOB_MD);
        metadataList.add(MAJOR_POWER_AMOUNT_MD);
        metadataList.add(MINOR_POWER_AMOUNT_MD);
        metadataList.add(MAJOR_VISUAL_EFFECT_MD);
        metadataList.add(VISUAL_EFFECT_MD);
        metadataList.add(CUSTOM_NAME);
        metadataList.add(CUSTOM_ARMOR);
        metadataList.add(CUSTOM_HEALTH);
        metadataList.add(TAUNT_NAME);
        metadataList.add(FORBIDDEN_MD);

        metadataList.addAll(majorPowerList());

        metadataList.addAll(minorPowerList());

        metadataList.add(FROZEN);
        metadataList.add(FROZEN_COOLDOWN);
        metadataList.add(ZOMBIE_FRIENDS_ACTIVATED);
        metadataList.add(TEAM_ROCKET_ACTIVATED);
        metadataList.add(TEAM_ROCKET_MEMBER);
        metadataList.add(ZOMBIE_PARENTS_ACTIVATED);
        metadataList.add(ZOMBIE_CHANTING);
        metadataList.add(SHOOTING_ARROWS);
        metadataList.add(SHOOTING_FIREBALLS);

        return metadataList;

    }

    public static List<String> majorPowerList(){

        List<String> metadataList = new ArrayList<>();

        metadataList.add(ZOMBIE_FRIENDS_MD);
        metadataList.add(ZOMBIE_NECRONOMICON_MD);
        metadataList.add(ZOMBIE_TEAM_ROCKET_MD);
        metadataList.add(ZOMBIE_PARENTS_MD);

        return metadataList;

    }

    public static List<String> minorPowerList(){

        List<String> metadataList = new ArrayList<>();

        metadataList.add(ATTACK_ARROW_MD);
        metadataList.add(ATTACK_BLINDING_MD);
        metadataList.add(ATTACK_CONFUSING_MD);
        metadataList.add(ATTACK_FIRE_MD);
        metadataList.add(ATTACK_FIREBALL_MD);
        metadataList.add(ATTACK_FREEZE_MD);
        metadataList.add(ATTACK_GRAVITY_MD);
        metadataList.add(ATTACK_POISON_MD);
        metadataList.add(ATTACK_PUSH_MD);
        metadataList.add(ATTACK_WEAKNESS_MD);
        metadataList.add(ATTACK_WEB_MD);
        metadataList.add(ATTACK_WITHER_MD);
        metadataList.add(BONUS_LOOT_MD);
        metadataList.add(DOUBLE_DAMAGE_MD);
        metadataList.add(DOUBLE_HEALTH_MD);
        metadataList.add(INVULNERABILITY_ARROW_MD);
        metadataList.add(INVULNERABILITY_FALL_DAMAGE_MD);
        metadataList.add(INVULNERABILITY_FIRE_MD);
        metadataList.add(INVULNERABILITY_KNOCKBACK_MD);
        metadataList.add(MOVEMENT_SPEED_MD);
        metadataList.add(INVISIBILITY_MD);
        metadataList.add(TAUNT_MD);

        return metadataList;

    }

    public static List<String> allPowersList(){

        List metadataList = Stream.of(minorPowerList(), majorPowerList()).flatMap(List::stream).collect(Collectors.toList());

        return metadataList;

    }

    public static List<String> powerListHumanFormat() {

        List<String> metadataList = new ArrayList<>();

        metadataList.add(ATTACK_ARROW_H);
        metadataList.add(ATTACK_BLINDING_H);
        metadataList.add(ATTACK_CONFUSING_H);
        metadataList.add(ATTACK_FIRE_H);
        metadataList.add(ATTACK_FIREBALL_H);
        metadataList.add(ATTACK_FREEZE_H);
        metadataList.add(ATTACK_GRAVITY_H);
        metadataList.add(ATTACK_POISON_H);
        metadataList.add(ATTACK_PUSH_H);
        metadataList.add(ATTACK_WEAKNESS_H);
        metadataList.add(ATTACK_WEB_H);
        metadataList.add(ATTACK_WITHER_H);
        metadataList.add(BONUS_LOOT_H);
        metadataList.add(DOUBLE_DAMAGE_H);
        metadataList.add(DOUBLE_HEALTH_H);
        metadataList.add(INVULNERABILITY_ARROW_H);
        metadataList.add(INVULNERABILITY_FALL_DAMAGE_H);
        metadataList.add(INVULNERABILITY_FIRE_H);
        metadataList.add(INVULNERABILITY_KNOCKBACK_H);
        metadataList.add(MOVEMENT_SPEED_H);
        metadataList.add(INVISIBILITY_H);
        metadataList.add(TAUNT_H);

        metadataList.add(ZOMBIE_PARENTS_H);
        metadataList.add(ZOMBIE_NECRONOMICON_H);
        metadataList.add(ZOMBIE_TEAM_ROCKET_H);
        metadataList.add(ZOMBIE_PARENTS_H);

        return metadataList;

    }

    public String machineToHumanTranslator (String metadata) {

        switch (metadata) {

            case ZOMBIE_FRIENDS_MD:
                return ZOMBIE_FRIENDS_H;
            case ZOMBIE_NECRONOMICON_MD:
                return ZOMBIE_NECRONOMICON_H;
            case ZOMBIE_TEAM_ROCKET_MD:
                return ZOMBIE_TEAM_ROCKET_H;
            case ZOMBIE_PARENTS_MD:
                return ZOMBIE_PARENTS_H;

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

    public void flushMetadata(Entity entity) {

        for (String string : metadataList()) {

            if (entity.hasMetadata(string)) {

                if (!(entity instanceof IronGolem) && entity.hasMetadata(VISUAL_EFFECT_MD) ||
                        !(entity instanceof IronGolem) && entity.hasMetadata(MAJOR_VISUAL_EFFECT_MD) ) {

                    entity.remove();

                }

                entity.removeMetadata(string, plugin);

            }

            if (!(entity instanceof IronGolem) && ValidAgressiveMobFilter.ValidAgressiveMobFilter(entity)) {

                entity.remove();

            }

            if (entity instanceof Player) {


                entity.removeMetadata(string, plugin);

            }

        }

    }

}
