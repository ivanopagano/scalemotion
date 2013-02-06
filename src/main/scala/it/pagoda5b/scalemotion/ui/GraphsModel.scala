package it.pagoda5b.scalemotion.ui

import it.pagoda5b.scalemotion.reader.SOFFeed
import it.pagoda5b.javafx.FXPropertyUtils._
import it.pagoda5b.javafx.FXBindingsUtils._
import org.ocpsoft.prettytime.PrettyTime
import org.ocpsoft.prettytime.units.JustNow
import scala.collection.JavaConversions._
import scala.math.Ordering
import scalafx.beans.binding.StringBinding
import scalafx.scene.chart._
import scalafx.scene.chart.XYChart._
import scalafx.scene.chart.XYChart.Series._
import scalafx.scene.chart.XYChart.Data._

import scalafx.beans.property._
import scalafx.collections._
import java.util.Date
import javafx.beans.binding.ListBinding
import javafx.scene.{ chart => jfxsc }

/**
 * Contiene e gestisce i dati da rappresentare nei grafici
 */
object GraphsModel {

  /**
   * il feed da cui estrarre i valori
   */
  val feed: ObjectProperty[SOFFeed] = new ObjectProperty(this, "feed", SOFFeed("http://stackoverflow.com/feeds"))

  /**
   * stabilisce la soglia minima per un termine affinch&eacute; venga inclusa nei dati del grafico
   */
  val histogramThresholdProperty: IntegerProperty = new IntegerProperty(this, "histogramThreshold", 10)

  /**
   * stabilisce il numero ammesso di timelines mostrate nel grafico dei tag
   */
  val timelineThresholdProperty: IntegerProperty = new IntegerProperty(this, "timelineThreshold", 5)

  // tiene traccia del momento in cui &egrave; stato generato il modello
  private val histogramStart = {
    import java.util.Locale
    val pt = new PrettyTime(new Date, Locale.UK) //tutte le etichette sono in inglese
    pt removeUnit classOf[JustNow] //necessario per ridurre lo scarto minimo individuato a sotto i 5 minuti
    pt
  }

  /**
   * indica quanto tempo &egrave; passato fra l'ultimo aggiornamento dei dati e la creazione del modello
   */
  val elapsedTimeProperty = createStringBinding() {
    histogramStart format (new Date)
  }

  /**
   * aggiorna i dati, e i valori dei grafici se ci sono dati aggiornati
   */
  def refreshData() {
    feed.modify(_.updateFeed)
    elapsedTimeProperty.invalidate()
    if (feed.freshDataAvailable) wordsTabular.invalidate()
  }

  /*
   * elabora i valori forniti dal feed per l'analisi delle parole
   */
  private def extractCountValues: Seq[jfxsc.XYChart.Data[String, Number]] = feed
    .extractWordStatistics
    .filter {
      case (_, count) => count >= histogramThresholdProperty.get
    }
    .toSeq
    .sortBy { case (_, count) => count }(Ordering.Int.reverse)
    .map(toChartData)

  /**
   * elabora i valori del feed sui conteggi dei tag
   * TODO: troppe categorie, bisogna mettere una soglia sul numero minimo di conteggi...
   */
  def extractTagCounts: Map[String, Number] = feed
    .extractTagStatistics
    .mapValues(_.asInstanceOf[Number])

  //i dati tabellari dei conteggi delle parole, come property
  private val wordsTabular = ObservableBoundBuffer[jfxsc.XYChart.Data[String, Number]](
    binding = createListBinding(histogramThresholdProperty) {
      ObservableBuffer[jfxsc.XYChart.Data[String, Number]](extractCountValues)
    })

  //le serie di istogrammi da inserire nel grafico
  private val wordsSeries: Seq[jfxsc.XYChart.Series[String, Number]] = Seq(XYChart.Series(wordsTabular))

  /**
   * contiene le serie di istogrammi con i conteggi delle parole
   */
  val wordsSeriesList = ObservableBuffer[jfxsc.XYChart.Series[String, Number]](wordsSeries)

  //inserisce una coppia di valori in un dato valido per una `XYChart`
  private def toChartData(data: (String, Int)): jfxsc.XYChart.Data[String, Number] = XYChart.Data[String, Number](data._1, data._2)

}