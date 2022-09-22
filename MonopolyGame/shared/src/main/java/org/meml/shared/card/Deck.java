package org.meml.shared.card;

import java.util.*;

/**
 * A templated class, perform the following operations on the type of objects:
 *   shuffle, draw, refill
 * @param <T>
 */
public class Deck<T> {

    private final List<T> cardList;

    public Deck(List<T> objs) {
        cardList = new LinkedList<>(objs);
    }

    private void shuffle() {
        Collections.shuffle(cardList);
    }

    public T draw() {
        if (cardList.isEmpty()) {
            return null;
        }
        Random rand = new Random(System.currentTimeMillis());
        int drawIndex = rand.nextInt(cardList.size());
        return cardList.remove(drawIndex);
    }

    public void refill(T obj) {
        cardList.add(obj);
        shuffle();
    }
}
