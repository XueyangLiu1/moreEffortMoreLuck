package org.meml.shared.card;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MonopolyDeckV1Test {

    @Test
    public void MonopolyDeckApiTest() {
        Card card = Mockito.mock(Card.class);
        List<Card> cardList = new ArrayList<>();
        cardList.add(card);
        Deck<Card> deck = new MonopolyDeckV1<>(cardList);

        // single card in deck
        Assert.assertEquals(card, deck.draw());
        Assert.assertNull(deck.draw());

        // insert single card
        deck.insert(card);
        Assert.assertEquals(card, deck.draw());
        Assert.assertNull(deck.draw());

        // insert a lot of cards
        Set<Card> cardSet = new HashSet<>();
        int testCardNum = 100;
        for (int i = 0;i<testCardNum;i++) {
            Card newCard = Mockito.mock(Card.class);
            cardSet.add(newCard);
        }
        deck.insertAll(cardSet);
        for(int i = 0;i<testCardNum;i++) {
            Card curr = deck.draw();
            Assert.assertTrue(cardSet.contains(curr));
            cardSet.remove(curr);
        }
        Assert.assertNull(deck.draw());
    }

}