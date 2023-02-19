package net.juligames.core.addons.scoreboard;

import net.juligames.core.addons.scoreboard.service.ScoreboardProvider;
import net.juligames.core.addons.scoreboard.service.ScoreboardService;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

/**
 * @author Ture Bentzin
 * 19.02.2023
 */
public final class ScoreboardAdministrator {

    private final @NotNull RegisteredServiceProvider<ScoreboardService> serviceProvider;

    @SuppressWarnings("ProtectedMemberInFinalClass")
    protected ScoreboardAdministrator(@NotNull RegisteredServiceProvider<ScoreboardService> serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public @NotNull ScoreboardService serviceOrThrow() {
        return serviceProvider.getProvider();
    }
}
