package org.slk200.pdfreaderv26.cell;

import javafx.geometry.Insets;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import org.slk200.pdfreaderv26.bean.TicketId;
import org.slk200.pdfreaderv26.constant.ImageSource;

/**
 * 工单编号TableCell，区分已完成/未完成
 */
public class TicketIdListCell extends ListCell<TicketId> {

    private final ImageView imageView;

    public TicketIdListCell() {
        setPadding(new Insets(5));
        imageView = new ImageView();
        imageView.setFitHeight(20);
        imageView.setFitWidth(20);
    }

    @Override
    protected void updateItem(TicketId item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
            setText(null);
            return;
        }

        if (!item.getTicket_state().equals(imageView.getId())) {
            imageView.setImage(item.getTicket_state().equals("1") ? ImageSource.TICKET_COMPLETE : ImageSource.TICKET_UN_COMPLETE);
            setGraphic(imageView);
            imageView.setId(item.getTicket_state());
        }

        if (!item.getTicketId().equals(getText()))
            setText(item.getTicketId());
    }
}
