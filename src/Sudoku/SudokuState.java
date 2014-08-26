/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Sudoku;

import DataStructures.LinkedList;

/**
 *
 * @author psaikko
 */
public class SudokuState {

    public enum Status { Solved, Unfinished, Incorrect };

    private Square[][] sudoku = new Square[9][9];
    private Status state;

    public SudokuState(int[][] rawData) {
        for (int y = 0; y < 9; y++)
            for (int x = 0; x < 9; x++) {
                if (rawData[y][x] != 0) {
                    sudoku[y][x] = new Square(rawData[y][x], x, y);
                }
                else {
                    LinkedList<Integer> choices = new LinkedList<Integer>();
                    for(int i = 1; i < 10; i++)
                        choices.add(i);
                    sudoku[y][x] = new Square(choices, x, y);
                }
            }
        state = Status.Unfinished;
    }

    public SudokuState(SudokuState other) {
        for (int y = 0; y < 9; y++)
            for (int x = 0; x < 9; x++)
                sudoku[y][x] = new Square(other.getSquare(x, y));
        state = other.state;
    }

    public SudokuState(Square[][] sudoku, SudokuState.Status state) {
        this.sudoku = sudoku;
        this.state = state;
    }

    public SudokuState tryNumber(int x, int y, int number) {
        SudokuState guess = new SudokuState(this);
        guess.getSquare(x, y).setNumber(number);
        return guess;
    }

    public SudokuState makeCopy() {
        return new SudokuState(this);
    }

    public Square getSquare(int x, int y) {
        return sudoku[y][x];
    }

    public Square[][] getSudoku() {
        return sudoku;
    }

    public boolean isSolved() {
        return state == Status.Solved;
    }

    public boolean isIllegal() {
        return state == Status.Incorrect;
    }

    public boolean isUnfinished() {
        return state == Status.Unfinished;
    }

    public void setStatus(SudokuState.Status st) {
        this.state = st;
    }

    public LinkedList<Square> getAll(boolean includeSolved) {
        LinkedList<Square> allList = new LinkedList<Square>();
        for (int row = 0; row < 9; row++)
            for (int col = 0; col < 9; col++)
                if (!sudoku[row][col].isSolved() || includeSolved)
                    allList.add(sudoku[row][col]);
        return allList;
    }

    public LinkedList<Square> getRow(int row, boolean includeSolved) {
        LinkedList<Square> rowList = new LinkedList<Square>();
        for(int col = 0; col < 9; col++)
            if (!sudoku[row][col].isSolved() || includeSolved)
                rowList.add(sudoku[row][col]);
        return rowList;
    }

    public LinkedList<Square> getCol(int col, boolean includeSolved) {
        LinkedList<Square> colList = new LinkedList<Square>();
        for(int row = 0; row < 9; row++)
            if (!sudoku[row][col].isSolved() || includeSolved)
                colList.add(sudoku[row][col]);
        return colList;
    }

    public LinkedList<Square> getBox(int x, int y, boolean includeSolved) {
        LinkedList<Square> boxList = new LinkedList<Square>();
        for(int row = y * 3; row < (y + 1) * 3; row++)
            for(int col = x * 3; col < (x + 1) * 3; col++)
                if (!sudoku[row][col].isSolved() || includeSolved)
                    boxList.add(sudoku[row][col]);
        return boxList;
    }
}
