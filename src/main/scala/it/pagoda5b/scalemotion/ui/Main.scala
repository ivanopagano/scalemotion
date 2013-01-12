package it.pagoda5b.scalemotion.ui

import javafx.application.{ Application => FXApp, Platform }
import javafx.animation._
import javafx.animation.Animation.INDEFINITE
import javafx.stage.{ Stage, WindowEvent }
import javafx.event.ActionEvent
import javafx.scene.SceneBuilder
import javafx.scene.chart._
import javafx.scene.control.{ Label, LabelBuilder, ScrollPaneBuilder }
import javafx.scene.control.ScrollPane.ScrollBarPolicy
import javafx.scene.layout.{ AnchorPane, AnchorPaneBuilder }
import javafx.geometry.Pos._
import javafx.beans.property.SimpleStringProperty
import javafx.util.Builder
import javafx.util.Duration._
import javafx.util.converter.NumberStringConverter

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

  /**
   * costruisce la scena e mostra la finestra
   */
  override def start(stage: Stage) {
    import FXBuilderUtils._
    import FXEventHandlersUtils._

    //titolo della finestra
    stage.setTitle("Stack Overflow Analysis")

    //mostra la soglia minima stabilita per l'istogramma, per impedire di affollare il grafico
    lazy val thresholdLabel: Label = create[LabelBuilder]
      .alignment(TOP_RIGHT)

    //mostra i conteggi delle parole contenute nei feed
    lazy val chart: BarChart[String, Number] = makeBarChart

    //costruisce il contenuto della scena
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
                thresholdLabel)
          }
      }

    thresholdLabel.textProperty.bind(new SimpleStringProperty("Count threshold set to ").concat(GraphsModel.histogramThresholdProperty))

    //allinea etichetta e grafico
    AnchorPane.setTopAnchor(thresholdLabel, 50)
    AnchorPane.setRightAnchor(thresholdLabel, 20)
    AnchorPane.setTopAnchor(chart, 0)
    AnchorPane.setBottomAnchor(chart, 0)
    AnchorPane.setRightAnchor(chart, 0)
    AnchorPane.setLeftAnchor(chart, 0)

    //imposta la scena sullo stage
    stage.setScene(scene)
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