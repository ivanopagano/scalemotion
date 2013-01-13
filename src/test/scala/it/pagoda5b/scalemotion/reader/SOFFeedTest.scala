package it.pagoda5b.scalemotion.reader

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.OptionValues._

class SOFFeedTest extends WordSpec with ShouldMatchers {

  "The SOFFeed" when {
    "created with no specification tag" should {
      val feed = SOFFeed("http://stackoverflow.com/feeds")
      val entryIdPattern = """http://stackoverflow.com/q/\d+""".r.pattern

      "have a title" in {
        feed.title.value should be ("Recent Questions - Stack Overflow")
      }
      "count 30 entries" in {
        feed.numberOfEntries.value should be (30)
      }
      "read the latest available entries" in {
        feed.latestEntries.value should have size (30)
        (feed.latestEntries.value forall (entry => entryIdPattern.matcher(entry.id).matches)) should be (true)
      }
      "record his entries" in {
        feed.entries should have size (30)
        (feed.entries.values forall (entry => entryIdPattern.matcher(entry.id).matches)) should be (true)
      }
      "update his recorded entries" in {
        val updated = feed.updateFeed.entries
        updated.size should be >= feed.entries.size
        (updated.keys forall (entryIdPattern.matcher(_).matches)) should be (true)
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