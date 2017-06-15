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

package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.elitedrops.RandomItemGenerator;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Created by MagmaGuy on 08/06/2017.
 */
public class SimLootHandler {

    public void simLoot (Player player, int level) {

        Random random = new Random();

        level = (int) ((level * ConfigValues.randomItemsConfig.getDouble("Mob level to item rank multiplier")) + (random.nextInt(6) + 1 - 3));

        if (level < 1) {

            level = 0;

        }

        RandomItemGenerator randomItemGenerator = new RandomItemGenerator();

        ItemStack randomItem = randomItemGenerator.randomItemGeneratorCommand(level);

        player.getInventory().addItem(randomItem);

    }

}
