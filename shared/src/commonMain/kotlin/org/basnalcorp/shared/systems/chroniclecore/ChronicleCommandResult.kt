package org.basnalcorp.shared.systems.chroniclecore

/**
 * Result of a ChronicleCore mutation command.
 * Use [Failure] with message "name conflict" when the notebook name already exists in DB or file system.
 */
sealed class ChronicleCommandResult<out T> {

    data class Success<T>(val value: T) : ChronicleCommandResult<T>()

    data class Failure(val message: String) : ChronicleCommandResult<Nothing>()

    data class FailButRetry(val message: String) : ChronicleCommandResult<Nothing>()
}
