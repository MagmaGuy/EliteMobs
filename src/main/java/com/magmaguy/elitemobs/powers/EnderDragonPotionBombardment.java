package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.Bombardment;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;


public class EnderDragonPotionBombardment extends Bombardment {

    public EnderDragonPotionBombardment() {
        super(PowersConfig.getPower("ender_dragon_potion_bombardment.yml"));
    }

    @Override
    public void taskBehavior(EliteEntity eliteEntity) {
        for (int i = 0; i < 5; i++) {

            ItemStack itemStack = new ItemStack(Material.SPLASH_POTION);
            PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
            if (ThreadLocalRandom.current().nextBoolean())
                potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.HARM, 0, 1), true);
            else potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.SLOW, 0, 1), true);

            itemStack.setItemMeta(potionMeta);


            ThrownPotion thrownPotion = (ThrownPotion) eliteEntity.getLivingEntity().getWorld().spawnEntity(
                    eliteEntity.getLivingEntity().getLocation().clone().subtract(
                            new Vector(ThreadLocalRandom.current().nextInt(-1, 1),
                                    1,
                                    ThreadLocalRandom.current().nextInt(-1, 1)))
                    , EntityType.SPLASH_POTION);

            thrownPotion.setItem(itemStack);

            thrownPotion.setVelocity(
                    new Vector(ThreadLocalRandom.current().nextDouble() - 0.5,
                            0,
                            ThreadLocalRandom.current().nextDouble() - 0.5));

        }
    }

}
