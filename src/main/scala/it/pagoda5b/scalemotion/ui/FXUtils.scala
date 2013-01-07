package it.pagoda5b.scalemotion.ui

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
   * converte il [[Builder]] nell'oggetto creato, senza eseguire il {{{build()}}} esplicitamente
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
   */
  def create[A[B <: A[B]] <: Builder[_]](implicit builder: A[_]) = builder.asInstanceOf[A[_ <: A[_ <: A[_ <: A[_ <: A[_]]]]]]

  /**
   * Type Parameter issue workaround: see [[https://issues.scala-lang.org/browse/SI-6169]]
   *
   * Questa serie di metodi converte correttamente il valore di ritorno del [[Builder]], rispetto alla
   * parametrizzazione generica, che non permetterebbe di concatenare le chiamate in Scala
   */
  def createChart[X, Y, A[X, Y, B <: A[X, Y, B]] <: Builder[_]](implicit builder: A[_, _, _]) = builder.asInstanceOf[A[X, Y, _ <: A[X, Y, _ <: A[X, Y, _ <: A[X, Y, _ <: A[X, Y, _ <: A[X, Y, _]]]]]]]

}