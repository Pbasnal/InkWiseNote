using CommunityToolkit.Maui.Core;
using System.Collections.ObjectModel;

using CommunityToolkit.Maui.Markup;
using CommunityToolkit.Maui.Views;
using CommunityToolkit.Mvvm.ComponentModel;

using InkWiseNote.Commons;
using InkWiseNote.UiComponents.UiElements;
using InkWiseNote.UiComponents.UiLayouts;

using static CommunityToolkit.Maui.Markup.GridRowsColumns;
using Microsoft.Maui.Controls.Shapes;
using Newtonsoft.Json;
using System.Reflection;
using System.Runtime.Serialization;

namespace InkWiseNote.ViewModels;

public partial class NoteTakingViewModel : ObservableObject
{
    [ObservableProperty]
    private HandwrittenNoteCard handwrittenNote;

    [ObservableProperty]
    private DrawingCanvasData drawingCanvasData;

    public NoteTakingViewModel()
    {
        drawingCanvasData = new DrawingCanvasData();
    }

    public void SetNote(HandwrittenNoteCard note)
    {
        HandwrittenNote = note;

        if (File.Exists(HandwrittenNote.Path))
        {
            var serialisedCanvasData = File.ReadAllText(HandwrittenNote.Path);
            DrawingCanvasData = JsonConvert.DeserializeObject<DrawingCanvasData>(serialisedCanvasData, new JsonSerializerSettings
            {
                TypeNameHandling = TypeNameHandling.Objects
            });
        }
        else
        {
            drawingCanvasData = new DrawingCanvasData();
        }
    }

    internal View GetContent()
    {
        var noteNameEntry = new Entry
        {
            Placeholder = Constants.UNTITLED_NOTE_TITLE,
            FontSize = 20
        };
        noteNameEntry.SetBinding(Entry.TextProperty, nameof(HandwrittenNote.Title));

        Grid gridView = GridLayoutBuilder.NewGrid()
            .HasColumns(30, Star)
            .HasRows(50, Star)
            .HasChildren(noteNameEntry.Row(0).ColumnSpan(2))
            .HasChildren(GetDrawingView().Row(1).ColumnSpan(2));

        noteNameEntry.BindingContext = HandwrittenNote;

        return gridView;
    }

    internal void SaveNote()
    {
        if (string.IsNullOrWhiteSpace(HandwrittenNote?.Title))
        {
            HandwrittenNote.Title = Constants.UNTITLED_NOTE_TITLE;
        }

        File.WriteAllText(HandwrittenNote.Path, JsonConvert.SerializeObject(drawingCanvasData, Formatting.Indented, new JsonSerializerSettings
        {
            TypeNameHandling = TypeNameHandling.Objects,
            TypeNameAssemblyFormat = System.Runtime.Serialization.Formatters.FormatterAssemblyStyle.Simple
        }));

    }

    private View GetDrawingView()
    {
        var drawingViewElement = new DrawingViewElement();
        drawingViewElement.UiView.BindingContext = drawingCanvasData;

        return drawingViewElement.UiView;
    }
}
