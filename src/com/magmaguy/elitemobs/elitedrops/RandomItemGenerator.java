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

package com.magmaguy.elitemobs.elitedrops;

import java.util.Random;

/**
 * Created by MagmaGuy on 04/06/2017.
 */
public class RandomItemGenerator {

    Random random = new Random();

//    public ItemStack randomItemGenerator(int rankLevel) {
//
//        ItemStack randomItem = new ItemStack(randomMaterialConstructor(), 1);
//
//    }
//
//    private Material randomMaterialConstructor(){
//
//        List<Material> validMaterials = new ArrayList<>();
//
//        for (Object object : ConfigValues.randomItemsConfig.getList("Valid material list for random items")) {
//
//            try {
//
//                Material parsedMaterial = Material.getMaterial(object.toString());
//                validMaterials.add(parsedMaterial);
//
//            } catch (Exception e) {
//
//                Bukkit.getLogger().info("Invalid material type detected: " + object.toString());
//
//            }
//
//
//        }
//
//        validMaterials.addAll((Collection<? extends Material>) ConfigValues.randomItemsConfig.getList("Valid material list for random items"));
//
//        int index = random.nextInt(validMaterials.size()) + 1;
//
//        Material material = validMaterials.get(index);
//
//    }
//
//    private String randomItemNameConstructor(){
//
//    }
//
//    private List<String> randomItemLoreConstructor(){
//
//
//    }
//
//    private void randomItemEnchantmentConstructor(){
//
//    }

}
