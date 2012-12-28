package it.pagoda5b.scalemotion.reader

import org.scalatest._
import org.scalatest.matchers._

class FeedStatsTest extends WordSpec with ShouldMatchers {

  "A SOFFeedStatistics " should {

    val statsParser = new SOFFeedParser with SOFEntryStatistics {}

    "group word counts" in {

      val expectedCounts = Map(
        "I" -> 2,
        "want" -> 1,
        "to" -> 2,
        "replace" -> 1,
        "the" -> 1,
        "white" -> 1,
        "space" -> 1,
        "and" -> 1,
        "need" -> 1,
        "test" -> 1)
      (statsParser countWords "I want to replace the white space and I need to test .") should be(expectedCounts)

    }

    "extract the summary word count from a feed entry" in {
      import org.joda.time._

      val entry = FeedEntry(
        id = "id",
        title = "title",
        link = "link",
        categories = Seq[String](),
        author = "author",
        published = DateTime.now,
        updated = DateTime.now,
        summary = """<p>I'm taking a <pre><code>Programming class</code></pre>, and for one of the tests I need to make a class that inherits from an already created class.</p>""")

      val expectedCounts = Map(
        "I" -> 2,
        "m" -> 1,
        "taking" -> 1,
        "a" -> 2,
        "Programming" -> 1,
        "class" -> 3,
        "and" -> 1,
        "for" -> 1,
        "one" -> 1,
        "of" -> 1,
        "the" -> 1,
        "tests" -> 1,
        "need" -> 1,
        "to" -> 1,
        "make" -> 1,
        "that" -> 1,
        "inherits" -> 1,
        "from" -> 1,
        "an" -> 1,
        "already" -> 1,
        "created" -> 1)

      (statsParser extractWordCounts entry) should be(expectedCounts)
    }

  }

}
