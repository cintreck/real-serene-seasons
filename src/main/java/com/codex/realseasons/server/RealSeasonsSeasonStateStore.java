package com.codex.realseasons.server;

import sereneseasons.season.SeasonTime;

/**
 * Stores the latest calendar-derived season state for reuse across systems.
 */
public final class RealSeasonsSeasonStateStore {
    private volatile SeasonTime current = SeasonTime.ZERO;

    public SeasonTime current() {
        return current;
    }

    public void update(SeasonTime seasonTime) {
        this.current = seasonTime;
    }
}
