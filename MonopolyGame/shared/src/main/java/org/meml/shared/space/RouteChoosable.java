package org.meml.shared.space;

import org.meml.shared.Board;
import org.meml.shared.Player;

public interface RouteChoosable {

    boolean checkQualification(Player player);

    void enterBranch(Board board, Player player);
}
