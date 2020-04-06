import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession

object Context {
  val ss: SparkSession = SparkSession.builder()
    .appName("DataSet Test")
    .master("local[*]").getOrCreate()

  val sc: SparkContext = ss.sparkContext
}
