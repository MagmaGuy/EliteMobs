package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.Configuration;

/**
 * Created by MagmaGuy on 01/05/2017.
 */
public class MobPowersConfig {

    public static final String CONFIG_NAME = "MobPowers.yml";
    CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    private Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    private static final String POWERS = "Powers.";
    private static final String DEFENSIVE_POWERS = POWERS + "Defensive Powers.";
    private static final String OFFENSIVE_POWERS = POWERS + "Offensive Powers.";
    private static final String MISCELLANEOUS_POWERS = POWERS + "Miscellaneous Powers.";
    private static final String MAJOR_POWERS = POWERS + "Major Powers.";

    public static final String ATTACK_ARROW = OFFENSIVE_POWERS + "AttackArrow";
    public static final String ATTACK_BLINDING = OFFENSIVE_POWERS + "AttackBlinding";
    public static final String ATTACK_CONFUSING = OFFENSIVE_POWERS + "AttackConfusing";
    public static final String ATTACK_FIRE = OFFENSIVE_POWERS + "AttackFire";
    public static final String ATTACK_FIREBALL = OFFENSIVE_POWERS + "AttackFireball";
    public static final String ATTACK_FREEZE = OFFENSIVE_POWERS + "AttackFreeze";
    public static final String ATTACK_GRAVITY = OFFENSIVE_POWERS + "AttackGravity";
    public static final String ATTACK_LIGHTNING = OFFENSIVE_POWERS + "AttackLightning";
    public static final String ATTACK_POISON = OFFENSIVE_POWERS + "AttackPoison";
    public static final String ATTACK_PUSH = OFFENSIVE_POWERS + "AttackPush";
    public static final String ATTACK_WEAKNESS = OFFENSIVE_POWERS + "AttackWeakness";
    public static final String ATTACK_WEB = OFFENSIVE_POWERS + "AttackWeb";
    public static final String ATTACK_WITHER = OFFENSIVE_POWERS + "AttackWither";
    public static final String ATTACK_VACUUM = OFFENSIVE_POWERS + "AttackVacuum";

    public static final String INVISIBILITY = DEFENSIVE_POWERS + "Invisibility";
    public static final String INVULNERABILITY_ARROW = DEFENSIVE_POWERS + "InvulnerabilityArrow";
    public static final String INVULNERABILITY_FALL_DAMAGE = DEFENSIVE_POWERS + "InvulnerabilityFallDamage";
    public static final String INVULNERABILITY_FIRE = DEFENSIVE_POWERS + "InvulnerabilityFire";
    public static final String INVULNERABILITY_KNOCKBACK = DEFENSIVE_POWERS + "InvulnerabilityKnockback";

    public static final String BONUS_LOOT = MISCELLANEOUS_POWERS + "BonusLoot";
    public static final String MOVEMENT_SPEED = MISCELLANEOUS_POWERS + "MovementSpeed";
    public static final String TAUNT = MISCELLANEOUS_POWERS + "Taunt";
    public static final String CORPSE = MISCELLANEOUS_POWERS + "Corpse";
    public static final String MOON_WALK = MISCELLANEOUS_POWERS + "MoonWalk";
    public static final String IMPLOSION = MISCELLANEOUS_POWERS + "Implosion";

    public static final String SKELETON_PILLAR = MAJOR_POWERS + "SkeletonPillar";
    public static final String SKELETON_TRACKING_ARROW = MAJOR_POWERS + "SkeletonTrackingArrow";
    public static final String ZOMBIE_BLOAT = MAJOR_POWERS + "ZombieBloat";
    public static final String ZOMBIE_FRIENDS = MAJOR_POWERS + "ZombieFriends";
    public static final String ZOMBIE_NECRONOMICON = MAJOR_POWERS + "ZombieNecronomicon";
    public static final String ZOMBIE_PARENTS = MAJOR_POWERS + "ZombieParents";
    public static final String ZOMBIE_TEAM_ROCKET = MAJOR_POWERS + "ZombieTeamRocket";

    public static final String FROZEN_MESSAGE = "Freeze power message";

    public void initializeConfig() {

        configuration.addDefault(ATTACK_ARROW, true);
        configuration.addDefault(ATTACK_BLINDING, true);
        configuration.addDefault(ATTACK_CONFUSING, true);
        configuration.addDefault(ATTACK_FIRE, true);
        configuration.addDefault(ATTACK_FIREBALL, true);
        configuration.addDefault(ATTACK_FREEZE, true);
        configuration.addDefault(ATTACK_GRAVITY, true);
        configuration.addDefault(ATTACK_POISON, true);
        configuration.addDefault(ATTACK_PUSH, true);
        configuration.addDefault(ATTACK_WEAKNESS, true);
        configuration.addDefault(ATTACK_WEB, true);
        configuration.addDefault(ATTACK_WITHER, true);
        configuration.addDefault(ATTACK_VACUUM, true);

        configuration.addDefault(INVISIBILITY, true);
        configuration.addDefault(INVULNERABILITY_ARROW, true);
        configuration.addDefault(INVULNERABILITY_FALL_DAMAGE, true);
        configuration.addDefault(INVULNERABILITY_FIRE, true);
        configuration.addDefault(INVULNERABILITY_KNOCKBACK, true);

        configuration.addDefault(BONUS_LOOT, true);
        configuration.addDefault(MOVEMENT_SPEED, true);
        configuration.addDefault(TAUNT, true);
        configuration.addDefault(CORPSE, true);
        configuration.addDefault(MOON_WALK, true);
        configuration.addDefault(IMPLOSION, true);

        configuration.addDefault(SKELETON_PILLAR, true);
        configuration.addDefault(SKELETON_TRACKING_ARROW, true);
        configuration.addDefault(ZOMBIE_BLOAT, true);
        configuration.addDefault(ZOMBIE_FRIENDS, true);
        configuration.addDefault(ZOMBIE_NECRONOMICON, true);
        configuration.addDefault(ZOMBIE_PARENTS, true);
        configuration.addDefault(ZOMBIE_TEAM_ROCKET, true);

        configuration.addDefault(FROZEN_MESSAGE, "&9You've been frozen!");

        customConfigLoader.getCustomConfig(CONFIG_NAME).options().copyDefaults(true);
        UnusedNodeHandler.clearNodes(configuration);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
