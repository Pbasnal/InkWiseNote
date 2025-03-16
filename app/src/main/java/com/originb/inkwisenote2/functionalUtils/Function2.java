package com.originb.inkwisenote2.functionalUtils;

@FunctionalInterface
public interface Function2<A, B, R> {
    R apply(A a, B b);
}
