package net.juligames.core.addons.scoreboard.service;

import net.juligames.core.addons.scoreboard.ServiceLayer;
import net.juligames.core.api.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Ture Bentzin
 * 19.02.2023
 */
public sealed interface ScoreboardService permits ScoreboardServiceProvider {

    default @NotNull Function<UUID, Player> uuidIdentifier() {
        return Bukkit::getPlayer;
    }


    @NotNull String getDefaultKey();

    @NotNull String getKey(@Range(from = 0, to = 16) int position, @NotNull Player player);

    @NotNull Message renderMessage(@Range(from = 0, to = 16) int position, @NotNull Player player);

    @NotNull ServiceLayer getCurrentServiceLayer();

    boolean isEnabled();

    /**
     * @param enable the new value
     * @return the old value
     */
    boolean enable(boolean enable);


    /**
     * @param target the context
     * @return the value
     */
    boolean isEnabled(@NotNull Player target);

    /**
     * @param enable the new value
     * @param target the context
     * @return the old value
     */
    boolean enable(boolean enable, @NotNull Player target);

    void update();

    void update(@NotNull Predicate<Player> playerPredicate);

    @NotNull Collection<UUID> getEnabled();

    /**
     * @param provider if null: back to config or nothing
     */
    void setScoreboardProvider(@Nullable ScoreboardProvider provider);
}
