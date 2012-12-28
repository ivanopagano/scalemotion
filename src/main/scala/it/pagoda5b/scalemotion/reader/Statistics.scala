package it.pagoda5b.scalemotion.reader

/**
 * Aggiunge le funzioni di conteggio e statistica alle entry
 */
trait SOFEntryStatistics { self: SOFFeedParser =>

  import SOFFeedParser._

  private val commonSeparators = Array(' ', ',', '.', '\'', '?', '!', ';', ':', '\"')
  /**
   * conta le singole parole del testo passato, vengono rimossi i caratteri di interpunzione e gli spazi
   */
  def countWords(text: String): Map[String, Int] = text.split(commonSeparators).filterNot(_.isEmpty).groupBy(identity(_)).filterKeys(isPlainWord).mapValues(_.size)

  /**
   * controlla se il testo passato e' una parola composta solo da lettere o numeri
   */
  def isPlainWord(word: String): Boolean = word forall (_.isLetterOrDigit)

  /**
   * calcola un conteggio delle parole contenute nel *summary*, rimossi i tag xml
   */
  def extractWordCounts(entry: FeedEntry): Map[String, Int] = ((removeXmlTagging _) andThen (countWords _))(entry.summary)

}