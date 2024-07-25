package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.magmacore.command.AdvancedCommand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;

import java.util.List;

public class FireballCommand extends AdvancedCommand {
    public FireballCommand() {
        super(List.of("fireball"));
        setUsage("/em fireball");
        setPermission("elitemobs.fireball");
        setDescription("Shoots a fireball, to test terrain protections and explosion regeneration.");
    }

    @Override
    public void execute() {
        Fireball fireball = (Fireball) getCurrentPlayerSender().getWorld().spawnEntity(getCurrentPlayerSender().getLocation(), EntityType.FIREBALL);
        fireball.setDirection(getCurrentPlayerSender().getLocation().getDirection().normalize());
        fireball.setShooter(getCurrentPlayerSender());
        fireball.setYield(3F);
        EntityTracker.registerProjectileEntity(fireball);
    }
}