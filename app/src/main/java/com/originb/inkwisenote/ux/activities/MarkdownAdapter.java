package com.originb.inkwisenote.ux.activities;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.util.StringUtil;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.modules.commonutils.Strings;
import io.noties.markwon.Markwon;

import java.util.List;
import java.util.Objects;

public class MarkdownAdapter extends RecyclerView.Adapter<MarkdownAdapter.BlockViewHolder> {
    private List<Block> blocks;
    private static Markwon markwon;
    private final Context packageContext;
    private final RecyclerView recyclerView;

    private int positionInFocus = 0;

    public MarkdownAdapter(Context packageContext, RecyclerView recyclerView, List<Block> blocks) {
        this.blocks = blocks;
        markwon = Markwon.create(packageContext);
        this.packageContext = packageContext;
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public BlockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_block, parent, false);
        return new BlockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlockViewHolder holder, int position) {
        Block block = blocks.get(position);
        holder.bind(block);
    }

    @Override
    public int getItemCount() {
        return blocks.size();
    }

    class BlockViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final EditText editText;

        public BlockViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text_view);
            editText = itemView.findViewById(R.id.edit_text);
        }


        // todo: scenario of holding the enter key for too long
        public void onFocusChange(View view, boolean hasFocus, Block block) {
            int position = getAdapterPosition();
            String editTextText = editText.getText().toString();
            String textViewText = textView.getText().toString();
            String blockText = block.getText();

            Log.d("Something wrong", editTextText + textViewText + position + blockText);
            if (hasFocus) {
//                editText.setText(block.getText());
                editText.setSelection(editText.getText().length());
                editText.setVisibility(View.VISIBLE);
                textView.setVisibility(View.GONE);
                // Set the selection to the end of the text
            } else {
                if (!Strings.isNullOrWhitespace(editTextText)) {
                    markwon.setMarkdown(textView, editTextText);
                    textViewText = textView.getText().toString();
                    notifyItemChanged(position);
                    editText.setVisibility(View.GONE);
                    textView.setVisibility(View.VISIBLE);
                }
            }
        }

        public void bind(Block block) {
            editText.addTextChangedListener(new BlockTextWatcher(this, block));
            editText.setOnFocusChangeListener((view, hasFocus) -> onFocusChange(view, hasFocus, block));
            textView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                String editTextText = editText.getText().toString();
                String textViewText = textView.getText().toString();
                String blockText = block.getText();

                Log.d("Something wrong", editTextText + textViewText + position + blockText);
                // editText.setText(block.getText());
                editText.setVisibility(View.VISIBLE);
                textView.setVisibility(View.GONE);

                // Set the selection to the end of the text
                editText.requestFocus();
                editText.setSelection(editText.getText().length());
            });
//            textView.setOnClickListener(view -> {
//                int itemPosition = getAdapterPosition();
//                if (itemPosition == positionInFocus) return;
//                positionInFocus = itemPosition;
//
//                notifyItemChanged(itemPosition);
//
//                View focusedItem = recyclerView.getFocusedChild();
//                notifyItemChanged(recyclerView.getChildAdapterPosition(focusedItem));
//            });
//            editText.setOnClickListener(view -> {
//                int itemPosition = getAdapterPosition();
//                if (itemPosition == positionInFocus) return;
//                positionInFocus = itemPosition;
//                notifyItemChanged(itemPosition);
//
//                View focusedItem = recyclerView.getFocusedChild();
//                notifyItemChanged(recyclerView.getChildAdapterPosition(focusedItem));
//            });

//            int currentPosition = getAdapterPosition();
//
//            if (this.itemView.hasFocus() || positionInFocus == currentPosition) {
//                editText.setText(block.getText());
//                editText.setVisibility(View.VISIBLE);
//                textView.setVisibility(View.GONE);
//                editText.requestFocus();
//                // Set the selection to the end of the text
//                editText.setSelection(editText.getText().length());
//            } else {
//                if (!Strings.isNullOrWhitespace(block.getText())) {
//                    markwon.setMarkdown(textView, block.getText());
//
//                    editText.setVisibility(View.GONE);
//                    textView.setVisibility(View.VISIBLE);
//                }
//            }
        }
    }

    class BlockTextWatcher implements TextWatcher {

        private RecyclerView.ViewHolder view;
        private Block block;

        public BlockTextWatcher(RecyclerView.ViewHolder view, Block blockToWatchFor) {
            this.block = blockToWatchFor;
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() < 2) return;
            char secondLastChar = s.charAt(s.length() - 2);
            char lastChar = s.charAt(s.length() - 1);
            if (lastChar == '\n' && secondLastChar == '\n') {
                block.setText(s.delete(s.length() - 2, s.length()).toString());
                int adapterPosition = view.getAdapterPosition();
                notifyItemChanged(adapterPosition);

                int nextPosition = adapterPosition + 1;
                if (nextPosition >= blocks.size()) {
                    blocks.add(new Block(BlockType.MARKDOWN));
                    notifyItemInserted(nextPosition);
                } else {
                    notifyItemChanged(nextPosition);
                }

                // Post a runnable to ensure view hierarchy updates are complete
                recyclerView.post(() -> {
                    RecyclerView.ViewHolder nextViewHolder = recyclerView.findViewHolderForAdapterPosition(nextPosition);
                    if (nextViewHolder == null) return;

                    EditText nextView = nextViewHolder.itemView.findViewById(R.id.edit_text);
                    if (nextView == null) return;

                    view.itemView.clearFocus();
                    nextView.requestFocus();
                    positionInFocus = nextPosition;
                });
            }
        }
    }
}