package com.magmaguy.elitemobs.combatsystem.displays;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.EliteMobEnterCombatEvent;
import com.magmaguy.elitemobs.api.EliteMobExitCombatEvent;
import com.magmaguy.elitemobs.api.EliteMobHealEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.enchantments.premade.CriticalStrikesConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.Round;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Comprehensive health and damage display system for Elite Mobs and Custom Bosses.
 * Manages:
 * - Visual health bars (TextDisplay entities with bar characters)
 * - Numeric health display above bosses
 * - Damage popup indicators with critical strike support
 * - Heal popup indicators
 * - Boss bars for high health multiplier bosses
 * - Weak/Resist damage modifiers display
 */
public class BossHealthDisplay implements Listener {

    // Bar characters for visual health display
    private static final String FULL_BAR = "▌";
    private static final String EMPTY_BAR = "▌";
    private static final int BARS_PER_ROW = 10;

    // Colors for health bars
    private static final String COLOR_FULL_HIGH = "&a";      // Green - >75%
    private static final String COLOR_FULL_MED = "&e";       // Yellow - 50-75%
    private static final String COLOR_FULL_LOW = "&c";       // Red - 25-50%
    private static final String COLOR_FULL_CRITICAL = "&4";  // Dark Red - <25%
    private static final String COLOR_EMPTY = "&8";          // Dark Gray

    // Popup colors
    private static final String COLOR_DAMAGE = "&c";         // Red for damage
    private static final String COLOR_HEAL = "&a";           // Green for healing
    private static final String COLOR_CRIT = "&d";           // Magenta for crits

    // Proximity check range for high multiplier bosses
    private static final double PROXIMITY_RANGE = 30.0;

    // Popup animation duration in ticks
    private static final int POPUP_DURATION_TICKS = 20;

    // Active displays per elite entity
    private static final Map<UUID, HealthDisplayData> activeDisplays = new ConcurrentHashMap<>();

    // Active popup displays (for animated cleanup)
    private static final List<PopupData> activePopups = Collections.synchronizedList(new ArrayList<>());

    // Master update task
    private static BukkitTask masterUpdateTask = null;

    /**
     * Starts the master update task that handles all display updates
     */
    public static void startMasterUpdateTask() {
        if (masterUpdateTask != null) return;

        masterUpdateTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Update health displays
                Iterator<Map.Entry<UUID, HealthDisplayData>> iterator = activeDisplays.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<UUID, HealthDisplayData> entry = iterator.next();
                    HealthDisplayData data = entry.getValue();

                    // Check if elite is still valid
                    if (!data.isValid()) {
                        data.cleanup();
                        iterator.remove();
                        continue;
                    }

                    // Check combat timeout
                    if (data.checkCombatTimeout()) {
                        data.cleanup();
                        iterator.remove();
                        continue;
                    }

                    // Update display positions
                    data.updatePositions();

                    // Update boss bar for nearby players (high multiplier bosses)
                    data.updateProximityBossBar();
                }

