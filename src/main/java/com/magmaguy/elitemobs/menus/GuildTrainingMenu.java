package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.config.AdvancedCombatSystemConfig;
import com.magmaguy.elitemobs.advancedcombat.menu.ClassSelectionMenu;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.dialog.DialogManager;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Guild Attendant's stable training entry point.
 *
 * <p>The hub chooses the presentation suitable for the player, then delegates each training
 * system to its own canonical menu. It deliberately contains no progression or class-selection
 * logic.</p>
 */
public final class GuildTrainingMenu {
    private static final int FOUNDATION_SKILLS_SLOT = 11;
    private static final int CLASSES_SLOT = 15;
    private static final TrainingInventoryListener LISTENER = new TrainingInventoryListener();

    private GuildTrainingMenu() {
    }

    public static void open(Player player) {
        if (!AdvancedCombatSystemConfig.isEnabled()) {
            SkillBonusMenu.openWeaponSelectMenu(player);
            return;
        }

        if (MenuPresentation.supportsDialogs(player)) {
            openDialog(player);
        } else {
            openInventory(player);
        }
    }

    public static Collection<AdvancedCommand> commands() {
        return List.of(new OpenFoundationSkillsCommand());
    }

    public static Listener listener() {
        return LISTENER;
    }

    public static void shutdown() {
        LISTENER.inventories.clear();
    }

    private static void openDialog(Player player) {
        DialogManager.MultiActionDialogBuilder builder = new DialogManager.MultiActionDialogBuilder()
                .title("<g:#8B0000:#CC4400:#DAA520>Guild Training</g>")
                .columns(1)
                .addBody(DialogManager.PlainMessageBody.of(
                        "&7Choose what to train.")
                        .width(300));

        builder.addAction(DialogManager.ActionButton.of(
                "<g:#2E7D4F:#69C56F>Foundation Skills</g>",
                new DialogManager.RunCommandAction("/elitemobs guildtraining skills"))
                .tooltip("&7Choose bonuses for your trained skills.")
                .width(300));
        builder.addAction(DialogManager.ActionButton.of(
                "<g:#6D3AA8:#A855F7>Classes</g>",
                new DialogManager.RunCommandAction("/elitemobs class"))
                .tooltip("&7Choose a class and inspect its progression.")
                .width(300));

        DialogManager.sendDialog(player, builder);
    }

    private static void openInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 27, "Guild Training");
        inventory.setItem(FOUNDATION_SKILLS_SLOT, ItemStackGenerator.generateItemStack(
                Material.IRON_SWORD,
                "<g:#2E7D4F:#69C56F>Foundation Skills</g>",
                List.of(
                        "&7Choose bonuses for your trained skills.",
                        "",
                        "&eClick to open.")));
        inventory.setItem(CLASSES_SLOT, ItemStackGenerator.generateItemStack(
                Material.NETHER_STAR,
                "<g:#6D3AA8:#A855F7>Classes</g>",
                List.of(
                        "&7Choose a class and inspect its progression.",
                        "",
                        "&eClick to open.")));

        player.openInventory(inventory);
        LISTENER.inventories.add(inventory);
    }

    private static final class TrainingInventoryListener implements Listener {
        private final Set<Inventory> inventories = new HashSet<>();

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (!inventories.contains(event.getView().getTopInventory())) return;
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;
            if (event.getClickedInventory() != event.getView().getTopInventory()) return;

            if (event.getRawSlot() == FOUNDATION_SKILLS_SLOT) {
                player.closeInventory();
                SkillBonusMenu.openWeaponSelectMenu(player);
            } else if (event.getRawSlot() == CLASSES_SLOT) {
                player.closeInventory();
                ClassSelectionMenu.open(player);
            }
        }

        @EventHandler
        public void onInventoryDrag(InventoryDragEvent event) {
            Inventory inventory = event.getView().getTopInventory();
            if (!inventories.contains(inventory)) return;
            if (event.getRawSlots().stream().anyMatch(slot -> slot < inventory.getSize()))
                event.setCancelled(true);
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            inventories.remove(event.getView().getTopInventory());
        }
    }

    private static final class OpenFoundationSkillsCommand extends AdvancedCommand {
        private OpenFoundationSkillsCommand() {
            super(List.of("guildtraining"));
            addLiteral("skills");
            setUsage("/em guildtraining skills");
            setDescription("Opens foundation-skill training from the Guild Training menu.");
            setPermission("elitemobs.skill.npc");
            setSenderType(SenderType.PLAYER);
        }

        @Override
        public void execute(CommandData commandData) {
            SkillBonusMenu.openWeaponSelectMenu(commandData.getPlayerSender());
        }
    }
}
