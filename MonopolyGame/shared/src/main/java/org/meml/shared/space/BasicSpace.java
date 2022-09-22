package org.meml.shared.space;

import lombok.Data;
import org.meml.shared.Board;

@Data
public class BasicSpace {
    private Board board;
    private BasicSpace nextSpace;
    private String title;
    private String detail;
    public BasicSpace(Board board, String title, String detail) {
        this.board = board;
        this.title = title;
        this.detail = detail;
    }

    public String getTextRepresentation() {
        return this.title;
    }

}
