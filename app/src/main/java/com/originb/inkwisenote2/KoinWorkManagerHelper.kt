package com.originb.inkwisenote2

import androidx.work.WorkerFactory
import org.koin.androidx.workmanager.factory.KoinWorkerFactory

fun getKoinWorkManagerFactory(): WorkerFactory {
    return KoinWorkerFactory()
}

