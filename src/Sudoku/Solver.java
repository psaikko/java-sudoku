package Sudoku;

import DataStructures.Stack;
import DataStructures.HashMap;
import DataStructures.Entry;
import DataStructures.Point;
import java.awt.Color;
import DataStructures.LinkedList;

public class Solver {

    final Color primaryColor = new Color(255,140,0);
    final Color secondaryColor = new Color(255,215,0);
    final Color tertiaryColor = new Color(255,255,0);

    final Color onColor1 = new Color(0,191,255);
    final Color offColor1 = new Color(0,255,255);

    final Color onColor2 = new Color(127,255,0);
    final Color offColor2 = new Color(50,205,50);

    final Color chainStartColor = onColor1;
    final Color chainMainColor = onColor2;

    public Solver(int[][] sudokuGrid) {
        sudokuStack = new Stack<SudokuState>();
        backupStack = new Stack<SudokuState>();
        sudokuStack.push(new SudokuState(sudokuGrid));
    }

    Stack<SudokuState> sudokuStack;
    Stack<SudokuState> backupStack;

    public boolean isSolved() {
        return sudokuStack.peek().isSolved();
    }

    public void undoStep() {
        if (sudokuStack.size() > 1) {
            SudokuState s = sudokuStack.pop();
            if (!backupStack.empty())
                if (backupStack.peek().equals(s))
                    backupStack.pop();
        }
    }

    public void undoAll() {
        while (sudokuStack.size() > 1) {
            SudokuState s = sudokuStack.pop();
            if (!backupStack.empty())
                if (backupStack.peek().equals(s))
                    backupStack.pop();
        }
    }

    public long solve() {
        long time = System.nanoTime();
        while (doStep() != null);
        return System.nanoTime() - time;
    }

    public StepDescription doStep() {
        SudokuState current = sudokuStack.peek().makeCopy();
        StepDescription sd = null;

        if (!checkIntegrity(current)) {
            if (!backupStack.empty()) {
                SudokuState lastGoodState = backupStack.pop();
                while(current != lastGoodState)
                    current = sudokuStack.pop();
                sd = new StepDescription();
                sd.setDescription("Sudoku has become unsolvable, undoing to last guess.");
            } else {
                // Sudoku is unsolvable
                return null;
            }
        }

        if (sd == null)
            sd = simpleEliminate(current);

        if (sd == null)
            sd = findSingles(current);

        if (sd == null)
            sd = findHiddenSingles(current);

        if (sd == null)
            sd = findLineClaims(current);

        if (sd == null)
            sd = findBoxClaims(current);       

        if (sd == null)
            sd = findExposedSubsets(current);

        if (sd == null)
            sd = findHiddenSubsets(current);

        if (sd == null)
            sd = findXWing(current);

        if (sd == null)
            sd = findSwordfish(current);

        if (sd == null)
            sd = findXYWing(current);

        if (sd == null)
            sd = findXYZWing(current);

        if (sd == null)
            sd = findSimpleChains(current);     

        if (sd == null)
            sd = findXYChains(current);

        if (sd == null)
            sd = makeGuess(current);

        if (sd != null)
            sudokuStack.push(current);
        
        return sd;
    }

    public SudokuState getSudokuState() {
        return sudokuStack.peek();
    }

    private StepDescription findSingles(SudokuState s) {
        StepDescription sd = new StepDescription();
        for (Square sq : s.getAll(false)) {
            if (sq.getCandidates().size() == 1) {
                sq.setNumber(sq.getCandidates().getFirst());
                sd.addPointColor(sq.getPoint(), primaryColor);
                sd.setDescription("Found single-candidate squares");
                return sd;
            }
        }
        return null;
    }

    private StepDescription simpleEliminate(SudokuState sudoku) {
        StepDescription sd = new StepDescription();

        // for each solved square
        for (Square sq : sudoku.getAll(true)) {
            if (sq.isSolved()) {
                boolean didEliminating = false;
                Integer elim = sq.getNumber();
                // eliminate number from row
                for (Square other : sudoku.getRow(sq.getY(), false))
                    if (other.getCandidates().remove(elim)) {
                        sd.addPointColor(other.getPoint(), secondaryColor);
                        didEliminating = true;
                    }

                // eliminate number from column
                for (Square other : sudoku.getCol(sq.getX(), false))
                    if (other.getCandidates().remove(elim)) {
                        sd.addPointColor(other.getPoint(), secondaryColor);
                        didEliminating = true;
                    }

                // eliminate number from box
                for (Square other : sudoku.getBox(sq.getX() / 3, sq.getY() / 3, false))
                    if (other.getCandidates().remove(elim)) {
                        sd.addPointColor(other.getPoint(), secondaryColor);
                        didEliminating = true;
                    }

                if (didEliminating) {
                    sd.addPointColor(sq.getPoint(), primaryColor);
                }
            }
        }

        if (sd.getPointColors().size() > 0) {
            sd.setDescription("Eliminating numbers from the same rows, columns, and squares");
            return sd;
        }

        return null;
    }

    private StepDescription findHiddenSingles(SudokuState s) {
        StepDescription sd = null;

        for (int row = 0; row < 9; row++) {
            sd = findHiddenSingleInArea(
                    s.getRow(row, false),
                    s.getRow(row, true));
            if (sd != null)
                return sd;
        }

        for (int col = 0; col < 9; col++) {
            sd = findHiddenSingleInArea(
                    s.getCol(col, false),
                    s.getCol(col, true));
            if (sd != null)
                return sd;
        }

        for (int box = 0; box < 9; box++) {
            sd = findHiddenSingleInArea(
                    s.getBox(box / 3, box % 3, false),
                    s.getBox(box / 3, box % 3, true));
            if (sd != null)
                return sd;
        }
        return null;
    }

    private StepDescription findHiddenSingleInArea(
            LinkedList<Square> openSquares,
            LinkedList<Square> allSquares) {
        for (Square sq : openSquares) {
            for (int i : sq.getCandidates()) {
                boolean singleton = true;
                for (Square other : openSquares)
                    if (other != sq)
                        for (int j : other.getCandidates())
                            if (i == j)
                                singleton = false;

                if (singleton) {
                    sq.setNumber(i);
                    StepDescription sd = new StepDescription();
                    for (Square other : allSquares)
                        sd.addPointColor(other.getPoint(), tertiaryColor);
                    sd.addPointColor(sq.getPoint(), primaryColor);
                    sd.setDescription("Found hidden singleton");
                    return sd;
                }
            }
        }

        return null;
    }

