package com.example.bloodlink.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodlink.R;
import com.example.bloodlink.activities.BloodBankDetailActivity;
import com.example.bloodlink.models.BloodBank;

import java.util.List;

public class BloodBankAdapter extends RecyclerView.Adapter<BloodBankAdapter.ViewHolder> {

    private Context context;
    private List<BloodBank> bloodBankList;

    public BloodBankAdapter(Context context, List<BloodBank> bloodBankList) {
        this.context = context;
        this.bloodBankList = bloodBankList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_blood_bank, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        BloodBank bank = bloodBankList.get(position);

        holder.tvName.setText(bank.getName());
        holder.tvType.setText("Type: " + bank.getType());
        holder.tvDistrict.setText("District: " + bank.getDistrict());
        holder.tvPhone.setText("Phone: " + bank.getPhone());

        // Open Detail Page
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BloodBankDetailActivity.class);
            intent.putExtra("bankId", bank.getId());
            context.startActivity(intent);
        });

        // Call Button
        holder.btnCall.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + bank.getPhone()));
            context.startActivity(callIntent);
        });
    }

    @Override
    public int getItemCount() {
        return bloodBankList != null ? bloodBankList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvType, tvDistrict, tvPhone;
        Button btnCall;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvBankName);
            tvType = itemView.findViewById(R.id.tvBankType);
            tvDistrict = itemView.findViewById(R.id.tvBankDistrict);
            tvPhone = itemView.findViewById(R.id.tvBankPhone);
            btnCall = itemView.findViewById(R.id.btnCallBank);
        }
    }
}
