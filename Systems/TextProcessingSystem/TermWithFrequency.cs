namespace Systems.TextProcessingSystem;

public class TermWithFrequency
{
    public string Term { get; private set; }
    public string Document { get; private set; }
    public int Frequency { get; private set; }
    public float TFScore { get; private set; }
    public float IDFScore { get; private set; }

    public float TfIdfScore { get; set; }

    public TermWithFrequency(string document, string term, int frequency)
    {
        Term = term;
        Document = document;
        Frequency = frequency;
    }

    public void Update(string document, string term, int frequencyChange)
    {
        Document = document;
        Term = term;
        Frequency += frequencyChange;
    }

    public void UpdateTFScore(float score)
    {
        TFScore = score;
    }

    internal void UpdateIDFScore(float idfScore)
    {
        IDFScore = idfScore;
    }
}

