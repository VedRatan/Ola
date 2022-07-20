package com.example.android.ola_clone.RecyclerViewHelper;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.ola_clone.ExpandedHistoryAcitvity;
import com.example.android.ola_clone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HistoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView rideId;
    public TextView time;
    String checker;
    public HistoryViewHolder(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        time = itemView.findViewById(R.id.time);
        rideId = itemView.findViewById(R.id.rideId);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(view.getContext(), ExpandedHistoryAcitvity.class);
        intent.putExtra("rideId", rideId.getText().toString());
      view.getContext().startActivity(intent);
    }
}
