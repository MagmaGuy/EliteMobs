package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
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
    public void execute(CommandData commandData) {
        Fireball fireball = (Fireball) commandData.getPlayerSender().getWorld().spawnEntity(commandData.getPlayerSender().getLocation(), EntityType.FIREBALL);
        fireball.setDirection(commandData.getPlayerSender().getLocation().getDirection().normalize());
        fireball.setShooter(commandData.getPlayerSender());
        fireball.setYield(3F);
        EntityTracker.registerProjectileEntity(fireball);
    }
}