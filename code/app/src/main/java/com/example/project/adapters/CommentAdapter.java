package com.example.project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.Usercomment;

import java.text.SimpleDateFormat;
import java.util.List;


/**
 * Adapter for displaying a list of user comments in a RecyclerView.
 * Binds {@link Usercomment} data to the corresponding views in each item layout.
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private List<Usercomment> commentList;


    /**
     * Constructor for the CommentAdapter.
     *
     * @param commentList List of user comments .
     */
    public CommentAdapter(List<Usercomment> commentList) {
        this.commentList = commentList;
    }

    /**
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return A new ViewHolder that holds the view for a comment item.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    /**
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //bind
        Usercomment comment = commentList.get(position);
        holder.tvAuthor.setText(comment.getAuthor());
        holder.tvContent.setText(comment.getContent());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        holder.tvTimestamp.setText(sdf.format(comment.getTimestamp()));
    }

    /**
     *
     * @return The number of comments.
     */

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    /**
     * ViewHolder class for handling views for each comment item.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAuthor, tvContent, tvTimestamp;


        /**
         *
         * @param itemView tHE ROOT VIEW OF THE COMMENT ITEM LAYOUT.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}