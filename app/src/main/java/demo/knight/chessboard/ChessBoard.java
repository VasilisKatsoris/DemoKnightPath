package demo.knight.chessboard;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

import demo.knight.R;
import demo.knight.utils.ViewUtils;

public class ChessBoard extends View implements View.OnTouchListener {

    public static int CHESSBOARD_SIZE = 8;

    private Paint paint;
    private Bitmap knightBitmap, flagBitmap;
    private float strokeWidth = ViewUtils.dpToPx(5), circleRadius = ViewUtils.dpToPx(5);
    private float cellSize, knightSize, flagSize, bitmapsDrawSize;
    private ValueAnimator animator;
    private int pathColor, circleColor;
    private PathPoint pointDown;    //used to detec click events on ACTION_UP
    private OnChessBoardCellClickedInterface onChessBoardCellClickedInterface;
    private ChessBoardHelper chessBoardHelper;
    private static final long KNIGHT_MOVE_DURATION = 1500; //animation duration for a single knight move

    public ChessBoard(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChessBoard(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    void init(){
        pathColor = ContextCompat.getColor(getContext(), R.color.path_color);
        circleColor = ContextCompat.getColor(getContext(), R.color.circle_color);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(strokeWidth);

        chessBoardHelper = new ChessBoardHelper(getContext());

        knightBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.knight);
        flagBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.flag);

        setOnTouchListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //always keep view square
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int size = Math.min(width, height);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateSizes(w,h);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                pointDown = new PathPoint(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                //find cell that was clicked
                PathPoint pointUp = new PathPoint(event.getX(), event.getY());
                BoardCell clickedBoardCell = chessBoardHelper.findClickedCell(pointDown, pointUp);
                if(clickedBoardCell!=null){
                    onCellClicked(clickedBoardCell);
                }
                invalidate();
                break;
        }
        return true;
    }

