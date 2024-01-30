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

    public IfWithAction RunIfTrue(Action action)
    {
        return new IfWithAction(action, conditionResult);
    }


    public IfWithFunc<T> IsTrueRun<T>(Func<T> actionWithResult)
    {
        return new IfWithFunc<T>(actionWithResult, conditionResult);
    }
}

public class IfWithAction : If
{
    private Action ifTrueAction;

    public IfWithAction(Action ifTrueAction, bool condition) : base(condition)
    {
        this.ifTrueAction = ifTrueAction;
    }

    public void OrElse(Action elseAction)
    {
        if (conditionResult)
            ifTrueAction.Invoke();
        else
            elseAction.Invoke();
    }
}

public class IfWithFunc<T> : If
{
    private Func<T> ifTrueFunc;

    public IfWithFunc(Func<T> ifTrueFunc, bool condition) : base(condition)
    {
        this.ifTrueFunc = ifTrueFunc;
    }

    public T OrElse(Func<T> elseFunc)
    {
        if (conditionResult)
           return ifTrueFunc.Invoke();
        else
            return elseFunc.Invoke();
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