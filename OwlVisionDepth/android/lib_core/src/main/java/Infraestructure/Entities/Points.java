package Infraestructure.Entities;

import java.util.ArrayList;
import java.util.List;

public class Points {
    private List<Point> points;

    public Points() {
        points = new ArrayList<>();
    }

    public void addPoint(Point point) {
        points.add(point);
    }

    public void deletePoint(Point point) {
        points.remove(point);
    }

    public List<Point> getPoints() {
        return new ArrayList<>(points);
    }
}