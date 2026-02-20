package com.originb.inkwisenote2.common

import android.content.Context
import android.content.Intent
import com.originb.inkwisenote2.modules.queries.ui.QueryCreationActivity
import com.originb.inkwisenote2.modules.queries.ui.QueryResultsActivity
import com.originb.inkwisenote2.modules.smarthome.SmartHomeActivity

class Routing {
    object SmartNotebookActivity {
        @JvmStatic
        fun newNoteIntent(packageContext: Context, currentDirectoryPath: String?) {
            val intent =
                Intent(packageContext, com.originb.inkwisenote2.modules.smartnotes.ui.SmartNotebookActivity::class.java)
            intent.putExtra("workingNotePath", currentDirectoryPath)
            packageContext.startActivity(intent)
        }

        @JvmStatic
        fun openNotebookIntent(packageContext: Context, currentDirectoryPath: String?, bookId: Long?) {
            val intent =
                Intent(packageContext, com.originb.inkwisenote2.modules.smartnotes.ui.SmartNotebookActivity::class.java)
            intent.putExtra("workingNotePath", currentDirectoryPath)
            intent.putExtra("bookId", bookId)
            packageContext.startActivity(intent)
        }

        @JvmStatic
        fun openNotebookIntent(
            packageContext: Context,
            currentDirectoryPath: String?,
            bookTitle: String?,
            commaSeparatedNoteIds: String?
        ) {
            val intent =
                Intent(packageContext, com.originb.inkwisenote2.modules.smartnotes.ui.SmartNotebookActivity::class.java)
            intent.putExtra("workingNotePath", currentDirectoryPath)
            intent.putExtra("bookTitle", bookTitle)
            intent.putExtra("noteIds", commaSeparatedNoteIds)
            packageContext.startActivity(intent)
        }

        @JvmStatic
        fun openNotebookIntent(
            packageContext: Context,
            currentDirectoryPath: String?,
            bookTitle: String?,
            commaSeparatedNoteIds: String?,
            selectedNoteId: Long
        ) {
            val intent =
                Intent(packageContext, com.originb.inkwisenote2.modules.smartnotes.ui.SmartNotebookActivity::class.java)
            intent.putExtra("workingNotePath", currentDirectoryPath)
            intent.putExtra("bookTitle", bookTitle)
            intent.putExtra("noteIds", commaSeparatedNoteIds)
            intent.putExtra("selectedNoteId", selectedNoteId)
            packageContext.startActivity(intent)
        }
    }

    object RelatedNotesActivity {
        @JvmStatic
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

    object NoteSearchActivity {
        @JvmStatic
        fun openSearchPage(packageContext: Context) {
            val searchIntent = Intent(
                packageContext,
                com.originb.inkwisenote2.modules.notesearch.NoteSearchActivity::class.java
            )
            packageContext.startActivity(searchIntent)
        }

        @JvmStatic
        fun openAllNotebooksPage(packageContext: Context) {
            val intent = Intent(
                packageContext,
                com.originb.inkwisenote2.modules.notesearch.NoteSearchActivity::class.java
            )
            intent.putExtra("show_all_notebooks", true)
            packageContext.startActivity(intent)
        }
    }

    object HomePageActivity {
        @JvmStatic
        fun openSmartHomePageAndStartFresh(packageContext: Context) {
            val intent = Intent(packageContext, SmartHomeActivity::class.java)
            // Clear all activities on top and start fresh
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            packageContext.startActivity(intent)
            // packageContext.finish(); // Optional since CLEAR_TASK will finish this activity anyway
        }
    }

    object QueryActivity {
        @JvmStatic
        fun openQueryActivity(packageContext: Context) {
            val intent = Intent(packageContext, QueryCreationActivity::class.java)
            packageContext.startActivity(intent)
        }

        @JvmStatic
        fun openQueryResultsActivity(packageContext: Context, queryName: String?) {
            val intent = Intent(packageContext, QueryResultsActivity::class.java)
            intent.putExtra("query_name", queryName)
            packageContext.startActivity(intent)
        }
    }

    object AdminActivity {
        @JvmStatic
        fun openAdminActivity(packageContext: Context) {
            val intent = Intent(packageContext, com.originb.inkwisenote2.modules.admin.AdminActivity::class.java)
            packageContext.startActivity(intent)
        }
    }
}