    private StepDescription findLineClaims(SudokuState s) {
        for (int box = 0; box < 9; box++)
            for (Square sq : s.getBox(box / 3, box % 3, false))
                for (int candidate : sq.getCandidates()) {
                    StepDescription sd = new StepDescription();

                    LinkedList<Integer> candidateRows = new LinkedList<Integer>();
                    LinkedList<Integer> candidateCols = new LinkedList<Integer>();

                    for (Square other : s.getBox(box / 3, box % 3, false))
                        for (int otherCandidate : other.getCandidates())
                            if (otherCandidate == candidate) {
                                if (!candidateRows.contains(other.getY()))
                                    candidateRows.add(other.getY());
                                if (!candidateCols.contains(other.getX()))
                                    candidateCols.add(other.getX());
                            }

                    if (candidateRows.size() == 1) {
                        boolean didEliminating = false;
                        int claimedRow = candidateRows.getFirst();

                        for (Square rowSquare : s.getRow(claimedRow, true))
                            sd.addPointColor(rowSquare.getPoint(), tertiaryColor);

                        for (Square other : s.getRow(claimedRow, false))
                            if (other.getX() / 3 != sq.getX() / 3)
                                if (other.getCandidates().remove(new Integer(candidate))) {
                                    didEliminating = true;
                                    sd.addPointColor(other.getPoint(), secondaryColor);
                                }

                        if (didEliminating) {
                            LinkedList<Square> boxRow = s.getBox(box / 3, box % 3, true);
                            boxRow.retainAll(s.getRow(claimedRow, true));
                            for (Square boxRowSquare : boxRow)
                                sd.addPointColor(boxRowSquare.getPoint(), primaryColor);

                            sd.setDescription("Number "+candidate+" is claimed in the row by the box.");
                            return sd;
                        }
                    }

                    sd = new StepDescription();

                    if (candidateCols.size() == 1) {
                        boolean didEliminating = false;
                        int claimedCol = candidateCols.getFirst();

                        for (Square colSquare : s.getCol(claimedCol, true))
                            sd.addPointColor(colSquare.getPoint(), tertiaryColor);

                        for (Square other : s.getCol(claimedCol, false))
                            if (other.getY() / 3 != sq.getY() / 3)
                                if (other.getCandidates().remove(new Integer(candidate))) {
                                    didEliminating = true;
                                    sd.addPointColor(other.getPoint(), secondaryColor);
                                }

                        if (didEliminating) {
                            LinkedList<Square> boxCol = s.getBox(box / 3, box % 3, true);
                            boxCol.retainAll(s.getCol(claimedCol, true));

                            for (Square boxColSquare : boxCol )
                                sd.addPointColor(boxColSquare.getPoint(), primaryColor);

                            sd.setDescription("Number "+candidate+" is claimed in the column by the box.");
                            return sd;
                        }
                    }

                }
        return null;
    }

    private StepDescription findBoxClaims(SudokuState s) {
        // we check each row and column if all the possible locations for some number
        // fall within the same box. If so, we can eliminate that number from
        // other squares in the box.

        //for each unsolved square
        for (Square sq : s.getAll(false)) {
            // for each candidate
            for (Integer num : sq.getCandidates()) {
                // check if the candidate occurs in any other box in the same row
                boolean boxClaimedByRow = true;
                for (Square rowSquare : s.getRow(sq.getY(), false))
                    if (!sq.sameBox(rowSquare))
                        if (rowSquare.getCandidates().contains(num))
                            boxClaimedByRow = false;

                // if not, the candidate must be in this row of the box
                if (boxClaimedByRow) {
                    StepDescription sd = new StepDescription();
                    boolean didEliminating = false;

                    // color the box
                    for (Square boxSquare : s.getBox(sq.getX() /3, sq.getY() /3 , true))
                        sd.addPointColor(boxSquare.getPoint(), tertiaryColor);

                    // and it can be eliminated from other rows
                    for (Square boxSquare : s.getBox(sq.getX() /3, sq.getY() /3 , false))
                        if (!sq.sameRow(boxSquare))
                            if (boxSquare.getCandidates().remove(num)) {
                                didEliminating = true;
                                // color squares with changes
                                sd.addPointColor(boxSquare.getPoint(), secondaryColor);
                            }

                    if (didEliminating) {
                        // color claiming row
                        for (Square boxSquare : s.getBox(sq.getX() /3, sq.getY() /3 , true))
                            if (sq.sameRow(boxSquare))
                                sd.addPointColor(boxSquare.getPoint(), primaryColor);

                        sd.setDescription(num+" is claimed in the box by row");
                        return sd;
                    }
                }

                // check if the candidate occurs in any other box in the same column
                boolean boxClaimedByCol = true;
                for (Square colSquare : s.getCol(sq.getX(), false))
                    if (!sq.sameBox(colSquare))
                        if (colSquare.getCandidates().contains(num))
                            boxClaimedByCol = false;

                // if not, the candidate must be in this column of the box
                if (boxClaimedByCol) {
                    StepDescription sd = new StepDescription();
                    boolean didEliminating = false;

                    // color the box
                    for (Square boxSquare : s.getBox(sq.getX() /3, sq.getY() /3 , true))
                        sd.addPointColor(boxSquare.getPoint(), tertiaryColor);

                    // and it can be eliminated from other columns
                    for (Square boxSquare : s.getBox(sq.getX() /3, sq.getY() /3 , false))
                        if (!sq.sameCol(boxSquare))
                            if (boxSquare.getCandidates().remove(num)) {
                                didEliminating = true;
                                // color squares with changes
                                sd.addPointColor(boxSquare.getPoint(), secondaryColor);
                            }

                    if (didEliminating) {
                        // color claiming column
                        for (Square boxSquare : s.getBox(sq.getX() /3, sq.getY() /3 , true))
                            if (sq.sameCol(boxSquare))
                                sd.addPointColor(boxSquare.getPoint(), primaryColor);

                        sd.setDescription(num+" is claimed in the box by column");
                        return sd;
                    }
                }
            }
        }
        return null;
    }

    private StepDescription findExposedSubsets(SudokuState s) {
        StepDescription sd = null;

        for (int subsetLength = 2; subsetLength < 4; subsetLength++) {
            for (int row = 0; row < 9; row++) {
                sd = findExposedSubsetInArea(
                        s.getRow(row, false),
                        s.getRow(row, true),
                        subsetLength);
                if (sd != null)
                    return sd;
            }

            for (int col = 0; col < 9; col++) {
                sd = findExposedSubsetInArea(
                        s.getCol(col, false),
                        s.getCol(col, true),
                        subsetLength);
                if (sd != null)
                    return sd;
            }

            for (int box = 0; box < 9; box++) {
                sd = findExposedSubsetInArea(
                        s.getBox(box / 3, box % 3, false),
                        s.getBox(box / 3, box % 3, true),
                        subsetLength);
                if (sd != null)
                    return sd;
            }
        }
        return null;
    }
    
