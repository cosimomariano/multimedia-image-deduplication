# Progetto del corso di Trattamento dei Dati Multimediali

## Multimedia Image Deduplication

## Descrizione del problema
Il progetto si concentra sulla risoluzione del problema della deduplicazione di immagini all'interno di una directory del file system e sull'estrazione dei metadati EXIF. 
L'obiettivo è produrre un report strutturato (file .CSV) che identifichi le immagini duplicate, quelle quasi duplicate e quelle uniche.
Il sistema si basa su caratteristiche percettive dell'immagine ed i metadati ad essa associati per raggiungere questo obiettivo.

Il sistema è in grado di individuare immagini:

- Duplicate esatte
- Quasi duplicate, ovvero visivamente simili
- Uniche

## Obiettivi
L'obiettivo principale del sistema è identificare e raggruppare immagini in base alla loro similarità visiva.
Ciò avviene attraverso:

- Analisi della luminanza (struttura dell'immagine)
- Analisi della crominanza (informazione di colore)
- Estrazione e gestione dei metadati EXIF

Il sistema restituisce gruppi di immagini duplicate o quasi duplicate e produce un report strutturato (file .CSV).

---

## Pipeline del sistema
Il sistema segue la seguente pipeline:

1. **Scansione**: lettura ricorsiva della directory fornita dall'utente e filtraggio per formati supportati censiti in apposito enumerativo: JPG, JPEG, PNG, TIFF.
2. **Estrazione metadati EXIF**: lettura dei metadati tramite la libreria `metadata-extractor` e salvataggio in una struttura dati (Key-Value Map) associata all'immagine.
3. **Normalizzazione**: ridimensionamento delle immagini a 64x64 per uniformare lo spazio di confronto.
4. **Conversione spazio colore**: conversione da RGB a YCbCr per separare luminanza e crominanza.
5. **Estrazione feature luminanza**: costruzione di una firma numerica basata sui valori di luminanza Y dei pixel.
6. **Estrazione feature cromatiche**: suddivisione dell'immagine in una griglia 8x8 e calcolo della media dei valori Cb e Cr per ogni blocco.
7. **Confronto tra immagini**:
    - Mean Squared Error (MSE) sulla componente della luminanza
    - Mean Absolute Error (MAE) sulle componenti della crominanza
8. **Raggruppamento**: aggregazione delle immagini in gruppi (GROUP_{id}) sulla base di soglie empiriche di similarità.
9. **Reportistica**: generazione di un file CSV contenente gruppi, proprietà dell'immagine, metadati EXIF e distanza.

## Scelte progettuali principali
- Uso dello spazio colore YCbCr per separare struttura (luminanza) e colore (crominanza).
- Rappresentazione della luminanza tramite firma numerica (array di double).
- Uso della Mean Squared Error (MSE) per il confronto strutturale tra immagini.
- Uso della Mean Absolute Error (MAE) per il confronto delle componenti cromatiche.
- Uso di medie a blocchi per ridurre il rumore cromatico potenzialmente riscontrabile.
- Architettura modulare a servizi per garantire manutenibilità ed estensibilità per future nuove implementazioni o rivisitazioni del codice.

## Argomenti del corso trattati
Il progetto copre direttamente i seguenti argomenti teorici del corso:

- Rappresentazione digitale delle immagini.
- Formati di immagine e metadati EXIF.
- Modelli di colore (RGB, YCbCr).
- Database multimediali e strutturazione dei dati.
- Computer vision di base:
    - estrazione di feature
    - confronto tra immagini

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