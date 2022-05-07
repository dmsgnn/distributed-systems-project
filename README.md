# ds-project
## Requisiti
- **Java Version**: `Java 15`
- 2 jar: **Client** e **Server**
- Librerie Socket: `java.net.*`
- Interfaccia client: 1) `write <key> <val>` e 2) `read <key>`
- Il client deve poter scegliere il it.polimi.ds.client.peer da contattare
- No persistenza

## Classi
- Tuple (serializable)
- Wrapper per i socket
- TODO...

## Todo-list
- [x] [Dav] Modifica menu (due menu, uno per la selezione dei server e l'altro per le operazioni possibili in una transazione (read, write, etx...))
- [x] Forwarding delle read se la tupla non è presente localmente
  - [x] Aggiungere timestamp di creazione del socket all'interno di SocketHandler
  - [x] Creare un'estensione di ReadMessage aggiungendo il timestamp che identifica il socket
  - [x] Creare un messaggio di risposta al forwarding
- [x] [Gio] Modificare write lato server per non forwardare
- [x] [Dav] BeginMessage
- [x] [Dav] CommitMessage
- [x] [Dav] AbortMessage
- [x] Controllare che il client non mandi commit vuoti (senza operazioni)
- [ ] 2-phase commit
  - [x] creare la coda (buffer) di pendingTransactions - i commit vanno messi qua dentro quando vengono ricevuti (in ordine di timestamp)
  - [x] forwarding del commit ai server che non sono nella lista dei riceventi
  - [x] aggiungere l'ack se il workspace è stato validato (AckMessage)
  - [ ] gestire gli ack ricevuti, se il numero di ack corrisponde con il numero di server la transazione va persistita localmente e va mandato il persist a tutti gli altri.
    - [ ] la lista di ack ricevuti non è necessaria (per ora) quindi utilizzare un contatore di ack
  - [ ] [Gio] persistence del workspace alla fine del 2-phase commit
  - [ ] notificare il client al termine del 2pc
  - [ ] abort se un server non valida la transazione
  - [x] [Dav] VoteMessage
    - [x] gestire iterazioni di vote in CommitInfo
    - [x] aggiungere iterazione all'AckMessage
    - [x] verificare che `iter(CommitInfo) == iter(AckMessage)`
  
## Note
- [x] Nel read è necessario vedere se il client sta già lavorando su un workspace locale ed eventualmente restituire il valore del workspace
- [x] Nel read è necessario salvare il valore letto nel private workspace per garantire la consistency sulle read