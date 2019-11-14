package demo.knight.chessboard;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;

import demo.knight.R;

public class ChessBoardHelper implements Parcelable {

    /**
     * - each square is represented by a BoardCell in boardCells
     * - the knight route is stored in pathCells list of BoardCell
     * - the knight route is drawn using PathPoint points that are created
     *   based on pathCells and the size of the ChessBoard view
     * - percentage is used to implement the knight path animation.
     *   It's range is 0-1. 0->no path is drawn, 1->whole path is drawn
     */

    private int CHESSBOARD_SIZE = ChessBoard.CHESSBOARD_SIZE;
    private BoardCell startCell, stopCell;
    private ArrayList<BoardCell> boardCells;
    private float percentage;

    //pathCells represent the selected knight route and pathPoints represent
    //the same route but in PathPoint objects that will be used for the knight animation
    private ArrayList<BoardCell> pathCells;
    private ArrayList<PathPoint> pathPoints;

    ChessBoardHelper(Context context) {
        int lightCellColor = ContextCompat.getColor(context, R.color.cell_light);
        int darkCellColor = ContextCompat.getColor(context, R.color.cell_dark);

        this.boardCells = new ArrayList<>();
        boolean cellIsLightColored = true;
        for(int row=0; row<CHESSBOARD_SIZE; row++){
            for(int column = 0; column<CHESSBOARD_SIZE; column++){
                BoardCell boardCell = new BoardCell(row, column);
                boardCell.setCellColor(cellIsLightColored ? lightCellColor : darkCellColor);
                boardCells.add(boardCell);
                cellIsLightColored = !cellIsLightColored;
            }

            if(CHESSBOARD_SIZE%2==0) {
                //in chessboards with even number of cells the start
                //of a row is the same color as the end of the above
                cellIsLightColored = !cellIsLightColored;
            }
        }
    }

    /**
     * @param animate whether path points are created in order to immediately perform a path animation
     */
    void createPathPointsFromPathCellsCells(boolean animate) {
        //By changing the count of these extra points added to the
        //path line we control the behavior of the knight animation
        int pointsCountForFirstLine = 30;
        int pointsCountForSecondLine = 40;
        int extraPoinsOnLandingCell = 25;

        // pathCells are BoardCell objects that are not in the boardCells ArrayList that is used
        // to draw the ChessBoard and therefore do not have the BoardCell.rect
        // calculated
        ArrayList<BoardCell> cellsPathToDraw = mapEmptyCellsToChessBoardCells(pathCells);
        pathPoints = new ArrayList<>();

        //create a MyPoints list that represents the route that passes through these BoardCells
        for(int i=0; i<cellsPathToDraw.size()-1; i++){
            BoardCell cell1 = cellsPathToDraw.get(i);
            BoardCell cell2 = cellsPathToDraw.get(i+1);
            BoardCell inBetweenCell = calculateKnightTurningPoint(cell1, cell2);

            pathPoints.add(cell1.getCenter());
            //only create all these extra points if they will be used in animation.Otherwise they are not needed
            if(animate){
                ArrayList<PathPoint> morePoints = createPointsOnLineBetween(cell1.getCenter(), inBetweenCell.getCenter(), pointsCountForFirstLine);
                pathPoints.addAll(morePoints);
            }
            pathPoints.add(inBetweenCell.getCenter());

            if(animate){
                ArrayList<PathPoint> morePoints = createPointsOnLineBetween(inBetweenCell.getCenter(), cell2.getCenter(), pointsCountForSecondLine);
                pathPoints.addAll(morePoints);
                //add some more points on the landing cell center to make the animation pause here for a while.
                //This will seperate the animation of each knight movement on this path
                for(int j=0;j<extraPoinsOnLandingCell; j++){
                    pathPoints.add(cell2.getCenter());
                }
            }

            pathPoints.add(cell2.getCenter());

            if(i<cellsPathToDraw.size()-2){
                //mark the path points at which a knight move is completed
                //to provide better visual understanding of the path
                PathPoint pathPoint = new PathPoint(cell2.getCenter().x, cell2.getCenter().y);
                pathPoint.markAsCirclePoint();
                pathPoints.add(pathPoint);
            }
        }
    }

