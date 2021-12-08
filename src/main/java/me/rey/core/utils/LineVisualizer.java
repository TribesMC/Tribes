package me.rey.core.utils;

import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LineVisualizer {

    public final List<Vector> points = new ArrayList<>();

    public LineVisualizer(int concentration, Vector... line) {
        addPoints(concentration, line);
    }

    public LineVisualizer addPoints(int concentration, Vector... line) {
        List<Vector> vectors = new ArrayList<>(Arrays.asList(line));
        if (vectors.size() >= 2) {
            int pointsPerLine = concentration / (vectors.size() / 2);
            for (int i = 1; i < vectors.size(); i++) {
                addPoints(vectors.get(i - 1), vectors.get(i), pointsPerLine);
            }
            return this;
        } else {
            throw new IllegalArgumentException("there must be 2 or more vectors!");
        }
    }

    public void addPoints(Vector start, Vector end, int points) {
        Vector increment = start.clone().subtract(end).divide(new Vector(points, points, points));
        Vector last = start;
        for (int i = 0; i < points; i++) {
            this.points.add(last);
            last = last.clone().subtract(increment);
        }
    }
}
