package com.example.semestralkavamz.Comparators;

import com.example.semestralkavamz.data.Note;

import java.util.Comparator;


public abstract class MyNotesComparators implements Comparable<Note> {


     public static Comparator<Note> compareAZ = new Comparator<Note>() {
         @Override
         public int compare(Note o1, Note o2) {
             return o1.getTitle().compareTo(o2.getTitle());
         }
     };

    public static Comparator<Note> compareZA = new Comparator<Note>() {
        @Override
        public int compare(Note o1, Note o2) {
            return o2.getTitle().compareTo(o1.getTitle());
        }
    };

    public static Comparator<Note> compareTime = new Comparator<Note>() {
        @Override
        public int compare(Note o1, Note o2) {
            return o1.getTime().compareTo(o2.getTime());
        }
    };

}
