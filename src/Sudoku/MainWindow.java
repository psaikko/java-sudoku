package Sudoku;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.event.*;
import javax.swing.*;
import java.io.File;
import java.awt.FileDialog;
import java.util.Scanner;

/**
 *
 * @author psaikko
 */
public class MainWindow extends JFrame implements MouseListener {
    SudokuComponent display;
    JButton undoAll;
    JButton solve;
    JButton forward;
    JButton undo;
    Solver solver;

    public MainWindow() {
        super();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        if (getSudoku() == false) {
            dispose();
            return;
        }

        JPanel panel = new JPanel();

        display = new SudokuComponent();
        display.setSudoku(solver.getSudokuState());
        panel.add(display);

        undoAll = new JButton("<<");
        undoAll.addMouseListener(this);
        panel.add(undoAll);

        undo = new JButton("<");
        undo.addMouseListener(this);
        panel.add(undo);

        forward = new JButton(">");
        forward.addMouseListener(this);
        panel.add(forward);

        solve = new JButton(">>");
        solve.addMouseListener(this);
        panel.add(solve);

        add(panel);
        pack();
        setVisible(true);
    }

    private boolean getSudoku() {
        int[][] sudoku = new int[9][9];

        try {
            FileDialog fd = new FileDialog(this);
            fd.setVisible(true);

            if (fd.getFile() != null) {
                this.setTitle(fd.getFile());
                Scanner sc = new Scanner(new File(fd.getDirectory() + fd.getFile()));
                for (int y = 0; y < 9; y++) {
                    String s = sc.nextLine();
                    for (int x = 0; x < 9; x++) {
                        int c = s.charAt(x) - '0';
                        if (c > 0 && c < 10)
                            sudoku[y][x] = c;
                        else
                            sudoku[y][x] = 0;
                    }
                }
                this.solver = new Solver(sudoku);
            } else {
                throw new Exception("No file selected.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: "+e.getMessage());
            return false;
        }


        return true;
    }

    boolean showChanges = false;
    public void forward() {
        if (!showChanges) {
            StepDescription sd = solver.doStep();
            display.setDescription(sd);
        } else {
            display.setSudoku(solver.getSudokuState());
        }
        showChanges = !showChanges;
        display.repaint();
    }

    public void undo() {
        solver.undoStep();
        display.setDescription(null);
        display.setSudoku(solver.getSudokuState());
        showChanges = false;
        display.repaint();
    }

    public void solve() {
        long time = solver.solve();
        JOptionPane.showMessageDialog(this, "Solved sudoku in "+(time/1000/1000)+"ms");
        display.setSudoku(solver.getSudokuState());
        display.setDescription(null);
        showChanges = false;
        display.repaint();
    }

    public void undoAll() {
        solver.undoAll();
        display.setSudoku(solver.getSudokuState());
        display.setDescription(null);
        showChanges = false;
        display.repaint();
    }

    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {
        if ((JButton)e.getSource() == forward) {
            forward();
        }

        if ((JButton)e.getSource() == undo) {
            undo();
        }

        if ((JButton)e.getSource() == undoAll) {
            undoAll();
        }

        if ((JButton)e.getSource() == solve) {
            solve();
        }
    }
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {
        
    }
}
