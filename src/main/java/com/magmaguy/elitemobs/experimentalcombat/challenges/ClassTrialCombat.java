package com.magmaguy.elitemobs.experimentalcombat.challenges;

import com.magmaguy.elitemobs.instanced.arena.ArenaContainer;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.powers.lua.LuaElitePower;
import org.bukkit.entity.Player;

/** Binds an authored power to the existing challenge's lifetime and damage ownership. */
final class ClassTrialCombat implements AutoCloseable {
    private final CustomBossEntity boss;
    private final LuaElitePower power;
    private final TrialScriptActor actor;
    private boolean closed;

    ClassTrialCombat(ClassTrialDefinition definition, CustomBossEntity boss, Player player, ArenaContainer arena) {
        this.boss = boss;
        var encounter = TrialEncounterAssets.require(definition.form().id());
        power = new LuaElitePower(encounter.power());
        if (encounter.magicWeapon() != null)
            boss.getLivingEntity().getEquipment().setItemInMainHand(TrialEquipment.magic(encounter.magicWeapon()));
        power.setOwnerEntity(boss);
        actor = new TrialScriptActor(boss, player, arena, power);
        boss.getLivingEntity().setAI(false);
        boss.getElitePowers().add(power);
        try { power.startRuntimeOrThrow(actor); }
        catch (RuntimeException failure) { close(); throw failure; }
    }

    /** The normal Lua runtime owns its tick loop. Existing admission handles failure/refund on this check. */
    void tick() {
        if (!closed && !power.isRuntimeActive()) throw new IllegalStateException("Authored class-trial power stopped; ending this attempt.");
    }

    boolean owns(CustomBossEntity entity) { return actor.owns(entity); }
    double incomingMultiplier() { return 1; }
    double outgoingMultiplier() { return 1; }

    @Override public void close() {
        if (closed) return;
        closed = true;
        boss.getElitePowers().remove(power);
        try { power.closeRuntime(); }
        finally { actor.onShutdown(); }
    }
}
