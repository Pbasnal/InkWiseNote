using InkWiseNote.Commons;

using Systems.SaveLoadSystem;

using UtilsLibrary;

namespace Systems.TextProcessingSystem;

public class TermFrequencySystem
{
    private VocabularyTable vocabulary;
    private const string VOCABULARY_FILE_NAME = "vocabulary";

    public void LoadVocabulary()
    {
        vocabulary = SaveSystem.ReadFromFile<VocabularyTable>(Configs.VOCABULAR_FODLER_LOCATION,
            VOCABULARY_FILE_NAME);

        if (Objects.IsNull(vocabulary))
        {
            vocabulary = new VocabularyTable();
        }
    }

    public void SaveVocabulary()
    {
        vocabulary.CalculateTfIdfScores();
        SaveSystem.SaveData(Configs.VOCABULAR_FODLER_LOCATION,
           VOCABULARY_FILE_NAME, vocabulary, false);
    }

    public void RemoveDocumentFromVocabulary(Document document)
    {
        if (Objects.IsNull(vocabulary)) return;

        foreach (Term term in GetDocumentTerms(document))
        {
            Term actualTerm = vocabulary.FindTerm(term);
            if (Objects.IsNull(term)) continue;

            vocabulary.RemoveTermFrquencies(document, actualTerm);
        }
    }

    public void UpdateVocabulary(Document document)
    {
        vocabulary.InsertDocument(document);

        foreach (Term term in GetDocumentTerms(document))
        {
            vocabulary.InsertTermInVocabulary(term, document);
            vocabulary.UpdateTermFrequencyInDocument(document, term);
        }

    }

    public List<TermWithFrequency> GetTermFrequency()
    {
        return vocabulary.TermsWithFrequency;
    }

    private static IEnumerable<Term> GetDocumentTerms(Document document)
    {
        // sanitisation should be done before splitting. - removing punctuations and turning to lowercase
        return document.DocumentContent
            .Replace('\n', ' ')
            .Split(' ')
            .Select(t => new Term(t.ToLower()));
    }
}
