namespace InkWiseNote.PageUtils;

internal class NavigatePage
{
    private IDictionary<string, object> navigationParameters;
    private string destinationPageName;

    private NavigatePage() { }

    public static NavigatePage To<T>()
    {
        var navigation = new NavigatePage();
        navigation.destinationPageName = typeof(T).Name;
        navigation.navigationParameters = new Dictionary<string, object>();

        return navigation;
    }

    public NavigatePage WithParameter(string key, object value)
    {
        navigationParameters.Add(key, value);
        return this;
    }

    public async Task Navigate()
    {
        if (navigationParameters.Count == 0)
        {
            await Shell.Current.GoToAsync($"{destinationPageName}");
        }
        else
        {
            await Shell.Current.GoToAsync($"{destinationPageName}", navigationParameters);
        }
    }
}
