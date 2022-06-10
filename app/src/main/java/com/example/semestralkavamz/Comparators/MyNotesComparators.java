package com.example.semestralkavamz.Comparators;

import com.example.semestralkavamz.data.Note;

import java.util.Comparator;

/**
 * @author TOMAS KOTRIK 2022
 * Vlastna trieda na komparovanie poznamok pomocou Comparable porovnavam jednotlive fieldy poznamok
 * a vraciam ich v poradi akom potrebujem cize nazov zostupne/vzostupne a datum zostupne
 * */
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