    private void onCellClicked(BoardCell clickedBoardCell){

        if(animator!=null){
            animator.cancel();
        }

        if(chessBoardHelper.getStartCell() == null || chessBoardHelper.getStopCell()!=null) {
            chessBoardHelper.erasePathPoints();
            chessBoardHelper.setStartCell(clickedBoardCell);
            chessBoardHelper.setStopCell(null);
            animateBitmapEntry(BitmapToAnimate.KNIGHT);
        }
        else{
            if(clickedBoardCell == chessBoardHelper.getStartCell()){
                return;
            }
            chessBoardHelper.setStopCell(clickedBoardCell);
            animateBitmapEntry(BitmapToAnimate.FLAG);
        }

        if(onChessBoardCellClickedInterface!=null){
            onChessBoardCellClickedInterface.onChessBoardCellClicked();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //draw chess board
        paint.setStyle(Paint.Style.FILL);
        for(BoardCell boardCell: chessBoardHelper.getBoardCells()){
            paint.setColor(boardCell.getCellColor());
            canvas.drawRect(boardCell.getRect(), paint);
        }

        //draw knight path if one exists
        PathPoint lastPathPointDrawn = null;
        ArrayList<PathPoint> pathPoints = chessBoardHelper.getPathPoints();
        if(pathPoints !=null) {
            float numberOfPointsToDraw = chessBoardHelper.getNumberOfPointsToDraw();
            paint.setColor(pathColor);
            paint.setStyle(Paint.Style.STROKE);
            //draw selected path
            for (int i = 0; i < numberOfPointsToDraw; i++) {
                PathPoint p1 = pathPoints.get(i);
                PathPoint p2 = pathPoints.get(i + 1);
                canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
                lastPathPointDrawn = p2;
            }
            //draw white circles to highlight each knight move in the route
            for(int i = 0; i< numberOfPointsToDraw; i++){
                PathPoint p = pathPoints.get(i);
                if(p.isCircle()){
                    paint.setColor(circleColor);
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(p.x, p.y, circleRadius, paint);
                }
            }
        }

        //draw knight and flag if user has made selections

        //if there is a path animation in progress then we need to draw the knight
        //at the last path point that was drawn. Otherwise we draw the knight at
        //the center of the selected cell;
        boolean isAnimating = animator!=null && animator.isStarted();
        PathPoint knightStartPoint = chessBoardHelper.getStartCell()!=null ? chessBoardHelper.getStartCell().getCenter():null;
        PathPoint knightPoint = (isAnimating && lastPathPointDrawn!=null) ? lastPathPointDrawn:knightStartPoint;
        PathPoint knightStopPoint = chessBoardHelper.getStopCell()!=null ? chessBoardHelper.getStopCell().getCenter():null;

        if(knightPoint!=null && knightStopPoint!=null) {
            //skip drawing flag if it is too close to the knight to avoid overlay.
            if (Math.abs(knightPoint.x - knightStopPoint.x) > cellSize/4 || Math.abs(knightPoint.y - knightStopPoint.y) > cellSize/4) {
                drawBitmapOnPoint(canvas, flagBitmap, knightStopPoint, flagSize);
            }
        }

        drawBitmapOnPoint(canvas, knightBitmap, knightPoint, knightSize);

    }

    void drawBitmapOnPoint(Canvas canvas, Bitmap bitmap, PathPoint point, float size){
        if(point==null){
            return;
        }
        RectF destRec = new RectF();
        destRec.left = point.x - size/2;
        destRec.right = destRec.left+size;
        destRec.top = point.y - size/2;
        destRec.bottom = destRec.top+size;

        Rect bitmapSrcRect = new Rect(0,0,bitmap.getWidth(), bitmap.getHeight());
        canvas.drawBitmap(bitmap,bitmapSrcRect, destRec, paint);
    }

    /**
     * @param cells knight route in BoardCell items
     * @param fromPercentage allows for the animation to be resume from a specific progress.
     *                       used to resume animation after orientation change.
     */
    private void animatePath(ArrayList<BoardCell> cells, float fromPercentage){
        if(cells==null){
            return;
        }
        if(animator!=null){
            animator.cancel();
        }
        chessBoardHelper.setPathCells(cells);
        long duration = (cells.size() - 1) * KNIGHT_MOVE_DURATION;
        duration = (long) (duration*(1f-fromPercentage ));
        chessBoardHelper.createPathPointsFromPathCellsCells(true);
        animator = ValueAnimator.ofFloat(fromPercentage, 1);
        animator.setDuration(duration);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                chessBoardHelper.setPercentage((float) animation.getAnimatedValue());
                invalidate();
            }
        });
        animator.start();
    }

    /**
     * animate knight or flag bitmaps entry when user clicks on a cell
     * @param bitmapToAnimate intDef to specify which bitmap's size to animate
     */
    void animateBitmapEntry(@BitmapToAnimate final int bitmapToAnimate){
        ValueAnimator animator = ValueAnimator.ofFloat(bitmapsDrawSize/2f, bitmapsDrawSize);
        animator.setDuration(200);
        animator.setInterpolator(new DecelerateInterpolator(2f));
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float) animation.getAnimatedValue();
                if(bitmapToAnimate== BitmapToAnimate.KNIGHT){
                    knightSize = progress;
                }
                else{
                    flagSize = progress;
                }
                invalidate();
            }
        });
        animator.start();
    }

    private void calculateSizes(int width, int height){
        int chessBoardSize = Math.min(width, height);
        cellSize = chessBoardSize/(float)CHESSBOARD_SIZE;
        bitmapsDrawSize = knightSize = flagSize = cellSize*0.7f;

        chessBoardHelper.onCellSizeCalculated(cellSize);
    }

    //public methods
    public void animatePath(ArrayList<BoardCell> cells){
        animatePath(cells, 0f);
    }

    public void setOnChessBoardCellClickedInterface(OnChessBoardCellClickedInterface onChessBoardCellClickedInterface){
        this.onChessBoardCellClickedInterface = onChessBoardCellClickedInterface;
    }

    public boolean startAndStopCellsSelected() {
        return chessBoardHelper.startAndStopCellsSelected();
    }

    public BoardCell getStartCell() {
        return chessBoardHelper.getStartCell();
    }

    public BoardCell getStopCell() {
        return chessBoardHelper.getStopCell();
    }

    //keep view state on orientation changes and continue animation if one was in progress
    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        ChessBoardState ss = new ChessBoardState(superState);
        ss.chessBoardHelper = chessBoardHelper;

        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if(!(state instanceof ChessBoardState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        ChessBoardState ss = (ChessBoardState)state;
        super.onRestoreInstanceState(ss.getSuperState());

        chessBoardHelper = ss.chessBoardHelper;

        //wait for layout so that getWidth and getHeight do not return 0
        ViewUtils.doOnceOnGlobalLayoutOfView(this, new Runnable() {
            @Override
            public void run() {
                calculateSizes(getWidth(), getHeight());

                if(chessBoardHelper.getStopCell()!=null) {
                    if(chessBoardHelper.getPercentage()<1) {
                        //if there was a path animation in progress when the orientation
                        //change happened, resume it from where it stopped
                        animatePath(chessBoardHelper.getPathCells(), chessBoardHelper.getPercentage());
                    }
                    else{
                        //calculate new path points to handle screen size changes
                        chessBoardHelper.createPathPointsFromPathCellsCells(false);
                    }
                }
            }
        });


    }

    static class ChessBoardState extends BaseSavedState{
        ChessBoardHelper chessBoardHelper;

        ChessBoardState(Parcelable superState) {
            super(superState);
        }

        private ChessBoardState(Parcel in) {
            super(in);
            chessBoardHelper = in.readParcelable(ChessBoardHelper.class.getClassLoader());
        }

        public static final Creator<ChessBoardState> CREATOR = new Creator<ChessBoardState>() {
            @Override
            public ChessBoardState createFromParcel(Parcel in) {
                return new ChessBoardState(in);
            }

            @Override
            public ChessBoardState[] newArray(int size) {
                return new ChessBoardState[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(chessBoardHelper, flags);
        }
    }

    @IntDef({BitmapToAnimate.KNIGHT, BitmapToAnimate.FLAG})
    @Retention(RetentionPolicy.SOURCE)
    public @interface BitmapToAnimate {
        int KNIGHT = 1, FLAG = 2;
    }
}
