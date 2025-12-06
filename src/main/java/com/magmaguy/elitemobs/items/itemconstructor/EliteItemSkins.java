package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.CustomModelsConfig;
import com.magmaguy.elitemobs.utils.CustomModelAdder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Centralizes all custom item skin/model logic for EliteMobs items.
 * Determines which visual appearance to apply based on item level and material type.
 *
 * <p>This class handles:
 * <ul>
 *   <li>Level-based skin tier selection</li>
 *   <li>Item model IDs for 3D appearance (inventory/hand/worn helmets)</li>
 *   <li>Equipment model IDs for worn armor textures</li>
 *   <li>Application of both models to ItemStacks</li>
 * </ul>
 *
 * <h2>Adding New Skin Tiers</h2>
 * <p>To add a new skin tier:
 * <ol>
 *   <li>Add a new entry to {@link SkinTier} enum</li>
 *   <li>Register it in the static block with {@code registerTier(minLevel, SkinTier.YOUR_TIER)}</li>
 *   <li>Add corresponding resource pack assets:
 *     <ul>
 *       <li>Item models: {@code assets/elitemobs/models/equipment/[tier]_[itemtype].json}</li>
 *       <li>Item definitions: {@code assets/elitemobs/items/equipment/[tier]_[itemtype].json}</li>
 *       <li>Equipment definition: {@code assets/elitemobs/equipment/[tier].json}</li>
 *       <li>Worn textures: {@code assets/elitemobs/textures/entity/equipment/humanoid/[tier].png}</li>
 *     </ul>
 *   </li>
 * </ol>
 */
public class EliteItemSkins {

    /**
     * Maps minimum level thresholds to skin tiers.
     * Using NavigableMap for efficient floor lookups.
     */
    private static final NavigableMap<Integer, SkinTier> TIER_THRESHOLDS = new TreeMap<>();
    // Base vanilla attribute values for netherite
    private static final double NETHERITE_SWORD_DAMAGE = 8.0;
    private static final double NETHERITE_AXE_DAMAGE = 10.0;
    private static final double NETHERITE_HOE_DAMAGE = 1.0;

    // ========================================
    // Tier Registration
    // ========================================
    private static final double SWORD_ATTACK_SPEED = 1.6;

    // ========================================
    // Tier Lookup
    // ========================================
    private static final double AXE_ATTACK_SPEED = 1.0;
    private static final double HOE_ATTACK_SPEED = 4.0;

    // ========================================
    // Model ID Generation
    // ========================================
    private static final double NETHERITE_HELMET_ARMOR = 3.0;
    private static final double NETHERITE_CHESTPLATE_ARMOR = 8.0;
    private static final double NETHERITE_LEGGINGS_ARMOR = 6.0;
    private static final double NETHERITE_BOOTS_ARMOR = 3.0;

    // ========================================
    // ItemStack Application
    // ========================================
    private static final double NETHERITE_ARMOR_TOUGHNESS = 3.0; // Per piece for netherite
    private static final double NETHERITE_KNOCKBACK_RESISTANCE = 0.1; // Per piece for netherite (10%)

    // ========================================
    // Utility Methods
    // ========================================
    // Caps
    private static final double MAX_TOTAL_ARMOR = 30.0;
    private static final double MAX_TOTAL_TOUGHNESS = 20.0;
    private static final double MAX_KNOCKBACK_RESISTANCE = 1.0; // 100%

    // ========================================
    // Attribute Scaling
    // ========================================
    private static final double MAX_ATTACK_SPEED_MULTIPLIER = 0.2; // 20% bonus max

