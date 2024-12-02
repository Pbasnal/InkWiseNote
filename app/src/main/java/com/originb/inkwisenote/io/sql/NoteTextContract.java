package com.originb.inkwisenote.io.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;
import com.originb.inkwisenote.data.notedata.NoteOcrText;

import java.util.ArrayList;
import java.util.List;

public final class NoteTextContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private NoteTextContract() {
    }

    /* Inner class that defines the table contents */
    public static class NoteTextEntry implements BaseColumns {
        public static final String TABLE_NAME = "note_text";
        public static final String COLUMN_NAME_EXTRACTED_TEXT = "extracted_text";
    }

    public static class NoteTextDbHelper extends InkwiseNoteDbHelper {

        public NoteTextDbHelper(Context context) {
            super(context, DATABASE_NAME, DATABASE_VERSION);
        }

        protected String getSqlCreateQuery() {
            return "CREATE TABLE " + NoteTextEntry.TABLE_NAME + "(" +
                    NoteTextEntry._ID + "," +
                    NoteTextEntry.COLUMN_NAME_EXTRACTED_TEXT + ")";
        }

        protected String getSqlDropQuery() {
            return "DROP TABLE IF EXISTS " + NoteTextEntry.TABLE_NAME;
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

    public static class NoteTextQueries {
        public static List<NoteOcrText> readTextFromDb(Long noteId, NoteTextDbHelper noteTextDbHelper) {
            SQLiteDatabase db = noteTextDbHelper.getReadableDatabase();

            // Define a projection that specifies which columns from the database
            // you will actually use after this query.
            String[] projection = {
                    BaseColumns._ID,
                    NoteTextContract.NoteTextEntry.COLUMN_NAME_EXTRACTED_TEXT
            };

            // Filter results WHERE "title" = 'My Title'
            String selection = NoteTextContract.NoteTextEntry._ID + " = ?";
            String[] selectionArgs = {String.valueOf(noteId)};

            // How you want the results sorted in the resulting Cursor
            String sortOrder =
                    NoteTextContract.NoteTextEntry._ID + " DESC";

            Cursor cursor = db.query(
                    NoteTextContract.NoteTextEntry.TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    selection,              // The columns for the WHERE clause
                    selectionArgs,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );

            List<NoteOcrText> noteOcrTexts = new ArrayList<>();
            while (cursor.moveToNext()) {
                noteOcrTexts.add(new NoteOcrText(cursor.getLong(
                        cursor.getColumnIndexOrThrow(NoteTextContract.NoteTextEntry._ID)),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(NoteTextEntry.COLUMN_NAME_EXTRACTED_TEXT))));
            }
            cursor.close();
            return noteOcrTexts;
        }

        public static List<Long> searchTextFromDb(String searchTerm, NoteTextDbHelper noteTextDbHelper) {
            SQLiteDatabase db = noteTextDbHelper.getReadableDatabase();

            // Define a projection that specifies which columns from the database
            // you will actually use after this query.
            String[] projection = {
                    BaseColumns._ID,
                    NoteTextContract.NoteTextEntry.COLUMN_NAME_EXTRACTED_TEXT
            };

            // Filter results WHERE "title" = 'My Title'
            String selection = NoteTextEntry.COLUMN_NAME_EXTRACTED_TEXT + " LIKE ?";
            String[] selectionArgs = {"%" + searchTerm + "%"};

            // How you want the results sorted in the resulting Cursor
            String sortOrder =
                    NoteTextContract.NoteTextEntry._ID + " DESC";

            Cursor cursor = db.query(
                    NoteTextContract.NoteTextEntry.TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    selection,              // The columns for the WHERE clause
                    selectionArgs,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );

            List<Long> itemIds = new ArrayList<>();
            while (cursor.moveToNext()) {
                long itemId = cursor.getLong(
                        cursor.getColumnIndexOrThrow(NoteTextContract.NoteTextEntry._ID));
                itemIds.add(itemId);
            }
            cursor.close();
            return itemIds;
        }

        public static void updateTextToDb(NoteOcrText noteOcrText, NoteTextDbHelper noteTextDbHelper) {
            SQLiteDatabase db = noteTextDbHelper.getWritableDatabase();

            // New value for one column
//            String noteText = noteText.getExtractedText();
            ContentValues values = new ContentValues();
            values.put(NoteTextContract.NoteTextEntry.COLUMN_NAME_EXTRACTED_TEXT, noteOcrText.getExtractedText());

            // Which row to update, based on the title
            String selection = NoteTextContract.NoteTextEntry._ID + " = ?";
            String[] selectionArgs = {noteOcrText.getNoteId().toString()};

            int count = db.update(
                    NoteTextContract.NoteTextEntry.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs);
        }

        public static void insertTextToDb(NoteOcrText noteOcrText, NoteTextDbHelper noteTextDbHelper) {
            // Gets the data repository in write mode
            SQLiteDatabase db = noteTextDbHelper.getWritableDatabase();

            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(NoteTextContract.NoteTextEntry._ID, String.valueOf(noteOcrText.getNoteId()));
            values.put(NoteTextContract.NoteTextEntry.COLUMN_NAME_EXTRACTED_TEXT, noteOcrText.getExtractedText());


            // Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(NoteTextContract.NoteTextEntry.TABLE_NAME, null, values);
        }

        public static void deleteNoteText(Long noteId, NoteTextDbHelper noteTextDbHelper) {
            // Get writable database
            SQLiteDatabase db = noteTextDbHelper.getWritableDatabase();

            // Define 'where' part of query
            String selection = NoteTextContract.NoteTextEntry._ID + " = ?";

            // Specify the arguments in placeholder order
            String[] selectionArgs = {String.valueOf(noteId)};

            // Issue SQL delete command
            int deletedRows = db.delete(NoteTextContract.NoteTextEntry.TABLE_NAME, selection, selectionArgs);

            // Log or handle the number of deleted rows if necessary
            Log.d("Delete", "Number of rows deleted: " + deletedRows);
        }
    }

}
