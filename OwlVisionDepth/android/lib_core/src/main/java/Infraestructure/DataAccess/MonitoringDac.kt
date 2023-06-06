package Infraestructure.DataAccess

import java.sql.Connection
import java.sql.DriverManager

class MonitoringDac : IMonitoringDac
{
    private val connection: Connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/nome_do_banco_de_dados", "usuario", "senha")

    override fun selectAll(): List<Monitoring>
    {
        val query = "SELECT * FROM Monitoring"
        val statement = connection.prepareStatement(query)
        val resultSet = statement.executeQuery()

        val tabelas = mutableListOf<Monitoring>()
        while (resultSet.next())
        {
            val tabela = Monitoring(
                    resultSet.getInt("id"),
                    resultSet.getString("data"),
                    resultSet.getString("ip_cliente"),
                    resultSet.getDouble("velocidade_atual"),
                    resultSet.getDouble("velocidade_almejada"),
                    resultSet.getDouble("posicao_x_almejada"),
                    resultSet.getDouble("posicao_y_almejada"),
                    resultSet.getDouble("posicao_x_atual"),
                    resultSet.getDouble("posicao_y_atual")
            )
            tabelas.add(tabela)
        }

        statement.close()
        return tabelas
    }

    override fun insert(tabela: Monitoring) {
        val query = "INSERT INTO tabela (data, ip_cliente, velocidade_atual, velocidade_almejada, posicao_x_almejada, posicao_y_almejada, posicao_x_atual, posicao_y_atual) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
        val statement = connection.prepareStatement(query)
        statement.setString(1, tabela.data)
        statement.setString(2, tabela.ipCliente)
        statement.setDouble(3, tabela.velocidadeAtual)
        statement.setDouble(4, tabela.velocidadeAlmejada)
        statement.setDouble(5, tabela.posicaoXAlmejada)
        statement.setDouble(6, tabela.posicaoYAlmejada)
        statement.setDouble(7, tabela.posicaoXAtual)
        statement.setDouble(8, tabela.posicaoYAtual)

        statement.executeUpdate()
        statement.close()
    }
}