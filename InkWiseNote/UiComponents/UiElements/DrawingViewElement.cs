using System.Collections.ObjectModel;

using CommunityToolkit.Maui.Core;
using CommunityToolkit.Maui.Views;

using InkWiseNote.UiComponents.UiLayouts;

namespace InkWiseNote.UiComponents.UiElements;

public interface IHaveDrawingViewData
{
    ObservableCollection<IDrawingLine> Lines { get; }
    Color LineColor { get; }

    Color BackgroundColor { get; }
    float LineWidth { get; }
}

public class DrawingViewElement : IUiElement
{
    public View UiView { get; private set; }

    public DrawingViewElement()
    {
        UiView = new DrawingView
        {
            IsMultiLineModeEnabled = true,
            ShouldClearOnFinish = false,
        };

        UiView.SetBinding(DrawingView.LinesProperty, nameof(IHaveDrawingViewData.Lines));
        UiView.SetBinding(DrawingView.LineColorProperty, nameof(IHaveDrawingViewData.LineColor));
        UiView.SetBinding(DrawingView.BackgroundColorProperty, nameof(IHaveDrawingViewData.BackgroundColor));
        UiView.SetBinding(DrawingView.LineWidthProperty, nameof(IHaveDrawingViewData.LineWidth));
    }
}