    private StepDescription findExposedSubsetInArea(
            LinkedList<Square> openSquares,
            LinkedList<Square> allSquares,
            int length) {

        for (LinkedList<Square> subset : getAllSubsets(openSquares, length)) {
            LinkedList<Integer> subsetNums = new LinkedList<Integer>();
            StepDescription sd = new StepDescription();

            for(Square sq : allSquares)
                sd.addPointColor(sq.getPoint(), tertiaryColor);

            for (Square setSquare : subset)
                for (int num : setSquare.getCandidates())
                    if (!subsetNums.contains(num))
                        subsetNums.add(num);

            if (subsetNums.size() == length) {
                boolean didEliminating = false;
                for (Square areaSquare : openSquares) {
                    if (!subset.contains(areaSquare))
                        if (areaSquare.getCandidates().removeAll(subsetNums)) {
                            didEliminating = true;
                            sd.addPointColor(areaSquare.getPoint(), secondaryColor);
                        }
                }

                if (didEliminating) {
                    for(Square sq : subset)
                        sd.addPointColor(sq.getPoint(), primaryColor);
                    String description = "Found exposed subset {";
                    for (int i : subsetNums)
                        description += " " + i;
                    description += " }";
                    sd.setDescription(description);
                    return sd;
                }
            }
        }
        return null;
    }

    private StepDescription findHiddenSubsets(SudokuState s) {
        StepDescription sd = null;

        for (int subsetLength = 2; subsetLength < 4; subsetLength++) {
            for (int row = 0; row < 9; row++) {
                sd = findHiddenSubsetInArea(
                        s.getRow(row, false),
                        s.getRow(row, true),
                        subsetLength);
                if (sd != null)
                    return sd;
            }

            for (int col = 0; col < 9; col++) {
                sd = findHiddenSubsetInArea(
                        s.getCol(col, false),
                        s.getCol(col, true),
                        subsetLength);
                if (sd != null)
                    return sd;
            }

            for (int box = 0; box < 9; box++) {
                sd = findHiddenSubsetInArea(
                        s.getBox(box / 3, box % 3, false),
                        s.getBox(box / 3, box % 3, true),
                        subsetLength);
                if (sd != null)
                    return sd;
            }
        }
        return null;
    }

    private StepDescription findHiddenSubsetInArea(
            LinkedList<Square> openSquares,
            LinkedList<Square> allSquares,
            int length) {
        for (LinkedList<Square> subset : getAllSubsets(openSquares, length)) {
            LinkedList<Integer> subsetNums = new LinkedList<Integer>();
            StepDescription sd = new StepDescription();

            for(Square sq : allSquares)
                sd.addPointColor(sq.getPoint(), secondaryColor);
            for(Square sq : subset)
                sd.addPointColor(sq.getPoint(), primaryColor);

            for (Square setSquare : subset)
                for (int num : setSquare.getCandidates()) {
                    boolean foundOutsideSubset = false;
                    for (Square rowSquare : openSquares)
                        if (!subset.contains(rowSquare))
                            if (rowSquare.getCandidates().contains(num))
                                foundOutsideSubset = true;

                    if (!foundOutsideSubset && !subsetNums.contains(num))
                        subsetNums.add(num);
                }

            if (subsetNums.size() == length) {
                boolean didSomething = false;
                for (Square subsetSquare : subset)
                    if (subsetSquare.getCandidates().retainAll(subsetNums))
                        didSomething = true;

                if (didSomething) {
                    String description = "Found hidden subset {";
                    for (int i : subsetNums)
                        description += " " + i;
                    description += " }";
                    sd.setDescription(description);
                    return sd;
                }
            }
        }
        return null;
    }

    private LinkedList<LinkedList<Square>> getAllSubsets(LinkedList<Square> squares, int length) {
        LinkedList<LinkedList<Square>> subsets = new LinkedList<LinkedList<Square>>();

        for (int i = 0; i < squares.size(); i++)
            for (int j = i + 1; j < squares.size(); j++) {
                if (length > 2) {
                    for (int k = j + 1; k < squares.size(); k++) {
                        if (length > 3) {
                            for (int l = k + 1; l < squares.size(); l++) {
                                LinkedList<Square> subset = new LinkedList<Square>();
                                subset.add(squares.get(i));
                                subset.add(squares.get(j));
                                subset.add(squares.get(k));
                                subset.add(squares.get(l));
                                subsets.add(subset);
                            }
                        } else {
                            LinkedList<Square> subset = new LinkedList<Square>();
                            subset.add(squares.get(i));
                            subset.add(squares.get(j));
                            subset.add(squares.get(k));
                            subsets.add(subset);
                        }
                    }
                } else {
                    LinkedList<Square> subset = new LinkedList<Square>();
                    subset.add(squares.get(i));
                    subset.add(squares.get(j));
                    subsets.add(subset);
                }
            }
        return subsets;
    }

