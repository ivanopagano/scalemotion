package it.pagoda5b.scalemotion.reader

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.OptionValues._

class SOFFeedTest extends WordSpec with ShouldMatchers {

  "The SOFFeed" when {
    "created with no specification tag" should {
      val feed = SOFFeed("http://stackoverflow.com/feeds")
      val entryIdPattern = """http://stackoverflow.com/q/\d+"""

      "have a title" in {
        feed.title.value should be ("Recent Questions - Stack Overflow")
      }
      "count 30 entries" in {
        feed.numberOfEntries.value should be (30)
      }
      "read the latest available entries" in {
        feed.latestEntries.value should have size (30)
        for (e <- feed.latestEntries.value) e.id should fullyMatch regex (entryIdPattern)
      }
      "record his entries" in {
        feed.entries should have size (30)
        for (e <- feed.entries.values) e.id should fullyMatch regex (entryIdPattern)
      }
      "update his recorded entries" in {
        val updated = feed.updateFeed.entries
        updated.size should be >= feed.entries.size
        for (k <- updated.keys) k should fullyMatch regex (entryIdPattern)
        for (k <- feed.entries.keys) updated should contain key (k)
      }
    }

    "created with a specification tag " should {
      val tag = "scala"
      val tagged = SOFFeed("http://stackoverflow.com/feeds", Some(tag))

      "have a title" in {
        tagged.title.value should be ("active questions tagged scala - Stack Overflow")
      }
      "count 30 entries" in {
        tagged.numberOfEntries.value should be (30)
      }
      "read the latest available entries with the selected category" in {
        tagged.latestEntries.value should have size (30)
        for (e <- tagged.latestEntries.value) e.categories should contain (tag)
      }
    }
  }
}