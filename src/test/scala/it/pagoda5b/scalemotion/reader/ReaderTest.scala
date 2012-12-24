package it.pagoda5b.scalemotion.reader

import org.scalatest._
import org.scalatest.matchers._
import xml._

class RemoteSourceTest extends WordSpec with ShouldMatchers {

  "A remote reader " should {

    "read the expected xml feed" in {
      //converte la Promise in un valore opzionale e aspetta la risposta asincrona (chiamando apply)
      val xmlTree: Option[Elem] = (new RemoteSource("http://www.scala-lang.org/rss.xml").read).option.apply()

      xmlTree should be('defined)
      xmlTree map (content => (content \\ "channel" \ "title").text) should be(Some("The Scala Programming Language"))
      xmlTree map (content => (content \\ "channel" \ "link").text) should be(Some("http://www.scala-lang.org/"))
    }

    "find the correct number of entries" in {
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

  "The SOF feed parser" should {

    import java.io.File
    import org.joda.time._
    import SOFFeedParser._

    implicit val source: Elem = XML.loadFile(new File(this.getClass.getClassLoader.getResource("testFeed.xml").toURI))
    val parser = new SOFFeedParser {}

    "read the feed title" in {
      (parser parseTitle) should be("Recent Questions - Stack Overflow")
    }
    "count the number of entries" in {
      (parser parseNumberOfEntries) should be(30)
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
        'updated(new DateTime(2012, 12, 17, 17, 30, 33)),
        'summary("""<p>I want to replace the white space and special characters with a hyphen .Thank you in advance</p>"""))
    }
    "parse as objects all the entries" in {
      val entryIdMatcher = """http://stackoverflow.com/q/\d+""".r
      val entries = (parser parseAllEntries)
      entries should have size (30)
      (entries forall (_.isInstanceOf[FeedEntry])) should be(true)
      (entries forall (entry => (entryIdMatcher findFirstIn entry.id).isDefined)) should be(true)
    }
    "parse as objects the entries filtered by author" in {
      val entries = (parser parseAllEntries withAuthor("Josh Livingston"))
      entries should have size (1)
      entries.head should have(
        'id("http://stackoverflow.com/q/13919022"),
        'title("Something wrong with Python class Inheritance"),
        'link("http://stackoverflow.com/questions/13919022/something-wrong-with-python-class-inheritance"),
        'categories(Seq("python", "class", "inheritance")),
        'author("Josh Livingston"),
        'published(new DateTime(2012, 12, 17, 17, 29, 53)),
        'updated(new DateTime(2012, 12, 17, 17, 29, 53)),
        'summary("""<p>I'm taking a Programming class, and for one of the tests I need to make a class that inherits from an already created class. This is the code the teacher taught me to do, but it doesn't seem to be working:</p>""" +
          """<pre><code>class Intern(Employer):""" +
          """def __init__(self, last_name, first_name, address, phone, email, end_date):""" +
          """    Employer(last_name, first_name, address, phone, email)""" +
          """    self.end_date=end_date""" +
          """def intern_info(self):""" +
          """    self.print_info()""" +
          """    print self.end_date""" +
          """</code></pre>"""))
    }
    "parse as objects the entries filtered by title content" in {
      val entries = (parser parseAllEntries withTitle("selection sort"))
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
    "parse as objects the entries filtered by a tag" in {
      val entries = (parser parseAllEntries withTag("swap"))
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
    "parse as objects the entries filtered by composite criteria" in {
      //verifica il singolo filtro
      (parser parseAllEntries withTitle("not")) should have size (4)
      //applica il filtro composto
      val entries = (parser parseAllEntries (withTitle("not") and withTag("java")))
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
    "clean up simple html tags from a string" in {
      val text = """<p>I'm taking a Programming class, and for one of the tests I need to make a class that inherits from an already created class. This is the code the teacher taught me to do, but it doesn't seem to be working:</p>""" +
          """<pre><code>class Intern(Employer):""" +
          """def __init__(self, last_name, first_name, address, phone, email, end_date):""" +
          """    Employer(last_name, first_name, address, phone, email)""" +
          """    self.end_date=end_date""" +
          """def intern_info(self):""" +
          """    self.print_info()""" +
          """    print self.end_date""" +
          """</code></pre>"""
      val cleaned = """I'm taking a Programming class, and for one of the tests I need to make a class that inherits from an already created class. This is the code the teacher taught me to do, but it doesn't seem to be working:""" +
          """class Intern(Employer):""" +
          """def __init__(self, last_name, first_name, address, phone, email, end_date):""" +
          """    Employer(last_name, first_name, address, phone, email)""" +
          """    self.end_date=end_date""" +
          """def intern_info(self):""" +
          """    self.print_info()""" +
          """    print self.end_date"""
      (SOFFeedParser removeXmlTagging text) should equal (cleaned)
      (SOFFeedParser removeXmlTagging "<no tag here< nor here") should equal ("<no tag here< nor here")
    }

  }

}