    private StepDescription findSimpleChains(SudokuState sudoku) {
        // if the candidate appears exactly twice in an area (row/col/box)
        // we know that one of them is the number and the other is not
        // these relationships can be chained together

        for (Integer chainNum = 1; chainNum < 10; chainNum++) {
            LinkedList<Square> inAChain = new LinkedList<Square>();
            LinkedList<Entry<LinkedList<Entry<Point,Point>>, HashMap<Square, Boolean>>> chains = new
                    LinkedList<Entry<LinkedList<Entry<Point,Point>>, HashMap<Square, Boolean>>>();
            for (Square chainStart : sudoku.getAll(false))
                if (!inAChain.contains(chainStart) && chainStart.getCandidates().contains(chainNum)) {
                    // first, we find a chain

                    HashMap<Square, Boolean> chain = new HashMap<Square, Boolean>();
                    LinkedList<Entry<Point,Point>> links = new LinkedList<Entry<Point,Point>>();
                    Stack<Square> chainStack = new Stack<Square>();
                    Stack<Boolean> stateStack = new Stack<Boolean>();

                    chain.put(chainStart, true);
                    chainStack.push(chainStart);
                    stateStack.push(true);

                    while(!chainStack.empty()) {
                        Square sq = chainStack.pop();
                        boolean state = stateStack.pop();

                        addToChain(
                                findCandidatePair(sudoku.getCol(sq.getX(), false), sq, chainNum),
                                sq, state, chain, chainStack, stateStack, links);

                        addToChain(
                                findCandidatePair(sudoku.getRow(sq.getY(), false), sq, chainNum),
                                sq, state, chain, chainStack, stateStack, links);

                        addToChain(
                                findCandidatePair(sudoku.getBox(sq.getX() / 3, sq.getY() / 3, false), sq, chainNum),
                                sq, state, chain, chainStack, stateStack, links);
                    }

                    // if the chain is long enough, we analyze it further
                    if (chain.size() > 3) {
                        // if the chain contains two squares with the same state
                        // in an area (row/col/box), we know that state is impossible
                        for (Square s1 : chain.keySet())
                            for (Square s2: chain.keySet())
                                if ((s1 != s2) &&
                                   (s1.sameBox(s2) || s1.sameCol(s2) || s1.sameRow(s2)) &&
                                   (chain.get(s1) == chain.get(s2))) {

                                    StepDescription sd = new StepDescription();
                                    sd.setDescription("Found same color in the chain of "+chainNum+"s twice in the same area");

                                    // remove candidates
                                    for (Entry<Square, Boolean> e : chain.entrySet())
                                        if (e.getValue() == chain.get(s1))
                                            e.getKey().getCandidates().remove(chainNum);
                                    

                                    // color the relevant area
                                    if (s1.sameBox(s2))
                                        for (Square areaSquare : sudoku.getBox(s1.getX() / 3, s1.getY() / 3, true))
                                            sd.addPointColor(areaSquare.getPoint(), tertiaryColor);
                                    else if (s1.sameCol(s2))
                                        for (Square areaSquare : sudoku.getCol(s1.getX(), true))
                                            sd.addPointColor(areaSquare.getPoint(), tertiaryColor);
                                    else
                                        for (Square areaSquare : sudoku.getRow(s1.getY(), true))
                                            sd.addPointColor(areaSquare.getPoint(), tertiaryColor);

                                    // color the chain
                                    for (Entry<Square, Boolean> e : chain.entrySet())
                                        sd.addPointColor(e.getKey().getPoint(), e.getValue() ? onColor1 : offColor1);
                                    sd.setLinks(links);

                                    return sd;
                                }

                        // if there exists a square with the same candidate the chain
                        // is made of, that is not in the chain, such that it shares and
                        // area with two separate parts of the chain with opposite
                        // states, we can eliminate the chain candidate from that square
                        StepDescription sd = new StepDescription();
                        boolean didEliminating = false;
                        LinkedList<Square> ends = new LinkedList<Square>();

                        for (Square s1 : sudoku.getAll(false)) {
                            if (!chain.containsKey(s1) && s1.getCandidates().contains(chainNum)) {
                                Square on = null;
                                Square off = null;

                                for (Square s2 : chain.keySet()) {
                                    if (s1.sameBox(s2)) 
                                        if (chain.get(s2) == true)
                                            on = s2;
                                        else
                                            off = s2;
                                    else if(s1.sameCol(s2))
                                        if (chain.get(s2) == true)
                                            on = s2;
                                        else
                                            off = s2;
                                    else if (s1.sameRow(s2))
                                        if (chain.get(s2) == true)
                                            on = s2;
                                        else
                                            off = s2;
                                }

                                if (on != null && off != null) {
                                    ends.add(on);
                                    ends.add(off);

                                    s1.getCandidates().remove(chainNum);
                                    
                                    // color the affected square
                                    sd.addPointColor(s1.getPoint(), secondaryColor);

                                    didEliminating = true;
                                }
                            }
                        }
                        if (didEliminating) {
                            trimColorChain(ends, chain, links);

                            // color the chain
                            for (Entry<Square, Boolean> e : chain.entrySet())
                                sd.addPointColor(e.getKey().getPoint(), e.getValue() ? onColor1 : offColor1);
                            
                            sd.setDescription("Found squares that share an area with both colors of the chain of "+chainNum+"s.");
                            sd.setLinks(links);
                            return sd;
                        }
                    }

                    // a square cannot be in two chains for the same candidate
                    // so we do not need to check chains for these squares again
                    // for this candidate
                    for (Square sq : chain.keySet())
                        inAChain.add(sq);

                     if (chain.size() > 1)
                         chains.add(new Entry<LinkedList<Entry<Point,Point>>, HashMap<Square, Boolean>>(links, chain));

                }

            // check for interactions between two separate chains of the same number
            if (chains.size() > 1) {
                // consider every possible pair of chains
                System.out.println("Chains of "+chainNum+"s : "+chains.size());
                for (int i = 0; i < chains.size(); i++)
                    for (int j = i + 1; j < chains.size(); j++) {
                        
                        HashMap<Square, Boolean> chain1 = chains.get(i).getValue();
                        HashMap<Square, Boolean> chain2 = chains.get(j).getValue();

                        // to do anything useful, we need to find two pairs of squares
                        // from the two squares that share an area
                        LinkedList<Entry<Square, Square>> pairs = new LinkedList<Entry<Square, Square>>();
                        for (Square s1 : chain1.keySet())
                            for (Square s2 : chain2.keySet())
                                if (s1.sameBox(s2) || s1.sameCol(s2) || s1.sameRow(s2))
                                    pairs.add(new Entry<Square,Square>(s1,s2));

                        if (pairs.size() > 1) {
//                            System.out.println("Pairs between chains "+i+","+j+" : "+pairs.size());
//                            for (Entry<Square,Square> pair : pairs) {
//                                Square s1 = pair.getKey();
//                                Square s2 = pair.getValue();
//                                System.out.println("("+s1.getX()+","+s1.getY()+") and ("+s2.getX()+","+s2.getY()+")");
//                            }

                            // if two squares with the same state in one chain share an area with
                            // two squares with different states in another chain, we can
                            // eliminate that state from the first chain
                            for (int p1 = 0; p1 < pairs.size(); p1++)
                                for (int p2 = p1 + 1; p2 < pairs.size(); p2++) {
                                    Square pair1chain1 = pairs.get(p1).getKey();
                                    Square pair1chain2 = pairs.get(p1).getValue();
                                    Square pair2chain1 = pairs.get(p2).getKey();
                                    Square pair2chain2 = pairs.get(p2).getValue();

                                    // check that we have two completely separate pairs
                                    if (!pair1chain1.equals(pair2chain1) && !pair1chain2.equals(pair2chain2)) {
                                        //System.out.println("Chains linked");
                                        StepDescription sd = new StepDescription();
                                        boolean didEliminating = false;

                                        if (!((chain1.get(pair1chain1).equals(chain1.get(pair2chain1))) &&
                                              (chain2.get(pair1chain2).equals(chain2.get(pair2chain2))))) {

                                            if (chain1.get(pair1chain1).equals(chain1.get(pair2chain1)))
                                                for (Entry<Square,Boolean> e : chain1.entrySet())
                                                    if (e.getValue() == chain1.get(pair1chain1)) {
                                                        e.getKey().getCandidates().remove(chainNum);
                                                        didEliminating = true;
                                                    }

                                            if (chain2.get(pair1chain2).equals(chain2.get(pair2chain2)))
                                                for (Entry<Square,Boolean> e : chain2.entrySet())
                                                    if (e.getValue() == chain2.get(pair1chain2)) {
                                                        e.getKey().getCandidates().remove(chainNum);
                                                        didEliminating = true;
                                                    }

                                        }

                                        if (didEliminating) {
                                            sd.setDescription("Two chains of "+chainNum+"s are contradictory");

                                            // color the chains
                                            for (Entry<Square, Boolean> e : chain1.entrySet())
                                                sd.addPointColor(e.getKey().getPoint(), e.getValue() ? onColor1 : offColor1);
                                            sd.setLinks(chains.get(i).getKey());

                                            for (Entry<Square, Boolean> e : chain2.entrySet())
                                                sd.addPointColor(e.getKey().getPoint(), e.getValue() ? onColor2 : offColor2);
                                            sd.getLinks().addAll(chains.get(j).getKey());

                                            return sd;
                                        }
                                    }
                                }

                            // if two squares with opposite in one chain share an area with
                            // two squares with opposite states in another chain, we can
                            // treat the two as one continuous chain and eliminate the chain
                            // candidate from squares that share an area with two squares with
                            // opposite states in the chain
                            for (int p1 = 0; p1 < pairs.size(); p1++)
                                for (int p2 = p1 + 1; p2 < pairs.size(); p2++) {
                                    Square pair1chain1 = pairs.get(p1).getKey();
                                    Square pair1chain2 = pairs.get(p1).getValue();
                                    Square pair2chain1 = pairs.get(p2).getKey();
                                    Square pair2chain2 = pairs.get(p2).getValue();

                                    // check that we have two completely separate pairs
                                    if (!pair1chain1.equals(pair2chain1) && !pair1chain2.equals(pair2chain2)) {
                                         if (!((chain1.get(pair1chain1).equals(chain1.get(pair2chain1))) &&
                                              (chain2.get(pair1chain2).equals(chain2.get(pair2chain2))))) {
                                            //System.out.println("Chains linked");
                                            StepDescription sd = new StepDescription();

                                            for (Square s : sudoku.getAll(false))
                                                if ((s.sameBox(pair1chain1) || s.sameCol(pair1chain1) || s.sameRow(pair1chain1)) &&
                                                    (s.sameBox(pair1chain2) || s.sameCol(pair1chain2) || s.sameRow(pair1chain2)))
                                                    if (!chain1.containsKey(s) && !chain2.containsKey(s))
                                                        if (s.getCandidates().remove(chainNum))
                                                            sd.addPointColor(s.getPoint(), secondaryColor);

                                            for (Square s : sudoku.getAll(false))
                                                if ((s.sameBox(pair2chain1) || s.sameCol(pair2chain1) || s.sameRow(pair2chain1)) &&
                                                    (s.sameBox(pair2chain2) || s.sameCol(pair2chain2) || s.sameRow(pair2chain2)))
                                                    if (!chain1.containsKey(s) && !chain2.containsKey(s))
                                                        if (s.getCandidates().remove(chainNum))
                                                            sd.addPointColor(s.getPoint(), secondaryColor);

                                            if (sd.getPointColors().size() > 0) {
                                                sd.setDescription("Two chains of "+chainNum+"s are linked");
                                                // color the chains
                                                for (Entry<Square, Boolean> e : chain1.entrySet())
                                                    sd.addPointColor(e.getKey().getPoint(), e.getValue() ? onColor1 : offColor1);
                                                sd.setLinks(chains.get(i).getKey());

                                                for (Entry<Square, Boolean> e : chain2.entrySet())
                                                    sd.addPointColor(e.getKey().getPoint(), e.getValue() ? onColor2 : offColor2);
                                                sd.getLinks().addAll(chains.get(j).getKey());

                                                return sd;
                                            }
                                        }
                                    }
                                }
                            

                        }
                        //System.out.println();
                    }
            }
        }
        return null;
    }

