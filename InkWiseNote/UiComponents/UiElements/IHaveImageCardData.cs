using CommunityToolkit.Mvvm.ComponentModel;

using InkWiseNote.Commons;

namespace InkWiseNote.UiComponents.UiElements;

public interface IHaveImageCardData
{
    string Title { get; internal set; }
    string ImageName { get; }
    Func<IHaveImageCardData, Task> OnNoteTap { get; }
}

[Serializable]
public partial class HandwrittenNoteCard : ObservableObject, IHaveImageCardData
{
    [ObservableProperty]
    public string title = string.Empty;
    public string Path => $"{Configs.ROOT_DIRECTORY}/{Title}.json";
    public string ParsedNote => $"{Configs.PARSED_NOTES_DIRECTORY}/{Title}.json";
    public string ImageName { get; set; } = "default_note.png";

    public Func<IHaveImageCardData, Task> OnNoteTap { get; set; }

}

public partial class NewNoteCard : ObservableObject, IHaveImageCardData
{
    [ObservableProperty]
    public string title = "Add New Note";
    public string Path => $"{Configs.ROOT_DIRECTORY}/{Title}.json";
    public int Id { get; set; }
    public string ImageName { get; set; } = "new_note.png";

    public Func<IHaveImageCardData, Task> OnNoteTap { get; set; }
}


public class NoteCardFactory
{
    public static IHaveImageCardData NewNoteCard(Func<IHaveImageCardData, Task> onTappingNote)
    {
        return new NewNoteCard
        {
            //Title = "New Note",
            //ImageName = "new_note.png",
            OnNoteTap = onTappingNote,
        };
    }

    public static IHaveImageCardData NoteCard(string title, Func<IHaveImageCardData, Task> onTappingNote)
    {
        return new HandwrittenNoteCard
        {
            Title = title,
            OnNoteTap = onTappingNote,
        };
    }
}