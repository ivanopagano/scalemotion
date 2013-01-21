package it.pagoda5b.javafx

/**
 * Permette delle semplificazioni per la definizione dell' ''Event Handling''
 */
object FXEventHandlersUtils {
  import java.lang.Runnable
  import javafx.event._

  /**
   * converte una funzione anonima che elabora un [[Event]] in un [[EventHandler]] dello stesso evento,
   * la cui implementazione utilizza la funzione
   */
  implicit def toHandler[E <: Event](handling: E => Unit): EventHandler[E] = new EventHandler[E] {
    def handle(event: E) = handling(event)
  }

  /**
   * converte una funzione anonima generica senza risultati in un [[java.lang.Runnable]] che la esegue
   */
  implicit def toRunnable(runCode: => Unit): Runnable = new Runnable {
    def run() = runCode
  }

}

/**
 * Semplifica l'utilizzo delle javafx [[Property]] e simili
 */
object FXPropertyUtils {
  import javafx.beans.value._

  //Un tipo strutturale, identificato dalla presenza del metodo `modify`
  type MOD[A] = { def modify(f: A => A): Unit }

  /**
   * converte un qualunque [[WritableValue]] in un ''wrapper'' che permette
   * di trasformare il valore contenuto combinando `getValue/setValue`
   *
   * e.g. invece di chiamare `myProperty.set(myProperty.get + 1)` è sufficiente `myProperty.modify(_ + 1)`
   */
  implicit def toModifiableProperty[A](p: WritableValue[A]): MOD[A] = new {
    def modify(f: A => A) {
      p.setValue(f(p.getValue))
    }
  }

  /**
   * estrae in modo implicito il valore da un [[ObservableValue]], se necessario per chiamare un metodo
   *
   * e.g. invece di chiamare `myProperty.get.myCall` &egrave; sufficiente `myProperty.myCall`
   */
  implicit def observableToValue[A](o: ObservableValue[A]): A = o.getValue
}

object FXBindingsUtils {
  import javafx.beans.binding.{ StringBinding, ListBinding }
  import javafx.beans.Observable
  import javafx.collections.ObservableList

  /**
   * costruisce un binding che ha una stringa come risultato
   *
   * @param dependency [[Observable]] a cui il [[Binding]] fa riferimneto
   * @param il valore che il binding deve restituire
   */
  def createStringBinding(dependency: Observable*)(computeFunction: => String): StringBinding = new StringBinding {
    //il binding viene invalidato con l'oggetto a cui e' vincolato
    for (o <- dependency) bind(o)
    //calcola il valore usando la funzione passata
    override def computeValue: String = computeFunction
  }

  /**
   * costruisce un binding che ha una `ObservableList` come risultato
   *
   * @param dependency [[Observable]] a cui il [[Binding]] fa riferimneto
   * @param il valore che il binding deve restituire
   */
  def createListBinding[A](dependency: Observable*)(computeFunction: => ObservableList[A]): ListBinding[A] = new ListBinding[A] {
    //il binding viene invalidato con l'oggetto a cui e' vincolato
    for (o <- dependency) bind(o)
    //calcola il valore usando la funzione passata
    override def computeValue: ObservableList[A] = computeFunction
  }

}

/**
 * Aggiunge delle facilitazioni per l'utilizzo delle ''fluent api'' utilizzate
 * dalle classi *Builder dei componenti ''JavaFX''
 */
object FXBuilderUtils {
  import javafx.scene.SceneBuilder
  import javafx.scene.chart.BarChartBuilder
  import javafx.scene.control._
  import javafx.scene.layout._
  import javafx.util.Builder

  /**
   * converte il [[Builder]] nell'oggetto creato, senza eseguire il `build()` esplicitamente
   */
  implicit def builderToObject[A](b: Builder[A]): A = b.build()

  /*
   * le istanze implicite servono ai metodi create* per trovare
   * l'argomento implicito in base al type parameter richiesto
   */
  implicit val sceneBuild: SceneBuilder[_] = SceneBuilder.create()
  implicit val buttonBuild: ButtonBuilder[_] = ButtonBuilder.create()
  implicit val labelBuild: LabelBuilder[_] = LabelBuilder.create()
  implicit val sliderBuild: SliderBuilder[_] = SliderBuilder.create()
  implicit val stackPaneBuild: StackPaneBuilder[_] = StackPaneBuilder.create()
  implicit val anchorPaneBuild: AnchorPaneBuilder[_] = AnchorPaneBuilder.create()
  implicit val scrollPaneBuild: ScrollPaneBuilder[_] = ScrollPaneBuilder.create()
  implicit val tabPaneBuild: TabPaneBuilder[_] = TabPaneBuilder.create()
  implicit val tabBuild: TabBuilder[_] = TabBuilder.create()
  implicit val barChartBuild: BarChartBuilder[_, _, _] = BarChartBuilder.create()

  /**
   * Type Parameter issue workaround: see [[https://issues.scala-lang.org/browse/SI-6169]]
   *
   * Questa serie di metodi converte correttamente il valore di ritorno del [[Builder]], rispetto alla
   * parametrizzazione generica, che non permetterebbe di concatenare le chiamate in Scala
   *
   * *Nota*: il builder restituito &egrave; sempre lo stesso, ossia l'istanza implicita, quindi il meccanismo
   * va riverificato nel caso servisse utilizzare il builder pi&ugrave; volte, per controllare che le chiamate che impostano
   * le property non restituiscano lo stesso oggetto modificato, ma una nuova istanza
   */
  def create[A[B <: A[B]] <: Builder[_]](implicit builder: A[_]) = builder.asInstanceOf[A[_ <: A[_ <: A[_ <: A[_ <: A[_ <: A[_ <: A[_ <: A[_]]]]]]]]]

