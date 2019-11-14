package demo.knight.solver;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import demo.knight.chessboard.BoardCell;

public class Solution implements Parcelable {

    private boolean isForText; //if true it is a dummy Solution object to make RecyclerView show a text
    private ArrayList<BoardCell> cellsPath;
    private int numberOfMoves;
    private String description;
    private boolean isSelected;

    Solution(ArrayList<BoardCell> cellsPath) {
        this.cellsPath = cellsPath;
        numberOfMoves = cellsPath.size() - 1;
        description = cellsPath.toString().replace("[","").replace("]","");
    }

    public Solution(String message){
        this.description = message;
        isForText = true;
    }

    //setters - getters
    public ArrayList<BoardCell> getCellsPath() {
        return cellsPath;
    }

    int getNumberOfMoves() {
        return numberOfMoves;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isForText() {
        return isForText;
    }

    //parcelable implementation
    private Solution(Parcel in) {
        cellsPath = in.createTypedArrayList(BoardCell.CREATOR);
        numberOfMoves = in.readInt();
        description = in.readString();
        isSelected = in.readByte() != 0;
        isForText = in.readByte() != 0;
    }

    public static final Creator<Solution> CREATOR = new Creator<Solution>() {
        @Override
        public Solution createFromParcel(Parcel in) {
            return new Solution(in);
        }

        @Override
        public Solution[] newArray(int size) {
            return new Solution[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(cellsPath);
        dest.writeInt(numberOfMoves);
        dest.writeString(description);
        dest.writeByte((byte) (isSelected ? 1 : 0));
        dest.writeByte((byte) (isForText ? 1 : 0));
    }
}
