package org.basnalcorp.shared.util

class Either<Err, Res> private constructor(val error: Err?, val result: Res?) {
    companion object {
        fun <Res> result(value: Res?): Either<Nothing, Res?> =
            Either(null, value)

        fun <Err> error(error: Err?): Either<Err?, Nothing> =
            Either(error, null)
    }
}
