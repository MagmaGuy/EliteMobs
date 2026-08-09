package com.magmaguy.elitemobs.combatsystem.displays;

import com.magmaguy.easyminecraftgoals.internal.FakeText;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.enchantments.premade.CriticalStrikesConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.utils.VisualDisplay;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

/** Owns transient damage, heal, status, and immunity text popup animation. */
final class CombatPopupManager {

    private static final int DURATION_TICKS = 20;
    private static final List<Popup> activePopups = new ArrayList<>();

    private CombatPopupManager() {
    }

    static void update() {
        Iterator<Popup> iterator = activePopups.iterator();
        while (iterator.hasNext()) {
            Popup popup = iterator.next();
            boolean keep = false;
            try {
                keep = popup.update();
            } catch (RuntimeException exception) {
                MetadataHandler.PLUGIN.getLogger().log(
                        Level.WARNING, "Discarding a failed combat popup", exception);
            }
            if (keep) continue;
            try {
                popup.cleanup();
            } catch (RuntimeException exception) {
                MetadataHandler.PLUGIN.getLogger().log(
                        Level.WARNING, "Failed to clean a combat popup", exception);
            } finally {
                iterator.remove();
            }
        }
    }

    static void shutdown() {
        activePopups.forEach(popup -> {
            try {
                popup.cleanup();
            } catch (RuntimeException exception) {
                MetadataHandler.PLUGIN.getLogger().log(
                        Level.WARNING, "Failed to clean a combat popup during shutdown", exception);
            }
        });
        activePopups.clear();
    }

    static void createDamagePopup(EliteEntity eliteEntity, double damage, boolean critical,
                                  double damageModifier, Vector offset, Player player) {
        if (!MobCombatSettingsConfig.isDisplayDamageOnHit()) return;
        LivingEntity entity = eliteEntity.getUnsyncedLivingEntity();
        if (entity == null || !entity.isValid()) return;

        Location baseLocation = entity.getLocation().clone();
        Location mobLocation = eliteEntity.getLocation();
        double eyeHeight = entity.getEyeHeight();
        baseLocation.add(offset.getX(), eyeHeight + offset.getY() + 0.3, offset.getZ());

        StringBuilder text = new StringBuilder();
        if (damageModifier < 1) {
            text.append(MobCombatSettingsConfig.getResistTextColor());
            if (mobLocation.getWorld() != null)
                mobLocation.getWorld().playSound(mobLocation, Sound.BLOCK_ANVIL_USE, 1f, 1f);
            if (MobCombatSettingsConfig.isDoResistEffect() && player != null)
                HitEffectManager.createResistEffect(eliteEntity, player);
        } else if (damageModifier > 1) {
            text.append(MobCombatSettingsConfig.getWeakTextColor());
            if (mobLocation.getWorld() != null)
                mobLocation.getWorld().playSound(mobLocation, Sound.ENTITY_ITEM_BREAK, 1f, 1f);
            if (MobCombatSettingsConfig.isDoWeakEffect() && player != null)
                HitEffectManager.createWeakEffect(eliteEntity, player);
        } else {
            text.append("&c");
        }

        if (critical) text.append(CriticalStrikesConfig.getCriticalHitColor()).append("&l");
        text.append(eliteEntity.isScaledCombat()
                ? DisplayTextFormatter.percentage(damage / Math.max(1D, eliteEntity.getMaxHealth()))
                : DisplayTextFormatter.number(damage));
        create(baseLocation, ChatColorConverter.convert(text.toString()),
                critical ? PopupType.CRITICAL : PopupType.DAMAGE, critical ? 1.3f : 1.0f);

        if (damageModifier != 1) {
            Vector modifierOffset = offset.clone().subtract(new Vector(0, 0.3, 0));
            Location modifierLocation = entity.getLocation().clone();
            modifierLocation.add(modifierOffset.getX(), eyeHeight + modifierOffset.getY() + 0.3,
                    modifierOffset.getZ());
            boolean resisted = damageModifier < 1;
            create(modifierLocation,
                    resisted ? MobCombatSettingsConfig.getResistText() : MobCombatSettingsConfig.getWeakText(),
                    resisted ? PopupType.RESIST : PopupType.WEAK, 0.8f);
        }

        if (critical) {
            Vector criticalOffset = offset.clone().add(new Vector(0, 0.4, 0));
            Location criticalLocation = entity.getLocation().clone();
            criticalLocation.add(criticalOffset.getX(), eyeHeight + criticalOffset.getY() + 0.3,
                    criticalOffset.getZ());
            create(criticalLocation, CriticalStrikesConfig.getCriticalHitPopup(), PopupType.CRITICAL, 0.9f);
        }
    }

