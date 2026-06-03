package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.npcs.NPCEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NPCProximityEnterEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final NPCEntity npcEntity;
    private final Player player;
    private final double activationRadius;

    public NPCProximityEnterEvent(NPCEntity npcEntity, Player player, double activationRadius) {
        this.npcEntity = npcEntity;
        this.player = player;
        this.activationRadius = activationRadius;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public NPCEntity getNPCEntity() {
        return npcEntity;
    }

    public Player getPlayer() {
        return player;
    }

    public double getActivationRadius() {
        return activationRadius;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
