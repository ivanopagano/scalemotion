package it.pagoda5b.scalemotion.reader

import org.scalatest._
import org.scalatest.matchers._
import xml._

class RemoteSourceTest extends WordSpec with ShouldMatchers {

  "A remote reader for scala site feed " should {

    "read the expected xml feed" in {
      val xmlTree: Option[Elem] = (new RemoteSource("http://www.scala-lang.org/rss.xml").read).option.apply()

      xmlTree should be('defined)
      xmlTree map (content => (content \\ "channel" \ "title").text) should be(Some("The Scala Programming Language"))
      xmlTree map (content => (content \\ "channel" \ "link").text) should be(Some("http://www.scala-lang.org/"))
    }

    "find the correct number of 24 entries" in {
      val xmlTree: Option[Elem] = (new RemoteSource("http://www.scala-lang.org/rss.xml").read).option.apply()

      xmlTree map (content => (content \\ "item").size) should be(Some(24))

    }

    "read a stackoverflow tag-specific feed" in {
      val xmlTree: Option[Elem] = (new RemoteSource("http://stackoverflow.com/feeds/tag/scala").read).option.apply()

      xmlTree should be('defined)
      xmlTree map (content => (content \\ "feed" \ "id").text) should be(Some("http://stackoverflow.com/feeds/tag/scala"))
      xmlTree map (content => (content \\ "feed" \ "title").text) should be(Some("active questions tagged scala - Stack Overflow"))
    }

  }
}

class FeedParserTest extends WordSpec with ShouldMatchers {

  "A feed parser" should {

    import java.io.File
    import org.joda.time._

    val source: Elem = XML.loadFile(new File(getClass.getClassLoader.getResource("testFeed.xml").toURI))
    val parser = new SOFFeedParser {}

    "read the feed title" in {
      (parser parseTitle source) should be("Recent Questions - Stack Overflow")
    }
    "count the number of entries" in {
      (parser parseNumberOfEntries source) should be(30)
    }
    "correctly read the first entry" in {
      val entry = parser parseEntry (source \\ "entry").head.asInstanceOf[Elem]
      entry should have(
        'id("http://stackoverflow.com/q/13919025"),
        'title("Visual c# Replace special characters and white space from a string"),
        'link("http://stackoverflow.com/questions/13919025/visual-c-sharp-replace-special-characters-and-white-space-from-a-string"),
        'categories(Seq("c#", "regex", "replace")),
        'author("user1346598"),
        'published(new DateTime(2012, 12, 17, 17, 30, 33)),
        'updated(new DateTime(2012, 12, 17, 17, 30, 33)))
    }
    "parse as objects all the entries" in {
      val entryIdMatcher = """http://stackoverflow.com/q/\d+""".r
      val entries = (parser parseAllEntries source)
      entries should have size (30)
      (entries forall (_.isInstanceOf[FeedEntry])) should be(true)
      (entries forall (entry => (entryIdMatcher findFirstIn entry.id).isDefined)) should be(true)
    }
    "parse as objects and filter by author the entries" in {
      import SOFFeedParser._
      // val withAuthor: String => Option[Node => Boolean] = author => Some(node => (node \\ "author" \ "name").text == author)
      val entries = (parser parseAllEntries (source, withAuthor("Josh Livingston")))
      entries should have size (1)
      entries.head should have(
        'id("http://stackoverflow.com/q/13919022"),
        'title("Something wrong with Python class Inheritance"),
        'link("http://stackoverflow.com/questions/13919022/something-wrong-with-python-class-inheritance"),
        'categories(Seq("python", "class", "inheritance")),
        'author("Josh Livingston"),
        'published(new DateTime(2012, 12, 17, 17, 29, 53)),
        'updated(new DateTime(2012, 12, 17, 17, 29, 53)))
    }
    "parse as objects and filter by title content the entries" in {
      import SOFFeedParser._
      val entries = (parser parseAllEntries (source, withTitle("selection sort")))
      entries should have size (1)
      entries.head should have(
        'id("http://stackoverflow.com/q/13919019"),
        'title("Swapping in selection sort not working?"),
        'link("http://stackoverflow.com/questions/13919019/swapping-in-selection-sort-not-working"),
        'categories(Seq("java", "sorting", "selection", "swap", "xor")),
        'author("coder005"),
        'published(new DateTime(2012, 12, 17, 17, 29, 42)),
        'updated(new DateTime(2012, 12, 17, 17, 29, 42)))
    }

  }

}