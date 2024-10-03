using CommunityToolkit.Mvvm.ComponentModel;

using InkWiseCore.UiComponents.UiLayouts;

using InkWiseNote.Commons;

namespace InkWiseCore.UiComponents.UiElements;


public partial class ImageCardData : ObservableObject
{
    [ObservableProperty]
    public string title = string.Empty;
    public string Path => $"{Configs.ROOT_DIRECTORY}/{Title}.json";
    public string ParsedNote => $"{Configs.PARSED_NOTES_DIRECTORY}/{Title}.json";
    public string ImageName { get; set; } = "default_note.png";

    public Func<ImageCardData, Task> OnNoteTap { get; set; }

    public IUiElement UiViewForPlaceholder { get; set; }

}

//public partial class NewNoteCard : ObservableObject, IHaveImageCardData
//{
//    [ObservableProperty]
//    public string title = "Add New Note";
//    public string Path => $"{Configs.ROOT_DIRECTORY}/{Title}.json";
//    public int Id { get; set; }
//    public string ImageName { get; set; } = "new_note.png";

//    public Func<IHaveImageCardData, Task> OnNoteTap { get; set; }
//}


public class NoteCardFactory
{
    public static ImageCardData NewNoteCard(Func<ImageCardData, Task> onTappingNote)
    {
        return new ImageCardData
        {
            Title = "Add New Note",
            ImageName = "new_note.png",
            OnNoteTap = onTappingNote
        };
    }

    public static ImageCardData NoteCard(string title, Func<ImageCardData, Task> onTappingNote)
    {
        return new ImageCardData
        {
            Title = title,
            ImageName = "default_note.png",
            OnNoteTap = onTappingNote,
        };
    }
}