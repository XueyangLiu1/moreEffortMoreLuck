package org.meml.shared.space;

import org.meml.shared.Board;
import org.meml.shared.Player;

public class ForkSpace extends BasicSpace implements RouteChoosable{

    public ForkSpace(Board board, String title, String detail) {
        super(board, title, detail);
    }


    @Override
    public boolean checkQualification(Player player) {
        return false;
    }

    @Override
    public void enterBranch(Board board, Player player) {

    }
}
