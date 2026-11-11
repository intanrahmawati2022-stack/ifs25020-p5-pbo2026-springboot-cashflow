package org.delcom.ifs25020springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadHandler;

/** Upload satu gambar (disimpan di memori sampai form disubmit). */
public class ImageUpload extends Div {
    private final Upload upload;
    private byte[] data;
    private String fileName;

    public ImageUpload(String buttonLabel) {
        upload = new Upload(UploadHandler.inMemory((metadata, bytes) -> {
            this.data = bytes;
            this.fileName = metadata.fileName();
        }));
        upload.setMaxFiles(1);
        upload.setMaxFileSize(5 * 1024 * 1024);
        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/gif", "image/webp");
        upload.setUploadButton(new Button(buttonLabel, VaadinIcon.UPLOAD.create()));
        upload.getElement().addEventListener("file-remove", e -> clearData());
        add(upload);
    }

    public boolean hasFile() {
        return data != null && fileName != null;
    }

    public byte[] bytes() {
        return data;
    }

    public String fileName() {
        return fileName;
    }

    private void clearData() {
        data = null;
        fileName = null;
    }

    public void reset() {
        clearData();
        upload.clearFileList();
    }
}
