namespace Systems.MonoBehaviour;

public interface IHaveBehaviours;

// Making behaviour a template to keep behaviours compile time constants.
// Class need to define each behaviour separately and use them
// we can't add them dynamically
public interface IBehaviour<B, T, R>
    where B : IHaveBehaviours
{
    public B ObjectWithBehaviours { get; }

    public R RunBehaviour(T input);
}


public class WithBehaviours : IHaveBehaviours
{
    NoBehaviour noBehaviour { get; }
    ComplexBehaviour complexBehaviour { get; }

    public WithBehaviours()
    {
        noBehaviour = new NoBehaviour(this);
        complexBehaviour = new ComplexBehaviour(this);
    }
}

public class NoBehaviour : IBehaviour<WithBehaviours, string, int>
{
    public WithBehaviours ObjectWithBehaviours { get; set; }

    public NoBehaviour(WithBehaviours haveBehaviours)
    {
        ObjectWithBehaviours = haveBehaviours;
    }

    public int RunBehaviour(string input)
    {
        return input.Length;
    }
}

public class ComplexBehaviour : IBehaviour<WithBehaviours, string, string>
{
    public WithBehaviours ObjectWithBehaviours { get; set; }

    public ComplexBehaviour(WithBehaviours haveBehaviours)
    {
        ObjectWithBehaviours = haveBehaviours;
    }

    public string RunBehaviour(string input)
    {
        return input;
    }
}
