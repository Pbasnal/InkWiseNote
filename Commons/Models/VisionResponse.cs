namespace Commons.Models;

public class VisionResponse
{
    public ReadResult readResult { get; set; }
    public string modelVersion { get; set; }
    public Metadata metadata { get; set; }
}

public class Line
{
    public string content { get; set; }
    public List<double> boundingBox { get; set; }
    public List<Span> spans { get; set; }
}

public class Metadata
{
    public int width { get; set; }
    public int height { get; set; }
}

public class Page
{
    public double height { get; set; }
    public double width { get; set; }
    public double angle { get; set; }
    public int pageNumber { get; set; }
    public List<Word> words { get; set; }
    public List<Span> spans { get; set; }
    public List<Line> lines { get; set; }
}

public class ReadResult
{
    public string stringIndexType { get; set; }
    public string content { get; set; }
    public List<Page> pages { get; set; }
    public List<Style> styles { get; set; }
    public string modelVersion { get; set; }
}



public class Span
{
    public int offset { get; set; }
    public int length { get; set; }
}

public class Span2
{
    public int offset { get; set; }
    public int length { get; set; }
}

public class Style
{
    public bool isHandwritten { get; set; }
    public List<Span> spans { get; set; }
    public double confidence { get; set; }
}

public class Word
{
    public string content { get; set; }
    public List<double> boundingBox { get; set; }
    public double confidence { get; set; }
    public Span span { get; set; }
}
