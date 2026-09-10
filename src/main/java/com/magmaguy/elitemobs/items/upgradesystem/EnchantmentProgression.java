package com.magmaguy.elitemobs.items.upgradesystem;

import com.magmaguy.elitemobs.config.SpecialItemSystemsConfig;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.magmacore.util.Round;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import java.util.List;

/** Acquisition weight and the existing rounded EM economy, computed without reading or writing lore. */
public final class EnchantmentProgression {
    private EnchantmentProgression() { }

    public static int weight(ItemStack item) {
        if (item == null) return 0;
        var meta = item.getItemMeta();
        if (meta == null) throw new IllegalArgumentException("Missing item metadata");
        var nativeLevels = meta instanceof EnchantmentStorageMeta book ? book.getStoredEnchants() : meta.getEnchants();
        int total = 0;
        for (int value : nativeLevels.values()) total = Math.addExact(total, value);
        // Match the prior lore calculation, including its nine enhanced-native contributions.
        for (Enchantment enchantment : List.of(Enchantment.SHARPNESS, Enchantment.POWER, Enchantment.PROTECTION,
                Enchantment.BANE_OF_ARTHROPODS, Enchantment.SMITE, Enchantment.BLAST_PROTECTION,
                Enchantment.FIRE_PROTECTION, Enchantment.PROJECTILE_PROTECTION, Enchantment.THORNS))
            total = Math.addExact(total, Math.max(0, ItemTagger.getEnchantment(meta, enchantment.getKey()) - enchantment.getMaxLevel()));
        for (int value : EliteEnchantmentItems.custom(item).values()) total = Math.addExact(total, value);
        if (total < 0) throw new IllegalArgumentException("Invalid enchantment progression weight");
        return total;
    }

    public record Quote(int price, double success, double criticalFailure, double challenge, double failure) { }

    public static Quote quote(ItemStack item, boolean lucky) {
        return quote(weight(item), lucky, SpecialItemSystemsConfig.getLuckyTicketMultiplier(),
                SpecialItemSystemsConfig.getCriticalFailureChance(), SpecialItemSystemsConfig.getChallengeChance());
    }

    static Quote quote(int weight, boolean lucky, double luckyMultiplier, double criticalChance, double challengeChance) {
        if (weight < 0 || !Double.isFinite(luckyMultiplier) || luckyMultiplier <= 0
                || !probability(criticalChance) || !probability(challengeChance))
            throw new IllegalArgumentException("Invalid enchantment economy settings");
        double price = Math.pow(weight + 1D, 4);
        if (!Double.isFinite(price) || price > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Enchantment price exceeds the supported range");
        // Preserve the original operation order as well as its two-decimal rounding boundaries.
        double chance = 100D / (weight + 1D) * 4D;
        if (lucky) chance *= luckyMultiplier;
        double success = Round.twoDecimalPlaces(Math.min(1D, chance / 100D));
        double critical = Round.twoDecimalPlaces((criticalChance / (lucky ? 2D : 1D)) * (1D - success));
        double challenge = Round.twoDecimalPlaces((1D - success - critical) * challengeChance);
        double failure = Round.twoDecimalPlaces(1D - success - critical - challenge);
        if (!probability(critical) || !probability(challenge) || !probability(failure))
            throw new IllegalArgumentException("Enchantment outcome probabilities are invalid");
        return new Quote((int) price, success, critical, challenge, failure);
    }

    private static boolean probability(double value) { return Double.isFinite(value) && value >= 0D && value <= 1D; }
}
