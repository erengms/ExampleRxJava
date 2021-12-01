package com.example.examplerxjava.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examplerxjava.databinding.RecyclerRowBinding;
import com.example.examplerxjava.model.Place;
import com.example.examplerxjava.view.MapsActivity;

import java.util.ArrayList;
import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceHolder>  implements Filterable {

    List<Place> placeList;
    private List<Place> placeListForSearch;

    public PlaceAdapter(List<Place> placeList){
        this.placeList = placeList;

        placeListForSearch = new ArrayList<>(placeList);
    }

    @NonNull
    @Override
    public PlaceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PlaceHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceHolder holder, int position) {
        holder.recyclerRowBinding.recyclerText.setText(placeList.get(position).name);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), MapsActivity.class);
                intent.putExtra("info", "old");
                intent.putExtra("place", placeList.get(holder.getAdapterPosition())); //Listeden bir Place objesi gönderiyoruz, Place sınıfını Serializable etmemiz gerekir.
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    @Override
    public Filter getFilter() {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Place> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(placeListForSearch);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (Place item : placeListForSearch) {
                        if (item.name.toLowerCase().contains(filterPattern)) {
                            filteredList.add(item);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredList;

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                placeList.clear();
                placeList.addAll((List) results.values);
                notifyDataSetChanged();
            }
        };
    }

    public class PlaceHolder extends RecyclerView.ViewHolder {

        RecyclerRowBinding recyclerRowBinding;

        public PlaceHolder(RecyclerRowBinding recyclerRowBinding) {
            super(recyclerRowBinding.getRoot());
            this.recyclerRowBinding = recyclerRowBinding;
        }
    }
}
