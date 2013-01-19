package it.pagoda5b.scalemotion.ui

import it.pagoda5b.scalemotion.reader.SOFFeed
import it.pagoda5b.javafx.FXPropertyUtils._
import it.pagoda5b.javafx.FXBindingsUtils._
import scala.collection.JavaConversions._
import scala.math.Ordering
import java.util.{ Collection => JCollection, List => JList, Date }
import org.ocpsoft.prettytime.PrettyTime
import org.ocpsoft.prettytime.units.JustNow
import javafx.scene.chart._
import javafx.beans.property._
import javafx.beans.binding.{ ListBinding, StringBinding }
import javafx.collections.{ FXCollections, ObservableList }

/**
 * Contiene e gestisce i dati da rappresentare nei grafici
 */
object GraphsModel {

  /**
   * il feed da cui estrarre i valori
   */
  val feedProperty: ObjectProperty[SOFFeed] = new SimpleObjectProperty(this, "feed", SOFFeed("http://stackoverflow.com/feeds"))

  /**
   * stabilisce la soglia minima per un termine affinch&eacute; venga inclusa nei dati del grafico
   */
  val histogramThresholdProperty: IntegerProperty = new SimpleIntegerProperty(this, "histogramThreshold", 10)

  /**
   * stabilisce il numero ammesso di timelines mostrate nel grafico dei tag
   */
  val timelineThresholdProperty: IntegerProperty = new SimpleIntegerProperty(this, "timelineThreshold", 5)

  // tiene traccia del momento in cui &egrave; stato generato il modello
  private val histogramStart = {
    val pt = new PrettyTime(new Date)
    //necessario per ridurre lo scarto minimo individuato a sotto i 5 minuti
    pt removeUnit classOf[JustNow]
    pt
  }

  /**
   * indica quanto tempo è passato fra l'ultimo aggiornamento dei dati e la creazione del modello
   */
  val elapsedTimeProperty = new StringBinding {
    override def computeValue = histogramStart format (new Date)
  }

  /**
   * aggiorna i dati, e i valori dei grafici se ci sono dati aggiornati
   */
  def refreshData() {
    feedProperty.modify(_.updateFeed)
    elapsedTimeProperty.invalidate()
    if (feedProperty.freshDataAvailable) wordsTabular.invalidate()
  }

  /*
   * elabora i valori forniti dal feed per l'analisi delle parole
   */
  private def extractCountValues: JList[XYChart.Data[String, Number]] = feedProperty
    .extractWordStatistics
    .filter {
      case (_, count) => count >= histogramThresholdProperty.get
    }
    .toSeq
    .sortBy { case (key, count) => count }(Ordering.Int.reverse)
    .map(toChartData)

  /**
   * elabora i valori del feed sui conteggi dei tag
   * TODO: troppe categorie, bisogna mettere una soglia sul numero minimo di conteggi...
   */
  def extractTagCounts: Map[String, Number] = feedProperty
    .extractTagStatistics
    .mapValues(_.asInstanceOf[Number])

  //i dati tabellari dei conteggi delle parole, come property
  private val wordsTabular: ListBinding[XYChart.Data[String, Number]] = createListBinding(histogramThresholdProperty) {
    FXCollections.observableArrayList[XYChart.Data[String, Number]](extractCountValues)
  }

  //le serie di istogrammi da inserire nel grafico
  private val wordsSeries: XYChart.Series[String, Number] = new XYChart.Series(wordsTabular)

  //inserisce una coppia di valori in un dato valido per una `XYChart`
  private def toChartData(data: (String, Int)): XYChart.Data[String, Number] = new XYChart.Data(data._1, data._2)

  /**
   * contiene le serie di istogrammi con i conteggi delle parole
   */
  val series: ObservableList[XYChart.Series[String, Number]] = FXCollections.observableArrayList[XYChart.Series[String, Number]](wordsSeries)

}