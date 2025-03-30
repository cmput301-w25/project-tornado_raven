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


    /**
     * Represents a single follow request item from another user.
     */
    public static class RequestItem {
        public String fromUser;

        /**
         *
         * @param fromUser Who sent the follow request.
         */
        public RequestItem(String fromUser) {
            this.fromUser = fromUser;
        }
    }

    /**
     * Listener interface to handle accept and reject actions for follow requests.
     */
    public interface DecisionListener {
        void onAccept(String fromUser);
        void onReject(String fromUser);
    }

    private List<RequestItem> requestList;
    private DecisionListener decisionListener;

    /**
     *
     * @param requestList the list of pending follow requests.
     * @param listener the listener to handle accept/reject actions.
     */
    public FollowRequestAdapter(List<RequestItem> requestList, DecisionListener listener) {
        this.requestList = requestList;
        this.decisionListener = listener;
    }

    /**
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return A new viewholder instance
     */
    @NonNull
    @Override
    public FollowRequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_follow_request, parent, false);
        return new ViewHolder(v);
    }


    /**
     *
     * @param holder The ViewHolder in bind.
     * @param position The position of the item within the adapter's data set.
     */
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


    /**
     *
     * @return The size of the list.
     */
    @Override
    public int getItemCount() {
        return requestList.size();
    }

    /**
     * ViewHolder class for the follow request item layout.
     * Holds views for displaying the username and action buttons.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtFromUser;
        Button btnAccept, btnReject;

        /**
         *
         * @param itemView The inflated item view.
         */
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtFromUser = itemView.findViewById(R.id.txtFromUser);
            btnAccept   = itemView.findViewById(R.id.btnAccept);
            btnReject   = itemView.findViewById(R.id.btnReject);
        }
    }
}

