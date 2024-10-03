using Commons.Models;

using CommunityToolkit.Maui.Core.Extensions;
using CommunityToolkit.Mvvm.ComponentModel;

using InkWiseCore.UiComponents.UiElements;
using InkWiseCore.UiComponents.UiLayouts;

using InkWiseNote.Commons;
using InkWiseNote.PageUtils;
using InkWiseNote.ViewModels;

using Systems.BackgroundJob;
using Systems.InMemoryDataStore;
using Systems.MonoBehaviour;
using Systems.SaveLoadSystem;
using Systems.TextProcessingSystem;

using UtilsLibrary;

namespace InkWiseNote.Pages;

internal enum HomePageMessage
{
    OnNoteDelete,
    OnNoteTap
}

public class HomePage : ContentPage, IHaveBehaviours
{
    // Behaviours
    HomeViewCreatorBehaviour creatorBehaviour;
    // /Behaviours

    HashSet<string> sortedListOfNoteTitles;
    private ExisitingCardTitlesTable exisitingCardTitlesTable;
    private TermFrequencySystem termFrequencySystem;

    //private HomeViewModel viewModel;
    private JobSystem jobSystem;

    private const string DIRECTORY_READER_JOB = "NOTES_PARSER_JOB";
    private const int WAIT_TIME_BEFORE_DIRECTORY_READER_JOB_STARTS_MS = 10;
    private const int WAIT_TIME_BETWEEN_DIRECTORY_READS_MS = 1000;

    public HomePage(HomeViewModel viewModel, JobSystem jobSystem, InMemoryDb inMemoryDb,
        TermFrequencySystem termFrequencySystem)
    {
        //this.viewModel = viewModel;
        this.jobSystem = jobSystem;
        this.termFrequencySystem = termFrequencySystem;

        sortedListOfNoteTitles = new HashSet<string>();
        creatorBehaviour = new HomeViewCreatorBehaviour(this, OnDeleteNote);

        exisitingCardTitlesTable = inMemoryDb.GetTable<ExisitingCardTitlesTable>(InMemoryDb.EXISTING_CARD_TITLES);

        exisitingCardTitlesTable.OnDataDeleteEvent += DeleteCardTriggeredFromOtherViews;

        ReloadNotesFromDirectory();

        jobSystem.RegisterJob(DIRECTORY_READER_JOB,
            () => ReloadNotesFromDirectory(),
            WAIT_TIME_BEFORE_DIRECTORY_READER_JOB_STARTS_MS,
            WAIT_TIME_BETWEEN_DIRECTORY_READS_MS);
    }


    protected override void OnAppearing()
    {
        base.OnAppearing();

        //viewModel.Clear();
        Content = ReloadNotesFromDirectory();

        jobSystem.StartJob(DIRECTORY_READER_JOB);
    }

    protected override void OnDisappearing()
    {
        base.OnDisappearing();
        jobSystem.StopJob(DIRECTORY_READER_JOB);
    }

    private View ReloadNotesFromDirectory()
    {
        sortedListOfNoteTitles = ReadNoteTitlesFromDisk();
        return Try<View>.Executing(() => creatorBehaviour.RunBehaviour(sortedListOfNoteTitles))
             .HandleIfThrows(HandleContentCreationException)
             .GetResult;
    }

    private void HandleContentCreationException(Exception exception)
    {
        throw new NotImplementedException();
    }

    public void OnDeleteNote(ImageCardData noteData)
    {
        if (Objects.IsNull(noteData)) return;

        FileSystemUtils.DeleteFile(noteData.Path);

        VisionResponse visionResponse = LoadVisionResponse(noteData.Title);
        DeleteVisionData(visionResponse, noteData.Title);

        FileSystemUtils.DeleteFile(noteData.ParsedNote);

        var noteTitle = noteData.Title;
        exisitingCardTitlesTable.Remove(noteTitle);
    }

    private VisionResponse LoadVisionResponse(string handwrittenNoteTitle)
    {
        return SaveSystem.ReadFromFile<VisionResponse>(Configs.PARSED_NOTES_DIRECTORY, handwrittenNoteTitle);
    }

    // external change handler
    private void DeleteCardTriggeredFromOtherViews(string cardTitle)
    {
        //var cardToDelete = CardCollectionViewData.Items.FirstOrDefault(card => cardTitle.Equals(card.Title));
        //CardCollectionViewData.Items.Remove(cardToDelete);

        sortedListOfNoteTitles.Remove(cardTitle);
        // the card will be removed in the next refresh cycle
    }

