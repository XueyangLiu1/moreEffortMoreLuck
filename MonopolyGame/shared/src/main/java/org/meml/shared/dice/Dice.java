package org.meml.shared.dice;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class Dice {

    static final String BOARDER = ". . . . .";
    static final String EMPTY = ".       .";
    static final String SINGLE_CENTER = ".   @   .";
    static final String SINGLE_LEFT = ". @     .";
    static final String SINGLE_RIGHT = ".     @ .";
    static final String DOUBLE_LEFT_RIGHT = ". @   @ .";

    private final int num;
    private TreeMap<Integer, String> textRep;

    /**
     * roll the dice!
     */
    public Dice(){
        this(6,true);
    }

    /**
     * set the dice num to be number
     * @param number
     */
    public Dice(int number){
        this(number, false);
    }

    /**
     * when random == true:
     * init num with an int which <=limit and >=1
     * e.g. limit = 6, init int randomly among 1,2,3,4,5,6
     * else:
     * init num = limit
     * @param limit
     * @param random
     */
    private Dice(int limit, boolean random){
        if (random) {
            Random r = new Random();
            this.num = r.nextInt(limit) + 1;
        } else {
            this.num = limit;
        }
        this.textRep = generateDiceStringRep(this.num);
    }

    private TreeMap<Integer, String> generateDiceStringRep(int number){
        TreeMap<Integer, String> lineMap = new TreeMap<>();
        lineMap.put(0,BOARDER);
        switch (number) {
            case 1:
                lineMap.put(1,EMPTY);
                lineMap.put(2,SINGLE_CENTER);
                lineMap.put(3,EMPTY);
                break;
            case 2:
                lineMap.put(1,SINGLE_LEFT);
                lineMap.put(2,EMPTY);
                lineMap.put(3,SINGLE_RIGHT);
                break;
            case 3:
                lineMap.put(1,SINGLE_LEFT);
                lineMap.put(2,SINGLE_CENTER);
                lineMap.put(3,SINGLE_RIGHT);
                break;
            case 4:
                lineMap.put(1,DOUBLE_LEFT_RIGHT);
                lineMap.put(2,EMPTY);
                lineMap.put(3,DOUBLE_LEFT_RIGHT);
                break;
            case 5:
                lineMap.put(1,DOUBLE_LEFT_RIGHT);
                lineMap.put(2,SINGLE_CENTER);
                lineMap.put(3,DOUBLE_LEFT_RIGHT);
                break;
            case 6:
                lineMap.put(1,DOUBLE_LEFT_RIGHT);
                lineMap.put(2,DOUBLE_LEFT_RIGHT);
                lineMap.put(3,DOUBLE_LEFT_RIGHT);
                break;
            default:
                return null;
        }
        lineMap.put(4,BOARDER);
        return lineMap;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        for (Integer i : this.textRep.keySet()) {
            sb.append(this.textRep.get(i));
            sb.append("\n");
        }
        return sb.toString();
    }

    public TreeMap<Integer, String> getRawTextRep(){
        return this.textRep;
    }

    public int getNum(){
        return this.num;
    }
}