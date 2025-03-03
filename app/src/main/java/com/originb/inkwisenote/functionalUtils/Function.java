package com.originb.inkwisenote.functionalUtils;

@FunctionalInterface
public interface Function<A, B, C, R> {
    R apply(A a, B b, C c);
}