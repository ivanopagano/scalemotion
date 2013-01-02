package it.pagoda5b.scalemotion.reader

/**
 * Raccoglie le entry  e i dati di un feed di domande su StackOverflow.
 * Bisogna tener presente che un feed rss raccoglie solo le 30 domande pi&ugrave; recenti,
 * mentre questa classe pu&ograve; accumulare ulteriori entry partendo dalla mappa passata
 * nel costruttore
 *
 * @param feedUrl dove trovare il feed
 * @param tagSpecific il feed deve contenere questo tag specifico nelle sue categorie
 * @param entries una mappa delle entry gi&agrave; lette
 */
case class SOFFeed(feedUrl: String, tagSpecific: Option[String] = None, entries: Map[String, FeedEntry] = Map()) extends SOFFeedParser with SOFEntryStatistics {

  // rappresentazione semplificata, per un debug pi&ugrave; immediato
  override def toString: String = "SOFFeed(%s, %s, %d entries)".format(feedUrl, tagSpecific, entries.size)

  //Riferimento al feed remoto
  private[this] val remote = RemoteSource(tagSpecific map { feedUrl + "/" + _ } getOrElse feedUrl)

  /*
   *legge il contenuto remoto, come [[Promise]] di un possibile risultato, 
   *che sar&agrave; {{{None}}} in caso di qualche errore di lettura del feed
   */
  private[this] lazy val xmlContent = remote.read.option

  //applica la funzione di parsing al contenuto del feed, letto dalla [[Promise]], restituendo un valore opzionale
  private[this] def optionally[T](parseFunction: xml.Elem => T): Option[T] = xmlContent() map parseFunction

  /**
   * restituisce l'eventuale titolo del feed, se disponibile
   */
  def title: Option[String] = optionally(implicit xml => parseTitle)

  /**
   * restituisce il numero di entries contenute leggendo in remoto, se disponibile
   */
  def numberOfEntries: Option[Int] = optionally(implicit xml => parseNumberOfEntries)

  /**
   * restituisce le entry contenute leggendo da remoto, se disponibili
   */
  def latestEntries: Option[Seq[FeedEntry]] = optionally(implicit xml => parseAllEntries)

  /**
   * restituisce una mappa aggiornata delle entry di questo feed,
   * ottenuta aggiungendo i valori disponibili da remoto, se possibile
   */
  def updatedEntries: Map[String, FeedEntry] = entries ++ {
    for {
      updates <- latestEntries.toList
      entry <- updates
    } yield (entry.id, entry)
  }

  /**
   * restituisce un nuovo [[SOFFeed]], le cui entry sono arricchite con i dati
   * ottenuti in remoto, se al momento disponibili
   */
  def updateFeed: SOFFeed = copy(entries = updatedEntries)

  /**
   * ottiene i conteggi delle parole contenute in tutte le domande al momento
   * registrate in questo feed
   */
  def extractWordStatistics: Map[String, Int] = extractWordCounts(entries.values)
}