package com.magmaguy.elitemobs.experimentalcombat.menu;

import com.magmaguy.elitemobs.menus.MenuPresentation;
import com.magmaguy.magmacore.command.AdvancedCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * One entry point for command, status-screen and NPC class selection.
 *
 * <p>Callers do not choose a UI technology and do not touch progression state. This facade routes
 * to a modern dialog or inventory fallback while the coordinator applies the same projection and
 * action path to both.</p>
 */
public final class ClassSelectionMenu {
    private static final PlayerActionTokenRegistry TOKENS = new PlayerActionTokenRegistry();
    private static final ClassMenuDialogAdapter DIALOGS = new ClassMenuDialogAdapter(TOKENS);
    private static final ClassMenuInventoryAdapter INVENTORIES = new ClassMenuInventoryAdapter(TOKENS);
    private static final ClassMenuCoordinator COORDINATOR =
            new ClassMenuCoordinator(TOKENS, DIALOGS, INVENTORIES);
    private static final Collection<AdvancedCommand> COMMANDS =
            List.of(new ClassMenuActionCommand(COORDINATOR));

    static {
        INVENTORIES.installCoordinator(COORDINATOR);
    }

    private ClassSelectionMenu() {
    }

    public static void open(Player player) {
        COORDINATOR.open(Objects.requireNonNull(player, "player"));
    }

    public static void openForm(Player player, String formId) {
        COORDINATOR.openForm(Objects.requireNonNull(player, "player"), formId);
    }

    /** Commands which must be registered alongside the ordinary /em commands. */
    public static Collection<AdvancedCommand> commands() {
        return COMMANDS;
    }

    /** Listener which must be registered once with the plugin lifecycle. */
    public static Listener listener() {
        return INVENTORIES;
    }

    public static void shutdown() {
        INVENTORIES.shutdown();
        TOKENS.clear();
    }

    static boolean supportsDialogs(Player player) {
        return MenuPresentation.supportsDialogs(player);
    }
}
