using InkWiseNote.Commons;
using InkWiseNote.ViewModels;

using UtilsLibrary;

namespace InkWiseNote.Pages;

public class HomePage : ContentPage
{
    private HomeViewModel viewModel;

    public HomePage(HomeViewModel viewModel)
    {
        this.viewModel = viewModel;
    }

    protected override void OnAppearing()
    {
        base.OnAppearing();

        Content = Try<View>.Executing(viewModel.GetContent)
            .HandleIfThrows(HandleContentCreationException)
            .GetResult;

        viewModel.LoadNotesFrom(Configs.ROOT_DIRECTORY);
    }

    private void HandleContentCreationException(Exception exception)
    {
        throw new NotImplementedException();
    }
}