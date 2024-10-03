namespace InkWiseNote.Commons;

public class Configs
{
#if ANDROID
    public static string ROOT_DIRECTORY => Path.Combine(FileSystem.AppDataDirectory, "notes");
#else
    public static string ROOT_DIRECTORY => Path.Combine("F:/InkWiseNote", "notes");
#endif

    public static string PARSED_NOTES_DIRECTORY => Path.Combine($"{ROOT_DIRECTORY}", "parsed");

    public static string VOCABULAR_FODLER_LOCATION => Path.Combine($"{ROOT_DIRECTORY}", "vocabulary");

    public const int DEFAULT_LINE_WIDTH = 2;
    public const int WIDTH_OF_NOTE = 200;
    public const int NUMBER_OF_NOTES_PER_ROW = 2;

    public const string VISION_KEY = "";
    public const string VISION_END_POINT = "";
    public static float MINIMUM_TF_IDF_SCORE = -1.0f;
}
