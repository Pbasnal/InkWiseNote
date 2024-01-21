using UtilsLibrary;

namespace Systems.TextProcessingSystem;

public class VocabularyTable
{
    // To get IDs of terms and documents, search will be happening on the terms and 
    // documents array. A better datastructure will be needed to handle faster search.
    // Or the terms should be sorted to apply binary search
    public List<Term> Terms { get; }

    // Document table is needed because this library should be
    // able to handle CRUD operations on a document
    public List<Document> Documents { get; }

    public List<TermWithFrequency> TermsWithFrequency { get; }

    // These arrays need to handle size changes as well!
    public VocabularyTable()
    {
        Terms = new();
        Documents = new();
        TermsWithFrequency = new();
    }

    public void CalculateTfIdfScores()
    {
        foreach (var termWithFrequency in TermsWithFrequency)
        {
            termWithFrequency.TfIdfScore = termWithFrequency.TFScore * termWithFrequency.IDFScore;
        }

        TermsWithFrequency.Sort((t1, t2) => t2.TfIdfScore.CompareTo(t1.TfIdfScore));
    }

    public void InsertDocument(Document document)
    {
        if (Documents.Any(doc => doc.DocumentName == document.DocumentName)) return;
        Documents.Add(document);
        Documents.Sort(CompareDocuments);
    }

    public void InsertTermInVocabulary(Term term, Document document)
    {
        if (!Terms.Any(t => t.TermValue == term.TermValue))
        {
            Terms.Add(term);
            Terms.Sort(CompareTerms);
        }
        
        term.DocumentIdsContainingTerm.Add(document.DocumentName);
    }

    public void UpdateTermFrequencyInDocument(Document document, Term term)
    {
        TermWithFrequency termWithFrequency = FindTermFrequency(document, term);

        If.Condition(Objects.IsNull(termWithFrequency))
            .RunIfTrue(() =>
            {
                termWithFrequency = new TermWithFrequency(document.DocumentName, term.TermValue, 1);
                TermsWithFrequency.Add(termWithFrequency);
            })
            .OrElse(() => termWithFrequency.Update(document.DocumentName, term.TermValue, 1));

        float tfScore = (float)termWithFrequency.Frequency / document.TotalTerms;
        termWithFrequency.UpdateTFScore(tfScore);

        // +1 in (term.DocumentIdsContainingTerm.Count + 1) is to avoid divide by zero error
        float idfScore = MathF.Log2(Documents.Count / (term.DocumentIdsContainingTerm.Count + 1));
        idfScore = If.Condition(idfScore < 0)
            .IsTrue(0f)
            .OrElse(idfScore);
        termWithFrequency.UpdateIDFScore(idfScore);
    }

    public void RemoveTermFrquencies(Document document, Term term)
    {
        TermWithFrequency termWithFrequency = FindTermFrequency(document, term);
        if (Objects.IsNull(termWithFrequency) || Objects.IsNull(term)) return;

        // soft delete by setting frequency to 0
        int currentFrequencyOfTheTerm = termWithFrequency.Frequency;
        termWithFrequency.Update(document.DocumentName, term.TermValue, -currentFrequencyOfTheTerm);
        termWithFrequency.UpdateTFScore(0);

        // also remove the document ref from term
        term.DocumentIdsContainingTerm.Remove(document.DocumentName);
    }

    public TermWithFrequency FindTermFrequency(Document document, Term term)
    {
        for (int i = 0; i < TermsWithFrequency.Count; i++)
        {
            if (TermsWithFrequency[i].Document == document.DocumentName && TermsWithFrequency[i].Term == term.TermValue)
            {
                return TermsWithFrequency[i];
            }
        }
        return null;
    }

    public Term FindTerm(Term term)
    {
        return Terms.Find(t => t.TermValue.Equals(term.TermValue));
    }

    private static int CompareDocuments(Document doc1, Document doc2)
    {
        if (string.IsNullOrWhiteSpace(doc1.DocumentName)) return 1;

        return doc1.DocumentName.CompareTo(doc2.DocumentName);
    }

    private static int CompareTerms(Term x, Term y)
    {
        if (string.IsNullOrWhiteSpace(x.TermValue)) return 1;

        return x.TermValue.CompareTo(y.TermValue);
    }
}

