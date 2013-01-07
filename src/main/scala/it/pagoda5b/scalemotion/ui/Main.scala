package it.pagoda5b.scalemotion.ui

import javafx.application.{ Application => FXApp, Platform }
import javafx.stage.{ Stage, WindowEvent }
import javafx.scene.SceneBuilder
import javafx.scene.chart._
import javafx.scene.control.{ ButtonBuilder, ScrollPaneBuilder }
import javafx.scene.control.ScrollPane.ScrollBarPolicy
import javafx.scene.layout.StackPaneBuilder
import javafx.beans.property.SimpleStringProperty
import javafx.util.Builder
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

    stage.setTitle("Stack Overflow Analisys")

    val scene = create[SceneBuilder]
      .width(800)
      .height(600)
      .root {
        create[ScrollPaneBuilder]
          .fitToWidth(true)
          .fitToHeight(true)
          .hbarPolicy(ScrollBarPolicy.AS_NEEDED)
          .content {
            create[StackPaneBuilder]
              .children(makeBarChart)
          }
      }

    stage.setScene(scene)
    stage.sizeToScene()
    stage.setOnCloseRequest {
      (e: WindowEvent) => Platform.exit()
    }
    GraphsModel.populate()
    stage.show()

  }

  private def makeBarChart: BarChart[String, Number] = {
    import FXBuilderUtils._

    val chart = createChart[String, Number, BarChartBuilder]
      .XAxis {
        CategoryAxisBuilder.create
          .label("words in the feed summaries")
          .build
      }
      .YAxis {
        NumberAxisBuilder.create
          .label("frequency of appearance")
          .tickUnit(1.0)
          .tickLabelFormatter(new NumberStringConverter(new java.text.DecimalFormat("0")))
          .minorTickVisible(false)
          .build
      }
      .animated(true)
      .data(GraphsModel.getSeries)
      .build

    chart.titleProperty.bind(new SimpleStringProperty("Word Histograms for the Stackoverflow feed\nwith a count of at least ").concat(GraphsModel.histogramThresholdProperty))
    chart
  }

}