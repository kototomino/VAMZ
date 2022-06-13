package com.example.semestralkavamz.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.semestralkavamz.R;
import com.example.semestralkavamz.data.Note;
import com.example.semestralkavamz.interfaces.NotesListener;


import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
/**
 * @author TOMAS KOTRIK 2022
 * Trieda NoteAdapter dedi od recyclerViewu jeho adapter a dotvara ho este poznamkovy view holder
 * Tu sa vpodstate nastavuje Vizualizacia poznamok pre recycler view
 * */
public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    //notes su vseobecne poznamky
    private List<Note> notes;
    private NotesListener notesListener;
    //casovac pre priebeh vyhladavania poznamky
    private Timer timer;
    // source notes su poznamky pre vyhladavanie
    private List<Note> sourceNotes;

    public NoteAdapter(List<Note> notes, NotesListener notesListener) {
        this.notes = notes;
        this.notesListener = notesListener;
        this.sourceNotes = notes;
    }

    /**
     * Vytvorenie View holdera ktory vytvori vzhlad danej poznamky
     * */

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NoteViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note_view, parent, false));
    }

    /**
     * Bindovanie view holdera v zmysle nastavenie onclick Listenera kde prekriva metodu onNoteClicked
     * */
    @Override
    public void onBindViewHolder( NoteViewHolder holder, int position) {
        holder.initializeNote(notes.get(position));
        holder.layoutNoteItem.setOnClickListener(view -> notesListener.onNoteClicked(notes.get(position),position));
    }

    /**
     * Vrati velkost vsetkych poznamok cize kolko poznamok je momentalne v liste
     * */
    @Override
    public int getItemCount() {
        return notes.size();
    }

    /**
     * Vrati poziciu danej poznamky
     * */
    @Override
    public int getItemViewType(int position) {
        return position;
    }

    /**
     * Vnutorna trieda ktora nastavuje cely obsah danej poznamky na zovnajsku cize v IntroScreenActivity
     * */
    public static class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView titleText, timeText,imageNoteText;
        LinearLayout layoutNoteItem;

        public NoteViewHolder( View itemView) {
            super(itemView);
            //nastavenie fieldov
            titleText = itemView.findViewById(R.id.titleText);
            timeText = itemView.findViewById(R.id.timeText);
            layoutNoteItem = itemView.findViewById(R.id.item_note_view_layout);
            imageNoteText = itemView.findViewById(R.id.imageNoteText);

        }

        /**
         * initializeNote metoda sluzi na Nastavenie potrebnych fieldov a zobrazenie farby a
         * ak je aj obrazok vo vnutri poznamky tak sa ukaze text ze poznamka obsahuje obrazok
         * */
        void initializeNote(Note note) {
            titleText.setText(note.getTitle());
            timeText.setText(note.getTime());
            //ak pouzivatel zadal customizovanu farbu poznamky tak sa nastavi inak je defaultne tmava
            GradientDrawable gradientDrawable = (GradientDrawable) layoutNoteItem.getBackground();
            if (note.getColor() != null) {
                gradientDrawable.setColor(Color.parseColor(note.getColor()));

            } else {
                gradientDrawable.setColor(Color.parseColor("#292929"));
            }
            //ak sa nachadza obrazok tak nastavi text ze poznamka obsahuje obrazok inak zakryje dany text
            if (note.getImagePath() != null &&  !note.getImagePath().trim().isEmpty()) {
               imageNoteText.setVisibility(View.VISIBLE);
            } else {
                imageNoteText.setVisibility(View.GONE);
            }
        }
    }
    /**
     * Vyhladavanie poznamok
     * nastavi sa casovy Task kde je delay 500ms
     * postupne sa vyhlada dana poznamka v zmysle containst takze nemusi byt presne zadana
     * staci ak si pouzivatel pamata priblizny nazov
     *
     * */
    public void searchNotes(final String key) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (key.trim().isEmpty()) {
                    //ak parameter zadaneho textboxu je prazdny tak sa nic nevyhlada teda zobrazia sa vsetky poznamky
                    notes = sourceNotes;
                } else {
                    //ak parameter nieje "" tak sa ide hladat temp list ako docasny kde sa budu vkladat zhody najdenia
                    ArrayList<Note> temp = new ArrayList<>();
                    for (Note note : sourceNotes) {
                        //for each cez poznamky ak sa zadany nazov nachadza tak sa prida do temp listu
                        if (note.getTitle().toLowerCase().contains(key.toLowerCase()) ||
                                note.getInputNote().toLowerCase().contains(key.toLowerCase())) {
                            temp.add(note);
                        }
                    }
                    //po pridany sa povodny list nahradi zhodami vo vyhladavani
                    notes = temp;

                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void run() {
                        //kym uzivatel hlada tak sa updatuje view
                        notifyDataSetChanged();
                    }
                });
            }

        },500);
    }
    //zrusenie timeru
    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }
}
