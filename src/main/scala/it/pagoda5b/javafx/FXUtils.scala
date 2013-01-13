package it.pagoda5b.javafx

/**
 * Permette delle semplificazioni per la definizione dell' ''Event Handling''
 */
object FXEventHandlersUtils {
  import javafx.event._

  /**
   * converte una funzione anonima che elabora un [[Event]] in un [[EventHandler]] dello stesso evento,
   * la cui implementazione utilizza la funzione
   */
  implicit def toHandler[E <: Event](handling: E => Unit): EventHandler[E] = new EventHandler[E] {
    def handle(event: E) = handling(event)
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
  import javafx.beans.binding.StringBinding
  import javafx.beans.Observable

  /**
   * costruisce un binding che ha una stringa come risultato
   *
   * @param boundTo Observable a cui il Binding fa riferimneto
   * @param il valore che il binding deve restituire
   */
  def createStringBinding(boundTo: Observable)(computeFunction: => String): StringBinding = new StringBinding {
    //il binding viene invalidato con l'oggetto a cui e' vincolato
    bind(boundTo)
    //calcola il valore usando la funzione passata
    override def computeValue: String = computeFunction
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
  implicit val stackPaneBuild: StackPaneBuilder[_] = StackPaneBuilder.create()
  implicit val anchorPaneBuild: AnchorPaneBuilder[_] = AnchorPaneBuilder.create()
  implicit val scrollPaneBuild: ScrollPaneBuilder[_] = ScrollPaneBuilder.create()
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
  def create[A[B <: A[B]] <: Builder[_]](implicit builder: A[_]) = builder.asInstanceOf[A[_ <: A[_ <: A[_ <: A[_ <: A[_]]]]]]

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
  def createChart[X, Y, A[X, Y, B <: A[X, Y, B]] <: Builder[_]](implicit builder: A[_, _, _]) = builder.asInstanceOf[A[X, Y, _ <: A[X, Y, _ <: A[X, Y, _ <: A[X, Y, _ <: A[X, Y, _ <: A[X, Y, _]]]]]]]

}