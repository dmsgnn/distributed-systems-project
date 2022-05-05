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
- [ ] 2-phase commit
  - [ ] aggiungere l'ack se il workspace è stato validato (AckMessage)
  - [ ] gestire gli ack ricevuti, se il numero di ack corrisponde con il numero di server la transazione va persistita localmente
  - [ ] persistence del workspace alla fine del 2-phase commit
  - [ ] creare la coda di pendingTransactions
  - [ ] notificare il client se la transazione è invalidata
  
## Note
1) Nel read è necessario vedere se il client sta già lavorando su un workspace locale ed eventualmente restituire il valore del workspace
2) Nel read è necessario salvare il valore letto nel private workspace per garantire la consistency sulle read
