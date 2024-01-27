using System.Collections.ObjectModel;

using CommunityToolkit.Mvvm.ComponentModel;

using InkWiseCore.UiComponents.UiElements;

namespace InkWiseCore.UiComponents.UiLayouts;

public partial class CardCollectionViewData : ObservableObject
{
    public int WidthOfNote { get; }


    [ObservableProperty]
    public ObservableCollection<IHaveImageCardData> items;

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
        this.items = new ObservableCollection<IHaveImageCardData>();
    }

    public void SetBindingContextOf(BindableObject bindableObject)
    {
        bindableObject.BindingContext = this;
    }
}