package com.originb.inkwisenote.common;

import android.content.Context;
import android.content.Intent;
import com.originb.inkwisenote.modules.markdowntext.MarkdownNoteActivity;

public class Routing {
    public static class NoteActivity {
        public static void newMarkdownNoteIntent(Context packageContext, String currentDirectoryPath) {
            Intent intent = new Intent(packageContext, MarkdownNoteActivity.class);
            intent.putExtra("workingNotePath", currentDirectoryPath);
            packageContext.startActivity(intent);
        }
    }

    public static class SmartNotebookActivity {
        public static void newNoteIntent(Context packageContext, String currentDirectoryPath) {
            Intent intent = new Intent(packageContext, com.originb.inkwisenote.modules.smartnotes.ui.SmartNotebookActivity.class);
            intent.putExtra("workingNotePath", currentDirectoryPath);
            packageContext.startActivity(intent);
        }

        public static void openNotebookIntent(Context packageContext, String currentDirectoryPath, Long bookId) {

            Intent intent = new Intent(packageContext, com.originb.inkwisenote.modules.smartnotes.ui.SmartNotebookActivity.class);
            intent.putExtra("workingNotePath", currentDirectoryPath);
            intent.putExtra("bookId", bookId);
            packageContext.startActivity(intent);
        }
    }

    public static class RelatedNotesActivity {
        public static void openRelatedNotesIntent(Context packageContext, Long bookId) {
            Intent intent = new Intent(packageContext, com.originb.inkwisenote.modules.noterelation.ui.RelatedNotesActivity.class);
            intent.putExtra("book_id", bookId);
            packageContext.startActivity(intent);

            if (packageContext.getClass() == com.originb.inkwisenote.modules.noterelation.ui.RelatedNotesActivity.class) {
                ((com.originb.inkwisenote.modules.noterelation.ui.RelatedNotesActivity) packageContext).finish();
            }
        }
    }

    public static class HomePageActivity {
        public static void openHomePageAndStartFresh(Context packageContext) {
            Intent intent = new Intent(packageContext, com.originb.inkwisenote.HomePageActivity.class);
            // Clear all activities on top and start fresh
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            packageContext.startActivity(intent);
            // packageContext.finish(); // Optional since CLEAR_TASK will finish this activity anyway
        }
    }
}

