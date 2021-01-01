package com.magmaguy.elitemobs.entitytracker;

import com.magmaguy.elitemobs.api.internal.RemovalReason;

interface AbstractTrackedEntity {
    void specificRemoveHandling(RemovalReason removalReason);
}
