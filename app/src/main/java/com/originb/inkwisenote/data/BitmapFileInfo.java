package com.originb.inkwisenote.data;

import android.graphics.Bitmap;
import com.originb.inkwisenote.filemanager.FileInfo;
import com.originb.inkwisenote.filemanager.FileType;
import lombok.Getter;

@Getter
public class BitmapFileInfo extends FileInfo<Bitmap> {
    public BitmapFileInfo(String filePath, Bitmap bitmap) {
        super(filePath, FileType.BITMAP, bitmap);
        this.extraFields.put("scale", 1f);
    }

    public BitmapFileInfo(String filePath) {
        super(filePath, FileType.BITMAP, Bitmap.class);
        this.extraFields.put("scale", 1f);
    }

    public BitmapFileInfo(String filePath, float scale) {
        super(filePath, FileType.BITMAP, Bitmap.class);

        if(scale < 0 || scale > 1) {
            throw new IllegalArgumentException("Scale must be between 0 and 1");
        }
        this.extraFields.put("scale", scale);
    }
}
