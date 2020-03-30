package edu.njit.quill.ui;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import edu.njit.quill.R;
import edu.njit.quill.model.Journal;

public class JournalRecyclerAdapter extends RecyclerView.Adapter<JournalRecyclerAdapter.ViewHolder> {

    private Context context;
    private List<Journal> journalList;

    public JournalRecyclerAdapter(Context context, List<Journal> journalList) {
        this.context = context;
        this.journalList = journalList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.journal_row, parent, false);
        return new ViewHolder(view, parent.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Journal journal = journalList.get(position);
        String imageUrl, timeAgo;

        holder.titleTextView.setText(journal.getTitle());
        holder.thoughtTextView.setText(journal.getThought());
        holder.usernametextView.setText(journal.getUsername());
        imageUrl = journal.getImageUrl();

        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.image_one)
                .fit()
                .into(holder.journalImageView);

        timeAgo = (String) DateUtils.getRelativeTimeSpanString(journal.getTimeAdded().getSeconds() * 1000);
        holder.journalTimeTextView.setText(timeAgo);
    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView, thoughtTextView, journalTimeTextView, usernametextView;
        public ImageView journalImageView;
        public ImageButton shareButton;
        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);
            context = ctx;
            titleTextView = itemView.findViewById(R.id.journal_title_list);
            thoughtTextView = itemView.findViewById(R.id.journal_thoughts_list);
            journalTimeTextView = itemView.findViewById(R.id.journal_timestamp_list);
            journalImageView = itemView.findViewById(R.id.journal_image_list);
            usernametextView = itemView.findViewById(R.id.journal_list_username);
            shareButton = itemView.findViewById(R.id.share_button);

            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   // context.startActivity();
                }
            });
        }
    }
}
