using CommunityToolkit.Mvvm.ComponentModel;

using InkWiseNote.Commons;
using InkWiseNote.Pages;
using InkWiseNote.PageUtils;
using InkWiseNote.UiComponents.UiElements;
using InkWiseNote.UiComponents.UiLayouts;

using Systems.SaveLoadSystem;

using UtilsLibrary;

namespace InkWiseNote.ViewModels;

public partial class HomeViewModel : ObservableObject
{
    [ObservableProperty]
    private CardCollectionViewData cardCollectionViewData;

    private HashSet<string> existingCardTitles;

    public HomeViewModel()
    {
        CardCollectionViewData = new CardCollectionViewData(this,
            Configs.WIDTH_OF_NOTE,
            Configs.NUMBER_OF_NOTES_PER_ROW);

        existingCardTitles = new HashSet<string>();
        CardCollectionViewData.Items.Add(NoteCardFactory.NewNoteCard(OnTappingNewNote));
    }

    internal View GetContent()
    {
        CardCollectionView cardCollectionView = new CardCollectionView();
        var cardCollectionViewForNotes = cardCollectionView.GetCardCollectionView(CardCollectionViewData,
            new CardViewTemplateBuilder());
        CardCollectionViewData.SetBindingContextOf(cardCollectionViewForNotes);

        return cardCollectionViewForNotes;
    }

    internal void LoadImageCardData(string rootDirectory)
    {
        NotesFileSystem.CreateRootDirectoryIfNotExists(rootDirectory);

        LoadSystem.ListFilesFromDirectory(rootDirectory)
            .Select(NotesFileSystem.FileNameToNoteTitle)
            .Where(noteTitle => !existingCardTitles.Contains(noteTitle))
            .Select(noteTitle => { existingCardTitles.Add(noteTitle); return noteTitle; })
            .Select(noteTitle => NoteCardFactory.NoteCard(noteTitle, OnTappingNote))
            .ToList()
            .ForEach(CardCollectionViewData.Items.Add);
    }

    private static async Task OnTappingNewNote(IHaveImageCardData handwrittenNote)
    {
        await NavigatePage.To<NoteTakingPage>()
                    .WithParameter("HandwrittenNoteCard", NoteCardFactory.NoteCard("", OnTappingNote))
                    .Navigate();
    }

    private static async Task OnTappingNote(IHaveImageCardData handwrittenNote)
    {
        await NavigatePage.To<NoteTakingPage>()
                    .WithParameter("HandwrittenNoteCard", (object)handwrittenNote)
                    .Navigate();
    }
}


public class CardViewTemplateBuilder : DataTemplateSelector
{
    public DataTemplate AmericanMonkey { get; set; }
    public DataTemplate OtherMonkey { get; set; }

    protected override DataTemplate OnSelectTemplate(object item, BindableObject container)
    {
        return If.Condition(item is NewNoteCard)
            .IsTrue(NewNoteDataTemplate())
            .OrElse(NoteDataTemplate());
    }

    private static DataTemplate NewNoteDataTemplate() =>
        new DataTemplate(() => new ImageCardElement().UiView);

    private static DataTemplate NoteDataTemplate()
    {

        var cardMenuContainer = new VerticalStackLayout();
        var dividerBetweenMenuAndCard = new BoxView
        {
            HeightRequest = 1,
            Color = Colors.LightGray
        };

        var cardMenu = new HorizontalStackLayout
        {
            HorizontalOptions = LayoutOptions.Center,
        };
        var deleteMenuOption = new Label
        {
            Text = "Delete",
        };

        cardMenu.Children.Add(deleteMenuOption);
        cardMenuContainer.Children.Add(dividerBetweenMenuAndCard);
        cardMenuContainer.Children.Add(cardMenu);

        return new DataTemplate(() =>
        {
            var cardView = new ImageCardElement();

            cardView.SetPlaceHolder(cardMenuContainer);

            return cardView.UiView;
        });
    }
}
