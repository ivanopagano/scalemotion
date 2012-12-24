package it.pagoda5b.scalemotion.reader
import org.joda.time._
import xml.{ Elem, Node }

/**
 * La classe accede ad una sorgente di dati remota (un feed)
 */
case class RemoteSource(urlString: String) {

  import dispatch._

  //il feed
  val feed = url(urlString)

  /**
   * legge la sorgente in modo asincrono, restituendo una [[Promise]] del contenuto xml
   */
  def read: Promise[Elem] = Http(feed OK as.xml.Elem)

}

/**
 * Il parser estrae i dati utili dal contenuto della risposta remota.
 */
trait ContentParser[T] {

  //Il titolo
  def parseTitle(implicit source: Elem): String

  //Il numero di entries
  def parseNumberOfEntries(implicit source: Elem): Int

  //Genera la singola entry dall'elemento del feed
  def parseEntry(entry: Node): T

  //Estrae le entries del feed, con un filtro opzionale
  def parseAllEntries(filtering: Option[Node => Boolean] = None)(implicit source: Elem): Seq[T]

}

/**
 * Raccoglie i dati di una entry nel feed
 */
case class FeedEntry(id: String, title: String, link: String, categories: Seq[String], author: String, published: DateTime, updated: DateTime, summary: String)

/**
 * Parser specifico per il feed rss di StackOverflow
 */
trait SOFFeedParser extends ContentParser[FeedEntry] {

  import SOFFeedParser.EntryFilter

  //Regular Expression per estrarre le date
  private val DateExpression = """(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})Z""".r

  //Converte la data in una DateTime di joda-time
  private implicit def parseDate(date: String): DateTime = {
    val DateExpression(year, month, day, hour, minutes, seconds) = date
    new DateTime(year.toInt,
      month.toInt,
      day.toInt,
      hour.toInt,
      minutes.toInt,
      seconds.toInt)
  }

  def parseTitle(implicit source: Elem) = (source \\ "feed" \ "title").text

  def parseNumberOfEntries(implicit source: Elem) = (source \\ "entry").size

  def parseAllEntries(filtering: EntryFilter = None)(implicit source: Elem): Seq[FeedEntry] = {
    val allSeq = (source \\ "entry")
    val filtered = filtering map (allSeq filter _) getOrElse (allSeq)
    filtered map parseEntry
  }

  /**
   * overloading che rende piu' omogenea la chiamata senza parametri a quella con i filtri
   * i.e. "parser parseAllEntries" vs. "parser parseAllEntries ()"
   *      che e' analoga, ad esempio, a "parser parseAllEntries withTag(tag)"
   */
  def parseAllEntries(implicit source: Elem): Seq[FeedEntry] = parseAllEntries()(source)

  def parseEntry(entry: Node) = FeedEntry(
    id = (entry \ "id").text,
    title = (entry \ "title").text,
    link = (entry \ "link" \ "@href").text,
    categories = (entry \ "category" \\ "@term") map (_.text),
    author = (entry \ "author" \ "name").text,
    published = (entry \ "published").text,
    updated = (entry \ "updated").text,
    summary = ((entry \ "summary").text filter (!_.isControl)).trim) //Il sommario e' semplificato, tolti spazi e caratteri di controllo
}

object SOFFeedParser {

  //alias di un filtro opzionale sui nodi xml
  type EntryFilter = Option[Node => Boolean]

  //l'espressione seleziona dei semplici tag xml
  private val tagMatcher = "</?[A-Za-z]+>".r
  /**
   * Rimuove da un testo i tag racchiusi fra parentesi angolate (e.g. <p> e </p>)
   */
  def removeXmlTagging(text: String): String = tagMatcher replaceAllIn (text, "")

  /**
   * ****************************
   * Filtri predefiniti
   * ****************************
   */

  /**
   * sceglie le entry con un id
   */
  val withId: String => EntryFilter =
    id =>
      Some(node => (node \\ "id").text == id)
  /**
   * sceglie le entry con un delle parole specifiche nel titolo
   */
  val withTitle: String => EntryFilter =
    title =>
      Some(node => (node \\ "title").text contains title)
  /**
   * sceglie le entry di un autore
   */
  val withAuthor: String => EntryFilter =
    author =>
      Some(node => (node \\ "author" \ "name").text == author)
  /**
   * sceglie le entry con un tag
   */
  val withTag: String => EntryFilter =
    tag =>
      Some(node => (node \\ "category" \\ "@term") exists (_.text == tag))

  /**
   * ****************************
   * Composizione dei filtri
   * ****************************
   */

  /**
   * DSL per comporre EntryFilters
   */
  case class Composable(filter: EntryFilter) {

    /**
     * compone il filtro contenuto in questa classe con un altro filtro
     */
    def and(other: EntryFilter): EntryFilter = for {
      f1 <- filter
      f2 <- other
    } yield (node: Node) => f1(node) && f2(node)

  }

  //converte implicitamente un EntryFilter in un [[Composable]] di filtri
  implicit def filterComposable(f: EntryFilter): Composable = Composable(f)

}