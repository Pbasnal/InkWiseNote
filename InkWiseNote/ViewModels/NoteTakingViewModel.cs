using Commons;
using Commons.Models;

using CommunityToolkit.Maui.Core;
using CommunityToolkit.Maui.Core.Views;
using CommunityToolkit.Maui.Markup;
using CommunityToolkit.Mvvm.ComponentModel;

using InkWiseCore.NotesFuncationalities;
using InkWiseCore.UiComponents.UiElements;
using InkWiseCore.UiComponents.UiLayouts;

using InkWiseNote.Commons;
using InkWiseNote.Pages;
using InkWiseNote.PageUtils;

using System.Collections.ObjectModel;

using Systems.InMemoryDataStore;
using Systems.SaveLoadSystem;
using Systems.TextProcessingSystem;

using UtilsLibrary;

using static CommunityToolkit.Maui.Markup.GridRowsColumns;

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
    private TermFrequencySystem termFrequencySystem;
    private IDictionary<string, HashSet<string>> allRelatedNotes;

    DrawingViewElement notePage;

    public NoteTakingViewModel(InMemoryDb inMemoryDb, TermFrequencySystem termFrequencySystem)
    {
        drawingCanvasData = new DrawingCanvasData();
        drawingBackgroundCanvasData = new DrawingCanvasData
        {
            BackgroundColor = Colors.Transparent,
        };

        exisitingCardTitlesTable = inMemoryDb.GetTable<ExisitingCardTitlesTable>(InMemoryDb.EXISTING_CARD_TITLES);
        this.termFrequencySystem = termFrequencySystem;
    }

    public void OnAppearing()
    {
        IDictionary<string, HashSet<string>> groupedOnKeyword = NotesKeywords.GetNotesGroupedByKeywords(termFrequencySystem);

        allRelatedNotes = NotesKeywords.RelateNotesByCommonKeywords(groupedOnKeyword);
    }

    public void SetNote(HandwrittenNoteCard note)
    {
        HandwrittenNote = note;
        originalNotePath = note.Path;
        originalNoteTitle = note.Title;

        DrawingBackgroundCanvasData = new DrawingCanvasData
        {
            BackgroundColor = AppSettings.Current.CanvasBackgroundColour,
        };

        if (File.Exists(HandwrittenNote.Path))
        {
            var canvasModel = NotesFileSystem.ReadFromFile<CanvasModel>(Configs.ROOT_DIRECTORY, HandwrittenNote.Title);
            DrawingCanvasData = DrawingCanvasData.FromCanvasModel(canvasModel);
        }
        else
        {
            DrawingCanvasData = new DrawingCanvasData();
        }
        DrawingCanvasData.BackgroundColor = AppSettings.Current.CanvasColor;
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
        notePage = GetDrawingView(DrawingCanvasData);

        View relatedNotes = GetRelatedNotesView(HandwrittenNote.Title);

        Grid gridView = GridLayoutBuilder.NewGrid()
            .HasColumns(30, Star)
            .HasRows(50, 50, Star)
            .HasChildren(noteNameEntry.Row(0).ColumnSpan(2))
            .HasChildren(relatedNotes.Row(1).ColumnSpan(2))
            .HasChildren(notePageBackground.UiView.Row(2).Column(2))
            .HasChildren(notePage.UiView.Row(2).ColumnSpan(2));

        noteNameEntry.BindingContext = HandwrittenNote;

        notePageBackground.UiView.SizeChanged += (object sender, EventArgs e) =>
        {
            DrawingBackgroundCanvasData.Lines = GetPageLines(verticalDistanceBetweenRuleLines,
                        (int)notePageBackground.UiView.Width,
                        (int)notePageBackground.UiView.Height);
        };

        return gridView;
    }

    private Grid GetRelatedNotesView(string noteTitle)
    {
        List<Label> relatedNotes;
        if (allRelatedNotes.ContainsKey(noteTitle) && allRelatedNotes[noteTitle].Count() > 0)
        {

            relatedNotes = allRelatedNotes[noteTitle].Select(n =>
            {
                Label relatedNoteLabel = new Label
                {
                    Text = n
                };

                var tapGestureRecognizer = new TapGestureRecognizer();
                tapGestureRecognizer.Tapped += OnRelatedNoteTap;

                relatedNoteLabel.GestureRecognizers.Add(tapGestureRecognizer);
                return relatedNoteLabel;
            }).ToList();
        }
        else
        {
            relatedNotes = new List<Label>
            {
                new Label
                {
                    Text = "No related note"
                }
            };
        }

        GridLayoutBuilder gridViewBuilder = GridLayoutBuilder.NewGrid()
            .HasColumns(relatedNotes.Select(n => new GridLength(100)).ToArray())
            .HasRows(50);

        int column = 0;
        foreach (var relatedNote in relatedNotes)
        {
            gridViewBuilder.HasChildren(relatedNote.Row(0).Column(column++));
        }

        return gridViewBuilder;
    }

    private async void OnRelatedNoteTap(object? sender, TappedEventArgs e)
    {
        if (Objects.IsNull(sender) || !(sender is Label)) return;

        Label relatedNoteLabel = sender as Label;
        string relatedNotename = relatedNoteLabel.Text;

        HandwrittenNoteCard noteCard = new HandwrittenNoteCard
        {
            Title = relatedNotename,
        };

        await NavigatePage.To<NoteTakingPage>()
                   .WithParameter("HandwrittenNoteCard", (object)noteCard)
                   .Navigate();
    }

    internal async Task SaveNote()
    {
        if (!ShouldSaveNote()) return;

        EnsureNoteHasAName();

        if (HasNoteNameChanged())
        {
            Notes.UpdateNote(originalNoteTitle, HandwrittenNote.Title, DrawingCanvasData, termFrequencySystem);
            NotesKeywords.RemoveNoteFromVocabulary(originalNoteTitle, termFrequencySystem);

            exisitingCardTitlesTable.Remove(originalNoteTitle);
        }
        else
        {
            Notes.SaveNote(HandwrittenNote.Title, DrawingCanvasData);
        }

        NotesKeywords.RemoveNoteFromVocabulary(HandwrittenNote.Title, termFrequencySystem);

       await Task.Factory.StartNew(() =>
        {
            var imageStream = GetHandwrittenNoteAsImageStream();
            VisionResponse ocrResult = OcrFunctionalities.ApplyOcrOnNote(HandwrittenNote.Title, imageStream).Result;

            if (!OcrFunctionalities.isVisionResponseValid(ocrResult))
            {
                return;
            }

            NotesKeywords.UpdateNoteInVocabulary(HandwrittenNote.Title, ocrResult.readResult.content,
                 termFrequencySystem);
        });
    }

    private bool ShouldSaveNote()
    {
        bool noteHasTitle = !string.IsNullOrWhiteSpace(HandwrittenNote?.Title);
        bool noteHasContent = DrawingCanvasData.Lines.Count > 0;
        
        return noteHasTitle || noteHasContent;
    }

    private void EnsureNoteHasAName()
    {
        HandwrittenNote.Title = If.Condition(string.IsNullOrWhiteSpace(HandwrittenNote?.Title))
            .IsTrue(Constants.UNTITLED_NOTE_TITLE)
            .OrElse(HandwrittenNote.Title);
    }

    private bool HasNoteNameChanged() => !string.Equals(originalNotePath, HandwrittenNote.Path);

    private Stream GetHandwrittenNoteAsImageStream()
    {
        return If.Condition(DrawingCanvasData.Lines.Count > 0)
             .IsTrueRun(() => notePage.DrawingView.GetImageStream(notePage.DrawingView.Width,
                    notePage.DrawingView.Height).Result)
             .OrElse(() => null);
    }

    private DrawingViewElement GetDrawingView(DrawingCanvasData drawingCanvasData)
    {
        var drawingViewElement = new DrawingViewElement();
        drawingViewElement.UiView.BindingContext = drawingCanvasData;

        return drawingViewElement;
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
