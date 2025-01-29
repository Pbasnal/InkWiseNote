package com.originb.inkwisenote.modules;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

// test code
public class LayoutEngine {

    private ViewGroup viewGroup;
    private Context context;

    private Map<Integer, View> viewElementsMap;

    public LayoutEngine(ViewGroup viewGroup, Context context) {
        this.viewGroup = viewGroup;
        this.context = context;
        this.viewElementsMap = new HashMap<>();
    }


    // LinearLayout.VERTICAL
    public static LayoutEngine linearLayout(Context context, int orientation) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(orientation);

        return new LayoutEngine(layout, context);
    }

    public LayoutEngine addView(Function<LayoutEngine, Integer, Context, View> createView) {
        viewGroup.addView(createView.apply(this, viewElementsMap.size(), context));
        return this;
    }

    public static SeekBar createSeekbar(LayoutEngine layoutEngine, int viewId, Context context) {
        return layoutEngine.createViewElement(viewId, () -> new SeekBar(context));
    }

    public static TextView createTextView(LayoutEngine layoutEngine, int viewId, Context context) {
        return layoutEngine.createViewElement(viewId, () -> new TextView(context));
    }

    private <T extends View> T createViewElement(int viewId, Callable<T> viewCreator) {
        if (viewElementsMap.containsKey(viewId)) {
            Log.e("LayoutEngine", "View with id " + viewId + " already exists");
            throw new RuntimeException("View with id " + viewId + " already exists");
        }
        try {
            T view = viewCreator.call();
            view.setId(viewId);
            viewElementsMap.put(viewId, view);
            return view;
        } catch (Exception e) {
            Log.e("LayoutEngine", "Error creating view element", e);
            throw new RuntimeException(e);
        }
    }

    public ViewGroup build() {
        return viewGroup;
    }

}
