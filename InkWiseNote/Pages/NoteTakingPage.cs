using InkWiseNote.UiComponents.UiElements;
using InkWiseNote.ViewModels;

using UtilsLibrary;

namespace InkWiseNote.Pages;

[QueryProperty("HandwrittenNoteCard", "HandwrittenNoteCard")]
public class NoteTakingPage : ContentPage
{
    NoteTakingViewModel viewModel;

    public HandwrittenNoteCard HandwrittenNoteCard
    {
        set { viewModel.SetNote(value); }
    }

    public NoteTakingPage(NoteTakingViewModel viewModel)
    {
        this.viewModel = viewModel;
    }

    protected override void OnAppearing()
    {
        base.OnAppearing();

        Content = Try<View>.Executing(viewModel.GetContent)
            .HandleIfThrows(HandleContentCreationException)
            .GetResult;
    }

    protected override void OnDisappearing()
    {
        base.OnDisappearing();
        viewModel.SaveNote();
    }

    protected override bool OnBackButtonPressed()
    {
        viewModel.SaveNote();
        return base.OnBackButtonPressed();
    }


    private void HandleContentCreationException(Exception exception)
    {
        throw new NotImplementedException();
    }
}