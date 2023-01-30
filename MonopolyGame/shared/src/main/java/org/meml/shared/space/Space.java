package org.meml.shared.space;

import org.meml.shared.Player;

public interface Space {
    void triggerStayAction(Player player);

    void triggerPassAction(Player player);
}
