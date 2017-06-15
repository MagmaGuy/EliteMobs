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

package com.magmaguy.elitemobs.mobs.passive;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.elitedrops.ItemDropVelocity;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Material.*;

/**
 * Created by MagmaGuy on 19/12/2016.
 */
public class SheepHandler implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void superDrops(EntityDamageByEntityEvent event) {

        if (event.getEntity().hasMetadata(MetadataHandler.PASSIVE_ELITE_MOB_MD) && event.getEntity() instanceof Sheep) {

            Random random = new Random();

            Sheep sheep = (Sheep) event.getEntity();

            double damage = event.getFinalDamage();
            //health is hardcoded here, maybe change it at some point?
            double dropChance = damage / 8;
            double dropRandomizer = random.nextDouble();
            //this rounds down
            int dropMinAmount = (int) dropChance;

            ItemStack muttonStack = new ItemStack(MUTTON, random.nextInt(2) + 1);
            ItemStack woolStack = new ItemStack(WOOL, 1);

            //for different wool colors
            switch (sheep.getColor()) {
                case WHITE:
                    woolStack = new ItemStack(WOOL, 1);
                    break;
                case ORANGE:
                    woolStack = new ItemStack(WOOL, 1, (short) 1);
                    break;
                case MAGENTA:
                    woolStack = new ItemStack(WOOL, 1, (short) 2);
                    break;
                case LIGHT_BLUE:
                    woolStack = new ItemStack(WOOL, 1, (short) 3);
                    break;
                case YELLOW:
                    woolStack = new ItemStack(WOOL, 1, (short) 4);
                    break;
                case LIME:
                    woolStack = new ItemStack(WOOL, 1, (short) 5);
                    break;
                case PINK:
                    woolStack = new ItemStack(WOOL, 1, (short) 6);
                    break;
                case GRAY:
                    woolStack = new ItemStack(WOOL, 1, (short) 7);
                    break;
                case SILVER:
                    woolStack = new ItemStack(WOOL, 1, (short) 8);
                    break;
                case CYAN:
                    woolStack = new ItemStack(WOOL, 1, (short) 9);
                    break;
                case PURPLE:
                    woolStack = new ItemStack(WOOL, 1, (short) 10);
                    break;
                case BLUE:
                    woolStack = new ItemStack(WOOL, 1, (short) 11);
                    break;
                case BROWN:
                    woolStack = new ItemStack(WOOL, 1, (short) 12);
                    break;
                case GREEN:
                    woolStack = new ItemStack(WOOL, 1, (short) 13);
                    break;
                case RED:
                    woolStack = new ItemStack(WOOL, 1, (short) 14);
                    break;
                case BLACK:
                    woolStack = new ItemStack(WOOL, 1, (short) 15);
                    break;
                default:
                    getLogger().info("Something went wrong with the sheep colors, one is missing. Contact the dev.");
                    break;
            }

            for (int i = 0; i < dropMinAmount; i++) {

                sheep.getWorld().dropItem(sheep.getLocation(), muttonStack).setVelocity(ItemDropVelocity.ItemDropVelocity());
                sheep.getWorld().dropItem(sheep.getLocation(), woolStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

            }

            if (dropChance > dropRandomizer) {

                sheep.getWorld().dropItem(sheep.getLocation(), muttonStack).setVelocity(ItemDropVelocity.ItemDropVelocity());
                sheep.getWorld().dropItem(sheep.getLocation(), woolStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

            }

        }

    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {

        if (event.getEntity() instanceof Sheep && event.getEntity().hasMetadata(MetadataHandler.PASSIVE_ELITE_MOB_MD)) {

            Sheep sheep = (Sheep) event.getEntity();

            ItemStack sheepMonsterEgg = new ItemStack(MONSTER_EGG, 2, (short) 91);
            sheep.getWorld().dropItem(sheep.getLocation(), sheepMonsterEgg);

        }

    }

    @EventHandler
    public void onShear(PlayerShearEntityEvent event) {

        if (event.getEntity() instanceof Sheep && event.getEntity().hasMetadata(MetadataHandler.PASSIVE_ELITE_MOB_MD)) {

            Sheep sheep = (Sheep) event.getEntity();

            for (int i = 0; i < 50; i++) {

                sheep.getWorld().dropItem(sheep.getLocation(), woolStackRandomizer(sheep)).setVelocity(ItemDropVelocity.ItemDropVelocity());

            }

        }

    }

    public ItemStack woolStackRandomizer(Sheep sheep) {


        Random random = new Random();

        ItemStack woolStack = new ItemStack(WOOL, ((random.nextInt(3) + 1) * 50));

        switch (sheep.getColor()) {
            case WHITE:
                woolStack = new ItemStack(WOOL, (random.nextInt(3) + 1));
                break;
            case ORANGE:
                woolStack = new ItemStack(WOOL, ((random.nextInt(3) + 1)), (short) 1);
                break;
            case MAGENTA:
                woolStack = new ItemStack(WOOL, ((random.nextInt(3) + 1)), (short) 2);
                break;
            case LIGHT_BLUE:
                woolStack = new ItemStack(WOOL, ((random.nextInt(3) + 1)), (short) 3);
                break;
            case YELLOW:
                woolStack = new ItemStack(WOOL, ((random.nextInt(3) + 1)), (short) 4);
                break;
            case LIME:
                woolStack = new ItemStack(WOOL, ((random.nextInt(3) + 1)), (short) 5);
                break;
            case PINK:
                woolStack = new ItemStack(WOOL, ((random.nextInt(3) + 1)), (short) 6);
                break;
            case GRAY:
                woolStack = new ItemStack(WOOL, ((random.nextInt(3) + 1)), (short) 7);
                break;
            case SILVER:
                woolStack = new ItemStack(WOOL, ((random.nextInt(3) + 1)), (short) 8);
                break;
            case CYAN:
                woolStack = new ItemStack(WOOL, ((random.nextInt(3) + 1)), (short) 9);
                break;
            case PURPLE:
                woolStack = new ItemStack(WOOL, ((random.nextInt(3) + 1)), (short) 10);
                break;
            case BLUE:
                woolStack = new ItemStack(WOOL, ((random.nextInt(3) + 1)), (short) 11);
                break;
            case BROWN:
                woolStack = new ItemStack(WOOL, ((random.nextInt(3) + 1)), (short) 12);
                break;
            case GREEN:
                woolStack = new ItemStack(WOOL, ((random.nextInt(3) + 1)), (short) 13);
                break;
            case RED:
                woolStack = new ItemStack(WOOL, ((random.nextInt(3) + 1)), (short) 14);
                break;
            case BLACK:
                woolStack = new ItemStack(WOOL, ((random.nextInt(3) + 1)), (short) 15);
                break;
            default:
                getLogger().info("Something went wrong with the sheep colors, one is missing. Contact the dev.");
                break;
        }

        return woolStack;

    }

}
