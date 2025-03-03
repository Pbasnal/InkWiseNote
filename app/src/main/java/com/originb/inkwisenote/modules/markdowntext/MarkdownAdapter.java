package com.originb.inkwisenote.modules.markdowntext;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.common.Strings;
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

        public void bind(Block block) {
            editText.addTextChangedListener(new BlockTextWatcher(this, block));
            textView.setOnClickListener(view -> {
                View focusedChild = recyclerView.getFocusedChild();
                if (Objects.nonNull(focusedChild)) {
                    focusedChild.clearFocus();
                    notifyItemChanged(recyclerView.getChildAdapterPosition(focusedChild));
                }

//                itemView.findViewById(R.id.edit_text).setVisibility(View.VISIBLE);
//                itemView.findViewById(R.id.text_view).setVisibility(View.GONE);

                // Post a runnable to ensure view hierarchy updates are complete
                recyclerView.post(() -> {
                    int itemPosition = getAdapterPosition();
                    recyclerView.scrollToPosition(itemPosition);
                    itemView.requestFocus();
                    positionInFocus = itemPosition;
                    notifyItemChanged(itemPosition);
                });
            });

            if (this.itemView.hasFocus() || positionInFocus == getAdapterPosition()) {
                editText.setText(block.getText());
                editText.setVisibility(View.VISIBLE);
                textView.setVisibility(View.GONE);
            } else {
                if (!Strings.isNullOrWhitespace(block.getText())) {
                    markwon.setMarkdown(textView, block.getText());

                    editText.setVisibility(View.GONE);
                    textView.setVisibility(View.VISIBLE);
                }
            }
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

                positionInFocus = nextPosition;
                view.itemView.clearFocus();
                // Post a runnable to ensure view hierarchy updates are complete
                recyclerView.post(() -> {
                    RecyclerView.ViewHolder nextViewHolder = recyclerView.findViewHolderForAdapterPosition(nextPosition);
                    if (nextViewHolder != null) {
                        View nextView = nextViewHolder.itemView.findViewById(R.id.edit_text);
                        if (nextView != null) {
                            nextView.requestFocus();
                        }
                    }
                });
            }
        }
    }
}