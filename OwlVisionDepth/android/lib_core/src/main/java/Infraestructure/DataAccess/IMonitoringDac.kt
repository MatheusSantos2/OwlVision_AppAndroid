package Infraestructure.DataAccess

import Infraestructure.Entities.Monitoring
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface IMonitoringDac {
    @Query("SELECT * FROM Monitoring")
    fun selectAll(): List<Monitoring>

    @Insert
    fun insert(tabela: Monitoring)
}