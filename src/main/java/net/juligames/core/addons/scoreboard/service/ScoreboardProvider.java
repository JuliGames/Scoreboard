package net.juligames.core.addons.scoreboard.service;

import net.juligames.core.adventure.AdventureTagManager;
import net.juligames.core.adventure.api.AdventureAPI;
import net.juligames.core.api.API;
import net.juligames.core.api.message.Message;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Ture Bentzin
 * 19.02.2023
 */
@FunctionalInterface
public interface ScoreboardProvider extends Function<Player, ScoreboardProvider.ScoreboardResult> {

    static void handlePlayerUpdate(@NotNull Player player, @NotNull ScoreboardProvider provider) {
        ScoreboardResult scoreboardResult = provider.apply(player);
        API.get().getAPILogger().debug("handle scoreboard update for " + player.getName());
        scoreboardResult.createAndApply(player, Bukkit.getScoreboardManager(), integer -> true);
    }

    static void handlePlayerUpdate(@NotNull Player player, @NotNull ScoreboardProvider provider, @NotNull Predicate<Integer> integerPredicate) {
        ScoreboardResult scoreboardResult = provider.apply(player);
        API.get().getAPILogger().debug("handle scoreboard update for " + player.getName());
        scoreboardResult.createAndApply(player, Bukkit.getScoreboardManager(), integerPredicate);
    }

    @NotNull ScoreboardReturn provide(@NotNull Player target, @Range(from = 0, to = 16) int line);

    @Override
    default @NotNull ScoreboardResult apply(@NotNull Player player) {
        return apply(player, integer -> true);
    }

    default @NotNull ScoreboardResult apply(@NotNull Player player, @NotNull Predicate<Integer> integerPredicate) {
        Set<ScoreboardLine> scoreboardLineSet = new HashSet<>();
        for (int i = 0; i < 16; i++) {
            if (!integerPredicate.test(i)) continue;
            ScoreboardReturn scoreboardReturn = provide(player, i);
            scoreboardLineSet.add(scoreboardReturn.toLine(i));
        }
        return new ScoreboardResult(scoreboardLineSet);
    }

    record ScoreboardLine(@Range(from = 0, to = 16) int line,
                          String messageKey,
                          BiFunction<Player, Integer, String[]> replacementSupplier) implements Comparable<ScoreboardLine> {

        @Override
        public int hashCode() {
            return line;
        }

        @Override
        public int compareTo(@NotNull ScoreboardProvider.ScoreboardLine o) {
            return line - o.line;
        }

        public @NotNull Component render(@NotNull Player player) {
            AdventureTagManager adventureTagManager = AdventureAPI.get().getAdventureTagManager();
            return adventureTagManager.resolve(export(player));
        }

        public @NotNull Message export(@NotNull Player player) {
            String[] replacements = replacementSupplier.apply(player, line);
            Message message;
            if (replacements == null) {
                message = API.get().getMessageApi().getMessageSmart(messageKey, player.locale());
            } else {
                message = API.get().getMessageApi().getMessageSmart(messageKey, player.locale(), replacements);
            }
            return message;
        }
    }

    record ScoreboardReturn(String messageKey,
                            BiFunction<Player, Integer, String[]> replacementSupplier) {

        public @NotNull ScoreboardLine toLine(@Range(from = 0, to = 16) int line) {
            return new ScoreboardLine(line, messageKey, replacementSupplier);
        }
    }


    class ScoreboardResult {
        private final @NotNull Set<ScoreboardLine> lines;

        public ScoreboardResult(@NotNull Set<ScoreboardLine> lines) {
            this.lines = lines;
        }

        public ScoreboardLine @NotNull [] asArray() {
            return lines.stream().sorted().toArray(ScoreboardLine[]::new);
        }

        public @NotNull ScoreboardLine get(@Range(from = 0, to = 16) int line) {
            return lines.stream().filter(scoreboardLine ->
                    scoreboardLine.line == line).findFirst().orElseThrow();
        }

        public @NotNull ScoreboardLine getDisplayName() {
            return get(0);
        }

        public @NotNull Scoreboard createScoreboardForPlayer(@NotNull Player player,
                                                             @NotNull ScoreboardManager scoreboardManager, @NotNull Predicate<Integer> integerPredicate) {
            Scoreboard scoreboard = scoreboardManager.getNewScoreboard(); //fix
            Objective objective = createObjectiveForPlayer(player, scoreboard, integerPredicate);
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            return scoreboard;
        }

        public void createAndApply(@NotNull Player player, @NotNull ScoreboardManager manager, @NotNull Predicate<Integer> integerPredicate) {
            Scoreboard scoreboard = createScoreboardForPlayer(player, manager, integerPredicate);
            API.get().getAPILogger().debug("applied scoreboard " + scoreboard + " for " + player.getName());
            player.setScoreboard(scoreboard);
        }

        protected @NotNull Objective createObjectiveForPlayer(@NotNull Player player, @NotNull Scoreboard scoreboard, @NotNull Predicate<Integer> integerPredicate) {
            Objective objective = scoreboard.registerNewObjective(player.getUniqueId().toString(), "dummy",
                    getDisplayName().render(player));
            populateObjective(objective, player, integerPredicate);
            return objective;
        }

        protected void populateObjective(@NotNull Objective objective, @NotNull Player player, @NotNull Predicate<Integer> integerPredicate) {
            for (int i = 1; i < 16; i++) {
                if(!integerPredicate.test(i)) continue;
                ScoreboardLine scoreboardLine = get(i);
                objective.getScore(convertToLEGACY(scoreboardLine.render(player))).setScore(i);
            }
        }

        protected @NotNull String convertToLEGACY(@NotNull Component component) {
            return LegacyComponentSerializer.legacySection().serialize(component);
        }

        @Override
        public @NotNull String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ScoreboardResult: {\n");
            for (ScoreboardLine line : lines)
                builder.append(line.messageKey()).append(", \n");
            builder.append("}");
            return builder.toString();
        }
    }
}
