package com.example.semestralkavamz.activities;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;



import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Bundle;
import android.os.Environment;
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


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Locale;
/**
 * @author TOMAS KOTRIK 2022
 * NewNoteActivity je aktivita kde sa vytvara nova poznamka
 * zlozena je z Titulku kde ide nadpis nasledne z obsahu poznamky a datumu ...
 * datum sa nastavuje automaticky podla momentalneho datumu.
 * Ked si uzivatel scrollne nizsie tak si moze svoju poznamku customizovat ci uz fontom alebo
 * pridania obrazka z galerie alebo aj priamo z kamery ak je to priamo z kamery tak sa nasledne uklada obrazok
 * do subora com.example.semestralkavamz ktory sa vytvori.
 * Taktiez je mozne si nastavit farbu poznamky ktora bude potom v IntroScreenActivite.
 *
 * */
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
    private final static int REQUEST_CODE_CAPTURE_IMAGE = 3;
    private final static int REQUEST_CODE_WRITE_STORAGE = 4;
    private Note availableNote;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);
        setStuff();
        initializeCustomDrawer();
        handleOpenedNote();
        deleteNote();



    }
    /**
     * Zakladne inicializovanie vnutra poznamky
     * findViewById ...
     * */
    private void setStuff() {
        imageBack = findViewById(R.id.backNavigation);
        imageBack.setOnClickListener(view -> onBackPressed());

        title = findViewById(R.id.title);
        inputNote = findViewById(R.id.inputNote);
        date = findViewById(R.id.dateTime);
        addNote = findViewById(R.id.submitNote);
        imageNote = findViewById(R.id.imageNote);
        findViewById(R.id.removeImage).setOnClickListener(view -> {
            imageNote.setImageBitmap(null);
            imageNote.setVisibility(View.GONE);
            findViewById(R.id.removeImage).setVisibility(View.GONE);
            selectedImagePath = "";
        });
        addNote.setOnClickListener(view -> saveNote());
        date.setText(new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()).format(new Date()));
    }

    /**
     * setViewOrUpdateNote je metoda na nastavenie danych fieldov ktore boli zapisane do poznamky a nasledne zobrazenie
     * ak bol pridany obrazok tak sa umozni tento obrazok vymazat
     * */
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
    /**
     * Ak sa dostalo z vnutorneho intentu aj boolean parameter true tak to znamena ze sa poznamka neotvori lebo bola vymazana
     * inak sa otvori a prebehne metoda setViewOrUpdateNote
     * */
    private void handleOpenedNote() {
        if (getIntent().getBooleanExtra("isViewOrUpdate",false)) {
            availableNote = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }
    }

    /**
     * saveNote metoda sluzi na ulozenie poznamky a poslanie do Room Databazy
     * nastavia sa potrebne parametre ktora poznamka dostala od uzivatela
     * ak uzivatel nezada Titulok poznamky tak mu aplikacia neumozni ist dalej
     * */
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
        String message = "Note: " + note.getTitle().toUpperCase() + " created !";

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
        Toast.makeText(getApplicationContext(), "Note: " + note.getTitle().toUpperCase() + " created !", Toast.LENGTH_LONG).show();
        makeNotificationADDNote(note,message);
    }


    /**
     * initializeCustomDrawer je metoda kde sa nastavia vsetky potrebne parametre pre customizovanie poznamky
     * cize farba font a pridany obrazok.
     * Ked si uzivatel voli farby alebo fonty tak sa zobrazuje aj ukazatel aku farbu alebo font si prave vybral
     * */
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
/*
 * Uzivatelovi je pridane povolenie o vstupe do galerie a vybratie z nej
 *
 * */
        drawerLayout.findViewById(R.id.addImage).setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)!=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(NewNoteActivity.this,
                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_CODE_STORAGE_PERMISSION);
            }
            else {
                selectImage();
            }
        });
        drawerLayout.findViewById(R.id.captureImage).setOnClickListener(view -> checkStoragePermission());
    }
    /**
     * Vybratie z galerie obrazkov
     * */
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager())!= null) {
            startActivityForResult(intent,REQUEST_CODE_SELECT_IMAGE);
        }

    }

    /**
     * Vytvorenie obrazku z pouzitej kamery
     * kod zo stranky developer.android.com
     * */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        selectedImagePath = image.getAbsolutePath();
        return image;
    }

    /**
     * Vytvorenie notifikacie po pridani poznamky
     * do parametru poznamka a sprava ktora sa ma zobrazit v notifikacii
     * */
    private void makeNotificationADDNote(Note note,String message) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //vytvorenie jedinecneho channelu pre notifikaciu
            NotificationChannel notificationChannel = new NotificationChannel("ADDNOTE","ADDNOTE", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(getApplicationContext(),"ADDNOTE");
        //nadpis notifikacie
        notifBuilder.setContentTitle("KeepInMind");
        //sprava vo vnutri notifikacie
        notifBuilder.setContentText(message);
        //ikonka
        notifBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
        //auto zrusenie
        notifBuilder.setAutoCancel(true);
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getApplicationContext());
        //zobrazenie notifikacie
        managerCompat.notify(1, notifBuilder.build());
    }

