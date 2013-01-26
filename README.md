Scalemotion
===========

Questo progetto consiste di una semplice (ma non troppo) applicazione illustrativa in `scala`.

Session Abstract
----------------
The `scala` language took his share of attention in the last couple of years as it came out of the academic scope and reached out to the enterprise audience.  
Depending on who's speaking, it's being described as scalable, modern and powerful or as too complex and abstract to be useful.
 
Nowadays there are many resources available to learn the language itself, in the form of books, tutorials, blog-posts. Being educational in their intent, these resources can't give a practical view of how production scala code looks like.
 
In this session we'll try to address the issue, by presenting a code-level view of a showcase `scala` application, all the while giving you the chance to make your own opinion about the language.  
The app itself is designed with the goal of avoiding the overwhelming complexity of the most popular projects accessible on the `github`, while trying to leverage a good share of the interesting features available.
 
Come join us if you're a developer interested in seeing how the `scala` features build up to make a concrete real-world case. The talk should be accessible to anyone with some working knowledge of `OOP` (`java` in particular) while being interesting to the developer that already got his hands dirty with `scala`.
 
*disclaimer*: no monad has been harmed or mistreated in the making of this application.

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