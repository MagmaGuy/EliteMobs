package com.magmaguy.elitemobs.parties;

import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.magmacore.util.SpigotMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Introduces the party invite command at the moment a player tries to interact with another player. */
public final class PartyInteractionHint implements Listener {
    private static final Map<UUID, Long> nextHintAtNanos = new HashMap<>();

    private static NamespacedKey disabledKey() {
        return new NamespacedKey(MetadataHandler.PLUGIN, "party_interaction_hints_disabled");
    }

    public static void disable(Player player) {
        player.getPersistentDataContainer().set(disabledKey(), PersistentDataType.BYTE, (byte) 1);
        nextHintAtNanos.remove(player.getUniqueId());
        PartyManager.sendConfiguredMessage(player, PartyConfig.getPlayerInteractionHintDisabledMessage());
    }

    public static void shutdown() {
        nextHintAtNanos.clear();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND
                || !(event.getRightClicked() instanceof Player target)
                || !PartyConfig.isEnabled()
                || !PartyConfig.isPlayerInteractionHintsEnabled()
                || !event.getPlayer().hasPermission("elitemobs.party")) return;

        Player player = event.getPlayer();
        if (player.getPersistentDataContainer().has(disabledKey(), PersistentDataType.BYTE)) return;
        Party party = PartyManager.getParty(player.getUniqueId());
        if (target.hasMetadata("NPC")
                || !target.hasPermission("elitemobs.party")
                || PartyManager.hasActivePendingInvite(target.getUniqueId())
                || PartyManager.isInParty(target.getUniqueId())
                || (party != null && party.isFull())) return;

        long now = System.nanoTime();
        if (nextHintAtNanos.getOrDefault(player.getUniqueId(), 0L) > now) return;
        nextHintAtNanos.put(player.getUniqueId(), now
                + PartyConfig.getPlayerInteractionHintCooldownSeconds() * 1_000_000_000L);

        if (PartyInventoryMenu.usesInventoryFallback(player)) {
            PartyInventoryMenu.openInteractionHint(player, target);
            return;
        }

        player.spigot().sendMessage(
                SpigotMessage.simpleMessage(PartyManager.format(
                        PartyConfig.getPlayerInteractionHintMessage().replace("$player", target.getName()))),
                SpigotMessage.commandHoverMessage(
                        PartyManager.format(PartyConfig.getPlayerInteractionHintInviteButton()),
                        PartyManager.format(PartyConfig.getPlayerInteractionHintInviteHover()
                                .replace("$player", target.getName())),
                        "/em party invite " + target.getName()),
                SpigotMessage.simpleMessage(" "),
                SpigotMessage.commandHoverMessage(
                        PartyManager.format(PartyConfig.getPlayerInteractionHintDisableButton()),
                        PartyManager.format(PartyConfig.getPlayerInteractionHintDisableHover()),
                        "/em party hideinteractionhint"));
    }
}
