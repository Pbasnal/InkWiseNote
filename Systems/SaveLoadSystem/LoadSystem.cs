using InkWiseNote.Commons;

namespace Systems.SaveLoadSystem;

public class LoadSystem
{
    public static IEnumerable<string> ListFilesFromDirectory(string directoryPath)
    {
        IEnumerable<string> files = Directory.EnumerateFiles(directoryPath);
        foreach (string file in files)
        {
            yield return file;
        }
    }
}
