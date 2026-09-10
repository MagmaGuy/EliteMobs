package com.magmaguy.elitemobs.items.upgradesystem;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** One EM purchase owns payment and item custody until a gameplay outcome or infrastructure recovery. */
public final class EnchantmentAcquisition {
    private enum State { PREPARED, WITHDRAWING, OWNED, RESOLVED }
    private final UUID id = UUID.randomUUID();
    private final Player player;
    private final ItemStack source, book, ticket;
    private final UpgradeSystem.Preview preview;
    private final EnchantmentProgression.Quote quote;
    private ItemStack upgraded;
    private State state = State.PREPARED;

    public EnchantmentAcquisition(Player player, ItemStack source, ItemStack book, ItemStack ticket) {
        this.player = player;
        this.source = source.clone();
        this.book = book.clone();
        this.ticket = ticket == null ? null : ticket.clone();
        if (ticket != null && (ticket.getAmount() < 1 || !EliteEnchantmentItems.isEliteLuckyTicket(ticket)))
            throw new IllegalArgumentException("Invalid lucky ticket");
        preview = UpgradeSystem.preview(this.source, this.book);
        quote = EnchantmentProgression.quote(this.source, ticket != null);
        upgraded = preview.apply(this.source, this.book);
    }

    public EnchantmentProgression.Quote quote() { return quote; }
    public ItemStack original() { return single(source); }
    public ItemStack upgraded() { return upgraded.clone(); }
    public boolean isOwned() { return state == State.OWNED; }

    /** The caller guards reentrant menu events while an external economy provider is invoked. */
    public boolean purchase(Inventory inventory, int sourceSlot, int bookSlot, int ticketSlot, java.util.function.BooleanSupplier menuActive) {
        if (state != State.PREPARED) throw new IllegalStateException("Purchase already attempted");
        if (!menuActive.getAsBoolean()) throw new IllegalStateException("Menu is no longer active");
        checkInputs(inventory, sourceSlot, bookSlot, ticketSlot);
        validateProviders();
        state = State.WITHDRAWING;
        final boolean paid;
        try { paid = EconomyHandler.tryWithdraw(player.getUniqueId(), quote.price()); }
        catch (RuntimeException ambiguous) {
            state = State.RESOLVED;
            recovery("withdrawal outcome unknown; inputs remain in menu", "unknown", List.of(), ambiguous);
            return false;
        }
        if (!paid) { state = State.RESOLVED; return false; }
        try {
            if (!menuActive.getAsBoolean()) throw new IllegalStateException("Menu closed during payment");
            checkInputs(inventory, sourceSlot, bookSlot, ticketSlot);
            validateProviders();
        } catch (RuntimeException stale) {
            state = State.RESOLVED;
            String refund = refund();
            if (!refund.equals("refunded")) recovery("purchase invalidated; inputs remain in menu", refund, List.of(), stale);
            return false;
        }
        inventory.clear(sourceSlot);
        inventory.clear(bookSlot);
        inventory.clear(ticketSlot);
        state = State.OWNED;
        return true;
    }

    public void validateProviders() { upgraded = preview.apply(source, book); }

    private void checkInputs(Inventory inventory, int sourceSlot, int bookSlot, int ticketSlot) {
        if (!source.equals(inventory.getItem(sourceSlot)) || !book.equals(inventory.getItem(bookSlot))
                || !Objects.equals(ticket, inventory.getItem(ticketSlot)) || !player.isOnline())
            throw new ConcurrentModificationException("Enchantment inputs changed before purchase");
    }

    public void success() { settle(upgraded, false, "success"); }
    public void failure() { settle(single(source), false, "gameplay failure"); }
    public void criticalFailure() { settle(null, false, "gameplay destruction"); }
    public void abort(String reason) { settle(single(source), true, reason); }

