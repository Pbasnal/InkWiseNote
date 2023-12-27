namespace InkWiseNote.Commons;

public class Configs
{
#if ANDROID
    public static string ROOT_DIRECTORY => System.IO.Path.Combine(FileSystem.AppDataDirectory, "notes");
#else
    public static string ROOT_DIRECTORY => System.IO.Path.Combine("F:/InkWiseNote", "notes"); 
#endif
    public const int WIDTH_OF_NOTE = 200;
    public const int NUMBER_OF_NOTES_PER_ROW = 2;
}
