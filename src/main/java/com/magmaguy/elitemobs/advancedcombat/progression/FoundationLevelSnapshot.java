package com.magmaguy.elitemobs.advancedcombat.progression;

import com.magmaguy.elitemobs.skills.SkillType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Complete immutable foundation-level view captured from the loaded PlayerData cache. */
public record FoundationLevelSnapshot(UUID playerId, Map<SkillType, Integer> levels) {

    public FoundationLevelSnapshot {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(levels, "levels");
        Map<SkillType, Integer> copy = new EnumMap<>(SkillType.class);
        for (SkillType skillType : SkillType.values()) {
            Integer level = levels.get(skillType);
            if (level == null || level < 0)
                throw new IllegalArgumentException("Missing or invalid foundation level for " + skillType);
            copy.put(skillType, level);
        }
        levels = Collections.unmodifiableMap(copy);
    }

    public int level(SkillType skillType) {
        return levels.get(Objects.requireNonNull(skillType, "skillType"));
    }
}
