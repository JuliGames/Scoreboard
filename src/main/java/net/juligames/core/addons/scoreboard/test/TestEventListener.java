package net.juligames.core.addons.scoreboard.test;

import net.juligames.core.addons.scoreboard.service.ScoreboardService;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.net.http.WebSocket;
import java.util.Objects;

/**
 * @author Ture Bentzin
 * 19.02.2023
 */
public class TestEventListener implements Listener {

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        scoreboardService().enable(true, event.getPlayer());
    }



    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        scoreboardService().enable(false, event.getPlayer());
    }


    public @NotNull ScoreboardService scoreboardService() {
        return Objects.requireNonNull(Bukkit.getServicesManager().load(ScoreboardService.class));
    }
}
