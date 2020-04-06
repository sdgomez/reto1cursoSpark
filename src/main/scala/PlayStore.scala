import Context._
import org.apache.spark.sql.Dataset
import Model._
import ss.implicits._
import scala.util.Try

object PlayStore extends App {
  val appsRating: Dataset[RatingApplication] =
    ss.read.option("header", "true").csv("src/main/resources/googleplaystore.csv")
      .selectExpr(
        "`App` as app",
        "`Rating` as rating"
      )
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
  val bestApps: Dataset[RatingApplication] = appsRating.filter(x => Try(x.rating.toDouble > 4.7).getOrElse(false))

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

