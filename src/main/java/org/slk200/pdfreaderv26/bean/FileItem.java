package org.slk200.pdfreaderv26.bean;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slk200.pdfreaderv26.constant.FileState;
import org.slk200.pdfreaderv26.constant.FileType;

/**
 * 计数的文件
 */
public class FileItem {
    private ObjectProperty<FileType> file_type;
    private StringProperty file_name;
    private ObjectProperty<FileState> file_state;
    private StringProperty file_page;
    private StringProperty file_path;
    private StringProperty file_note;

    public FileItem() {
    }

    public FileItem(FileType file_type, String file_name, FileState file_state, String file_page, String file_path) {
        this.file_type = new SimpleObjectProperty<>(file_type);
        this.file_name = new SimpleStringProperty(file_name);
        this.file_state = new SimpleObjectProperty<>(file_state);
        this.file_page = new SimpleStringProperty(file_page);
        this.file_path = new SimpleStringProperty(file_path);
        this.file_note = new SimpleStringProperty("纸张类型：普通纸 80g；纸张大小：A4；单双面：单面；颜色：黑白；页数范围：所有页；打印份数：1份；封装工艺：骑马钉");
    }

    public FileType getFile_type() {
        return file_type.get();
    }

    public ObjectProperty<FileType> file_typeProperty() {
        return file_type;
    }

    public void setFile_type(FileType file_type) {
        this.file_type.set(file_type);
    }

    public String getFile_note() {
        return file_note.get();
    }

    public StringProperty file_noteProperty() {
        return file_note;
    }

    public void setFile_note(String file_note) {
        this.file_note.set(file_note);
    }

    public String getFile_name() {
        return file_name.get();
    }

    public StringProperty file_nameProperty() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name.set(file_name);
    }

    public FileState getFile_state() {
        return file_state.get();
    }

    public ObjectProperty<FileState> file_stateProperty() {
        return file_state;
    }

    public void setFile_state(FileState file_state) {
        this.file_state.set(file_state);
    }

    public String getFile_path() {
        return file_path.get();
    }

    public StringProperty file_pathProperty() {
        return file_path;
    }

    public void setFile_path(String file_path) {
        this.file_path.set(file_path);
    }

    public String getFile_page() {
        return file_page.get();
    }

    public StringProperty file_pageProperty() {
        return file_page;
    }

    public void setFile_page(String file_page) {
        this.file_page.set(file_page);
    }
}
