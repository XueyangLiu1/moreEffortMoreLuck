package org.meml.shared;

import org.junit.Assert;
import org.junit.Test;
import org.meml.shared.Dice;
import org.meml.shared.SetOfDices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class DiceTest {

    @Test
    public void testToString() {
        InputStream inputStream = Dice.class.getResourceAsStream("/DiceExamples.txt");
        String expected = readFromInputStream(inputStream);
        StringBuilder actual = new StringBuilder();
        for (int i = 1;i<=6;i++) {
            Dice dice = new Dice(i);
            actual.append(dice.toString());
        }
        Assert.assertEquals(expected, actual.toString());
    }

    @Test
    public void testSetOfDices() {
        InputStream inputStream = Dice.class.getResourceAsStream("/SetOfDicesExamples.txt");
        String expected = readFromInputStream(inputStream);
        StringBuilder actual = new StringBuilder();

        SetOfDices first = new SetOfDices(new Dice(3));
        Assert.assertEquals(3, first.getSum());

        SetOfDices second = new SetOfDices(new Dice(6), new Dice(6));
        Assert.assertEquals(12, second.getSum());

        SetOfDices third = new SetOfDices(new Dice(1), new Dice(2), new Dice(4));
        Assert.assertEquals(7, third.getSum());

        actual.append(first.toString());
        actual.append(second.toString());
        actual.append(third.toString());

        third.addDice(new Dice(5));
        Assert.assertEquals(12, third.getSum());

        actual.append(third.toString());

        Assert.assertEquals(expected, actual.toString());
    }

    private String readFromInputStream(InputStream inputStream){
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            return "";
        }
        return resultStringBuilder.toString();
    }
}