    /*
     * Register all skin tier thresholds here.
     * Add new tiers by calling registerTier(minLevel, tier).
     * Tiers are automatically selected based on the highest threshold <= item level.
     */
    static {
        // Tier registration - add new tiers here!
        // Format: registerTier(minimumLevel, SkinTier.TIER_NAME)

        registerTier(20, SkinTier.BRONZE);
        registerTier(50, SkinTier.PALLADIUM);
        registerTier(100, SkinTier.CORRUPTED);
        registerTier(150, SkinTier.LIVING);
        registerTier(200, SkinTier.ULTIMATIUM);

        // Future tiers - uncomment and add resource pack assets when ready:
        // registerTier(210, SkinTier.TIER_210);
        // registerTier(220, SkinTier.TIER_220);
        // registerTier(230, SkinTier.TIER_230);
        // registerTier(240, SkinTier.TIER_240);
        // registerTier(250, SkinTier.TIER_250);
    }

    /**
     * Registers a skin tier at a minimum level threshold.
     * @param minLevel The minimum item level for this tier
     * @param tier The skin tier to apply
     */
    private static void registerTier(int minLevel, SkinTier tier) {
        TIER_THRESHOLDS.put(minLevel, tier);
    }

    /**
     * Determines the skin tier based on item level.
     * @param level The item level
     * @return The appropriate SkinTier (NONE if below all thresholds)
     */
    public static SkinTier getSkinTier(int level) {
        var entry = TIER_THRESHOLDS.floorEntry(level);
        return entry != null ? entry.getValue() : SkinTier.NONE;
    }

    /**
     * Gets the minimum level required for a specific tier.
     * @param tier The tier to look up
     * @return The minimum level, or -1 if tier is NONE or not registered
     */
    public static int getTierMinLevel(SkinTier tier) {
        if (tier == SkinTier.NONE) return -1;
        for (var entry : TIER_THRESHOLDS.entrySet()) {
            if (entry.getValue() == tier) {
                return entry.getKey();
            }
        }
        return -1;
    }

    /**
     * Gets the item model ID for the given material and level.
     * This determines how the item looks in inventory, hand, and for helmets when worn.
     *
     * @param material The item material
     * @param level The item level
     * @return The item model ID (e.g., "elitemobs:equipment/bronze_sword") or null
     */
    public static String getItemModelId(Material material, int level) {
        SkinTier tier = getSkinTier(level);
        if (!tier.hasCustomAssets()) return null;

        ItemType itemType = ItemType.fromMaterial(material);
        if (itemType == null || !itemType.hasItemModel()) return null;

        return buildItemModelId(tier, itemType);
    }

    /**
     * Gets the equipment model ID for armor items (worn appearance).
     * This determines how armor looks when worn on the player's body.
     * Note: Helmets return a value but CustomModelAdder will use item_model instead.
     *
     * @param material The item material
     * @param level The item level
     * @return The equipment model ID (e.g., "elitemobs:bronze") or null
     */
    public static String getEquipmentModelId(Material material, int level) {
        SkinTier tier = getSkinTier(level);
        if (!tier.hasCustomAssets()) return null;

        ItemType itemType = ItemType.fromMaterial(material);
        if (itemType == null || !itemType.hasEquipmentModel()) return null;

        return buildEquipmentModelId(tier);
    }

    /**
     * Builds the item model ID string.
     * Override this method to change the resource pack path structure.
     */
    private static String buildItemModelId(SkinTier tier, ItemType itemType) {
        return "elitemobs:equipment/" + tier.getId() + "_" + itemType.getModelName();
    }

    /**
     * Builds the equipment model ID string.
     * Override this method to change the resource pack path structure.
     */
    private static String buildEquipmentModelId(SkinTier tier) {
        return "elitemobs:" + tier.getId();
    }

    /**
     * Applies level-based custom skins to an ItemStack.
     * This is the main entry point for applying skins to items.
     *
     * <p>Handles both:
     * <ul>
     *   <li>Item model (3D appearance in inventory/hand, and for helmets when worn)</li>
     *   <li>Equipment model (worn texture for body armor)</li>
     *   <li>Attribute scaling (damage, armor, toughness, etc. based on level)</li>
     * </ul>
     *
     * @param itemStack The item to apply skins to
     * @param level The item level (determines which tier's skin to use)
     */
    public static void applyLevelBasedSkin(ItemStack itemStack, int level) {
        if (itemStack == null) return;

        Material material = itemStack.getType();
        String itemModelId = getItemModelId(material, level);
        String equipmentModelId = getEquipmentModelId(material, level);

        if (itemModelId != null) {
            CustomModelAdder.addCustomModel(itemStack, itemModelId);
        }
        if (equipmentModelId != null) {
            CustomModelAdder.addEquippableModel(itemStack, equipmentModelId);
        }

        // Apply attribute scaling based on level
        applyAttributeScaling(itemStack, level);
    }

