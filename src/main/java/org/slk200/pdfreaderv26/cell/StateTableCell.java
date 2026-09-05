package org.slk200.pdfreaderv26.cell;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.image.ImageView;
import org.slk200.pdfreaderv26.bean.FileItem;
import org.slk200.pdfreaderv26.constant.ImageSource;
import org.slk200.pdfreaderv26.constant.FileState;

/**
 * 文件状态TableCell
 */
public class StateTableCell extends TableCell<FileItem, FileState> {

    public StateTableCell() {
        setAlignment(Pos.CENTER);
    }

    @Override
    protected void updateItem(FileState item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        String targetText;
        ImageView targetIcon;

        switch (item) {
            case WITHOUT_CONVERT:
                targetText = "无需转换";
                targetIcon = new ImageView(ImageSource.STATE_NO_NEED);
                break;
            case CONVERT_DONE:
                targetText = "转换完成";
                targetIcon = new ImageView(ImageSource.STATE_DONE);
                break;
            case FAILED:
                targetText = "转换失败";
                targetIcon = new ImageView(ImageSource.STATE_FAILED);
                break;
            default:
                setText(null);
                setGraphic(null);
                return;
        }

        if (!targetText.equals(getText())) {
            setText(targetText);
            setGraphic(targetIcon);
        }
    }
}