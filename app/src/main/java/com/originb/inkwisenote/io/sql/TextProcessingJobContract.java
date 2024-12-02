package com.originb.inkwisenote.io.sql;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;
import com.originb.inkwisenote.modules.backgroundjobs.data.TextProcessingJobStatus;

public class TextProcessingJobContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private TextProcessingJobContract() {
    }

    /* Inner class that defines the table contents */
    protected static class TextProcessingJobEntry implements BaseColumns {
        public static final String TABLE_NAME = "text_processing_jobs";
        public static final String COLUMN_NAME_NOTE_ID = "note_id";
        public static final String COLUMN_NAME_STAGE = "stage";
    }

    public static class TextProcessingJobDbHelper extends InkwiseNoteDbHelper {

        public TextProcessingJobDbHelper(Context context) {
            super(context, DATABASE_NAME, DATABASE_VERSION);
        }

        protected String getSqlCreateQuery() {
            return "CREATE TABLE " + TextProcessingJobEntry.TABLE_NAME + "(" +
                    TextProcessingJobEntry._ID + "," +
                    TextProcessingJobEntry.COLUMN_NAME_NOTE_ID + "," +
                    TextProcessingJobEntry.COLUMN_NAME_STAGE
                    + ")";
        }

        protected String getSqlDropQuery() {
            return "DROP TABLE IF EXISTS " + TextProcessingJobEntry.TABLE_NAME;
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(getSqlDropQuery());
            onCreate(db);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    public static class TextProcessingDbQueries {
        public static TextProcessingJobStatus readFirstNoteJobStatus(TextProcessingJobDbHelper textProcessingJobDbHelper) {
            SQLiteDatabase db = textProcessingJobDbHelper.getReadableDatabase();

            // Define a projection that specifies which columns from the database
            // you will actually use after this query.
            String[] projection = {
                    TextProcessingJobEntry._ID,
                    TextProcessingJobEntry.COLUMN_NAME_NOTE_ID,
                    TextProcessingJobEntry.COLUMN_NAME_STAGE
            };

            // How you want the results sorted in the resulting Cursor
            String sortOrder =
                    TextProcessingJobEntry.COLUMN_NAME_NOTE_ID + " DESC";

            Cursor cursor = db.query(
                    TextProcessingJobEntry.TABLE_NAME,   // The table to query
                    projection,                          // The array of columns to return (pass null to get all)
                    null,                                // No where clause
                    null,                                // No where clause arguments
                    null,                                // Don't group the rows
                    null,                                // Don't filter by row groups
                    sortOrder                            // The sort order with LIMIT
            );

            TextProcessingJobStatus textProcessingJobStatus = null;
            while (cursor.moveToNext()) {
                textProcessingJobStatus = new TextProcessingJobStatus(cursor.getLong(
                        cursor.getColumnIndexOrThrow(TextProcessingJobEntry.COLUMN_NAME_NOTE_ID)),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(TextProcessingJobEntry.COLUMN_NAME_STAGE)));
            }
            cursor.close();
            Log.d("TextProcessingJob", "Loaded text jobs");
            return textProcessingJobStatus;
        }
    }
}

