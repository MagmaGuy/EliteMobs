package com.magmaguy.elitemobs.experimentalcombat.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Shared concise, level-aware passive copy for dialog and chest menus. */
final class ClassMenuBonusText {
    private static final int MAXIMUM_VISIBLE_ROW_LENGTH = 64;

    private ClassMenuBonusText() {
    }

    static List<String> lines(
            ClassMenuView.FormView form,
            ClassMenuView.PassiveView passive) {
        String styledHeader = (passive.preview() ? "&8Preview &8• " : "")
                + ClassMenuStyle.section(ClassMenuStyle.PURPLE, "Bonus")
                + " " + ClassMenuStyle.themed(form, passive.sourceFormName())
                + " &8• &7Lv &f" + passive.contributionLevel();

        List<String> lines = new ArrayList<>();
        lines.add(styledHeader);
        lines.add("&7" + passive.description());
        if (passive.stats().isEmpty()) return List.copyOf(lines);

        StringBuilder styled = new StringBuilder();
        int visibleLength = 0;
        for (ClassMenuView.PassiveStat stat : passive.stats()) {
            String percent = signedPercent(stat.fraction());
            String value = stat.label() + " " + percent;
            boolean beneficial = stat.label().startsWith("Damage taken")
                    ? stat.fraction() < 0D
                    : stat.fraction() > 0D;
            String styledValue = "&7" + stat.label() + " "
                    + (beneficial ? "&a" : "&c") + percent;
            int separatorLength = 3;
            if (visibleLength + separatorLength + value.length() > MAXIMUM_VISIBLE_ROW_LENGTH) {
                if (!styled.isEmpty()) lines.add(styled.toString());
                styled = new StringBuilder(styledValue);
                visibleLength = value.length();
            } else if (!styled.isEmpty()) {
                styled.append(" &8• ").append(styledValue);
                visibleLength += separatorLength + value.length();
            } else {
                styled.append(styledValue);
                visibleLength = value.length();
            }
        }
        if (!styled.isEmpty()) lines.add(styled.toString());
        return List.copyOf(lines);
    }

    private static String signedPercent(double fraction) {
        return String.format(Locale.ROOT, "%+.1f%%", fraction * 100D);
    }
}
