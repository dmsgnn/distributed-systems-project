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
### Giovanni
- Capire come decidere per il forwarding in base a chi manda il messaggio (https://docs.oracle.com/javase/7/docs/api/java/net/Socket.html)

### Davide
- Gestire forwarding rispetto a parametro R nei peer (modulo della chiave, forwardando agli R-1 peer successivi)
