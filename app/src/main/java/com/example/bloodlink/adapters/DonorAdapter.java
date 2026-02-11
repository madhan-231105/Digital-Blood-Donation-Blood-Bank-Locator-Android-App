package com.example.bloodlink.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodlink.R;
import com.example.bloodlink.models.User;
import com.example.bloodlink.utils.DatabaseHelper;

import java.util.List;
import java.util.Locale;

public class DonorAdapter extends RecyclerView.Adapter<DonorAdapter.ViewHolder> {

    private final Context context;
    private final List<User> list;
    private final DatabaseHelper dbHelper;

    public DonorAdapter(Context context, List<User> list) {
        this.context = context;
        this.list = list;
        this.dbHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_donor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        User user = list.get(position);

        holder.name.setText(user.getName());

        // Using strings.xml placeholders (professional way)
        holder.bloodGroup.setText(
                context.getString(R.string.blood_group_label,
                        user.getBloodGroup())
        );

        holder.district.setText(
                context.getString(R.string.district_label,
                        user.getDistrict())
        );

        holder.distance.setText(
                context.getString(R.string.distance_label,
                        user.getDistance())
        );

        // Load image from SQLite
        Bitmap bitmap = dbHelper.getImage(user.getUid());
        if (bitmap != null) {
            holder.img.setImageBitmap(bitmap);
        } else {
            holder.img.setImageResource(R.mipmap.ic_launcher_round);
        }

        holder.btnCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + user.getPhone()));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, bloodGroup, district, distance;
        ImageView img;
        Button btnCall;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.tvDonorName);
            bloodGroup = itemView.findViewById(R.id.tvBloodGroup);
            district = itemView.findViewById(R.id.tvDistrict);
            distance = itemView.findViewById(R.id.tvDistance);
            img = itemView.findViewById(R.id.imgDonor);
            btnCall = itemView.findViewById(R.id.btnCall);
        }
    }
}