    /**
     *
     * @param startCell cell from which the knight starts moving
     * @param endCell cell at which the knight lands
     * @return the cell at which the knight turns 90 degrees to reach the end cell
     */
    private BoardCell calculateKnightTurningPoint(BoardCell startCell, BoardCell endCell){
        int rowDif = endCell.getRow() - startCell.getRow();
        int columnDif = endCell.getColumn()- startCell.getColumn();
        int indexOfInBetweenCell;
        if(Math.abs(rowDif)==2){
            indexOfInBetweenCell = (startCell.getRow()+rowDif)*CHESSBOARD_SIZE + startCell.getColumn();
        }
        else{
            indexOfInBetweenCell = (startCell.getRow())*CHESSBOARD_SIZE + startCell.getColumn()+columnDif;
        }

        return boardCells.get(indexOfInBetweenCell);
    }

    /**
     *
     * @param cellsWithoutRects cells that were not made by the ChessBoard view and
     *                          their rect field is empty.
     * @return                  the cells made by ChessBoard view with the same coordinates as the input cells
     */
    private ArrayList<BoardCell> mapEmptyCellsToChessBoardCells(ArrayList<BoardCell> cellsWithoutRects){
        ArrayList<BoardCell> chessBoardCells = new ArrayList<>();
        if(cellsWithoutRects!=null) {
            for (BoardCell pathCell : cellsWithoutRects) {
                int indexInBoardCellsList = (pathCell.getRow()) * CHESSBOARD_SIZE + pathCell.getColumn();
                chessBoardCells.add(boardCells.get(indexInBoardCellsList));
            }
        }
        return chessBoardCells;
    }

    /**
     * @param pointsCount how many points to spread out between p1 and p2
     * @return a list of pointsCount points equally spread out in a line between p1 and p2
     */
    private ArrayList<PathPoint> createPointsOnLineBetween(PathPoint p1, PathPoint p2, int pointsCount) {
        ArrayList<PathPoint> points = new ArrayList<>();

        float xStep = (p2.x - p1.x)/pointsCount;
        float yStep = (p2.y - p1.y)/pointsCount;

        float lastX = p1.x;
        float lastY = p1.y;

        for(int i=0; i<pointsCount; i++){
            lastX+=xStep;
            lastY+=yStep;
            points.add(new PathPoint(lastX, lastY));
        }

        return points;
    }

    void onCellSizeCalculated(float cellSize) {
        for(BoardCell boardCell:boardCells){
            boardCell.calculateRectForCellSize(cellSize);
        }
    }

    BoardCell findClickedCell(PathPoint pointDown, PathPoint pointUp) {
        for (BoardCell boardCell : boardCells) {
            if (boardCell.wasClicked(pointDown, pointUp)) {
                return boardCell;
            }
        }
        return null;
    }

    boolean startAndStopCellsSelected() {
        return startCell!=null && stopCell!=null;
    }

    void erasePathPoints() {
        pathPoints = null;
        pathCells = null;
    }

    //setters
    void setPathCells(ArrayList<BoardCell> pathCells) {
        this.pathCells = pathCells;
    }

    void setPercentage(float percentage) {
        this.percentage = percentage;
    }

    void setStartCell(BoardCell startCell) {
        this.startCell = startCell;
    }

    void setStopCell(BoardCell stopCell) {
        this.stopCell = stopCell;
    }

    //getters
    BoardCell getStartCell() {
        return startCell;
    }

    BoardCell getStopCell() {
        return stopCell;
    }

    ArrayList<PathPoint> getPathPoints() {
        return pathPoints;
    }

    int getNumberOfPointsToDraw(){
        return pathPoints!=null ? (int) (pathPoints.size() * percentage -1) : 0;
    }
    ArrayList<BoardCell> getBoardCells() {
        return boardCells;
    }

    ArrayList<BoardCell> getPathCells() {
        return pathCells;
    }

    float getPercentage() {
        return percentage;
    }

    //parcelable implementation
    private ChessBoardHelper(Parcel in) {
        startCell = in.readParcelable(BoardCell.class.getClassLoader());
        stopCell = in.readParcelable(BoardCell.class.getClassLoader());
        CHESSBOARD_SIZE = in.readInt();
        boardCells = in.createTypedArrayList(BoardCell.CREATOR);
        pathCells = in.createTypedArrayList(BoardCell.CREATOR);
        percentage = in.readFloat();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(startCell, flags);
        dest.writeParcelable(stopCell, flags);
        dest.writeInt(CHESSBOARD_SIZE);
        dest.writeTypedList(boardCells);
        dest.writeTypedList(pathCells);
        dest.writeFloat(percentage);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ChessBoardHelper> CREATOR = new Creator<ChessBoardHelper>() {
        @Override
        public ChessBoardHelper createFromParcel(Parcel in) {
            return new ChessBoardHelper(in);
        }

        @Override
        public ChessBoardHelper[] newArray(int size) {
            return new ChessBoardHelper[size];
        }
    };
}
