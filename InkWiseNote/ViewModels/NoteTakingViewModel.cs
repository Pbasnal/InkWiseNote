using CommunityToolkit.Maui.Core.Views;
using CommunityToolkit.Maui.Core;
using System.Collections.ObjectModel;

using CommunityToolkit.Maui.Markup;
using CommunityToolkit.Mvvm.ComponentModel;

using InkWiseNote.Commons;
using InkWiseNote.UiComponents.UiElements;
using InkWiseNote.UiComponents.UiLayouts;

using Newtonsoft.Json;

using static CommunityToolkit.Maui.Markup.GridRowsColumns;
using Systems.SaveLoadSystem;
using Systems.InMemoryDataStore;

namespace InkWiseNote.ViewModels;

public partial class NoteTakingViewModel : ObservableObject
{
    [ObservableProperty]
    private HandwrittenNoteCard handwrittenNote;

    [ObservableProperty]
    private DrawingCanvasData drawingCanvasData;
    [ObservableProperty]
    private DrawingCanvasData drawingBackgroundCanvasData;

    private int verticalDistanceBetweenRuleLines = 50;

    private string originalNotePath = string.Empty;
    private string originalNoteTitle = string.Empty;

    private ExisitingCardTitlesTable exisitingCardTitlesTable;
    public NoteTakingViewModel(InMemoryDb inMemoryDb)
    {
        drawingCanvasData = new DrawingCanvasData();
        drawingBackgroundCanvasData = new DrawingCanvasData
        {
            BackgroundColor = Colors.Transparent,
        };

        exisitingCardTitlesTable = inMemoryDb.GetTable<ExisitingCardTitlesTable>(InMemoryDb.EXISTING_CARD_TITLES);
    }

    public void SetNote(HandwrittenNoteCard note)
    {
        HandwrittenNote = note;
        originalNotePath = note.Path;
        originalNoteTitle = note.Title;

        DrawingBackgroundCanvasData = new DrawingCanvasData
        {
            BackgroundColor = Colors.White,
        };

        if (File.Exists(HandwrittenNote.Path))
        {
            var serialisedCanvasData = File.ReadAllText(HandwrittenNote.Path);
            DrawingCanvasData = JsonConvert.DeserializeObject<DrawingCanvasData>(serialisedCanvasData, new JsonSerializerSettings
            {
                TypeNameHandling = TypeNameHandling.Objects
            });
            DrawingCanvasData.BackgroundColor = Colors.Transparent;
        }
        else
        {
            DrawingCanvasData = new DrawingCanvasData {
                BackgroundColor = Colors.Transparent,
            };
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

        var notePageBackground = GetDrawingView(DrawingBackgroundCanvasData);
        Grid gridView = GridLayoutBuilder.NewGrid()
            .HasColumns(30, Star)
            .HasRows(50, Star)
            .HasChildren(noteNameEntry.Row(0).ColumnSpan(2))
            .HasChildren(notePageBackground.Row(1).Column(2))
            .HasChildren(GetDrawingView(DrawingCanvasData).Row(1).ColumnSpan(2));

        noteNameEntry.BindingContext = HandwrittenNote;

        notePageBackground.SizeChanged += (object sender, EventArgs e) =>
        {
            DrawingBackgroundCanvasData.Lines = GetPageLines(verticalDistanceBetweenRuleLines,
                        (int)notePageBackground.Width,
                        (int)notePageBackground.Height);
        };

        return gridView;
    }

    internal void SaveNote()
    {
        if (string.IsNullOrWhiteSpace(HandwrittenNote?.Title))
        {
            HandwrittenNote.Title = Constants.UNTITLED_NOTE_TITLE;
        }

        NotesFileSystem.WriteNoteToFile(HandwrittenNote.Path, drawingCanvasData);

        if (originalNotePath != HandwrittenNote.Path)
        {
            NotesFileSystem.DeleteNote(originalNotePath);
            exisitingCardTitlesTable.Remove(originalNoteTitle);
        }
    }

    private View GetDrawingView(DrawingCanvasData drawingCanvasData)
    {
        var drawingViewElement = new DrawingViewElement();
        drawingViewElement.UiView.BindingContext = drawingCanvasData;

        return drawingViewElement.UiView;
    }

    private ObservableCollection<IDrawingLine> GetPageLines(int lineGap, int pageWidth, int pageHeight)
    {
        var lines = new ObservableCollection<IDrawingLine>();

        for (int lineY = lineGap; lineY < pageHeight; lineY += lineGap)
        {
            var line = new DrawingLine
            {
                LineColor = Colors.Blue,
                LineWidth = 1,
                Points = new ObservableCollection<PointF>
                {
                    new PointF(0, lineY),
                    new PointF(pageWidth, lineY),
                }
            };

            lines.Add(line);
        }

        return lines;
    }
}
