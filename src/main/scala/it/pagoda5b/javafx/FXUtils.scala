package it.pagoda5b.javafx

/**
 * Permette delle semplificazioni per la definizione dell' ''Event Handling''
 */
object FXEventHandlersUtils {
  import java.lang.Runnable
  import javafx.event._

  /**
   * converte una funzione anonima che elabora un [[Event]] in un [[EventHandler]] dello stesso evento,
   * la cui implementazione utilizza la funzione
   */
  implicit def toHandler[E <: Event](handling: E => Unit): EventHandler[E] = new EventHandler[E] {
    def handle(event: E) = handling(event)
  }

  /**
   * converte una funzione anonima generica senza risultati in un [[java.lang.Runnable]] che la esegue
   */
  implicit def toRunnable(runCode: => Unit): Runnable = new Runnable {
    def run() = runCode
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
  import javafx.beans.binding.{ StringBinding, ListBinding }
  import javafx.beans.Observable
  import javafx.collections.ObservableList

  /**
   * costruisce un binding che ha una stringa come risultato
   *
   * @param dependency [[Observable]] a cui il [[Binding]] fa riferimneto
   * @param il valore che il binding deve restituire
   */
  def createStringBinding(dependency: Observable*)(computeFunction: => String): StringBinding = new StringBinding {
    //il binding viene invalidato con l'oggetto a cui e' vincolato
    for (o <- dependency) bind(o)
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
    for (o <- dependency) bind(o)
    //calcola il valore usando la funzione passata
    override def computeValue: ObservableList[A] = computeFunction
  }

}