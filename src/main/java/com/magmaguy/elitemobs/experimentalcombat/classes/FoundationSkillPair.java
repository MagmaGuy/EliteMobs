package com.magmaguy.elitemobs.experimentalcombat.classes;

import com.magmaguy.elitemobs.skills.SkillType;

import java.util.List;
import java.util.Objects;
import java.util.function.ToIntFunction;

/**
 * The two distinct foundation skills that unlock and cap one class form.
 */
public record FoundationSkillPair(SkillType first, SkillType second) {
    public FoundationSkillPair {
        first = Objects.requireNonNull(first, "first");
        second = Objects.requireNonNull(second, "second");
        if (first == second) throw new IllegalArgumentException("A class form requires two distinct foundation skills");
    }

    public List<SkillType> asList() {
        return List.of(first, second);
    }

    public int limitingLevel(ToIntFunction<SkillType> levelProvider) {
        Objects.requireNonNull(levelProvider, "levelProvider");
        return Math.min(requireLevel(levelProvider.applyAsInt(first)), requireLevel(levelProvider.applyAsInt(second)));
    }

    public List<SkillType> limitingSkills(ToIntFunction<SkillType> levelProvider) {
        Objects.requireNonNull(levelProvider, "levelProvider");
        int firstLevel = requireLevel(levelProvider.applyAsInt(first));
        int secondLevel = requireLevel(levelProvider.applyAsInt(second));
        if (firstLevel < secondLevel) return List.of(first);
        if (secondLevel < firstLevel) return List.of(second);
        return List.of(first, second);
    }

    private static int requireLevel(int level) {
        if (level < 0) throw new IllegalArgumentException("Foundation skill levels must not be negative");
        return level;
    }
}
