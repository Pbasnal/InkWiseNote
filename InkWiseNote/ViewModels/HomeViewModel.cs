using CommunityToolkit.Mvvm.ComponentModel;

using InkWiseNote.Pages;
using InkWiseNote.PageUtils;
using InkWiseNote.Commons;
using InkWiseNote.UiComponents.UiElements;
using InkWiseNote.UiComponents.UiLayouts;

using Systems.SaveLoadSystem;
using UtilsLibrary;

namespace InkWiseNote.ViewModels;

public partial class HomeViewModel : ObservableObject
{
    [ObservableProperty]
    private CardCollectionViewData cardCollectionViewData;

    public HomeViewModel()
    {
        CardCollectionViewData = new CardCollectionViewData(this,
            Configs.WIDTH_OF_NOTE,
            Configs.NUMBER_OF_NOTES_PER_ROW);
    }

    internal View GetContent()
    {
        CardCollectionView cardCollectionView = new CardCollectionView();
        var cardCollectionViewForNotes = cardCollectionView.GetCardCollectionView(CardCollectionViewData, CreateImageCardView);
        CardCollectionViewData.SetBindingContextOf(cardCollectionViewForNotes);
        
        return cardCollectionViewForNotes;
    }

    // this method will get called for each data element that gets created by
    // LoadImageCardData() function
    private IUiElement CreateImageCardView()
    {
        return new ImageCardElement();
    }

    internal void LoadImageCardData(string rootDirectory)
    {
        CardCollectionViewData.Items.Clear();

        CardCollectionViewData.Items.Add(NoteCardFactory.NewNoteCard(OnTappingNote));

        LoadSystem.ListFilesFromDirectory(rootDirectory)
            .Select(NotesFileSystem.FileNameToNoteTitle)
            .Select(noteTitle => NoteCardFactory.NoteCard(noteTitle, OnTappingNote))
            .ToList()
            .ForEach(CardCollectionViewData.Items.Add);
    }

    private static async Task OnTappingNote(IHaveImageCardData handwrittenNote)
    {
        await NavigatePage.To<NoteTakingPage>()
                    .WithParameter("HandwrittenNoteCard", (object)handwrittenNote)
                    .Navigate();
    }
}
