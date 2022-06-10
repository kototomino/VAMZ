package com.example.semestralkavamz.interfaces;

import com.example.semestralkavamz.data.Note;
/**
 * @author TOMAS KOTRIK 2022
 * NotesListener je moj interface kde vlastne sa riesi len metoda
 * ked sa klikne na danu poznamku nech sa vie aplikacia spravat
 * */
public interface NotesListener {
    void onNoteClicked(Note note , int position);
}
