package org.slk200.pdfreaderv26.cell;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.image.ImageView;
import org.slk200.pdfreaderv26.constant.FileType;
import org.slk200.pdfreaderv26.constant.ImageSource;

/**
 * 文件类型TableCell
 */
public class FileTypeTableCell<S> extends TableCell<S, FileType> {

    public FileTypeTableCell() {
        setAlignment(Pos.CENTER);
    }

    @Override
    protected void updateItem(FileType item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
            return;
        }

        if (getGraphic() != null && getUserData().equals(item)) {
            return;
        }

        ImageView targetIcon = switch (item) {
            case PDF -> new ImageView(ImageSource.OFFICE_PDF);
            case WORD -> new ImageView(ImageSource.OFFICE_DOC);
            case IMAGE -> new ImageView(ImageSource.OFFICE_PIC);
            case EXCEL -> new ImageView(ImageSource.OFFICE_ELS);
            case PPT -> new ImageView(ImageSource.OFFICE_PPT);
            case OTHER -> new ImageView(ImageSource.OFFICE_OTHER);
        };
        setGraphic(targetIcon);
        setUserData(item);
    }
}
