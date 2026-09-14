package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.powers.scripts.EliteScript;
import com.magmaguy.elitemobs.powers.scripts.caching.EliteScriptBlueprint;
import com.magmaguy.elitemobs.thirdparty.libsdisguises.DisguiseEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedStatic;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

class DisguisedBodyRemovalTest {
    private ServerMock server;
    private LivingEntity body;
    private EliteEntity elite;
    private MockedStatic<DisguiseEntity> disguises;
    private final List<Boolean> validAtUndisguise = new ArrayList<>();

    @BeforeEach void open() {
        server = MockBukkit.mock();
        MetadataHandler.PLUGIN = MockBukkit.createMockPlugin();
        MockBukkit.createMockPlugin("LibsDisguises");
        var world = server.addSimpleWorld("disguised-removal");
        body = (LivingEntity) world.spawnEntity(world.getSpawnLocation(), EntityType.ZOMBIE);
        elite = new BoundElite(body);
        disguises = mockStatic(DisguiseEntity.class);
        disguises.when(() -> DisguiseEntity.undisguise(any())).thenAnswer(invocation -> {
            LivingEntity removedBody = invocation.getArgument(0);
            validAtUndisguise.add(removedBody.isValid());
            return null;
        });
    }

    @AfterEach void close() {
        elite.getElitePowers().forEach(power -> power.closeRuntime());
        EliteScriptBlueprint.shutdown();
        if (disguises != null) disguises.close();
        MockBukkit.unmock();
    }

    @ParameterizedTest
    @EnumSource(value = RemovalReason.class,
            names = {"PHASE_BOSS_PHASE_END", "REINFORCEMENT_CULL", "WORLD_UNLOAD"})
    void discardedBodiesAreInvalidBeforeDisguiseCleanupCanRefreshTheirTracker(RemovalReason reason) {
        assertTrue(body.isValid());
        elite.remove(reason);
        assertFalse(body.isValid());
        assertEquals(List.of(false), validAtUndisguise,
                "LibsDisguises must not refresh the tracker of a still-valid body being discarded");
        assertNull(elite.getLivingEntity());
    }

    @Test void actualDeathKeepsTheExistingDelayedDisguiseCleanupForTheDeathAnimation() {
        elite.remove(RemovalReason.DEATH);
        assertTrue(body.isValid(), "EM leaves the native death animation to the server");
        assertTrue(validAtUndisguise.isEmpty());
        server.getScheduler().performTicks(59);
        assertTrue(validAtUndisguise.isEmpty());
        // Model the server finishing the death animation independently of EM's remove call.
        body.remove();
        server.getScheduler().performTicks(2);
        assertEquals(List.of(false), validAtUndisguise);
    }

    @Test void failedMaterializationAlsoInvalidatesItsBodyBeforeDisguiseCleanup() {
        elite.discardSpawnBody(body);
        assertEquals(List.of(false), validAtUndisguise);
        assertFalse(body.isValid());
        assertNull(elite.getLivingEntity());
        assertNull(elite.getUnsyncedLivingEntity());
    }

    @ParameterizedTest
    @EnumSource(value = RemovalReason.class, names = {"DEATH", "PHASE_BOSS_PHASE_END"})
    void entityRemovalPreservesOnlyTheAdmittedDeathAftermath(RemovalReason reason) {
        var player = server.addPlayer();
        player.teleport(body.getLocation());
        var blueprint = new EliteScriptBlueprint(new CustomConfigFields("removal_runtime.yml", true),
                Map.of("Events", List.of("EliteMobDeathEvent"), "Actions", List.of(Map.of(
                        "action", "MESSAGE", "sValue", "Death aftermath", "wait", 3,
                        "Target", Map.of("targetType", "DIRECT_TARGET")))), "Aftermath");
        var script = new EliteScript(blueprint, new LinkedHashMap<>(), elite);
        elite.getElitePowers().add(script);
        script.check(new EliteMobDeathEvent(elite), elite, player);
        assertNull(player.nextMessage(), "The action is queued before entity removal");

        elite.remove(reason);
        assertNull(elite.getLivingEntity());
        server.getScheduler().performTicks(10);

        assertEquals(reason == RemovalReason.DEATH ? "Death aftermath" : null, player.nextMessage(),
                "EliteEntity must forward DEATH, while a phase removal must hard-cancel the same queued work");
        assertNull(player.nextMessage());
    }

    private static final class BoundElite extends EliteEntity {
        BoundElite(LivingEntity body) {
            livingEntity = body;
            unsyncedLivingEntity = body;
            name = "Removal test boss";
        }
    }
}
