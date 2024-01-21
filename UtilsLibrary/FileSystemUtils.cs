namespace UtilsLibrary;

public class FileSystemUtils
{
    public static IEnumerable<string> ListNotesFromDirectory(string directoryPath)
    {
        if (!Directory.Exists(directoryPath))
        {
            yield break;
        }
        IEnumerable<string> files = Directory.EnumerateFiles(directoryPath);
        foreach (string file in files)
        {
            yield return file;
        }
    }

    public static void DeleteFile(string path)
    {
        if (File.Exists(path))
        {
            File.Delete(path);
        }
    }
}
