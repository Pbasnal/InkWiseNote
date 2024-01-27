using Commons.Models;

using InkWiseNote.Commons;

using Systems.TextProcessingSystem;

namespace InkWiseCore.NotesFuncationalities;

public static class NotesKeywords
{
    public static IDictionary<string, HashSet<string>> GetNotesGroupedByKeywords(TermFrequencySystem termFrequencySystem)
    {
        return termFrequencySystem.GetTermFrequency()
            .Where(tf => tf.TfIdfScore > Configs.MINIMUM_TF_IDF_SCORE)
            .GroupBy(tf => tf.Term)
            .ToDictionary(group => group.Key, GetRelatedNotes);
    }

    public static IDictionary<string, HashSet<string>> RelateNotesByCommonKeywords(IDictionary<string, HashSet<string>> notesGroupedOnKeywords) {
        Dictionary<string, HashSet<string>> allRelatedNotes = new ();

        foreach (string keyword in notesGroupedOnKeywords.Keys)
        {
            foreach (string noteName in notesGroupedOnKeywords[keyword])
            {
                if (!allRelatedNotes.ContainsKey(noteName))
                {
                    allRelatedNotes.Add(noteName, new HashSet<string>());
                }

                HashSet<string> relatedNotes = new(notesGroupedOnKeywords[keyword]);
                relatedNotes.Remove(noteName);

                allRelatedNotes[noteName].UnionWith(relatedNotes);
            }
        }

        return allRelatedNotes;
    }

    public static void RemoveNoteFromVocabulary(string noteName, TermFrequencySystem termFrequencySystem)
    {
        (bool isVisionLoaded, VisionResponse visionResponse) = OcrFunctionalities.LoadVisionResponse(noteName);
        if (isVisionLoaded)
        {
            termFrequencySystem.RemoveDocumentFromVocabulary(new Document(noteName, visionResponse.readResult.content));
        }
    }

    public static void UpdateNoteInVocabulary(string noteTitle,
      string noteContent,
      TermFrequencySystem termFrequencySystem)
    {
        Document document = new Document(noteTitle, noteContent);
        termFrequencySystem.UpdateVocabulary(document);

        termFrequencySystem.SaveVocabulary();
    }

    private static HashSet<string> GetRelatedNotes(IGrouping<string, TermWithFrequency> grouping)
    {
        return grouping
            .Select(tf => tf.Document)
            .Where(documentName => documentName != grouping.Key)
            .ToHashSet();
    }
}
