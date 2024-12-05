package com.originb.inkwisenote.io.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;
import com.originb.inkwisenote.data.admin.TermFrequencyEntry;

import java.util.*;

public class NoteTermFrequencyContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private NoteTermFrequencyContract() {
    }

    /* Inner class that defines the table contents */
    protected static class NoteTermFrequencyEntry implements BaseColumns {
        public static final String TABLE_NAME = "note_term_frequency";
        public static final String COLUMN_NAME_NOTE_ID = "note_id";
        public static final String COLUMN_NAME_TERM = "term";
        public static final String COLUMN_NAME_FQ_IN_DOC = "fq_in_doc";
    }

    public static class NoteTermFrequencyDbQueries extends InkwiseNoteDbHelper {

        public NoteTermFrequencyDbQueries(Context context) {
            super(context, DATABASE_NAME, DATABASE_VERSION);
        }

        public NoteTermFrequencyDbQueries(Context context, String dbPath) {
            super(context, dbPath != null ? dbPath : DATABASE_NAME, DATABASE_VERSION);
        }

        protected String getSqlCreateQuery() {
            return "CREATE TABLE " + NoteTermFrequencyEntry.TABLE_NAME + "(" +
                    NoteTermFrequencyEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    NoteTermFrequencyEntry.COLUMN_NAME_NOTE_ID + "," +
                    NoteTermFrequencyEntry.COLUMN_NAME_TERM + "," +
                    NoteTermFrequencyEntry.COLUMN_NAME_FQ_IN_DOC
                    + ")";
        }

        protected String getSqlDropQuery() {
            return "DROP TABLE IF EXISTS " + NoteTermFrequencyEntry.TABLE_NAME;
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

        public Map<String, Integer> readTermFrequenciesOfNote(Long noteIdToRead) {
            SQLiteDatabase db = getReadableDatabase();

            Cursor cursor = db.rawQuery("SELECT * FROM " + NoteTermFrequencyEntry.TABLE_NAME
                    + " where " + NoteTermFrequencyEntry.COLUMN_NAME_NOTE_ID + " = " + noteIdToRead, null);

            // <Term, Frequency>
            Map<String, Integer> termFrequencyMap = new HashMap<>();
            while (cursor.moveToNext()) {
                String term = cursor.getString(
                        cursor.getColumnIndexOrThrow(NoteTermFrequencyEntry.COLUMN_NAME_TERM));
                Integer frequency = cursor.getInt(
                        cursor.getColumnIndexOrThrow(NoteTermFrequencyEntry.COLUMN_NAME_FQ_IN_DOC));
                termFrequencyMap.putIfAbsent(term, frequency);

            }
            cursor.close();
            return termFrequencyMap;
        }

        public Map<String, Set<Long>> getNoteIdsForTerms(Set<String> terms) {
            SQLiteDatabase db = getReadableDatabase();

            // Convert list of terms to a comma-separated string for the SQL query
            StringBuilder placeholders = new StringBuilder();
            for (int i = 0; i < terms.size(); i++) {
                placeholders.append("?");
                if (i < terms.size() - 1) {
                    placeholders.append(",");
                }
            }

            // SQL query
            String sql = "SELECT term, note_id FROM " + NoteTermFrequencyEntry.TABLE_NAME +
                    " WHERE term IN (" + placeholders + ")";

            // Convert the List to an array
            String[] termsArray = terms.toArray(new String[0]);

            Map<String, Set<Long>> termRelatedNotes = new HashMap<>();
            // Execute the query
            Cursor cursor = db.rawQuery(sql, termsArray);
            try {
                while (cursor.moveToNext()) {
                    String term = cursor.getString(
                            cursor.getColumnIndexOrThrow(NoteTermFrequencyEntry.COLUMN_NAME_TERM));
                    Long noteId = cursor.getLong(
                            cursor.getColumnIndexOrThrow(NoteTermFrequencyEntry.COLUMN_NAME_NOTE_ID));

                    if (termRelatedNotes.containsKey(term)) {
                        termRelatedNotes.get(term).add(noteId);
                    } else {
                        Set<Long> noteIds = new HashSet<>();
                        noteIds.add(noteId);
                        termRelatedNotes.put(term, noteIds);
                    }
                }
            } catch (Exception ex) {
                Log.e("NoteTermFrequency", "Failed to get #docs in which terms occur", ex);
            } finally {
                cursor.close();
            }

            return termRelatedNotes;
        }

        public void insertTermFrequencieToDb(Long noteId, Map<String, Integer> termFrequencies) {
            // Gets the data repository in write mode
            SQLiteDatabase db = getWritableDatabase();

            // Create a new map of values, where column names are the keys

            try {
                // Start a transaction
                db.beginTransaction();

                for (Map.Entry<String, Integer> entry : termFrequencies.entrySet()) {
                    ContentValues values = new ContentValues();
                    values.put(NoteTermFrequencyEntry.COLUMN_NAME_NOTE_ID, noteId);
                    values.put(NoteTermFrequencyEntry.COLUMN_NAME_TERM, entry.getKey());
                    values.put(NoteTermFrequencyEntry.COLUMN_NAME_FQ_IN_DOC, entry.getValue());

                    // Insert the row
                    db.insert(NoteTermFrequencyEntry.TABLE_NAME, null, values);
                }

                // Mark the transaction as successful
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.e("NoteTermFrequencyContract", "Failed to insert term frequencies", ex);
            } finally {
                // End the transaction
                db.endTransaction();
            }
        }

        // Future TODO: find limit of number terms I can have in one query
        // DB size for which this query is performant
        //
        public Map<String, Integer> getTermOccurrences(Set<String> terms) {
            SQLiteDatabase db = getReadableDatabase();
            Map<String, Integer> occurrences = new HashMap<>();

            // Convert list of terms to a comma-separated string for the SQL query
            StringBuilder placeholders = new StringBuilder();
            for (int i = 0; i < terms.size(); i++) {
                placeholders.append("?");
                if (i < terms.size() - 1) {
                    placeholders.append(",");
                }
            }

            // SQL query
            String sql = "SELECT term, COUNT(*) as occurrence_count FROM " + NoteTermFrequencyEntry.TABLE_NAME +
                    " WHERE term IN (" + placeholders + ") GROUP BY term";

            // Convert the List to an array
            String[] termsArray = terms.toArray(new String[0]);

            // Execute the query
            Cursor cursor = db.rawQuery(sql, termsArray);
            try {
                while (cursor.moveToNext()) {
                    String term = cursor.getString(cursor.getColumnIndexOrThrow(NoteTermFrequencyEntry.COLUMN_NAME_TERM));
                    int count = cursor.getInt(cursor.getColumnIndexOrThrow("occurrence_count"));
                    occurrences.put(term, count);
                }
            } catch (Exception ex) {
                Log.e("NoteTermFrequency", "Failed to get #docs in which terms occur", ex);
            } finally {
                cursor.close();
            }

            return occurrences;
        }

        public int getDistinctNoteIdCount() {
            SQLiteDatabase db = getReadableDatabase();
            int distinctCount = 0;

            // SQL query to count distinct note_id values
            String sql = "SELECT COUNT(DISTINCT note_id) as distinct_count FROM " + NoteTermFrequencyEntry.TABLE_NAME;

            // Execute the query
            Cursor cursor = db.rawQuery(sql, null);
            try {
                if (cursor.moveToFirst()) {
                    distinctCount = cursor.getInt(cursor.getColumnIndexOrThrow("distinct_count"));
                }
            } finally {
                cursor.close();
            }

            return distinctCount;
        }

        public void deleteTermFrequencies(Long noteId) {
            // Get writable database
            SQLiteDatabase db = getWritableDatabase();

            // SQL query to count distinct note_id values
            String sql = "DELETE FROM " + NoteTermFrequencyEntry.TABLE_NAME + " WHERE "
                    + NoteTermFrequencyEntry.COLUMN_NAME_NOTE_ID + " = " + noteId;

            // Execute the query
            Cursor cursor = db.rawQuery(sql, null);
            try {
                cursor.moveToFirst();
            } finally {
                cursor.close();
            }
            // Log or handle the number of deleted rows if necessary
            Log.d("Delete", "Number of rows deleted: ");
        }

        public List<TermFrequencyEntry> getAllTermFrequencies() {
            SQLiteDatabase db = getReadableDatabase();
            List<TermFrequencyEntry> entries = new ArrayList<>();

            String[] projection = {
                NoteTermFrequencyEntry.COLUMN_NAME_NOTE_ID,
                NoteTermFrequencyEntry.COLUMN_NAME_TERM,
                NoteTermFrequencyEntry.COLUMN_NAME_FQ_IN_DOC
            };

            Cursor cursor = db.query(
                NoteTermFrequencyEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                NoteTermFrequencyEntry.COLUMN_NAME_NOTE_ID + " ASC"
            );

            while (cursor.moveToNext()) {
                entries.add(new TermFrequencyEntry(
                    cursor.getLong(cursor.getColumnIndexOrThrow(NoteTermFrequencyEntry.COLUMN_NAME_NOTE_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(NoteTermFrequencyEntry.COLUMN_NAME_TERM)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(NoteTermFrequencyEntry.COLUMN_NAME_FQ_IN_DOC))
                ));
            }
            cursor.close();

            return entries;
        }
    }
}

