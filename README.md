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
- [ ] [Gio] Forwarding delle read se la tupla non è presente localmente
- [ ] [Gio] Modificare write lato server per non forwardare
- [ ] [Dav] BeginMessage
- [ ] [Dav] CommitMessage
- [ ] [Dav] AbortMessage
- [ ] 2-phase commit