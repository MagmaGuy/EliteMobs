package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.magmacore.command.AdvancedCommand;

import java.util.List;

public class RespawnAllCommand extends AdvancedCommand {
    public RespawnAllCommand() {
        super(List.of("respawn"));
        addLiteral("all");
        setUsage("/em respawn all");
        setPermission("elitemobs.*");
        setDescription("Forces all regional bosses to respawn.");
    }

    @Override
    public void execute() {
        RegionalBossEntity.getRegionalBossEntities().forEach(regionalBossEntity -> {
            if (regionalBossEntity.isRespawning()) regionalBossEntity.forceRespawn();
        });
    }
}