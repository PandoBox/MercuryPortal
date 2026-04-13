package com.mercury.messengerportal.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mercury.messengerportal.data.local.dao.DayLogDao
import com.mercury.messengerportal.data.local.dao.JobDao
import com.mercury.messengerportal.data.local.dao.JobStatusLogDao
import com.mercury.messengerportal.data.local.entity.DayLogEntity
import com.mercury.messengerportal.data.local.entity.JobEntity
import com.mercury.messengerportal.data.local.entity.JobStatusLogEntity

@Database(
    entities = [
        JobEntity::class,
        JobStatusLogEntity::class,
        DayLogEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun jobDao(): JobDao
    abstract fun jobStatusLogDao(): JobStatusLogDao
    abstract fun dayLogDao(): DayLogDao
}
