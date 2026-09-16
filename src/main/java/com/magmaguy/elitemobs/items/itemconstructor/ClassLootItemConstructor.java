package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.config.ProceduralItemGenerationSettingsConfig;
import com.magmaguy.elitemobs.config.ClassLootSettingsConfig;
import com.magmaguy.elitemobs.config.ClassLootSettingsConfig.Difficulty;
import com.magmaguy.elitemobs.config.ClassLootSettingsConfig.Rank;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;

/** The CLASS_LOOT construction policy; identity and authored options remain ordinary custom-item fields. */
public final class ClassLootItemConstructor {
    private ClassLootItemConstructor() {}

    public static boolean available(CustomItem item, Difficulty difficulty, Rank rank) {
        return available(item, difficulty, rank, null);
    }

    private static boolean available(CustomItem item, Difficulty difficulty, Rank rank, ItemConstructionContext construction) {
        var fields = item.getCustomItemsConfigFields();
        var family = fields.getClassLootFamily();
        if (!fields.isEnabled() || family == null || fields.getMaterial() == null
                || ClassLootSettingsConfig.profile(difficulty, rank, family) == null) return false;
        var magic = family.magicType();
        if (magic == null) return true;
        if (construction != null) {
            if (magic.magicSkill() == SkillType.STAVES
                    && !ProceduralItemGenerationSettingsConfig.isStavesEnabled()) return false;
            if (magic.magicSkill() == SkillType.WANDS
                    && !ProceduralItemGenerationSettingsConfig.isWandsEnabled()) return false;
            var defaultWeapon = construction.model(magic.fmmItemId());
            if (defaultWeapon == null || defaultWeapon.kind() == null) return false;
            String id = fields.getFmmItemModel();
            var prepared = construction.model(id == null || id.isBlank() ? magic.fmmItemId() : id);
            return prepared != null && prepared.kind() != null;
        }
        if (!magic.isAvailable()) return false;
        try {
            String model = fields.getFmmItemModel();
            return model == null || model.isBlank()
                    || com.magmaguy.freeminecraftmodels.api.magic.MagicWeaponAPI.isWeapon(model);
        } catch (LinkageError incompatible) { return false; }
    }

    public static ItemStack construct(CustomItem item, int level, Difficulty difficulty, Rank rank,
                                      EliteEntity source, Player owner) {
        return construct(item, level, difficulty, rank, source, owner, source == null ? "Elite" : source.getName());
    }

    public static ItemStack construct(CustomItem item, int level, Difficulty difficulty, Rank rank,
                                      EliteEntity source, Player owner, String sourceName) {
        return construct(item, level, difficulty, rank, source, owner, sourceName, null);
    }

    public static ItemStack construct(CustomItem item, int level, Difficulty difficulty, Rank rank,
                                      EliteEntity source, Player owner, String sourceName, ItemConstructionContext construction) {
        if (!available(item, difficulty, rank, construction)) return null;
        if (owner != null && !item.getPermission().isEmpty() && !owner.hasPermission(item.getPermission())) return null;
        var fields = item.getCustomItemsConfigFields();
        var family = fields.getClassLootFamily();
        var profile = ClassLootSettingsConfig.profile(difficulty, rank, family);
        var roll = profile.roll(ClassLootSettingsConfig.budgetFraction(), ClassLootSettingsConfig.minimumPrimaryLevel());
        var nativeEnchantments = new HashMap<>(roll.nativeEnchantments());
        nativeEnchantments.putAll(item.getEnchantments());
        var customEnchantments = new HashMap<>(roll.customEnchantments());
        customEnchantments.putAll(item.getCustomEnchantments());
        var effects = new ArrayList<>(profile.potionEffects());
        effects.addAll(item.getPotionEffects());
        String bossName = ChatColor.stripColor(ChatColorConverter.convert(sourceName))
                .replace("$bossLevel", "").replace("$minibossLevel", "").replace("$normalLevel", "")
                .replace("$eventBossLevel", "").replace("$reinforcementLevel", "")
                .replace("$level", Integer.toString(source == null ? level : source.getLevel())).strip();
        java.util.function.UnaryOperator<String> expand = value -> value.replace("$boss", bossName)
                .replace("$weapon", family.label()).replace("$item", family.label()).replace("$difficulty", difficulty.name());
        String model = fields.getFmmItemModel();
        if ((model == null || model.isBlank()) && family.magicType() != null) model = family.magicType().fmmItemId();
        ItemStack result = ItemConstructor.constructItem(Math.max(1, level), expand.apply(fields.getName()),
                fields.getMaterial(), nativeEnchantments, customEnchantments, effects,
                fields.getLore().stream().map(expand).toList(), source, owner, false,
                fields.getCustomModelID(), fields.getEquipmentModelID(), fields.isSoulbound(), fields.getFilename(),
                fields.getScriptedItem(), fields.getWeaponType(), model, construction);
        if (family.isWeapon() && (construction == null ? WeaponIdentityResolver.progressionSkill(result) : construction.skill(result)) != family.skill())
            throw new IllegalStateException("Custom item " + fields.getFilename() + " lost its " + family.skill() + " identity");
        return result;
    }
}
