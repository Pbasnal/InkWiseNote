using CommunityToolkit.Mvvm.ComponentModel;

using InkWiseNote.Pages;
using InkWiseNote.PageUtils;
using InkWiseNote.Commons;
using InkWiseNote.UiComponents.UiElements;
using InkWiseNote.UiComponents.UiLayouts;

using Systems.SaveLoadSystem;

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
        return cardCollectionView.GetCardCollectionView(CardCollectionViewData, new ImageCardElement());
    }

    internal void LoadNotesFrom(string rootDirectory)
    {
        CardCollectionViewData.Items.Clear();

        CardCollectionViewData.Items.Add(HandwrittenNoteCardFactory.NewNoteCard(OnTappingNote));

        LoadSystem.ListFilesFromDirectory(rootDirectory)
            .Select(NotesFileSystem.FileNameToNoteTitle)
            .Select(noteTitle => HandwrittenNoteCardFactory.NoteCard(noteTitle, OnTappingNote))
            .ToList()
            .ForEach(CardCollectionViewData.Items.Add);
    }


    private static async Task OnTappingNote(HandwrittenNoteCard handwrittenNote)
    {
        await NavigatePage.To<NoteTakingPage>()
                    .WithParameter("HandwrittenNoteCard", handwrittenNote)
                    .Navigate();
    }
}
