using static CommunityToolkit.Maui.Markup.GridRowsColumns;

namespace InkWiseCore.UiComponents.UiLayouts;

public class GridLayoutBuilder
{
    private ColumnDefinitionCollection ColumnDefinitions;
    private RowDefinitionCollection RowDefinitions;

    private List<View> elements;

    private GridLayoutBuilder()
    {
        elements = new List<View>();
    }

    public static GridLayoutBuilder NewGrid()
    {
        return new GridLayoutBuilder();
    }

    public GridLayoutBuilder HasColumns(params GridLength[] widths)
    {
        ColumnDefinitions = Columns.Define(widths);

        return this;
    }
    public GridLayoutBuilder HasRows(params GridLength[] widths)
    {
        RowDefinitions = Rows.Define(widths);

        return this;
    }

    public GridLayoutBuilder HasChildren(View view)
    {
        elements.Add(view);
        return this;
    }

    public static implicit operator Grid(GridLayoutBuilder builder)
    {
        var grid = new Grid
        {
            ColumnDefinitions = builder.ColumnDefinitions,
            RowDefinitions = builder.RowDefinitions,
        };

        builder.elements.ForEach(grid.Children.Add);

        return grid;
    }
}
