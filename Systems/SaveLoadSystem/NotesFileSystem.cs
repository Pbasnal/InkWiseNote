using InkWiseNote.Commons;

using Microsoft.Maui.Controls.PlatformConfiguration;

namespace Systems.SaveLoadSystem;

public class NotesFileSystem
{
    public static void CreateRootDirectoryIfNotExists(string rootDirectory)
    {
        Directory.CreateDirectory(rootDirectory);   
    }

    public static string FileNameToNoteTitle(string noteFilePath) => 
        noteFilePath.Split(Constants.PATH_FOLDER_SEPARATOR).Last().Split('.').First();
}
