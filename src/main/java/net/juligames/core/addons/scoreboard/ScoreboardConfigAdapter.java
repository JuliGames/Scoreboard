package net.juligames.core.addons.scoreboard;

import net.juligames.core.addons.scoreboard.service.ScoreboardProvider;
import net.juligames.core.addons.scoreboard.service.ScoreboardServiceProvider;
import org.bukkit.configuration.file.FileConfiguration;
import org.checkerframework.checker.optional.qual.MaybePresent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.BiFunction;

import static net.juligames.core.addons.scoreboard.service.ScoreboardServiceProvider.NULL_KEY;

/**
 * @author Ture Bentzin
 * 19.02.2023
 */
public final class ScoreboardConfigAdapter {

    private final @NotNull ScoreboardProvider provider;
    private final @NotNull FileConfiguration configuration;

    public ScoreboardConfigAdapter(@NotNull ScoreboardProvider provider, @NotNull FileConfiguration configuration) {
        this.provider = provider;
        this.configuration = configuration;

        applyDefaults();

    }

    private void applyDefaults() {
        for (int i = 0; i < 15; i++) {
            configuration.addDefault(String.valueOf(i), NULL_KEY);
        }
    }

    public boolean isDefault() {
        boolean stillDefault = true;
        for (int i = 0; i < 15; i++)
            if (!configuration.getString(String.valueOf(i), NULL_KEY).equals(NULL_KEY)) stillDefault = false;
        return stillDefault;
    }

    @MaybePresent
    public @NotNull Optional<ScoreboardProvider> scoreboardProvider() {
        if (isDefault())
            return Optional.empty();

        ScoreboardProvider provider1 = (target, line) -> new ScoreboardProvider.ScoreboardReturn(configuration.getString(String.valueOf(line), NULL_KEY), new ScoreboardServiceProvider.EmptyReplacementSupplier());
        return Optional.of(provider1);
    }

    @ApiStatus.Internal
    public <R> @NotNull R execute(@NotNull BiFunction<ScoreboardProvider, FileConfiguration, R> function) {
        return function.apply(provider, configuration);
    }
}
