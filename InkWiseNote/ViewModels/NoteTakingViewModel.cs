using Commons.Models;

using CommunityToolkit.Maui.Core;
using CommunityToolkit.Maui.Core.Views;
using CommunityToolkit.Maui.Markup;
using CommunityToolkit.Mvvm.ComponentModel;

using InkWiseNote.Commons;
using InkWiseNote.Pages;
using InkWiseNote.PageUtils;
using InkWiseNote.UiComponents.UiElements;
using InkWiseNote.UiComponents.UiLayouts;

using Newtonsoft.Json;

using System.Collections.ObjectModel;
using System.Net.Http.Headers;

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
        //OnAppearing();
    }

    public void OnAppearing()
    {
        termFrequencySystem.LoadVocabulary();
        IDictionary<string, HashSet<string>> groupedOnTerm = termFrequencySystem.GetTermFrequency()
             .Where(tf => tf.TfIdfScore > Configs.MINIMUM_TF_IDF_SCORE)
             .GroupBy(tf => tf.Term)
             .ToDictionary(group => group.Key, GetRelatedNotes);

        allRelatedNotes = new Dictionary<string, HashSet<string>>();
        foreach (string term in groupedOnTerm.Keys)
        {
            foreach (string noteName in groupedOnTerm[term])
            {
                if (!allRelatedNotes.ContainsKey(noteName))
                {
                    allRelatedNotes.Add(noteName, new HashSet<string>());
                }
                foreach (string relatedNoteName in groupedOnTerm[term])
                {
                    if (relatedNoteName == noteName) continue;
                    allRelatedNotes[noteName].Add(relatedNoteName);
                }
            }
        }
    }

    private HashSet<string> GetRelatedNotes(IGrouping<string, TermWithFrequency> grouping)
    {
        return grouping
            .Select(tf => tf.Document)
            .Where(documentName => documentName != grouping.Key)
            .ToHashSet();
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
            var canvasModel = NotesFileSystem.ReadFromFile<CanvasModel>(Configs.ROOT_DIRECTORY, HandwrittenNote.Title);
            DrawingCanvasData = DrawingCanvasData.FromCanvasModel(canvasModel);
        }
        else
        {
            DrawingCanvasData = new DrawingCanvasData();
        }
        DrawingCanvasData.BackgroundColor = Colors.Transparent;
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

        View relatedNotes = GetRelatedNotes(HandwrittenNote.Title);

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

    private Grid GetRelatedNotes(string noteTitle)
    {
        HashSet<string> relatedNotes;
        if (allRelatedNotes.ContainsKey(noteTitle) && allRelatedNotes[noteTitle].Count() > 0)
        {
            relatedNotes = allRelatedNotes[noteTitle];
        }
        else
        {
            relatedNotes = new HashSet<string>
            {
                "No related note"
            };
        }
        GridLayoutBuilder gridViewBuilder = GridLayoutBuilder.NewGrid()
            .HasColumns(relatedNotes.Select(n => new GridLength(100)).ToArray())
            .HasRows(50);

        int column = 0;
        foreach (var relatedNote in relatedNotes)
        {
            Label relatedNoteLabel = new Label
            {
                Text = relatedNote
            };
            gridViewBuilder.HasChildren(relatedNoteLabel.Row(0).Column(column++));

            var tapGestureRecognizer = new TapGestureRecognizer();
            tapGestureRecognizer.Tapped += OnRelatedNoteTap;

            relatedNoteLabel.GestureRecognizers.Add(tapGestureRecognizer);
        }

        return gridViewBuilder;
    }

    private async void OnRelatedNoteTap(object? sender, TappedEventArgs e)
    {

        Label relatedNoteLabel = sender as Label;
        if (Objects.IsNull(relatedNoteLabel)) return;

        string relatedNotename = relatedNoteLabel.Text;

        HandwrittenNoteCard noteCard = new HandwrittenNoteCard
        {
            Title = relatedNotename,
        };

        await NavigatePage.To<NoteTakingPage>()
                   .WithParameter("HandwrittenNoteCard", (object)noteCard)
                   .Navigate();
    }

    internal void SaveNote()
    {
        if (string.IsNullOrWhiteSpace(HandwrittenNote?.Title))
        {
            HandwrittenNote.Title = Constants.UNTITLED_NOTE_TITLE;
        }

        NotesFileSystem.WriteNoteToFile(Configs.ROOT_DIRECTORY, HandwrittenNote.Title, CanvasModel.FromCanvasData(drawingCanvasData));

        if (originalNotePath != HandwrittenNote.Path)
        {
            NotesFileSystem.DeleteNote(originalNotePath);
            exisitingCardTitlesTable.Remove(originalNoteTitle);

            VisionResponse visionResponse = LoadVisionResponse(originalNoteTitle);
            if (Objects.IsNotNull(visionResponse) && Objects.IsNotNull(visionResponse.readResult) && Objects.IsNotNull(visionResponse.readResult.content))
                termFrequencySystem.RemoveDocumentFromVocabulary(new Document(originalNoteTitle, visionResponse.readResult.content));
        }

        ProcessNoteContent(HandwrittenNote);
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


    private void ProcessNoteContent(HandwrittenNoteCard handwrittenNote)
    {
        Task.Factory.StartNew(() =>
        {
            VisionResponse visionResponse = LoadVisionResponse(handwrittenNote.Title);
            if (Objects.IsNotNull(visionResponse) && Objects.IsNotNull(visionResponse.readResult) && Objects.IsNotNull(visionResponse.readResult.content))
                termFrequencySystem.RemoveDocumentFromVocabulary(new Document(handwrittenNote.Title, visionResponse.readResult.content));

            var imageStream = notePage.DrawingView.GetImageStream(notePage.DrawingView.Width,
                notePage.DrawingView.Height).Result;

            visionResponse = ApplyOcr(imageStream);

            If.Condition(Objects.IsNotNull(visionResponse))
            .RunIfTrue(() => UpdateTfIdfScores(handwrittenNote.Title, visionResponse.readResult.content))
            .OrElse(() => { });
        });
    }

    private VisionResponse LoadVisionResponse(string handwrittenNoteTitle)
    {
        return SaveSystem.ReadFromFile<VisionResponse>(Configs.PARSED_NOTES_DIRECTORY, handwrittenNoteTitle);
    }

    public VisionResponse ApplyOcr(Stream imageStream)
    {
        VisionResponse visionResponse = null;
        try
        {
            string textConverstionResponse = ConvertHandwritingToText(imageStream);
            NotesFileSystem.WriteNoteToFile(Configs.PARSED_NOTES_DIRECTORY, HandwrittenNote.Title, textConverstionResponse, isJson: true);

            visionResponse = JsonConvert.DeserializeObject<VisionResponse>(textConverstionResponse);
        }
        catch (Exception ex)
        {
        }

        return visionResponse;
    }

    public void UpdateTfIdfScores(string noteTitle, string noteContent)
    {
        Document document = new Document(noteTitle, noteContent);
        termFrequencySystem.UpdateVocabulary(document);

        termFrequencySystem.SaveVocabulary();
    }

    private string ConvertHandwritingToText(Stream imageStream)
    {
        var client = new HttpClient();

        byte[] imageBytes = new byte[imageStream.Length];
        imageStream.Read(imageBytes, 0, imageBytes.Length);
        string contents = Convert.ToBase64String(imageBytes);
        HttpContent content = new ByteArrayContent(imageBytes);//, Encoding.UTF8, "image/*");
        content.Headers.ContentType = new MediaTypeHeaderValue("application/octet-stream");


        client.BaseAddress = new Uri(Configs.VISION_END_POINT);
        //client.DefaultRequestHeaders.Add("Content-Type", "image/png");
        client.DefaultRequestHeaders.Add("Ocp-Apim-Subscription-Key", Configs.VISION_KEY);

        var result = client.PostAsync("/computervision/imageanalysis:analyze?api-version=2023-02-01-preview&features=read&language=en", content).Result;
        string resultContent = result.Content.ReadAsStringAsync().Result;

        //VisionResponse visionResponse = JsonConvert.DeserializeObject<VisionResponse>(resultContent);

        return resultContent;
    }

   
}
