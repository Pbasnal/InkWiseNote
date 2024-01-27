using InkWiseCore.UiComponents.UiElements;

using InkWiseNote.Commons;

using Systems.SaveLoadSystem;
using Systems.TextProcessingSystem;

namespace InkWiseCore.NotesFuncationalities;

// All the code in this file is included in all platforms.
public static class Notes
{
    public static void SaveNote(string noteName,
        DrawingCanvasData drawingCanvasData)
    {
        NotesFileSystem.WriteNoteToFile(Configs.ROOT_DIRECTORY, noteName, CanvasModel.FromCanvasData(drawingCanvasData));
    }

    public static void UpdateNote(string originalName,
        string newName,
        DrawingCanvasData drawingCanvasData,
        TermFrequencySystem termFrequencySystem)
    {
        NotesFileSystem.DeleteNote(Configs.ROOT_DIRECTORY, originalName);
        
        SaveNote(newName, drawingCanvasData);
    }
}
