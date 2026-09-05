package org.slk200.pdfreaderv26.constant;

import javafx.scene.image.Image;

import java.util.Objects;

/**
 * 图片资源
 * <note>Java类中用到的图片，统一管理</note>
 */
public class ImageSource {
    public static final Image LOGO = new Image(Objects.requireNonNull(ImageSource.class.getResourceAsStream("/org/slk200/pdfreaderv26/image/logo@256px.png")));

    public static final Image STATE_FAILED = new Image(Objects.requireNonNull(ImageSource.class.getResourceAsStream("/org/slk200/pdfreaderv26/image/failed@16px.png")));
    public static final Image STATE_DONE = new Image(Objects.requireNonNull(ImageSource.class.getResourceAsStream("/org/slk200/pdfreaderv26/image/done@16px.png")));
    public static final Image STATE_NO_NEED = new Image(Objects.requireNonNull(ImageSource.class.getResourceAsStream("/org/slk200/pdfreaderv26/image/no_need@16px.png")));

    public static final Image OFFICE_DOC = new Image(Objects.requireNonNull(ImageSource.class.getResourceAsStream("/org/slk200/pdfreaderv26/image/office_doc@16px.png")));
    public static final Image OFFICE_ELS = new Image(Objects.requireNonNull(ImageSource.class.getResourceAsStream("/org/slk200/pdfreaderv26/image/office_els@16px.png")));
    public static final Image OFFICE_PPT = new Image(Objects.requireNonNull(ImageSource.class.getResourceAsStream("/org/slk200/pdfreaderv26/image/office_ppt@16px.png")));
    public static final Image OFFICE_PDF = new Image(Objects.requireNonNull(ImageSource.class.getResourceAsStream("/org/slk200/pdfreaderv26/image/office_pdf@16px.png")));
    public static final Image OFFICE_PIC = new Image(Objects.requireNonNull(ImageSource.class.getResourceAsStream("/org/slk200/pdfreaderv26/image/office_image@16px.png")));
    public static final Image OFFICE_OTHER = new Image(Objects.requireNonNull(ImageSource.class.getResourceAsStream("/org/slk200/pdfreaderv26/image/office_other@16px.png")));

    public static final Image CONVERT = new Image(Objects.requireNonNull(ImageSource.class.getResourceAsStream("/org/slk200/pdfreaderv26/image/convert@64px.png")));

    public static final Image TICKET_COMPLETE = new Image(Objects.requireNonNull(ImageSource.class.getResourceAsStream("/org/slk200/pdfreaderv26/image/complete@32px.png")));
    public static final Image TICKET_UN_COMPLETE = new Image(Objects.requireNonNull(ImageSource.class.getResourceAsStream("/org/slk200/pdfreaderv26/image/uncomplete@32px.png")));
}
