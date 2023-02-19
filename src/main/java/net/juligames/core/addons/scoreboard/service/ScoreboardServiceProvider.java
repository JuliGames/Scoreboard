package net.juligames.core.addons.scoreboard.service;

import net.juligames.core.addons.scoreboard.ScoreboardConfigAdapter;
import net.juligames.core.addons.scoreboard.ServiceLayer;
import net.juligames.core.api.API;
import net.juligames.core.api.err.dev.TODOException;
import net.juligames.core.api.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Ture Bentzin
 * 19.02.2023
 */
public non-sealed class ScoreboardServiceProvider implements ScoreboardService {


    public static final @NotNull String NULL_KEY = "null";

    private final @NotNull Set<UUID> enabledUUIDs;
    private final @NotNull String defaultKey;
    private @Nullable ScoreboardProvider scoreboardProvider;
    private @NotNull ServiceLayer serviceLayer = ServiceLayer.NOTHING;
    private boolean enabled = true;
    private @NotNull ScoreboardConfigAdapter adapter;


    public ScoreboardServiceProvider(@NotNull Set<UUID> enabledUUIDs, @Nullable ScoreboardProvider provider, @NotNull String defaultKey, @NotNull ScoreboardConfigAdapter adapter) {
        this.enabledUUIDs = enabledUUIDs;
        scoreboardProvider = provider;
        this.defaultKey = defaultKey;
        adapter.setService(this);
        this.adapter = adapter;
        updateServiceLayer();

    }

    public ScoreboardServiceProvider(@NotNull ScoreboardConfigAdapter adapter) {
        this(new HashSet<>(), null, NULL_KEY, adapter);
    }

    @Override
    public @NotNull String getDefaultKey() {
        return defaultKey;
    }

    @Override
    public @NotNull String getKey(@Range(from = 0, to = 15) int position, @NotNull Player player) {
        return provideBest().map(provider -> provider.provide(player, position).messageKey()).orElse(getDefaultKey());
    }

    @Override
    public @NotNull Message renderMessage(@Range(from = 0, to = 15) int position, @NotNull Player player) {
        return provideBest().map(provider ->
                        provider.provide(player, position).toLine(position).export(player))
                .orElseGet(() ->
                        API.get().getMessageApi().getMessageSmart(getDefaultKey(), player.locale()));
    }

    @Override
    public @NotNull ServiceLayer getCurrentServiceLayer() {
        updateServiceLayer(); //May be removed
        return serviceLayer;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean enable(boolean enable) {
        if (enable) {
            boolean m = enabled;
            enabled = true;
            update();
            return m;
        } /*else*/
        {
            boolean m = enabled;
            enabled = false;
            clearAllBoards(player -> true);
            return m;
        }
    }

    @Override
    public boolean isEnabled(@NotNull Player target) {
        return enabledUUIDs.contains(target.getUniqueId());
    }

    /**
     *
     * @param enable the new value
     * @param target the context
     * @return if the operation was successful
     */
    @Override
    public boolean enable(boolean enable, @NotNull Player target) {
        if(enable) {
           return enabledUUIDs.add(target.getUniqueId());
        }else {
            return enabledUUIDs.remove(target.getUniqueId());
        }
    }

    @Override
    public void update() {
        if (enabled)
            update(player -> true);
    }

    @Override
    public void update(@NotNull Predicate<Player> playerPredicate) {
        if (!enabled) return;
        //noinspection ConstantValue
        if ((serviceLayer.equals(ServiceLayer.API) && scoreboardProvider != null) || canProvideFromConfig()) {
            ScoreboardProvider provider = provideBest().orElseThrow();
            forEachEnabledOnlinePlayer(player ->
                    ScoreboardProvider.handlePlayerUpdate(player, provider), playerPredicate);
        } else {
            //empy Scoreboard for every player
            clearAllBoards(playerPredicate);
        }
    }

    private void forEachEnabledOnlinePlayer(@NotNull Consumer<Player> forEach, @NotNull Predicate<Player> playerPredicate) {
        getEnabled().stream().map(Bukkit::getPlayer).filter(playerPredicate)
                .filter(Objects::nonNull).forEach(forEach);
    }

    private void forEachEnabledOnlinePlayer(@NotNull Consumer<Player> forEach) {
        getEnabled().stream().map(Bukkit::getPlayer)
                .filter(Objects::nonNull).filter(OfflinePlayer::isOnline).forEach(forEach);
    }

    private void updateServiceLayer() {
        serviceLayer = identifyServiceLayer();
    }

    private @NotNull ServiceLayer identifyServiceLayer() {
        if (scoreboardProvider != null) {
            return ServiceLayer.API;
        }
        if (canProvideFromConfig()) {
            return ServiceLayer.CONFIG;
        }
        return ServiceLayer.NOTHING;
    }

    @Override
    public @NotNull Collection<UUID> getEnabled() {
        return enabledUUIDs.stream().collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public void setScoreboardProvider(@Nullable ScoreboardProvider provider) {
        this.scoreboardProvider = provider;
    }

    private boolean canProvideFromConfig() {
        return !adapter.isDefault();
    }

    private @NotNull Optional<ScoreboardProvider> provideFromConfig() {
        if (canProvideFromConfig()) {
            return adapter.scoreboardProvider();
        } else return Optional.empty();
    }

    private @NotNull Optional<ScoreboardProvider> provideBest() {
        if (scoreboardProvider != null) return Optional.of(scoreboardProvider);
        return provideFromConfig();
    }

    private void clearAllBoards(@NotNull Predicate<Player> playerPredicate) {
        EmptyScoreboardSupplier emptyScoreboardSupplier = new EmptyScoreboardSupplier();
        forEachEnabledOnlinePlayer(player -> player.setScoreboard(emptyScoreboardSupplier.get()), playerPredicate);
    }

    public static final class EmptyScoreboardSupplier implements Supplier<Scoreboard> {

        @Override
        public @NotNull Scoreboard get() {
            return Bukkit.getScoreboardManager().getNewScoreboard();
        }

    }

    public static final class EmptyReplacementSupplier implements BiFunction<Player, Integer, String[]> {
        @Contract(pure = true)
        @Override
        public String @Nullable [] apply(@Nullable Player player, @Nullable Integer integer) {
            return null;
        }
    }
}
