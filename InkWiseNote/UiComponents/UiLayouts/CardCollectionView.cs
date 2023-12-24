using UtilsLibrary;

namespace InkWiseNote.UiComponents.UiLayouts;

internal class CardCollectionView
{
    public View GetCardCollectionView(CardCollectionViewData cardCollectionViewData, IUiElement cardTemplate)
    {
        var notePageCollection = BuildCollectionView(cardCollectionViewData);

        AddItemsToCollection(notePageCollection, 
            nameof(cardCollectionViewData.Items),
            () => CreateCardElement(cardTemplate));

        notePageCollection.SizeChanged += (object sender, EventArgs e) =>
            Resize(cardCollectionViewData, notePageCollection.Width);

        cardCollectionViewData.SetBindingContextOf(notePageCollection);
        return notePageCollection;

    }

    private void AddItemsToCollection(CollectionView notePageCollection,
        string propertyName,
        Func<IUiElement> elementFactory)
    {
        notePageCollection.SetBinding(ItemsView.ItemsSourceProperty, propertyName);
        notePageCollection.ItemTemplate = new DataTemplate(() => elementFactory().UiView);
    }

    private IUiElement CreateCardElement(IUiElement cardTemplate)
    {
        IUiElement cardElement = cardTemplate.InstantiateElement();
        var tapGestureRecognizer = new TapGestureRecognizer();
        tapGestureRecognizer.Tapped += async (s, e) =>
        {
            var handwrittenNoteGrid = s as View;
            if (Objects.IsNull(handwrittenNoteGrid)) return;

            await cardElement.OnElementTap(handwrittenNoteGrid, e);
        };
        cardElement.UiView.GestureRecognizers.Add(tapGestureRecognizer);

        return cardElement;
    }

    private void Resize(CardCollectionViewData cardCollectionViewData, double width)
    {
        if (width == 0) return;

        var numberOfNotesToShowPerRow = (int)width / cardCollectionViewData.WidthOfNote;
        if (cardCollectionViewData.NumberOfNotesPerRow == numberOfNotesToShowPerRow) return;

        cardCollectionViewData.NumberOfNotesPerRow = numberOfNotesToShowPerRow;
    }


    private CollectionView BuildCollectionView(CardCollectionViewData cardCollectionViewData)
    {
        var gridItemsLayout = new GridItemsLayout(ItemsLayoutOrientation.Vertical);
        gridItemsLayout.SetBinding(GridItemsLayout.SpanProperty, nameof(cardCollectionViewData.NumberOfNotesPerRow));

        return new CollectionView
        {
            ItemsLayout = gridItemsLayout
        };

    }
}
