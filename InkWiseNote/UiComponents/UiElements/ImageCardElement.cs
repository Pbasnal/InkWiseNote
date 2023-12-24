using InkWiseNote.Pages;
using InkWiseNote.PageUtils;
using InkWiseNote.UiComponents.UiLayouts;

using Microsoft.Maui.Controls.Shapes;

using UtilsLibrary;

namespace InkWiseNote.UiComponents.UiElements;

internal class ImageCardElement : IUiElement
{
    public View UiView => border;

    private Grid gridView;
    private Border border;

    public ImageCardElement()
    {
        gridView = new Grid();

        gridView.RowDefinitions.Add(new RowDefinition { Height = new GridLength(200) });
        gridView.RowDefinitions.Add(new RowDefinition { Height = new GridLength(50) });
        gridView.RowDefinitions.Add(new RowDefinition { Height = GridLength.Auto });

        gridView.ColumnDefinitions.Add(new ColumnDefinition { Width = GridLength.Star });

        gridView.HorizontalOptions = LayoutOptions.Fill;
        //gridView.BackgroundColor = Colors.AliceBlue;

        border = new Border
        {
            Content = gridView,
            StrokeShape = new RoundRectangle
            {
                CornerRadius = new CornerRadius(10, 10, 10, 10)
            },
            Margin = new Thickness(5),
            BackgroundColor = Colors.Grey,
            HorizontalOptions = LayoutOptions.Fill,
        };
    }

    public static ImageCardElement GetImageCard()
    {
        var imageCard = new ImageCardElement();

        imageCard.gridView.Add(GetCardImage(), column: 0, row: 0);
        imageCard.gridView.Add(GetCardTitleLabel(), column: 0, row: 1);

        return imageCard;
    }

    private static View GetCardImage()
    {
        Image image = new();
        image.SetBinding(Image.SourceProperty, nameof(IHaveImageWithTitle.ImageName));

        return image;
    }

    private static View GetCardTitleLabel()
    {
        Label titleLabel = new()
        {
            FontAttributes = FontAttributes.Bold,
            TextColor = Colors.White,
            LineBreakMode = LineBreakMode.WordWrap,
            HorizontalTextAlignment = TextAlignment.Center,
            VerticalTextAlignment = TextAlignment.Center,
            HorizontalOptions = LayoutOptions.Fill
        };
        titleLabel.SetBinding(Label.TextProperty, nameof(IHaveImageWithTitle.Title));

        return titleLabel;
    }

    public IUiElement InstantiateElement()
    {
        var imageCard = new ImageCardElement();

        imageCard.gridView.Add(GetCardImage(), column: 0, row: 0);
        imageCard.gridView.Add(GetCardTitleLabel(), column: 0, row: 1);

        return imageCard;
    }

    public async Task OnElementTap(View view, TappedEventArgs e)
    {
        HandwrittenNote? handwrittenNote = view.BindingContext as HandwrittenNote;
        if (Objects.IsNull(handwrittenNote)) return;

        await handwrittenNote.OnNoteTap(handwrittenNote);
        
    }
}
