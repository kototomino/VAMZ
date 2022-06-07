package com.example.semestralkavamz.database;
import com.example.semestralkavamz.data.*;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;



import java.util.List;

@Dao
public interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY id DESC")
    List<Note> notesList();

    @Query("SELECT * FROM notes ORDER BY title ASC")
    List<Note> notesAZList();

    @Query("SELECT * FROM notes ORDER BY title ASC")
    List<Note> notesZAList();

    @Query("SELECT * FROM notes ORDER BY time DESC")
    List<Note> notesTimeList();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(Note note);

    @Delete
    void deleteNote(Note note);
}
