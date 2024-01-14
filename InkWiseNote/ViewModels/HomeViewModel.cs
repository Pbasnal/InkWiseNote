using CommunityToolkit.Mvvm.ComponentModel;

using InkWiseNote.Commons;
using InkWiseNote.Pages;
using InkWiseNote.PageUtils;
using InkWiseNote.UiComponents.UiElements;
using InkWiseNote.UiComponents.UiLayouts;

using Systems.InMemoryDataStore;
using Systems.SaveLoadSystem;

using UtilsLibrary;

namespace InkWiseNote.ViewModels;

public partial class HomeViewModel : ObservableObject
{
    [ObservableProperty]
    private CardCollectionViewData cardCollectionViewData;

    private ExisitingCardTitlesTable exisitingCardTitlesTable;

    public HomeViewModel(InMemoryDb inMemoryDb)
    {
        CardCollectionViewData = new CardCollectionViewData(this,
            Configs.WIDTH_OF_NOTE,
            Configs.NUMBER_OF_NOTES_PER_ROW);

        CardCollectionViewData.Items.Add(NoteCardFactory.NewNoteCard(OnTappingNewNote));

        exisitingCardTitlesTable = inMemoryDb.GetTable<ExisitingCardTitlesTable>(InMemoryDb.EXISTING_CARD_TITLES);

        exisitingCardTitlesTable.OnDataDeleteEvent += DeleteCardWithTitle;
    }

    private void DeleteCardWithTitle(string cardTitle)
    {
        var cardToDelete = CardCollectionViewData.Items.FirstOrDefault(card => cardTitle.Equals(card.Title));
        CardCollectionViewData.Items.Remove(cardToDelete);
    }

    internal View GetContent()
    {
        CardCollectionView cardCollectionView = new CardCollectionView();
        var cardCollectionViewForNotes = cardCollectionView.GetCardCollectionView(CardCollectionViewData,
            new CardViewTemplateBuilder(OnDeleteNote));
        CardCollectionViewData.SetBindingContextOf(cardCollectionViewForNotes);

        return cardCollectionViewForNotes;
    }

    public void OnDeleteNote(Object? sender, TappedEventArgs e)
    {
        var view = sender as View;
        if (Objects.IsNull(view)) return;

        HandwrittenNoteCard? noteData = view.BindingContext as HandwrittenNoteCard;
        if (Objects.IsNull(noteData)) return;

        NotesFileSystem.DeleteNote(noteData.Path);

        var noteTitle = noteData.Title;
        exisitingCardTitlesTable.Remove(noteTitle);
    }

    internal void LoadImageCardData(string rootDirectory)
    {
        NotesFileSystem.CreateRootDirectoryIfNotExists(rootDirectory);

        NotesFileSystem.ListFilesFromDirectory(rootDirectory)
            .Select(NotesFileSystem.FileNameToNoteTitle)
            .Where(noteTitle => !exisitingCardTitlesTable.Contains(noteTitle))
            .Select(noteTitle => { exisitingCardTitlesTable.Add(noteTitle); return noteTitle; })
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
    private EventHandler<TappedEventArgs> onTapAction;

    public DataTemplate AmericanMonkey { get; set; }
    public DataTemplate OtherMonkey { get; set; }

    

    public CardViewTemplateBuilder(EventHandler<TappedEventArgs> onTapAction)
    {
        this.onTapAction = onTapAction;
    }

    protected override DataTemplate OnSelectTemplate(object item, BindableObject container)
    {
        return If.Condition(item is NewNoteCard)
            .IsTrue(NewNoteDataTemplate())
            .OrElse(NoteDataTemplate());
    }

    private DataTemplate NewNoteDataTemplate() =>
        new DataTemplate(() => new ImageCardElement().UiView);

    private DataTemplate NoteDataTemplate()
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


        var tapGestureRecognizer = new TapGestureRecognizer();
        tapGestureRecognizer.Tapped += onTapAction;

        deleteMenuOption.GestureRecognizers.Add(tapGestureRecognizer);

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
