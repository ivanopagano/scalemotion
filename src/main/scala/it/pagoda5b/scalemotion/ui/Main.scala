package it.pagoda5b.scalemotion.ui

import javafx.application.{ Application => FXApp, Platform }
import javafx.animation._
import javafx.animation.Animation.INDEFINITE
import javafx.stage.{ Stage, WindowEvent }
import javafx.event.ActionEvent
import javafx.scene.{ Scene, SceneBuilder }
import javafx.scene.chart._
import javafx.scene.control.{ Label, Slider, Tab, LabelBuilder, SliderBuilder, TabPaneBuilder, TabBuilder }
import javafx.scene.control.TabPane.TabClosingPolicy._
import javafx.scene.layout.{ AnchorPane, AnchorPaneBuilder, StackPaneBuilder }
import javafx.scene.effect.{ DropShadow, DropShadowBuilder }
import javafx.geometry.Pos._
import javafx.geometry.Orientation._
import javafx.geometry.Side._
import javafx.util.Builder
import javafx.util.Duration._
import javafx.util.converter.NumberStringConverter
import it.pagoda5b.javafx._
import it.pagoda5b.javafx.chart.TimelineChart
import it.pagoda5b.javafx.FXPropertyUtils._

/**
 * La classe principale, lancia l'applicazione JavaFX
 */
object GraphsApp extends App {
  FXApp.launch(classOf[GraphsApp], args: _*)
}

/**
 * L'istanza dell'applicazione grafica che mostra le statistiche sui feed
 */
class GraphsApp extends FXApp {
  import FXBuilderUtils._
  import FXEventHandlersUtils._

  lazy val tagChart: TimelineChart = makeTimeline

  /**
   * costruisce la scena e mostra la finestra
   */
  override def start(stage: Stage) {

    //titolo della finestra
    stage.setTitle("Stack Overflow Analysis")

    //imposta la scena sullo stage
    stage.setScene(prepareScene)
    stage.sizeToScene()
    //esce in caso di chiusura della finestra
    stage.setOnCloseRequest {
      (_: WindowEvent) => Platform.exit()
    }
    //aggiorna i dati dal feed remoto
    val updateTimer: Timeline = TimelineBuilder.create
      .keyFrames(
        new KeyFrame(
          seconds(5),
          (_: ActionEvent) => {
            GraphsModel.refreshData()
            tagChart.pushToSeries(GraphsModel.extractTagCounts)
          }))
      .cycleCount(INDEFINITE)

    updateTimer.play()

    stage.show()
  }

