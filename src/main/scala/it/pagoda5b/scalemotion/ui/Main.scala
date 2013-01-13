package it.pagoda5b.scalemotion.ui

import javafx.application.{ Application => FXApp, Platform }
import javafx.animation._
import javafx.animation.Animation.INDEFINITE
import javafx.stage.{ Stage, WindowEvent }
import javafx.event.ActionEvent
import javafx.scene.{ Scene, SceneBuilder }
import javafx.scene.chart._
import javafx.scene.control.{ Label, Slider, LabelBuilder, ScrollPaneBuilder, SliderBuilder }
import javafx.scene.control.ScrollPane.ScrollBarPolicy
import javafx.scene.layout.{ AnchorPane, AnchorPaneBuilder }
import javafx.geometry.Pos._
import javafx.geometry.Orientation._
import javafx.util.Builder
import javafx.util.Duration._
import javafx.util.converter.NumberStringConverter
import it.pagoda5b.javafx._
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
          (_: ActionEvent) => GraphsModel.refreshData()))
      .cycleCount(INDEFINITE)

    updateTimer.play()

    stage.show()
  }

  //costruisce il contenuto della scena
  private def prepareScene: Scene = {
    import FXBuilderUtils._
    import FXBindingsUtils._

    //mostra la soglia minima stabilita per l'istogramma, per impedire di affollare il grafico
    lazy val thresholdLabel: Label = create[LabelBuilder]
      .alignment(TOP_RIGHT)

    lazy val thresholdControl: Slider = create[SliderBuilder]
      .min(5)
      .max(50)
      .majorTickUnit(1)
      .minorTickCount(0)
      .blockIncrement(1)
      .orientation(HORIZONTAL)

    //indica il tempo passato dall'inizio dei conteggi
    lazy val elapsedLabel: Label = create[LabelBuilder]
      .alignment(TOP_RIGHT)

    //il numero di entry utilizzate per il conteggio
    lazy val entryCountLabel: Label = create[LabelBuilder]
      .alignment(TOP_RIGHT)

    //mostra i conteggi delle parole contenute nei feed come grafico
    lazy val chart: BarChart[String, Number] = makeBarChart

    val scene = create[SceneBuilder]
      .width(800)
      .height(600)
      .root {
        create[ScrollPaneBuilder]
          .fitToWidth(true)
          .fitToHeight(true)
          .hbarPolicy(ScrollBarPolicy.AS_NEEDED)
          .content {
            create[AnchorPaneBuilder]
              .children(
                chart,
                elapsedLabel,
                entryCountLabel,
                thresholdLabel,
                thresholdControl)
          }
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

    //imposta dinamicamente il valore dei controlli in base al modello
    thresholdControl.valueProperty.bindBidirectional(GraphsModel.histogramThresholdProperty)
    thresholdLabel.textProperty.bind(thresholdText)
    elapsedLabel.textProperty.bind(elapsedText)
    entryCountLabel.textProperty.bind(entryCountText)

    //allinea i controlli e il grafico
    AnchorPane.setTopAnchor(elapsedLabel, 50)
    AnchorPane.setRightAnchor(elapsedLabel, 20)
    AnchorPane.setTopAnchor(entryCountLabel, 70)
    AnchorPane.setRightAnchor(entryCountLabel, 20)
    AnchorPane.setTopAnchor(thresholdLabel, 90)
    AnchorPane.setRightAnchor(thresholdLabel, 20)
    AnchorPane.setTopAnchor(thresholdControl, 110)
    AnchorPane.setRightAnchor(thresholdControl, 20)
    AnchorPane.setTopAnchor(chart, 0)
    AnchorPane.setBottomAnchor(chart, 0)
    AnchorPane.setRightAnchor(chart, 0)
    AnchorPane.setLeftAnchor(chart, 0)

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
      .data(GraphsModel.series)
      .build

  }

}