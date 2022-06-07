package com.example.semestralkavamz.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.room.RoomDatabase;

import com.example.semestralkavamz.R;
import com.example.semestralkavamz.adapter.NoteAdapter;
import com.example.semestralkavamz.data.Note;
import com.example.semestralkavamz.database.NotesDatabase;
import com.example.semestralkavamz.interfaces.NotesListener;

import java.util.ArrayList;
import java.util.List;

public class IntroScreenActivity extends AppCompatActivity implements NotesListener {

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTES = 3;
    public static final int REQUEST_CODE_SHOW_NOTES_AZ = 4;
    public static final int REQUEST_CODE_SHOW_NOTES_ZA = 5;
    public static final int REQUEST_CODE_SHOW_NOTES_TIME = 6;
    private RecyclerView notesRecycler;
    private List<Note> notesList;
    private List<Note> notesAZList;
    private List<Note> notesZAList;
    private List<Note> notesTimeList;
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
        sortAZ();
        initializeSearcher();

    }
    private void initializeRecyclerView() {

        notesRecycler = findViewById(R.id.recyclerView);
        //keby nieco tak tu staggeredGridlayout
        notesRecycler.setLayoutManager(new GridLayoutManager(this,GridLayoutManager.VERTICAL));
        notesList = new ArrayList<>();
        notesAZList = new ArrayList<>();
        notesZAList = new ArrayList<>();
        notesTimeList = new ArrayList<>();
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
    private void sortAZ() {
        findViewById(R.id.sortAZ).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNotes(REQUEST_CODE_SHOW_NOTES_AZ,false);
                Log.d("TAG","KLIKOL SI");
            }
        });
    }

    private void getNotes(final int reqCode,final boolean isDeleted) {

        @SuppressLint("StaticFieldLeak")
                class GetNoteTask extends AsyncTask<Void,Void, List<Note>> {

            @Override
            protected List<Note> doInBackground(Void... voids) {
                if(reqCode == REQUEST_CODE_SHOW_NOTES_AZ ) {
                    return NotesDatabase.getDatabase(getApplicationContext()).noteDao().notesAZList();
                } else if(reqCode == REQUEST_CODE_SHOW_NOTES_ZA) {
                    return NotesDatabase.getDatabase(getApplicationContext()).noteDao().notesZAList();
                } else if (reqCode == REQUEST_CODE_SHOW_NOTES_TIME) {
                    return NotesDatabase.getDatabase(getApplicationContext()).noteDao().notesTimeList();
                } else {
                    return NotesDatabase.getDatabase(getApplicationContext()).noteDao().notesList();
                }

            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                if (reqCode == REQUEST_CODE_SHOW_NOTES) {
                    notesList.addAll(notes);
                    //notesAZList.addAll(notes);
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

                    notesAZList.addAll(notes);
                    setMyAdapter(notesAZList);
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


    public void setMyAdapter(List<Note> notes) {
        notesRecycler.setLayoutManager(new GridLayoutManager(this,GridLayoutManager.VERTICAL));
        notesAdapter = new NoteAdapter(notes,listener);
        notesRecycler.setAdapter(notesAdapter);
    }
}
