# javanaise
## Ce qui a été fait :
- Javanaise v1
- Javanaise v2
- Persistence des objets du coordinateur
- Gestion de crash des serveurs
 - Stress Tests (Ecriture & Ecriture+Lecture aléatoire)

## Compilation des sources
```
mvn install
```

## Lancement du coordinateur
```
mvn exec:java@coord
```

## Lancement d'un client IRC
```
mvn exec:java@irc
```

## Lancement des stress tests (avoir le coordinateur de lancé avant)
### 4 clients qui comptent jusqu'à 500 ensemble (accès en écriture seulement)
```
mvn exec:java@ircCount
```

### 4 clients qui comptent jusqu'à 500 ensemble (accès en écriture et lecture aléatoire)
```
mvn exec:java@ircFuzz
```
