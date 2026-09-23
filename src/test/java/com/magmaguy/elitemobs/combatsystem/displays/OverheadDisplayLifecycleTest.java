package com.magmaguy.elitemobs.combatsystem.displays;

import com.magmaguy.easyminecraftgoals.internal.FakeText;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.utils.VisualDisplay;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OverheadDisplayLifecycleTest {
    private ServerMock server;
    private MockedStatic<MobCombatSettingsConfig> settings;
    private MockedStatic<VisualDisplay> visuals;
    private final List<FakeText> emittedLabels = new ArrayList<>();
    private EliteEntity boss;
    private EliteMobDamagedByPlayerEvent hit;

    @BeforeEach void open() {
        server = MockBukkit.mock();
        MetadataHandler.PLUGIN = MockBukkit.createMockPlugin();
        var world = server.addSimpleWorld("display-lifecycle");
        var viewer = server.addPlayer();
        viewer.teleport(new Location(world, 0, 65, 0));
        settings = mockStatic(MobCombatSettingsConfig.class);
        settings.when(MobCombatSettingsConfig::isDisplayVisualHealthBars).thenReturn(true);
        settings.when(MobCombatSettingsConfig::getCombatDisplayTimeoutSeconds).thenReturn(30);
        visuals = mockStatic(VisualDisplay.class);
        visuals.when(() -> VisualDisplay.createStyledFakeText(
                any(Location.class), anyString(), any(Color.class), anyBoolean(), anyFloat()))
                .thenAnswer(ignored -> {
                    FakeText label = mock(FakeText.class);
                    Location location = ignored.getArgument(0);
                    when(label.getLocation()).thenAnswer(call -> location.clone());
                    emittedLabels.add(label);
                    return label;
                });
        var body = mock(LivingEntity.class);
        when(body.getLocation()).thenAnswer(ignored -> new Location(world, 0, 65, 0));
        when(body.getEyeHeight()).thenReturn(1.6);
        boss = mock(EliteEntity.class);
        when(boss.getEliteUUID()).thenReturn(UUID.randomUUID());
        when(boss.isValid()).thenReturn(true);
        when(boss.getLivingEntity()).thenReturn(body);
        when(boss.getHealth()).thenReturn(20.0);
        when(boss.getMaxHealth()).thenReturn(20.0);
        when(boss.getHealthMultiplier()).thenReturn(1.0);
        hit = mock(EliteMobDamagedByPlayerEvent.class);
        when(hit.getEliteMobEntity()).thenReturn(boss);
        when(hit.getPlayer()).thenReturn(viewer);
        BossHealthDisplay.startMasterUpdateTask();
    }

    @AfterEach void close() {
        BossHealthDisplay.shutdown();
        if (visuals != null) visuals.close();
        if (settings != null) settings.close();
        MockBukkit.unmock();
    }

    @Test void removedDisplayCannotReappearWhenTheSameLogicalBossHasANewPhase() {
        var displays = new BossHealthDisplay();
        displays.onDamage(hit);
        // Phase changes remove the display and synchronously respawn the same logical boss.
        BossHealthDisplay.removeDisplay(boss);
        displays.onDamage(hit);
        server.getScheduler().performOneTick();

        assertEquals(1, emittedLabels.size(), "Only the current phase's display may emit labels");
        verify(emittedLabels.getFirst()).displayTo(hit.getPlayer().getUniqueId());
        BossHealthDisplay.removeDisplay(boss);
        verify(emittedLabels.getFirst()).remove();
        server.getScheduler().performOneTick();
        assertEquals(1, emittedLabels.size(), "Cleanup must remain final after queued work drains");
    }

    @Test void shutdownInvalidatesAlreadyQueuedRefreshes() {
        new BossHealthDisplay().onDamage(hit);
        BossHealthDisplay.shutdown();
        server.getScheduler().performOneTick();
        assertEquals(0, emittedLabels.size());
    }

    @Test void viewersEnteringAndLeavingRangeReconcileWithoutAnotherHealthChange() {
        new BossHealthDisplay().onDamage(hit);
        server.getScheduler().performOneTick();
        FakeText label = emittedLabels.getFirst();
        var player = hit.getPlayer();
        var near = player.getLocation();
        player.teleport(near.clone().add(100, 0, 0));
        server.getScheduler().performOneTick();
        verify(label).hideFrom(player.getUniqueId());
        player.teleport(near);
        server.getScheduler().performOneTick();
        verify(label, times(2)).displayTo(player.getUniqueId());
        assertEquals(1, emittedLabels.size(), "Audience changes must reuse the existing display");
    }
}
