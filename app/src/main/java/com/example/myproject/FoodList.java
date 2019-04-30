package com.example.myproject;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.myproject.Interface.ItemClickListener;
import com.example.myproject.Model.Food;
import com.example.myproject.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.core.Tag;
import com.squareup.picasso.Picasso;

public class FoodList extends AppCompatActivity {

    private static final String TAG = "FoodList";

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference foodList;
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;
    String categoryId = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_food_list);

        //Firebase
        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Foods");

        recyclerView = (RecyclerView)findViewById(R.id.recycler_food);
        
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        Log.d(TAG, "onCreate: ");
        //getIntent
        if (getIntent() != null)
            categoryId = getIntent().getStringExtra("CategoryID");
        Log.d(TAG, "onCreate: " + categoryId);
        if (!categoryId.isEmpty() && categoryId != null)
        {
            loadListFood(categoryId);
        }

    }
    //foodList.orderByChild("MenuId").equalTo(categoryId)
    private void loadListFood(String categoryId) {
        Log.d(TAG, "loadListFood: Load List Food");
        Log.d(TAG, "loadListFood: " + categoryId);

        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(Food.class,
                                                            R.layout.food_item,
                                                            FoodViewHolder.class,
                                                            foodList) {
            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, Food model, int position) {
                Log.d(TAG, "populateViewHolder: ");
                viewHolder.food_Name.setText(model.getName());
                Log.d(TAG, "populateViewHolder: " + model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.food_image);

                final Food local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Start new Activity
                        Intent foodDetail = new Intent(FoodList.this, FoodDetail.class);
                        foodDetail.putExtra("FoodId", adapter.getRef(position).getKey());
                        startActivity(foodDetail);
                    }
                });
            }
        };
        //Set Adapter
        recyclerView.setAdapter(adapter);

    }
}
