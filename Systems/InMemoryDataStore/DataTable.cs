

namespace Systems.InMemoryDataStore;

public class InMemoryDb
{

    public Dictionary<int, DataTable> dataTables = new Dictionary<int, DataTable>();

    public const int EMPTY_TABLE_ID = 0;
    public const int EXISTING_CARD_TITLES = 1;

    public InMemoryDb()
    {
        dataTables.Add(EMPTY_TABLE_ID, new DataTable(EMPTY_TABLE_ID));
        dataTables.Add(EXISTING_CARD_TITLES, new ExisitingCardTitlesTable(EXISTING_CARD_TITLES));
    }

    public T GetTable<T>(int tableId) where T : DataTable
    {
        if (dataTables.ContainsKey(tableId))
        {
            return (T)dataTables[tableId];
        }

        return default;
    }

}

public class DataTable
{
    public int tableId { get; internal set; }

    public DataTable(int tableId)
    {
        this.tableId = tableId;
    }
}


public class ExisitingCardTitlesTable : DataTable
{
    private HashSet<string> existingCardTitles;

    public event Action<string> OnDataDeleteEvent;

    public ExisitingCardTitlesTable(int tableId) : base(tableId)
    {
        existingCardTitles = new HashSet<string>();
    }

    public void Add(string cardTitle)
    {
        existingCardTitles.Add(cardTitle);
    }

    public void Remove(string cardTitle)
    {
        existingCardTitles.Remove(cardTitle);
        OnDataDeleteEvent?.Invoke(cardTitle);
    }

    public bool Contains(string cardTitle) { return existingCardTitles.Contains(cardTitle); }
}
