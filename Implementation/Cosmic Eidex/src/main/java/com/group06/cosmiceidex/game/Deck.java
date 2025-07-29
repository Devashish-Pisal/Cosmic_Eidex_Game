package com.group06.cosmiceidex.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck implements Serializable {
    private List<Card> cards;

    public Deck() {
        this.cards = new ArrayList<>();
        cards.add(new Card(Colour.EIDEX, Value.SECHS, "Eidex6.png"));
        cards.add(new Card(Colour.EIDEX, Value.SIEBEN, "Eidex7.png"));
        cards.add(new Card(Colour.EIDEX, Value.ACHT, "Eidex8.png"));
        cards.add(new Card(Colour.EIDEX, Value.NEUN, "Eidex9.png"));
        cards.add(new Card(Colour.EIDEX, Value.ZEHN, "Eidex10.png"));
        cards.add(new Card(Colour.EIDEX, Value.ASS, "EidexAs.png"));
        cards.add(new Card(Colour.EIDEX, Value.BUBE, "EidexBube.png"));
        cards.add(new Card(Colour.EIDEX, Value.DAME, "EidexDame.png"));
        cards.add(new Card(Colour.EIDEX, Value.KOENIG, "EidexKoenig.png"));

        cards.add(new Card(Colour.HERZ, Value.SECHS, "Herz6.png"));
        cards.add(new Card(Colour.HERZ, Value.SIEBEN, "Herz7.png"));
        cards.add(new Card(Colour.HERZ, Value.ACHT, "Herz8.png"));
        cards.add(new Card(Colour.HERZ, Value.NEUN, "Herz9.png"));
        cards.add(new Card(Colour.HERZ, Value.ZEHN, "Herz10.png"));
        cards.add(new Card(Colour.HERZ, Value.ASS, "HerzAs.png"));
        cards.add(new Card(Colour.HERZ, Value.BUBE, "HerzBube.png"));
        cards.add(new Card(Colour.HERZ, Value.DAME, "HerzDame.png"));
        cards.add(new Card(Colour.HERZ, Value.KOENIG, "HerzKoenig.png"));

        cards.add(new Card(Colour.RABE, Value.SECHS, "Rabe6.png"));
        cards.add(new Card(Colour.RABE, Value.SIEBEN, "Rabe7.png"));
        cards.add(new Card(Colour.RABE, Value.ACHT, "Rabe8.png"));
        cards.add(new Card(Colour.RABE, Value.NEUN, "Rabe9.png"));
        cards.add(new Card(Colour.RABE, Value.ZEHN, "Rabe10.png"));
        cards.add(new Card(Colour.RABE, Value.ASS, "RabeAs.png"));
        cards.add(new Card(Colour.RABE, Value.BUBE, "RabeBube.png"));
        cards.add(new Card(Colour.RABE, Value.DAME, "RabeDame.png"));
        cards.add(new Card(Colour.RABE, Value.KOENIG, "RabeKoenig.png"));

        cards.add(new Card(Colour.STERN, Value.SECHS, "Stern6.png"));
        cards.add(new Card(Colour.STERN, Value.SIEBEN, "Stern7.png"));
        cards.add(new Card(Colour.STERN, Value.ACHT, "Stern8.png"));
        cards.add(new Card(Colour.STERN, Value.NEUN, "Stern9.png"));
        cards.add(new Card(Colour.STERN, Value.ZEHN, "Stern10.png"));
        cards.add(new Card(Colour.STERN, Value.ASS, "SternAs.png"));
        cards.add(new Card(Colour.STERN, Value.BUBE, "SternBube.png"));
        cards.add(new Card(Colour.STERN, Value.DAME, "SternDame.png"));
        cards.add(new Card(Colour.STERN, Value.KOENIG, "SternKoenig.png"));
    }

    public void shuffle() {
        Collections.shuffle(this.cards);
    }


    //Oberste Karte holen und aus dem Deck entfernen
    public Card dealCard() {
        if (this.cards.isEmpty()) {
            return null;
        }
        return this.cards.remove(0);
    }
    
    public int size() {
        return this.cards.size();
    }

}