    private void addToChain(Square newLink, Square oldLink, Boolean state, HashMap<Square, Boolean> chain,
            Stack<Square> chainStack, Stack<Boolean> stateStack, LinkedList<Entry<Point,Point>> links) {
        // if there is exactly one other square in an area
        // with the candidate, and it isn't already in the chain
        // we add it to the chain
        if (newLink != null && !chain.containsKey(newLink)) {
            links.add(new Entry<Point, Point>(newLink.getPoint(), oldLink.getPoint()));
            chain.put(newLink, !state);
            chainStack.push(newLink);
            stateStack.push(!state);
        }
    }

    private void trimColorChain(LinkedList<Square> keep, HashMap<Square,Boolean> chain,
            LinkedList<Entry<Point,Point>> links) {
        boolean finished;
        do {
            finished = true;
            Square removeSquare = null;
            Entry removeLink = null;

            for (Entry<Square,Boolean> e : chain.entrySet()) {
                removeSquare = null;
                removeLink = null;

                Square s = e.getKey();
                if (!keep.contains(s)) {
                    int count = 0;
                    for (Entry<Point,Point> link : links) {
                        if (s.getPoint().equals(link.getKey())) {
                            removeLink = link;
                            count++;
                        }
                        if (s.getPoint().equals(link.getValue())) {
                            removeLink = link;
                            count++;
                        }
                    }
                    if (count == 1)
                        removeSquare = s;
                    
                }

                if (removeSquare != null) {
                    finished = false;
                    chain.removeKey(removeSquare);
                    links.remove(removeLink);
                }
            }
        } while (!finished);
    }

    private Square findCandidatePair(LinkedList<Square> area, Square first, int candidate) {
        int count = 0;
        Square second = null;

        for (Square s : area)
            if (s != first)
                for (int i : s.getCandidates())
                    if (i == candidate) {
                        count++;
                        second = s;
                    }

        if (count == 1)
            return second;
        else
            return null;
    }

    private boolean checkIntegrity(SudokuState sudoku) {
        for (Square sq : sudoku.getAll(true))
            if (!sq.isSolved() && sq.getCandidates().isEmpty())
                return false;
        return true;
    }  

    private StepDescription makeGuess(SudokuState sudoku) {
        int min = 10;
        Square minSquare = null;
        for (Square sq : sudoku.getAll(false))
            if (sq.getCandidates().size() < min) {
                min = sq.getCandidates().size();
                minSquare = sq;
            }
        if (min < 10) {
            SudokuState backup = sudoku.makeCopy();
            int i = backup.getSquare(minSquare.getX(), minSquare.getY()).getCandidates().removeFirst();
            sudokuStack.push(backup);
            backupStack.push(backup);
            minSquare.setNumber(i);
            
            StepDescription sd = new StepDescription();
            sd.addPointColor(minSquare.getPoint(), primaryColor);
            sd.setDescription("Guessing "+i+" for square.");
            return sd;
        }

        return null;
    }

