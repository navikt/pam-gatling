# pam-gatling

#### Offisiell dokumentasjon
- https://gatling.io/docs/current/quickstart/
- https://gatling.io/docs/current/advanced_tutorial/#advanced-tutorial

#### Kjør test
###### Windows
```sh
cd bin
gatling.bat
```
###### Linux
```sh
cd bin
./gatling.sh
```
En liste med simuleringer (tester) vil vises.
- Skriv inn tall for ønsket simulering.
- Skriv inn simulerings-ID om ønsket, eller trykk Enter for default-verdi.
- Skriv inn beskrivelse av kjøring om ønskelig, eller trykk Enter for tom beskrivelse.

Simulering blir nå kjørt.

#### Resultater
Resultatene ligger i mappen "results". Resultater man ønsker å beholde kan flyttes oveer til mappen "results_to_keep" og pushes til GitHub.
For å se på resultatet åpner man index.html som ligger i mappen for den respektive kjøring.  
