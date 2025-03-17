package com.originb.inkwisenote2.modules.handwrittennotes.ui

import android.graphics.Path
import java.io.Serializable

class WriteablePath : Path(), Serializable {
    enum class PathActionType {
        LINE_TO, MOVE_TO
    }

    class PathData(var x: Float, var y: Float, var pathActionType: PathActionType) : Serializable

    var paths: MutableList<PathData> = ArrayList()

    override fun moveTo(x: Float, y: Float) {
        paths.add(PathData(x, y, PathActionType.MOVE_TO))
        super.moveTo(x, y)
    }

    override fun lineTo(x: Float, y: Float) {
        paths.add(PathData(x, y, PathActionType.LINE_TO))
        super.lineTo(x, y)
    }

    fun loadThisPath() {
        for (p in paths) {
            if (PathActionType.MOVE_TO == p.pathActionType) {
                super.moveTo(p.x, p.y)
            } else if (PathActionType.LINE_TO == p.pathActionType) {
                super.lineTo(p.x, p.y)
            }
        }
    }
}
