package com.originb.inkwisenote2.modules.handwrittennotes.data

import lombok.Getter
import lombok.Setter
import java.io.Serializable

@Getter
@Setter
class PageTemplate : Serializable {
    private val templateId: Long = 0
    private val lineSpacing = 0
    private val lineColor: String? = null
    private val lineWidth = 0
}