    private void settle(ItemStack result, boolean restore, String reason) {
        if (state != State.OWNED) return;
        state = State.RESOLVED; // Reentrant callbacks cannot settle the same custody twice.
        List<ItemStack> delivery = new ArrayList<>();
        remainder(delivery, source);
        remainder(delivery, book);
        remainder(delivery, ticket);
        if (result != null) delivery.add(result.clone());
        if (restore) {
            delivery.add(single(book));
            if (ticket != null) delivery.add(single(ticket));
        }
        String payment = restore ? refund() : "charged";
        List<Map<String,Object>> unresolved = new ArrayList<>();
        boolean deliveryFailed = false;
        for (ItemStack item : delivery) {
            if (deliveryFailed || !player.isOnline()) {
                unresolved.add(Map.of("status", "not-delivered", "item", item.clone()));
                continue;
            }
            try {
                // addItem can partially succeed before a third-party callback fails. Never replay that uncertainty.
                var leftovers = player.getInventory().addItem(item.clone());
                for (ItemStack leftover : leftovers.values()) {
                    var dropped = player.getWorld().dropItem(player.getLocation(), leftover);
                    if (!dropped.isValid()) throw new IllegalStateException("Item drop was rejected");
                }
            } catch (RuntimeException uncertain) {
                unresolved.add(Map.of("status", "delivery-unknown", "item", item.clone()));
                deliveryFailed = true;
            }
        }
        if (!unresolved.isEmpty() || (restore && !payment.equals("refunded")))
            recovery(reason, payment, unresolved, null);
    }

    private String refund() {
        try { return EconomyHandler.refundPayment(player.getUniqueId(), quote.price()) ? "refunded" : "refund-rejected"; }
        catch (RuntimeException ambiguous) { return "refund-unknown"; }
    }

    private void recovery(String reason, String payment, List<Map<String,Object>> items, RuntimeException error) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("transaction", id.toString());
        yaml.set("player", player.getUniqueId().toString());
        yaml.set("reason", reason);
        yaml.set("price", quote.price());
        yaml.set("payment", payment);
        yaml.set("owed-amount", payment.equals("refund-rejected") ? quote.price() : 0);
        yaml.set("manual-payment-reconciliation-required", payment.equals("unknown") || payment.equals("refund-unknown"));
        yaml.set("items", items);
        yaml.set("source-snapshot", source);
        yaml.set("book-snapshot", book);
        yaml.set("ticket-snapshot", ticket);
        if (error != null) yaml.set("error", error.toString());
        Path path = MetadataHandler.PLUGIN.getDataFolder().toPath().resolve("enchantment_recovery").resolve(id + ".yml");
        try {
            Files.createDirectories(path.getParent());
            Path temp = Files.createTempFile(path.getParent(), ".enchantment-", ".tmp");
            try {
                try (var channel = java.nio.channels.FileChannel.open(temp, StandardOpenOption.WRITE)) {
                    var bytes = java.nio.ByteBuffer.wrap(yaml.saveToString().getBytes(StandardCharsets.UTF_8));
                    while (bytes.hasRemaining()) channel.write(bytes);
                    channel.force(true);
                }
                try { Files.move(temp, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
                catch (AtomicMoveNotSupportedException unsupported) { Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING); }
            } finally { Files.deleteIfExists(temp); }
            MetadataHandler.PLUGIN.getLogger().severe("Enchantment acquisition requires operator recovery: " + path);
        } catch (IOException failed) {
            MetadataHandler.PLUGIN.getLogger().severe("Could not persist enchantment recovery " + id + ": " + failed
                    + "\n" + yaml.saveToString());
        }
        if (player.isOnline()) player.sendMessage("§cThis enchantment transaction needs administrator recovery. Reference: " + id);
    }

    private static void remainder(List<ItemStack> items, ItemStack stack) {
        if (stack == null || stack.getAmount() <= 1) return;
        var remainder = stack.clone();
        remainder.setAmount(stack.getAmount() - 1);
        items.add(remainder);
    }
    private static ItemStack single(ItemStack item) { var clone = item.clone(); clone.setAmount(1); return clone; }
}
