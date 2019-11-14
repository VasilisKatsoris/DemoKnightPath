package demo.knight.recyclerview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * RecyclerView adapter class that handles click events for the adapter rows. Individual click listeners
 * can be set inside the row views without causing problems in the functionality of this class
 *
 * @param <ViewHolderClass> the class of the ViewHolder the extending adapter will use
 * @param <DataClass> the class of the objects that will be represented in the adapter
 */

public abstract class ClickableRecyclerViewAdapter<ViewHolderClass extends ClickableViewHolder<DataClass>, DataClass> extends RecyclerView.Adapter<ViewHolderClass> {

    private ArrayList<DataClass> items;
    private RecyclerViewClickListener recyclerViewClickListener;

    public ArrayList<DataClass> getItems() {
        return items;
    }

    private DataClass getItem(int position){
        return getItems()!=null?getItems().get(position):null;
    }

    public void clearData() {
        if(items!=null) {
            items.clear();
        }
        notifyDataSetChanged();
    }

    public void setData(ArrayList<DataClass> items) {
        this.items = items;
    }

    public ArrayList<DataClass> getData(){
        return items;
    }

    public ClickableRecyclerViewAdapter(ArrayList<DataClass> items, RecyclerViewClickListener<DataClass> onItemClickListener) {
        setRecyclerViewClickListener(onItemClickListener);
        setData(items);
    }

    RecyclerViewClickListener getRecyclerViewClickListener() {
        return recyclerViewClickListener;
    }

    private void setRecyclerViewClickListener(RecyclerViewClickListener recyclerViewClickListener) {
        this.recyclerViewClickListener = recyclerViewClickListener;
    }


    @NonNull
    @Override
    public final ViewHolderClass onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return onViewHolderCreate(viewGroup, i);
    }

    @Override
    public final void onBindViewHolder(@NonNull ViewHolderClass holder, int position) {
        holder.setPosition(position);
        holder.setData(getItem(position));
        onViewHolderBind(holder, getItem(position),position);
    }

    @Override
    public int getItemCount() {
        return items!=null ? items.size() : 0;
    }

    public abstract ViewHolderClass onViewHolderCreate(ViewGroup viewGroup, int viewType);

    public abstract void onViewHolderBind(ViewHolderClass holder, DataClass data, int position);

}
