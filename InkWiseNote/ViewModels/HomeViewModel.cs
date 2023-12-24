using CommunityToolkit.Mvvm.ComponentModel;

using InkWiseNote.Pages;
using InkWiseNote.PageUtils;
using InkWiseNote.Common;
using InkWiseNote.UiComponents.UiElements;
using InkWiseNote.UiComponents.UiLayouts;

using Systems.SaveLoadSystem;

namespace InkWiseNote.ViewModels;

public partial class HomeViewModel : ObservableObject
{
    [ObservableProperty]
    private CardCollectionViewData cardCollectionViewData;

    private readonly int widthOfNote = 200;
    private int numberOfNotesPerRow = 2;

    public HomeViewModel()
    {
        CardCollectionViewData = new CardCollectionViewData(this,
            widthOfNote,
            numberOfNotesPerRow);

        CardCollectionViewData.Items.Add(new HandwrittenNote
        {
            Title = "New Note",
            ImageName = "new_note.png",
        });

    }

    internal View GetContent()
    {
        CardCollectionView cardCollectionView = new CardCollectionView();
        return cardCollectionView.GetCardCollectionView(CardCollectionViewData, new ImageCardElement());
    }

    internal void LoadNotesFrom(string rootDirectory)
    {
        CardCollectionViewData.Items.Clear();

        CardCollectionViewData.Items.Add(NewNoteCard());

        LoadSystem.ListFilesFromDirectory(rootDirectory)
            .Select(HandWrittenNoteBuilder)
            .ToList()
            .ForEach(CardCollectionViewData.Items.Add);
    }


    private static HandwrittenNote HandWrittenNoteBuilder(string noteFilePath)
    {
        return new HandwrittenNote
        {
            Title = noteFilePath.Split(Constants.PATH_FOLDER_SEPARATOR).Last().Split('.').First(),
            OnNoteTap = OnTappingNote
        };
    }

    private static HandwrittenNote NewNoteCard()
    {
        return new HandwrittenNote
        {
            Title = "",
            ImageName = "new_note.png",
            OnNoteTap = OnTappingNote,
        };
    }

    private static async Task OnTappingNote(HandwrittenNote handwrittenNote)
    {
        //if (!File.Exists(handwrittenNote.Path))
        //{
        //    var file = File.Create(handwrittenNote.Path);
        //    file.Close();
        //}
        await NavigatePage.To<NoteTakingPage>()
                    .WithParameter("HandwrittenNote", handwrittenNote)
                    .Navigate();
    }
}
