package com.originb.inkwisenote.ux.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.originb.inkwisenote.DebugContext;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.data.notedata.NoteEntity;
import com.originb.inkwisenote.data.notedata.NoteMeta;
import com.originb.inkwisenote.modules.functionalUtils.Try;
import com.originb.inkwisenote.modules.noteoperations.NoteOperations;
import com.originb.inkwisenote.modules.repositories.Repositories;
import com.originb.inkwisenote.modules.repositories.SmartNotebook;
import com.originb.inkwisenote.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote.ux.utils.NoteStack;
import com.originb.inkwisenote.ux.views.DrawingView;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

public class SmartNotebookActivity extends AppCompatActivity {

    private DebugContext debugContext;

    private SmartNotebookRepository smartNotebookRepository;
    private DrawingView drawingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
