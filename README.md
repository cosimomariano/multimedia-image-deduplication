# Progetto del corso di trattamento dei dati multimediali - Multimedia Image Deduplication

## Descrizione del problema analizzato

Il progetto affronta il problema della deduplicazione di immagini all'interno del file system e dell'estrazione dei metadati EXIF in file strutturati CSV.

# Obiettivi

L'obiettivo del sistema è quello di identificare tre categorie di immagini: 

1) Duplicati esatti ( Stesso contenuto )
2) Quasi duplicati ( Immagini simili visivamente ma non identiche in senso stretto )
3) Immagini uniche

Tali identificazioni saranno possibili grazie all'analisi delle seguenti caratteristiche delle immagini: luminanza, struttura locale (variazioni di intensità) e metadati EXIF.

## Pipeline
1. Scansione: Lettura tramite la scansione della cartella indicata dall'utente delle immagini da analizzare
2. Estrazione Metadati: Estrazione dei metadati EXIF per ogni immagine
3. Normalizzazione: Normalizzazione (resize) delle immagini
4. Isolamento Luminanza: Conversione in scala di grigi
5. Predizione Spaziale: Predizione spaziale (pixel precedente)
6. Calcolo del Residuo Differenziale: Calcolo residuo differenziale
7. Binarizzazione: Binarizzazione (firma dell’immagine)
8. Confronto: Confronto tra immagini (distanza di Hamming)
9. Classificazione:
    * duplicati
    * quasi-duplicati
10. Reportistica: Stampa del report finale in formato CSV con i metadati EXIF delle immagini analizzate

## Argomenti del corso trattati

Gli argomenti del corso trattati sono:
   1) Metadati EXIF (Modulo 1)
   2) Formati immagini (Modulo 1)
   3) Database multimediali (Modulo 1)
   4) Rappresentazione digitale delle immagini (Modulo 1)
   5) Modelli di colore (Modulo 1)
   6) Computer vision di base (estrazione feature e confronto tra immagini) (Modulo 1)
   7) Compressione lossless, predizione spaziale e codifica differenziale (Modulo 4)

## Struttura del progetto (bozza)

`
src/
 ┣ main/
 ┃ ┣ java/
 ┃ ┃ ┣ service/
 ┃ ┃ ┣ model/
 ┃ ┃ ┣ runner/
 ┃ ┣ resources/
 ┣ test/
`

## Come eseguire il progetto

### TODO CM da fare

## Uscite generate dal sistema

Come anticipato nel paragrafo descrittivo il sistema genererà un file strutturato CSV contenente i metadati EXIF di ogni immagine con particolare evidenza dei duplicati e quasi-duplicati. 
(Da valutare se stampare anche in console il risultato)

## Note sul codice

1) I commenti sono utilizzati solo laddove necessario per non appesantire troppo la lettura.
2) I commit sono organizzati in questo modo:

    `[Prefisso del corso (acronimo)] - Breve commento sull'attività svolta`
    
    Es. `[TDM] - Initial commit`

    Inoltre i commit sono tutti in lingua inglese come anche i nomi dei packages e classi presenti nel progetto.

## Autore

Cosimo Mariano

