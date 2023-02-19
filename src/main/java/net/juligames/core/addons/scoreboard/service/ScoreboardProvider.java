package net.juligames.core.addons.scoreboard.service;

import net.juligames.core.adventure.AdventureTagManager;
import net.juligames.core.adventure.api.AdventureAPI;
import net.juligames.core.api.API;
import net.juligames.core.api.message.Message;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author Ture Bentzin
 * 19.02.2023
 */
@FunctionalInterface
public interface ScoreboardProvider extends Function<Player, ScoreboardProvider.ScoreboardResult> {

    @NotNull ScoreboardReturn provide(@NotNull Player target, @Range(from = 0, to = 15) int line);

    @Override
    default @NotNull ScoreboardResult apply(@NotNull Player player) {

        Set<ScoreboardLine> scoreboardLineSet = new HashSet<>();

        for (int i = 0; i < 15; i++) {
            ScoreboardReturn scoreboardReturn = provide(player, i);
            scoreboardLineSet.add(scoreboardReturn.toLine(i));
        }

        return new ScoreboardResult(scoreboardLineSet);
    }

    record ScoreboardLine(@Range(from = 0, to = 15) int line,
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
            Message message = API.get().getMessageApi().getMessageSmart(messageKey, player.locale());
            return adventureTagManager.resolve(message);
        }

        public @NotNull Component render(@NotNull Player player, String... replacements) {
            AdventureTagManager adventureTagManager = AdventureAPI.get().getAdventureTagManager();
            Message message = API.get().getMessageApi().getMessageSmart(messageKey, player.locale(), replacements);
            return adventureTagManager.resolve(message);
        }
    }

    record ScoreboardReturn(String messageKey,
                            BiFunction<Player, Integer, String[]> replacementSupplier) {

        public @NotNull ScoreboardLine toLine(@Range(from = 0, to = 15) int line) {
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

        public @NotNull ScoreboardLine get(@Range(from = 0, to = 15) int line) {
            return lines.stream().filter(scoreboardLine ->
                    scoreboardLine.line == line).findFirst().orElseThrow();
        }

        public @NotNull ScoreboardLine getDisplayName() {
            return get(0);
        }

        public @NotNull Scoreboard createScoreboardForPlayer(@NotNull Player player,
                                                             @NotNull ScoreboardManager scoreboardManager) {
            Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
            Objective objective = createObjectiveForPlayer(player, scoreboard);
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            return scoreboard;
        }

        private void createAndApply(@NotNull Player player, @NotNull ScoreboardManager manager) {
            Scoreboard scoreboard = createScoreboardForPlayer(player, manager);
            API.get().getAPILogger().debug("applied scoreboard " + scoreboard + " for " + player.getName());
            player.setScoreboard(scoreboard);
        }

        protected @NotNull Objective createObjectiveForPlayer(@NotNull Player player, @NotNull Scoreboard scoreboard) {
            return scoreboard.registerNewObjective(player.getUniqueId().toString(), Criteria.DUMMY,
                    getDisplayName().render(player));
        }

        protected void populateObjective(@NotNull Objective objective, @NotNull Player player) {
            for (int i = 0; i < 15; i++) {
                ScoreboardLine scoreboardLine = get(i);
                String[] apply = scoreboardLine.replacementSupplier.apply(player, i);
                if (apply == null) {
                    objective.getScore(convertToLEGACY(scoreboardLine.render(player)));
                } else {
                    objective.getScore(convertToLEGACY(scoreboardLine.render(player, apply)));
                }
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
