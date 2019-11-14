package demo.knight.chessboard;

import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;


import java.util.ArrayList;
import java.util.Collections;

public class BoardCell implements Parcelable {

    private static final char[] columnLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private int cellColor;
    private RectF rect;
    private BoardCell peviousBoardCell; //store which BoardCell the knight started from to land on this BoardCell
    private int column, row;

    BoardCell(int row, int column) {
        this.row = row;
        this.column = column;
        rect = new RectF();
    }

    public BoardCell(BoardCell boardCell){
        this.row = boardCell.getRow();
        this.column = boardCell.getColumn();
    }

    /**
     *
     * @return List of all BoardCells a knight can move to from this BoardCell in one move
     */
    public ArrayList<BoardCell> getAllValidKnightDestinations(){
        ArrayList<BoardCell> nextBoardCells = new ArrayList<>();
        addBoardCellIfValid(nextBoardCells, new BoardCell(row-2, column-1));
        addBoardCellIfValid(nextBoardCells, new BoardCell(row-2, column+1));
        addBoardCellIfValid(nextBoardCells, new BoardCell(row+2, column-1));
        addBoardCellIfValid(nextBoardCells, new BoardCell(row+2, column+1));
        addBoardCellIfValid(nextBoardCells, new BoardCell(row-1, column-2));
        addBoardCellIfValid(nextBoardCells, new BoardCell(row+1, column-2));
        addBoardCellIfValid(nextBoardCells, new BoardCell(row-1, column+2));
        addBoardCellIfValid(nextBoardCells, new BoardCell(row+1, column+2));
        return nextBoardCells;
    }

    private void addBoardCellIfValid(ArrayList<BoardCell> boardCells, BoardCell boardCell){
        if(boardCell.getColumn()<0 || boardCell.getColumn()>=ChessBoard.CHESSBOARD_SIZE){
            return;
        }
        if(boardCell.getRow()<0 || boardCell.getRow()>=ChessBoard.CHESSBOARD_SIZE){
            return;
        }
        boardCells.add(boardCell);
    }

    /**
     *
     * @return a list of all the BoardCells the knight had landed on to reach this BoardCell.
     */
    public ArrayList<BoardCell> getHistory(){
        ArrayList<BoardCell> nodesPath = new ArrayList<>();
        BoardCell prevBoardCell = getPeviousBoardCell();
        while (prevBoardCell!=null){
            nodesPath.add(prevBoardCell);
            prevBoardCell = prevBoardCell.getPeviousBoardCell();
        }
        Collections.reverse(nodesPath);
        nodesPath.add(this);
        return nodesPath;
    }

    /**
     *
     * @return the center point of the BoardCell rect
     */
    PathPoint getCenter() {
        return  new PathPoint(rect.centerX(), rect.centerY());
    }

    //setters - getters
    RectF getRect() {
        return rect;
    }

    int getCellColor() {
        return cellColor;
    }

    void setCellColor(int cellColor) {
        this.cellColor = cellColor;
    }

    boolean wasClicked(PathPoint pointDown, PathPoint pointUp) {
        if(rect == null || pointDown == null || pointUp == null){
            return false;
        }
        return rect.contains(pointDown.x, pointDown.y) && rect.contains(pointUp.x, pointUp.y);
    }

    int getColumn() {
        return column;
    }

    int getRow() {
        return row;
    }

    private BoardCell getPeviousBoardCell() {
        return peviousBoardCell;
    }

    public void setPreviousBoardCell(BoardCell peviousBoardCell) {
        this.peviousBoardCell = peviousBoardCell;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BoardCell cell = (BoardCell) o;

        return row == cell.row && column == cell.column;
    }

    @Override
    public String toString() {
        return Character.toString(columnLetters[column]) + (ChessBoard.CHESSBOARD_SIZE-row) ;
    }

    //parcelable implementation
    public static final Creator<BoardCell> CREATOR = new Creator<BoardCell>() {
        @Override
        public BoardCell createFromParcel(Parcel in) {
            return new BoardCell(in);
        }

        @Override
        public BoardCell[] newArray(int size) {
            return new BoardCell[size];
        }
    };

    private BoardCell(Parcel in) {
        cellColor = in.readInt();
        rect = in.readParcelable(RectF.class.getClassLoader());
        peviousBoardCell = in.readParcelable(BoardCell.class.getClassLoader());
        column = in.readInt();
        row = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(cellColor);
        dest.writeParcelable(rect, flags);
        dest.writeParcelable(peviousBoardCell, flags);
        dest.writeInt(column);
        dest.writeInt(row);
    }

    void calculateRectForCellSize(float cellSize) {
        rect.top = getRow()*cellSize;
        rect.left = getColumn()*cellSize;
        rect.bottom = rect.top+cellSize;
        rect.right = rect.left+cellSize;
    }
}
