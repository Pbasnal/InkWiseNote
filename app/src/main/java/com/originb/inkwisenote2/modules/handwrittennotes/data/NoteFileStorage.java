package com.originb.inkwisenote2.modules.handwrittennotes.data;

import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class NoteFileStorage {
    public static String getImagePath(AtomicNoteEntity note) { return note.getFilepath() + "/" + note.getFilename() + ".png"; }
    public static String getThumbnailPath(AtomicNoteEntity note) { return note.getFilepath() + "/" + note.getFilename() + "-t.png"; }
    public static String getTemplatePath(AtomicNoteEntity note) { return note.getFilepath() + "/" + note.getFilename() + ".pt"; }
    public static String getMarkdownPath(AtomicNoteEntity note) { return note.getFilepath() + "/" + note.getFilename() + ".md"; }

    public static String serializeStrokesToMarkdown(List<Stroke> strokes) {
        StringBuilder markdown = new StringBuilder("# Handwritten Note\n\n```inkwise\n");
        for (Stroke stroke : strokes) {
            markdown.append(serializeStroke(stroke)).append("\n");
        }
        markdown.append("```\n");
        return markdown.toString();
    }

    public static String serializeStroke(Stroke stroke) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");

        // Add stroke properties
        builder.append("\"color\":").append(stroke.getColor()).append(",");
        builder.append("\"width\":").append(stroke.getWidth()).append(",");

        // Add points
        builder.append("\"points\":[");
        List<StrokePoint> points = stroke.getPoints();
        for (int i = 0; i < points.size(); i++) {
            StrokePoint point = points.get(i);
            builder.append("{")
                    .append("\"x\":").append(point.getX()).append(",")
                    .append("\"y\":").append(point.getY()).append(",")
                    .append("\"p\":").append(point.getPressure()).append(",")
                    .append("\"t\":").append(point.getTimestamp())
                    .append("}");

            if (i < points.size() - 1) {
                builder.append(",");
            }
        }
        builder.append("]");

        builder.append("}");
        return builder.toString();
    }

    public static Stroke deserializeStroke(String strokeStr) throws JSONException {
        JSONObject strokeJson = new JSONObject(strokeStr);

        Stroke stroke = new Stroke();
        stroke.setColor(strokeJson.getInt("color"));
        stroke.setWidth((float) strokeJson.getDouble("width"));

        JSONArray pointsArray = strokeJson.getJSONArray("points");
        for (int i = 0; i < pointsArray.length(); i++) {
            JSONObject pointJson = pointsArray.getJSONObject(i);
            float x = (float) pointJson.getDouble("x");
            float y = (float) pointJson.getDouble("y");
            float p = (float) pointJson.getDouble("p");
            long t = pointJson.has("t") ? pointJson.getLong("t") : System.currentTimeMillis();
            StrokePoint point = new StrokePoint(x, y, p, t);
            stroke.addPoint(point);
        }

        return stroke;
    }
}
