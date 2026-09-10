package com.magmaguy.elitemobs.advancedcombat.menu;

import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Inventory presentation used by Bedrock, legacy servers and players who disable book menus. */
final class ClassMenuInventoryAdapter implements ClassMenuRenderer, Listener {
    private static final int MENU_SIZE = 54;
    private static final int[] ROOT_SLOTS = {20, 21, 22, 23, 24, 29, 30, 31, 32, 33, 34, 35};
    private final PlayerActionTokenRegistry tokens;
    private final Map<Inventory, InventorySession> sessions = new HashMap<>();
    private ClassMenuCoordinator coordinator;

    ClassMenuInventoryAdapter(PlayerActionTokenRegistry tokens) {
        this.tokens = tokens;
    }

    void installCoordinator(ClassMenuCoordinator coordinator) {
        if (this.coordinator != null) throw new IllegalStateException("Class menu coordinator already installed");
        this.coordinator = coordinator;
    }

    @Override
    public void showOverview(Player player, ClassMenuView view) {
        ClassMenuPresentation page = ClassMenuPresenter.overview(view);
        Inventory inventory = inventory(player, "&6Classes");
        InventorySession session = new InventorySession(player.getUniqueId());
        inventory.setItem(4, item(
                Material.NETHER_STAR,
                page.bodyLines().get(0),
                List.of()));

        int rootIndex = 0;
        for (ClassMenuPresentation.ActionView action : page.actions()) {
            if (action.kind() == ClassMenuPresentation.ActionKind.FORM) {
                if (rootIndex >= ROOT_SLOTS.length) continue;
                ClassMenuAction.OpenForm openForm = (ClassMenuAction.OpenForm) action.action();
                ClassMenuView.FormView root = view.requireForm(openForm.formId());
                action(inventory, session, ROOT_SLOTS[rootIndex++], rootMaterial(root),
                        action.label(), tooltipLore(action), player, action.action());
            } else if (action.kind() == ClassMenuPresentation.ActionKind.CONTROLS) {
                action(inventory, session, 40, Material.WRITABLE_BOOK,
                        action.label(), tooltipLore(action), player, action.action());
            } else if (action.kind() == ClassMenuPresentation.ActionKind.DEACTIVATE) {
                action(inventory, session, 45, Material.RED_STAINED_GLASS_PANE,
                        action.label(), tooltipLore(action), player, action.action());
            }
        }
        inventory.setItem(49, item(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));
        open(player, inventory, session);
    }

    @Override
    public void showForm(Player player, ClassMenuView view, ClassMenuView.FormView form, boolean showAllClasses) {
        ClassMenuPresentation page = ClassMenuPresenter.form(view, form, showAllClasses);
        Inventory inventory = inventory(player, "&6Class: " + form.displayName());
        InventorySession session = new InventorySession(player.getUniqueId());

        List<String> summary = new ArrayList<>();
        if (form.lineage().size() > 1) summary.add("&7" + String.join(" &8> &7", form.lineage()));
        summary.add(ClassMenuStyle.state(form));
        if (form.unlocked()) {
            summary.add("&7Level &8• &f" + form.effectiveLevel() + "/" + form.effectiveCap());
            summary.add("&7XP &8• &f" + form.xpSummary());
        }
        summary.add(ClassMenuStyle.themed(form, form.resourceName()) + " &8• &7" + form.resourceDescription());
        inventory.setItem(13, item(rootMaterial(form), ClassMenuStyle.themed(form, form.displayName()), summary));

        inventory.setItem(20, abilityItem(Material.FEATHER,
                ClassMenuStyle.section(ClassMenuStyle.BLUE, "Mobility"), form.mobility()));
        inventory.setItem(22, abilityItem(Material.NETHER_STAR,
                ClassMenuStyle.section(ClassMenuStyle.ORANGE, "Signature"), form.signature()));
        inventory.setItem(24, abilityItem(Material.COMPASS,
                ClassMenuStyle.section(ClassMenuStyle.GREEN, "Utility"), form.utility()));
        inventory.setItem(29, requirementsItem(form));
        inventory.setItem(33, passivesItem(form));

        int childIndex = 0;
        int[] childSlots = {16, 17};
        for (ClassMenuPresentation.ActionView action : page.actions()) {
            switch (action.kind()) {
                case PARENT -> action(inventory, session, 9, Material.ARROW,
                        action.label(), tooltipLore(action), player, action.action());
                case FORM -> {
                    if (childIndex >= childSlots.length) continue;
                    ClassMenuAction.OpenForm openForm = (ClassMenuAction.OpenForm) action.action();
                    ClassMenuView.FormView child = view.requireForm(openForm.formId());
                    action(inventory, session, childSlots[childIndex++], rootMaterial(child),
                            action.label(), tooltipLore(action), player, action.action());
                }
                // Green pane is the shared EliteMobs confirmation convention.
                case SELECT -> action(inventory, session, 40, Material.GREEN_STAINED_GLASS_PANE,
                        action.label(), tooltipLore(action), player, action.action());
                case OVERVIEW -> action(inventory, session, 49, Material.ARROW,
                        action.label(), tooltipLore(action), player, action.action());
                default -> {
                }
            }
        }
        open(player, inventory, session);
    }

