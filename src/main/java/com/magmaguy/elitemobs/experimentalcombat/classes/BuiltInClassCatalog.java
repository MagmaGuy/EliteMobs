package com.magmaguy.elitemobs.experimentalcombat.classes;

import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;

/** Compatibility facade for callers that only need the built-in class catalog. */
public final class BuiltInClassCatalog {
    public static final int VERSION = BuiltInClassContent.PERSISTENCE_VERSION;

    private BuiltInClassCatalog() {
    }

    public static ClassCatalog catalog() {
        return BuiltInClassContent.catalog();
    }
}
