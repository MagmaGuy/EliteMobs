package com.magmaguy.elitemobs.mobs.passive;

import com.magmaguy.elitemobs.api.SuperMobDamageEvent;
import com.magmaguy.elitemobs.api.SuperMobDeathEvent;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.items.ItemDropVelocity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Material.*;

/**
 * Created by MagmaGuy on 19/12/2016.
 */
public class SheepHandler implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void superDrops(SuperMobDamageEvent event) {

        if (event.getEntityDamageEvent().getFinalDamage() < 1)
            return;

        if (!event.getLivingEntity().getType().equals(EntityType.SHEEP)) return;

        Random random = new Random();

        Sheep sheep = (Sheep) event.getLivingEntity();

        double damage = event.getEntityDamageEvent().getFinalDamage();
        //health is hardcoded here, maybe change it at some point?
        double dropChance = damage / 8;
        double dropRandomizer = random.nextDouble();
        //this rounds down
        int dropMinAmount = (int) dropChance;

        ItemStack muttonStack = new ItemStack(MUTTON, random.nextInt(2) + 1);
        ItemStack woolStack = new ItemStack(WHITE_WOOL, 1);

        //for different wool colors
        switch (sheep.getColor()) {
            case WHITE:
                woolStack = new ItemStack(WHITE_WOOL, 1);
                break;
            case ORANGE:
                woolStack = new ItemStack(ORANGE_WOOL, 1);
                break;
            case MAGENTA:
                woolStack = new ItemStack(MAGENTA_WOOL, 1);
                break;
            case LIGHT_BLUE:
                woolStack = new ItemStack(LIGHT_BLUE_WOOL, 1);
                break;
            case YELLOW:
                woolStack = new ItemStack(YELLOW_WOOL, 1);
                break;
            case LIME:
                woolStack = new ItemStack(LIME_WOOL, 1);
                break;
            case PINK:
                woolStack = new ItemStack(PINK_WOOL, 1);
                break;
            case GRAY:
                woolStack = new ItemStack(GRAY_WOOL, 1);
                break;
            case LIGHT_GRAY:
                woolStack = new ItemStack(LIGHT_GRAY_WOOL, 1);
                break;
            case CYAN:
                woolStack = new ItemStack(CYAN_WOOL, 1);
                break;
            case PURPLE:
                woolStack = new ItemStack(PURPLE_WOOL, 1);
                break;
            case BLUE:
                woolStack = new ItemStack(BLUE_WOOL, 1);
                break;
            case BROWN:
                woolStack = new ItemStack(BROWN_WOOL, 1);
                break;
            case GREEN:
                woolStack = new ItemStack(GREEN_WOOL, 1);
                break;
            case RED:
                woolStack = new ItemStack(RED_WOOL, 1);
                break;
            case BLACK:
                woolStack = new ItemStack(BLACK_WOOL, 1);
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

    @EventHandler(ignoreCancelled = true)
    public void onDeath(SuperMobDeathEvent event) {
        if (!event.getLivingEntity().getType().equals(EntityType.SHEEP)) return;
        Sheep sheep = (Sheep) event.getLivingEntity();
        ItemStack sheepMonsterEgg = new ItemStack(SHEEP_SPAWN_EGG, 2);
        sheep.getWorld().dropItem(sheep.getLocation(), sheepMonsterEgg);
    }

    @EventHandler(ignoreCancelled = true)
    public void onShear(PlayerShearEntityEvent event) {
        if (!event.getEntity().getType().equals(EntityType.SHEEP)) return;
        if (!EntityTracker.isSuperMob(event.getEntity())) return;
        Sheep sheep = (Sheep) event.getEntity();

        for (int i = 0; i < 50; i++)
            sheep.getWorld().dropItem(sheep.getLocation(), woolStackRandomizer(sheep)).setVelocity(ItemDropVelocity.ItemDropVelocity());
    }

    public ItemStack woolStackRandomizer(Sheep sheep) {

        Random random = new Random();

        ItemStack woolStack = new ItemStack(WHITE_WOOL, ((random.nextInt(3) + 1) * 50));

        switch (sheep.getColor()) {
            case WHITE:
                woolStack = new ItemStack(WHITE_WOOL, (random.nextInt(3) + 1));
                break;
            case ORANGE:
                woolStack = new ItemStack(ORANGE_WOOL, ((random.nextInt(3) + 1)));
                break;
            case MAGENTA:
                woolStack = new ItemStack(MAGENTA_WOOL, ((random.nextInt(3) + 1)));
                break;
            case LIGHT_BLUE:
                woolStack = new ItemStack(LIGHT_BLUE_WOOL, ((random.nextInt(3) + 1)));
                break;
            case YELLOW:
                woolStack = new ItemStack(YELLOW_WOOL, ((random.nextInt(3) + 1)));
                break;
            case LIME:
                woolStack = new ItemStack(LIME_WOOL, ((random.nextInt(3) + 1)));
                break;
            case PINK:
                woolStack = new ItemStack(PINK_WOOL, ((random.nextInt(3) + 1)));
                break;
            case GRAY:
                woolStack = new ItemStack(GRAY_WOOL, ((random.nextInt(3) + 1)));
                break;
            case LIGHT_GRAY:
                woolStack = new ItemStack(LIGHT_GRAY_WOOL, ((random.nextInt(3) + 1)));
                break;
            case CYAN:
                woolStack = new ItemStack(CYAN_WOOL, ((random.nextInt(3) + 1)));
                break;
            case PURPLE:
                woolStack = new ItemStack(PURPLE_WOOL, ((random.nextInt(3) + 1)));
                break;
            case BLUE:
                woolStack = new ItemStack(BLUE_WOOL, ((random.nextInt(3) + 1)));
                break;
            case BROWN:
                woolStack = new ItemStack(BROWN_WOOL, ((random.nextInt(3) + 1)));
                break;
            case GREEN:
                woolStack = new ItemStack(GREEN_WOOL, ((random.nextInt(3) + 1)));
                break;
            case RED:
                woolStack = new ItemStack(RED_WOOL, ((random.nextInt(3) + 1)));
                break;
            case BLACK:
                woolStack = new ItemStack(BLACK_WOOL, ((random.nextInt(3) + 1)));
                break;
            default:
                getLogger().info("Something went wrong with the sheep colors, one is missing. Contact the dev.");
                break;
        }

        return woolStack;

    }

}
