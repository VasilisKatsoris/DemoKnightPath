package demo.knight.recyclerview;

public interface RecyclerViewClickListener<DataClass> {
    void onItemClicked(DataClass data, int position);
}
