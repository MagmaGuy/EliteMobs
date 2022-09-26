package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.events.BossCustomAttackDamage;
import com.magmaguy.elitemobs.explosionregen.Explosion;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.CombatEnterScanPower;
import com.magmaguy.elitemobs.utils.EnderDragonPhaseSimplifier;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class EnderDragonShockwave extends CombatEnterScanPower {

    private final int radius = 30;
    //this is structured like this because the relative block generator is moving out of this class
    private final ArrayList<PieBlock> realBlocks = new ArrayList<>();
    private ArrayList<PieBlock> pieBlocks = new ArrayList<>();
    private int warningPhaseCounter = 0;
    private int damagePhaseCounter = 0;

    public EnderDragonShockwave() {
        super(PowersConfig.getPower("ender_dragon_shockwave.yml"));
    }

    @Override
    protected void finishActivation(EliteEntity eliteEntity) {
        super.bukkitTask = new BukkitRunnable() {

            @Override
            public void run() {
                if (doExit(eliteEntity) || isInCooldown(eliteEntity)) {
                    return;
                }

                if (eliteEntity.getLivingEntity().getType().equals(EntityType.ENDER_DRAGON)) {
                    EnderDragon.Phase phase = ((EnderDragon) eliteEntity.getLivingEntity()).getPhase();
                    if (!EnderDragonPhaseSimplifier.isLanded(phase)) return;
                }

                doPower(eliteEntity);

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 10);
    }

    private void doPower(EliteEntity eliteEntity) {
        doCooldown(eliteEntity);

        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {

                if (eliteEntity.isValid())
                    if (eliteEntity.getLivingEntity().getType().equals(EntityType.ENDER_DRAGON))
                        ((EnderDragon) eliteEntity.getLivingEntity()).setPhase(EnderDragon.Phase.SEARCH_FOR_BREATH_ATTACK_TARGET);

                if (doExit(eliteEntity) || eliteEntity.getLivingEntity().getType().equals(EntityType.ENDER_DRAGON) &&
                        !EnderDragonPhaseSimplifier.isLanded(((EnderDragon) eliteEntity.getLivingEntity()).getPhase())) {
                    cancel();
                    return;
                }

                if (counter == 0) {
                    setAffectedBlocks();
                    generateRealCircle(eliteEntity);
                    warningPhaseCounter = 0;
                    damagePhaseCounter = 0;
                }

                if (counter < 20 * 3) {
                    doWarningPhase(eliteEntity);
                }

                if (counter > 20 * 3) {
                    doDamagePhase(eliteEntity);
                }

                if (counter >= 20 * (3 + 6)) {
                    cancel();
                }

                counter++;
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    //todo: move this to its own class, make sure it only generates once ever and then just randomizes rotations
    private void setAffectedBlocks() {
        pieBlocks = new ArrayList<>();
        double rotation = ThreadLocalRandom.current().nextDouble(2);
        Vector relative0 = new Vector(0, 0, 0);
        for (int x = -radius; x < radius; x++)
            for (int z = -radius; z < radius; z++) {
                if (x > 0 && z > 0) continue;
                double distance = relative0.distance(new Vector(x, 0, z));
                if (distance > radius) continue;

                pieBlocks.add(new PieBlock((int) Math.ceil(distance), new Vector(x, 0, z).rotateAroundY(rotation)));
            }

    }

    private void generateRealCircle(EliteEntity eliteEntity) {
        for (PieBlock pieBlock : pieBlocks) {
            Location rawPieBlock = eliteEntity.getLivingEntity().getLocation().clone().add(pieBlock.vector);
            for (int y = 0; y > -10; y--) {
                Location tempLocation = rawPieBlock.clone();
                tempLocation.setY(rawPieBlock.getY() + y);
                if (!tempLocation.getBlock().isPassable()) {
                    Vector newVector = pieBlock.vector.clone();
                    newVector.setY(newVector.getY() + y);
                    realBlocks.add(new PieBlock(pieBlock.distance, newVector));
                    break;
                }
            }
        }

    }

    private void doWarningPhase(EliteEntity eliteEntity) {

        Iterator<PieBlock> pieBlockIterator = ((ArrayList<PieBlock>) realBlocks.clone()).iterator();

        while (pieBlockIterator.hasNext()) {
            PieBlock pieBlock = pieBlockIterator.next();
            if (pieBlock.distance < warningPhaseCounter) {
                Location rawPieBlock = eliteEntity.getLivingEntity().getLocation().clone().add(pieBlock.vector);

                pieBlockIterator.remove();

                if (rawPieBlock.getBlock().isPassable())
                    continue;

                rawPieBlock.getWorld().spawnParticle(Particle.SOUL, rawPieBlock.clone().add(0.5, 1, 0.5), 1, 0.1, 0.1, 0.1, 0.1);
            }
        }

        warningPhaseCounter++;

    }

    private void doDamagePhase(EliteEntity eliteEntity) {
        List<Block> blockList = new ArrayList<>();
        Iterator<PieBlock> pieBlockIterator = realBlocks.iterator();
        while (pieBlockIterator.hasNext()) {
            PieBlock pieBlock = pieBlockIterator.next();

            if (pieBlock.distance < damagePhaseCounter) {
                Location rawPieBlock = eliteEntity.getLivingEntity().getLocation().clone().add(pieBlock.vector);

                if (rawPieBlock.getBlock().isPassable()) continue;

                for (Entity entity : rawPieBlock.getWorld().getNearbyEntities(
                        new BoundingBox(rawPieBlock.getX(), rawPieBlock.getY(), rawPieBlock.getZ(),
                                rawPieBlock.getX() + 1, rawPieBlock.getY() + 3, rawPieBlock.getZ() + 1))) {
                    if (entity.getType().equals(EntityType.FALLING_BLOCK)) continue;
                    entity.setVelocity(entity.getLocation().clone().subtract(eliteEntity.getLivingEntity().getLocation()).toVector().setY(1).normalize().multiply(3));
                    if (entity.getType().equals(EntityType.PLAYER))
                        BossCustomAttackDamage.dealCustomDamage(eliteEntity.getLivingEntity(), (LivingEntity) entity, 20);
                }

                if (rawPieBlock.getBlock().getType().getBlastResistance() >= 7)
                    continue;

                blockList.add(rawPieBlock.getBlock());
                pieBlockIterator.remove();

            }
        }

        damagePhaseCounter++;

        Explosion.generateFakeExplosion(blockList, eliteEntity.getLivingEntity());

    }

    @Override
    protected void finishDeactivation(EliteEntity eliteEntity) {

    }

    private class PieBlock {
        public Integer distance;
        public Vector vector;

        public PieBlock(Integer distance, Vector vector) {
            this.distance = distance;
            this.vector = vector;
        }
    }

}
