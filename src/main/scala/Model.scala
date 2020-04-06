object Model {
  final case class RatingApplication(app: String, rating: String)
  final case class UserReview(appName: String, sentiment: String, sentimentPolarity: String, translatedReview: String)
  final case class ApplicationReview(
    app: String,
    rating: String,
    sentiment: String,
    sentimentPolarity: String,
    translatedReview: String
  )
}
