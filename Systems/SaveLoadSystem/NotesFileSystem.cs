using InkWiseNote.Commons;

using Newtonsoft.Json;

namespace Systems.SaveLoadSystem;

public class NotesFileSystem
{
    public static void CreateRootDirectoryIfNotExists(string rootDirectory)
    {
        Directory.CreateDirectory(rootDirectory);   
    }

    public static IEnumerable<string> ListFilesFromDirectory(string directoryPath)
    {
        IEnumerable<string> files = Directory.EnumerateFiles(directoryPath);
        foreach (string file in files)
        {
            yield return file;
        }
    }

    public static string FileNameToNoteTitle(string noteFilePath) => 
        noteFilePath.Split(Constants.PATH_FOLDER_SEPARATOR).Last().Split('.').First();

    public static void DeleteNote(string path)
    {
        if (File.Exists(path))
        {
            File.Delete(path);
        }
    }

    public static void WriteNoteToFile(string path, object note)
    {
        File.WriteAllText(path, JsonConvert.SerializeObject(note, Formatting.Indented, new JsonSerializerSettings
        {
            TypeNameHandling = TypeNameHandling.Objects,
            TypeNameAssemblyFormat = System.Runtime.Serialization.Formatters.FormatterAssemblyStyle.Simple
        }));
    }
}
