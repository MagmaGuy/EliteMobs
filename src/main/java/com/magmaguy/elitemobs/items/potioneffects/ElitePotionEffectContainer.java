package com.magmaguy.elitemobs.items.potioneffects;

import com.magmaguy.elitemobs.items.ItemTagger;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.ItemTagType;

import java.util.ArrayList;
import java.util.List;

public class ElitePotionEffectContainer {

    /**
     * Tags an item with the potion effects
     *
     * @param itemMeta
     * @param potionEffects
     */
    public ElitePotionEffectContainer(ItemMeta itemMeta, List<String> potionEffects) {

        if (potionEffects.isEmpty()) return;

        StringBuilder onHit = new StringBuilder();
        StringBuilder continuous = new StringBuilder();

        for (String substring : potionEffects) {
            ElitePotionEffect elitePotionEffect = new ElitePotionEffect(substring);
            switch (elitePotionEffect.getApplicationMethod()) {
                case ONHIT:
                    onHit.append(substring).append(":");
                    break;
                case CONTINUOUS:
                default:
                    continuous.append(substring).append(":");
                    break;
            }
        }

        if (onHit.length() > 0)
            itemMeta.getCustomTagContainer().setCustomTag(ItemTagger.onHitPotionEffectKey, ItemTagType.STRING, onHit.toString());
        if (continuous.length() > 0)
            itemMeta.getCustomTagContainer().setCustomTag(ItemTagger.continuousPotionEffectKey, ItemTagType.STRING, continuous.toString());

    }

    public static ArrayList<ElitePotionEffect> getElitePotionEffectContainer(ItemMeta itemMeta, NamespacedKey namespacedKey) {
        if (!itemMeta.getCustomTagContainer().hasCustomTag(namespacedKey, ItemTagType.STRING)) return null;
        ArrayList<ElitePotionEffect> elitePotionEffects = new ArrayList<>();
        for (String string : itemMeta.getCustomTagContainer().getCustomTag(namespacedKey, ItemTagType.STRING).split(":"))
            if (string.length() > 0)
                elitePotionEffects.add(new ElitePotionEffect(string));

        return elitePotionEffects;
    }

    public static ArrayList<ElitePotionEffect> getElitePotionEffects(ItemMeta itemMeta, ElitePotionEffect.ApplicationMethod applicationMethod) {
        String enchantmentsString = "";
        switch (applicationMethod) {
            case CONTINUOUS:
                enchantmentsString = itemMeta.getCustomTagContainer().getCustomTag(ItemTagger.continuousPotionEffectKey, ItemTagType.STRING);
                break;
            case ONHIT:
                enchantmentsString = itemMeta.getCustomTagContainer().getCustomTag(ItemTagger.onHitPotionEffectKey, ItemTagType.STRING);
                break;
        }
        ArrayList<ElitePotionEffect> elitePotionEffects = new ArrayList<>();
        for (String subString : enchantmentsString.split(":"))
            elitePotionEffects.add(new ElitePotionEffect(subString));
        return elitePotionEffects;
    }

}
