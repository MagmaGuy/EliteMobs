package com.magmaguy.elitemobs.items.potioneffects;

import com.magmaguy.elitemobs.items.ItemTagger;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

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
            itemMeta.getPersistentDataContainer().set(ItemTagger.onHitPotionEffectKey, PersistentDataType.STRING, onHit.toString());
        if (continuous.length() > 0)
            itemMeta.getPersistentDataContainer().set(ItemTagger.continuousPotionEffectKey, PersistentDataType.STRING, continuous.toString());

    }

    public static ArrayList<ElitePotionEffect> getElitePotionEffectContainer(ItemMeta itemMeta, NamespacedKey namespacedKey) {
        if (!itemMeta.getPersistentDataContainer().has(namespacedKey, PersistentDataType.STRING)) return null;
        ArrayList<ElitePotionEffect> elitePotionEffects = new ArrayList<>();
        for (String string : itemMeta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING).split(":"))
            if (string.length() > 0)
                elitePotionEffects.add(new ElitePotionEffect(string));

        return elitePotionEffects;
    }

    public static ArrayList<ElitePotionEffect> getElitePotionEffects(ItemMeta itemMeta, ElitePotionEffect.ApplicationMethod applicationMethod) {
        String elitePotionEfects = "";
        switch (applicationMethod) {
            case CONTINUOUS:
                elitePotionEfects = itemMeta.getPersistentDataContainer().get(ItemTagger.continuousPotionEffectKey, PersistentDataType.STRING);
                break;
            case ONHIT:
                elitePotionEfects = itemMeta.getPersistentDataContainer().get(ItemTagger.onHitPotionEffectKey, PersistentDataType.STRING);
                break;
        }
        ArrayList<ElitePotionEffect> elitePotionEffects = new ArrayList<>();
        if (elitePotionEfects == null || elitePotionEfects.isEmpty())
            return elitePotionEffects;
        for (String subString : elitePotionEfects.split(":"))
            elitePotionEffects.add(new ElitePotionEffect(subString));
        return elitePotionEffects;
    }

}
