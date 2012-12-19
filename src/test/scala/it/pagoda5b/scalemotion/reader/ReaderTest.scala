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

  }

}