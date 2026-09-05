package org.slk200.pdfreaderv26.bean;

/**
 * 工单内容
 */
public class TicketContent {

    private String file_name, state, page, file_path, note;

    public TicketContent(String file_name, String state, String page, String file_path, String note) {
        this.file_name = file_name;
        this.state = state;
        this.page = page;
        this.file_path = file_path;
        this.note = note;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getFile_path() {
        return file_path;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
