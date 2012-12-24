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
   * legge la sorgente in modo asincrono, restituendo una {{{Promise}}} del contenuto xml
   */
  def read: Promise[Elem] = Http(feed OK as.xml.Elem)

}

/**
 * Il parser estrae i dati utili dal contenuto della risposta remota.
 */
trait ContentParser[T] {

  //Il titolo
  def parseTitle(root: Elem): String

  //Il numero di entries
  def parseNumberOfEntries(root: Elem): Int

  //Genera la singola entry dall'elemento del feed
  def parseEntry(entry: Node): T

  //Estrae le entries del feed, con un filtro opzionale
  def parseAllEntries(root: Elem, filtering: Option[Node => Boolean] = None): Seq[T]

}

/**
 * Raccoglie i dati di una entry nel feed
 */
case class FeedEntry(id: String, title: String, link: String, categories: Seq[String], author: String, published: DateTime, updated: DateTime)

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

  def parseTitle(root: Elem) = (root \\ "feed" \ "title").text

  def parseNumberOfEntries(root: Elem) = (root \\ "entry").size

  def parseAllEntries(root: Elem, filtering: EntryFilter = None): Seq[FeedEntry] = {
    val allSeq = (root \\ "entry")
    val filtered = filtering map (allSeq filter _) getOrElse (allSeq)
    filtered map parseEntry
  }

  def parseEntry(entry: Node) = FeedEntry(
    id = (entry \ "id").text,
    title = (entry \ "title").text,
    link = (entry \ "link" \ "@href").text,
    categories = (entry \ "category" \\ "@term") map (_.text),
    author = (entry \ "author" \ "name").text,
    published = (entry \ "published").text,
    updated = (entry \ "updated").text)

}

object SOFFeedParser {

  //Alias for an optional filter on xml nodes
  type EntryFilter = Option[Node => Boolean]

  /**
   * ****************************
   * Predefined filters
   * ****************************
   */

  /**
   * Selects entries with specific id
   */
  val withId: String => EntryFilter =
    id =>
      Some(node => (node \\ "id").text == id)
  /**
   * Selects entries with specific words in the title
   */
  val withTitle: String => EntryFilter =
    title =>
      Some(node => (node \\ "title").text contains title)
  /**
   * Selects entries with a specific author
   */
  val withAuthor: String => EntryFilter =
    author =>
      Some(node => (node \\ "author" \ "name").text == author)
  /**
   * Selects entries with the specified tag
   */
  val withTag: String => EntryFilter =
    tag =>
      Some(node => (node \\ "category" \\ "@term") exists (_.text == tag))

  /**
   * ****************************
   * Filters composition
   * ****************************
   */

  /**
   * DSL to compose EntryFilters
   */
  case class Composable(filter: EntryFilter) {

    /**
     * combines the filter wrapped by this class with another provided filter
     */
    def and(other: EntryFilter): EntryFilter = for {
      f1 <- filter
      f2 <- other
    } yield (node: Node) => f1(node) && f2(node)

  }

  //implicitly converts a EntryFilter to a Composable Filter wrapper
  implicit def filterComposable(f: EntryFilter): Composable = Composable(f)

}