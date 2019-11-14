package demo.knight.chessboard;

import android.graphics.PointF;
import android.os.Parcel;

public class PathPoint extends PointF {

    //used to draw a white circle at intermediate BoardCells the knight lands on
    private boolean isCircle;

    PathPoint(float x, float y){
        super(x,y);
    }

    boolean isCircle() {
        return isCircle;
    }

    void markAsCirclePoint() {
        this.isCircle = true;
    }


    //parcelable implementation
    private PathPoint(Parcel in) {
        isCircle = in.readByte() != 0;
    }

    public static final Creator<PathPoint> CREATOR = new Creator<PathPoint>() {
        @Override
        public PathPoint createFromParcel(Parcel in) {
            return new PathPoint(in);
        }

        @Override
        public PathPoint[] newArray(int size) {
            return new PathPoint[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isCircle ? 1 : 0));
    }
}
