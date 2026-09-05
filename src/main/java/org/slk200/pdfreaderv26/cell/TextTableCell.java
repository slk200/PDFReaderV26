package org.slk200.pdfreaderv26.cell;

import javafx.geometry.Pos;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.scene.text.TextAlignment;

/**
 * 文本TableCell，带Tooltip
 */
public class TextTableCell<S> extends TableCell<S, String> {

    public TextTableCell() {
        setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
        setTextAlignment(TextAlignment.LEFT);
    }

    public TextTableCell(Pos alignment) {
        setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
        setAlignment(alignment);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setTooltip(null);
            return;
        }

        setText(item);
        setTooltip(new Tooltip(item));
    }
}
