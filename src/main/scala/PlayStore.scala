import Context._
import Context.ss.implicits._
import Model._
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}

object PlayStore extends App {
  val customSchema = StructType(
    Seq(
      StructField("App", StringType, true),
      StructField("Rating", DoubleType, true),
    )
  )
  val appsRating: Dataset[RatingApplication] =
    ss.read.format("csv").option("header", "true")
      .schema(customSchema)
      .load("src/main/resources/googleplaystore.csv")
      .selectExpr(
        "`App` as app",
        "`Rating` as rating"
      ).na.drop()
      .as[RatingApplication]

  val userReviews: Dataset[UserReview] =
    ss.read.option("header", "true").csv("src/main/resources/googleplaystore_user_reviews.csv")
      .selectExpr(
        "`App` as appName",
        "`Sentiment` as sentiment",
        "`Sentiment_Polarity` as sentimentPolarity",
        "`Translated_Review` as translatedReview"
      ).as[UserReview]

  // Filtre las aplicaciones con Rating mayor a 4.7
  val bestApps: Dataset[RatingApplication] = appsRating.filter(x => x.rating > 4.7)

  // bestApps.show()

  // realice el join con los Reviews por el nombre de la aplicación
  val dsApplicationReview: Dataset[ApplicationReview] =
    bestApps.joinWith(userReviews, $"app" === $"appName", "inner")
    .map(record =>
      ApplicationReview(
        record._1.app, record._1.rating, record._2.sentiment, record._2.sentimentPolarity, record._2.translatedReview
      )
    )
  // Filtre los Sentiment "Negative" y ordene las aplicaciones por "Sentiment_Polarity" de manera descendente
  val dsAppsNegativeSentiments: Dataset[ApplicationReview] =
    dsApplicationReview.filter(_.sentiment == "Negative")
      .sort($"sentimentPolarity".desc)

  // muestre los 10 peores comentarios `Translated_Review`
  dsAppsNegativeSentiments.map(x => (x.app, x.translatedReview)).show(10)
}

