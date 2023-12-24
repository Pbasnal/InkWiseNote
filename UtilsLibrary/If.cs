namespace UtilsLibrary;

public class Condition
{
    protected bool conditionResult;

    protected Condition(bool condition)
    {
        conditionResult = condition;
    }

    public static Condition If(bool condition)
    {
        return new Condition(condition);
    }

    public ConditionWithResult<T> OnTrue<T>(T value)
    {
        return new ConditionWithResult<T>(value, conditionResult);
    }
}

public class ConditionWithResult<T> : Condition
{
    private T resultValue;

    public ConditionWithResult(T resultValue, bool condition) : base(condition)
    {
        this.resultValue = resultValue;
    }

    public T OrElse(T value)
    {
        return conditionResult ? resultValue : value;
    }
}