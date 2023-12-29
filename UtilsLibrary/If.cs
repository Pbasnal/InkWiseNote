namespace UtilsLibrary;

public class If
{
    protected bool conditionResult;

    protected If(bool condition)
    {
        conditionResult = condition;
    }

    public static If Condition(bool condition)
    {
        return new If(condition);
    }

    public IfWithResult<T> IsTrue<T>(T value)
    {
        return new IfWithResult<T>(value, conditionResult);
    }
}

public class IfWithResult<T> : If
{
    private T resultValue;

    public IfWithResult(T resultValue, bool condition) : base(condition)
    {
        this.resultValue = resultValue;
    }

    public T OrElse(T value)
    {
        return conditionResult ? resultValue : value;
    }
}