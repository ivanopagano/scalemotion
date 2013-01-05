package it.pagoda5b.scalemotion.ui

import javafx.application.{ Application => FXApp, Platform }
import javafx.stage.{ Stage, WindowEvent }
import javafx.scene.SceneBuilder
import javafx.scene.chart._
import javafx.scene.control.ButtonBuilder
import javafx.scene.layout.StackPaneBuilder
import javafx.util.Builder

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
        create[StackPaneBuilder]
          .children(makeBarChart)
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
          .tickUnit(1.0)
          .build
      }
      .animated(true)
      .data(GraphsModel.getSeries)
      .build
  }

}