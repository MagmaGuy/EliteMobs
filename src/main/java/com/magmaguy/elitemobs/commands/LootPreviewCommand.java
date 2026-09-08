package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.ClassLootSettingsConfig.Difficulty;
import com.magmaguy.elitemobs.config.ClassLootSettingsConfig.Rank;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.items.ClassLootCoverage;
import com.magmaguy.elitemobs.items.ClassLootFamily;
import com.magmaguy.elitemobs.items.MobLootPreview;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.DynamicListStringCommandArgument;
import com.magmaguy.magmacore.command.arguments.IntegerCommandArgument;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.magmacore.util.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class LootPreviewCommand extends AdvancedCommand {
    private final boolean giveAll;
    private final boolean detailed;

    public LootPreviewCommand(boolean giveAll, boolean detailed) {
        super(List.of("loot"));
        this.giveAll = giveAll;
        this.detailed = detailed;
        String action = giveAll ? "giveall" : "preview";
        addLiteral(action);
        addArgument("mob", new DynamicListStringCommandArgument(
                () -> CustomBossesConfig.getCustomBosses().keySet().stream().sorted().toList(), "<mob.yml>"));
        addArgument("level", new IntegerCommandArgument("<level>"));
        addArgument("difficulty", new ListStringCommandArgument(List.of("NORMAL", "HARD", "MYTHIC"), "<difficulty>"));
        if (detailed) {
            addArgument("rank", new ListStringCommandArgument(List.of("AUTO", "TRASH", "MINIBOSS", "BOSS"), "<rank>"));
            if (!giveAll) {
                var families = new ArrayList<>(List.of("ALL"));
                families.addAll(Arrays.stream(ClassLootFamily.values()).map(Enum::name).toList());
                addArgument("family", new ListStringCommandArgument(families, "<family|ALL>"));
            }
        }
        setUsage("/em loot " + action + " <mob.yml> <level> <difficulty>"
                + (detailed ? " <rank|AUTO>" + (giveAll ? "" : " <family|ALL>") : ""));
        setPermission("elitemobs.loot.admin");
        setSenderType(SenderType.PLAYER);
        setDescription(giveAll ? "Gives one sample of every automatic family and authored item entry; reports non-item rewards."
                : "Reports class loot odds, profiles and the authored table; a selected family also gives a sample.");
    }

    @Override public void execute(CommandData data) {
        var player = data.getPlayerSender();
        var filename = data.getStringArgument("mob");
        var fields = CustomBossesConfig.getCustomBosses().entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(filename)).map(java.util.Map.Entry::getValue).findFirst().orElse(null);
        if (fields == null) { Logger.sendMessage(player, "&cUnknown mob: " + filename); return; }
        Integer level = data.getIntegerArgument("level");
        if (level == null) return;
        if (level < 1 || level > 10000) { Logger.sendMessage(player, "&cPreview level must be between 1 and 10000."); return; }
        Difficulty difficulty;
        Rank rank;
        ClassLootFamily family;
        try {
            difficulty = Difficulty.valueOf(data.getStringArgument("difficulty").toUpperCase(Locale.ROOT));
            String rankInput = detailed ? data.getStringArgument("rank").toUpperCase(Locale.ROOT) : "AUTO";
            rank = rankInput.equals("AUTO") ? ClassLootCoverage.rank(fields) : Rank.valueOf(rankInput);
            String familyInput = detailed && !giveAll ? data.getStringArgument("family").toUpperCase(Locale.ROOT) : "ALL";
            family = familyInput.equals("ALL") ? null : ClassLootFamily.valueOf(familyInput);
        } catch (IllegalArgumentException invalid) {
            Logger.sendMessage(player, "&cInvalid difficulty, rank or family. Use tab completion. " + getUsage());
            return;
        }
        MobLootPreview.run(player, fields, level, difficulty, rank, family, giveAll);
    }
}
