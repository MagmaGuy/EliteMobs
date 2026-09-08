package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.config.ClassLootSettingsConfig;
import com.magmaguy.elitemobs.config.ClassLootSettingsConfig.Difficulty;
import com.magmaguy.elitemobs.config.ClassLootSettingsConfig.Rank;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.items.customloottable.*;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** Administrative samples, never death events, command execution, money grants or loot votes. */
public final class MobLootPreview {
    private MobLootPreview() {}

    public static void run(Player player, CustomBossesConfigFields fields, int level, Difficulty difficulty,
                           Rank rank, ClassLootFamily selectedFamily, boolean giveAll) {
        var form = ClassLootSelection.activeClass(player);
        var available = ClassLootCoverage.availableFamilies(difficulty, rank);
        var probabilities = ClassLootSelection.probabilities(available, form);
        List<ItemStack> samples = new ArrayList<>();
        List<String> failures = new ArrayList<>();
        tell(player, "&6Loot review: " + fields.getFilename() + " | level " + level + " | " + difficulty + " | " + rank);
        tell(player, "&7Class: " + (form == null ? "none (unbiased)" : form.displayName())
                + "; boss roll chance: " + percent(ClassLootSettingsConfig.dropChance(rank))
                + "; family percentages below are conditional on a successful roll.");
        if (!ClassLootSettingsConfig.enabled() || !fields.isClassLoot() || fields.isReinforcement())
            tell(player, "&eAutomatic drops are disabled for this configuration. Review samples deliberately bypass that switch.");
        for (String issue : fields.getClassLootPresentationIssues()) tell(player, "&ePresentation: " + issue);
        List<ClassLootFamily> families = selectedFamily == null ? Arrays.asList(ClassLootFamily.values()) : List.of(selectedFamily);
        for (ClassLootFamily family : families) {
            if (!available.contains(family)) {
                tell(player, "&e" + family + ": unavailable on this server (material, profile or magic service).");
                continue;
            }
            var profile = ClassLootSettingsConfig.profile(difficulty, rank, family);
            tell(player, "&f" + family + " &7" + percent(probabilities.getOrDefault(family, 0D))
                    + " | primary " + profile.primary() + " | " + rules(profile.enchantments())
                    + (profile.rareEnchantments().isEmpty() ? "" : " | rare " + rules(profile.rareEnchantments()))
                    + (profile.potionEffects().isEmpty() ? "" : " | effects " + profile.potionEffects()));
            if (giveAll || selectedFamily != null) {
                try {
                    ItemStack sample = ClassLootCoverage.preview(fields, level, difficulty, rank, family, player);
                    if (sample == null) failures.add(family + " could not be generated");
                    else samples.add(sample);
                } catch (RuntimeException failure) { failures.add(family + ": " + failure.getMessage()); }
            }
        }
        if (selectedFamily == null) {
            tell(player, "&6Authored table: all configured variants, including other difficulties."
                    + " The selected tier above applies to automatic equipment; conditions below are reported, not rolled.");
            int index = 0;
            for (CustomLootEntry entry : fields.getCustomLootTable().getEntries()) {
                String label = label(entry);
                tell(player, "&7#" + (++index) + " " + label + " | chance " + percent(entry.getChance())
                        + " | amount " + entry.getAmount()
                        + (entry.getPermission().isEmpty() ? "" : " | permission " + entry.getPermission())
                        + (entry.getWave() < 0 ? "" : " | wave " + entry.getWave()));
                if (!giveAll || !(entry instanceof EliteCustomLootEntry || entry instanceof VanillaCustomLootEntry)) continue;
                // One sample per entry, including disabled/permission-gated entries, without running reward actions.
                try {
                    ItemStack sample = entry instanceof EliteCustomLootEntry elite
                            ? elite.previewDropAtLevel(level, player) : entry.previewDrop(level, player);
                    if (sample == null || sample.getType().isAir()) failures.add(label + " could not be generated");
                    else samples.add(sample);
                } catch (RuntimeException failure) { failures.add(label + ": " + failure.getMessage()); }
            }
        }
        int overflow = 0;
        for (ItemStack sample : samples) {
            for (ItemStack extra : player.getInventory().addItem(sample).values()) {
                player.getWorld().dropItem(player.getLocation(), extra);
                overflow += extra.getAmount();
            }
        }
        for (String failure : failures) {
            tell(player, "&cSkipped: " + failure);
            Logger.warn("[Loot preview] " + fields.getFilename() + ": " + failure);
        }
        tell(player, "&7Generated " + samples.size() + " item samples; " + failures.size() + " failures."
                + (overflow == 0 ? "" : " " + overflow + " overflow items dropped at your feet."));
        if (giveAll || selectedFamily != null)
            tell(player, "&7Samples use normal random enchantment rolls. Commands and currency were not awarded; boss drop chances were bypassed.");
    }

    private static String label(CustomLootEntry entry) {
        if (entry instanceof EliteCustomLootEntry elite)
            return elite.getFilename() + (elite.getDifficultyIDs() == null ? "" : " | difficulty " + elite.getDifficultyIDs());
        if (entry instanceof VanillaCustomLootEntry vanilla) return String.valueOf(vanilla.getMaterial());
        if (entry instanceof CurrencyCustomLootEntry currency) return "currency " + currency.getCurrencyAmount() + " (report only)";
        if (entry instanceof CommandLootTable command) return "command " + command.getCommand() + " (report only)";
        return entry.getClass().getSimpleName() + " (report only)";
    }

    private static String rules(List<ClassLootProfile.Rule> rules) {
        return String.join(", ", rules.stream().map(rule -> rule.key() + " " + rule.level() + " @ " + percent(rule.chance())).toList());
    }
    private static String percent(double value) { return String.format(Locale.ROOT, "%.2f%%", value * 100); }
    private static void tell(Player player, String message) { Logger.sendMessage(player, message); }
}
