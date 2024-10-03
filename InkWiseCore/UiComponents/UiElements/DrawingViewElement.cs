using CommunityToolkit.Maui.Core;
using CommunityToolkit.Maui.Core.Views;
using CommunityToolkit.Maui.Views;
using CommunityToolkit.Mvvm.ComponentModel;

using InkWiseCore.UiComponents.UiLayouts;

using InkWiseNote.Commons;

using System.Collections.ObjectModel;

namespace InkWiseCore.UiComponents.UiElements;

public class DrawingViewElement : IUiElement
{
    public View UiView { get; private set; }

    public DrawingView DrawingView => (DrawingView) UiView;

    public DrawingViewElement()
    {
        UiView = new DrawingView
        {
            IsMultiLineModeEnabled = true,
            ShouldClearOnFinish = false
        };

        UiView.SetBinding(DrawingView.LinesProperty, nameof(DrawingCanvasData.Lines));
        UiView.SetBinding(DrawingView.LineColorProperty, nameof(DrawingCanvasData.LineColor));
        UiView.SetBinding(DrawingView.BackgroundColorProperty, nameof(DrawingCanvasData.BackgroundColor));
        UiView.SetBinding(DrawingView.LineWidthProperty, nameof(DrawingCanvasData.LineWidth));
    }
}


public partial class DrawingCanvasData : ObservableObject
{
    [ObservableProperty]
    public ObservableCollection<IDrawingLine> lines;

    [ObservableProperty]
    public Color lineColor;

    [ObservableProperty]
    public Color backgroundColor;

    [ObservableProperty]
    public int lineWidth;

    public DrawingCanvasData()
    {
        Lines = new ObservableCollection<IDrawingLine>();
        LineColor = Colors.BlueViolet;
        BackgroundColor = Colors.White;
        LineWidth = Configs.DEFAULT_LINE_WIDTH;
    }

    public static DrawingCanvasData FromCanvasModel(CanvasModel canvasModel)
    {
        var canvasData = new DrawingCanvasData
        {
            LineColor = canvasModel.LineColor,
            BackgroundColor = canvasModel.BackgroundColor,
            LineWidth = canvasModel.LineWidth,
            Lines = []
        };

        for (int i = 0; i < canvasModel.PointCollection.Length; i++)
        {
            var drawingLine = new DrawingLine
            {
                LineWidth = canvasModel.LineWidth,
                LineColor = canvasModel.LineColor,
                Points = []
            };

            var pointsOfLine = canvasModel.PointCollection[i];
            for (int j = 0; j < pointsOfLine.Length; j++)
            {
                drawingLine.Points.Add(pointsOfLine[j]);
            }

            canvasData.Lines.Add(drawingLine);
        }


        return canvasData;
    }
}


public class CanvasModel
{
    public Color LineColor { get; set; }
    public Color BackgroundColor { get; set; }
    public int LineWidth { get; set; }
    public PointF[][] PointCollection { get; set; }

    public static CanvasModel FromCanvasData(DrawingCanvasData drawingCanvasData)
    {
        var model = new CanvasModel
        {
            LineColor = drawingCanvasData.LineColor,
            BackgroundColor = drawingCanvasData.BackgroundColor,
            LineWidth = drawingCanvasData.LineWidth,
            PointCollection = new PointF[drawingCanvasData.Lines.Count][]
        };

        int pointCollectionIndex = 0;
        foreach (var line in drawingCanvasData.Lines)
        {
            model.PointCollection[pointCollectionIndex] = new PointF[line.Points.Count];

            int pointIndex = 0;
            foreach (var point in line.Points)
            {
                model.PointCollection[pointCollectionIndex][pointIndex] = point;
                pointIndex++;
            }
            pointCollectionIndex++;
        }

        return model;
    }
}