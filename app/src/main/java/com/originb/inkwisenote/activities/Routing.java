package com.originb.inkwisenote.activities;

import android.content.Context;
import android.content.Intent;

public class Routing {
    public static class NoteActivity {
        public static void newNoteIntent(Context packageContext, String currentDirectoryPath) {
            Intent intent = new Intent(packageContext, com.originb.inkwisenote.activities.NoteActivity.class);
            intent.putExtra("workingNotePath", currentDirectoryPath);
            packageContext.startActivity(intent);
        }

        public static void openNoteIntent(Context packageContext, String currentDirectoryPath, Long noteId) {
            Intent intent = new Intent(packageContext, com.originb.inkwisenote.activities.NoteActivity.class);
            intent.putExtra("workingNotePath", currentDirectoryPath);
            intent.putExtra("noteId", noteId);
            packageContext.startActivity(intent);
        }
    }

    public static class RelatedNotesActivity {
        public static void openRelatedNotesIntent(Context packageContext, Long noteId) {
            Intent intent = new Intent(packageContext, com.originb.inkwisenote.activities.RelatedNotesActivity.class);
            intent.putExtra("noteId", noteId);
            packageContext.startActivity(intent);

            if (packageContext.getClass() == com.originb.inkwisenote.activities.RelatedNotesActivity.class) {
                ((com.originb.inkwisenote.activities.RelatedNotesActivity) packageContext).finish();
            }
        }
    }

    public static class HomePageActivity {
        public static void openHomePageAndStartFresh(Context packageContext) {
            Intent intent = new Intent(packageContext, com.originb.inkwisenote.activities.HomePageActivity.class);
            // Clear all activities on top and start fresh
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            packageContext.startActivity(intent);
            // packageContext.finish(); // Optional since CLEAR_TASK will finish this activity anyway
        }
    }
}