    /**
     * Applies a specific skin tier to an ItemStack, ignoring level.
     * Useful for admin commands or special items.
     * Note: Attribute scaling will use the tier's minimum level.
     *
     * @param itemStack The item to apply skins to
     * @param tier The specific tier to apply
     */
    public static void applySkinTier(ItemStack itemStack, SkinTier tier) {
        if (itemStack == null || !tier.hasCustomAssets()) return;

        Material material = itemStack.getType();
        ItemType itemType = ItemType.fromMaterial(material);
        if (itemType == null) return;

        if (itemType.hasItemModel()) {
            String itemModelId = buildItemModelId(tier, itemType);
            CustomModelAdder.addCustomModel(itemStack, itemModelId);
        }
        if (itemType.hasEquipmentModel()) {
            String equipmentModelId = buildEquipmentModelId(tier);
            CustomModelAdder.addEquippableModel(itemStack, equipmentModelId);
        }

        // Apply attribute scaling using the tier's minimum level
        int tierLevel = getTierMinLevel(tier);
        if (tierLevel > 0) {
            applyAttributeScaling(itemStack, tierLevel);
        }
    }

    /**
     * Checks if a material can have custom skins applied.
     * @param material The material to check
     * @return true if the material is supported
     */
    public static boolean isSupportedMaterial(Material material) {
        return ItemType.fromMaterial(material) != null;
    }

    /**
     * Checks if an item at the given level would receive a custom skin.
     * @param material The item material
     * @param level The item level
     * @return true if a custom skin would be applied
     */
    public static boolean wouldApplySkin(Material material, int level) {
        return getItemModelId(material, level) != null || getEquipmentModelId(material, level) != null;
    }

    /**
     * Gets the skin tier name for display purposes.
     * @param level The item level
     * @return The tier name (e.g., "Bronze", "Palladium") or "None"
     */
    public static String getTierDisplayName(int level) {
        SkinTier tier = getSkinTier(level);
        if (tier == SkinTier.NONE) return "None";
        String id = tier.getId();
        return id.substring(0, 1).toUpperCase() + id.substring(1);
    }

    /**
     * Applies attribute scaling to an item based on its level.
     * Weapons get damage and attack speed bonuses.
     * Armor gets armor, toughness, and knockback resistance bonuses.
     *
     * @param itemStack The item to apply attributes to
     * @param level The item level (determines scaling)
     */
    public static void applyAttributeScaling(ItemStack itemStack, int level) {
        if (itemStack == null || level < 1) return;
        if (!CustomModelsConfig.useAttributeScaling) return;

        Material material = itemStack.getType();
        ItemType itemType = ItemType.fromMaterial(material);
        if (itemType == null) return;

        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return;

        double levelMultiplier = level / 100.0; // Level 200 = 2.0 = 200% bonus

        switch (itemType) {
            case SWORD -> applyWeaponAttributes(meta, NETHERITE_SWORD_DAMAGE, SWORD_ATTACK_SPEED, levelMultiplier, EquipmentSlotGroup.MAINHAND);
            case AXE -> applyWeaponAttributes(meta, NETHERITE_AXE_DAMAGE, AXE_ATTACK_SPEED, levelMultiplier, EquipmentSlotGroup.MAINHAND);
            case SCYTHE -> applyWeaponAttributes(meta, NETHERITE_HOE_DAMAGE, HOE_ATTACK_SPEED, levelMultiplier, EquipmentSlotGroup.MAINHAND);
            case HELMET -> applyArmorAttributes(meta, NETHERITE_HELMET_ARMOR, levelMultiplier, EquipmentSlotGroup.HEAD);
            case CHESTPLATE -> applyArmorAttributes(meta, NETHERITE_CHESTPLATE_ARMOR, levelMultiplier, EquipmentSlotGroup.CHEST);
            case LEGGINGS -> applyArmorAttributes(meta, NETHERITE_LEGGINGS_ARMOR, levelMultiplier, EquipmentSlotGroup.LEGS);
            case BOOTS -> applyArmorAttributes(meta, NETHERITE_BOOTS_ARMOR, levelMultiplier, EquipmentSlotGroup.FEET);
            case BOW, CROSSBOW -> {
                // Ranged weapons don't use attack_damage/attack_speed attributes for projectile damage.
                // Arrow damage is based on velocity and Power enchantment, not item attributes.
                // Custom model is applied but no attribute scaling needed.
            }
        }

        itemStack.setItemMeta(meta);
    }

