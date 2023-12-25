using InkWiseNote.Pages;
using InkWiseNote.PageUtils;

namespace InkWiseNote
{
    public partial class MainPage : ContentPage
    {
        int count = 0;
        
        public MainPage()
        {
            InitializeComponent();
        }

        protected async override void OnAppearing()
        {
            base.OnAppearing();

            PermissionStatus storageReadStatus = PermissionStatus.Unknown;
            while (storageReadStatus != PermissionStatus.Granted)
            {
                storageReadStatus = await Permissions.RequestAsync<Permissions.StorageRead>();
            }

            PermissionStatus storageWriteStatus = PermissionStatus.Unknown;
            while (storageWriteStatus != PermissionStatus.Granted)
            {
                storageWriteStatus = await Permissions.RequestAsync<Permissions.StorageWrite>();
            }

            await NavigatePage
                 .To<HomePage>()
                 .Navigate();
        }



        private void OnCounterClicked(object sender, EventArgs e)
        {
            count++;

            if (count == 1)
                CounterBtn.Text = $"Clicked {count} time";
            else
                CounterBtn.Text = $"Clicked {count} times";

            SemanticScreenReader.Announce(CounterBtn.Text);
        }
    }

}
