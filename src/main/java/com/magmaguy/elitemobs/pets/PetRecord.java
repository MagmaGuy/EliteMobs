package com.magmaguy.elitemobs.pets;

import java.util.UUID;

/** One pet in a player's collection, as stored in their player data. Plain fields so Gson can read and write it. */
public final class PetRecord {
    public String id;
    public String species;
    public String model;
    public boolean baby;
    /** Epoch milliseconds the pet was born or tamed; a baby grows up a species-defined time after it. */
    public long bornAt;
    public boolean equipped;
    /** Epoch milliseconds before which the pet cannot breed again. */
    public long breedCooldownUntil;
    public String name;

    public PetRecord() {
    }

    public PetRecord(String species, String model, boolean baby, long bornAt) {
        this.id = UUID.randomUUID().toString();
        this.species = species;
        this.model = model;
        this.baby = baby;
        this.bornAt = bornAt;
        this.equipped = true;
    }

    public UUID uuid() {
        return UUID.fromString(id);
    }
}
