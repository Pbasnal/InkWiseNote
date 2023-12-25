namespace InkWiseNote.UiComponents.UiLayouts;

internal class CardCollectionView
{
    public View GetCardCollectionView(CardCollectionViewData cardCollectionViewData,
        DataTemplateSelector cardViewSelector)
    {
        var notePageCollection = BuildCollectionView(nameof(cardCollectionViewData.NumberOfNotesPerRow));

        AddItemsToCollection(notePageCollection,
            nameof(cardCollectionViewData.Items),
            cardViewSelector);

        notePageCollection.SizeChanged += (object sender, EventArgs e) =>
            Resize(cardCollectionViewData, notePageCollection.Width);

        cardCollectionViewData.SetBindingContextOf(notePageCollection);
        return notePageCollection;

    }

    private CollectionView BuildCollectionView(string propertyName)
    {
        var gridItemsLayout = new GridItemsLayout(ItemsLayoutOrientation.Vertical);
        gridItemsLayout.SetBinding(GridItemsLayout.SpanProperty, propertyName);

        return new CollectionView
        {
            ItemsLayout = gridItemsLayout
        };

    }

    private void AddItemsToCollection(CollectionView notePageCollection,
        string propertyName,
        DataTemplateSelector cardViewSelector)
    {
        notePageCollection.SetBinding(ItemsView.ItemsSourceProperty, propertyName);
        notePageCollection.ItemTemplate = cardViewSelector;
    }

    private void Resize(CardCollectionViewData cardCollectionViewData, double width)
    {
        if (width == 0) return;

        var numberOfNotesToShowPerRow = (int)width / cardCollectionViewData.WidthOfNote;
        if (cardCollectionViewData.NumberOfNotesPerRow == numberOfNotesToShowPerRow) return;

        cardCollectionViewData.NumberOfNotesPerRow = numberOfNotesToShowPerRow;
    }



}