    private StepDescription findXWing(SudokuState sudoku) {
        for (int num = 1; num < 10; num++) {
            // look in rows
            for (int y1 = 0; y1 < 9; y1++) {
                LinkedList<Square> row1 = sudoku.getRow(y1, false);
                for (Square row1Square1 : row1)
                    if (row1Square1.getCandidates().contains(new Integer(num))) {
                        Square row1Square2 = findCandidatePair(row1, row1Square1, num);
                        if (row1Square2 != null) {
                            // we have found a row with only two choices for the candidate
                            // now, to look for another row where the only choices for
                            // the candidate fall in the same two columns

                            for (int y2 = 0; y2 < 9; y2++)
                                if (y1 != y2) {
                                    LinkedList<Square> row2 = sudoku.getRow(y2, false);
                                    if (candidateCountInArea(row2, num) == 2) {
                                        Square row2Square1 = sudoku.getSquare(row1Square1.getX(), y2);
                                        Square row2Square2 = sudoku.getSquare(row1Square2.getX(), y2);
                                        if (!row2Square1.isSolved() && !row2Square2.isSolved())
                                            if (row2Square1.getCandidates().contains(num) &&
                                                row2Square2.getCandidates().contains(num)) {
                                                // xwing pattern found
                                                // we can eliminate the number from the two columns
                                                // everywhere else
                                                StepDescription sd = new StepDescription();
                                                sd.setDescription("Found X-Wing pattern in rows for " + num);

                                                LinkedList<Square> col1 = sudoku.getCol(row1Square1.getX(), false);
                                                for (Square s : col1) {
                                                    if (s.getY() != y1 && s.getY() != y2)
                                                        if (s.getCandidates().remove(num))
                                                            sd.addPointColor(s.getPoint(), secondaryColor);
                                                }

                                                LinkedList<Square> col2 = sudoku.getCol(row1Square2.getX(), false);
                                                for (Square s : col2) {
                                                    if (s.getY() != y1 && s.getY() != y2)
                                                        if (s.getCandidates().remove(num))
                                                            sd.addPointColor(s.getPoint(), secondaryColor);
                                                }

                                                if (sd.getPointColors().size() > 0) {
                                                    sd.addPointColor(row1Square1.getPoint(), primaryColor);
                                                    sd.addPointColor(row1Square2.getPoint(), primaryColor);
                                                    sd.addPointColor(row2Square1.getPoint(), primaryColor);
                                                    sd.addPointColor(row2Square2.getPoint(), primaryColor);
                                                    return sd;
                                                }
                                            }
                                    }
                                }

                        }
                    }
            }

            // look in columns
            for (int x1 = 0; x1 < 9; x1++) {
                LinkedList<Square> col1 = sudoku.getCol(x1, false);
                for (Square col1Square1 : col1)
                    if (col1Square1.getCandidates().contains(new Integer(num))) {
                        Square col1Square2 = findCandidatePair(col1, col1Square1, num);
                        if (col1Square2 != null) {
                            // we have found a column with only two choices for the candidate
                            // now, to look for another column where the only choices for
                            // the candidate fall in the same two rows

                            for (int x2 = 0; x2 < 9; x2++)
                                if (x1 != x2) {
                                    LinkedList<Square> col2 = sudoku.getCol(x2, false);
                                    if (candidateCountInArea(col2, num) == 2) {
                                        Square col2Square1 = sudoku.getSquare(x2, col1Square1.getY());
                                        Square col2Square2 = sudoku.getSquare(x2, col1Square2.getY());
                                        if (!col2Square1.isSolved() && !col2Square2.isSolved())
                                            if (col2Square1.getCandidates().contains(num) &&
                                                col2Square2.getCandidates().contains(num)) {
                                                // xwing pattern found
                                                // we can eliminate the number from the two rows
                                                // everywhere else
                                                StepDescription sd = new StepDescription();
                                                sd.setDescription("Found X-Wing pattern in columns for " + num);

                                                LinkedList<Square> row1 = sudoku.getRow(col1Square1.getY(), false);
                                                for (Square s : row1) {
                                                    if (s.getX() != x1 && s.getX() != x2)
                                                        if (s.getCandidates().remove(num))
                                                            sd.addPointColor(s.getPoint(), secondaryColor);
                                                }

                                                LinkedList<Square> row2 = sudoku.getRow(col1Square2.getY(), false);
                                                for (Square s : row2) {
                                                    if (s.getX() != x1 && s.getX() != x2)
                                                        if (s.getCandidates().remove(num))
                                                            sd.addPointColor(s.getPoint(), secondaryColor);
                                                }

                                                if (sd.getPointColors().size() > 0) {
                                                    sd.addPointColor(col1Square1.getPoint(), primaryColor);
                                                    sd.addPointColor(col1Square2.getPoint(), primaryColor);
                                                    sd.addPointColor(col2Square1.getPoint(), primaryColor);
                                                    sd.addPointColor(col2Square2.getPoint(), primaryColor);
                                                    return sd;
                                                }
                                            }
                                    }
                                }

                        }
                    }
            }
        }
        return null;
    }

    private int candidateCountInArea(LinkedList<Square> area, int candidate) {
        int count = 0;
        for (Square s : area) {
            if (s.getCandidates().contains(candidate))
                count++;
        }
        return count;
    }

    private StepDescription findXYWing(SudokuState sudoku) {
        for (Square pivot : sudoku.getAll(false)) {
            if (pivot.getCandidates().size() == 2) {
                Integer x = pivot.getCandidates().get(0);
                Integer y = pivot.getCandidates().get(1);

                for (Square wing1 : sudoku.getAll(false))
                    if (wing1 != pivot &&
                        wing1.getCandidates().size() == 2 &&
                        wing1.getCandidates().contains(x) &&
                        !wing1.getCandidates().contains(y) &&
                        (pivot.sameBox(wing1) || pivot.sameCol(wing1) || pivot.sameRow(wing1))) {
                        Integer z =
                            wing1.getCandidates().get(0).equals(x) ?
                                wing1.getCandidates().get(1) :
                                wing1.getCandidates().get(0);

                        for (Square wing2 : sudoku.getAll(false))
                            if (wing2 != wing1 && wing2 != pivot && 
                                wing2.getCandidates().size() == 2 &&
                                wing2.getCandidates().contains(y) &&
                                wing2.getCandidates().contains(z) &&
                                (pivot.sameBox(wing2) || pivot.sameCol(wing2) || pivot.sameRow(wing2))) {
                                // xy-wing pattern found
                                // we can eliminate the candidate which both wings have
                                // in common from any square which shares an area with both
                                StepDescription sd = new StepDescription();
                                sd.setDescription("Found "+z+"-eliminating XY-Wing pattern");

                                for (Square s : sudoku.getAll(false))
                                    if (s != pivot && s != wing1 && s != wing2 &&
                                        (s.sameBox(wing2) || s.sameCol(wing2) || s.sameRow(wing2)) &&
                                        (s.sameBox(wing1) || s.sameCol(wing1) || s.sameRow(wing1)))
                                        if (s.getCandidates().remove(z))
                                            sd.addPointColor(s.getPoint(), secondaryColor);

                                if (sd.getPointColors().size() > 0) {
                                    sd.addPointColor(pivot.getPoint(), onColor1);
                                    sd.addPointColor(wing1.getPoint(), offColor1);
                                    sd.addPointColor(wing2.getPoint(), offColor1);

                                    return sd;
                                }
                            }
                    }
            }
        }

        return null;
    }

