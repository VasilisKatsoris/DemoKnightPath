package demo.knight;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

import demo.knight.chessboard.BoardCell;
import demo.knight.chessboard.ChessBoard;
import demo.knight.chessboard.OnChessBoardCellClickedInterface;
import demo.knight.recyclerview.RecyclerViewClickListener;
import demo.knight.recyclerview.SolutionsAdapter;
import demo.knight.solver.KnightRouteSolver;
import demo.knight.solver.Solution;

public class MainActivity extends AppCompatActivity implements OnChessBoardCellClickedInterface, RecyclerViewClickListener<Solution> {

    RecyclerView recyclerView;
    Button button;
    ChessBoard chessBoard;
    SolutionsAdapter adapter;
    ArrayList<Solution> solutions;
    KnightRouteSolver knightRouteSolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        chessBoard = findViewById(R.id.chessboard);
        button = findViewById(R.id.calculate_path_button);
        View listAndButtonLayout = findViewById(R.id.list_and_button_layout);
        ConstraintLayout root = findViewById(R.id.main_activity_root);
        recyclerView = findViewById(R.id.recyclerview);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCalculateClicked();
            }
        });

        chessBoard.setOnChessBoardCellClickedInterface(this);

        //if landscape adust layout
        if(getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT){
            listAndButtonLayout.getLayoutParams().width=0;
            ConstraintSet set = new ConstraintSet();
            set.clone(root);
            set.connect(listAndButtonLayout.getId(), ConstraintSet.LEFT, chessBoard.getId(), ConstraintSet.RIGHT );
            set.connect(listAndButtonLayout.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT );
            set.connect(listAndButtonLayout.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            set.applyTo(root);
        }

        knightRouteSolver = new KnightRouteSolver();
    }

    void onCalculateClicked(){
        ArrayList<Solution> solutions;
        if(chessBoard.startAndStopCellsSelected()){
            BoardCell start = new BoardCell(chessBoard.getStartCell());
            BoardCell end = new BoardCell(chessBoard.getStopCell());

            solutions = knightRouteSolver.solveRoute(start, end);
            if(solutions.size()==0){
                solutions.add(new Solution(getString(R.string.cannot_solve_in_x_moves, KnightRouteSolver.MAX_STEPS)));
            }
        }
        else{
            solutions = new ArrayList<>();
            solutions.add(new Solution(getString(R.string.select_start_and_destination_positions)));
        }
        showSolutionsOnRecyclerView(solutions, true);
    }

    void showSolutionsOnRecyclerView(ArrayList<Solution> solutions, boolean showInChessBoard){
        this.solutions = solutions;

        if(adapter == null) {
            adapter = new SolutionsAdapter(solutions, this);
            recyclerView.setAdapter(adapter);
        }
        else{
            adapter.setData(solutions);
            adapter.notifyDataSetChanged();
        }

        if(showInChessBoard && solutions.size()>0) {
            onItemClicked(solutions.get(0), 0);
        }
    }

    @Override
    public void onChessBoardCellClicked() {
        //clear the RecyclerView because a BoardCell was clicked
        //which means the user reset the ChessBoard
        showSolutionsOnRecyclerView(new ArrayList<Solution>(), false);
    }

    @Override
    public void onItemClicked(Solution clickedSolution, int position) {
        for(Solution solution:adapter.getItems()){
            solution.setSelected(false);
        }
        clickedSolution.setSelected(true);
        adapter.notifyDataSetChanged();
        if(clickedSolution.getCellsPath()!=null) {
            chessBoard.animatePath(clickedSolution.getCellsPath());
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("solutions",solutions);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        solutions = savedInstanceState.getParcelableArrayList("solutions");
        if(solutions!=null){
            showSolutionsOnRecyclerView(solutions, false);
        }
    }
}
