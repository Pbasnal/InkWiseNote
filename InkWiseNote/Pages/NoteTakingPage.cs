using InkWiseNote.UiComponents.UiElements;
using InkWiseNote.ViewModels;

using UtilsLibrary;

namespace InkWiseNote.Pages;

[QueryProperty("HandwrittenNoteCard", "HandwrittenNoteCard")]
public class NoteTakingPage : ContentPage
{
    NoteTakingViewModel viewModel;
    bool isSaving = false;

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
        isSaving = false;

        viewModel.OnAppearing();

        Content = Try<View>.Executing(viewModel.GetContent)
            .HandleIfThrows(HandleContentCreationException)
            .GetResult;
    }

    protected override void OnDisappearing()
    {
        base.OnDisappearing();
        if (!isSaving)
        {
            isSaving = true;
            viewModel.SaveNote();
        }
    }

    protected override bool OnBackButtonPressed()
    {
        if (!isSaving)
        {
            isSaving = true;
            viewModel.SaveNote();
        }
        return base.OnBackButtonPressed();
    }


    private void HandleContentCreationException(Exception exception)
    {
        throw new NotImplementedException();
    }
}