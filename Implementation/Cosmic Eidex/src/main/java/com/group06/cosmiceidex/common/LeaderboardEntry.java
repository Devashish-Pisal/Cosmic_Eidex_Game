package com.group06.cosmiceidex.common;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Repräsentiert einen Eintrag im leaderboard. [Username | Siege: 10 | Stiche: 42 | Punkte: 541]
 */
public class LeaderboardEntry implements Serializable {

    private final String username;
    private int wins;
    private int tricks;
    private int points;
    private static final Pattern PARSE_PATTERN = Pattern.compile(
            "^(.*) \\| Siege: (\\d+) \\| Stiche: (\\d+) \\| Punkte: (\\d+)$"
    );


    /**
     * Erstelle Leaderboard Eintrag mit gegebenen Werten.
     * @param username Username
     * @param wins Anzahl Siege
     * @param tricks Anzahl Stiche
     * @param points Anzahl Punkte
     */
    public LeaderboardEntry(String username, int wins, int tricks, int points) {
        this.username = username;
        this.wins = wins;
        this.tricks = tricks;
        this.points = points;
    }

    /**
     * Getter für username
     * @return Username
     */
    public String getUserName() {
        return username;
    }

    /**
     * Getter für wins
     * @return Siege
     */
    public int getWins() {
        return wins;
    }

    /**
     * Getter für Stiche
     * @return Stiche
     */
    public int getTricks() {
        return tricks;
    }

    /**
     * Getter für Punkte
     * @return Punkte
     */
    public int getPoints() {
        return points;
    }


    /**
     * Gibt Eintrag als String im Format: "username | Siege: X | Stiche: Y | Punkte: Z" zurück.
     * @return formatierter String
     */
    @Override
    public String toString() {
        return String.format("%s | Siege: %d | Stiche: %d | Punkte: %d",
                username, wins, tricks, points);
    }

    /**
     * Erzeugt LeaderboardEntry Objekt sofern der ggb. String das erwartete Format hat sonst null
     * @param line zeichenkette die geparst werden soll
     * @return LeaderboardEntry Objekt
     */
    public static LeaderboardEntry fromString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        Matcher matcher = PARSE_PATTERN.matcher(line.trim());
        if (matcher.matches()) {
            String username = matcher.group(1).trim();
            int wins = Integer.parseInt(matcher.group(2));
            int tricks = Integer.parseInt(matcher.group(3));
            int points = Integer.parseInt(matcher.group(4));
            return new LeaderboardEntry(username, wins, tricks, points);
        }
        return null;
    }

    /**
     * Vergleicht 2 LeaderboardEntry Objekte auf gleichheit über Username
     * @param o zu vergleichendes Objekt
     * @return true | false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LeaderboardEntry that = (LeaderboardEntry) o;
        return Objects.equals(username, that.username);
    }

    /**
     * Erzeugt Hashcode über Username
     * @return Hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}