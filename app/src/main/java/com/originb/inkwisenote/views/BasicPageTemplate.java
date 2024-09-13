package com.originb.inkwisenote.views;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.originb.inkwisenote.config.ConfigReader;
import com.originb.inkwisenote.data.config.PageTemplate;
import lombok.Getter;
import lombok.Setter;

public class BasicPageTemplate {

    @Getter
    @Setter
    private PageTemplate pageTemplate;
    private Bitmap pageTemplateBitmap;
    private Canvas templateCanvas;


    private ConfigReader configReader;

    public BasicPageTemplate(int width, int height) {
        configReader = ConfigReader.getInstance();

        pageTemplateBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        templateCanvas = new Canvas(pageTemplateBitmap);

        pageTemplate = loadPageTemplate();


    }

    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != oldw || h != oldh) {
            pageTemplateBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            templateCanvas = new Canvas(pageTemplateBitmap);
        }
    }

    public Bitmap drawTemplate() {
        int canvasWidth = templateCanvas.getWidth();
        int canvasHeight = templateCanvas.getHeight();
        int lineSpacing = pageTemplate.getLineSpacing();  // Space between each line, you can change to your desired value

        int color = Color.parseColor(pageTemplate.getLineColor());
        Paint linePaint  = new Paint();
        linePaint.setColor(color);
        linePaint.setStrokeWidth(pageTemplate.getLineWidth()); // You can change the thickness of the lines here

        // Draw horizontal lines
        for (int y = lineSpacing; y < canvasHeight; y += lineSpacing) {
            templateCanvas.drawLine(0, y, canvasWidth, y, linePaint);
        }

        return pageTemplateBitmap;
    }

    private PageTemplate loadPageTemplate() {
        return configReader.getAppConfig().getPageTemplates().get(PageTemplateType.BASIC_RULED_PAGE_TEMPLATE.name());
    }
}
