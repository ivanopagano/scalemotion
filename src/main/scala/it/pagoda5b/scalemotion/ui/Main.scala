package it.pagoda5b.scalemotion.ui

// import javafx.application.{ Application => FXApp, Platform }
// import javafx.animation._
// import javafx.animation.Animation.INDEFINITE
// import javafx.stage.{ Stage, WindowEvent }
// import javafx.event.ActionEvent
// import javafx.scene.{ Scene, SceneBuilder }
// import javafx.scene.chart._
// import javafx.scene.control.{ Label, Slider, Tab, LabelBuilder, SliderBuilder, TabPaneBuilder, TabBuilder }
// import javafx.scene.control.TabPane.TabClosingPolicy._
// import javafx.scene.layout.{ AnchorPane, AnchorPaneBuilder, StackPaneBuilder }
// import javafx.geometry.Pos._
// import javafx.geometry.Orientation._
// import javafx.geometry.Side._
// import javafx.util.Builder
// import javafx.util.Duration._
// import javafx.util.converter.NumberStringConverter

import it.pagoda5b.javafx._
import it.pagoda5b.javafx.chart.TimelineChart
import it.pagoda5b.javafx.FXPropertyUtils._

import scalafx.application.JFXApp
import scalafx.Includes._
import scalafx.animation._
import scalafx.animation.Animation.INDEFINITE
import scalafx.stage.{ Stage, WindowEvent }
import scalafx.event.ActionEvent
import scalafx.scene.Scene
import scalafx.scene.chart._
import scalafx.scene.control.{ Label, Slider, TabPane, Tab }
import scalafx.scene.control.TabPane.TabClosingPolicy._
import scalafx.scene.layout.AnchorPane
import scalafx.geometry.Pos._
import scalafx.geometry.Orientation._
import scalafx.geometry.Side._
import scalafx.util.Duration._
import scalafx.util.converter.NumberStringConverter

/**
 * La classe principale, lancia l'applicazione JavaFX
 */
object GraphsApp extends JFXApp {
  import FXBuilderUtils._
  import FXEventHandlersUtils._


  /**
   * costruisce la scena e mostra la finestra
   */
  stage = new JFXApp.PrimaryStage {
    title = "Stack Overflow Analysis"
    scene = prepareScene
  }

  stage.sizeToScene()

  //aggiorna i dati dal feed remoto
  {
    val frame = KeyFrame(
      time = (5 s),
      onFinished = {
        GraphsModel.refreshData()
        // tagChart.pushToSeries(GraphsModel.extractTagCounts)
      }
    )
    new Timeline() {
      keyFrames = frame
      cycleCount = INDEFINITE
    }.play
  }

  // override def start(stage: Stage) {

  //   //titolo della finestra
  //   stage.setTitle("Stack Overflow Analysis")

  //   //imposta la scena sullo stage
  //   stage.setScene(prepareScene)
  //   stage.sizeToScene()
  //   //esce in caso di chiusura della finestra
  //   stage.setOnCloseRequest {
  //     (_: WindowEvent) => Platform.exit()
  //   }
  //   //aggiorna i dati dal feed remoto
  //   val updateTimer: Timeline = TimelineBuilder.create
  //     .keyFrames(
  //       new KeyFrame(
  //         seconds(5),
  //         (_: ActionEvent) => {
  //           GraphsModel.refreshData()
  //           tagChart.pushToSeries(GraphsModel.extractTagCounts)
  //         }))
  //     .cycleCount(INDEFINITE)

  //   updateTimer.play()

  //   stage.show()
  // }

  //costruisce il contenuto della scena
  private def prepareScene: Scene = new Scene(
    width = 1140,
    height = 712
  ) {
    stylesheets add "css/style.css"

    //mostra la soglia minima stabilita per l'istogramma, per impedire di affollare il grafico
    lazy val thresholdLabel = new Label {
      alignment = TOP_RIGHT
      styleClass += "text-shadow"
      text <== thresholdText
    }

    //indica il tempo passato dall'inizio dell'esecuzione
    lazy val elapsedLabel = new Label {
      alignment = TOP_RIGHT
      styleClass += "text-shadow"
      text <== elapsedText
    }

    //duplicata per mostrare il dato su entrambi i grafici
    lazy val elapsedLabel2 = new Label {
      alignment = TOP_RIGHT
      styleClass += "text-shadow"
      text <== elapsedText
    }

    //il numero di entry lette da remoto
    lazy val entryCountLabel = new Label {
      alignment = TOP_RIGHT
      styleClass += "text-shadow"
      text <== entryCountText
    }

    //duplicata per mostrare il dato su entrambi i grafici
    lazy val entryCountLabel2 = new Label {
      alignment = TOP_RIGHT
      styleClass += "text-shadow"
      text <== entryCountText
    }

    //controlla la soglia
    lazy val thresholdControl = new Slider {
      min = 5
      max = 50
      majorTickUnit = 1
      minorTickCount = 0
      blockIncrement = 1
      orientation = HORIZONTAL
      value <==> GraphsModel.histogramThresholdProperty
    }

    // lazy val tagChart: TimelineChart = makeTimeline

    //mostra i conteggi delle parole contenute nei feed come grafico
    lazy val countChart: BarChart[String, Number] = makeBarChart

    import FXBindingsUtils._

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


    content = new TabPane() {
      side = RIGHT
      tabClosingPolicy = UNAVAILABLE
      tabMinWidth = 250
    } += new Tab {
      text = "word counts"
      content = new AnchorPane {
        content = Seq(
          countChart,
          elapsedLabel,
          entryCountLabel,
          thresholdLabel,
          thresholdControl
        )
      }
    }//  += new Tab {
    //   text = "top categories"
    //   content = new AnchorPane {
    //     content = Seq(
    //       tagChart,
    //       elapsedLabel,
    //       entryCountLabel
    //     )
    //   }
    // }

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
    // AnchorPane.setTopAnchor(tagChart, 0)
    // AnchorPane.setBottomAnchor(tagChart, 0)
    // AnchorPane.setRightAnchor(tagChart, 0)
    // AnchorPane.setLeftAnchor(tagChart, 0)

  }

  /*
   * crea l'istogramma e lo popola
   */
  private def makeBarChart: BarChart[String, Number] = {
    val xAxis = new CategoryAxis
    val yAxis = new NumberAxis
    val chart = BarChart(
      xAxis = xAxis,
      yAxis = yAxis,
      data = GraphsModel.wordsSeriesList
    )

    chart.title = "Word Histograms for the Stackoverflow feed"
    xAxis.label = "words in the feed summaries"
    yAxis.label = "frequency of appearance"
    yAxis.tickLabelFormatter = new NumberStringConverter(new java.text.DecimalFormat("0"))

    chart
  }


  // /*
  //  * crea la timeline
  //  */
  // private def makeTimeline: TimelineChart = {
  //   import FXBuilderUtils._
  //   import chart.TimelineChart

  //   val timeAxis = CategoryAxisBuilder.create
  //     .label("time")
  //     .build

  //   val countAxis = NumberAxisBuilder.create
  //     .label("feed entries")
  //     .tickLabelFormatter(new NumberStringConverter(new java.text.DecimalFormat("0.0")))
  //     .build

  //   new TimelineChart(timeAxis, countAxis, GraphsModel.extractTagCounts)
  // }

}