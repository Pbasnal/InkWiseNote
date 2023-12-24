using CommunityToolkit.Mvvm.ComponentModel;

using InkWiseNote.Common;

namespace InkWiseNote.UiComponents.UiElements;

public interface IHaveImageWithTitle
{
    string Title { get; }
    string ImageName { get; }

    Func<HandwrittenNote, Task> OnNoteTap { get; }
    //Task OnNoteTap(HandwrittenNote handwrittenNote);
}

[Serializable]
public partial class HandwrittenNote : ObservableObject, IHaveImageWithTitle
{
    [ObservableProperty]
    public string title = string.Empty;
    public string Path => $"{Configs.ROOT_DIRECTORY}/{Title}.json";
    public int Id { get; set; }
    public string ImageName { get; set; } = "default_note.png";

    public Func<HandwrittenNote, Task> OnNoteTap { get; set; }
}

