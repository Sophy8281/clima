package com.example.clima;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class ChangeCityController extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_city_layout);

        final EditText editTextField = findViewById(R.id.queryET);
        ImageButton backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());

        //Allows the app to get the city name enter by the user and pass it to the
        //Weather controller
        editTextField.setOnEditorActionListener((v, actionId, event) -> {

            String newCity = editTextField.getText().toString();
            Intent newCityIntent = new Intent(ChangeCityController.this, WeatherController.class);
            newCityIntent.putExtra("City", newCity);
            startActivity(newCityIntent);

            return false;
        });
    }
}