package demo.knight.recyclerview;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;

import demo.knight.R;
import demo.knight.solver.Solution;

public class SolutionsAdapter extends ClickableRecyclerViewAdapter<SolutionsAdapter.SolutionsHolder, Solution> {


    public SolutionsAdapter(ArrayList<Solution> items, RecyclerViewClickListener<Solution> onItemClickListener) {
        super(items, onItemClickListener);
    }

    @Override
    public SolutionsHolder onViewHolderCreate(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_solution, viewGroup, false);
        return new SolutionsHolder(itemView, this);
    }

    @Override
    public void onViewHolderBind(SolutionsHolder holder, Solution data, int position) {
        Solution solution = getItems().get(position);
        holder.tv.setText(solution.getDescription());
        holder.radioButton.setChecked(solution.isSelected());
        holder.radioButton.setVisibility(solution.isForText() ? View.GONE:View.VISIBLE);
    }


    static class SolutionsHolder extends ClickableViewHolder<Solution>{

        TextView tv;
        RadioButton radioButton;

        SolutionsHolder(@NonNull View view, ClickableRecyclerViewAdapter adapter) {
            super(view, adapter);
            tv = view.findViewById(R.id.row_solution_tv);
            radioButton = view.findViewById(R.id.row_solution_radio_btn);
        }
    }
}
