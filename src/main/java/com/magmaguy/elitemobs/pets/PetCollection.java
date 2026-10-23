package com.magmaguy.elitemobs.pets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.magmacore.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** A player's pets, kept in the {@code Pets} column of their player data as a JSON list. */
public final class PetCollection {
    private static final Gson GSON = new GsonBuilder().create();
    private static final java.lang.reflect.Type LIST = new TypeToken<List<PetRecord>>() {
    }.getType();

    private final UUID player;
    private final List<PetRecord> pets;

    private PetCollection(UUID player, List<PetRecord> pets) {
        this.player = player;
        this.pets = pets;
    }

    public static PetCollection load(UUID player) {
        String json = PlayerData.getPetsJson(player);
        List<PetRecord> pets = new ArrayList<>();
        if (json != null && !json.isBlank()) {
            try {
                List<PetRecord> stored = GSON.fromJson(json, LIST);
                if (stored != null) for (PetRecord record : stored) if (record != null && record.id != null) pets.add(record);
            } catch (RuntimeException failure) {
                Logger.warn("Could not read the pets of " + player + ": " + failure.getMessage());
            }
        }
        return new PetCollection(player, pets);
    }

    public UUID player() {
        return player;
    }

    public List<PetRecord> pets() {
        return pets;
    }

    public PetRecord find(UUID id) {
        for (PetRecord record : pets) if (record.uuid().equals(id)) return record;
        return null;
    }

    public List<PetRecord> equipped() {
        List<PetRecord> equipped = new ArrayList<>();
        for (PetRecord record : pets) if (record.equipped) equipped.add(record);
        return equipped;
    }

    public void add(PetRecord record) {
        pets.add(record);
        save();
    }

    public void remove(PetRecord record) {
        pets.removeIf(other -> other.id.equals(record.id));
        save();
    }

    public void save() {
        PlayerData.setPetsJson(player, GSON.toJson(pets, LIST));
    }
}
