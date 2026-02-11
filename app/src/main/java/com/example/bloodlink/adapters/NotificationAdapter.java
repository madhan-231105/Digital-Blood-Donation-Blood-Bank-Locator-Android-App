package com.example.bloodlink.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodlink.R;
import com.example.bloodlink.models.BloodRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class NotificationAdapter
        extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Context context;
    private List<BloodRequest> list;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public NotificationAdapter(Context context, List<BloodRequest> list) {
        this.context = context;
        this.list = list;
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_notification, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        BloodRequest request = list.get(position);

        // ================= SET DATA =================

        holder.tvRequesterName.setText(
                request.getRequesterName() != null
                        ? request.getRequesterName()
                        : "Unknown");

        holder.tvBloodGroup.setText(
                "Blood Needed: " + request.getBloodGroup());

        holder.tvLocation.setText(
                "Location: " + request.getLocationMessage());

        holder.tvDistrict.setText(
                "District: " + request.getDistrict());

        // ================= CALL BUTTON =================

        holder.btnCall.setOnClickListener(v -> {

            if (request.getPhone() == null) {
                Toast.makeText(context,
                        "Phone number not available",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + request.getPhone()));
            context.startActivity(intent);
        });

        // ================= REJECT BUTTON =================

        holder.btnReject.setOnClickListener(v -> {

            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION)
                return;

            BloodRequest currentRequest = list.get(adapterPosition);

            if (currentRequest.getId() == null) {
                Toast.makeText(context,
                        "Request ID missing",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (mAuth.getCurrentUser() == null) return;

            String myUid = mAuth.getCurrentUser().getUid();

            db.collection("blood_requests")
                    .document(currentRequest.getId())
                    .update("rejectedBy",
                            FieldValue.arrayUnion(myUid))
                    .addOnSuccessListener(unused -> {

                        // Remove only for this user
                        list.remove(adapterPosition);
                        notifyItemRemoved(adapterPosition);

                        Toast.makeText(context,
                                "Request Hidden",
                                Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context,
                                    "Reject failed: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show());
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ================= VIEW HOLDER =================

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvRequesterName, tvBloodGroup, tvLocation, tvDistrict;
        Button btnCall, btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvRequesterName =
                    itemView.findViewById(R.id.tvRequesterName);

            tvBloodGroup =
                    itemView.findViewById(R.id.tvReqBloodGroup);

            tvLocation =
                    itemView.findViewById(R.id.tvReqLocation);

            tvDistrict =
                    itemView.findViewById(R.id.tvReqDistrict);

            btnCall =
                    itemView.findViewById(R.id.btnCallRequester);

            btnReject =
                    itemView.findViewById(R.id.btnReject);
        }
    }
}
