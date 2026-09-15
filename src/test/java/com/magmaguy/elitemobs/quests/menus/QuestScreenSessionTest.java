package com.magmaguy.elitemobs.quests.menus;

import com.magmaguy.elitemobs.quests.dialogue.QuestDialogueBossBarManager;
import com.magmaguy.magmacore.dialog.DialogManager;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class QuestScreenSessionTest {
    private MockedStatic<Bukkit> bukkit;
    private MockedStatic<QuestDialogueBossBarManager> bossBars;
    private Player player;
    private ConsoleCommandSender console;
    private EntityDamageEvent damage;

    @BeforeEach void open() {
        MockBukkit.mock();
        player = mock(Player.class);
        when(player.getName()).thenReturn("QuestTester");
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        InventoryView view = mock(InventoryView.class);
        when(view.getTopInventory()).thenReturn(mock(Inventory.class));
        when(player.getOpenInventory()).thenReturn(view);
        console = mock(ConsoleCommandSender.class);
        bukkit = mockStatic(Bukkit.class, CALLS_REAL_METHODS);
        bukkit.when(Bukkit::getConsoleSender).thenReturn(console);
        bukkit.when(() -> Bukkit.dispatchCommand(eq(console), anyString())).thenReturn(true);
        bossBars = mockStatic(QuestDialogueBossBarManager.class);
        damage = mock(EntityDamageEvent.class);
        when(damage.getEntity()).thenReturn(player);
        when(damage.getFinalDamage()).thenReturn(1D);
    }

    @AfterEach void close() {
        DialogManager.forgetDialogOwner(player, QuestScreenSession.DIALOG_OWNER);
        bossBars.close();
        bukkit.close();
        MockBukkit.unmock();
    }

    private void show(Object owner) {
        DialogManager.sendDialog(player, new DialogManager.MultiActionDialogBuilder().title("Quest")
                .addAction(DialogManager.ActionButton.of("Quest", new DialogManager.RunCommandAction("/em"))), owner);
    }

    @Test void damageClosesQuestDialogAndContinuesBossBarCleanupOnPaperApi() {
        show(QuestScreenSession.DIALOG_OWNER);
        assertDoesNotThrow(() -> new QuestScreenSession().onDamage(damage));
        bukkit.verify(() -> Bukkit.dispatchCommand(console, "minecraft:dialog clear QuestTester"));
        bossBars.verify(() -> QuestDialogueBossBarManager.close(player, false));
        verify(player, never()).closeInventory();
    }

    @Test void damageLeavesAnotherOwnersDialogUntouched() {
        Object otherOwner = new Object();
        try {
            show(otherOwner);
            new QuestScreenSession().onDamage(damage);
            bukkit.verify(() -> Bukkit.dispatchCommand(console, "minecraft:dialog clear QuestTester"), never());
            bossBars.verify(() -> QuestDialogueBossBarManager.close(player, false));
        } finally {
            DialogManager.forgetDialogOwner(player, otherOwner);
        }
    }

    @Test void zeroDamageDoesNotDismissQuestPresentations() {
        show(QuestScreenSession.DIALOG_OWNER);
        when(damage.getFinalDamage()).thenReturn(0D);
        new QuestScreenSession().onDamage(damage);
        bukkit.verify(() -> Bukkit.dispatchCommand(console, "minecraft:dialog clear QuestTester"), never());
        bossBars.verifyNoInteractions();
    }
}
