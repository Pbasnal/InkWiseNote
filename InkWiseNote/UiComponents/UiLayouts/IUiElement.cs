namespace InkWiseNote.UiComponents.UiLayouts;

internal interface IUiElement
{
    public View UiView { get; }
    public IUiElement InstantiateElement();

    public Task OnElementTap(View view, TappedEventArgs e);
}