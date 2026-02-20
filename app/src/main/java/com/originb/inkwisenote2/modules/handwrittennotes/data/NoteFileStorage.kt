package com.originb.inkwisenote2.modules.handwrittennotes.data

import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import org.json.JSONException
import org.json.JSONObject

object NoteFileStorage {
    fun getImagePath(note: AtomicNoteEntity): String {
        return note.filepath + "/" + note.filename + ".png"
    }

    fun getThumbnailPath(note: AtomicNoteEntity): String {
        return note.filepath + "/" + note.filename + "-t.png"
    }

    fun getTemplatePath(note: AtomicNoteEntity): String {
        return note.filepath + "/" + note.filename + ".pt"
    }

    fun getMarkdownPath(note: AtomicNoteEntity): String {
        return note.filepath + "/" + note.filename + ".md"
    }

    fun serializeStrokesToMarkdown(strokes: MutableList<Stroke>): String {
        val markdown = StringBuilder("# Handwritten Note\n\n```inkwise\n")
        for (stroke in strokes) {
            markdown.append(serializeStroke(stroke)).append("\n")
        }
        markdown.append("```\n")
        return markdown.toString()
    }

    fun serializeStroke(stroke: Stroke): String {
        val builder = StringBuilder()
        builder.append("{")

        // Add stroke properties
        builder.append("\"color\":").append(stroke.getColor()).append(",")
        builder.append("\"width\":").append(stroke.getWidth()).append(",")

        // Add points
        builder.append("\"points\":[")
        val points = stroke.getPoints()
        for (i in points.indices) {
            val point = points.get(i)
            builder.append("{")
                .append("\"x\":").append(point.getX()).append(",")
                .append("\"y\":").append(point.getY()).append(",")
                .append("\"p\":").append(point.getPressure()).append(",")
                .append("\"t\":").append(point.getTimestamp())
                .append("}")

            if (i < points.size - 1) {
                builder.append(",")
            }
        }
        builder.append("]")

        builder.append("}")
        return builder.toString()
    }

    @Throws(JSONException::class)
    fun deserializeStroke(strokeStr: String): Stroke {
        val strokeJson = JSONObject(strokeStr)

        val stroke = Stroke()
        stroke.setColor(strokeJson.getInt("color"))
        stroke.setWidth(strokeJson.getDouble("width").toFloat())

        val pointsArray = strokeJson.getJSONArray("points")
        for (i in 0..<pointsArray.length()) {
            val pointJson = pointsArray.getJSONObject(i)
            val x = pointJson.getDouble("x").toFloat()
            val y = pointJson.getDouble("y").toFloat()
            val p = pointJson.getDouble("p").toFloat()
            val t = if (pointJson.has("t")) pointJson.getLong("t") else System.currentTimeMillis()
            val point = StrokePoint(x, y, p, t)
            stroke.addPoint(point)
        }

        return stroke
    }
}
