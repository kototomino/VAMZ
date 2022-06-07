package com.example.semestralkavamz.activities;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.content.CursorLoader;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.semestralkavamz.R;
import com.example.semestralkavamz.data.Note;
import com.example.semestralkavamz.database.NotesDatabase;


import java.io.InputStream;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Locale;

public class NewNoteActivity extends AppCompatActivity {

    private ImageView imageBack;
    private EditText title;
    private EditText inputNote;
    private TextView date;
    private ImageView addNote;
    private String color;
    private String selectedImagePath;
    private ImageView imageNote;
    private final static int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private final static int REQUEST_CODE_SELECT_IMAGE = 2;

    private Note availableNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);


        imageBack = findViewById(R.id.backNavigation);
        imageBack.setOnClickListener(view -> onBackPressed());

        title = findViewById(R.id.title);
        inputNote = findViewById(R.id.inputNote);
        date = findViewById(R.id.dateTime);
        addNote = findViewById(R.id.submitNote);
        imageNote = findViewById(R.id.imageNote);
        setTime();
        setStuff();
        findViewById(R.id.removeImage).setOnClickListener(view -> {
            imageNote.setImageBitmap(null);
            imageNote.setVisibility(View.GONE);
            findViewById(R.id.removeImage).setVisibility(View.GONE);
            selectedImagePath = "";
        });
        initializeCustomDrawer();
        handleOpenedNote();
        deleteNote();



    }
    private void setStuff() {
        addNote.setOnClickListener(view -> saveNote());
    }
    private void setTime() {
        date.setText(new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()).format(new Date()));
    }
    private void setViewOrUpdateNote() {
        title.setText(availableNote.getTitle());
        inputNote.setText(availableNote.getInputNote());
        date.setText(availableNote.getTime());


        if(availableNote.getImagePath() != null && !availableNote.getImagePath().trim().isEmpty()) {
            imageNote.setImageBitmap(BitmapFactory.decodeFile(availableNote.getImagePath()));
            imageNote.setVisibility(View.VISIBLE);
            findViewById(R.id.removeImage).setVisibility(View.VISIBLE);
            selectedImagePath = availableNote.getImagePath();
            Log.d("Mess","file path = " + selectedImagePath);
        } else {
            imageNote.setVisibility(View.GONE);
        }
    }
    private void handleOpenedNote() {
        if (getIntent().getBooleanExtra("isViewOrUpdate",false)) {
            availableNote = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }
    }
    private void saveNote() {
        if(title.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Title is empty ... please insert title before saving note!",Toast.LENGTH_LONG).show();
            return;
        }
        Note note = new Note();
        note.setTitle(title.getText().toString());
        note.setInputNote(inputNote.getText().toString());
        note.setTime(date.getText().toString());
        note.setColor(color);
        note.setImagePath(selectedImagePath);

        if(availableNote != null) {
            note.setId(availableNote.getId());
        }
        @SuppressLint("StaticFieldLeak")
        class SaveNote extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                NotesDatabase.getDatabase(getApplicationContext()).noteDao().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent intent = new Intent();
                setResult(RESULT_OK,intent);
                finish();
            }
        }
        new SaveNote().execute();
    }
    private void initializeCustomDrawer() {

        LinearLayout drawerLayout = findViewById(R.id.linearLayoutOfCustom);


        final ImageView blue = drawerLayout.findViewById(R.id.colorBlue);
        final ImageView red = drawerLayout.findViewById(R.id.colorRed);
        final ImageView brown = drawerLayout.findViewById(R.id.colorBrown);
        final ImageView orange = drawerLayout.findViewById(R.id.colorOrange);
        final ImageView yellow = drawerLayout.findViewById(R.id.colorYellow);
        final ImageView purple = drawerLayout.findViewById(R.id.colorPurple);
        final ImageView dark = drawerLayout.findViewById(R.id.colorDark);

        final TextView fontBangers = drawerLayout.findViewById(R.id.font_bangers);
        final TextView fontAmita = drawerLayout.findViewById(R.id.font_amita);
        final TextView fontUbuntu = drawerLayout.findViewById(R.id.font_ubuntu);
        final TextView fontGotu = drawerLayout.findViewById(R.id.font_gotu);
        final TextView fontKrona = drawerLayout.findViewById(R.id.font_krona);
        final TextView fontRozha = drawerLayout.findViewById(R.id.font_rozha);


        drawerLayout.findViewById(R.id.viewColorBlue).setOnClickListener(view -> {
            color = "#304FFE";
            blue.setImageResource(R.drawable.ic_baseline_close_24);
            red.setImageResource(0);
            brown.setImageResource(0);
            orange.setImageResource(0);
            yellow.setImageResource(0);
            purple.setImageResource(0);
            dark.setImageResource(0);

        });
        drawerLayout.findViewById(R.id.viewColorRed).setOnClickListener(view -> {
            color = "#D50000";
            red.setImageResource(R.drawable.ic_baseline_close_24);
            blue.setImageResource(0);
            brown.setImageResource(0);
            orange.setImageResource(0);
            yellow.setImageResource(0);
            purple.setImageResource(0);
            dark.setImageResource(0);

        });
        drawerLayout.findViewById(R.id.viewColorBrown).setOnClickListener(view -> {
            color = "#6A2707";
            brown.setImageResource(R.drawable.ic_baseline_close_24);
            red.setImageResource(0);
            blue.setImageResource(0);
            orange.setImageResource(0);
            yellow.setImageResource(0);
            purple.setImageResource(0);
            dark.setImageResource(0);
        });
        drawerLayout.findViewById(R.id.viewColorOrange).setOnClickListener(view -> {
            color = "#FF6D00";
            orange.setImageResource(R.drawable.ic_baseline_close_24);
            red.setImageResource(0);
            brown.setImageResource(0);
            blue.setImageResource(0);
            yellow.setImageResource(0);
            purple.setImageResource(0);
            dark.setImageResource(0);
        });
        drawerLayout.findViewById(R.id.viewColorYellow).setOnClickListener(view -> {
            color = "#FFD600";
            yellow.setImageResource(R.drawable.ic_baseline_close_24);
            red.setImageResource(0);
            brown.setImageResource(0);
            orange.setImageResource(0);
            blue.setImageResource(0);
            purple.setImageResource(0);
            dark.setImageResource(0);
        });
        drawerLayout.findViewById(R.id.viewColorPurple).setOnClickListener(view -> {
            color = "#FF6200EE";
            purple.setImageResource(R.drawable.ic_baseline_close_24);
            red.setImageResource(0);
            brown.setImageResource(0);
            orange.setImageResource(0);
            yellow.setImageResource(0);
            blue.setImageResource(0);
            dark.setImageResource(0);
        });

        drawerLayout.findViewById(R.id.viewColorDark).setOnClickListener(view -> {
            color = "#292929";
            dark.setImageResource(R.drawable.ic_baseline_close_24);
            red.setImageResource(0);
            brown.setImageResource(0);
            orange.setImageResource(0);
            yellow.setImageResource(0);
            purple.setImageResource(0);
            blue.setImageResource(0);
        });

        //-----------------FONTS------------------------//
        drawerLayout.findViewById(R.id.font_bangers).setOnClickListener(view -> {
            fontBangers.setBackgroundResource(R.drawable.custom_color_selector);
            Typeface typeFace = fontBangers.getTypeface();
            fontAmita.setBackgroundResource(0);
            fontGotu.setBackgroundResource(0);
            fontUbuntu.setBackgroundResource(0);
            fontKrona.setBackgroundResource(0);
            fontRozha.setBackgroundResource(0);
            inputNote.setTypeface(typeFace);
        });
        drawerLayout.findViewById(R.id.font_amita).setOnClickListener(view -> {
            fontAmita.setBackgroundResource(R.drawable.custom_color_selector);
            Typeface typeFace = fontAmita.getTypeface();
            fontBangers.setBackgroundResource(0);
            fontGotu.setBackgroundResource(0);
            fontUbuntu.setBackgroundResource(0);
            fontKrona.setBackgroundResource(0);
            fontRozha.setBackgroundResource(0);
            inputNote.setTypeface(typeFace);
        });
        drawerLayout.findViewById(R.id.font_gotu).setOnClickListener(view -> {
            fontGotu.setBackgroundResource(R.drawable.custom_color_selector);
            Typeface typeFace = fontGotu.getTypeface();
            fontAmita.setBackgroundResource(0);
            fontBangers.setBackgroundResource(0);
            fontUbuntu.setBackgroundResource(0);
            fontKrona.setBackgroundResource(0);
            fontRozha.setBackgroundResource(0);
            inputNote.setTypeface(typeFace);
        });
        drawerLayout.findViewById(R.id.font_krona).setOnClickListener(view -> {
            fontKrona.setBackgroundResource(R.drawable.custom_color_selector);
            Typeface typeFace = fontKrona.getTypeface();
            fontAmita.setBackgroundResource(0);
            fontGotu.setBackgroundResource(0);
            fontUbuntu.setBackgroundResource(0);
            fontBangers.setBackgroundResource(0);
            fontRozha.setBackgroundResource(0);
            inputNote.setTypeface(typeFace);
        });
        drawerLayout.findViewById(R.id.font_ubuntu).setOnClickListener(view -> {
            fontUbuntu.setBackgroundResource(R.drawable.custom_color_selector);
            Typeface typeFace = fontUbuntu.getTypeface();
            fontAmita.setBackgroundResource(0);
            fontGotu.setBackgroundResource(0);
            fontBangers.setBackgroundResource(0);
            fontRozha.setBackgroundResource(0);
            fontKrona.setBackgroundResource(0);
            inputNote.setTypeface(typeFace);

        });
        drawerLayout.findViewById(R.id.font_rozha).setOnClickListener(view -> {
            fontRozha.setBackgroundResource(R.drawable.custom_color_selector);
            Typeface typeFace = fontRozha.getTypeface();
            fontAmita.setBackgroundResource(0);
            fontGotu.setBackgroundResource(0);
            fontBangers.setBackgroundResource(0);
            fontKrona.setBackgroundResource(0);
            inputNote.setTypeface(typeFace);
        });

        drawerLayout.findViewById(R.id.addImage).setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)!=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(NewNoteActivity.this,
                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_CODE_STORAGE_PERMISSION);
            }else {
                selectImage();
            }
        });
    }
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager())!= null) {
            startActivityForResult(intent,REQUEST_CODE_SELECT_IMAGE);
        }


    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                selectImage();
            }
            else{
                Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }


    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if(data != null) {
                Uri selectedImageUri = data.getData();
                if(selectedImageUri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        imageNote.setImageBitmap(bitmap);
                        imageNote.setVisibility(View.VISIBLE);

                        selectedImagePath = getPathFromUri(selectedImageUri);
                        findViewById(R.id.removeImage).setVisibility(View.VISIBLE);

                    }catch (Exception exception) {
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private String getPathFromUri(Uri content) {
        String filePath;
        Cursor cursor = getContentResolver().query(content,null,null,null,null);
        if (cursor == null) {
            filePath = content.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();

        }
        return filePath;
    }
    public void deleteNote() {
        findViewById(R.id.removeNote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("TAG","KLIKOL SI");
                @SuppressLint("StaticFieldLeak")
                class DeleteNoteTask extends AsyncTask<Void,Void,Void> {

                    @Override
                    protected Void doInBackground(Void... voids) {
                        NotesDatabase.getDatabase(getApplicationContext()).noteDao().deleteNote(availableNote);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void avoid) {
                        super.onPostExecute(avoid);
                        Intent intent = new Intent();
                        intent.putExtra("isNoteDeleted",true);
                        setResult(RESULT_OK,intent);
                        finish();
                    }
                }
                new DeleteNoteTask().execute();
            }
        });
    }
}