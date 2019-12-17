package ch.heigvd.iict.sym_labo4.abstractactivies;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;

/**
 * Project: Labo3
 * Created by fabien.dutoit on 01.11.2016
 * (C) 2016 - HEIG-VD, IICT
 */

public abstract class BaseTemplateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch(Exception e) { /* BEST EFFORT */ }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item != null && item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
