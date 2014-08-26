/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Sudoku;

import DataStructures.Point;
import DataStructures.HashMap;
import DataStructures.Entry;
import DataStructures.LinkedList;
import java.awt.Color;
/**
 *
 * @author Paul
 */
public class StepDescription {
    private String description;
    private HashMap<Point, Color> colors;
    private LinkedList<Entry<Point, Point>> links = new LinkedList<Entry<Point, Point>>();

    public StepDescription() {
        description = "";
        colors = new HashMap<Point, Color>();
    }

    public String getDescription() {
        return this.description;
    }

    public HashMap<Point, Color> getPointColors() {
        return colors;
    }

    public void setDescription(String s) {
        description = s;
    }

    public void addPointColor(Point p, Color c) {
        colors.put(p, c);
    }

    public LinkedList<Entry<Point, Point>> getLinks() {
        return links;
    }

    public void setLinks(LinkedList<Entry<Point, Point>> links) {
        if (links != null)
            this.links = links;
    }

    public void addLink(Point p1, Point p2) {
        links.add(new Entry<Point, Point>(p1, p2));
    }
}
