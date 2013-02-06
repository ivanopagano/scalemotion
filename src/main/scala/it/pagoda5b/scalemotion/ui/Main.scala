package it.pagoda5b.scalemotion.ui

import it.pagoda5b.javafx._
import it.pagoda5b.scalafx.chart.TimelineChart

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

  //mostra i conteggi piu' alti dei tag nei feed, nel tempo
  lazy val tagChart: TimelineChart = makeTimeline

  //mostra i conteggi delle parole contenute nei feed come grafico
  lazy val countChart: BarChart[String, Number] = makeBarChart

  /**
   * costruisce la scena e mostra la finestra
   */
  stage = new JFXApp.PrimaryStage {
    title = "Stack Overflow Analysis"
    scene = prepareScene
  }

  stage.sizeToScene()

  //aggiorna i dati dal feed remoto
  val loop = Timeline(
    Seq(KeyFrame(
      time = (5 s), //durata del frame di animazione
      onFinished = { (_: ActionEvent) =>
        GraphsModel.refreshData()
        tagChart.pushToSeries(GraphsModel.extractTagCounts)
      })))
  loop.cycleCount = INDEFINITE //l'animazione si ripete all'infinito
  loop.play

  //costruisce il contenuto della scena
  private def prepareScene: Scene = {
    import FXBindingsUtils._
    import FXPropertyUtils._
    import scalafx.geometry.Pos

    //il testo per la soglia
    def thresholdText = createStringBinding(GraphsModel.histogramThresholdProperty) {
      "count lower threshold is %d".format(GraphsModel.histogramThresholdProperty.intValue)
    }

    //il testo per il tempo trascorso
    def elapsedText = createStringBinding(GraphsModel.elapsedTimeProperty) {
      "count began %s".format(GraphsModel.elapsedTimeProperty.getValueSafe)
    }

    //il testo per il numero di entry
    def entryCountText = createStringBinding(GraphsModel.feed) {
      "%d feed entries were processed".format(GraphsModel.feed.entries.size)
    }

    //mostra la soglia minima stabilita per l'istogramma, per impedire di affollare il grafico
    val thresholdLabel = new Label {
      alignment = TOP_RIGHT
      styleClass += "text-shadow"
      text <== thresholdText
    }

    //indica il tempo passato dall'inizio dell'esecuzione
    val elapsedLabel = new Label {
      alignment = TOP_RIGHT
      styleClass += "text-shadow"
      text <== elapsedText
    }

    //duplicata per mostrare il dato su entrambi i grafici
    val elapsedLabel2 = new Label {
      alignment = TOP_LEFT
      styleClass += "text-shadow"
      text <== elapsedText
    }

    //il numero di entry lette da remoto
    val entryCountLabel = new Label {
      alignment = TOP_RIGHT
      styleClass += "text-shadow"
      text <== entryCountText
    }

    //duplicata per mostrare il dato su entrambi i grafici
    val entryCountLabel2 = new Label {
      alignment = TOP_LEFT
      styleClass += "text-shadow"
      text <== entryCountText
    }

    //controlla la soglia
    val thresholdControl = new Slider {
      min = 5
      max = 50
      majorTickUnit = 1
      minorTickCount = 0
      blockIncrement = 1
      orientation = HORIZONTAL
      value <==> GraphsModel.histogramThresholdProperty
    }

    val root = new TabPane() {
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
          thresholdControl)
      }
    } += new Tab {
      text = "top categories"
      content = new AnchorPane {
        content = Seq(
          tagChart,
          elapsedLabel2,
          entryCountLabel2)
      }
    }

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

    //costruita cosi' la scena occupa tutta la dimensione dello stage
    new Scene(new javafx.scene.Scene(root, 1140, 712)) {
      stylesheets add "css/style.css"
    }

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
      data = GraphsModel.wordsSeriesList)

    chart.title = "Word Histograms for the Stackoverflow feed"
    xAxis.label = "words in the feed summaries"
    yAxis.label = "frequency of appearance"
    yAxis.tickLabelFormatter = new NumberStringConverter(new java.text.DecimalFormat("0"))

    chart
  }

  /*
   * crea la timeline
   */
  private def makeTimeline: TimelineChart = {
    import it.pagoda5b.scalafx.chart.TimelineChart

    val timeAxis = new CategoryAxis
    val countAxis = new NumberAxis

    timeAxis.label = "time"
    countAxis.label = "feed entries"
    countAxis.tickLabelFormatter = new NumberStringConverter(new java.text.DecimalFormat("0.0"))

    TimelineChart(
      xAxis = timeAxis,
      yAxis = countAxis,
      initialData = GraphsModel.extractTagCounts)
  }

}