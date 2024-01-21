namespace Systems.TextProcessingSystem;

public class Document 
{
    public string DocumentName { get; }
    public string DocumentContent { get; }
    public int TotalTerms { get; }

    public Document(string documentName, string documentContent)
    {
        DocumentName = documentName;
        DocumentContent = documentContent;

        TotalTerms = documentContent.Split(' ').Length;
    }

    public override string ToString()
    {
        return DocumentName;
    }
}

