package net.juligames.core.addons.scoreboard;

import net.juligames.core.addons.scoreboard.service.ScoreboardService;
import net.juligames.core.addons.scoreboard.service.ScoreboardServiceProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class ScoreboardPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        registerService();
        saveConfig();
    }

    private void registerService() {

        ScoreboardConfigAdapter scoreboardConfigAdapter = new ScoreboardConfigAdapter(getConfig());
        Bukkit.getServicesManager().register(ScoreboardService.class,
                new ScoreboardServiceProvider(scoreboardConfigAdapter),
                this,
                ServicePriority.Highest);
    }

    public @NotNull ScoreboardService scoreboardService() {
        return Objects.requireNonNull(Bukkit.getServicesManager().load(ScoreboardService.class));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
