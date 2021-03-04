package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.api.EliteMobsItemDetector;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.config.potioneffects.PotionEffectsConfig;
import com.magmaguy.elitemobs.items.customenchantments.CustomEnchantment;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffect;
import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffectContainer;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EliteItemLore {

    ItemStack itemStack;
    ItemMeta itemMeta;
    ArrayList<String> vanillaEnchantmentsLore = new ArrayList<>();
    HashMap<Enchantment, Integer> eliteVanillaEnchantments = new HashMap<>();
    ArrayList<String> eliteVanillaEnchantmentsLore = new ArrayList<>();
    HashMap<CustomEnchantment, Integer> customEnchantments = new HashMap<>();
    ArrayList<String> customEnchantmentLore = new ArrayList<>();
    List<String> potionListLore = new ArrayList<>();
    List<String> lore;
    String soulbindInfo = null;
    boolean showItemWorth;
    String itemWorth = null;
    String itemSource = null;
    EliteMobEntity eliteMobEntity;
    Player soulboundPlayer = null;
    List<String> customLore = new ArrayList<>();
    int prestigeLevel = 0;


    public EliteItemLore(ItemStack itemStack, boolean showItemWorth) {

        if (!EliteMobsItemDetector.isEliteMobsItem(itemStack)) {
            new WarningMessage("Attempted to rewrite the lore of a non-elitemobs item! This is not supposed to happen.");
            return;
        }

        this.itemStack = itemStack;
        this.itemMeta = itemStack.getItemMeta();
        this.lore = new ArrayList<>();
        this.showItemWorth = showItemWorth;

        constructVanillaEnchantments();

        parseAllEliteEnchantments();
        constructEliteEnchantments();

        parseCustomEnchantments();
        constructCustomEnchantments();

        constructSoulbindEntry();
        constructSoulboundOwner();
        constructItemWorth();
        constructItemSource();
        constructCustomLore();
        constructPrestigeLevel();

        constructPotionEffects();

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
        parseEliteEnchantments(Enchantment.DAMAGE_ARTHROPODS);
        parseEliteEnchantments(Enchantment.DAMAGE_UNDEAD);
        parseEliteEnchantments(Enchantment.PROTECTION_ENVIRONMENTAL);
        parseEliteEnchantments(Enchantment.PROTECTION_PROJECTILE);
        parseEliteEnchantments(Enchantment.PROTECTION_EXPLOSIONS);
    }

    private void parseEliteEnchantments(Enchantment enchantment) {
        int enchantmentLevel = ItemTagger.getEnchantment(itemMeta, enchantment.getKey());
        if (enchantmentLevel > enchantment.getMaxLevel())
            eliteVanillaEnchantments.put(enchantment, enchantmentLevel - enchantment.getMaxLevel());
    }

    private void constructEliteEnchantments() {
        for (Enchantment enchantment : eliteVanillaEnchantments.keySet())
            eliteVanillaEnchantmentsLore.add(ChatColorConverter.convert(
                    "&7" + ItemSettingsConfig.eliteEnchantLoreString + " "
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
            soulbindInfo = ChatColorConverter.convert(ItemSettingsConfig.noSoulbindLore);
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
            itemWorth = ItemSettingsConfig.loreWorth
                    .replace("$worth", ItemWorthCalculator.determineItemWorth(itemStack, soulboundPlayer) + "")
                    .replace("$currencyName", EconomySettingsConfig.currencyName);
        else
            itemWorth = ItemSettingsConfig.loreResale
                    .replace("$resale", ItemWorthCalculator.determineResaleWorth(itemStack, soulboundPlayer) + "")
                    .replace("$currencyName", EconomySettingsConfig.currencyName);
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
                    + ItemSettingsConfig.potionEffectContinuousLore + " " + (elitePotionEffect.getPotionEffect().getAmplifier() + 1)));
        for (ElitePotionEffect elitePotionEffect : ElitePotionEffectContainer.getElitePotionEffectContainer(itemMeta, ItemTagger.onHitPotionEffectKey))
            if (elitePotionEffect.getTarget().equals(ElitePotionEffect.Target.SELF))
                potionListLore.add(ChatColorConverter.convert(PotionEffectsConfig.getPotionEffect(
                        elitePotionEffect.getPotionEffect().getType().getName().toLowerCase() + ".yml").getName()
                        + ItemSettingsConfig.potionEffectOnHitSelfLore + " " + (elitePotionEffect.getPotionEffect().getAmplifier() + 1)));
            else
                potionListLore.add(ChatColorConverter.convert(PotionEffectsConfig.getPotionEffect(
                        elitePotionEffect.getPotionEffect().getType().getName().toLowerCase() + ".yml").getName()
                        + ItemSettingsConfig.potionEffectOnHitTargetLore + " " + (elitePotionEffect.getPotionEffect().getAmplifier() + 1)));
    }

    private void writeNewLore() {
        for (String string : ItemSettingsConfig.loreStructure) {

            if (string.contains("$prestigeLevel"))
                string = string.replace("$prestigeLevel", prestigeLevel + "");

            if (string.contains("$itemLevel"))
                string = string.replace("$itemLevel", ItemTierFinder.findBattleTier(itemStack) + "");

            if (string.contains("$enchantments")) {
                for (String entry : vanillaEnchantmentsLore)
                    lore.add(ChatColorConverter.convert(ItemSettingsConfig.vanillaEnchantmentColor + entry));
            } else if (string.contains("$eliteEnchantments")) {
                for (String entry : eliteVanillaEnchantmentsLore)
                    lore.add(ChatColorConverter.convert(ItemSettingsConfig.eliteEnchantmentColor + ChatColor.stripColor(entry)));
            } else if (string.contains("$itemSource")) {
                if (itemSource != null)
                    lore.add(itemSource);
            } else if (string.contains("$potionEffect")) {
                for (String entry : potionListLore)
                    lore.add(ChatColorConverter.convert(ItemSettingsConfig.potionEffectColor + entry));
            } else if (string.contains("$customEnchantments")) {
                for (String entry : customEnchantmentLore)
                    lore.add(ChatColorConverter.convert(ItemSettingsConfig.customEnchantmentColor + ChatColor.stripColor(entry)));
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
            } else
                lore.add(ChatColorConverter.convert(string));

        }
    }

}
