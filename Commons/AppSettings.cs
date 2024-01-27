namespace Commons;

public class AppSettings
{
    public static AppSettings Current = new AppSettings();

    public Color CanvasBackgroundColour { get; private set; } = Colors.White;

    public Color CanvasColor { get; private set; } = Colors.Transparent;
}
