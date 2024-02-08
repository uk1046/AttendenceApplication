package com.example.attendenceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
FloatingActionButton fadd;
RecyclerView recycle;
ClassAdapter classadapter;
RecyclerView.LayoutManager layoutManager;
ArrayList<classItem> classItems = new ArrayList<>();
Toolbar toolbar;
EditText class_edt;
EditText subject_edt;
DbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DbHelper(this);

        fadd = (FloatingActionButton) findViewById(R.id.add);
        fadd.setOnClickListener( v-> showDialog());

        loadData();

        recycle = findViewById(R.id.recycler);
        recycle.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recycle.setLayoutManager(layoutManager);
        classadapter = new ClassAdapter(this, classItems);
        recycle.setAdapter(classadapter);
        classadapter.setOnItemClickListener(position -> gotoItemActivity(position));
        setToolBar();


    }

    private void loadData() {

        Cursor cursor = dbHelper.getClassTable();

        classItems.clear();
        while(cursor.moveToNext()){
            @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(DbHelper.C_ID));
            @SuppressLint("Range") String className = cursor.getString(cursor.getColumnIndex(DbHelper.CLASS_NAME_KEY));
            @SuppressLint("Range") String subjectName = cursor.getString(cursor.getColumnIndex(DbHelper.SUBJECT_NAME_KEY));

            classItems.add(new classItem(id,className,subjectName));
        }



    }


    private void setToolBar() {
        toolbar = findViewById(R.id.toolbar);
        TextView title = toolbar.findViewById(R.id.title_toolbar);
        TextView subtitle = toolbar.findViewById(R.id.subtitle_toolbar);
        ImageButton back = toolbar.findViewById(R.id.back);
        ImageButton save = toolbar.findViewById(R.id.save);

        title.setText("AttendenceLite");
        subtitle.setVisibility(View.GONE);
        back.setVisibility(View.INVISIBLE);
        save.setVisibility(View.INVISIBLE);
    }

    private void gotoItemActivity(int position) {
        Intent intent = new Intent(this, StudentActivity.class);

        intent.putExtra("className", classItems.get(position).getClassname());
        intent.putExtra("subjectName", classItems.get(position).getSubjectName());
        intent.putExtra("position",position);
        intent.putExtra("cid",classItems.get(position).getCid());
        startActivity(intent);

    }

    private void showDialog(){
        MyDialog dialog = new MyDialog();
        dialog.show(getSupportFragmentManager() , MyDialog.CLASS_ADD_DIALOG);
        dialog.setListener((classname,subjectname)->addClass(classname,subjectname));
    }
    private void addClass(String classname, String subjectname) {
        long cid = dbHelper.addClass(classname, subjectname);
        classItem classItem = new classItem(cid, classname, subjectname); // Include cid when creating new classItem
        classItems.add(classItem);
        classadapter.notifyDataSetChanged(); // Notify adapter after adding new class
    }


    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case 0:
                showUpdateDialog(item.getGroupId());
            break;

            case 1:
                deleteClass(item.getGroupId());

        }
        return super.onContextItemSelected(item);
    }

    private void showUpdateDialog(int position) {
        MyDialog dialog = new MyDialog();
        dialog.show(getSupportFragmentManager(),MyDialog.CLASS_UPDATE_DIALOG);
        dialog.setListener((className, subjectName)->updateClass(position, className, subjectName));

    }

    private void updateClass(int position, String className, String subjectName){
        dbHelper.updateClass(classItems.get(position).getCid(),className,subjectName);
        classItems.get(position).setClassname(className);
        classItems.get(position).setSubjectName(subjectName);
        classadapter.notifyItemChanged(position);
    }

    private void deleteClass(int position) {
        dbHelper.deleteClass(classItems.get(position).getCid());
        classItems.remove(position);
        classadapter.notifyItemRemoved(position);
    }
}