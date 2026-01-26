package com.example.bloodlink.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodlink.R;
import com.example.bloodlink.models.PlaceItem;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.ViewHolder> {

    private List<PlaceItem> list;
    private SharedPreferences prefs;
    private Set<String> favSet;

    public PlaceAdapter(Context ctx, List<PlaceItem> list) {
        this.list = list;
        prefs = ctx.getSharedPreferences("favs", Context.MODE_PRIVATE);
        favSet = prefs.getStringSet("fav_names", new HashSet<>());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int i) {
        PlaceItem p = list.get(i);

        h.name.setText(p.name);
        h.type.setText(p.type);
        h.distance.setText(String.format("%.2f km", p.distance));

        boolean isFav = favSet.contains(p.name);
        h.favIcon.setImageResource(
                isFav ? R.drawable.ic_star : R.drawable.ic_star_border
        );

        h.favIcon.setOnClickListener(v -> {
            if (favSet.contains(p.name)) {
                favSet.remove(p.name);
            } else {
                favSet.add(p.name);
            }
            prefs.edit().putStringSet("fav_names", favSet).apply();
            notifyItemChanged(i);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, type, distance;
        ImageView favIcon;

        ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            type = v.findViewById(R.id.type);
            distance = v.findViewById(R.id.distance);
            favIcon = v.findViewById(R.id.favIcon);
        }
    }
}
