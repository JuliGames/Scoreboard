package net.juligames.core.addons.scoreboard;

import net.juligames.core.addons.scoreboard.service.ScoreboardProvider;
import net.juligames.core.addons.scoreboard.service.ScoreboardService;
import net.juligames.core.addons.scoreboard.service.ScoreboardServiceProvider;
import net.juligames.core.api.API;
import org.bukkit.configuration.file.FileConfiguration;
import org.checkerframework.checker.optional.qual.MaybePresent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiFunction;

/**
 * @author Ture Bentzin
 * 19.02.2023
 */
public final class ScoreboardConfigAdapter {

    private final @NotNull FileConfiguration configuration;
    private @Nullable ScoreboardService service;

    public ScoreboardConfigAdapter(@NotNull ScoreboardService service, @NotNull FileConfiguration configuration) {
        this.service = service;
        this.configuration = configuration;
        applyDefaults();
    }

    public ScoreboardConfigAdapter(@NotNull FileConfiguration configuration) {
        this.service = null;
        this.configuration = configuration;
        applyDefaults();
    }

    private void applyDefaults() {
        for (int i = 0; i < 16; i++) {
            if (service != null) {
                configuration.addDefault(String.valueOf(i), service.getDefaultKey());
            }
        }
    }

    public boolean isDefault() {
        boolean stillDefault = true;
        for (int i = 0; i < 16; i++)
            if (service != null && !configuration.getString(String.valueOf(i), service.getDefaultKey()).equals(service.getDefaultKey()))
                stillDefault = false;
        return stillDefault;
    }

    @MaybePresent
    public @NotNull Optional<ScoreboardProvider> scoreboardProvider() {
        if (isDefault())
            return Optional.empty();

        ScoreboardProvider provider1 = (target, line) -> {
            if (service != null) {
                String string = configuration.getString(String.valueOf(line), service.getDefaultKey());
                API.get().getAPILogger().debug("extract: " + string + " for " + line);
                return new ScoreboardProvider.ScoreboardReturn(string, new ScoreboardServiceProvider.EmptyReplacementSupplier());
            }
            throw new IllegalStateException("adapter is not ready!");
        };
        return Optional.of(provider1);
    }

    @ApiStatus.Internal
    public <R> @NotNull R execute(@NotNull BiFunction<ScoreboardService, FileConfiguration, R> function) {
        return function.apply(service, configuration);
    }

    public void setService(@NotNull ScoreboardService service) {
        this.service = service;
    }
}
