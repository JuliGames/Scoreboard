package net.juligames.core.addons.scoreboard.service;

import net.juligames.core.addons.scoreboard.ServiceLayer;
import net.juligames.core.api.message.Message;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/**
 * @author Ture Bentzin
 * 19.02.2023
 */
public non-sealed class ScoreboardServiceProvider implements ScoreboardService{

    private final @NotNull Set<UUID> enabledUUIDs;
    private @Nullable ScoreboardProvider scoreboardProvider;
    private final @NotNull String defaultKey;


    public ScoreboardServiceProvider(@NotNull Set<UUID> enabledUUIDs, @Nullable ScoreboardProvider provider, @NotNull String defaultKey) {
        this.enabledUUIDs = enabledUUIDs;
        scoreboardProvider = provider;
        this.defaultKey = defaultKey;
    }

    @Override
    public @NotNull String getDefaultKey() {
        return defaultKey;
    }

    @Override
    public @NotNull String getKey(@Range(from = 0, to = 15) int position, @NotNull Player player) {
        return scoreboardProvider.;
    }

    @Override
    public @NotNull Message renderMessage(@Range(from = 0, to = 15) int position, @NotNull Player player) {
        return null;
    }

    @Override
    public @NotNull ServiceLayer getCurrentServiceLayer() {
        return null;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public boolean enable(boolean enable) {
        return false;
    }

    @Override
    public boolean isEnabled(@NotNull Player target) {
        return false;
    }

    @Override
    public boolean enable(boolean enable, @NotNull Player target) {
        return false;
    }

    @Override
    public void update() {

    }

    @Override
    public @NotNull Collection<UUID> getEnabled() {
        return null;
    }

    @Override
    public void setScoreboardProvider(@Nullable ScoreboardProvider provider) {

    }
}
