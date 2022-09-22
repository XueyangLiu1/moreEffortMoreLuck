package org.meml.shared.card;

import org.meml.shared.Board;
import org.meml.shared.Player;

public interface Card {

    /**
     * return the usage of this card
     */
    String usage();

    /*
     *
     */
    void useOn(Player player, Board board);
}
