package Infraestructure.DataAccess

import Utils.StringHelper
import androidx.room.Room
import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

class DatabaseManager {
    private lateinit var database: MonitoringDatabase

    fun inicialize(context: Context)  {
        if (database == null) {
            database = Room.databaseBuilder(
                    context.applicationContext,
                    MonitoringDatabase::class.java,
                    "my_database.db"
            ).build()
        }
    }

    fun insert(message: String)
    {
        val messageArray = StringHelper().stringToArray(message)

        val monitoring = Monitoring(
                data = getDateTime(),
                ipCliente = "1",
                velocidadeAtual = messageArray[0].toDouble(),
                velocidadeAlmejada = messageArray[1].toDouble(),
                posicaoXAlmejada = messageArray[2].toDouble(),
                posicaoYAlmejada = messageArray[3].toDouble(),
                posicaoXAtual = messageArray[4].toDouble(),
                posicaoYAtual = messageArray[5].toDouble()
        )

        database.MonitoringDac().insert(monitoring)
    }

    fun getDateTime() : String
    {
        val currentDate = Date()
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        return format.format(currentDate)
    }
}