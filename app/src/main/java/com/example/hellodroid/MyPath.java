package com.example.hellodroid;

import android.graphics.Path;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public  class MyPath extends Path implements Serializable {

    public enum PathActionType {LINE_TO, MOVE_TO};
    public static class PathData implements Serializable {
        float x;
        float y;
        PathActionType pathActionType;

        public PathData(float x, float y, PathActionType pathActionType) {
            this.x = x;
            this.y = y;
            this.pathActionType = pathActionType;
        }
    }

    List<PathData> paths = new ArrayList<>();

    @Override
    public void moveTo(float x, float y) {
        paths.add(new PathData(x, y, PathActionType.MOVE_TO));
        super.moveTo(x, y);
    }

    @Override
    public void lineTo(float x, float y) {
        paths.add(new PathData(x, y, PathActionType.LINE_TO));
        super.lineTo(x, y);
    }

    public void loadThisPath() {
        for (PathData p : paths) {
            if (PathActionType.MOVE_TO.equals(p.pathActionType)) {
                super.moveTo(p.x, p.y);
            } else if (PathActionType.LINE_TO.equals(p.pathActionType)) {
                super.lineTo(p.x, p.y);
            }
        }
    }
}
