package com.example.bloodlink.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bloodlink.R;
import com.example.bloodlink.models.BloodBank;
import java.util.List;
import java.util.Map;

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
        // Ensure you create item_blood_bank.xml layout
        View view = LayoutInflater.from(context).inflate(R.layout.item_blood_bank, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BloodBank bank = bloodBankList.get(position);
        holder.tvName.setText(bank.getName());

        // Display stock string
        StringBuilder stockStr = new StringBuilder("Available: ");
        if (bank.getBloodStock() != null) {
            for (Map.Entry<String, Integer> entry : bank.getBloodStock().entrySet()) {
                stockStr.append(entry.getKey()).append(":").append(entry.getValue()).append("  ");
            }
        }
        holder.tvStock.setText(stockStr.toString());
    }

    @Override
    public int getItemCount() {
        return bloodBankList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStock;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // These IDs must exist in item_blood_bank.xml
            tvName = itemView.findViewById(R.id.tvBankName);
            tvStock = itemView.findViewById(R.id.tvBankStock);
        }
    }
}