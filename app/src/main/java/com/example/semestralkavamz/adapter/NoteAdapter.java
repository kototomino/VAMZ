package com.example.semestralkavamz.adapter;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.semestralkavamz.R;

import com.example.semestralkavamz.data.Note;

import com.example.semestralkavamz.interfaces.NotesListener;
import com.makeramen.roundedimageview.RoundedImageView;


import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private List<com.example.semestralkavamz.data.Note> notes;
    private NotesListener notesListener;
    private Timer timer;
    private List<Note> sourceNotes;

    public NoteAdapter(List<Note> notes, NotesListener notesListener) {
        this.notes = notes;
        this.notesListener = notesListener;
        this.sourceNotes = notes;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note_view,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.initializeNote(notes.get(position));
        holder.layoutNoteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notesListener.onNoteClicked(notes.get(position),position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView titleText, timeText;
        LinearLayout layoutNoteItem;
        RoundedImageView imageNoteView;
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            timeText = itemView.findViewById(R.id.timeText);
            layoutNoteItem = itemView.findViewById(R.id.item_note_view_layout);
            imageNoteView = itemView.findViewById(R.id.imageNoteView);

        }

        void initializeNote(Note note) {
            titleText.setText(note.getTitle());
            timeText.setText(note.getTime());

            GradientDrawable gradientDrawable = (GradientDrawable) layoutNoteItem.getBackground();
            if (note.getColor() != null) {
                gradientDrawable.setColor(Color.parseColor(note.getColor()));

            } else {
                gradientDrawable.setColor(Color.parseColor("#292929"));
            }

            if (note.getImagePath() != null &&  !note.getImagePath().trim().isEmpty()) {
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inSampleSize = 3;
//                Bitmap bitmap = BitmapFactory.decodeFile(note.getImagePath());
                //imageNoteView.setImageBitmap(bitmap);
                imageNoteView.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
                imageNoteView.setVisibility(View.VISIBLE);
            } else {
                imageNoteView.setVisibility(View.GONE);
            }
        }
    }
    public void searchNotes(final String key) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (key.trim().isEmpty()) {
                    notes = sourceNotes;
                } else {
                    ArrayList<Note> temp = new ArrayList<>();
                    for (Note note : sourceNotes) {
                        if (note.getTitle().toLowerCase().contains(key.toLowerCase()) ||
                                note.getInputNote().toLowerCase().contains(key.toLowerCase())) {
                            temp.add(note);
                        }
                    }
                    notes = temp;

                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }

        },500);
    }
    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }
}
