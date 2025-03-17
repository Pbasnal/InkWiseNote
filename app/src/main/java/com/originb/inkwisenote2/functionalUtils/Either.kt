package com.originb.inkwisenote2.functionalUtils

class Either<Err, Res> private constructor(var error: Err, var result: Res) {
    companion object {
        fun <Res> result(value: Res): Either<*, *> {
            return Either<Any?, Any?>(null, value)
        }

        fun <Err> error(error: Err): Either<*, *> {
            return Either<Any?, Any?>(error, null)
        }
    }
}
