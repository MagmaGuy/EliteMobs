package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffectContainer;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import com.magmaguy.elitemobs.utils.CustomModelAdder;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ItemConstructor {

    public static ItemStack constructItem(int level,
                                          String rawName,
                                          Material material,
                                          HashMap<Enchantment, Integer> enchantments,
                                          HashMap<String, Integer> customEnchantments,
                                          List<String> potionEffects,
                                          List<String> lore,
                                          EliteEntity eliteEntity,
                                          Player player,
                                          boolean showItemWorth,
                                          String customModelID,
                                          String equipmentModelID,
                                          boolean soulbound,
                                          String filename,
                                          String scriptedItem,
                                          SkillType weaponType,
                                          String fmmItemModel) {
        return constructItem(level, rawName, material, enchantments, customEnchantments, potionEffects, lore, eliteEntity, player, showItemWorth, customModelID, equipmentModelID, soulbound, filename, scriptedItem, weaponType, fmmItemModel, null);
    }

    public static ItemStack constructItem(int level,
                                          String rawName,
                                          Material material,
                                          HashMap<Enchantment, Integer> enchantments,
                                          HashMap<String, Integer> customEnchantments,
                                          List<String> potionEffects,
                                          List<String> lore,
                                          EliteEntity eliteEntity,
                                          Player player,
                                          boolean showItemWorth,
                                          String customModelID,
                                          String equipmentModelID,
                                          boolean soulbound,
                                          String filename,
                                          String scriptedItem,
                                          SkillType weaponType,
                                          String fmmItemModel, ItemConstructionContext construction) {
        boolean modelsEnabled = construction == null ? org.bukkit.Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels") : construction.modelsEnabled();
        if (!modelsEnabled
                && (weaponType == SkillType.STAVES || weaponType == SkillType.WANDS
                || customEnchantments.keySet().stream().anyMatch(id -> id.startsWith("freeminecraftmodels:"))))
            return null;
        if (weaponType == SkillType.STAVES || weaponType == SkillType.WANDS) {
            enchantments = new HashMap<>(enchantments);
            enchantments.remove(Enchantment.PUNCH);
        }
        /*
        Construct initial item
         */
        ItemStack itemStack = ItemStackGenerator.generateItemStack(material);
        /*
        Set the item level
         */
        if (level != -1)
            EliteItemManager.initializeEliteLevel(itemStack, level);
        /*
        Get meta
         */
        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.setDisplayName(ChatColorConverter.convert(rawName));

        /*
        Generate item enchantments
        Note: This only applies enchantments up to a certain level, above that threshold item enchantments only exist
        in the item lore and get interpreted by the combat system
         */
        if (!enchantments.isEmpty())
            EnchantmentGenerator.generateEnchantments(itemMeta, enchantments);

        itemStack.setItemMeta(itemMeta);

        /*
        Generate item lore
         */
        if (lore != null && !lore.isEmpty())
            ItemTagger.registerCustomLore(itemMeta, lore);

        //Tag the potion effects
        new ElitePotionEffectContainer(itemMeta, potionEffects);

        itemStack.setItemMeta(itemMeta);

        // Apply ordinary models only when neither FMM path owns presentation.
        if ((scriptedItem == null || scriptedItem.isEmpty())
                && (fmmItemModel == null || fmmItemModel.isEmpty())) {
            if ((customModelID != null && !customModelID.isEmpty()) || (equipmentModelID != null && !equipmentModelID.isEmpty())) {
                //Config defines at least one model - use config values (may be null for the other)
                String effectiveCustomModelID = (customModelID != null && !customModelID.isEmpty())
                        ? customModelID
                        : EliteItemSkins.getItemModelId(material, level);
                String effectiveEquipmentModelID = (equipmentModelID != null && !equipmentModelID.isEmpty())
                        ? equipmentModelID
                        : EliteItemSkins.getEquipmentModelId(material, level);
                CustomModelAdder.addCustomModel(itemStack, effectiveCustomModelID);
                CustomModelAdder.addEquippableModel(itemStack, effectiveEquipmentModelID);
            } else {
                //No config models defined - use full level-based skin system
                EliteItemSkins.applyLevelBasedSkin(itemStack, level);
            }
        }

        itemMeta = itemStack.getItemMeta();

        //Register filename of the custom item into the persistent metadata
        Objects.requireNonNull(itemMeta).getPersistentDataContainer().set(new NamespacedKey(MetadataHandler.PLUGIN, filename), PersistentDataType.STRING, filename);
        ItemTagger.registerCustomItemId(itemMeta, filename);
        itemStack.setItemMeta(itemMeta);

        // FMM assigns its sole authored identity. Other model IDs remain presentation-only.
        if (fmmItemModel != null && !fmmItemModel.isEmpty() && modelsEnabled) {
            if (construction == null) applyFmmItemData(itemStack, fmmItemModel, filename);
            else {
                var model = construction.model(fmmItemModel);
                if (model == null || (model.kind() == null && model.model() == null))
                    com.magmaguy.magmacore.util.Logger.warn("FMM presentation model '" + fmmItemModel
                            + "' was not found for custom item " + filename + "; its vanilla material will be used.");
                else if (model.kind() != null) model.apply(itemStack);
                else new com.magmaguy.freeminecraftmodels.api.ItemConstructionSnapshot.ItemData(null, model.model(), null).apply(itemStack);
            }
        }
        if (weaponType != null && (construction == null ? WeaponIdentityResolver.progressionSkill(itemStack) : construction.skill(itemStack)) != weaponType) {
            com.magmaguy.magmacore.util.Logger.warn("Custom item " + filename + " requires an available FMM authored weapon matching " + weaponType + ".");
            return null;
        }

        // Apply FMM scripted item data if configured and FMM is installed
        if (scriptedItem != null && !scriptedItem.isEmpty()
                && modelsEnabled) {
            var skill = construction == null ? WeaponIdentityResolver.progressionSkill(itemStack) : construction.skill(itemStack);
            if (skill == SkillType.WANDS || skill == SkillType.STAVES) {
                com.magmaguy.magmacore.util.Logger.warn("Custom item " + filename
                        + " cannot combine an authored FMM weapon with the old scriptedItem path.");
                return null;
            }
            try {
                boolean applied;
                if (construction == null) applied = com.magmaguy.freeminecraftmodels.api.ScriptedItemAPI.applyScriptedItemData(itemStack, scriptedItem);
                else {
                    var data = construction.scriptedItem(scriptedItem);
                    applied = data != null && data.authoredId() != null;
                    if (applied) data.apply(itemStack);
                }
                if (!applied) {
                    com.magmaguy.magmacore.util.Logger.warn("Scripted item '" + scriptedItem
                            + "' not found in FreeMinecraftModels! Referenced by custom item: " + filename);
                }
            } catch (NoClassDefFoundError e) {
                com.magmaguy.magmacore.util.Logger.warn("Failed to apply scripted item data for '"
                        + scriptedItem + "': " + e.getMessage());
            }
        }

        var skill = construction == null ? WeaponIdentityResolver.progressionSkill(itemStack) : construction.skill(itemStack);
        com.magmaguy.elitemobs.items.ItemDurability.prepareMagicWeapon(itemStack, skill == SkillType.WANDS || skill == SkillType.STAVES);
        return commonFeatures(itemStack, eliteEntity, player, enchantments, customEnchantments, showItemWorth, soulbound, construction);
    }

    private static void applyFmmItemData(ItemStack itemStack, String fmmItemModel, String filename) {
        if (!org.bukkit.Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels")) return;
        boolean magicWeapon = false;
        boolean applied = false;
        try {
            magicWeapon = com.magmaguy.freeminecraftmodels.api.magic.MagicWeaponAPI.isWeapon(fmmItemModel);
            applied = magicWeapon
                    ? com.magmaguy.freeminecraftmodels.api.magic.MagicWeaponAPI.applyWeaponData(itemStack, fmmItemModel)
                    : com.magmaguy.freeminecraftmodels.api.ModelItemAPI.applyDisplayModel(itemStack, fmmItemModel);
        } catch (LinkageError incompatibleFmm) {
            // The integration emits the actionable compatibility warning. Never stamp an old identity.
        }
        if (!applied && !magicWeapon) {
            com.magmaguy.magmacore.util.Logger.warn("FMM presentation model '"
                    + fmmItemModel + "' was not found for custom item " + filename
                    + "; its vanilla material will be used.");
        }
    }

    /**
     * Constructs a procedural item with a specific material and level.
     * Used by the skill-based shop system where the material is chosen first,
     * then the level is determined based on player's skill level.
     *
     * @param material      The material to use for the item
     * @param level         The elite level to assign to the item
     * @param player        The player (for soulbinding)
     * @param showItemWorth Whether to show the item's worth in lore
     * @return The constructed ItemStack, or null if material is invalid
     */
    public static ItemStack constructItemWithMaterial(Material material, int level, Player player, boolean showItemWorth) {
        return material == null ? null : constructProceduralItem(
                ProceduralItemType.vanilla(material), level, null, player, showItemWorth);
    }

    public static ItemStack constructItem(double itemTier, EliteEntity killedMob, Player player, boolean showItemWorth) {
        return constructProceduralItem(MaterialGenerator.generateItemType(itemTier), itemTier,
                killedMob, player, showItemWorth);
    }

    public static ItemStack constructProceduralItem(ProceduralItemType type, double itemTier,
                                                   EliteEntity killedMob, Player player, boolean showItemWorth) {
        if (type == null || !type.isAvailable()) return null;
        int level = (int) Math.round(itemTier);
        ItemStack itemStack = ItemStackGenerator.generateItemStack(type.material());
        EliteItemManager.initializeEliteLevel(itemStack, level);
        ItemMeta itemMeta = itemStack.getItemMeta();
        HashMap<Enchantment, Integer> enchantmentMap = EnchantmentGenerator.generateEnchantments(
                itemTier, type.material(), type.magicSkill(), itemMeta);
        HashMap<String, Integer> customEnchantmentMap = type.magicSkill() == null
                ? EnchantmentGenerator.generateCustomEnchantments(itemTier, type.material())
                : com.magmaguy.elitemobs.items.itemconstructor.MagicEnchantmentGeneration.generate(itemTier, type.magicSkill());
        itemMeta.setDisplayName(NameGenerator.generateName(type));
        itemStack.setItemMeta(itemMeta);

        if (type.magicSkill() == null) EliteItemSkins.applyLevelBasedSkin(itemStack, level);
        else if (!type.applyMagicData(itemStack)) return null;
        ItemQualityColorizer.dropQualityColorizer(itemStack);

        return commonFeatures(itemStack, killedMob, player, enchantmentMap, customEnchantmentMap, showItemWorth, true);
    }

    private static ItemStack commonFeatures(ItemStack itemStack,
                                            EliteEntity eliteEntity,
                                            Player player,
                                            HashMap<Enchantment, Integer> enchantments,
                                            HashMap<String, Integer> customEnchantments,
                                            boolean showItemWorth,
                                            boolean soulbound) {
        return commonFeatures(itemStack, eliteEntity, player, enchantments, customEnchantments, showItemWorth, soulbound, null);
    }

    private static ItemStack commonFeatures(ItemStack itemStack,
                                            EliteEntity eliteEntity,
                                            Player player,
                                            HashMap<Enchantment, Integer> enchantments,
                                            HashMap<String, Integer> customEnchantments,
                                            boolean showItemWorth,
                                            boolean soulbound, ItemConstructionContext construction) {

        ItemMeta itemMeta = itemStack.getItemMeta();

        //hide default lore
        if (ItemSettingsConfig.isHideEnchants())
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        /*
        Register elite item
         */
        ItemTagger.registerEliteItem(itemMeta);

        /*
        Register item source for lore redraw
         */
        if (com.magmaguy.elitemobs.items.LootItemPolicy.keepsMobProvenance(itemStack, construction))
            ItemTagger.registerItemSource(eliteEntity, itemMeta);

        //Tag the item
        ItemTagger.registerEnchantments(itemMeta, enchantments);
        itemStack.setItemMeta(itemMeta);
        if (!customEnchantments.isEmpty()) {
            itemStack = construction == null
                    ? com.magmaguy.elitemobs.items.upgradesystem.EliteEnchantmentItems.ITEMS.previewAuthoredCustom(itemStack, customEnchantments).apply(itemStack)
                    : construction.enchantments().applyCustom(itemStack, customEnchantments);
        }

        /*
        Add soulbind if applicable
         */
        if (soulbound) SoulbindEnchantment.addEnchantment(itemStack, player);

        /*
        Update lore
         */
        new EliteItemLore(itemStack, showItemWorth, false, construction);

        return itemStack;

    }

}
