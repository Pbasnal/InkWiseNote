using Commons.Models;

using CommunityToolkit.Mvvm.ComponentModel;

using InkWiseCore.UiComponents.UiElements;
using InkWiseCore.UiComponents.UiLayouts;

using InkWiseNote.Commons;
using InkWiseNote.Pages;
using InkWiseNote.PageUtils;

using Systems.InMemoryDataStore;
using Systems.SaveLoadSystem;
using Systems.TextProcessingSystem;

using UtilsLibrary;

namespace InkWiseNote.ViewModels;

public partial class HomeViewModel : ObservableObject
{
    [ObservableProperty]
    private CardCollectionViewData cardCollectionViewData;

    private ExisitingCardTitlesTable exisitingCardTitlesTable;

    private TermFrequencySystem termFrequencySystem;

    public HomeViewModel(InMemoryDb inMemoryDb, TermFrequencySystem termFrequencySystem)
    {
        Action setupViewData = () =>
        {
            CardCollectionViewData = new CardCollectionViewData(this,
                Configs.WIDTH_OF_NOTE,
                Configs.NUMBER_OF_NOTES_PER_ROW);

            ImageCardData newNoteCardData = NoteCardFactory.NewNoteCard(OnTappingNewNote);
            CardCollectionViewData.Items.Add(newNoteCardData);
        };

        Action setupHandlersForExternalChanges = () =>
        {
            exisitingCardTitlesTable = inMemoryDb.GetTable<ExisitingCardTitlesTable>(InMemoryDb.EXISTING_CARD_TITLES);

            exisitingCardTitlesTable.OnDataDeleteEvent += DeleteCardWithTitle;

            this.termFrequencySystem = termFrequencySystem;
        };

        setupViewData();
        setupHandlersForExternalChanges();
    }

    // View builders
    internal View GetContent()
    {
        List<string> sortedListOfNoteTitles = ReadAndSortNoteTitlesFromDisk();
        UpdateCardCollectionData(sortedListOfNoteTitles);

        CardCollectionView cardCollectionView = new CardCollectionView();
        var cardCollectionViewForNotes = cardCollectionView.GetCardCollectionView(
            CardCollectionViewData,
            new CardViewBuilder());

        CardCollectionViewData.SetBindingContextOf(cardCollectionViewForNotes);

        return cardCollectionViewForNotes;
    }

    internal List<string> ReadAndSortNoteTitlesFromDisk()
    {
        List<string> sortedNoteNames = NotesFileSystem.ListAllNotes()
            .Select(NotesFileSystem.FileNameToNoteTitle)
            .Where(noteTitle => !exisitingCardTitlesTable.Contains(noteTitle))
            .Select(noteTitle => { exisitingCardTitlesTable.Add(noteTitle); return noteTitle; })
            .ToList();

        // shouldn't be needed.
        sortedNoteNames.Sort();
        return sortedNoteNames;
    }

    internal void UpdateCardCollectionData(List<string> sortedListOfNoteTitles)
    {
        for (int i = 0; i < sortedListOfNoteTitles.Count; i++)
        {
            ImageCardData noteCard = NoteCardFactory.NoteCard(sortedListOfNoteTitles[i], OnTappingNote);
            noteCard.UiViewForPlaceholder = new HorizontalTabsElement(OnDeleteNote);

            CardCollectionViewData.Items.Add(noteCard);
        }
    }

    public void OnDeleteNote(Object? sender, TappedEventArgs e)
    {
        var view = sender as View;
        if (Objects.IsNull(view)) return;

        ImageCardData? noteData = view.BindingContext as ImageCardData;
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



    // external change handler
    private void DeleteCardWithTitle(string cardTitle)
    {
        var cardToDelete = CardCollectionViewData.Items.FirstOrDefault(card => cardTitle.Equals(card.Title));
        CardCollectionViewData.Items.Remove(cardToDelete);
    }

    private void DeleteVisionData(VisionResponse visionResponse, string noteTitle)
    {
        termFrequencySystem.LoadVocabulary();
        if (Objects.IsNotNull(visionResponse) && Objects.IsNotNull(visionResponse.readResult) && Objects.IsNotNull(visionResponse.readResult.content))
            termFrequencySystem.RemoveDocumentFromVocabulary(new Document(noteTitle, visionResponse.readResult.content));
    }
}


public class CardViewBuilder : DataTemplateSelector
{
    protected override DataTemplate OnSelectTemplate(object item, BindableObject container)
    {
        ImageCardElement imageCardElement = new ImageCardElement();

        if (item is not null && item is ImageCardData)
        {
            ImageCardData imageCardData = (ImageCardData)item;
            if (imageCardData.UiViewForPlaceholder != null)
            {
                imageCardElement.SetPlaceHolder(imageCardData.UiViewForPlaceholder);
            }
        }

        return new DataTemplate(() => imageCardElement.UiView);
    }
}
