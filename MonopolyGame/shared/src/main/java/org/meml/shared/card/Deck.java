package org.meml.shared.card;

import java.util.Collection;

/**
 * A templated interface, perform the following operations on T type of objects:
 *   shuffle, draw, refill
 * @param <T>
 */
public interface Deck<T> {

    void shuffle();

    T draw();

    void insert(T data);

    void insertAll(Collection<T> dataCollection);
}
