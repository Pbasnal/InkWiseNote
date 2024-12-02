package com.originb.inkwisenote.io.sql;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class InkwiseNoteDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    protected static final int DATABASE_VERSION = 2;
    protected static final String DATABASE_NAME = "NoteText.db";

    private static List<String> createQueries = new ArrayList<>();

    public InkwiseNoteDbHelper(@Nullable Context context, @Nullable String name, int version) {
        super(context, name, null, version);
        if (Objects.isNull(getSqlCreateQuery())) return;
        createQueries.add(getSqlCreateQuery());
    }

    public void dropTable() {
        getWritableDatabase().execSQL(getSqlDropQuery());
    }

    abstract protected String getSqlCreateQuery();

    abstract protected String getSqlDropQuery();

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String createQuery : createQueries) {
            try {
                db.execSQL(createQuery);
            } catch (Exception ex) {
                Log.e("InkwiseNoteDbHelper", "Failed to create table", ex);
                if (!ex.getMessage().contains("already exists")) {
                    throw ex;
                }
            }
        }
    }
}
