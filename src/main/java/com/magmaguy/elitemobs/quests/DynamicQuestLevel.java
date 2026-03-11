package com.magmaguy.elitemobs.quests;

public final class DynamicQuestLevel {
    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 100;

    private DynamicQuestLevel() {
    }

    public static int clamp(int level) {
        return Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, level));
    }

    public static int toTemplateBucket(int level) {
        return Math.min(20, Math.max(1, clamp(level) / 5));
    }
}
