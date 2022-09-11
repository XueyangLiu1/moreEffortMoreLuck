package org.meml.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SetOfDices {

    private List<Dice> diceList;
    private int sum;
    private TreeMap<Integer, String> textRep;

    /**
     * given a list of dice, generate a combined rep of them
     * @param dices
     */
    public SetOfDices(Dice... dices){
        this.diceList = new ArrayList<>();
        int sum = 0;
        for (Dice d : dices) {
            this.diceList.add(d);
            sum += d.getNum();
        }
        this.sum = sum;
        this.textRep = generateSetOfDicesTextRep(this.diceList);
    }

    /**
     * generate the combined text rep of multiple dices
     * @param diceList
     * @return
     */
    private TreeMap<Integer, String> generateSetOfDicesTextRep(List<Dice> diceList) {
        TreeMap<Integer, StringBuilder> lineBuilderMap  = new TreeMap<>();
        for (Dice d : diceList) {
            TreeMap<Integer,String> currDiceRawRep = d.getRawTextRep();
            for (Integer i: currDiceRawRep.keySet()) {
                StringBuilder currentLineBuilder = lineBuilderMap.getOrDefault(i, new StringBuilder());
                if (!currentLineBuilder.isEmpty()) {
                    currentLineBuilder.append(" ");
                }
                currentLineBuilder.append(currDiceRawRep.get(i));
                lineBuilderMap.put(i, currentLineBuilder);
            }
        }
        TreeMap<Integer, String> lineMap = new TreeMap<>();
        for (Integer i: lineBuilderMap.keySet()) {
            lineMap.put(i, lineBuilderMap.get(i).toString());
        }
        return lineMap;
    }

    public void addDice(Dice dice) {
        this.diceList.add(dice);
        this.sum += dice.getNum();
        TreeMap<Integer,String> diceRawRep = dice.getRawTextRep();
        for (Integer i: diceRawRep.keySet()) {
            this.textRep.put(i, this.textRep.get(i) + " " + diceRawRep.get(i));
        }
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        for (Integer i : this.textRep.keySet()) {
            sb.append(this.textRep.get(i));
            sb.append("\n");
        }
        return sb.toString();
    }

    public int getSum(){
        return this.sum;
    }
}