    @Override
    public void showControls(Player player, ClassMenuView view) {
        ClassMenuPresentation page = ClassMenuPresenter.controls(view);
        Inventory inventory = inventory(player, "&6Class Ability Controls");
        InventorySession session = new InventorySession(player.getUniqueId());
        inventory.setItem(4, item(Material.WRITABLE_BOOK,
                ClassMenuStyle.title("Ability Controls"), page.bodyLines()));

        for (ClassMenuPresentation.ActionView action : page.actions()) {
            switch (action.kind()) {
                case OVERVIEW -> action(inventory, session, 49, Material.ARROW,
                        action.label(), tooltipLore(action), player, action.action());
                default -> {
                }
            }
        }
        open(player, inventory, session);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        InventorySession session = sessions.get(top);
        if (session == null) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!session.playerId().equals(player.getUniqueId())) return;
        int rawSlot = event.getRawSlot();
        if (rawSlot < 0 || rawSlot >= top.getSize()) return;
        if (rawSlot == 49 && session.tokensBySlot().get(rawSlot) == null) {
            player.closeInventory();
            return;
        }
        String token = session.tokensBySlot().get(rawSlot);
        if (token != null && coordinator != null) coordinator.dispatch(player, token);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!sessions.containsKey(top)) return;
        if (event.getRawSlots().stream().anyMatch(slot -> slot < top.getSize())) event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        sessions.remove(event.getView().getTopInventory());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        sessions.entrySet().removeIf(entry -> entry.getValue().playerId().equals(playerId));
        tokens.invalidate(playerId);
    }

    void shutdown() {
        sessions.clear();
    }

    private static Inventory inventory(Player player, String title) {
        return Bukkit.createInventory(player, MENU_SIZE, ChatColorConverter.convert(title));
    }

    private void open(Player player, Inventory inventory, InventorySession session) {
        player.openInventory(inventory);
        sessions.put(inventory, session);
    }

    private void action(
            Inventory inventory,
            InventorySession session,
            int slot,
            Material material,
            String name,
            List<String> lore,
            Player player,
            ClassMenuAction action) {
        inventory.setItem(slot, item(material, name, lore));
        session.tokensBySlot().put(slot, tokens.issue(player.getUniqueId(), action));
    }

    private static ItemStack item(Material material, String name, List<String> lore) {
        return ItemStackGenerator.generateItemStack(material, name, lore);
    }

    private static ItemStack abilityItem(
            Material material,
            String heading,
            ClassMenuView.AbilityView ability) {
        return item(material, heading, abilityLore(ability));
    }

    static List<String> abilityLore(ClassMenuView.AbilityView ability) {
        return List.of("&f" + ability.displayName(), "", "&7" + ability.description());
    }

    private static ItemStack requirementsItem(ClassMenuView.FormView form) {
        List<String> lore = new ArrayList<>();
        for (ClassMenuView.FoundationRequirement requirement : form.requirements()) {
            lore.add((requirement.met() ? "&a" : "&c") + requirement.displayName() + " "
                    + requirement.currentLevel() + "/" + requirement.requiredLevel());
        }
        if (!form.blockers().isEmpty()) {
            lore.add("");
            for (ClassMenuView.BlockerView blocker : form.blockers())
                lore.addAll(ClassMenuText.wrap("", ClassMenuStyle.blockerText(blocker)));
        }
        return item(Material.EXPERIENCE_BOTTLE,
                ClassMenuStyle.section(ClassMenuStyle.GOLD, "Skills"), lore);
    }

    private static ItemStack passivesItem(ClassMenuView.FormView form) {
        List<String> lore = new ArrayList<>();
        for (ClassMenuView.PassiveView passive : form.passives()) {
            if (!lore.isEmpty()) lore.add("");
            lore.addAll(ClassMenuBonusText.lines(form, passive));
        }
        return item(Material.ENCHANTED_BOOK,
                ClassMenuStyle.section(ClassMenuStyle.PURPLE, "Bonuses"), lore);
    }

    private static List<String> tooltipLore(ClassMenuPresentation.ActionView action) {
        return List.of(action.tooltip().split("\\n"));
    }

    private static Material rootMaterial(ClassMenuView.FormView form) {
        return switch (form.resourceName()) {
            case "Resolve" -> Material.SHIELD;
            case "Fury" -> Material.IRON_AXE;
            case "Focus" -> Material.BOW;
            case "Grace" -> Material.GHAST_TEAR;
            case "Mana" -> Material.BLAZE_ROD;
            default -> Material.NETHER_STAR;
        };
    }

    private record InventorySession(UUID playerId, Map<Integer, String> tokensBySlot) {
        private InventorySession(UUID playerId) {
            this(playerId, new HashMap<>());
        }
    }
}
