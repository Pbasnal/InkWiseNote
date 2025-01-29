package com.originb.inkwisenote.modules.functionalUtils;

public class Either<Err, Res> {
    public Err error;
    public Res result;

    public static <Res> Either result( Res value) {
        return new Either(null, value);
    }

    public static <Err> Either error( Err error) {
        return new Either(error, null);
    }

    private Either(Err err, Res value) {
        this.error = err;
        this.result = value;
    }
}