    static void createHealPopup(EliteEntity eliteEntity, double healAmount, boolean fullHeal) {
        if (!MobCombatSettingsConfig.isDisplayDamageOnHit()) return;
        LivingEntity entity = eliteEntity.getUnsyncedLivingEntity();
        if (entity == null || !entity.isValid()) return;

        Vector offset = new Vector(
                ThreadLocalRandom.current().nextDouble(-1, 1), 0,
                ThreadLocalRandom.current().nextDouble(-1, 1));
        Location location = entity.getLocation().clone();
        location.add(offset.getX(), entity.getEyeHeight() + 0.3, offset.getZ());
        String text = fullHeal
                ? MobCombatSettingsConfig.getFullHealMessage()
                : "&a" + MobCombatSettingsConfig.getHealPopupFormat()
                .replace("$amount", DisplayTextFormatter.number(healAmount));
        create(location, ChatColorConverter.convert(text), PopupType.HEAL, fullHeal ? 1.2f : 1.0f);
    }

    static void createImmunePopup(Location location, Player viewer) {
        if (location == null || location.getWorld() == null || viewer == null || !viewer.isOnline()) return;
        FakeText display = VisualDisplay.createStyledFakeText(
                location, MobCombatSettingsConfig.getInvulnerablePopupText(),
                Color.fromARGB(120, 0, 30, 80), true, 1.0f);
        if (display == null) return;

        display.displayTo(viewer);
        activePopups.add(new Popup(display, location, 1.0f));
    }

    private static void create(Location location, String text, PopupType type, float scale) {
        if (location == null || location.getWorld() == null) return;
        FakeText display = VisualDisplay.createStyledFakeText(
                location, text, backgroundColor(type), true, scale);
        if (display == null) return;

        for (Player player : location.getWorld().getPlayers())
            if (player.getLocation().distanceSquared(location) <= 900) display.displayTo(player);
        activePopups.add(new Popup(display, location, scale));
    }

    private static Color backgroundColor(PopupType type) {
        return switch (type) {
            case DAMAGE -> Color.fromARGB(100, 50, 0, 0);
            case CRITICAL -> Color.fromARGB(120, 80, 0, 80);
            case HEAL -> Color.fromARGB(100, 0, 50, 0);
            case WEAK -> Color.fromARGB(100, 0, 0, 80);
            case RESIST -> Color.fromARGB(100, 80, 0, 0);
        };
    }

    private enum PopupType {
        DAMAGE, CRITICAL, HEAL, WEAK, RESIST
    }

    private static final class Popup {
        private final FakeText display;
        private final Location startLocation;
        private final float baseScale;
        private int ticksAlive;

        private Popup(FakeText display, Location startLocation, float baseScale) {
            this.display = display;
            this.startLocation = startLocation.clone();
            this.baseScale = baseScale;
        }

        private boolean update() {
            ticksAlive++;
            if (ticksAlive >= DURATION_TICKS || display == null) return false;

            float progress = (float) ticksAlive / DURATION_TICKS;
            display.teleport(startLocation.clone().add(0, progress * 0.8, 0));
            float scaleMultiplier;
            if (progress < 0.2f)
                scaleMultiplier = 1.0f + (progress / 0.2f) * 0.3f;
            else if (progress > 0.7f)
                scaleMultiplier = 1.3f - ((progress - 0.7f) / 0.3f) * 0.5f;
            else
                scaleMultiplier = 1.3f;
            display.setScale(baseScale * scaleMultiplier);
            if (progress > 0.7f)
                display.setTextOpacity((byte) ((1.0f - ((progress - 0.7f) / 0.3f)) * 255));
            return true;
        }

        private void cleanup() {
            if (display != null) display.remove();
        }
    }
}
