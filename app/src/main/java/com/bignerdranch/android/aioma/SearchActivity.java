package com.bignerdranch.android.aioma;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    RecyclerView mResultList;
    FirebaseDatabase mDatabase;
    DatabaseReference mAllMerchantsReference;

    ArrayList<Merchants> list;
    SearchView mSearchView;


    private AutoCompleteTextView mTextSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setElevation(0);
        actionBar.setTitle("");
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#5F5DD8")));


        mResultList = findViewById(R.id.search_result);
        mResultList.setHasFixedSize(true);
        mResultList.setLayoutManager(new LinearLayoutManager(this));
        mResultList.setOnTouchListener(new OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                View view = v;
                hideKeyboard(view);
                return false;
            }

        });


        mSearchView = (SearchView) findViewById(R.id.searchBar);
        ImageView searchIcon = mSearchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        searchIcon.setColorFilter(getResources().getColor(R.color.grey),
                android.graphics.PorterDuff.Mode.SRC_IN);

        mSearchView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mSearchView.setIconified(false);
            }
        });

        mDatabase = FirebaseDatabase.getInstance();
        mAllMerchantsReference = FirebaseDatabase.getInstance().getReference("merchantUsers");

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAllMerchantsReference != null) {

            mAllMerchantsReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                    if (snapshot.exists()) {
                        list = new ArrayList<>();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            list.add(ds.getValue(Merchants.class));
                        }
                        SearchAdapterClass searchAdapterClass = new SearchAdapterClass(list);
                        mResultList.setAdapter(searchAdapterClass);
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Toast.makeText(SearchActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (mSearchView != null){
            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    search(s);

                    return true;
                }
            });

        }

    }

    //search function
    private void search(String str){
        ArrayList<Merchants> merchantsList = new ArrayList<>();
        for(Merchants object : list){
            if(object.getMerchantName().toLowerCase().contains(str.toLowerCase())){
                merchantsList.add(object);
            }
            else if(object.getType().toLowerCase().contains(str.toLowerCase())){
                merchantsList.add(object);
            }
        }
        SearchAdapterClass searchAdapterClass = new SearchAdapterClass(merchantsList);
        mResultList.setAdapter(searchAdapterClass);

    }

    //to hide keyboard while scrolling
    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);

    }


}