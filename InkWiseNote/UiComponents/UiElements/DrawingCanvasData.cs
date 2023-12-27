using System.Collections.ObjectModel;

using CommunityToolkit.Maui.Core;
using CommunityToolkit.Mvvm.ComponentModel;

namespace InkWiseNote.UiComponents.UiElements;

public partial class DrawingCanvasData : ObservableObject, IHaveDrawingViewData
{
    [ObservableProperty]
    public ObservableCollection<IDrawingLine> lines;

    [ObservableProperty]
    public Color lineColor;

    [ObservableProperty]
    public Color backgroundColor;

    [ObservableProperty]
    public float lineWidth;

    public DrawingCanvasData()
    {
        Lines = new ObservableCollection<IDrawingLine>();
        LineColor = Colors.Black;
        BackgroundColor = Colors.White;
        LineWidth = 2;
    }

}