  //costruisce il contenuto della scena
  private def prepareScene: Scene = {
    import FXBuilderUtils._
    import FXBindingsUtils._
    import javafx.scene.paint.Color._

    //un effetto ombra per migliorare la leggibilita' delle scritte
    lazy val textShadow: DropShadow = create[DropShadowBuilder]
      .offsetX(2.0)
      .offsetY(2.0)
      .color(DARKGREY)

    //mostra la soglia minima stabilita per l'istogramma, per impedire di affollare il grafico
    lazy val thresholdLabel: Label = create[LabelBuilder]
      .alignment(TOP_RIGHT)
      .effect(textShadow)

    lazy val thresholdControl: Slider = create[SliderBuilder]
      .min(5)
      .max(50)
      .majorTickUnit(1)
      .minorTickCount(0)
      .blockIncrement(1)
      .orientation(HORIZONTAL)

    //indica il tempo passato dall'inizio dell'esecuzione
    lazy val elapsedLabel: Label = create[LabelBuilder]
      .alignment(TOP_RIGHT)
      .effect(textShadow)

    //il numero di entry lette da remoto
    lazy val entryCountLabel: Label = create[LabelBuilder]
      .alignment(TOP_RIGHT)
      .effect(textShadow)

    //duplicata per mostrare il dato su entrambi i grafici
    lazy val elapsedLabel2: Label = create[LabelBuilder]
      .alignment(BOTTOM_LEFT)
      .effect(textShadow)

    //duplicata per mostrare il dato su entrambi i grafici
    lazy val entryCountLabel2: Label = create[LabelBuilder]
      .alignment(BOTTOM_LEFT)
      .effect(textShadow)

    //mostra i conteggi delle parole contenute nei feed come grafico
    lazy val countChart: BarChart[String, Number] = makeBarChart

    val scene = create[SceneBuilder]
      .width(1140)
      .height(712)
      .root {
        create[TabPaneBuilder]
          .side(RIGHT)
          .tabClosingPolicy(UNAVAILABLE)
          .tabMinWidth(250)
          .tabs(
            create[TabBuilder]
              .text("word counts")
              .content {
                create[AnchorPaneBuilder]
                  .children(
                    countChart,
                    elapsedLabel,
                    entryCountLabel,
                    thresholdLabel,
                    thresholdControl)
              },
            create[TabBuilder]
              .text("top categories")
              .content {
                create[AnchorPaneBuilder]
                  .children(
                    tagChart,
                    elapsedLabel2,
                    entryCountLabel2)
              })
      }

    //il testo per la soglia
    val thresholdText = createStringBinding(GraphsModel.histogramThresholdProperty) {
      "count lower threshold is %d".format(GraphsModel.histogramThresholdProperty.intValue)
    }

    //il testo per il tempo trascorso
    val elapsedText = createStringBinding(GraphsModel.elapsedTimeProperty) {
      "count began %s".format(GraphsModel.elapsedTimeProperty.getValueSafe)
    }

    //il testo per il numero di entry
    val entryCountText = createStringBinding(GraphsModel.feedProperty) {
      "%d feed entries were processed".format(GraphsModel.feedProperty.entries.size)
    }

    //duplicati per utilizzarli sulle etichette duplicate
    // javafx non prevede il riutilizzo dei bindings?
    val elapsedText2 = createStringBinding(GraphsModel.elapsedTimeProperty) {
      "count began %s".format(GraphsModel.elapsedTimeProperty.getValueSafe)
    }

    val entryCountText2 = createStringBinding(GraphsModel.feedProperty) {
      "%d feed entries were processed".format(GraphsModel.feedProperty.entries.size)
    }

    //imposta dinamicamente il valore dei controlli in base al modello
    thresholdControl.valueProperty.bindBidirectional(GraphsModel.histogramThresholdProperty)
    thresholdLabel.textProperty.bind(thresholdText)
    elapsedLabel.textProperty.bind(elapsedText)
    elapsedLabel2.textProperty.bind(elapsedText2)
    entryCountLabel.textProperty.bind(entryCountText)
    entryCountLabel2.textProperty.bind(entryCountText2)

    //allinea i controlli e il grafico
    AnchorPane.setTopAnchor(elapsedLabel, 50)
    AnchorPane.setRightAnchor(elapsedLabel, 20)
    AnchorPane.setTopAnchor(entryCountLabel, 70)
    AnchorPane.setRightAnchor(entryCountLabel, 20)
    AnchorPane.setTopAnchor(thresholdLabel, 90)
    AnchorPane.setRightAnchor(thresholdLabel, 20)
    AnchorPane.setTopAnchor(thresholdControl, 110)
    AnchorPane.setRightAnchor(thresholdControl, 20)
    AnchorPane.setTopAnchor(countChart, 0)
    AnchorPane.setBottomAnchor(countChart, 0)
    AnchorPane.setRightAnchor(countChart, 0)
    AnchorPane.setLeftAnchor(countChart, 0)
    AnchorPane.setBottomAnchor(elapsedLabel2, 120)
    AnchorPane.setLeftAnchor(elapsedLabel2, 80)
    AnchorPane.setBottomAnchor(entryCountLabel2, 100)
    AnchorPane.setLeftAnchor(entryCountLabel2, 80)
    AnchorPane.setTopAnchor(tagChart, 0)
    AnchorPane.setBottomAnchor(tagChart, 0)
    AnchorPane.setRightAnchor(tagChart, 0)
    AnchorPane.setLeftAnchor(tagChart, 0)

    //restituisce la scena costruita
    scene
  }

  /*
   * crea l'istogramma e lo popola
   */
  private def makeBarChart: BarChart[String, Number] = {
    import FXBuilderUtils._

    createChart[String, Number, BarChartBuilder]
      .title("Word Histograms for the Stackoverflow feed")
      .XAxis {
        CategoryAxisBuilder.create
          .label("words in the feed summaries")
          .build
      }
      .YAxis {
        NumberAxisBuilder.create
          .label("frequency of appearance")
          .tickUnit(1)
          .tickLabelFormatter(new NumberStringConverter(new java.text.DecimalFormat("0")))
          .minorTickVisible(false)
          .build
      }
      .animated(true)
      .data(GraphsModel.wordsSeriesList)
      .verticalGridLinesVisible(false)
      .legendVisible(false)
      .build

  }

  /*
   * crea la timeline
   */
  private def makeTimeline: TimelineChart = {
    import FXBuilderUtils._
    import chart.TimelineChart

    val timeAxis = CategoryAxisBuilder.create
      .label("time")
      .build

    val countAxis = NumberAxisBuilder.create
      .label("feed entries")
      .tickUnit(1)
      .tickLabelFormatter(new NumberStringConverter(new java.text.DecimalFormat("0.0")))
      .minorTickVisible(false)
      .build

    new TimelineChart(timeAxis, countAxis, GraphsModel.extractTagCounts) {
      import javafx.beans.property._
      import javafx.geometry.Side

      alternativeRowFillVisibleProperty.set(false);
      legendSideProperty.set(RIGHT)
    }
  }

}