  /**
   * Type Parameter issue workaround: see [[https://issues.scala-lang.org/browse/SI-6169]]
   *
   * Questa serie di metodi converte correttamente il valore di ritorno del [[Builder]], rispetto alla
   * parametrizzazione generica, che non permetterebbe di concatenare le chiamate in Scala
   *
   * *Nota*: il builder restituito &egrave; sempre lo stesso, ossia l'istanza implicita, quindi il meccanismo
   * va riverificato nel caso servisse utilizzare il builder pi&ugrave; volte, per controllare che le chiamate che impostano
   * le property non restituiscano lo stesso oggetto modificato, ma una nuova istanza
   */
  def createChart[X, Y, A[X, Y, B <: A[X, Y, B]] <: Builder[_]](implicit builder: A[_, _, _]) =
    builder.asInstanceOf[A[X, Y, _ <: A[X, Y, _ <: A[X, Y, _ <: A[X, Y, _ <: A[X, Y, _ <: A[X, Y, _ <: A[X, Y, _ <: A[X, Y, _]]]]]]]]]

}

//package nesting
package chart {
  import javafx.scene.chart._
  import javafx.beans.property._
  import org.joda.time._
  import org.joda.time.format._
  import java.lang.Number
  import javafx.collections.FXCollections._

  /**
   * Una sottoclasse di XYChart che contiene ha come asse x il tempo e viene aggiornata ogni volta
   * che un nuovo set di dati gli viene passato, controllando autonomamente l'insieme dei dati visualizzati e
   * l'intervallo temporale, in base ad un parametro
   *
   * @param xAxis l'asse x, deve contenere dei valori stringa, che indicheranno il momento in cui vengono aggiunti i dati
   * @param yAxis l'asse y, contiene dei numeri
   * @param initialData i valori iniziali come coppie fra (nome_serie, valore)
   * @param xValuesDisplayed il numero di valori temporali mostrati dal grafico
   * @param seriesDisplayedProperty quante serie saranno presenti sul grafico (vengono selezionate quelle con i conteggi maggiori)
   */
  class TimelineChart(
    xAxis: CategoryAxis,
    yAxis: NumberAxis,
    initialData: Iterable[(String, Number)],
    xValuesDisplayed: Int = 60,
    seriesDisplayedProperty: IntegerProperty = new SimpleIntegerProperty(5))
    extends LineChart[String, Number](xAxis, yAxis) {

    import scala.collection._
    import javafx.util.converter.{ LongStringConverter, DateTimeStringConverter }
    import FXPropertyUtils._
    import FXBindingsUtils._

    //meglio che il grafico si adatti alle sue esigenze di visualizzazione
    yAxis.setAutoRanging(true)

    //la formattazione dell'asse temporale
    val timeLabelFormatProperty = new SimpleObjectProperty[DateTimeFormatter](DateTimeFormat.forPattern("hh:mm:ss"))

    //contiene lo storico di tutte le serie inserite, comprese quelle non visualizzate
    val series: mutable.Map[String, XYChart.Series[String, Number]] = mutable.Map()

    //Prepara il primo set di grafici con i valori iniziali passati nel costruttore
    {
      import scala.collection.JavaConversions._
      val timeTick = timeLabelFormatProperty.print(DateTime.now)
      series ++= initialData.par.map {
        case (name, y) => (name, new XYChart.Series[String, Number](name, observableArrayList(new XYChart.Data[String, Number](timeTick, y))))
      }.seq
      dataProperty.addAll(selectDisplayed)
    }

    //Rende dinamico il titolo del grafico
    val titleString = createStringBinding(seriesDisplayedProperty) {
      "Top %d feed categories on a total of %d".format(seriesDisplayedProperty.get, series.size)
    }
    titleProperty.bind(titleString)

    /*
     * estrae le serie che devono essere visualizzate, in base alla {{{seriesDisplayedProperty}}} della timeline
     */
    private[this] def selectDisplayed: Iterable[XYChart.Series[String, Number]] = {
      def extractLastValue(s: XYChart.Series[String, Number]): Int = {
        val data = s.getData()
        data.get(data.size - 1).getYValue.intValue()
      }

      //ordina le serie e seleziona le prime n, con n pari al valore di seriesDisplayedProperty
      series
        .values
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
      val timeTick = timeLabelFormatProperty.print(DateTime.now)

      //converte la chiave dell'aggiornamento nella corrispondente serie (l'oggetto)
      updates.map {
        case (name, y) => (series getOrElseUpdate (name, new XYChart.Series[String, Number](name, observableArrayList[XYChart.Data[String, Number]])), y)
      }
        .foreach {
          case (s, y) =>
            /*
             * aggiunge il nuovo dato alla serie eventualmente rimuovendo 
             * i valori obsoleti, se ce ne sono piu' di quanti previsti
             */
            s.dataProperty add (new XYChart.Data(timeTick, y))
            if (s.dataProperty.size > xValuesDisplayed) s.dataProperty.remove(0, 1)
        }

      //stabilisce quali serie mostrare, in base alla property
      val displayed = selectDisplayed
      dataProperty.retainAll(displayed)
      dataProperty.addAll(displayed.filter(s => !dataProperty.contains(s)))
      titleString.invalidate()

    }

  }

}