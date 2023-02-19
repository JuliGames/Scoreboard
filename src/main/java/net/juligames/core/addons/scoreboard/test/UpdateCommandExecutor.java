package net.juligames.core.addons.scoreboard.test;

import net.juligames.core.addons.scoreboard.service.ScoreboardService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * @author Ture Bentzin
 * 19.02.2023
 */
public class UpdateCommandExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(sender.isOp()) {
            Bukkit.getServicesManager().load(ScoreboardService.class).update();
        }
        return true;
    }
}
