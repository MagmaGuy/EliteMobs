package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.config.potioneffects.PotionEffectsConfig;
import com.magmaguy.elitemobs.items.customenchantments.CustomEnchantment;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffect;
import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffectContainer;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.utils.Round;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EliteItemLore {

    private final List<String> vanillaEnchantmentsLore = new ArrayList<>();
    private final HashMap<Enchantment, Integer> eliteVanillaEnchantments = new HashMap<>();
    private final ArrayList<String> eliteVanillaEnchantmentsLore = new ArrayList<>();
    private final HashMap<CustomEnchantment, Integer> customEnchantments = new HashMap<>();
    private final ArrayList<String> customEnchantmentLore = new ArrayList<>();
    private final List<String> potionListLore = new ArrayList<>();
    @Getter
    private ItemStack itemStack;
    private ItemMeta itemMeta;
    private List<String> lore;
    private String soulbindInfo = null;
    private boolean showItemWorth;
    private String itemWorth = null;
    private String itemSource = null;
    private EliteEntity eliteEntity;
    private Player soulboundPlayer = null;
    private List<String> customLore = new ArrayList<>();
    private int prestigeLevel = 0;


    public EliteItemLore(ItemStack itemStack, boolean showItemWorth) {

        if (!EliteItemManager.isEliteMobsItem(itemStack)) {
            new WarningMessage("Attempted to rewrite the lore of a non-elitemobs item! This is not supposed to happen.");
            return;
        }

        this.itemStack = itemStack;
        this.itemMeta = itemStack.getItemMeta();
        this.lore = new ArrayList<>();
        this.showItemWorth = showItemWorth;

        if (ItemSettingsConfig.isHideAttributes()) itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        constructVanillaEnchantments();

        parseAllEliteEnchantments();
        constructEliteEnchantments();

        parseCustomEnchantments();
        constructCustomEnchantments();

        constructSoulbindEntry();
        constructSoulboundOwner();
        constructItemSource();
        constructCustomLore();
        constructPrestigeLevel();

        constructPotionEffects();

        constructItemWorth();

        writeNewLore();

        this.itemMeta.setLore(lore);
        this.itemStack.setItemMeta(this.itemMeta);

    }

    private void constructVanillaEnchantments() {
        for (Enchantment enchantment : itemMeta.getEnchants().keySet())
            if (enchantment.getName().contains("CURSE"))
                vanillaEnchantmentsLore.add(ChatColorConverter.convert(
                        "&c" + EnchantmentsConfig.getEnchantment(enchantment).getName() + " "
                                + itemMeta.getEnchants().get(enchantment)));
            else
                vanillaEnchantmentsLore.add(ChatColorConverter.convert(
                        "&7" + EnchantmentsConfig.getEnchantment(enchantment).getName() + " "
                                + itemMeta.getEnchants().get(enchantment)));
    }

    private void parseAllEliteEnchantments() {
        parseEliteEnchantments(Enchantment.DAMAGE_ALL);
        parseEliteEnchantments(Enchantment.ARROW_DAMAGE);
        parseEliteEnchantments(Enchantment.PROTECTION_ENVIRONMENTAL);
        parseEliteEnchantments(Enchantment.DAMAGE_ARTHROPODS);
        parseEliteEnchantments(Enchantment.DAMAGE_UNDEAD);
        parseEliteEnchantments(Enchantment.PROTECTION_EXPLOSIONS);
        parseEliteEnchantments(Enchantment.PROTECTION_FIRE);
        parseEliteEnchantments(Enchantment.PROTECTION_PROJECTILE);
        parseEliteEnchantments(Enchantment.THORNS);
    }

    private void parseEliteEnchantments(Enchantment enchantment) {
        int enchantmentLevel = ItemTagger.getEnchantment(itemMeta, enchantment.getKey());
        if (enchantmentLevel > enchantment.getMaxLevel())
            eliteVanillaEnchantments.put(enchantment, enchantmentLevel - enchantment.getMaxLevel());
    }

    private void constructEliteEnchantments() {
        for (Enchantment enchantment : eliteVanillaEnchantments.keySet())
            eliteVanillaEnchantmentsLore.add(ChatColorConverter.convert(
                    "&7" + ItemSettingsConfig.getEliteEnchantLoreString() + " "
                            + EnchantmentsConfig.getEnchantment(enchantment).getName()
                            + " " + eliteVanillaEnchantments.get(enchantment)));

    }

    /**
     * Note: This excludes the soulbind enchantment as it doesn't store an integer value
     */
    private void parseCustomEnchantments() {
        for (CustomEnchantment customEnchantment : CustomEnchantment.getCustomEnchantments()) {
            int enchantmentLevel = ItemTagger.getEnchantment(itemMeta, customEnchantment.getKey());
            if (enchantmentLevel > 0)
                customEnchantments.put(customEnchantment, enchantmentLevel);
        }
    }

    private void constructCustomEnchantments() {
        for (CustomEnchantment customEnchantment : customEnchantments.keySet())
            customEnchantmentLore.add(ChatColorConverter.convert
                    ("&6" + customEnchantment.getEnchantmentsConfigFields().getName() + " "
                            + customEnchantments.get(customEnchantment)));
    }

    private void constructSoulbindEntry() {
        Player player = SoulbindEnchantment.getSoulboundPlayer(itemMeta);
        if (player == null) {
            soulbindInfo = ChatColorConverter.convert(ItemSettingsConfig.getNoSoulbindLore());
            return;
        }
        soulbindInfo = ChatColorConverter.convert(
                EnchantmentsConfig.getEnchantment("soulbind.yml")
                        .getFileConfiguration()
                        .getString("loreStrings")
                        .replace("$player", player.getDisplayName()));
    }

    private void constructPrestigeLevel() {
        this.prestigeLevel = SoulbindEnchantment.getPrestigeLevel(itemMeta);
    }

    private void constructItemWorth() {
        //Note: This writes a new value every time. Value might not have changed. This is not really an issue.
        itemStack.setItemMeta(itemMeta);
        ItemTagger.writeItemValue(itemStack, soulboundPlayer);
        itemMeta = itemStack.getItemMeta();
        if (showItemWorth)
            itemWorth = ItemSettingsConfig.getLoreWorth()
                    .replace("$worth", ItemWorthCalculator.determineItemWorth(itemStack, soulboundPlayer) + "")
                    .replace("$currencyName", EconomySettingsConfig.getCurrencyName());
        else
            itemWorth = ItemSettingsConfig.getLoreResale()
                    .replace("$resale", ItemWorthCalculator.determineResaleWorth(itemStack, soulboundPlayer) + "")
                    .replace("$currencyName", EconomySettingsConfig.getCurrencyName());
    }

    private void constructSoulboundOwner() {
        this.soulboundPlayer = SoulbindEnchantment.getSoulboundPlayer(itemMeta);
    }

    private void constructItemSource() {
        itemSource = ItemTagger.getItemSource(itemMeta);
    }

    private void constructCustomLore() {
        this.customLore = ItemTagger.getCustomLore(itemMeta);
    }

    private void constructPotionEffects() {
        for (ElitePotionEffect elitePotionEffect : ElitePotionEffectContainer.getElitePotionEffectContainer(itemMeta, ItemTagger.continuousPotionEffectKey))
            potionListLore.add(ChatColorConverter.convert(PotionEffectsConfig.getPotionEffect(
                    elitePotionEffect.getPotionEffect().getType().getName().toLowerCase() + ".yml").getName()
                    + ItemSettingsConfig.getPotionEffectContinuousLore() + " " + (elitePotionEffect.getPotionEffect().getAmplifier() + 1)));
        for (ElitePotionEffect elitePotionEffect : ElitePotionEffectContainer.getElitePotionEffectContainer(itemMeta, ItemTagger.onHitPotionEffectKey))
            if (elitePotionEffect.getTarget().equals(ElitePotionEffect.Target.SELF))
                potionListLore.add(ChatColorConverter.convert(PotionEffectsConfig.getPotionEffect(
                        elitePotionEffect.getPotionEffect().getType().getName().toLowerCase() + ".yml").getName()
                        + ItemSettingsConfig.getPotionEffectOnHitSelfLore() + " " + (elitePotionEffect.getPotionEffect().getAmplifier() + 1)));
            else
                potionListLore.add(ChatColorConverter.convert(PotionEffectsConfig.getPotionEffect(
                        elitePotionEffect.getPotionEffect().getType().getName().toLowerCase() + ".yml").getName()
                        + ItemSettingsConfig.getPotionEffectOnHitTargetLore() + " " + (elitePotionEffect.getPotionEffect().getAmplifier() + 1)));
    }

    private void writeNewLore() {
        for (String string : ItemSettingsConfig.getLoreStructure()) {

            if (string.contains("$weaponOrArmorStats")) {
                if (EliteItemManager.isWeapon(itemStack))
                    string = ItemSettingsConfig.getWeaponEntry();
                else if (EliteItemManager.isArmor(itemStack))
                    string = ItemSettingsConfig.getArmorEntry();
                else
                    string = "";
            }

            string = stringReplacer(string, "$EDPS", Round.twoDecimalPlaces(EliteItemManager.getDPS(itemStack)));
            string = stringReplacer(string, "$EDEF", Round.twoDecimalPlaces(EliteItemManager.getEliteDefense(itemStack) + EliteItemManager.getBonusEliteDefense(itemStack)));
            string = stringReplacer(string, "$prestigeLevel", prestigeLevel);
            string = stringReplacer(string, "$itemLevel", EliteItemManager.getRoundedItemLevel(itemStack));


            if (string.contains("$enchantments")) {
                for (String entry : vanillaEnchantmentsLore)
                    lore.add(ChatColorConverter.convert(ItemSettingsConfig.getVanillaEnchantmentColor() + entry));
            } else if (string.contains("$eliteEnchantments")) {
                for (String entry : eliteVanillaEnchantmentsLore)
                    lore.add(ChatColorConverter.convert(ItemSettingsConfig.getEliteEnchantmentColor() + ChatColor.stripColor(entry)));
            } else if (string.contains("$itemSource")) {
                if (itemSource != null)
                    lore.add(itemSource);
            } else if (string.contains("$potionEffect")) {
                for (String entry : potionListLore)
                    lore.add(ChatColorConverter.convert(ItemSettingsConfig.getPotionEffectColor() + entry));
            } else if (string.contains("$customEnchantments")) {
                for (String entry : customEnchantmentLore)
                    lore.add(ChatColorConverter.convert(ItemSettingsConfig.getCustomEnchantmentColor() + ChatColor.stripColor(entry)));
            } else if (string.contains("$loreResaleValue")) {
                lore.add(itemWorth);
            } else if (string.contains("$customLore")) {
                for (String entry : customLore)
                    lore.add(ChatColorConverter.convert(entry));
            } else if (string.contains("$soulbindInfo")) {
                lore.add(soulbindInfo);
                //bypasses in case of conditional formatting
            } else if (string.contains("$ifPotionEffects")) {
                if (!potionListLore.isEmpty())
                    lore.add(string.replace("$ifPotionEffects", ""));
            } else if (string.contains("$ifEnchantments")) {
                if (!vanillaEnchantmentsLore.isEmpty())
                    lore.add(string.replace("$ifEnchantments", ""));
            } else if (string.contains("$ifLore")) {
                if (!customLore.isEmpty())
                    lore.add(string.replace("$ifLore", ""));
            } else if (string.contains("$ifCustomEnchantments")) {
                if (!customEnchantments.isEmpty())
                    lore.add(string.replace("$ifCustomEnchantments", ""));
            } else if (!string.isEmpty())
                lore.add(ChatColorConverter.convert(string));
        }
    }

    private String stringReplacer(String originalString, String placeholder, int value) {
        return stringReplacer(originalString, placeholder, value + "");
    }

    private String stringReplacer(String originalString, String placeholder, double value) {
        return stringReplacer(originalString, placeholder, value + "");
    }

    private String stringReplacer(String originalString, String placeholder, String replacement) {
        String processedString = originalString;
        if (originalString.contains(placeholder))
            processedString = originalString.replace(placeholder, replacement);
        return processedString;
    }

}
