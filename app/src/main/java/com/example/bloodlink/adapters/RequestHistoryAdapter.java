package com.example.bloodlink.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodlink.R;
import com.example.bloodlink.models.RequestModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class RequestHistoryAdapter
        extends RecyclerView.Adapter<RequestHistoryAdapter.ViewHolder> {

    private Context context;
    private List<RequestModel> list;
    private FirebaseFirestore db;

    public RequestHistoryAdapter(Context context, List<RequestModel> list) {
        this.context = context;
        this.list = list;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_request_history, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        RequestModel model = list.get(position);

        // 🔥 Null Safety
        String blood = model.getBloodGroup() != null ? model.getBloodGroup() : "N/A";
        String status = model.getStatus() != null ? model.getStatus() : "pending";

        holder.tvBlood.setText("Blood: " + blood);
        holder.tvStatus.setText("Status: " + status);

        // Reset button state (important for RecyclerView reuse)
        holder.btnComplete.setEnabled(true);

        // 🔴 Pending
        if ("pending".equalsIgnoreCase(status)) {

            holder.tvStatus.setTextColor(Color.RED);
            holder.btnComplete.setText("Mark as Complete");
            holder.btnComplete.setBackgroundColor(Color.parseColor("#D32F2F"));
        }

        // 🟢 Completed
        else if ("completed".equalsIgnoreCase(status)) {

            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
            holder.btnComplete.setText("Completed");
            holder.btnComplete.setEnabled(false);
            holder.btnComplete.setBackgroundColor(Color.LTGRAY);
        }

        // 🔥 Complete Button Click
        holder.btnComplete.setOnClickListener(v -> {

            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) return;

            db.collection("blood_requests")
                    .document(model.getId())
                    .update("status", "completed")
                    .addOnSuccessListener(unused -> {

                        model.setStatus("completed");
                        notifyItemChanged(adapterPosition);

                        Toast.makeText(context,
                                "Request marked as completed",
                                Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context,
                                    "Update failed: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvBlood, tvStatus;
        Button btnComplete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvBlood = itemView.findViewById(R.id.tvReqBlood);
            tvStatus = itemView.findViewById(R.id.tvReqStatus);
            btnComplete = itemView.findViewById(R.id.btnCompleteRequest);
        }
    }
}
