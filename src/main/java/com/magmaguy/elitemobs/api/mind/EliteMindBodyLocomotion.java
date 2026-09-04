package com.magmaguy.elitemobs.api.mind;

/** Native movement model requested for an EliteMobs Mind body. */
public enum EliteMindBodyLocomotion {
    GROUNDED,
    FLYING,
    AQUATIC,
    /** Native pathfinding and movement across both land and water. */
    AMPHIBIOUS,
    STATIONARY
}
