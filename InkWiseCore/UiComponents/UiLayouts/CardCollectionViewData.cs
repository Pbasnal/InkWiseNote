using System.Collections.ObjectModel;

using CommunityToolkit.Mvvm.ComponentModel;

using InkWiseCore.UiComponents.UiElements;

namespace InkWiseCore.UiComponents.UiLayouts;

public partial class CardCollectionViewData : ObservableObject
{
    public int WidthOfNote { get; }


    [ObservableProperty]
    public ObservableCollection<ImageCardData> items;

    [ObservableProperty]
    public int numberOfNotesPerRow;

    private ObservableObject parent;

    public CardCollectionViewData(ObservableObject parent, 
        int widthOfNote, 
        int numberOfNotesPerRow)
    {
        this.parent = parent;
        this.WidthOfNote = widthOfNote;
        this.NumberOfNotesPerRow = numberOfNotesPerRow; ;
        this.items = new ObservableCollection<ImageCardData>();
    }

    public void SetBindingContextOf(BindableObject bindableObject)
    {
        bindableObject.BindingContext = this;
    }
}