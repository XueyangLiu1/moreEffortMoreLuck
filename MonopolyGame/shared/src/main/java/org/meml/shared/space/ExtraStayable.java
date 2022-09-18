package org.meml.shared.space;

import org.meml.shared.Player;

public interface ExtraStayable {
    void doExtraStay(Player player);

    boolean ableToStayExtra(Player player);
}
