package org.meml.shared.card;

import java.util.*;

/**
 * Monopoly Game's Card Deck
 * @param <Card>
 */
public class MonopolyDeckV1<Card> implements Deck<Card>{

    private final List<Card> cardList;

    public MonopolyDeckV1(Collection<Card> objs) {
        cardList = new ArrayList<>(objs);
    }

    public void shuffle() {
        Collections.shuffle(cardList);
    }

    public Card draw() {
        if (cardList.isEmpty()) {
            return null;
        }
        Random rand = new Random(System.currentTimeMillis());
        int drawIndex = rand.nextInt(cardList.size());
        return cardList.remove(drawIndex);
    }

    public void insert(Card card) {
        if (card != null) {
            cardList.add(card);
            shuffle();
        }
    }

    @Override
    public void insertAll(Collection<Card> collection) {
        if (collection != null) {
            cardList.addAll(collection);
            shuffle();
        }
    }
}
