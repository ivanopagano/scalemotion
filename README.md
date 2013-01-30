Scalemotion
===========

Questo progetto consiste di una semplice (ma non troppo) applicazione illustrativa in `scala`.

Session Abstract
----------------
The `scala` language has now come out of the academic scene and reached out to the enterprise audience.
 
Many resources teach the basics of the language, but we will focus on how production code looks like, through a  source-level view of a showcase application.
 
The app is designed to avoid the overwhelming complexity of publicly accessible projects, all the while leveraging a good share of the powerful idioms available in `scala`.
 
Some previous knowledge of `scala` or *functional programming* is highly recommended.
 
*Disclaimer*: no monad has been harmed or mistreated in the making of this app

Intento
-------
Lo scopo &egrave; di presentare una proposta di intervento al [codemotion 2013](http://roma.codemotion.it/) di Roma con l'idea di mostrare il linguaggio in un contesto applicativo non esclusivamente didattico.  
Questa esigenza nasce dalla volont&agrave; di esporre qualcosa che vada oltre il solito codice pensato "ad-hoc" per mostrare le varie strutture del linguaggio, ma che dia un'idea concreta di prodotto finale.

In tal senso l'applicazione dovrebbe essere auto-consistente, completa, purch&eacute; non troppo sofisticata, per evitare di introdurre troppi concetti ad un eventuale pubblico inesperto.

Allo stesso tempo l'obbiettivo &egrave; di suscitare la curiosit&agrave; dello sviluppatore con una discreta esperienza in altri linguaggi a oggetti (prevalentemente `java`) o che magari ha gi&agrave; avuto un assaggio di `scala` e desidera sapere come i vari tasselli vengono composti in un'applicazione completa.

Contenuto
---------
L'applicazione deve leggere dinamicamente il contenuto di un *feed rss* (il caso d'uso &egrave; il sito di [StackOverflow](http://stackoverflow.com/)) e calcolare poche semplici statistiche da mostrare attraverso uno o pi&ugrave; grafici.  
In una prima edizione potrebbe bastare avere un istogramma delle parole contenute nelle domande del sito.

Seguono alcune caratteristiche desiderate:

 - I dati vengono aggiornati costantemente attraverso la lettura del feed
 - &Egrave; possibile filtrare i dati scegliendo delle caratteristiche specifiche del feed di interesse, come `tag` o `autore`
 - In caso di assenza di connessione ad Internet sarebbe utile poter passare in modo manuale ad uno o pi&ugrave; file di contenuti depositati in locale, senza riavvio dell'applicazione
 - Si possono calcolare pi&ugrave; statistiche in contemporanea, attraverso l'uso di `parallel collections` o delle librerie asincrone di `akka`.
  - Sarebbe opportuno avere 2 o pi&ugrave; versioni dell'applicazione, per mostrare incrementalmente come introdurre il parallelismo
 - Utilizzo di un'interfaccia "desktop" e non *web*, per evitare la complessit&agrave; introdotta dall'uso di un qualsiasi *web-framework*. 
  - Inizialmente conterei di utilizzare [JavaFX](http://www.oracle.com/technetwork/java/javafx/overview/index.html) con o senza l'ausilio di [scalafx](https://code.google.com/p/scalafx/) (fra l'altro dovrebbe avere dei buoni componenti per i grafici)
 - Una *test suite* che mostra come viene gestito l'aspetto del *testing* in `scala`. Al momento viene utilizzato [scalatest](http://www.scalatest.org/)


###Note###
L'applicazione si accompagner&agrave; con delle *slide* che approfondiscono le parti di codice pi&ugrave; significative o interessanti.

Come gi&agrave; accennato, &egrave; preferibile mantenere ridotto il numero di librerie esterne e di costrutti "avanzati" del linguaggio: lo scopo &egrave; di dare un "assaggio" di come sia sviluppare in `scala`, senza rendere troppo complesso il tutto.

Non &egrave; previsto che l'applicazione sia sviluppata in modo interamente "funzionale", nel senso di aderire interamente ai concetti della *programmazione funzionale* quali "purity" o  "immutability".  
Pur facendo uso in modo pratico di questi concetti, non &egrave; nello scopo di questa presentazione introdurre il pubblico alla *programmazione funzionale* in quanto tale, n&eacute; tantomeno introdurre nel progetto l'ulteriore complessit&agrave; che ne deriverebbe.

Contributi
----------
Chiunque lo desideri &egrave; invitato a partecipare attivamente, sia allo sviluppo dell'applicazione e delle slide, nonch&eacute; all'intervento stesso.  
Al momento sto cercando di realizzare qualcosa di presentabile, prima di fare una richiesta esplicita all'organizzazione dell'evento, ma i tempi sono ridotti e mi sarebbe utile avere un riscontro o il supporto di chiunque fosse interessato, fosse anche solo per aggiungere un commento o una critica.

Potete contattarmi all'indirizzo [pagoda_5b@hotmail.com](mailto:pagoda_5b@hotmail.com)