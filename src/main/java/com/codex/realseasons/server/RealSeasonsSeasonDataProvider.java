package com.codex.realseasons.server;

import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import sereneseasons.api.season.ISeasonState;
import sereneseasons.api.season.SeasonHelper;

import java.util.Objects;

/**
 * Supplies Real Serene Seasons' computed season state while delegating other queries to Serene Seasons.
 */
public final class RealSeasonsSeasonDataProvider implements SeasonHelper.ISeasonDataProvider {
    private final RealSeasonsSeasonStateStore stateStore;
    private final SeasonHelper.ISeasonDataProvider delegate;

    public RealSeasonsSeasonDataProvider(RealSeasonsSeasonStateStore stateStore, SeasonHelper.ISeasonDataProvider delegate) {
        this.stateStore = Objects.requireNonNull(stateStore, "stateStore");
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public ISeasonState getServerSeasonState(Level level) {
        return stateStore.current();
    }

    @Override
    public ISeasonState getClientSeasonState(Level level) {
        // Use our synchronized state on client too to prevent desync
        return stateStore.current();
    }

    @Override
    public boolean usesTropicalSeasons(Holder<Biome> biome) {
        return delegate.usesTropicalSeasons(biome);
    }
}
