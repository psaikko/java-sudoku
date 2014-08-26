/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Sudoku;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import DataStructures.Entry;
import DataStructures.Point;

/**
 *
 * @author psaikko
 */
public class SudokuComponent extends JComponent {
    private SudokuState sudoku;
    private StepDescription desc;

    final int squareSize = 50;

    final int textSize = 14;
    final Font smallFont = new Font("SansSerif", Font.PLAIN, squareSize / 3);
    final Font largeFont = new Font("SansSerif", Font.PLAIN, squareSize);
    final Font textFont = new Font("SansSerif", Font.PLAIN, textSize);

    public void setSudoku(SudokuState sudoku) {
        this.sudoku = sudoku;
    }

    public void setDescription(StepDescription desc) {
        this.desc = desc;
    }

    public SudokuComponent() {
        super();
        this.setPreferredSize(new Dimension(squareSize * 9, squareSize * 9 + textSize * 2));
    }

    @Override
    public void paint(Graphics g) {
        if (sudoku == null) {
            g.setColor(Color.red);
            g.fillRect(0, 0, 50, 50);
        } else {
            g.setColor(Color.LIGHT_GRAY);
            for (int y = 1; y < 9; y++)
                g.drawLine(0, y*squareSize, squareSize * 9, y*squareSize);
            for (int x = 1; x < 9; x++)
                g.drawLine(x*squareSize, 0, x*squareSize, squareSize * 9);

            g.setColor(Color.BLACK);
            for (int y = 1; y < 3; y++)
                g.drawLine(0, y*squareSize*3, squareSize * 9, y*squareSize*3);
            for (int x = 1; x < 3; x++)
                g.drawLine(x*squareSize*3, 0, x*squareSize*3, squareSize * 9);

            if (desc != null) {
                for (Entry<Point, Color> pointColor : desc.getPointColors().entrySet()) {
                    g.setColor(pointColor.getValue());
                    Point p = pointColor.getKey();
                    g.fillRect(p.x * squareSize + 1, p.y * squareSize + 1, squareSize - 1, squareSize - 1);
                }

                g.setColor(Color.BLACK);
                g.setFont(textFont);
                g.drawString(desc.getDescription(), 0, squareSize * 9 + (textSize * 3) / 2);
            }

            for (int y = 0; y < 9; y++)
                for (int x = 0; x < 9; x++) {
                    int x2 = x * squareSize;
                    int y2 = y * squareSize;

                    if (sudoku.getSquare(x, y).isSolved()) {
                        g.setColor(Color.BLACK);
                        g.setFont(largeFont);
                        y2 += (g.getFont().getSize() * 9) / 10;
                        x2 += g.getFont().getSize() / 5;
                        g.drawString(sudoku.getSquare(x, y).getNumber().toString(), x2, y2);
                    } else {
                        g.setColor(Color.DARK_GRAY);
                        g.setFont(smallFont);
                        y2 += g.getFont().getSize();
                        x2 += g.getFont().getSize() / 5;
                        for (Integer choice : sudoku.getSquare(x, y).getCandidates()) 
                            g.drawString(choice.toString(), x2 + ((choice-1) % 3) * squareSize / 3,
                                    y2 + ((choice-1) / 3) * squareSize / 3);
                    }
                }


            if (desc != null && desc.getLinks() != null)
                for (Entry<Point, Point> link : desc.getLinks()) {
                    int x1 = link.getKey().x * squareSize + squareSize / 2;
                    int y1 = link.getKey().y * squareSize + squareSize / 2;
                    int x2 = link.getValue().x * squareSize + squareSize / 2;
                    int y2 = link.getValue().y * squareSize + squareSize / 2;

                    Graphics2D g2 = (Graphics2D)g;
                    QuadCurve2D curve = new QuadCurve2D.Float();

                    int len = (int)Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));

                    int ctrlx = (x1 + x2) / 2;
                    int ctrly = (y1 + y2) / 2;

                    if (x1 > x2)
                        ctrly -= len / 9;
                    if (x1 < x2)
                        ctrly += len / 9;
                    if (y1 > y2)
                        ctrlx += len / 9;
                    if (y1 < y2)
                        ctrlx -= len / 9;

                    g2.setStroke(new BasicStroke(2.0f));
                    g2.setPaint(Color.RED);
                    curve.setCurve(x1, y1, ctrlx, ctrly, x2, y2);
                    g2.draw(curve);
                }

        }
    }
}
