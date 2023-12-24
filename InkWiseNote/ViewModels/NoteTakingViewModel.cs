using CommunityToolkit.Maui.Markup;
using CommunityToolkit.Mvvm.ComponentModel;

using InkWiseNote.Common;
using InkWiseNote.UiComponents.UiElements;
using InkWiseNote.UiComponents.UiLayouts;

using static CommunityToolkit.Maui.Markup.GridRowsColumns;

namespace InkWiseNote.ViewModels;

public partial class NoteTakingViewModel : ObservableObject
{
    [ObservableProperty]
    private HandwrittenNote handwrittenNote;

    public void SetNote(HandwrittenNote note)
    {
        HandwrittenNote = note;
    }

    internal View GetContent()
    {
        var noteNameEntry = new Entry
        {
            Placeholder = "Untitled",
            FontSize = 20
        };
        noteNameEntry.SetBinding(Entry.TextProperty, nameof(HandwrittenNote.Title));

        Grid gridView = GridLayoutBuilder.NewGrid()
            .HasColumns(30, Star)
            .HasRows(50, Star)
            .HasChildren(noteNameEntry.Row(0).ColumnSpan(2));

        gridView.BindingContext = HandwrittenNote;

        return gridView;
    }

    internal void SaveNote()
    {
        if (string.IsNullOrWhiteSpace(HandwrittenNote?.Title)) {
            HandwrittenNote.Title = Constants.UNTITLED_NOTE_TITLE;
        }
        
        //File.WriteAllText(savedNoteFullName, JsonConvert.SerializeObject(Lines));
        File.WriteAllText(HandwrittenNote.Path, "Empty note");
    }
}
