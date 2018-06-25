/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.magmaguy.elitemobs.items;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ObfuscatedSignatureLoreData {

    public static final String ITEM_SIGNATURE = "Elite Drop";

    public static void obfuscateSignatureData(ItemStack itemStack) {

        ItemMeta itemMeta = itemStack.getItemMeta();
        String obfuscatedItemLore = loreObfuscator(ITEM_SIGNATURE);
        List<String> lore = new ArrayList<>();

        if (itemMeta.getLore() != null) {

            String hiddenLoreConcatenation = itemMeta.getLore().get(0) + obfuscatedItemLore;

            for (String string : itemMeta.getLore()) {

                if (string.equals(itemMeta.getLore().get(0))) {

                    lore.add(hiddenLoreConcatenation);

                } else {

                    lore.add(string);

                }

            }

        } else {

            lore.add(obfuscatedItemLore);

        }

        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);

    }

    public static String loreObfuscator(String textToObfuscate) {

        String insert = "§";
        int period = 1;

        StringBuilder stringBuilder = new StringBuilder(textToObfuscate.length() + insert.length() * (textToObfuscate.length() / period) + 0);

        int index = 0;
        String prefix = "";

        while (index < textToObfuscate.length()) {

            stringBuilder.append(prefix);
            prefix = insert;
            stringBuilder.append(textToObfuscate.substring(index, Math.min(index + period, textToObfuscate.length())));
            index += period;

        }

        String finalString = stringBuilder.toString();

        finalString = " §" + finalString;

        return finalString;

    }

    public static Boolean obfuscatedSignatureDetector(ItemStack itemStack) {

        if (!itemStack.getItemMeta().hasLore()) return false;

        for (String string : itemStack.getItemMeta().getLore()) {

            if (string.contains(loreObfuscator(ITEM_SIGNATURE))) {

                return true;

            }

        }

        return false;

    }

}
