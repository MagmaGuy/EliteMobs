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

package com.magmaguy.elitemobs.commands.guiconfig;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.DefaultConfig;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MagmaGuy on 12/11/2017.
 */
public class SignatureItem {

    public static final ItemStack SIGNATURE_ITEMSTACK = setSignatureItem();

    private static ItemStack setSignatureItem() {

        ItemStack signature;
        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.SKULL_SIGNATURE_ITEM)) {
            signature = new ItemStack(Material.LEGACY_SKULL_ITEM, 1, (short) 3);
            SkullMeta signatureSkullMeta = (SkullMeta) signature.getItemMeta();
            signatureSkullMeta.setOwner("magmaguy");
            signatureSkullMeta.setDisplayName("EliteMobs by MagmaGuy");
            List<String> signatureList = new ArrayList<>();
            signatureList.add("Support the plugins you enjoy!");
            signatureSkullMeta.setLore(signatureList);
            signature.setItemMeta(signatureSkullMeta);
        } else {
            signature = new ItemStack(Material.PAPER);
            ItemMeta itemMeta = signature.getItemMeta();
            itemMeta.setDisplayName("EliteMobs by MagmaGuy");
            List<String> signatureList = new ArrayList<>();
            signatureList.add("Support the plugins you enjoy!");
            itemMeta.setLore(signatureList);
            signature.setItemMeta(itemMeta);
        }

        return signature;

    }

}
