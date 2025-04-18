package com.originb.inkwisenote2.modules.handwrittennotes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.originb.inkwisenote2.common.ScreenUtils;
import com.originb.inkwisenote2.config.ConfigReader;
import com.originb.inkwisenote2.modules.handwrittennotes.data.PageTemplate;
import lombok.Getter;
import lombok.Setter;

public class RuledPageBackground {

    @Getter
    @Setter
    private PageTemplate pageTemplate;
    private Bitmap pageTemplateBitmap;
    private Canvas templateCanvas;
    private final Context context;

    private ConfigReader configReader;

    public RuledPageBackground(Context context, ConfigReader configReader, int width, int height) {
        this.context = context;

        this.configReader = configReader;

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
        float density = context.getResources().getDisplayMetrics().density;

        int canvasWidth = templateCanvas.getWidth();
        int canvasHeight = templateCanvas.getHeight();
        int lineSpacing = ScreenUtils.pxToDp(pageTemplate.getLineSpacing(), context);  // Space between each line, you can change to your desired value

        int color = Color.parseColor(pageTemplate.getLineColor());
        Paint linePaint = new Paint();
        linePaint.setColor(color);
        linePaint.setStrokeWidth(pageTemplate.getLineWidth() * density); // You can change the thickness of the lines here

        // Draw horizontal lines
        for (int y = lineSpacing; y < canvasHeight; y += lineSpacing) {
            templateCanvas.drawLine(0, y, canvasWidth, y, linePaint);
        }

        return pageTemplateBitmap;
    }

    private PageTemplate loadPageTemplate() {
        return configReader.getAppConfig().getPageTemplates().get(PageBackgroundType.BASIC_RULED_PAGE_TEMPLATE.name());
    }
}
