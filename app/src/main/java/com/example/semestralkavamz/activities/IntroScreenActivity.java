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
/**
 * @author TOMAS KOTRIK 2022
* IntroScreenActivity je aktivita kde sa uchovavaju vsetky doposial zapisane poznamky
* v tejto aktivite si moze uzivatel vytvorit svoju poznamku
* hladat medzi nimi pomocou search baru
* alebo updatovat danu poznamku jednoduchym kliknutim na danu poznamku
* Uzivatelovi sa ponuka triedenie medzi poznamkami
* v zmysle Titulku danej poznamke od A po Z
* od Z po A a napokon v zmysle Time kde sa zobrazi najstarsia poznamka ako prva
* obrazok refresh sluzi ako tlacitko na refreshnutie listu poznamok
*
*
*
* */
public class IntroScreenActivity extends AppCompatActivity implements NotesListener {


    /**
    * REQUEST CODE su pre rozoznanie daneho intentu ktory sa ma vykonat
    * kedze pouzim vacsinou startActivityForResult
    *
    * */
    private static final int REQUEST_CODE_ADD_NOTE = 1;
    private static final int REQUEST_CODE_UPDATE_NOTE = 2;
    private static final int REQUEST_CODE_SHOW_NOTES = 3;
    private static final int REQUEST_CODE_SHOW_NOTES_AZ = 4;
    private static final int REQUEST_CODE_SHOW_NOTES_ZA = 5;
    private static final int REQUEST_CODE_SHOW_NOTES_TIME = 6;
    private static final int REQUEST_CODE_REFRESH_NOTES = 7;

    //recycler view
    private RecyclerView notesRecycler;
    private List<Note> notesList;
    private NoteAdapter notesAdapter;

    //pozicia kliknutej poznamky
    private int noteClickedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_screen);

        //imageview ktory je na pridanie novej poznamky
        ImageView addNote = findViewById(R.id.addNoteImage);

        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getApplicationContext(), NewNoteActivity.class),REQUEST_CODE_ADD_NOTE);
            }
        });
        //uvodne nastavenie recycler Viewera
        initializeRecyclerView();
        getNotes(REQUEST_CODE_SHOW_NOTES,false);
        //umoznenie sortovania/filtrovania
        filtering();
        //umoznenie vyhladavania poznamok
        initializeSearcher();

    }
    /**
    * Metoda initializeRecyclerView sluzi na uvodne nastavenie recycler view
    * vytvori sa arraylist poznamok
    * nastavi sa adapter pre recycler view
    * */
    private void initializeRecyclerView() {
        notesRecycler = findViewById(R.id.recyclerView);

        notesRecycler.setLayoutManager(new GridLayoutManager(this,GridLayoutManager.VERTICAL));
        notesList = new ArrayList<>();
        notesAdapter = new NoteAdapter(notesList,this);
        notesRecycler.setAdapter(notesAdapter);

    }

    /**
    * uvedenie vyhladavania poznamok
    * onTextChanged zastavi sa casovac pre vyhladavanie
    * afterTextChanged ak je v liste aspon jedna poznamka tak zacne prehladavat
    * */
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

    /**
    * metoda filtering sluzi na sortovanie danych poznamok
    * */
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
/**
* metoda getNotes dostava do parametra request kod a ci nahodou nieje poznamka prave v stave deletovania
* vlozi sa asyncTask kde sa z databazy vytiahnu data.
* V onPostExecute sa podla request kodu uskutocnia dane algoritmy a potrebne funkcie
* */
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
                    //prida vsetky poznamky na zobrazenie
                    notesList.addAll(notes);
                    notesAdapter.notifyItemRangeChanged(0,notesList.size());
                    //notesAdapter.notifyDataSetChanged();
                } else if (reqCode == REQUEST_CODE_ADD_NOTE) {
                    //ak sa prave vytvori nova poznamka tak sa prida na zaciatok ako najnovsia
                    //upozorni sa adapteru ze sa pridala nova poznamka a recycler scrollne navrch
                    notesList.add(0,notes.get(0));
                    notesAdapter.notifyItemInserted(0);
                    notesRecycler.smoothScrollToPosition(0);

                } else if (reqCode == REQUEST_CODE_UPDATE_NOTE) {
                    //pri delete alebo update sa na zaciatku vymaze dana poznamka z listu
                    notesList.remove(noteClickedPosition);
                    if(isDeleted) {
                        //ak je to teda deletovanie tak upozorni adapter na zmenu
                        notesAdapter.notifyItemRemoved(noteClickedPosition);
                    } else {
                        //ak update tak ju znovu addne ale uz prepisanu
                        notesList.add(noteClickedPosition,notes.get(noteClickedPosition));
                        notesAdapter.notifyItemRangeChanged(0,notesList.size());

                    }
                } else if(reqCode == REQUEST_CODE_SHOW_NOTES_AZ) {
                    //vyprazdni sa list a naplni sa uz vysortovanym
                    notesList.clear();
                    Collections.sort(notes,MyNotesComparators.compareAZ);
                    notesList.addAll(notes);
                    notesAdapter.notifyItemRangeChanged(0,notesList.size());
                    notesRecycler.smoothScrollToPosition(0);
                  //  notesAdapter.notifyDataSetChanged();



                } else if(reqCode == REQUEST_CODE_SHOW_NOTES_ZA) {
                    //vyprazdni sa list a naplni sa uz vysortovanym
                    notesList.clear();
                    Collections.sort(notes,MyNotesComparators.compareZA);
                    notesList.addAll(notes);
                    notesAdapter.notifyItemRangeChanged(0,notesList.size());
                    notesRecycler.smoothScrollToPosition(0);
                    //notesAdapter.notifyDataSetChanged();

                } else if(reqCode == REQUEST_CODE_SHOW_NOTES_TIME) {
                    //vyprazdni sa list a naplni sa uz vysortovanym
                    notesList.clear();
                    Collections.sort(notes,MyNotesComparators.compareTime);
                    notesList.addAll(notes);
                    notesAdapter.notifyItemRangeChanged(0,notesList.size());
                    notesRecycler.smoothScrollToPosition(0);
                    //notesAdapter.notifyDataSetChanged();

                } else if(reqCode == REQUEST_CODE_REFRESH_NOTES) {
                    //vyprazdni sa list a naplni sa odznova ... jednoduchy refresh
                    notesList.clear();
                    notesList.addAll(notes);
                    notesAdapter.notifyItemRangeChanged(0,notesList.size());
                    notesRecycler.smoothScrollToPosition(0);

                    //notesAdapter.notifyDataSetChanged();
                }

            }
        }
        new GetNoteTask().execute();
    }

    /**
    * onActivityResult je metoda kde sa zobere request kod a spracuje sa v tomto konkretne pridanie poznamky a updatovanie
    * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getNotes(REQUEST_CODE_ADD_NOTE,false);
        }
        else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                getNotes(REQUEST_CODE_UPDATE_NOTE,data.getBooleanExtra("isNoteDeleted",false));
            }
        }
    }


    /**
     * Vlastna metoda onNoteClicked ktora je zdedena z interface NotesListener
     * kde sa zoberie momentalna pozicia poznamky a chysta sa na otvorenie poznamky
     * Request kod je v zmysle otvorenia cize pravdepodobne sa bude updatovat poznamka alebo minimalne prezerat
     */

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent openNote = new Intent(getApplicationContext(),NewNoteActivity.class);
        openNote.putExtra("isViewOrUpdate", true);
        openNote.putExtra("note",note);
        startActivityForResult(openNote,REQUEST_CODE_UPDATE_NOTE);
    }



}
