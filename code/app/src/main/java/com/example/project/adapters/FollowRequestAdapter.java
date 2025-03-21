package com.example.project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;

import java.util.List;

/**
 * Shows a list of PENDING follow requests to the current user.
 */
public class FollowRequestAdapter extends RecyclerView.Adapter<FollowRequestAdapter.ViewHolder> {

    public static class RequestItem {
        public String fromUser;
        public RequestItem(String fromUser) {
            this.fromUser = fromUser;
        }
    }

    public interface DecisionListener {
        void onAccept(String fromUser);
        void onReject(String fromUser);
    }

    private List<RequestItem> requestList;
    private DecisionListener decisionListener;

    public FollowRequestAdapter(List<RequestItem> requestList, DecisionListener listener) {
        this.requestList = requestList;
        this.decisionListener = listener;
    }

    @NonNull
    @Override
    public FollowRequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_follow_request, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowRequestAdapter.ViewHolder holder, int position) {
        RequestItem item = requestList.get(position);
        holder.txtFromUser.setText(item.fromUser);

        holder.btnAccept.setOnClickListener(v -> {
            if (decisionListener != null) {
                decisionListener.onAccept(item.fromUser);
            }
        });
        holder.btnReject.setOnClickListener(v -> {
            if (decisionListener != null) {
                decisionListener.onReject(item.fromUser);
            }
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtFromUser;
        Button btnAccept, btnReject;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtFromUser = itemView.findViewById(R.id.txtFromUser);
            btnAccept   = itemView.findViewById(R.id.btnAccept);
            btnReject   = itemView.findViewById(R.id.btnReject);
        }
    }
}

