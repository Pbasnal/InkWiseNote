package org.basnalcorp.shared.data.repository

import org.basnalcorp.shared.domain.AtomicNote

interface AtomicNotesRepository {
    fun insert(atomicNote: AtomicNote): Long
    fun update(atomicNote: AtomicNote): Int
    fun delete(noteId: Long)
    fun get(noteId: Long): AtomicNote?
    fun getByIds(noteIds: Set<Long>): List<AtomicNote>
}