    private void DeleteVisionData(VisionResponse visionResponse, string noteTitle)
    {
        termFrequencySystem.LoadVocabulary();
        if (Objects.IsNotNull(visionResponse) && Objects.IsNotNull(visionResponse.readResult) && Objects.IsNotNull(visionResponse.readResult.content))
            termFrequencySystem.RemoveDocumentFromVocabulary(new Document(noteTitle, visionResponse.readResult.content));
    }

    internal HashSet<string> ReadNoteTitlesFromDisk()
    {
        HashSet<string> setOfNoteTitles = NotesFileSystem.ListAllNotes()
            .Select(NotesFileSystem.FileNameToNoteTitle)
            .Where(noteTitle => !exisitingCardTitlesTable.Contains(noteTitle))
            .Select(noteTitle => { exisitingCardTitlesTable.Add(noteTitle); return noteTitle; })
            .ToHashSet();

        return setOfNoteTitles;
    }

}

internal partial class HomeViewCreatorBehaviour : ObservableObject, IBehaviour<HomePage, HashSet<string>, View>
{
    [ObservableProperty]
    private CardCollectionViewData cardCollectionViewData;


    public HomePage ObjectWithBehaviours => HomePage;
    public HomePage HomePage { get; }
    public Action<ImageCardData> OnDeleteNote { get; private set; }

    private View? cardCollectionViewForNotes;

    public HomeViewCreatorBehaviour(HomePage homePage,
        Action<ImageCardData> OnDeleteNote)
    {
        this.HomePage = homePage;
        this.OnDeleteNote = OnDeleteNote;

        CardCollectionViewData = new CardCollectionViewData(this,
               Configs.WIDTH_OF_NOTE,
               Configs.NUMBER_OF_NOTES_PER_ROW);

        ImageCardData newNoteCardData = NoteCardFactory.NewNoteCard(OnTappingNewNote);
        CardCollectionViewData.Items.Add(newNoteCardData);
    }

    // todo: this input could be the name of the note
    public View RunBehaviour(HashSet<string> setOfNoteTitles)
    {
        UpdateCardCollectionData(setOfNoteTitles);

        if (cardCollectionViewForNotes == null)
        {
            CardCollectionView cardCollectionView = new CardCollectionView();
            cardCollectionViewForNotes = cardCollectionView.GetCardCollectionView(
                CardCollectionViewData,
                new CardViewBuilder());

            CardCollectionViewData.SetBindingContextOf(cardCollectionViewForNotes);
        }

        return cardCollectionViewForNotes;
    }

    internal void UpdateCardCollectionData(HashSet<string> setOfNoteTitles)
    {
        if (setOfNoteTitles == null || setOfNoteTitles.Count == 0) return;

        HashSet<string> existingCards = CardCollectionViewData.Items
            .Select(card => card.Title)
            .ToHashSet();
        foreach (string noteTitle in setOfNoteTitles)
        {
            if (existingCards.FirstOrDefault(noteTitle.Equals) != null) continue;

            ImageCardData noteCard = NoteCardFactory.NoteCard(noteTitle, OnTappingNote);
            noteCard.UiViewForPlaceholder = new HorizontalTabsElement(OnTappingDeleteNote);
            CardCollectionViewData.Items.Add(noteCard);
        }

        existingCards.ExceptWith(setOfNoteTitles);
        if (existingCards.Count > 0)
        {
            HashSet<string> notesToRemoveFromView = existingCards;

            CardCollectionViewData.Items = CardCollectionViewData.Items.Where(card =>
                !notesToRemoveFromView.Contains(card.Title))
                .ToObservableCollection();
        }
    }

    private void OnTappingDeleteNote(object? sender, TappedEventArgs e)
    {
        var view = sender as View;
        if (Objects.IsNull(view)) return;
        ImageCardData? noteData = view.BindingContext as ImageCardData;

        if (Objects.IsNull(noteData)) return;

        OnDeleteNote(noteData);
    }


    // this should be part of the navigation component.. But i think we can leave it as it for now
    private static async Task OnTappingNewNote(ImageCardData handwrittenNote)
    {
        await NavigatePage.To<NoteTakingPage>()
                    .WithParameter("HandwrittenNoteCard", NoteCardFactory.NoteCard("", OnTappingNote))
                    .Navigate();
    }

    private static async Task OnTappingNote(ImageCardData handwrittenNote)
    {
        await NavigatePage.To<NoteTakingPage>()
                    .WithParameter("HandwrittenNoteCard", (object)handwrittenNote)
                    .Navigate();
    }
}