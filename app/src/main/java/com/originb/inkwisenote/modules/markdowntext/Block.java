package com.originb.inkwisenote.modules.markdowntext;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Block {
    private String text;
    private BlockType type;

    public Block(BlockType type) {
        this.text = "";
        this.type = type;
    }
}

