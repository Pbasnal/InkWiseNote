package com.originb.inkwisenote2.common

import android.content.Context
import android.content.Intent

class Routing {
    object SmartNotebookActivity {
        fun newNoteIntent(packageContext: Context, currentDirectoryPath: String?) {
            val intent =
                Intent(packageContext, com.originb.inkwisenote2.modules.smartnotes.ui.SmartNotebookActivity::class.java)
            intent.putExtra("workingNotePath", currentDirectoryPath)
            packageContext.startActivity(intent)
        }

        fun openNotebookIntent(packageContext: Context, currentDirectoryPath: String?, bookId: Long?) {
            val intent =
                Intent(packageContext, com.originb.inkwisenote2.modules.smartnotes.ui.SmartNotebookActivity::class.java)
            intent.putExtra("workingNotePath", currentDirectoryPath)
            intent.putExtra("bookId", bookId)
            packageContext.startActivity(intent)
        }
    }

    object TextNoteActivity {
        fun newNoteIntent(packageContext: Context, currentDirectoryPath: String?) {
            val intent = Intent(packageContext, com.originb.inkwisenote2.modules.textnote.TextNoteActivity::class.java)
            intent.putExtra("workingNotePath", currentDirectoryPath)
            packageContext.startActivity(intent)
        }

        fun openNotebookIntent(packageContext: Context, currentDirectoryPath: String?, bookId: Long?) {
            val intent = Intent(packageContext, com.originb.inkwisenote2.modules.textnote.TextNoteActivity::class.java)
            intent.putExtra("workingNotePath", currentDirectoryPath)
            intent.putExtra("bookId", bookId)
            packageContext.startActivity(intent)
        }
    }

    object RelatedNotesActivity {
        fun openRelatedNotesIntent(packageContext: Context, bookId: Long?) {
            val intent = Intent(
                packageContext,
                com.originb.inkwisenote2.modules.noterelation.ui.RelatedNotesActivity::class.java
            )
            intent.putExtra("book_id", bookId)
            packageContext.startActivity(intent)

            if (packageContext.javaClass == com.originb.inkwisenote2.modules.noterelation.ui.RelatedNotesActivity::class.java) {
                (packageContext as com.originb.inkwisenote2.modules.noterelation.ui.RelatedNotesActivity).finish()
            }
        }
    }

    object HomePageActivity {
        fun openHomePageAndStartFresh(packageContext: Context) {
            val intent = Intent(packageContext, com.originb.inkwisenote2.HomePageActivity::class.java)
            // Clear all activities on top and start fresh
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            packageContext.startActivity(intent)
            // packageContext.finish(); // Optional since CLEAR_TASK will finish this activity anyway
        }
    }
}

