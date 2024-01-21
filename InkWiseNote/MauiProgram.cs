using CommunityToolkit.Maui;
using CommunityToolkit.Maui.Core;
using CommunityToolkit.Maui.Markup;
using CommunityToolkit.Maui.Storage;

using InkWiseNote.Pages;
using InkWiseNote.ViewModels;

using Microsoft.Extensions.Logging;

using Systems.BackgroundJob;
using Systems.InMemoryDataStore;
using Systems.TextProcessingSystem;

namespace InkWiseNote
{
    public static class MauiProgram
    {
        public static MauiApp CreateMauiApp()
        {
            var builder = MauiApp.CreateBuilder();
            builder
                .UseMauiApp<App>()
                .UseMauiCommunityToolkit()
                .UseMauiCommunityToolkitCore()
                .UseMauiCommunityToolkitMarkup()
                .ConfigureFonts(fonts =>
                {
                    fonts.AddFont("OpenSans-Regular.ttf", "OpenSansRegular");
                    fonts.AddFont("OpenSans-Semibold.ttf", "OpenSansSemibold");
                });

            // Register Pages
            builder.Services.AddSingleton<MainPage>();
            builder.Services.AddSingleton<HomePage>();
            builder.Services.AddSingleton<NoteTakingPage>();

            // Register view models
            builder.Services.AddSingleton<HomeViewModel>();
            builder.Services.AddSingleton<NoteTakingViewModel>();

            // Custom systems
            builder.Services.AddSingleton<JobSystem>();
            builder.Services.AddSingleton<TermFrequencySystem>();

            builder.Services.AddSingleton<InMemoryDb>();
#if DEBUG
            builder.Logging.AddDebug();
#endif

            return builder.Build();
        }
    }
}
