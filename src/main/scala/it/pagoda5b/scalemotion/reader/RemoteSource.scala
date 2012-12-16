package it.pagoda5b.scalemotion.reader


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
trait ContentParser {

}