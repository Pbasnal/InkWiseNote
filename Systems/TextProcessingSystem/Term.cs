namespace Systems.TextProcessingSystem;

public class Term
{
    public string TermValue { get; }
    public HashSet<string> DocumentIdsContainingTerm { get; }

    public Term(string termValue)
    {
        TermValue = termValue;
        DocumentIdsContainingTerm = new HashSet<string>();
    }
    public override string ToString()
    {
        return TermValue;
    }
}

