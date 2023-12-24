namespace UtilsLibrary;

public class Try
{
    public Exception Exception { get; private set; }

    public Try(Exception ex)
    {
        Exception = ex;
    }

    public static Try CallFunction(Action action)
    {
        if (action == null)
        {
            var ex = new ArgumentNullException(nameof(action));
            return new Try(ex);
        }

        try
        {
            action.Invoke();
            return new Try(null);
        }
        catch (Exception ex)
        {
            return new Try(ex);
        }
    }
}

public class Try<T>
{
    public Exception Exception { get; private set; }
    public T GetResult { get; private set; }

    public Try(T result, Exception ex)
    {
        Exception = ex;
        GetResult = result;
    }

    public static Try<T> Executing(Func<T> func)
    {
        if (func == null)
        {
            var ex = new ArgumentNullException(nameof(func));
            return new Try<T>(default(T), ex);
        }

        try
        {
            return new Try<T>(func.Invoke(), null);
        }
        catch (Exception ex)
        {
            return new Try<T>(default(T), ex);
        }
    }

    public Try<T> HandleIfThrows(Action<Exception> exceptionHandler, bool throwIfHandlerFailed = true)
    {
        if (Exception == null) return this;

        var triedAction = Try.CallFunction(() => exceptionHandler.Invoke(Exception));
        if (triedAction.Exception == null) return this;
        if (throwIfHandlerFailed) throw triedAction.Exception;
        //else log the exception

        return this;
    }
}

