package com.example.semestralkavamz.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.semestralkavamz.Comparators.MyNotesComparators;
import com.example.semestralkavamz.R;
import com.example.semestralkavamz.adapter.NoteAdapter;
import com.example.semestralkavamz.data.Note;
import com.example.semestralkavamz.database.NotesDatabase;
import com.example.semestralkavamz.interfaces.NotesListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IntroScreenActivity extends AppCompatActivity implements NotesListener {

    private static final int REQUEST_CODE_ADD_NOTE = 1;
    private static final int REQUEST_CODE_UPDATE_NOTE = 2;
    private static final int REQUEST_CODE_SHOW_NOTES = 3;
    private static final int REQUEST_CODE_SHOW_NOTES_AZ = 4;
    private static final int REQUEST_CODE_SHOW_NOTES_ZA = 5;
    private static final int REQUEST_CODE_SHOW_NOTES_TIME = 6;
    private static final int REQUEST_CODE_REFRESH_NOTES = 7;
    private RecyclerView notesRecycler;
    private List<Note> notesList;
    private NoteAdapter notesAdapter;
    private NotesListener listener = this;



    private int noteClickedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_screen);
        ImageView addNote = findViewById(R.id.addNoteImage);

        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getApplicationContext(), NewNoteActivity.class),REQUEST_CODE_ADD_NOTE);
            }
        });

        initializeRecyclerView();
        getNotes(REQUEST_CODE_SHOW_NOTES,false);
        filtering();
        initializeSearcher();

    }
    private void initializeRecyclerView() {

        notesRecycler = findViewById(R.id.recyclerView);
        //keby nieco tak tu staggeredGridlayout
        notesRecycler.setLayoutManager(new GridLayoutManager(this,GridLayoutManager.VERTICAL));
        notesList = new ArrayList<>();
        notesAdapter = new NoteAdapter(notesList,this);
        notesRecycler.setAdapter(notesAdapter);


    }
    private void initializeSearcher() {
        EditText searcher = findViewById(R.id.input_search);
        searcher.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                notesAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (notesList.size()!= 0) {
                    notesAdapter.searchNotes(editable.toString());
                }
            }
        });
    }
    private void filtering() {
        findViewById(R.id.sortAZ).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNotes(REQUEST_CODE_SHOW_NOTES_AZ,false);


            }
        });

        findViewById(R.id.sortZA).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNotes(REQUEST_CODE_SHOW_NOTES_ZA,false);
            }
        });

        findViewById(R.id.sortTime).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNotes(REQUEST_CODE_SHOW_NOTES_TIME,false);
            }
        });

        findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNotes(REQUEST_CODE_REFRESH_NOTES,false);
            }
        });
    }

    private void getNotes(final int reqCode,final boolean isDeleted) {

        @SuppressLint("StaticFieldLeak")
                class GetNoteTask extends AsyncTask<Void,Void, List<Note>> {

            @Override
            protected List<Note> doInBackground(Void... voids) {

                return NotesDatabase.getDatabase(getApplicationContext()).noteDao().notesList();


            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                if (reqCode == REQUEST_CODE_SHOW_NOTES) {
                    notesList.addAll(notes);

                    notesAdapter.notifyDataSetChanged();
                } else if (reqCode == REQUEST_CODE_ADD_NOTE) {
                    notesList.add(0,notes.get(0));
                    notesAdapter.notifyItemInserted(0);
                    notesRecycler.smoothScrollToPosition(0);

                } else if (reqCode == REQUEST_CODE_UPDATE_NOTE) {
                    notesList.remove(noteClickedPosition);

                    if(isDeleted) {
                        notesAdapter.notifyItemRemoved(noteClickedPosition);
                    } else {
                        notesList.add(noteClickedPosition,notes.get(noteClickedPosition));
                        notesAdapter.notifyItemChanged(noteClickedPosition);

                    }
                } else if(reqCode == REQUEST_CODE_SHOW_NOTES_AZ) {
                    notesList.clear();
                    Collections.sort(notes,MyNotesComparators.compareAZ);
                    notesList.addAll(notes);
                    notesAdapter.notifyDataSetChanged();



                } else if(reqCode == REQUEST_CODE_SHOW_NOTES_ZA) {
                    notesList.clear();
                    Collections.sort(notes,MyNotesComparators.compareZA);
                    notesList.addAll(notes);
                    notesAdapter.notifyDataSetChanged();

                } else if(reqCode == REQUEST_CODE_SHOW_NOTES_TIME) {
                    notesList.clear();
                    Collections.sort(notes,MyNotesComparators.compareTime);
                    notesList.addAll(notes);
                    notesAdapter.notifyDataSetChanged();

                } else if(reqCode == REQUEST_CODE_REFRESH_NOTES) {
                    notesList.clear();
                    notesList.addAll(notes);
                    notesAdapter.notifyDataSetChanged();
                }

            }
        }
        new GetNoteTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getNotes(REQUEST_CODE_ADD_NOTE,false);
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                getNotes(REQUEST_CODE_UPDATE_NOTE,data.getBooleanExtra("isNoteDeleted",false));
            }
        }
    }


    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(),NewNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note",note);
        startActivityForResult(intent,REQUEST_CODE_UPDATE_NOTE);
    }



}
