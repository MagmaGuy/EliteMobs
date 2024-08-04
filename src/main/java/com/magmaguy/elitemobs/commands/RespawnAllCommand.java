package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;

import java.util.List;

public class RespawnAllCommand extends AdvancedCommand {
    public RespawnAllCommand() {
        super(List.of("respawn"));
        addLiteral("all");
        setUsage("/em respawn all");
        setPermission("elitemobs.respawn.force");
        setDescription("Forces all regional bosses to respawn.");
    }

    @Override
    public void execute(CommandData commandData) {
        RegionalBossEntity.getRegionalBossEntities().forEach(regionalBossEntity -> {
            if (regionalBossEntity.isRespawning()) regionalBossEntity.forceRespawn();
        });
    }
}