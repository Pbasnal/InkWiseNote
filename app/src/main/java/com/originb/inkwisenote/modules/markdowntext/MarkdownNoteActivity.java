package com.originb.inkwisenote.modules.markdowntext;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote.R;

import java.util.ArrayList;
import java.util.List;

public class MarkdownNoteActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MarkdownAdapter adapter;
    private List<Block> blocks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_markdown_editor2);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        blocks = new ArrayList<>();
        blocks.add(new Block(BlockType.MARKDOWN)); // Initial block

        adapter = new MarkdownAdapter(this, recyclerView, blocks);
        recyclerView.setAdapter(adapter);
    }
}
