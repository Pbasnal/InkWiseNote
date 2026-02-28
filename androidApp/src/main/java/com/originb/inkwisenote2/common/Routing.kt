package com.originb.inkwisenote2.common

import android.content.Context
import android.content.Intent
import com.originb.inkwisenote2.modules.queries.ui.QueryCreationActivity
import com.originb.inkwisenote2.modules.queries.ui.QueryResultsActivity
import com.originb.inkwisenote2.modules.smarthome.SmartHomeActivity

/** Intent extras for ComposeHostActivity to open a specific route (Phase 1: SmartNotebook; Phase 3: FileExplorer; Phase 5: RelatedNotes). */
object ComposeRouteExtras {
    const val ROUTE = "compose_route"
    const val ROUTE_SMART_NOTEBOOK = "smart_notebook"
    const val ROUTE_FILE_EXPLORER = "file_explorer"
    const val ROUTE_RELATED_NOTES = "related_notes"
    const val BOOK_ID = "book_id"
    const val WORKING_PATH = "working_path"
    const val BOOK_TITLE = "book_title"
    const val NOTE_IDS = "note_ids"
    const val SELECTED_NOTE_ID = "selected_note_id"
    const val FILE_EXPLORER_INITIAL_PATH = "file_explorer_initial_path"
}

class Routing {
    /** Launches ComposeHostActivity with SmartNotebook route (Phase 1: legacy SmartNotebookActivity removed). */
    object SmartNotebookActivity {
        @JvmStatic
        fun newNoteIntent(packageContext: Context, currentDirectoryPath: String?) {
            val intent = Intent(packageContext, com.originb.inkwisenote2.ComposeHostActivity::class.java)
            intent.putExtra(ComposeRouteExtras.ROUTE, ComposeRouteExtras.ROUTE_SMART_NOTEBOOK)
            intent.putExtra(ComposeRouteExtras.WORKING_PATH, currentDirectoryPath)
            packageContext.startActivity(intent)
        }

        @JvmStatic
        fun openNotebookIntent(packageContext: Context, currentDirectoryPath: String?, bookId: Long?) {
            val intent = Intent(packageContext, com.originb.inkwisenote2.ComposeHostActivity::class.java)
            intent.putExtra(ComposeRouteExtras.ROUTE, ComposeRouteExtras.ROUTE_SMART_NOTEBOOK)
            intent.putExtra(ComposeRouteExtras.WORKING_PATH, currentDirectoryPath)
            intent.putExtra(ComposeRouteExtras.BOOK_ID, bookId ?: -1L)
            packageContext.startActivity(intent)
        }

        @JvmStatic
        fun openNotebookIntent(
            packageContext: Context,
            currentDirectoryPath: String?,
            bookTitle: String?,
            commaSeparatedNoteIds: String?
        ) {
            val intent = Intent(packageContext, com.originb.inkwisenote2.ComposeHostActivity::class.java)
            intent.putExtra(ComposeRouteExtras.ROUTE, ComposeRouteExtras.ROUTE_SMART_NOTEBOOK)
            intent.putExtra(ComposeRouteExtras.WORKING_PATH, currentDirectoryPath)
            intent.putExtra(ComposeRouteExtras.BOOK_TITLE, bookTitle)
            intent.putExtra(ComposeRouteExtras.NOTE_IDS, commaSeparatedNoteIds)
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
            val intent = Intent(packageContext, com.originb.inkwisenote2.ComposeHostActivity::class.java)
            intent.putExtra(ComposeRouteExtras.ROUTE, ComposeRouteExtras.ROUTE_SMART_NOTEBOOK)
            intent.putExtra(ComposeRouteExtras.WORKING_PATH, currentDirectoryPath)
            intent.putExtra(ComposeRouteExtras.BOOK_TITLE, bookTitle)
            intent.putExtra(ComposeRouteExtras.NOTE_IDS, commaSeparatedNoteIds)
            intent.putExtra(ComposeRouteExtras.SELECTED_NOTE_ID, selectedNoteId)
            packageContext.startActivity(intent)
        }
    }

    /** Phase 5: Launches ComposeHostActivity with RelatedNotes route (legacy RelatedNotesActivity removed). */
    object RelatedNotesActivity {
        @JvmStatic
        fun openRelatedNotesIntent(packageContext: Context, bookId: Long?) {
            val intent = Intent(packageContext, com.originb.inkwisenote2.ComposeHostActivity::class.java)
            intent.putExtra(ComposeRouteExtras.ROUTE, ComposeRouteExtras.ROUTE_RELATED_NOTES)
            intent.putExtra(ComposeRouteExtras.BOOK_ID, bookId ?: -1L)
            packageContext.startActivity(intent)
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

