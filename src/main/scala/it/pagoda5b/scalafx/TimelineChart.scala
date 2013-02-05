package it.pagoda5b.scalafx.chart
import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.chart._
import scalafx.beans.property._
import scalafx.collections._
import org.joda.time._
import org.joda.time.format._
import java.lang.Number
import it.pagoda5b.javafx._

/**
 * Un trait da aggiungere ad una LineChart che contiene ha come asse x il tempo e viene aggiornata ogni volta
 * che un nuovo set di dati gli viene passato, controllando autonomamente l'insieme dei dati visualizzati e
 * l'intervallo temporale, in base ad un parametro
 *
 */
trait TimelineBehaviour extends Node with DelayedInit { self: XYChart[String, Number] =>
  import scala.collection._
  import FXBindingsUtils._
  import FXPropertyUtils._
  import javafx.util.converter.{ LongStringConverter, DateTimeStringConverter }

  //i valori iniziali come coppie fra (nome_serie, valore)
  def initialData: Iterable[(String, Number)]
  // il numero di valori temporali mostrati dal grafico
  def xValuesDisplayed: Int
  // quante serie saranno presenti sul grafico (vengono selezionate quelle con i conteggi maggiori)
  def seriesDisplayedProperty: IntegerProperty

  //imposta lo stile di default per i css
  styleClass.add("timeline-chart")

  //meglio che il grafico si adatti alle sue esigenze di visualizzazione
  YAxis.setAutoRanging(true)

  //la formattazione dell'asse temporale
  val timeLabelFormat = DateTimeFormat.forPattern("hh:mm:ss")

  //contiene lo storico di tutte le serie inserite, comprese quelle non visualizzate
  val series: mutable.Map[String, XYChart.Series[String, Number]] = mutable.Map()

  //Rende dinamico il titolo del grafico
  lazy val titleText = createStringBinding(seriesDisplayedProperty) {
    "Top %d feed categories on a total of %d".format(seriesDisplayedProperty.get, series.size)
  }

  //Prepara il primo set di grafici con il gruppo valori iniziali definiti in {{{initialData}}}
  def delayedInit(init: => Unit) {
    init
    import scala.collection.JavaConversions._
    val timeTick = timeLabelFormat.print(DateTime.now)
    series ++= initialData.par.map {
      case (name, y) =>
        val series = newSeries(name, Seq((timeTick, y)))
        (name, series)
    }.seq
    data.get ++= (selectDisplayed map (_.delegate))

    title <== titleText
  }

  /*
   * Crea una nuova serie di dati, con un nome ed un possibile elenco di valori iniziali
   */
  private[this] def newSeries(name: String, initValues: Seq[(String, Number)] = Seq()) = {
    import javafx.scene.{ chart => jfxsc }
    val chartData: Seq[jfxsc.XYChart.Data[String, Number]] = initValues map { case (x: String, y: Number) => new jfxsc.XYChart.Data(x, y) }
    XYChart.Series[String, Number](name, ObservableBuffer(chartData))
  }

  /*
   * estrae le serie che devono essere visualizzate, in base alla {{{seriesDisplayedProperty}}} della timeline
   */
  private[this] def selectDisplayed: Iterable[XYChart.Series[String, Number]] = {
    def extractLastValue(s: XYChart.Series[String, Number]): Int = {
      val dataList = s.data.get
      dataList(dataList.length - 1).getYValue.intValue()
    }

    //ordina le serie e seleziona le prime n, con n pari al valore di seriesDisplayedProperty
    series.values
      .toSeq
      .sortBy(extractLastValue)(Ordering.Int.reverse)
      .take(seriesDisplayedProperty.get)
  }

  /**
   * aggiunge un nuovo insieme di valori alle serie, identificate dal nome
   */
  def pushToSeries(updates: Iterable[(String, Number)]) {
    import scala.collection.JavaConversions._

    //costuisce l'etichetta per questo istante
    val timeTick = timeLabelFormat.print(DateTime.now)

    //converte la chiave dell'aggiornamento nella corrispondente serie (l'oggetto)
    updates.map {
      case (name, y) => (series getOrElseUpdate (name, newSeries(name)), y)
    }
      .foreach {
        case (s, y) =>
          /*
         * aggiunge il nuovo dato alla serie eventualmente rimuovendo 
         * i valori obsoleti, se ce ne sono piu' di quanti previsti
         */
          s.data add (XYChart.Data[String, Number](timeTick, y))
          if (s.data.size > xValuesDisplayed) s.data.remove(0, 1)
      }

    //stabilisce quali serie mostrare, in base alla property
    val displayed = selectDisplayed map (_.delegate)
    data.retainAll(displayed)
    data.get ++= (displayed.filter(s => !data.contains(s)))
    //questo ha l'effetto di riaggiornare il titolo
    titleText.invalidate()

  }

}

import javafx.scene.{ chart => jfxsc }

case class TimelineChart(
  xAxis: CategoryAxis,
  yAxis: NumberAxis,
  val initialData: Iterable[(String, Number)],
  val xValuesDisplayed: Int = 60,
  val seriesDisplayedProperty: IntegerProperty = IntegerProperty(5)) extends LineChart[String, Number](new jfxsc.LineChart[String, Number](xAxis, yAxis)) with TimelineBehaviour

