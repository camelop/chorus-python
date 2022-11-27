package chorus.integration

import chorus.schema.Schema
import chorus.rewriting.RewriterConfig
import chorus.sql.QueryParser
import chorus.mechanisms.EpsilonCompositionAccountant
import chorus.mechanisms.LaplaceMechClipping
import chorus.mechanisms.AverageMechClipping
import chorus.mechanisms.ReportNoisyMax

class QueryWithDP(dbFileLoc: String, configFileLoc: String, query: String, machanism: String, epsilon: Double, l: Double, u: Double) {
    def run() = {
        System.setProperty("db.driver", "org.sqlite.JDBC")
        System.setProperty("db.url", "jdbc:sqlite:" + dbFileLoc)
        System.setProperty("schema.config.path", configFileLoc)
        val database = Schema.getDatabase("default_db")
        val config = new RewriterConfig(database)
        val root = QueryParser.parseToRelTree(query, database)
        val accountant = new EpsilonCompositionAccountant()
        machanism match {
            case "LaplaceMechClipping" => {
                val r = new LaplaceMechClipping(epsilon, l, u, root, config).execute(accountant)
                r.head.vals.head.toDouble
            }
            case "AverageMechClipping" => {
                val r = new AverageMechClipping(epsilon, l, u, root, config).execute(accountant)
                r.head.vals.head.toDouble
            }
            case _ => {
                println("Error: machanism not supported")
                Double.NaN
            }
        }
    }
}

object CLI extends App {
    val dbFileLoc = args(0)
    val configFileLoc = args(1)
    val query = args(2)
    val machanism = args(3)
    val epsilon = args(4).toDouble
    val l = args(5).toDouble
    val u = args(6).toDouble
    val q = new QueryWithDP(dbFileLoc, configFileLoc, query, machanism, epsilon, l, u)
    println(q.run())
}
