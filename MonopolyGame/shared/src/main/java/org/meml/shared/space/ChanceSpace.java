package org.meml.shared.space;

import org.meml.shared.Board;
import org.meml.shared.Player;

public class ChanceSpace extends BasicSpace implements Space{
    static final String CHANCE_SPACE_NAME = "CHANCE";
    static final String CHANCE_SPACE_DETAIL = "";

    public ChanceSpace(Board board) {
        super(board, CHANCE_SPACE_NAME, CHANCE_SPACE_DETAIL);
    }

    @Override
    public void triggerStayAction(Player player) {

    }

    @Override
    public void triggerPassAction(Player player) {

    }
}
