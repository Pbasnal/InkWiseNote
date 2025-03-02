package com.originb.inkwisenote.testutils;

import java.util.ArrayList;
import java.util.List;

public class HandwritingStrokeHelper {

    public static class Point {
        public float x, y;

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    // Base coordinates for writing
    private static float START_X = 100;
    private static float BASE_Y = 300;
    private static float LETTER_SPACING = 50;

    public static List<List<Point>> getHelloWorldStrokes(float x, float y) {
        List<List<Point>> allStrokes = new ArrayList<>();
        START_X += x;
        BASE_Y += y;
        float currentX = START_X;

        // "H"
        allStrokes.add(createVerticalLine(currentX, BASE_Y, 150)); // left vertical
        allStrokes.add(createHorizontalLine(currentX, currentX + 30, BASE_Y + 50)); // middle horizontal
        allStrokes.add(createVerticalLine(currentX + 30, BASE_Y, 150)); // right vertical
        currentX += LETTER_SPACING;

        // "e"
        allStrokes.add(createLowerE(currentX, BASE_Y));
        currentX += LETTER_SPACING;

        // "l"
        allStrokes.add(createVerticalLine(currentX, BASE_Y - 50, 100));
        currentX += LETTER_SPACING;

        // "l"
        allStrokes.add(createVerticalLine(currentX, BASE_Y - 50, 100));
        currentX += LETTER_SPACING;

        // "o"
        allStrokes.add(createLowerO(currentX, BASE_Y));
        currentX += LETTER_SPACING + 20;

        // Space between words
        currentX += LETTER_SPACING;

        // "W"
        allStrokes.add(createUpperW(currentX, BASE_Y));
        currentX += LETTER_SPACING + 20;

        // "o"
        allStrokes.add(createLowerO(currentX, BASE_Y));
        currentX += LETTER_SPACING;

        // "r"
        allStrokes.add(createLowerR(currentX, BASE_Y));
        currentX += LETTER_SPACING;

        // "l"
        allStrokes.add(createVerticalLine(currentX, BASE_Y - 50, 100));
        currentX += LETTER_SPACING;

        // "d"
        allStrokes.add(createLowerD(currentX, BASE_Y));
        currentX += LETTER_SPACING;

        // "!"
        allStrokes.add(createExclamationMark(currentX, BASE_Y));

        return allStrokes;
    }

    private static List<Point> createVerticalLine(float x, float startY, float height) {
        List<Point> points = new ArrayList<>();
        for (int i = 0; i <= height; i += 2) {
            points.add(new Point(x, startY + i));
        }
        return points;
    }

    private static List<Point> createHorizontalLine(float startX, float endX, float y) {
        List<Point> points = new ArrayList<>();
        for (float x = startX; x <= endX; x += 2) {
            points.add(new Point(x, y));
        }
        return points;
    }

    private static List<Point> createLowerE(float x, float y) {
        List<Point> points = new ArrayList<>();
        float radius = 15;

        // Create a circular motion for 'e'
        for (int i = 45; i <= 360; i += 5) {
            double angle = Math.toRadians(i);
            float pointX = x + (float) (radius * Math.cos(angle));
            float pointY = y + (float) (radius * Math.sin(angle));
            points.add(new Point(pointX, pointY));
        }

        // Add the middle horizontal stroke
        for (float i = x - radius; i <= x + radius; i += 2) {
            points.add(new Point(i, y));
        }

        return points;
    }

    private static List<Point> createLowerO(float x, float y) {
        List<Point> points = new ArrayList<>();
        float radius = 15;

        for (int i = 0; i <= 360; i += 5) {
            double angle = Math.toRadians(i);
            float pointX = x + (float) (radius * Math.cos(angle));
            float pointY = y + (float) (radius * Math.sin(angle));
            points.add(new Point(pointX, pointY));
        }

        return points;
    }

    private static List<Point> createUpperW(float x, float y) {
        List<Point> points = new ArrayList<>();
        float width = 40;
        float height = 50;

        // First diagonal down
        for (float i = 0; i <= width / 4; i += 2) {
            points.add(new Point(x + i, y - height + i * 2));
        }

        // First diagonal up
        for (float i = 0; i <= width / 4; i += 2) {
            points.add(new Point(x + width / 4 + i, y - height / 2 - i * 2));
        }

        // Second diagonal down
        for (float i = 0; i <= width / 4; i += 2) {
            points.add(new Point(x + width / 2 + i, y - height + i * 2));
        }

        // Second diagonal up
        for (float i = 0; i <= width / 4; i += 2) {
            points.add(new Point(x + width * 3 / 4 + i, y - height / 2 - i * 2));
        }

        return points;
    }

    private static List<Point> createLowerR(float x, float y) {
        List<Point> points = new ArrayList<>();

        // Vertical line
        for (float i = 0; i <= 30; i += 2) {
            points.add(new Point(x, y - i));
        }

        // Curve at top
        for (int i = 0; i <= 90; i += 5) {
            double angle = Math.toRadians(i);
            float pointX = x + (float) (15 * Math.cos(angle));
            float pointY = (y - 30) + (float) (15 * Math.sin(angle));
            points.add(new Point(pointX, pointY));
        }

        return points;
    }

    private static List<Point> createLowerD(float x, float y) {
        List<Point> points = new ArrayList<>();

        // Vertical line
        for (float i = 0; i <= 100; i += 2) {
            points.add(new Point(x, y - i));
        }

        // Circle part
        float radius = 15;
        for (int i = 0; i <= 360; i += 5) {
            double angle = Math.toRadians(i);
            float pointX = (x - radius) + (float) (radius * Math.cos(angle));
            float pointY = y + (float) (radius * Math.sin(angle));
            points.add(new Point(pointX, pointY));
        }

        return points;
    }

    private static List<Point> createExclamationMark(float x, float y) {
        List<Point> points = new ArrayList<>();

        // Vertical line
        for (float i = 0; i <= 40; i += 2) {
            points.add(new Point(x, y - i));
        }

        // Dot
        for (int i = 0; i <= 360; i += 30) {
            double angle = Math.toRadians(i);
            float pointX = x + (float) (2 * Math.cos(angle));
            float pointY = (y + 10) + (float) (2 * Math.sin(angle));
            points.add(new Point(pointX, pointY));
        }

        return points;
    }
} 