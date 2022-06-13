package com.example.semestralkavamz.database;
import com.example.semestralkavamz.data.*;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;



/**
 * NoteDao je DAO od room databazy kde je jednoduchy query select pre list poznamok kde sa vybere vsetko
 * a insert pre vlozenie poznamky , delete pre odstranenie poznamky
 * */

@SuppressWarnings("AndroidUnresolvedRoomSqlReference")
@Dao
public interface NoteDao {


    @Query("SELECT * FROM notes ORDER BY id DESC")
    List<Note> notesList();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(Note note);

    @Delete
    void deleteNote(Note note);
}
