package com.originb.inkwisenote.io.ocr;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.util.Log;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.originb.inkwisenote.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class TesseractsOcr {

    private String tessDataPath;

    public TesseractsOcr(Context context) {
        tessDataPath = context.getFilesDir() + "/tesseract/";
        initializeTesseract(context, tessDataPath);

    }

    private void initializeTesseract(Context context, String dataPath) {
        File dir = new File(dataPath + "tessdata/");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, "eng.traineddata");
        if (!file.exists()) {
            try {
                InputStream in = context.getResources().openRawResource(R.raw.eng); // R.raw.eng refers to res/raw/eng.traineddata
                FileOutputStream out = new FileOutputStream(file);

                byte[] buffer = new byte[1024];
                int read = 0;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                out.flush();
                out.close();

            } catch (Exception ex) {
                Log.e("TesseractOcr", "Copying of asset file failed", ex);
            }
        }
    }

    public String extractText(Bitmap bitmap) {
        TessBaseAPI tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.init(tessDataPath, "eng"); // Ensure 'eng' matches your language file
        tessBaseAPI.setImage(bitmap);
        String recognizedText = tessBaseAPI.getUTF8Text();
        tessBaseAPI.end();
        return recognizedText;
    }
}