    private StepDescription findXYZWing(SudokuState sudoku) {
        for (Square pivot : sudoku.getAll(false)) {
            if (pivot.getCandidates().size() == 3) {

                for (Square wing1 : sudoku.getAll(false))
                    if (wing1.getCandidates().size() == 2 &&
                        pivot.getCandidates().containsAll(wing1.getCandidates()) &&
                        (pivot.sameBox(wing1) || pivot.sameCol(wing1) || pivot.sameRow(wing1))) {

                        for (Square wing2 : sudoku.getAll(false))
                            if (wing2 != wing1 &&
                                wing2.getCandidates().size() == 2 &&
                                pivot.getCandidates().containsAll(wing2.getCandidates()) &&
                                !wing1.getCandidates().containsAll(wing2.getCandidates()) &&
                                (pivot.sameBox(wing2) || pivot.sameCol(wing2) || pivot.sameRow(wing2))) {
                                // xyz-wing pattern found
                                // we can eliminate the candidate that is shared
                                // between the pivot and both wings from and squares
                                // that share an area with all three

                                int commonCandidate = 0;
                                for (int i : pivot.getCandidates())
                                    if (wing1.getCandidates().contains(i) &&
                                        wing2.getCandidates().contains(i))
                                        commonCandidate = i;

                                StepDescription sd = new StepDescription();
                                sd.setDescription("Found "+commonCandidate+"-eliminating XYZ-Wing pattern");

                                for (Square s : sudoku.getAll(false))
                                    if (s != pivot && s != wing1 && s != wing2 &&
                                        (s.sameBox(wing2) || s.sameCol(wing2) || s.sameRow(wing2)) &&
                                        (s.sameBox(wing1) || s.sameCol(wing1) || s.sameRow(wing1)) &&
                                        (s.sameBox(pivot) || s.sameCol(pivot) || s.sameRow(pivot)))
                                        if (s.getCandidates().remove(commonCandidate))
                                            sd.addPointColor(s.getPoint(), secondaryColor);

                                if (sd.getPointColors().size() > 0) {
                                    sd.addPointColor(pivot.getPoint(), onColor1);
                                    sd.addPointColor(wing1.getPoint(), offColor1);
                                    sd.addPointColor(wing2.getPoint(), offColor1);

                                    return sd;
                                }
                            }
                    }
            }
        } 
        return null;
    }

    private StepDescription findSwordfish(SudokuState sudoku) {
        for (int num = 1; num < 10; num++) {
            // look in rows
            for (int y1 = 0; y1 < 9; y1++) {
                LinkedList<Square> row1Squares = candidateSquaresInArea(sudoku.getRow(y1, false), num);
                if (row1Squares.size() <= 3 && row1Squares.size() > 0)
                    for (int y2 = y1 + 1; y2 < 9; y2++) {
                        LinkedList<Square> row2Squares = candidateSquaresInArea(sudoku.getRow(y2, false), num);
                        if (row2Squares.size() <= 3 && row2Squares.size() > 0)
                            for (int y3 = y2 + 1; y3 < 9; y3++) {
                                LinkedList<Square> row3Squares = candidateSquaresInArea(sudoku.getRow(y3, false), num);
                                if (row3Squares.size() <= 3 && row3Squares.size() > 0) {
                                    // now we know there are three rows in the sudoku
                                    // where the number occurs on no more than
                                    // three columns. We must now find out if the candidate
                                    // occurs on the same columns, creating the
                                    // swordfish pattern
                                    StepDescription sd = new StepDescription();

                                    LinkedList<Integer> columns = new LinkedList<Integer>();
                                    for (Square s : row1Squares)
                                        if (!columns.contains(s.getX()))
                                            columns.add(s.getX());
                                    for (Square s : row2Squares)
                                        if (!columns.contains(s.getX()))
                                            columns.add(s.getX());
                                    for (Square s : row3Squares)
                                        if (!columns.contains(s.getX()))
                                            columns.add(s.getX());

                                    if (columns.size() == 3) {
                                        boolean didEliminating = false;
                                        for (int col : columns)
                                            for (Square s : sudoku.getCol(col, false))
                                                if (s.getY() != y1 && s.getY() != y2 && s.getY() != y3)
                                                    if (s.getCandidates().remove(num)) {
                                                        sd.addPointColor(s.getPoint(), secondaryColor);
                                                        didEliminating = true;
                                                    }

                                        if (didEliminating) {
                                            for (Square s : row1Squares)
                                                sd.addPointColor(s.getPoint(), primaryColor);
                                            for (Square s : row2Squares)
                                                sd.addPointColor(s.getPoint(), primaryColor);
                                            for (Square s : row3Squares)
                                                sd.addPointColor(s.getPoint(), primaryColor);
                                            sd.setDescription("Found swordfish pattern in rows for "+num);
                                            return sd;
                                        }
                                    }
                                }
                            }
                    }
            }
            
            // look in columns
            for (int x1 = 0; x1 < 9; x1++) {
                LinkedList<Square> col1Squares = candidateSquaresInArea(sudoku.getCol(x1, false), num);
                if (col1Squares.size() <= 3 && col1Squares.size() > 0)
                    for (int x2 = x1 + 1; x2 < 9; x2++) {
                        LinkedList<Square> col2Squares = candidateSquaresInArea(sudoku.getCol(x2, false), num);
                        if (col2Squares.size() <= 3 && col2Squares.size() > 0)
                            for (int x3 = x2 + 1; x3 < 9; x3++) {
                                LinkedList<Square> col3Squares = candidateSquaresInArea(sudoku.getCol(x3, false), num);
                                if (col3Squares.size() <= 3 && col3Squares.size() > 0) {
                                    // now we know there are three columns in the sudoku
                                    // where the number occurs on no more than
                                    // three rows. We must now find out if the candidate
                                    // occurs on the same rows, creating the
                                    // swordfish pattern
                                    StepDescription sd = new StepDescription();

                                    LinkedList<Integer> rows = new LinkedList<Integer>();
                                    for (Square s : col1Squares)
                                        if (!rows.contains(s.getY()))
                                            rows.add(s.getY());
                                    for (Square s : col2Squares)
                                        if (!rows.contains(s.getY()))
                                            rows.add(s.getY());
                                    for (Square s : col3Squares)
                                        if (!rows.contains(s.getY()))
                                            rows.add(s.getY());

                                    if (rows.size() == 3) {
                                        boolean didEliminating = false;
                                        for (int row : rows)
                                            for (Square s : sudoku.getRow(row, false))
                                                if (s.getX() != x1 && s.getX() != x2 && s.getX() != x3)
                                                    if (s.getCandidates().remove(num)) {
                                                        sd.addPointColor(s.getPoint(), secondaryColor);
                                                        didEliminating = true;
                                                    }

                                        if (didEliminating) {
                                            for (Square s : col1Squares)
                                                sd.addPointColor(s.getPoint(), primaryColor);
                                            for (Square s : col2Squares)
                                                sd.addPointColor(s.getPoint(), primaryColor);
                                            for (Square s : col3Squares)
                                                sd.addPointColor(s.getPoint(), primaryColor);
                                            sd.setDescription("Found swordfish pattern in columns for "+num);
                                            return sd;
                                        }
                                    }
                                }
                            }
                    }
            }
        }

        return null;
    }