    /**
     * Applies weapon attribute modifiers (damage and attack speed).
     */
    private static void applyWeaponAttributes(ItemMeta meta, double baseDamage, double baseSpeed, double levelMultiplier, EquipmentSlotGroup slot) {
        // Damage: base × (1 + level/100)
        double scaledDamage = baseDamage * (1 + levelMultiplier);

        // Attack speed: base × (1 + level/100 × 0.1), capped at 20% bonus
        double speedMultiplier = Math.min(levelMultiplier * 0.1, MAX_ATTACK_SPEED_MULTIPLIER);
        double scaledSpeed = baseSpeed * (1 + speedMultiplier);

        // Remove default attributes first, then add scaled ones
        // Attack damage (subtract 1 for the base player damage of 1)
        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE,
                new AttributeModifier(
                        new NamespacedKey(MetadataHandler.PLUGIN, "elite_damage"),
                        scaledDamage - 1,
                        AttributeModifier.Operation.ADD_NUMBER,
                        slot));

        // Attack speed (subtract 4 for the base player attack speed of 4)
        meta.addAttributeModifier(Attribute.ATTACK_SPEED,
                new AttributeModifier(
                        new NamespacedKey(MetadataHandler.PLUGIN, "elite_speed"),
                        scaledSpeed - 4,
                        AttributeModifier.Operation.ADD_NUMBER,
                        slot));
    }

    /**
     * Applies armor attribute modifiers (armor, toughness, knockback resistance).
     */
    private static void applyArmorAttributes(ItemMeta meta, double baseArmor, double levelMultiplier, EquipmentSlotGroup slot) {
        // Armor: base × (1 + level/100 × 0.5)
        // Scaling is halved to avoid hitting armor cap too quickly
        double scaledArmor = baseArmor * (1 + levelMultiplier * 0.5);

        // Toughness: base × (1 + level/100 × 0.67)
        double scaledToughness = NETHERITE_ARMOR_TOUGHNESS * (1 + levelMultiplier * 0.67);

        // Knockback resistance: base × (1 + level/100)
        double scaledKnockbackResist = Math.min(
                NETHERITE_KNOCKBACK_RESISTANCE * (1 + levelMultiplier),
                MAX_KNOCKBACK_RESISTANCE / 4.0 // Per-piece cap (total 4 pieces = 100%)
        );

        meta.addAttributeModifier(Attribute.ARMOR,
                new AttributeModifier(
                        new NamespacedKey(MetadataHandler.PLUGIN, "elite_armor"),
                        scaledArmor,
                        AttributeModifier.Operation.ADD_NUMBER,
                        slot));

        meta.addAttributeModifier(Attribute.ARMOR_TOUGHNESS,
                new AttributeModifier(
                        new NamespacedKey(MetadataHandler.PLUGIN, "elite_toughness"),
                        scaledToughness,
                        AttributeModifier.Operation.ADD_NUMBER,
                        slot));

        meta.addAttributeModifier(Attribute.KNOCKBACK_RESISTANCE,
                new AttributeModifier(
                        new NamespacedKey(MetadataHandler.PLUGIN, "elite_knockback_resist"),
                        scaledKnockbackResist,
                        AttributeModifier.Operation.ADD_NUMBER,
                        slot));
    }

    /**
     * Available skin tiers. Each tier corresponds to a set of resource pack assets.
     *
     * <p>To add a new tier:
     * <ol>
     *   <li>Add enum value with the tier's ID string (used in resource pack paths)</li>
     *   <li>Register the threshold in the static block above</li>
     * </ol>
     */
    public enum SkinTier {
        /** No custom skin applied (below minimum level threshold) */
        NONE(null),

        // === Current tiers ===
        BRONZE("bronze"),
        PALLADIUM("palladium"),
        CORRUPTED("corrupted"),
        LIVING("living"),
        ULTIMATIUM("ultimatium"),

        // === Future tiers - add new entries here ===
        // TIER_210("tier_210"),
        // TIER_220("tier_220"),
        // TIER_230("tier_230"),
        // TIER_240("tier_240"),
        // TIER_250("tier_250"),
        ;

        private final String id;

        SkinTier(String id) {
            this.id = id;
        }

        /**
         * Gets the ID string used in resource pack paths.
         * @return The tier ID (e.g., "bronze", "palladium") or null for NONE
         */
        public String getId() {
            return id;
        }

        /**
         * Checks if this tier has custom assets.
         * @return true if this tier should apply custom models
         */
        public boolean hasCustomAssets() {
            return id != null;
        }
    }

    /**
     * Supported item types that can have custom skins.
     * Maps material suffixes to resource pack item type names.
     */
    public enum ItemType {
        HELMET("_HELMET", "helmet", true, true),
        CHESTPLATE("_CHESTPLATE", "chestplate", true, true),  // item_model for GUI icons
        LEGGINGS("_LEGGINGS", "leggings", true, true),        // item_model for GUI icons
        BOOTS("_BOOTS", "boots", true, true),                 // item_model for GUI icons
        SWORD("_SWORD", "sword", true, false),
        AXE("_AXE", "axe", true, false),
        SCYTHE("_HOE", "scythe", true, false),  // Hoes become scythes
        BOW("BOW", "bow", true, false),          // Exact match, not suffix
        CROSSBOW("CROSSBOW", "crossbow", true, false),  // Exact match, not suffix
        // TRIDENT - excluded (broken)
        // SPEAR - future, when MC adds spears
        ;

        private final String materialSuffix;
        private final String modelName;
        private final boolean hasItemModel;
        private final boolean hasEquipmentModel;

        ItemType(String materialSuffix, String modelName, boolean hasItemModel, boolean hasEquipmentModel) {
            this.materialSuffix = materialSuffix;
            this.modelName = modelName;
            this.hasItemModel = hasItemModel;
            this.hasEquipmentModel = hasEquipmentModel;
        }

        /**
         * Finds the ItemType for a given material.
         * @param material The material to check
         * @return The matching ItemType or null if not supported
         */
        public static ItemType fromMaterial(Material material) {
            String name = material.name();
            for (ItemType type : values()) {
                // BOW and CROSSBOW are exact matches, others are suffixes
                if (type == BOW || type == CROSSBOW) {
                    if (name.equals(type.materialSuffix)) {
                        return type;
                    }
                } else if (name.endsWith(type.materialSuffix)) {
                    return type;
                }
            }
            return null;
        }

        /**
         * Gets the model name used in resource pack paths.
         * @return The item type name (e.g., "helmet", "sword")
         */
        public String getModelName() {
            return modelName;
        }

        /**
         * Whether this item type has a custom 3D item model.
         * @return true if custom item_model should be applied
         */
        public boolean hasItemModel() {
            return hasItemModel;
        }

        /**
         * Whether this item type uses equipment model for worn appearance.
         * @return true if equippable model should be applied (armor pieces)
         */
        public boolean hasEquipmentModel() {
            return hasEquipmentModel;
        }
    }
}
