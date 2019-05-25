package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructor;
import com.magmaguy.elitemobs.items.parserutil.CustomEnchantmentConfigParser;
import com.magmaguy.elitemobs.items.parserutil.DropWeightConfigParser;
import com.magmaguy.elitemobs.items.parserutil.PotionEffectConfigParser;
import com.magmaguy.elitemobs.items.parserutil.ScalabilityConfigParser;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.magmaguy.elitemobs.ChatColorConverter.convert;
import static com.magmaguy.elitemobs.items.parserutil.ConfigPathBuilder.automatedStringBuilder;
import static com.magmaguy.elitemobs.items.parserutil.EnchantmentConfigParser.parseEnchantments;

/**
 * Created by MagmaGuy on 29/11/2016.
 */
public class CustomItemConstructor implements Listener {

    public static ArrayList<ItemStack> customItemList = new ArrayList();
    public static HashMap<ItemStack, Double> staticCustomItemHashMap = new HashMap<>();
    public static HashMap<Integer, List<ItemStack>> dynamicRankedItemStacks = new HashMap<>();

    public void superDropParser() {

        List<String> lootCount = lootCounter();

        for (String lootEntry : lootCount) {

            StringBuilder path = new StringBuilder();
            path.append(lootEntry);

            String previousPath = path.toString();

            Material material = getMaterial(previousPath);
            Bukkit.getLogger().info("Adding: " + previousPath);
            String rawName = getName(previousPath);

            if (material == null)
                continue;

            Configuration configuration = ConfigValues.itemsCustomLootListConfig;

            HashMap<Enchantment, Integer> enchantments = parseEnchantments(configuration, previousPath);
            HashMap<String, Integer> customEnchantments = CustomEnchantmentConfigParser.parseCustomEnchantments(configuration, previousPath);
            List potionEffects = PotionEffectConfigParser.itemPotionEffectHandler(configuration, previousPath);
            List<String> lore = getCustomLore(previousPath);
            String dropType = DropWeightConfigParser.getDropType(configuration, previousPath);
            String scalability = ScalabilityConfigParser.parseItemScalability(configuration, previousPath);

            ItemStack itemStack = ItemConstructor.constructItem(rawName, material, enchantments, customEnchantments,
                    potionEffects, lore, dropType, scalability);

            customItemList.add(itemStack);

        }

    }

    private List<String> lootCounter() {

        List<String> lootCount = new ArrayList();

        for (String configIterator : ConfigValues.itemsCustomLootListConfig.getKeys(true)) {

            int dotCount = 0;

            if (configIterator.contains("Loot.")) {

                for (int i = 0; i < configIterator.length(); i++)
                    if (configIterator.charAt(i) == '.')
                        dotCount++;

                if (dotCount == 1)
                    lootCount.add(configIterator);

            }

        }

        return lootCount;

    }

    private Material getMaterial(String previousPath) {

        String path = automatedStringBuilder(previousPath, "Item Type");

        Material material;

        try {

            material = Material.getMaterial(ConfigValues.itemsCustomLootListConfig.getString(path));

        } catch (Error error) {

            Bukkit.getLogger().warning("[EliteMobs] Invalid material!");
            return null;

        }


        return material;

    }

    private String getName(String previousPath) {

        String name = automatedStringBuilder(previousPath, "Item Name");

        if (ConfigValues.itemsCustomLootListConfig.getString(name) == null || ConfigValues.itemsCustomLootListConfig.getString(name).isEmpty())
            Bukkit.getLogger().warning("[EliteMobs] Invalid name!");

        return convert(ConfigValues.itemsCustomLootListConfig.getString(name));

    }

    private List<String> getCustomLore(String previousPath) {

        String path = automatedStringBuilder(previousPath, "Item Lore");

        List<String> itemLore = (List<String>) ConfigValues.itemsCustomLootListConfig.getList(path);

        List<String> newList = new ArrayList<>();

        if (itemLore != null && !itemLore.isEmpty())
            for (String string : itemLore)
                if (string != null && !string.isEmpty())
                    newList.add(convert(string));

        if (newList.isEmpty())
            return itemLore;

        return newList;

    }

}
