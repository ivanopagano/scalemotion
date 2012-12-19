package it.pagoda5b.scalemotion.reader
import org.joda.time._

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
  def read: Promise[xml.Elem] = Http(feed OK as.xml.Elem)

}

/**
 * Il parser estrae i dati utili dal contenuto della risposta remota.
 */
trait ContentParser[T] {

  //Il titolo
  def parseTitle(root: xml.Elem): String

  //Il numero di entries
  def parseNumberOfEntries(root: xml.Elem): Int

  //Genera la singola entry dall'elemento del feed
  def parseEntry(entry: xml.Elem): T

}

/**
 * Raccoglie i dati di una entry nel feed
 */
case class FeedEntry(id: String, title: String, link: String, categories: Seq[String], author: String, published: DateTime, updated: DateTime)

/**
 * Parser specifico per il feed rss di StackOverflow
 */
trait SOFFeedParser extends ContentParser[FeedEntry] {

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

  def parseTitle(root: xml.Elem) = (root \\ "feed" \ "title").text

  def parseNumberOfEntries(root: xml.Elem) = (root \\ "entry").size

  def parseEntry(entry: xml.Elem) = FeedEntry(
    id = (entry \ "id").text,
    title = (entry \ "title").text,
    link = (entry \ "link" \ "@href").text,
    categories = (entry \ "category" \\ "@term") map (_.text),
    author = (entry \ "author" \ "name").text,
    published = (entry \ "published").text,
    updated = (entry \ "updated").text)

}