                // Update popup animations
                synchronized (activePopups) {
                    Iterator<PopupData> popupIterator = activePopups.iterator();
                    while (popupIterator.hasNext()) {
                        PopupData popup = popupIterator.next();
                        if (!popup.update()) {
                            popup.cleanup();
                            popupIterator.remove();
                        }
                    }
                }
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1); // Run every tick for smooth animations
    }

    /**
     * Stops the master update task and cleans up all displays
     */
    public static void shutdown() {
        if (masterUpdateTask != null) {
            masterUpdateTask.cancel();
            masterUpdateTask = null;
        }

        for (HealthDisplayData data : activeDisplays.values()) {
            data.cleanup();
        }
        activeDisplays.clear();

        synchronized (activePopups) {
            for (PopupData popup : activePopups) {
                popup.cleanup();
            }
            activePopups.clear();
        }
    }

    /**
     * Gets or creates display data for an elite entity
     */
    private static HealthDisplayData getOrCreateDisplay(EliteEntity eliteEntity) {
        return activeDisplays.computeIfAbsent(eliteEntity.getEliteUUID(), uuid -> new HealthDisplayData(eliteEntity));
    }

    /**
     * Removes display for an elite entity
     */
    public static void removeDisplay(EliteEntity eliteEntity) {
        HealthDisplayData data = activeDisplays.remove(eliteEntity.getEliteUUID());
        if (data != null) {
            data.cleanup();
        }
    }

    /**
     * Creates a damage popup with animation
     */
    private void createDamagePopup(EliteEntity eliteEntity, double damage, boolean isCritical,
                                   double damageModifier, Vector offset, Player player) {
        if (!MobCombatSettingsConfig.isDisplayDamageOnHit()) return;

        LivingEntity entity = eliteEntity.getUnsyncedLivingEntity();
        if (entity == null || !entity.isValid()) return;

        Location baseLoc = entity.getLocation().clone();
        Location mobLocation = eliteEntity.getLocation();
        double eyeHeight = entity.getEyeHeight();
        baseLoc.add(offset.getX(), eyeHeight + offset.getY() + 0.3, offset.getZ());

        // Build damage text with modifiers
        StringBuilder textBuilder = new StringBuilder();

        // Add weak/resist prefix and effects
        if (damageModifier < 1) {
            // Resist hit
            textBuilder.append(MobCombatSettingsConfig.getResistTextColor());
            // Play resist sound
            if (mobLocation.getWorld() != null) {
                mobLocation.getWorld().playSound(mobLocation, Sound.BLOCK_ANVIL_USE, 1f, 1f);
            }
            // Create resist visual effect (shield armor stand)
            if (MobCombatSettingsConfig.isDoResistEffect() && player != null) {
                createResistArmorStandEffect(eliteEntity, player);
            }
        } else if (damageModifier > 1) {
            // Weak hit
            textBuilder.append(MobCombatSettingsConfig.getWeakTextColor());
            // Play weak sound
            if (mobLocation.getWorld() != null) {
                mobLocation.getWorld().playSound(mobLocation, Sound.ENTITY_ITEM_BREAK, 1f, 1f);
            }
            // Create weak visual effect (sword text displays)
            if (MobCombatSettingsConfig.isDoWeakEffect() && player != null) {
                createWeakVisualEffect(eliteEntity, player);
            }
        } else {
            textBuilder.append(COLOR_DAMAGE);
        }

        // Add critical indicator
        if (isCritical) {
            textBuilder.append(CriticalStrikesConfig.getCriticalHitColor());
            textBuilder.append("&l");
        }

        textBuilder.append(Round.twoDecimalPlaces(damage));

        // Create popup with enhanced display
        createAnimatedPopup(baseLoc, ChatColorConverter.convert(textBuilder.toString()),
                isCritical ? PopupType.CRITICAL : PopupType.DAMAGE, isCritical ? 1.3f : 1.0f);

        // Create additional popup for weak/resist text
        if (damageModifier < 1) {
            Vector modifierOffset = offset.clone().subtract(new Vector(0, 0.3, 0));
            Location modifierLoc = entity.getLocation().clone();
            modifierLoc.add(modifierOffset.getX(), eyeHeight + modifierOffset.getY() + 0.3, modifierOffset.getZ());
            createAnimatedPopup(modifierLoc, ChatColorConverter.convert(MobCombatSettingsConfig.getResistText()),
                    PopupType.RESIST, 0.8f);
        } else if (damageModifier > 1) {
            Vector modifierOffset = offset.clone().subtract(new Vector(0, 0.3, 0));
            Location modifierLoc = entity.getLocation().clone();
            modifierLoc.add(modifierOffset.getX(), eyeHeight + modifierOffset.getY() + 0.3, modifierOffset.getZ());
            createAnimatedPopup(modifierLoc, ChatColorConverter.convert(MobCombatSettingsConfig.getWeakText()),
                    PopupType.WEAK, 0.8f);
        }

        // Create critical hit popup
        if (isCritical) {
            Vector critOffset = offset.clone().add(new Vector(0, 0.4, 0));
            Location critLoc = entity.getLocation().clone();
            critLoc.add(critOffset.getX(), eyeHeight + critOffset.getY() + 0.3, critOffset.getZ());
            createAnimatedPopup(critLoc, ChatColorConverter.convert(CriticalStrikesConfig.getCriticalHitPopup()),
                    PopupType.CRITICAL, 0.9f);
        }
    }

    /**
     * Creates a shield armor stand effect for resist hits
     */
    private void createResistArmorStandEffect(EliteEntity eliteEntity, Player player) {
        if (!eliteEntity.isValid() || !player.isValid()) return;
        if (!eliteEntity.getLocation().getWorld().equals(player.getWorld())) return;

        Location armorStandLocation = getResistLocation(player, eliteEntity);
        if (armorStandLocation.getWorld() == null) return;

        try {
            ArmorStand armorStand = armorStandLocation.getWorld().spawn(armorStandLocation, ArmorStand.class, as -> {
                as.setVisible(false);
                as.setGravity(false);
                as.setMarker(true);
                as.setPersistent(false);
                as.getEquipment().setItemInMainHand(new ItemStack(Material.SHIELD));
                as.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
                as.setRightArmPose(new EulerAngle(Math.PI / 2d, Math.PI + Math.PI / 2d, Math.PI));
            });
            EntityTracker.registerVisualEffects(armorStand);

            new BukkitRunnable() {
                int counter = 0;

                @Override
                public void run() {
                    if (counter > 20 || !eliteEntity.isValid() || !player.isValid() ||
                            !eliteEntity.getLocation().getWorld().equals(player.getWorld())) {
                        EntityTracker.unregister(armorStand, RemovalReason.EFFECT_TIMEOUT);
                        cancel();
                        return;
                    }
                    try {
                        armorStand.teleport(getResistLocation(player, eliteEntity));
                    } catch (Exception e) {
                        // Sometimes x is not finite, doesn't matter
                    }
                    counter++;
                }
            }.runTaskTimer(MetadataHandler.PLUGIN, 1, 1);
        } catch (Exception e) {
            // Silently fail
        }
    }

    private Location getResistLocation(Player player, EliteEntity eliteEntity) {
        Vector armorStandVector = player.getLocation().subtract(eliteEntity.getLocation()).toVector().normalize().multiply(1.5);
        Location armorStandLocation = eliteEntity.getLocation().add(armorStandVector);
        armorStandLocation.setDirection(armorStandVector);
        return armorStandLocation;
    }

    /**
     * Creates text display effects for weak hits (converging on boss)
     */
    private void createWeakVisualEffect(EliteEntity eliteEntity, Player player) {
        if (!eliteEntity.isValid() || !player.isValid()) return;
        if (!eliteEntity.getLocation().getWorld().equals(player.getWorld())) return;

        TextDisplay[] textDisplays = new TextDisplay[2];
        textDisplays[0] = generateWeakTextDisplay(player, eliteEntity, -1);
        textDisplays[1] = generateWeakTextDisplay(player, eliteEntity, 1);

        if (textDisplays[0] == null || textDisplays[1] == null) return;

        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                if (counter > 10 || !eliteEntity.isValid() || !player.isValid() ||
                        !eliteEntity.getLocation().getWorld().equals(player.getWorld())) {
                    if (textDisplays[0] != null) EntityTracker.unregister(textDisplays[0], RemovalReason.EFFECT_TIMEOUT);
                    if (textDisplays[1] != null) EntityTracker.unregister(textDisplays[1], RemovalReason.EFFECT_TIMEOUT);
                    cancel();
                    return;
                }
                for (TextDisplay display : textDisplays) {
                    if (display != null && display.isValid()) {
                        display.teleport(display.getLocation().add(eliteEntity.getLocation()
                                .subtract(display.getLocation()).toVector().normalize().multiply(.4)));
                    }
                }
                counter++;
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 1, 1);
    }

    private TextDisplay generateWeakTextDisplay(Player player, EliteEntity eliteEntity, int offset) {
        Vector displayVector = player.getLocation().clone().add(new Vector(0, 2, 0))
                .subtract(eliteEntity.getLocation()).toVector().normalize().multiply(3.0).rotateAroundY(Math.PI / 8 * offset);
        Location displayLocation = eliteEntity.getLocation().add(displayVector);
        displayLocation.setDirection(displayVector.multiply(-1));

        if (displayLocation.getWorld() == null) return null;

        try {
            TextDisplay display = displayLocation.getWorld().spawn(displayLocation, TextDisplay.class, td -> {
                td.setText(ChatColorConverter.convert("&9&l✦"));
                td.setPersistent(false);
                td.setBillboard(Display.Billboard.CENTER);
                td.setShadowed(true);
                td.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            });
            EntityTracker.registerVisualEffects(display);
            return display;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Creates a heal popup with animation
     */
    private void createHealPopup(EliteEntity eliteEntity, double healAmount, boolean isFullHeal) {
        if (!MobCombatSettingsConfig.isDisplayDamageOnHit()) return;

        LivingEntity entity = eliteEntity.getUnsyncedLivingEntity();
        if (entity == null || !entity.isValid()) return;

        Vector offset = new Vector(
                ThreadLocalRandom.current().nextDouble(-1, 1),
                0,
                ThreadLocalRandom.current().nextDouble(-1, 1)
        );

        Location baseLoc = entity.getLocation().clone();
        double eyeHeight = entity.getEyeHeight();
        baseLoc.add(offset.getX(), eyeHeight + 0.3, offset.getZ());

        String text;
        if (isFullHeal) {
            text = MobCombatSettingsConfig.getFullHealMessage();
        } else {
            text = COLOR_HEAL + "+" + Round.twoDecimalPlaces(healAmount) + " HP";
        }

        createAnimatedPopup(baseLoc, ChatColorConverter.convert(text), PopupType.HEAL, isFullHeal ? 1.2f : 1.0f);
    }

    /**
     * Creates an animated popup display
     */
    private void createAnimatedPopup(Location location, String text, PopupType type, float scale) {
        if (location == null || location.getWorld() == null) return;

        try {
            TextDisplay display = location.getWorld().spawn(location, TextDisplay.class, textDisplay -> {
                textDisplay.setText(text);
                textDisplay.setPersistent(false);
                textDisplay.setBillboard(Display.Billboard.CENTER);
                textDisplay.setShadowed(true);
                textDisplay.setSeeThrough(false);

                // Set background color based on type
                switch (type) {
                    case DAMAGE:
                        textDisplay.setBackgroundColor(Color.fromARGB(100, 50, 0, 0));
                        break;
                    case CRITICAL:
                        textDisplay.setBackgroundColor(Color.fromARGB(120, 80, 0, 80));
                        break;
                    case HEAL:
                        textDisplay.setBackgroundColor(Color.fromARGB(100, 0, 50, 0));
                        break;
                    case WEAK:
                        textDisplay.setBackgroundColor(Color.fromARGB(100, 0, 0, 80));
                        break;
                    case RESIST:
                        textDisplay.setBackgroundColor(Color.fromARGB(100, 80, 0, 0));
                        break;
                }

                // Set up interpolation for smooth animation
                textDisplay.setInterpolationDelay(0);
                textDisplay.setInterpolationDuration(POPUP_DURATION_TICKS);

                // Initial transformation with scale
                Transformation transformation = new Transformation(
                        new Vector3f(0, 0, 0),           // Translation
                        new AxisAngle4f(0, 0, 1, 0),     // Left rotation
                        new Vector3f(scale, scale, scale), // Scale
                        new AxisAngle4f(0, 0, 1, 0)      // Right rotation
                );
                textDisplay.setTransformation(transformation);
            });

            EntityTracker.registerVisualEffects(display);

            // Add to active popups for animation
            synchronized (activePopups) {
                activePopups.add(new PopupData(display, location, type, scale));
            }

        } catch (Exception e) {
            // Silently fail if TextDisplay creation fails
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EliteMobDamagedByPlayerEvent event) {
        EliteEntity eliteEntity = event.getEliteMobEntity();
        if (!eliteEntity.isValid()) return;

        // Create damage popup
        Vector offset = new Vector(
                ThreadLocalRandom.current().nextDouble(-1.5, 1.5),
                0,
                ThreadLocalRandom.current().nextDouble(-1.5, 1.5)
        );
        createDamagePopup(eliteEntity, event.getDamage(), event.isCriticalStrike(),
                event.getDamageModifier(), offset, event.getPlayer());

        // Update health display
        if (MobCombatSettingsConfig.isDisplayVisualHealthBars() ||
            MobCombatSettingsConfig.isDisplayNumericHealth() ||
            MobCombatSettingsConfig.isDisplayBossBarForHighMultiplier()) {

            HealthDisplayData data = getOrCreateDisplay(eliteEntity);
            data.resetCombatTimer();
            data.updateHealthDisplay();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHeal(EliteMobHealEvent event) {
        EliteEntity eliteEntity = event.getEliteEntity();
        if (!eliteEntity.isValid()) return;

        // Create heal popup
        createHealPopup(eliteEntity, event.getHealAmount(), event.isFullHeal());

        // Update health display
        if (MobCombatSettingsConfig.isDisplayVisualHealthBars() ||
            MobCombatSettingsConfig.isDisplayNumericHealth()) {

            HealthDisplayData data = activeDisplays.get(eliteEntity.getEliteUUID());
            if (data != null) {
                data.updateHealthDisplay();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnterCombat(EliteMobEnterCombatEvent event) {
        EliteEntity eliteEntity = event.getEliteMobEntity();
        if (!eliteEntity.isValid()) return;

        // Create display and show boss bar if applicable
        if (MobCombatSettingsConfig.isDisplayVisualHealthBars() ||
            MobCombatSettingsConfig.isDisplayNumericHealth() ||
            MobCombatSettingsConfig.isDisplayBossBarForHighMultiplier()) {

            HealthDisplayData data = getOrCreateDisplay(eliteEntity);
            data.resetCombatTimer();

            // Show boss bar for custom bosses with multiplier > threshold
            if (MobCombatSettingsConfig.isDisplayBossBarForHighMultiplier() && eliteEntity instanceof CustomBossEntity customBoss) {
                double healthMultiplier = customBoss.getHealthMultiplier();
                if (healthMultiplier > MobCombatSettingsConfig.getBossBarHealthMultiplierThreshold()) {
                    data.showBossBarForPlayer(event.getTargetEntity());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onExitCombat(EliteMobExitCombatEvent event) {
        EliteEntity eliteEntity = event.getEliteMobEntity();
        removeDisplay(eliteEntity);
    }

    /**
     * Popup type enum for different display styles
     */
    private enum PopupType {
        DAMAGE, CRITICAL, HEAL, WEAK, RESIST
    }

    /**
     * Data class for animated popups
     */
    private static class PopupData {
        private final TextDisplay display;
        private final Location startLocation;
        private final PopupType type;
        private final float baseScale;
        private final int maxTicks = POPUP_DURATION_TICKS;
        private int ticksAlive = 0;

        public PopupData(TextDisplay display, Location startLocation, PopupType type, float baseScale) {
            this.display = display;
            this.startLocation = startLocation.clone();
            this.type = type;
            this.baseScale = baseScale;
        }

        /**
         * Updates the popup animation. Returns false when animation is complete.
         */
        public boolean update() {
            ticksAlive++;

            if (ticksAlive >= maxTicks || display == null || !display.isValid()) {
                return false;
            }

            // Calculate animation progress (0 to 1)
            float progress = (float) ticksAlive / maxTicks;

            // Move upward with easing
            double yOffset = progress * 0.8; // Rise 0.8 blocks total
            Location newLoc = startLocation.clone().add(0, yOffset, 0);
            display.teleport(newLoc);

            // Scale animation - grow then shrink
            float scaleMultiplier;
            if (progress < 0.2f) {
                // Grow quickly at start
                scaleMultiplier = 1.0f + (progress / 0.2f) * 0.3f;
            } else if (progress > 0.7f) {
                // Shrink at end
                scaleMultiplier = 1.3f - ((progress - 0.7f) / 0.3f) * 0.5f;
            } else {
                scaleMultiplier = 1.3f;
            }

            // Update scale transformation
            float finalScale = baseScale * scaleMultiplier;
            Transformation transformation = new Transformation(
                    new Vector3f(0, 0, 0),
                    new AxisAngle4f(0, 0, 1, 0),
                    new Vector3f(finalScale, finalScale, finalScale),
                    new AxisAngle4f(0, 0, 1, 0)
            );
            display.setTransformation(transformation);

            // Fade out near end
            if (progress > 0.7f) {
                byte opacity = (byte) ((1.0f - ((progress - 0.7f) / 0.3f)) * 255);
                display.setTextOpacity(opacity);
            }

            return true;
        }

        public void cleanup() {
            if (display != null && display.isValid()) {
                EntityTracker.unregister(display, RemovalReason.EFFECT_TIMEOUT);
            }
        }
    }

    /**
     * Data class holding all display elements for a single elite entity
     */
    private static class HealthDisplayData {
        private final EliteEntity eliteEntity;
        private final List<TextDisplay> healthBarDisplays = new ArrayList<>();
        private final Map<Player, BossBar> playerBossBars = new ConcurrentHashMap<>();
        private final double healthMultiplier;
        private TextDisplay numericDisplay = null;
        private long lastCombatTime;

        public HealthDisplayData(EliteEntity eliteEntity) {
            this.eliteEntity = eliteEntity;
            this.lastCombatTime = System.currentTimeMillis();

            if (eliteEntity instanceof CustomBossEntity customBoss) {
                this.healthMultiplier = customBoss.getHealthMultiplier();
            } else {
                this.healthMultiplier = eliteEntity.getHealthMultiplier();
            }
        }

        public boolean isValid() {
            return eliteEntity != null && eliteEntity.isValid();
        }

        public void resetCombatTimer() {
            this.lastCombatTime = System.currentTimeMillis();
        }

        public boolean checkCombatTimeout() {
            long elapsed = System.currentTimeMillis() - lastCombatTime;
            int timeoutSeconds = MobCombatSettingsConfig.getCombatDisplayTimeoutSeconds();
            return elapsed > (timeoutSeconds * 1000L);
        }

        public void updateHealthDisplay() {
            if (!isValid()) return;

            double currentHealth = eliteEntity.getHealth();
            double maxHealth = eliteEntity.getMaxHealth();

            // Clean up old displays
            cleanupVisualDisplays();

            // Create new visual health bars if enabled
            if (MobCombatSettingsConfig.isDisplayVisualHealthBars()) {
                createHealthBars(currentHealth, maxHealth);
            }

            // Create numeric display if enabled
            if (MobCombatSettingsConfig.isDisplayNumericHealth()) {
                createNumericDisplay(currentHealth, maxHealth);
            }

            // Update boss bars
            updateAllBossBars(currentHealth, maxHealth);
        }

        private void cleanupVisualDisplays() {
            if (numericDisplay != null && numericDisplay.isValid()) {
                EntityTracker.unregister(numericDisplay, RemovalReason.EFFECT_TIMEOUT);
                numericDisplay = null;
            }

            for (TextDisplay display : healthBarDisplays) {
                if (display != null && display.isValid()) {
                    EntityTracker.unregister(display, RemovalReason.EFFECT_TIMEOUT);
                }
            }
            healthBarDisplays.clear();
        }

        private void createHealthBars(double currentHealth, double maxHealth) {
            if (!isValid()) return;

            Location baseLoc = getBaseLocation();
            if (baseLoc == null) return;

            // Calculate layout based on health multiplier
            BarLayout layout = calculateBarLayout();
            int totalBars = layout.totalBars;
            int barsPerRow = layout.barsPerRow;
            int rows = layout.rows;

            int filledBars = (int) Math.ceil((currentHealth / maxHealth) * totalBars);

            // Determine color based on health percentage
            double healthPercent = (currentHealth / maxHealth) * 100;
            String filledColor = getHealthColor(healthPercent);

            // Create bars from bottom to top
            int barsRemaining = filledBars;
            int totalBarsRemaining = totalBars;

            for (int row = 0; row < rows; row++) {
                int barsInThisRow = Math.min(barsPerRow, totalBarsRemaining);
                int filledInThisRow = Math.min(barsInThisRow, barsRemaining);

                StringBuilder barText = new StringBuilder();

                // Build the bar string for this row
                for (int i = 0; i < filledInThisRow; i++) {
                    barText.append(filledColor).append(FULL_BAR);
                }
                for (int i = filledInThisRow; i < barsInThisRow; i++) {
                    barText.append(COLOR_EMPTY).append(EMPTY_BAR);
                }

                // Position each row with 0.22 spacing
                Location displayLoc = baseLoc.clone().add(0, row * 0.22, 0);

                TextDisplay display = createHealthBarDisplay(displayLoc, ChatColorConverter.convert(barText.toString()));
                if (display != null) {
                    healthBarDisplays.add(display);
                }

                barsRemaining -= filledInThisRow;
                totalBarsRemaining -= barsInThisRow;
            }
        }

        private void createNumericDisplay(double currentHealth, double maxHealth) {
            if (!isValid()) return;

            double healthPercent = (currentHealth / maxHealth) * 100;
            String color = getHealthColor(healthPercent);

            String numericText = color + "&l" + Round.twoDecimalPlaces(currentHealth) + " &7/ " + color + "&l" + Round.twoDecimalPlaces(maxHealth);

            Location baseLoc = getBaseLocation();
            if (baseLoc == null) return;

            // Position numeric display above the health bars
            BarLayout layout = calculateBarLayout();
            int rows = MobCombatSettingsConfig.isDisplayVisualHealthBars() ? layout.rows : 0;
            Location numericLoc = baseLoc.clone().add(0, rows * 0.22, 0);
            numericDisplay = createHealthBarDisplay(numericLoc, ChatColorConverter.convert(numericText));
        }

        /**
         * Calculates bar layout based on health multiplier.
         * Uses logarithmic scaling for multipliers > 1 to avoid excessive bars.
         * - Multiplier <= 1: 1 row, 10 bars (or proportionally less)
         * - Multiplier > 1: logarithmic scaling, capped at 20 bars for 20x multiplier
         *
         * If useFixedHealthBarSize is enabled in config, always returns a single row of 10 bars.
         */
        private BarLayout calculateBarLayout() {
            // If fixed size is enabled, always use single row of 10 bars
            if (MobCombatSettingsConfig.isUseFixedHealthBarSize()) {
                return new BarLayout(BARS_PER_ROW, BARS_PER_ROW, 1);
            }

            if (healthMultiplier <= 0) {
                return new BarLayout(BARS_PER_ROW, BARS_PER_ROW, 1);
            }

            if (healthMultiplier < 1) {
                // Less than 1x: single row with fewer bars (proportional)
                int totalBars = Math.max(1, (int) Math.round(BARS_PER_ROW * healthMultiplier));
                return new BarLayout(totalBars, totalBars, 1);
            }

            if (healthMultiplier <= 1) {
                // Exactly 1x: single row of 10
                return new BarLayout(BARS_PER_ROW, BARS_PER_ROW, 1);
            }

            // For multipliers > 1, use logarithmic scaling
            // Formula: 10 + 10 * log2(multiplier) / log2(20)
            // This gives us: 1x = 10 bars, 20x = 20 bars, scaling logarithmically between
            double logScale = Math.log(healthMultiplier) / Math.log(20); // log base 20 of multiplier
            int totalBars = (int) Math.ceil(BARS_PER_ROW + (BARS_PER_ROW * logScale));
            totalBars = Math.min(totalBars, 20); // Cap at 20 bars

            // Determine rows based on total bars
            if (totalBars <= 10) {
                return new BarLayout(totalBars, totalBars, 1);
            } else {
                // 2 rows for anything above 10 bars
                int barsPerRow = (int) Math.ceil(totalBars / 2.0);
                return new BarLayout(totalBars, barsPerRow, 2);
            }
        }

        private String getHealthColor(double healthPercent) {
            if (healthPercent > 75) return COLOR_FULL_HIGH;
            if (healthPercent > 50) return COLOR_FULL_MED;
            if (healthPercent > 25) return COLOR_FULL_LOW;
            return COLOR_FULL_CRITICAL;
        }

        private Location getBaseLocation() {
            if (!isValid()) return null;

            LivingEntity entity = eliteEntity.getLivingEntity();
            if (entity == null) return null;

            // Check if this is a CustomBossEntity with a custom model that has a nametag bone
            if (eliteEntity instanceof CustomBossEntity customBoss && customBoss.getCustomModel() != null) {
                Location nametagLocation = customBoss.getCustomModel().getNametagBoneLocation();
                if (nametagLocation != null) {
                    // Use the nametag bone location with a small offset above it
                    return nametagLocation.clone().subtract(0, .9, 0);
                }
            }

            // Fall back to default behavior: eye height + offset
            double height = entity.getEyeHeight() + 0.8;
            return entity.getLocation().clone().add(0, height, 0);
        }

        private TextDisplay createHealthBarDisplay(Location location, String text) {
            if (location == null || location.getWorld() == null) return null;

            try {
                TextDisplay display = location.getWorld().spawn(location, TextDisplay.class, textDisplay -> {
                    textDisplay.setText(text);
                    textDisplay.setPersistent(false);
                    textDisplay.setBillboard(Display.Billboard.CENTER);
                    textDisplay.setShadowed(true);
                    textDisplay.setSeeThrough(false);
                    textDisplay.setBackgroundColor(Color.fromARGB(80, 0, 0, 0));
                });
                EntityTracker.registerVisualEffects(display);
                return display;
            } catch (Exception e) {
                return null;
            }
        }

        public void updatePositions() {
            if (!isValid()) return;

            Location baseLoc = getBaseLocation();
            if (baseLoc == null) return;

            BarLayout layout = calculateBarLayout();

            // Update health bar positions (0.22 spacing between rows)
            for (int i = 0; i < healthBarDisplays.size(); i++) {
                TextDisplay display = healthBarDisplays.get(i);
                if (display != null && display.isValid()) {
                    Location newLoc = baseLoc.clone().add(0, i * 0.22, 0);
                    display.teleport(newLoc);
                }
            }

            // Update numeric display position (above health bars)
            if (numericDisplay != null && numericDisplay.isValid()) {
                int rows = MobCombatSettingsConfig.isDisplayVisualHealthBars() ? layout.rows : 0;
                Location numericLoc = baseLoc.clone().add(0, rows * 0.22, 0);
                numericDisplay.teleport(numericLoc);
            }
        }

        public void showBossBarForPlayer(Player player) {
            if (player == null || !player.isValid()) return;
            if (!isValid()) return;

            if (playerBossBars.containsKey(player)) return;

            String bossName = eliteEntity.getName();
            if (bossName == null) bossName = "Elite Boss";

            BossBar bossBar = Bukkit.createBossBar(
                    bossName,
                    getBarColor(eliteEntity.getHealth() / eliteEntity.getMaxHealth()),
                    BarStyle.SEGMENTED_10
            );

            double progress = Math.max(0, Math.min(1, eliteEntity.getHealth() / eliteEntity.getMaxHealth()));
            bossBar.setProgress(progress);
            bossBar.addPlayer(player);

            playerBossBars.put(player, bossBar);
        }

        public void removeBossBarForPlayer(Player player) {
            BossBar bossBar = playerBossBars.remove(player);
            if (bossBar != null) {
                bossBar.removePlayer(player);
                bossBar.removeAll();
            }
        }

        public void updateAllBossBars(double currentHealth, double maxHealth) {
            double progress = Math.max(0, Math.min(1, currentHealth / maxHealth));
            BarColor color = getBarColor(progress);

            Iterator<Map.Entry<Player, BossBar>> iterator = playerBossBars.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Player, BossBar> entry = iterator.next();
                Player player = entry.getKey();
                BossBar bossBar = entry.getValue();

                if (!player.isValid() || !player.isOnline()) {
                    bossBar.removeAll();
                    iterator.remove();
                    continue;
                }

                bossBar.setProgress(progress);
                bossBar.setColor(color);
            }
        }

        public void updateProximityBossBar() {
            if (!MobCombatSettingsConfig.isDisplayBossBarForHighMultiplier()) return;
            if (healthMultiplier < MobCombatSettingsConfig.getProximityBossBarHealthMultiplierThreshold()) return;
            if (!isValid()) return;

            Location bossLoc = eliteEntity.getLocation();
            if (bossLoc == null || bossLoc.getWorld() == null) return;

            LivingEntity livingEntity = eliteEntity.getLivingEntity();
            if (livingEntity == null) return;

            // Check all nearby players
            for (Entity entity : livingEntity.getNearbyEntities(PROXIMITY_RANGE, PROXIMITY_RANGE, PROXIMITY_RANGE)) {
                if (!(entity instanceof Player player)) continue;

                double distance = player.getLocation().distance(bossLoc);

                if (distance <= PROXIMITY_RANGE) {
                    if (!playerBossBars.containsKey(player)) {
                        showBossBarForPlayer(player);
                    }
                } else {
                    if (!eliteEntity.getDamagers().containsKey(player)) {
                        removeBossBarForPlayer(player);
                    }
                }
            }

            // Remove boss bars for players who left the area (and aren't in combat)
            Iterator<Map.Entry<Player, BossBar>> iterator = playerBossBars.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Player, BossBar> entry = iterator.next();
                Player player = entry.getKey();

                if (!player.isValid() || !player.isOnline()) {
                    entry.getValue().removeAll();
                    iterator.remove();
                    continue;
                }

                if (!player.getWorld().equals(bossLoc.getWorld())) {
                    entry.getValue().removeAll();
                    iterator.remove();
                    continue;
                }

                double distance = player.getLocation().distance(bossLoc);
                if (distance > PROXIMITY_RANGE && !eliteEntity.getDamagers().containsKey(player)) {
                    entry.getValue().removeAll();
                    iterator.remove();
                }
            }
        }

        private BarColor getBarColor(double healthRatio) {
            if (healthRatio > 0.75) return BarColor.GREEN;
            if (healthRatio > 0.50) return BarColor.YELLOW;
            if (healthRatio > 0.25) return BarColor.RED;
            return BarColor.RED;
        }

        public void cleanup() {
            cleanupVisualDisplays();

            for (BossBar bossBar : playerBossBars.values()) {
                bossBar.removeAll();
            }
            playerBossBars.clear();
        }

        /**
         * Simple data class to hold bar layout information
         */
        private static class BarLayout {
            final int totalBars;
            final int barsPerRow;
            final int rows;

            BarLayout(int totalBars, int barsPerRow, int rows) {
                this.totalBars = totalBars;
                this.barsPerRow = barsPerRow;
                this.rows = rows;
            }
        }
    }
}
