package it.pagoda5b.scalemotion.reader


import org.scalatest._
import org.scalatest.matchers._

class RemoteSourceTest extends WordSpec with ShouldMatchers {

	"A remote reader for scala site feed " should {

		"read the expected xml feed" in {
			val xmlTree: Option[xml.Elem] = (new RemoteSource("http://www.scala-lang.org/rss.xml").read).option.apply()

			xmlTree should be ('defined)
			xmlTree map (content => (content \\ "channel" \ "title").text) should be (Some("The Scala Programming Language"))
			xmlTree map (content => (content \\ "channel" \ "link").text) should be (Some("http://www.scala-lang.org/"))
		}

	}

}