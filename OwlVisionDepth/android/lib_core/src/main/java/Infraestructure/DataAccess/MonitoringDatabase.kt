package Infraestructure.DataAccess

import Infraestructure.Entities.Monitoring
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Monitoring::class], version = 1)
abstract class MonitoringDatabase : RoomDatabase()
{
    abstract fun MonitoringDac(): MonitoringDac
}