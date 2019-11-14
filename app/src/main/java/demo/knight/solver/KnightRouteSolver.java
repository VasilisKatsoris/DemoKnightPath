package demo.knight.solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import demo.knight.chessboard.BoardCell;

public class KnightRouteSolver {

    public static int MAX_STEPS = 4;
    private ArrayList<BoardCell> results = new ArrayList<>();

    public ArrayList<Solution> solveRoute(BoardCell start, BoardCell end){

        results = new ArrayList<>();

        solveRouteRecursively(start, end, 0);

        ArrayList<Solution> solutions = new ArrayList<>();
        for(BoardCell result:results){
            ArrayList<BoardCell> history = result.getHistory();
            if(!pathHasCircles(history)) {
                //only add paths without circles because if we remove the unnecessary
                //circle the remaining path would already exist in the results list
                //It is more efficient (execution time wise) to remove these paths after
                //calculating them than not adding them in the results in the first place.
                solutions.add(new Solution(history));
            }
        }
        results.clear();

        //sort solutions with faster first
        Collections.sort(solutions, new Comparator<Solution>() {


            @Override
            public int compare(Solution s1, Solution s2) {
                return Integer.compare(s1.getNumberOfMoves(), s2.getNumberOfMoves());
            }
        });

        return solutions;
    }

    private void solveRouteRecursively(BoardCell startCell, BoardCell endCell, int step) {
        step++;
        if(step>MAX_STEPS){
            return;
        }
        ArrayList<BoardCell> knightPossibleMovements = startCell.getAllValidKnightDestinations();
        for(BoardCell knightPossibleMove:knightPossibleMovements){
            //store in each BoardCell the cell from which the knight moved here.
            //when we reach the endCell it will know the entire path the knight followed
            knightPossibleMove.setPreviousBoardCell(startCell);
            if(knightPossibleMove.equals(endCell)){
                results.add(knightPossibleMove);
            }
            else {
                solveRouteRecursively(knightPossibleMove, endCell, step);
            }
        }
    }

    /**
     * check if path has cells that appear more than once. That would mean that
     * if the knight follows this path he would walk in a circle at least once
     */
    private boolean pathHasCircles(ArrayList<BoardCell> cellsPath){
        for(int i=0; i<cellsPath.size(); i++){
            for(int j = i; j<cellsPath.size(); j++){
                if(i!=j){
                    if(cellsPath.get(i).equals(cellsPath.get(j))){
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
