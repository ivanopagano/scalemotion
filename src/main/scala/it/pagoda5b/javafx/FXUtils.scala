package it.pagoda5b.javafx

/**
 * Permette delle semplificazioni per la definizione dell' ''Event Handling''
 */
object FXEventHandlersUtils {
  import java.lang.Runnable

  /**
   * converte una funzione anonima generica senza risultati in un [[java.lang.Runnable]] che la esegue
   */
  implicit def toRunnable(runCode: => Unit): Runnable = new Runnable {
    def run() = runCode
  }

}

/**
 * Semplifica l'utilizzo delle scalafx [[Property]] e simili
 */
object FXPropertyUtils {
  import scalafx.beans.property._
  import scalafx.beans.value._

  //Un tipo strutturale, identificato dalla presenza del metodo `modify`
  type MOD[A] = { def modify(f: A => A): Unit }

  /**
   * converte un qualunque [[ObjectProperty]] in un ''wrapper'' che permette
   * di trasformare il valore contenuto combinando `getValue/setValue`
   *
   * e.g. invece di chiamare `myProperty.value = myProperty.value + 1` &egrave; sufficiente `myProperty.modify(_ + 1)`
   */
  implicit def toModifiableProperty[A <: AnyRef](p: ObjectProperty[A]): MOD[A] = new {
    def modify(f: A => A) {
      p.value = f(p.value)
    }
  }

  /**
   * estrae in modo implicito il valore da un [[ObservableValue]], se necessario per chiamare un metodo
   *
   * e.g. invece di chiamare `myProperty.value.myCall` &egrave; sufficiente `myProperty.myCall`
   */
  implicit def observableToValue[A](o: ObservableValue[A, _]): A = o()

  implicit def observableToValue[A](o: javafx.beans.value.ObservableValue[A]): A = o.getValue
}

object FXBindingsUtils {
  import scalafx.Includes._
  import scalafx.beans.binding.StringBinding
  import scalafx.beans.Observable
  import scalafx.collections._
  import javafx.collections.ObservableList
  import javafx.beans.{ binding => jfxbb }
  import jfxbb.ListBinding

  /**
   * Aggiunge la possibilita' ad un [[ObservableBuffer]] di essere invalidato
   */
  private trait ObservableBound[T] { self: ObservableBuffer[T] =>
    def binding: ListBinding[T]

    def invalidate() {
      binding.invalidate()
    }

  }

  /**
   * Un [[ObservableBuffer]] vincolato ad un [[ListBinding]], che pu&ograve; essere usato per invalidarlo
   */
  case class ObservableBoundBuffer[T](val binding: ListBinding[T]) extends ObservableBuffer[T](binding) with ObservableBound[T]

  /**
   * costruisce un binding che ha una stringa come risultato
   *
   * @param dependency [[Observable]] a cui il [[Binding]] fa riferimneto
   * @param il valore che il binding deve restituire
   */
  def createStringBinding(dependency: Observable*)(computeFunction: => String): StringBinding = new jfxbb.StringBinding {
    //il binding viene invalidato con l'oggetto a cui e' vincolato
    dependency.foreach(this.bind(_))
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
    dependency.foreach(bind(_))
    //calcola il valore usando la funzione passata
    override def computeValue: ObservableList[A] = computeFunction
  }

}