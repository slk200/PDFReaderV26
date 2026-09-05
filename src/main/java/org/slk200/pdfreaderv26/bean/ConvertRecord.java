package org.slk200.pdfreaderv26.bean;

/**
 * Office文件转换记录
 */
public class ConvertRecord {

    private String convert_time;
    private String file_name;
    private String file_path;
    private String file_status;

    public ConvertRecord() {
    }

    public ConvertRecord(String convert_time, String file_name, String file_path, String file_status) {
        this.convert_time = convert_time;
        this.file_name = file_name;
        this.file_path = file_path;
        this.file_status = file_status;
    }

    public void setConvert_time(String convert_time) {
        this.convert_time = convert_time;
    }

    public void setFile_status(String file_status) {
        this.file_status = file_status;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getConvert_time() {
        return convert_time;
    }

    public String getFile_name() {
        return file_name;
    }

    public String getFile_path() {
        return file_path;
    }

    public String getFile_status() {
        return file_status;
    }
}
