package it.pagoda5b.scalemotion.ui

import it.pagoda5b.scalemotion.reader.SOFFeed
import scala.collection.JavaConversions._
import scala.math.Ordering
import java.util.{ Collection => JCollection, List => JList }
import javafx.scene.chart._
import javafx.beans.property._
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
   * aggiorna i dati, e i valori dei grafici se ci sono dati aggiornati
   */
  def refreshData() {
    feedProperty.set(feedProperty.get.updateFeed)
    if (feedProperty.get.freshDataAvailable) wordsValues.setAll(extractValues)
  }

  /*
   * elabora i valori forniti dal feed
   */
  private def extractValues: JList[XYChart.Data[String, Number]] = feedProperty.get
    .extractWordStatistics
    .filter {
      case (_, count) => count >= histogramThresholdProperty.get
    }
    .toSeq
    .sortBy { case (key, count) => count }(Ordering.Int.reverse)
    .map(toChartData)

  //i dati tabellari dei conteggi delle parole
  private val wordsValues: ObservableList[XYChart.Data[String, Number]] = FXCollections.observableArrayList[XYChart.Data[String, Number]](extractValues)

  //le serie di istogrammi da inserire nel grafico
  private val wordsSeries: XYChart.Series[String, Number] = new XYChart.Series(wordsValues)

  //inserisce una coppia di valori in un dato valido per una {{{XYChart}}}
  private def toChartData(data: (String, Int)): XYChart.Data[String, Number] = new XYChart.Data(data._1, data._2)

  /**
   * contiene le serie di istogrammi con i conteggi delle parole
   */
  val series: ObservableList[XYChart.Series[String, Number]] = FXCollections.observableArrayList[XYChart.Series[String, Number]](wordsSeries)
}