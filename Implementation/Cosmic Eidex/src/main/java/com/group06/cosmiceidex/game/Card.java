package com.group06.cosmiceidex.game;

import java.io.Serializable;

public class Card implements Serializable {
    private final Colour colour;
    private final Value value;
    private final String image;

    public Card(Colour colour, Value value, String image) {
        this.colour = colour;
        this.value = value;
        this.image = image;
    }

    public Colour getColour() {
        return colour;
    }

    public Value getValue() {
        return value;
    }

    public int getPoints() {
        return value.getPoints();
    }

    public String getImage(){return image;}

    @Override
    public String toString() {
        return colour.toString() + "-" + value.toString();
    }
}