package org.meml.shared.space;

import lombok.Data;
import org.meml.shared.Board;

@Data
public class BasicSpace {
    Board board;
    BasicSpace nextSpace;

    public BasicSpace(Board board) {
        this.board = board;
    }



}
