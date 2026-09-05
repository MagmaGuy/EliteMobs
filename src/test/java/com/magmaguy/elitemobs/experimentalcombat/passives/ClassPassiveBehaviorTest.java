package com.magmaguy.elitemobs.experimentalcombat.passives;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import com.magmaguy.elitemobs.experimentalcombat.progression.ActiveLineageSnapshot;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.plugin.PluginMock;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ClassPassiveBehaviorTest {
    private JavaPlugin previousPlugin;
    private PlayerMock player;
    private ClassPassiveRuntime runtime;
    private boolean active;

    @BeforeEach
    void open() {
        var server = MockBukkit.mock();
        previousPlugin = MetadataHandler.PLUGIN;
        MetadataHandler.PLUGIN = MockBukkit.loadWith(PluginMock.class, new java.io.ByteArrayInputStream("""
                name: PassiveBehaviorTest
                version: 1
                main: org.mockbukkit.mockbukkit.plugin.PluginMock
                authors: [Autotester]
                """.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        player = server.addPlayer();
        active = true;
    }

    @AfterEach
    void close() {
        try {
            if (runtime != null) runtime.shutdown();
        } finally {
            MockBukkit.unmock();
            MetadataHandler.PLUGIN = previousPlugin;
        }
    }

    @ParameterizedTest
    @CsvSource({
            "paladin,9.88,9.7465,9.7465,0.099,0.099",
            "berserker,10.2535,10.182,10.182,0.1,0.1",
            "ranger,10.152,10.121,10.334,0.106075,0.096845",
            "cleric,9.75,10,10,0.1,0.1",
            "spellcaster,10.152,10.08,10.08,0.1,0.1"
    })
    void rootPassiveChangesRealDamageEventsAndRevokesMovementOnExit(
            String form, double outgoing, double incoming, double followingIncoming,
            double movementBeforeHit, double movementAfterHit) {
        var lineage = BuiltInClassContent.catalog().lineageOf(form);
        var snapshot = new ActiveLineageSnapshot(form, 1, 1, List.of(form), Map.of(form, 1));
        var passive = PassiveAggregate.resolve(lineage, snapshot, BuiltInClassContent.passiveRegistry());
        runtime = new ClassPassiveRuntime(id -> passive, ignored -> active);
        Bukkit.getPluginManager().registerEvents(runtime, MetadataHandler.PLUGIN);
        runtime.reconcile(player, true);

        assertEquals(movementBeforeHit, player.getAttribute(Attribute.MOVEMENT_SPEED).getValue(), 0.000001);
        assertEquals(outgoing, outgoingDamage(), 0.000001);
        assertEquals(incoming, incomingDamage(), 0.000001);
        assertEquals(movementAfterHit, player.getAttribute(Attribute.MOVEMENT_SPEED).getValue(), 0.000001);
        assertEquals(followingIncoming, incomingDamage(), 0.000001);

        active = false;
        runtime.reconcile(player, false);
        assertEquals(10D, outgoingDamage());
        assertEquals(10D, incomingDamage());
        assertEquals(0.1D, player.getAttribute(Attribute.MOVEMENT_SPEED).getValue(), 0.000001);
    }

    private double outgoingDamage() {
        // Root modifiers need no live target. Target-dependent branch traits need separate cases.
        var event = new EliteMobDamagedByPlayerEvent(new EliteEntity(), player, 10D, false);
        Bukkit.getPluginManager().callEvent(event);
        return event.getDamage();
    }

    @SuppressWarnings("removal")
    private double incomingDamage() {
        var attacker = player.getWorld().spawnEntity(player.getLocation(), EntityType.ZOMBIE);
        var hit = new EntityDamageByEntityEvent(attacker, player,
                EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10D);
        var event = new PlayerDamagedByEliteMobEvent(new EliteEntity(), player, hit, null, 10D);
        Bukkit.getPluginManager().callEvent(event);
        attacker.remove();
        return event.getDamage();
    }
}
