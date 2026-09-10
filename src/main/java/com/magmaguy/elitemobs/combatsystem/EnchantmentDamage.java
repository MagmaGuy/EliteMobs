package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.MetadataHandler;
import java.util.Map;
import java.util.UUID;

/** EM attribution for explicitly authored secondary damage; the caller owns native application. */
public final class EnchantmentDamage {
    private EnchantmentDamage() { }

    public static void apply(UUID attackId, Map<String, Object> providerFacts, Runnable application) {
        if (MetadataHandler.PLUGIN == null || !MetadataHandler.PLUGIN.isEnabled())
            throw new IllegalStateException("EliteMobs damage owner is unavailable");
        Object raw = providerFacts.get("elitemobs");
        if (!(raw instanceof Map<?, ?> facts) || !(facts.get("loud_strikes") instanceof Number bonus)
                || !Double.isFinite(bonus.doubleValue()) || bonus.doubleValue() < 0)
            throw new IllegalArgumentException("Missing captured EM combat facts");
        double threatBonus = bonus.doubleValue();
        // The base attack already resolved critical behavior. Secondary effects do not roll again,
        // gain another native damage multiplier, or turn their fixed authored damage into weapon XP.
        var source = new CombatDamageContext.PlayerDamageSource(attackId, null, false, threatBonus);
        CombatDamageContext.runPlayerToEliteBypass(source, application);
    }
}
