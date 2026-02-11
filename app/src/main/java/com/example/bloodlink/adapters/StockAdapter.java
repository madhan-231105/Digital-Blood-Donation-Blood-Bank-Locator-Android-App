package com.example.bloodlink.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodlink.R;
import com.example.bloodlink.models.Stock;

import java.util.List;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.ViewHolder> {

    private List<Stock> stockList;

    public StockAdapter(List<Stock> stockList) {
        this.stockList = stockList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stock, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Stock stock = stockList.get(position);

        holder.tvBloodType.setText(stock.getBloodType());
        holder.tvUnits.setText("Units: " + stock.getUnits());
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvBloodType, tvUnits;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvBloodType = itemView.findViewById(R.id.tvStockBloodType);
            tvUnits = itemView.findViewById(R.id.tvStockUnits);
        }
    }
}