    private LinkedList<Square> candidateSquaresInArea(LinkedList<Square> area, int num) {
        LinkedList<Square> candidateSquares = new LinkedList<Square>();
        for (Square s : area)
            if (s.getCandidates().contains(num))
                candidateSquares.add(s);
        return candidateSquares;
    }

    private StepDescription findXYChains(SudokuState sudoku) {
        for (int chainStartNum = 1; chainStartNum < 10; chainStartNum++) {
            LinkedList<Square> discoveredChains = new LinkedList<Square>();
            for (Square chainStart : sudoku.getAll(false))
                if (!discoveredChains.contains(chainStart) &&
                    chainStart.getCandidates().size() == 2 &&
                    chainStart.getCandidates().contains(chainStartNum)) {

                    LinkedList<Square> chain = new LinkedList<Square>();
                    LinkedList<Entry<Point, Point>> links = new LinkedList<Entry<Point,Point>>();
                    Stack<Square> squareStack = new Stack<Square>();
                    Stack<Integer> numStack = new Stack<Integer>();

                    int firstNum = chainStart.getCandidates().get(0) == chainStartNum ?
                        chainStart.getCandidates().get(1) :
                        chainStart.getCandidates().get(0);

                    chain.add(chainStart);
                    squareStack.push(chainStart);
                    numStack.push(firstNum);

                    while(!squareStack.empty()) {
                        Square lastLink = squareStack.pop();
                        int lastNum = numStack.pop();

                        StepDescription sd = null;
                        sd = addToXYChain(
                                chainStartNum,
                                getNextLinkInArea(sudoku.getRow(lastLink.getY(), false), lastLink, lastNum),
                                lastLink,
                                lastNum,
                                sudoku,
                                chain,
                                links,
                                squareStack,
                                numStack);
                        if (sd != null)
                            return sd;

                        sd = addToXYChain(
                                chainStartNum,
                                getNextLinkInArea(sudoku.getCol(lastLink.getX(), false), lastLink, lastNum),
                                lastLink,
                                lastNum,
                                sudoku,
                                chain,
                                links,
                                squareStack,
                                numStack);
                        if (sd != null)
                            return sd;

                        sd = addToXYChain(
                                chainStartNum,
                                getNextLinkInArea(sudoku.getBox(lastLink.getX() / 3, lastLink.getY() / 3, false), lastLink, lastNum),
                                lastLink,
                                lastNum,
                                sudoku,
                                chain,
                                links,
                                squareStack,
                                numStack);
                        if (sd != null)
                            return sd;
                    }
                }
        }
        return null;
    }

    private StepDescription addToXYChain(
            int chainNum,
            Square newLink,
            Square lastLink, int lastNum,
            SudokuState sudoku,
            LinkedList<Square> chain,
            LinkedList<Entry<Point, Point>> links,
            Stack<Square> squareStack,
            Stack<Integer> numStack) {

        if (newLink != null && !chain.contains(newLink)) {
            int nextNum = newLink.getCandidates().get(0) == lastNum ?
                newLink.getCandidates().get(1) :
                newLink.getCandidates().get(0);

            squareStack.push(newLink);
            numStack.push(nextNum);
            chain.add(newLink);
            links.add(new Entry<Point, Point>(lastLink.getPoint(), newLink.getPoint()));

            if (nextNum == chainNum) {
                StepDescription sd = inspectXYChain(chainNum, chain, sudoku);
                if (sd != null) {
                    trimXYChain(chain, links);

                    sd.setLinks(links);
                    for (Square s : chain)
                        sd.addPointColor(s.getPoint(), chainMainColor);
                    sd.setDescription("Found XY-Chain for "+chainNum);
                    sd.addPointColor(chain.getFirst().getPoint(), chainStartColor);
                    sd.addPointColor(chain.getLast().getPoint(), chainStartColor);

                    return sd;
                }
            }
        }

        return null;
    }

    private StepDescription inspectXYChain(int chainNum, LinkedList<Square> chain, SudokuState sudoku) {
        Square start = chain.getFirst();
        Square end = chain.getLast();

        StepDescription sd = new StepDescription();
        for (Square s : sudoku.getAll(false))
            if (s != start && s != end)
                if ((s.sameBox(start) || s.sameCol(start) || s.sameRow(start)) &&
                    (s.sameBox(end) || s.sameCol(end) || s.sameRow(end)))
                    if (s.getCandidates().remove(chainNum))
                        sd.addPointColor(s.getPoint(), secondaryColor);

        if (sd.getPointColors().size() == 0)
            return null;
        return sd;
    }

    private void trimXYChain(LinkedList<Square> chain, LinkedList<Entry<Point,Point>> links) {
        Square chainStart = chain.getFirst();
        Square chainEnd = chain.getLast();

        boolean finished;
        do {
            finished = true;
            Square removeSquare = null;
            Entry removeLink = null;

            for (Square s : chain) {
                if (s != chainStart && s != chainEnd) {
                    int count = 0;
                    for (Entry<Point,Point> e : links) {
                        if (s.getPoint().equals(e.getKey())) {
                            removeLink = e;
                            count++;
                        }
                        if (s.getPoint().equals(e.getValue())) {
                            removeLink = e;
                            count++;
                        }
                    }
                    if (count == 1) {
                        removeSquare = s;
                        break;
                    }
                }
            }

            if (removeSquare != null) {
                finished = false;
                chain.remove(removeSquare);
                links.remove(removeLink);
            }
        } while (!finished);
    }

    private Square getNextLinkInArea(LinkedList<Square> area, Square lastLink, int nextNum) {
        for (Square s : area)
            if (s != lastLink)
                if (s.getCandidates().size() == 2)
                    if (s.getCandidates().contains(nextNum))
                        return s;
        return null;
    }
}

