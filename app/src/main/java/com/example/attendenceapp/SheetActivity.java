package com.example.attendenceapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class SheetActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_WRITE_STORAGE_PERMISSION = 1;

    ImageButton ib;
    TableLayout tableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sheet);

        ib = findViewById(R.id.add001);
        tableLayout = findViewById(R.id.tableLayout);

        showTable();

        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    generateAndDownloadPDF();
                } else {
                    requestPermission();
                }
            }
        });
    }

    private void showTable() {
        DbHelper dbHelper = new DbHelper(this);
        long[] idArray = getIntent().getLongArrayExtra("idArray");
        int[] rollArray = getIntent().getIntArrayExtra("rollArray");
        String[] nameArray = getIntent().getStringArrayExtra("nameArray");
        String month = getIntent().getStringExtra("month");

        assert month != null;
        int DAY_IN_MONTH = getDayInMonth(month);

        //row
        assert idArray != null;
        int rowSize = idArray.length + 1;
        TableRow[] rows = new TableRow[rowSize];
        TextView[] roll_tvs = new TextView[rowSize];
        TextView[] name_tvs = new TextView[rowSize];
        TextView[][] status_tvs = new TextView[rowSize][DAY_IN_MONTH + 1];

        // Initialize roll_tvs and name_tvs arrays
        for (int i = 0; i < rowSize; i++) {
            roll_tvs[i] = new TextView(this);
            name_tvs[i] = new TextView(this);

            // Initialize roll and name TextViews if index is within bounds
            if (i > 0 && i <= idArray.length) {
                assert rollArray != null;
                roll_tvs[i].setText(String.valueOf(rollArray[i - 1])); // Adjust index to start from 0
                assert nameArray != null;
                name_tvs[i].setText(nameArray[i - 1]); // Adjust index to start from 0
            }
        }

        //heading
        roll_tvs[0].setText("Roll");
        roll_tvs[0].setTypeface(roll_tvs[0].getTypeface(), Typeface.BOLD);
        name_tvs[0].setText("Name");
        name_tvs[0].setTypeface(name_tvs[0].getTypeface(), Typeface.BOLD);

        // Initialize status_tvs array
        for (int i = 0; i < rowSize; i++) {
            for (int j = 0; j <= DAY_IN_MONTH; j++) {
                status_tvs[i][j] = new TextView(this); // Initialize TextViews
            }
        }

        for (int i = 1; i <= DAY_IN_MONTH; i++) {
            status_tvs[0][i].setText(String.valueOf(i));
            status_tvs[0][i].setTypeface(status_tvs[0][i].getTypeface(), Typeface.BOLD);
        }

        for (int i = 1; i < rowSize; i++) {
            for (int j = 1; j <= DAY_IN_MONTH; j++) {
                String day = String.valueOf(j);
                if (day.length() == 1) day = "0" + day;
                String date = day + "." + month;
                String status = dbHelper.getStatus(idArray[i - 1], date);
                status_tvs[i][j].setText(status);
            }
        }

        for (int i = 0; i < rowSize; i++) {
            rows[i] = new TableRow(this);

            if (i % 2 == 0) {
                rows[i].setBackgroundColor(Color.parseColor("#EEEEEE"));
            } else {
                rows[i].setBackgroundColor(Color.parseColor("#E4E4E4"));
            }

            roll_tvs[i].setPadding(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5));
            name_tvs[i].setPadding(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5));

            rows[i].addView(roll_tvs[i]);

            for (int j = 0; j <= DAY_IN_MONTH; j++) {
                if (j > 0) {
                    // Add vertical line between columns
                    View verticalLine = new View(this);
                    TableRow.LayoutParams params = new TableRow.LayoutParams(dpToPx(1), TableRow.LayoutParams.MATCH_PARENT);
                    verticalLine.setLayoutParams(params);
                    verticalLine.setBackgroundColor(Color.BLACK);
                    rows[i].addView(verticalLine);
                }
                status_tvs[i][j].setPadding(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5));
                rows[i].addView(status_tvs[i][j]);
            }

            // Add horizontal line below each row
            View horizontalLine = new View(this);
            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, dpToPx(1));
            horizontalLine.setLayoutParams(params);
            horizontalLine.setBackgroundColor(Color.BLACK);
            tableLayout.addView(horizontalLine);

            tableLayout.addView(rows[i]);
        }
    }

    // Method to convert dp to pixels
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @SuppressLint("Range")
    private void generateAndDownloadPDF() {
        // Create a new PdfDocument
        PdfDocument pdfDocument = new PdfDocument();

        try {
            // Create a PageInfo
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(tableLayout.getWidth(), tableLayout.getHeight(), 1).create();

            // Start a page
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);

            // Draw the table layout on the page canvas
            Canvas canvas = page.getCanvas();

            // Draw class name, subject, and month as headings
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setTextSize(30); // Set heading font size
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

            int yPos = 50; // Initial Y-coordinate for heading

            DbHelper dbHelper = new DbHelper(this); // Initialize DbHelper with your context
            Cursor cursor = dbHelper.getClassNamesAndSubjects();

            String className, subjectName;
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    className = cursor.getString(cursor.getColumnIndex(DbHelper.CLASS_NAME_KEY));
                    subjectName = cursor.getString(cursor.getColumnIndex(DbHelper.SUBJECT_NAME_KEY));

                    // Draw class name
                    canvas.drawText("Class: " + className, 50, yPos, paint);
                    yPos += 50; // Increase Y-coordinate for subject name

                    // Draw subject name
                    canvas.drawText("Subject: " + subjectName, 50, yPos, paint);
                    yPos += 50; // Increase Y-coordinate for next data
                } while (cursor.moveToNext());

                cursor.close(); // Close the cursor when done
            }

            // Draw month as heading
            String month = getIntent().getStringExtra("month");
            String monthAndYear = month; // You can change the format as required
            paint.setTextSize(20); // Set font size for month and year
            yPos += 50; // Increase Y-coordinate for month and year
            canvas.drawText("Month: " + monthAndYear, 50, yPos, paint);

            // Add padding between headings and table
            yPos += 50; // Increase Y-coordinate for padding

            // Draw the table layout
            canvas.translate(0, yPos); // Move canvas down for padding
            tableLayout.draw(canvas);

            // Finish the page
            pdfDocument.finishPage(page);

            // Define the directory and file name for the PDF in the Documents directory
            File directory = new File(getExternalFilesDir(null), "AttendanceApp");
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    // Failed to create directory, fallback to internal storage
                    directory = getFilesDir();
                }
            }
            File file = new File(directory, "attendance_sheet.pdf");

            // Write the PDF content to the file
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "PDF saved to " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            // Close the PDF document in a finally block to ensure it gets closed regardless of any exceptions
            pdfDocument.close();
        }
    }



    private int getDayInMonth(String month) {
        int monthIndex = Integer.valueOf(month.substring(0, 2)) - 1; // Adjusted to get the correct month index
        int year = Integer.valueOf(month.substring(3));

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, monthIndex);
        calendar.set(Calendar.YEAR, year);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_STORAGE_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_WRITE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generateAndDownloadPDF();
            } else {
                Toast.makeText(this, "Permission denied. Cannot save PDF.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
