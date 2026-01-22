package com.example.bloodlink.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.bloodlink.R;
import com.example.bloodlink.models.User;
import java.util.List;

public class DonorAdapter extends RecyclerView.Adapter<DonorAdapter.ViewHolder> {

    private Context context;
    private List<User> list;

    public DonorAdapter(Context context, List<User> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_donor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = list.get(position);
        holder.name.setText(user.getName());
        holder.bloodGroup.setText(user.getBloodGroup());

        if (user.getProfileImageUrl() != null) {
            Glide.with(context).load(user.getProfileImageUrl()).circleCrop().into(holder.img);
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
        TextView name, bloodGroup;
        ImageView img;
        Button btnCall;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvDonorName);
            bloodGroup = itemView.findViewById(R.id.tvBloodGroup);
            img = itemView.findViewById(R.id.imgDonor);
            btnCall = itemView.findViewById(R.id.btnCall);
        }
    }
}