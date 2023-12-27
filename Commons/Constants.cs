namespace InkWiseNote.Commons;

public class Constants
{
    public const string UNTITLED_NOTE_TITLE = "Untitled";
#if ANDROID
    public const char PATH_FOLDER_SEPARATOR = '/';
#else
    public const char PATH_FOLDER_SEPARATOR = '\\';
#endif
}
