package com.magmaguy.elitemobs.powers.scripts.enums;

import lombok.Getter;

public enum ActionType {
    TELEPORT(false),
    MESSAGE(true),
    POTION_EFFECT(true),
    DAMAGE(true),
    RUN_SCRIPT(false),
    SET_ON_FIRE(true),
    VISUAL_FREEZE(true),
    PLACE_BLOCK(false),
    RUN_COMMAND_AS_PLAYER(true),
    RUN_COMMAND_AS_CONSOLE(false),
    STRIKE_LIGHTNING(false),
    SPAWN_PARTICLE(false),
    SET_MOB_AI(true),
    SET_MOB_AWARE(true),
    PLAY_SOUND(false),
    PUSH(true),
    SUMMON_REINFORCEMENT(false),
    BOSS_BAR_MESSAGE(false),
    ACTION_BAR_MESSAGE(false),
    TITLE_MESSAGE(false),
    SPAWN_FIREWORKS(false),
    MAKE_INVULNERABLE(true),
    TAG(true),
    UNTAG(true),
    SET_TIME(false),
    SET_WEATHER(false),
    PLAY_ANIMATION(true),
    SPAWN_FALLING_BLOCK(false);

    @Getter
    private final boolean requiresLivingEntity;

    ActionType(boolean requiresLivingEntity) {
        this.requiresLivingEntity = requiresLivingEntity;
    }
}
