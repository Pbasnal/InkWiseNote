using InkWiseCore.UiComponents.UiLayouts;

namespace InkWiseCore.UiComponents.UiElements;

public class HorizontalTabsElement : IUiElement
{
    public View UiView { get; }

    public HorizontalTabsElement(EventHandler<TappedEventArgs> onTapAction)
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

        UiView = cardMenuContainer;
    }
}
