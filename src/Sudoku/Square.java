/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Sudoku;

import DataStructures.LinkedList;
import DataStructures.Point;
/**
 *
 * @author Paul
 */
public class Square {
    private Integer number;
    private LinkedList<Integer> candidates;
    private boolean solved = false;
    private int x;
    private int y;

    public Square (LinkedList<Integer> candidates, int x, int y) {
        this.candidates = candidates;
        this.x = x;
        this.y = y;
    }

    public Square(Integer number, int x, int y) {
        this.number = number;
        this.solved = true;
        this.x = x;
        this.y = y;
    }

    public Square(Square other) {
        if (other.solved) {
            solved = true;
            number = new Integer(other.number);
        } else {
            candidates = new LinkedList<Integer>();
            for (int i : other.candidates)
                candidates.add(new Integer(i));
        }
        x = other.x;
        y = other.y;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
        if (number != null)
            solved = true;
    }

    public LinkedList<Integer> getCandidates() {
        return this.candidates;
    }

    public boolean isSolved() {
        return solved;
    }
    
    public Point getPoint() {
        return new Point(x, y);
    }

    public boolean sameRow(Square other) {
        return y == other.y;
    }

    public boolean sameCol(Square other) {
        return x == other.x;
    }

    public boolean sameBox(Square other) {
        return (x / 3 == other.x / 3) && (y / 3 == other.y / 3);
    }


    public int getX() { return this.x; }

    public int getY() { return this.y; }
}
