package com.example.semestralkavamz.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import com.example.semestralkavamz.R;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();






    }
    private void initialize() {
        ArrayList<Integer> images = new ArrayList<>();
        ImageView image = findViewById(R.id.randomImage);
        Random rnd = new Random();
        images.add(R.drawable.image1);
        images.add(R.drawable.image2);
        images.add(R.drawable.image3);
        images.add(R.drawable.image4);
        images.add(R.drawable.image5);
        images.add(R.drawable.image6);
        images.add(R.drawable.image7);
        images.add(R.drawable.image8);
        images.add(R.drawable.image9);
        images.add(R.drawable.image10);
        images.add(R.drawable.image11);
        images.add(R.drawable.image12);
        images.add(R.drawable.image13);
        images.add(R.drawable.image14);
        images.add(R.drawable.image15);
        images.add(R.drawable.image16);
        images.add(R.drawable.image17);
        images.add(R.drawable.image18);
        images.add(R.drawable.image19);
        images.add(R.drawable.image20);
        image.setImageResource(images.get(rnd.nextInt(images.size())));
        image.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), IntroScreenActivity.class)));
        }

    }