/**
 * Uskutocnenie povolenia uzivatela na fotenie alebo vyberanie z galerie
 * */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CODE_STORAGE_PERMISSION){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                selectImage();

            }
            else{
                Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
        if(requestCode == REQUEST_CODE_WRITE_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Permission is granted
                openCameraToCaptureImage();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * openCameraToCaptureImage sluzi na zaznamenanie fotky z kamery pouzivatela
     * kod taktiez pomocka z developer.android.com
     * */
    private void openCameraToCaptureImage() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.semestralkavamz",
                        photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_CODE_CAPTURE_IMAGE);
            }
        }
    }
/**
 * Kontrolovanie povolenia pre zapisovanie obrazka do suboru
 *
 * */
    private void checkStoragePermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(NewNoteActivity.this,
                //change permission to WRITE_STORAGE
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(NewNoteActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                new AlertDialog.Builder(NewNoteActivity.this)
                        .setTitle("Permission Required")
                        .setMessage("Storage permission is required to save image")
                        .setPositiveButton("ALLOW", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //on click Allow we need request again
                                dialogInterface.cancel();
                                ActivityCompat.requestPermissions(NewNoteActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        REQUEST_CODE_WRITE_STORAGE);
                            }
                        }).setNegativeButton("DENIED", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }).show();
            } else {
                ActivityCompat.requestPermissions(NewNoteActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_WRITE_STORAGE);

            }
        } else {

            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            openCameraToCaptureImage();

        }
    }
    /**
     * Nasledne pridanie obrazka do suboru
     * */
    private void galleryAddPic() {

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File imageFile = new File(selectedImagePath);
        Uri contentUri = Uri.fromFile(imageFile);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);

    }
/**
 * Zmensenie obrazka aby nezaberal tolko miesta riesenie tvorene v zmysle zmensenia bitmapy
 * */
    private void compressImage(Uri imageUri) {
        try{
            InputStream imageStream = getContentResolver().openInputStream(imageUri);
            Bitmap imageBitmap = BitmapFactory.decodeStream(imageStream);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG,50,stream);

            try {
                stream.close();

            } catch (IOException e) {

                e.printStackTrace();
            }

        }catch (IOException e) {
                e.printStackTrace();
        }
    }

/**
 * onActivityResult ak je request kod na snimanie kamery tak sa bude snimat kamera
 * ak je request kod na vybratie obrazka z galerie tak sa vybera z galerie
 * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CAPTURE_IMAGE && resultCode == RESULT_OK) {


            File fileImage = new File(selectedImagePath);
            Uri contentUri = Uri.fromFile(fileImage);


            compressImage(contentUri);
            galleryAddPic();
            //nastavenie obrazka aby bol zobrazeny uzivatelovi
            imageNote.setImageURI(contentUri);
            imageNote.setVisibility(View.VISIBLE);

        }if(requestCode == REQUEST_CODE_SELECT_IMAGE&& resultCode == RESULT_OK) {

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
/**
 * getPathFromUri vrati cestu k obrazku aby som ho vedel nasledne zobrazit
 * */
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

    /**
     * deleteNote sluzi na vymazanie poznamky z databazy a celkovo z listu poznamok
     * do intentu delete sa vlozi boolean parameter true aby sa vedelo ze sa ide mazat poznamka
     * */
    public void deleteNote() {

        findViewById(R.id.removeNote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


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
                        Intent delete = new Intent();
                        delete.putExtra("isNoteDeleted",true);
                        setResult(RESULT_OK,delete);
                        finish();
                    }
                }
                new DeleteNoteTask().execute();
                Toast.makeText(getApplicationContext(), "Note: " + availableNote.getTitle().toUpperCase() + " deleted !", Toast.LENGTH_LONG).show();

            }
        });
    }
}