using CommunityToolkit.Maui.Markup;

using InkWiseNote.UiComponents.UiLayouts;

using Microsoft.Maui.Controls.Shapes;

using UtilsLibrary;

namespace InkWiseNote.UiComponents.UiElements;

internal class ImageCardElement : IUiElement
{
    public View UiView { get; private set; }
    private Grid gridView;

    public ImageCardElement()
    {
        gridView = GridLayoutBuilder.NewGrid()
            .HasRows(200, 50, 30)
            .HasColumns(GridLength.Star)
            //.HorizontalOptions = LayoutOptions.Fill;
            .HasChildren(GetCardImage().Row(0).Column(0))
            .HasChildren(GetCardTitleLabel().Row(1).Column(0));

        gridView.HorizontalOptions = LayoutOptions.Fill;
        //gridView.BackgroundColor = Colors.AliceBlue;

        UiView = new Border
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


        var tapGestureRecognizer = new TapGestureRecognizer();
        tapGestureRecognizer.Tapped += OnElementTap;

        UiView.GestureRecognizers.Add(tapGestureRecognizer);
    }


    private static View GetCardImage()
    {
        Image image = new();
        image.SetBinding(Image.SourceProperty, nameof(IHaveImageCardData.ImageName));

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
        titleLabel.SetBinding(Label.TextProperty, nameof(IHaveImageCardData.Title));

        return titleLabel;
    }

    public async void OnElementTap(Object? sender, TappedEventArgs e)
    {
        var view = sender as View;
        if (Objects.IsNull(view)) return;

        IHaveImageCardData? imageCardData = view.BindingContext as IHaveImageCardData;
        if (Objects.IsNull(imageCardData)) return;

        await imageCardData.OnNoteTap(imageCardData);
    }

    public void SetPlaceHolder(View element)
    {
        gridView.Children.Add(element.Row(2).Column(0));
    }
}
