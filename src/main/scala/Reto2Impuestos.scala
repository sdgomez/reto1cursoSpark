import Context._
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.expressions.WindowSpec
import org.apache.spark.sql.functions._
object Reto2Impuestos extends App {
  val impuestoDf: DataFrame =
    ss.read.format("csv").option("header", "true")
      .load("src/main/resources/ImpuestoTransporteMinEnerg.csv")
      .selectExpr(
        "`ano` as ano",
        "`departamento` as departamento",
        "`totalimpuesto` as totalImpuesto"
      ).na.drop()
  impuestoDf.cache()
  impuestoDf.createOrReplaceTempView("impuestosTable")

  val windowSpec: WindowSpec =
    org.apache.spark.sql.expressions.Window
      .partitionBy("departamento" )
    .orderBy("departamento","ano")

  val impuestoDfWithNewColumns: DataFrame = ss.sql("""
          SELECT
            distinct ano,
            departamento,
            Sum(totalimpuesto) over (partition by departamento, ano order by ano) as totalano,
            sum(totalimpuesto) over (partition by departamento order by ano) as totalacc
          FROM
            impuestosTable
          ORDER BY departamento, ano
    """)
    .withColumn(
      "lastyear",
      lag("totalano", 1).over(windowSpec)
    )
    .withColumn(
      "difference",
      col("totalano") - col("lastyear")
    )
    .withColumn(
      "Trend",
      when( col("difference").isNull, "IGUAL" )
        .when( col("difference") > 0, "SUBIO" )
        .when( col("difference") < 0, "BAJO" )
    )

  impuestoDfWithNewColumns.show()

}
