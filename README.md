# Progetto del corso di Trattamento dei Dati Multimediali

## Multimedia Image Deduplication

## Descrizione del problema
Il progetto si concentra sulla risoluzione del problema della deduplicazione di immagini all'interno di una directory del file system e sull'estrazione dei metadati EXIF. L'obiettivo è produrre un report strutturato che identifichi le immagini duplicate, quelle quasi duplicate e quelle uniche. Il sistema si basa su caratteristiche percettive dell'immagine e metadati per raggiungere questo obiettivo.
Il sistema è in grado di individuare immagini:

  - Duplicate esatte
  - Quasi duplicate, ovvero visivamente simili
  - Uniche

Tutto ciò si basa su caratteristiche percettive dell'immagine e metadati.

## Obiettivi
L'obiettivo principale del nostro sistema è identificare e raggruppare immagini in base alla loro similarità visiva. Ciò avviene attraverso:

  - Analisi della luminanza, che rappresenta la struttura dell'immagine
  - Analisi della crominanza, che rappresenta il colore
  - Estrazione e gestione dei metadati EXIF

Il sistema restituisce gruppi di immagini duplicate o quasi duplicate e produce un report strutturato.

## Pipeline del sistema
Il sistema segue la seguente pipeline:

  1. **Scansione**: lettura ricorsiva della directory fornita dall'utente e filtraggio per formati supportati come JPG, JPEG, PNG e TIFF.
  2. **Estrazione metadati EXIF**: lettura dei metadati tramite la libreria `metadata-extractor` e salvataggio in una struttura dati associata all'immagine.
  3. **Normalizzazione**: ridimensionamento delle immagini a 64x64 e uniformazione dello spazio di confronto.
  4. **Conversione spazio colore**: conversione da RGB a YCbCr e separazione in luminanza e crominanza.
  5. **Estrazione feature luminanza**: costruzione di una firma differenziale binaria basata su variazioni locali tra pixel adiacenti.
  6. **Estrazione feature cromatiche**: suddivisione dell'immagine in una griglia 8x8 e calcolo della media dei valori Cb e Cr per ogni blocco.
  7. **Confronto tra immagini**: distanza di Hamming sulla firma di luminanza e distanza media assoluta sulle firme cromatiche.
  8. **Clustering**: approccio greedy per la creazione di gruppi di duplicati.
  9. **Reportistica**: generazione di un file CSV contenente gruppi, proprietà dell'immagine, metadati EXIF e distanza.

## Scelte progettuali principali
  - Uso dello spazio colore YCbCr per separare in modo netto la struttura dal colore.
  - Uso di una firma differenziale binaria per rappresentare la luminanza in modo compatto.
  - Uso della distanza di Hamming per un confronto binario rapido ed efficiente.
  - Uso di medie a blocchi per descrivere la crominanza riducendo il rumore.
  - Separazione modulare dei servizi per un codice manutenibile.

## Argomenti del corso trattati
Il nostro progetto copre direttamente i seguenti argomenti teorici del corso:

  - Rappresentazione digitale delle immagini.
  - Formati di immagine e metadati EXIF.
  - Modelli di colore come RGB e YCbCr.
  - Database multimediali e strutturazione e interrogazione dati.
  - Computer vision di base come estrazione di feature e confronto tra immagini.
  - Predizione spaziale e differenze locali, concetti base collegati alla compressione lossless.

## Struttura del progetto
Il progetto ha la seguente struttura:

`
src/
| main/
| | java/
| | | service/
| | | model/
| | | runner/
| | resources/
| test/
`

## Come eseguire il progetto
Per eseguire il progetto, sono necessari:
- Java 17 o versione successiva
- Maven

Eseguire i comandi:

```bash

mvn clean package

java -jar target/<nome-jar>.jar <percorso-directory-immagini>

```

Esempio di utilizzo:

```bash

java -jar target/deduplication.jar ./images

```

## Output del sistema
Il sistema genera un file CSV strutturato contenente le seguenti colonne:

- group_id
- file_path
- file_size
- width
- height
- format
- original_date
- exif_metadata
- distance

Ogni gruppo rappresenta un insieme di immagini duplicate o quasi duplicate.

## Testing
Sono presenti unit test dedicati per garantire la stabilità di:

- Algoritmi di deduplicazione e calcolo distanze.
- Scansione delle directory e delle immagini.
- Estrazione corretta dei metadati EXIF.
- Generazione del file CSV.

## Note sul codice
Il codice è rigorosamente strutturato in servizi con Single Responsibility Principle.
I commenti sono presenti solo dove strettamente necessari per spiegare la logica complessa.
La naming convention di classi, metodi e variabili è in inglese.
Il formato dei messaggi di commit è: [TDM] - breve descrizione.

## Autore
 - Cosimo Mariano