using InkWiseNote.Commons;

namespace Systems.SaveLoadSystem;


// this class should take HandwrittenNote as input
// for that HandwrittenNote should be moved to a different 
// common project
// this class still has information of how the data is stored on hardware.
// it knows that we need file system to store the data. That should get abstracted
// since we might want to store the data remotely or in a local db
public class NotesFileSystem
{
    public static IEnumerable<string> ListAllNotes() => UtilsLibrary.FileSystemUtils.ListNotesFromDirectory(Configs.ROOT_DIRECTORY);


    public static string FileNameToNoteTitle(string noteFilePath) =>
        noteFilePath.Split(Constants.PATH_FOLDER_SEPARATOR).Last().Split('.').First();

    // this method should delete all note related information.
    // including parsedNotes.
    // Or every system should listen to delete not event and 
    // do the necessary cleanup
    public static void DeleteNote(string path) => UtilsLibrary.FileSystemUtils.DeleteFile(path);

    public static void WriteNoteToFile(string directoryPath, string fileName, object data, bool isJson = false)
        => SaveSystem.SaveData(directoryPath, fileName, data, isJson);

    public static T ReadFromFile<T>(string directoryPath, string fileName) => SaveSystem.ReadFromFile<T>(directoryPath, fileName);
}
