package com.group06.cosmiceidex.game;


// Standardwerte für die Karten
public enum Value {
    SECHS(0),
    SIEBEN(0),
    ACHT(0),
    NEUN(0),
    ZEHN(10),
    BUBE(2),
    DAME(3),
    KOENIG(4),
    ASS(11);

    private final int points;

    Value(int points) {
        this.points = points;
    }


    public int getPoints() {
        return points;
    }
}
