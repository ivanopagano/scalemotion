package it.pagoda5b.scalemotion.ui

import it.pagoda5b.scalemotion.reader.SOFFeed
import scala.collection.JavaConversions._
import java.util.{ Collection => JCollection }
import javafx.scene.chart._
import javafx.beans.property.SimpleListProperty
import javafx.collections.{ FXCollections, ObservableList }

/**
 * Contiene e gestisce i dati da rappresentare nei grafici
 */
object GraphsModel {

  //il feed da cui estrarre i valori
  private var data = SOFFeed("http://stackoverflow.com/feeds")

  /**
   * aggiorna i valori dei grafici
   */
  def populate() {
    data = data.updateFeed
    wordsValues.setAll(extractValues)
  }

  private def extractValues: JCollection[XYChart.Data[String, Number]] = data
    .extractWordStatistics
    .filter {
      case (_, count) => count > 10
    }
    .map(toChartData)
    .toIterable

  val wordsValues: ObservableList[XYChart.Data[String, Number]] = FXCollections.observableArrayList[XYChart.Data[String, Number]]

  val wordsSeries: XYChart.Series[String, Number] = new XYChart.Series(wordsValues)

  def toChartData(data: (String, Int)): XYChart.Data[String, Number] = new XYChart.Data(data._1, data._2)

  def getSeries: ObservableList[XYChart.Series[String, Number]] = {
    val list = FXCollections.observableArrayList[XYChart.Series[String, Number]]
    list.setAll(wordsSeries)
    list
  }
}