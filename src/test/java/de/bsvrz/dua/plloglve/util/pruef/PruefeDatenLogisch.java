/*
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.2 Pl-Prüfung logisch LVE
 * Copyright (C) 2007-2015 BitCtrl Systems GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * Contact Information:<br>
 * BitCtrl Systems GmbH<br>
 * Weißenfelser Straße 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */

package de.bsvrz.dua.plloglve.util.pruef;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Assert;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.plloglve.util.PlPruefungLogisch;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.test.CSVImporter;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * KZD Listener Liest Ergebnis-CSV-Datei Wartet auf gesendete und gepruefte
 * Daten und gibt diese an Vergleicher-Klasse weiter.
 *
 * @author BitCtrl Systems GmbH, Görlitz
 */
public class PruefeDatenLogisch implements ClientReceiverInterface {

	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * Datenverteilerverbindung von der aufrufenden Klasse.
	 */
	private final ClientDavInterface dav;

	/**
	 * Aufrunfende Klasse.
	 */
	private final PlPruefungLogisch caller;

	/**
	 * CSV-Importer.
	 */
	private CSVImporter csvImp;

	/**
	 * CSV Index.
	 */
	private int csvOffset;

	/**
	 * Speichert die eingelesenen CSV Daten des FS1.
	 */
	private final ArrayList<HashMap<String, Integer>> csvZeilenFS1 = new ArrayList<>();

	/**
	 * Speichert die eingelesenen CSV Daten des FS2.
	 */
	private final ArrayList<HashMap<String, Integer>> csvZeilenFS2 = new ArrayList<>();

	/**
	 * Speichert die eingelesenen CSV Daten des FS3.
	 */
	private final ArrayList<HashMap<String, Integer>> csvZeilenFS3 = new ArrayList<>();

	/**
	 * Der aktuelle CSV-Nettowert.
	 */
	private Long csvWerttNetto;

	/**
	 * Speichert die eingelesenen CSV Nettowerte des FS1.
	 */
	private final ArrayList<Long> alCSVWerttNettoFS1 = new ArrayList<>();

	/**
	 * Speichert die eingelesenen CSV Nettowerte des FS2.
	 */
	private final ArrayList<Long> alCSVWerttNettoFS2 = new ArrayList<>();

	/**
	 * Speichert die eingelesenen CSV Nettowerte des FS3.
	 */
	private final ArrayList<Long> alCSVWerttNettoFS3 = new ArrayList<>();

	/**
	 * Zeitstempel der zu pruefenden Daten.
	 */
	private long pruefZeitstempel;

	/**
	 * Gibt an, ob die Fahrstreifenpruefung für FS1 erfolgt ist.
	 */
	private boolean pruefungFS1Fertig = false;

	/**
	 * Gibt an, ob die Fahrstreifenpruefung für FS2 erfolgt ist.
	 */
	private boolean pruefungFS2Fertig = false;

	/**
	 * Gibt an, ob die Fahrstreifenpruefung für FS3 erfolgt ist.
	 */
	private boolean pruefungFS3Fertig = false;

	/**
	 * Vergleicherthread für FS1 .
	 */
	private final VergleicheDaten vergleicheFS1;

	/**
	 * Vergleicherthread für FS2 .
	 */
	private final VergleicheDaten vergleicheFS2;

	/**
	 * Vergleicherthread für FS3 .
	 */
	private final VergleicheDaten vergleicheFS3;

	/**
	 * Empfange-Datenbeschreibung für KZD.
	 */
	public static DataDescription ddKzdEmpf = null;

	/**
	 * Empfange-Datenbeschreibung für LZD.
	 */
	public static DataDescription ddLzdEmpf = null;

	/**
	 * Listener, der auf ein bestimmtes Ergenis wartet und dann eine Prüfung der
	 * Ergebnisdaten durchführt.
	 *
	 * @param caller
	 *            Die aufrufende Klasse
	 * @param dav1
	 *            Datenverteiler-Verbindung
	 * @param fs
	 *            Die zu überwachenden Fahrstreifenobjekte
	 * @param csvQuelle
	 *            Die Quell CSV-Datei mit Soll-Werten
	 * @throws Exception
	 *             wird weitergereicht
	 */
	public PruefeDatenLogisch(final PlPruefungLogisch caller, final ClientDavInterface dav1, final SystemObject[] fs,
			final String csvQuelle) throws Exception {
		this.caller = caller; // aufrufende Klasse uebernehmen
		this.dav = dav1;

		// Empfangs-Datenbeschreibung
		PruefeDatenLogisch.ddKzdEmpf = new DataDescription(
				this.dav.getDataModel().getAttributeGroup(DUAKonstanten.ATG_KZD),
				this.dav.getDataModel().getAspect(DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH));

		PruefeDatenLogisch.ddLzdEmpf = new DataDescription(
				this.dav.getDataModel().getAttributeGroup(DUAKonstanten.ATG_LZD),
				this.dav.getDataModel().getAspect(DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH));

		// Empfänger anmelden
		this.dav.subscribeReceiver(this, fs, PruefeDatenLogisch.ddKzdEmpf, ReceiveOptions.normal(),
				ReceiverRole.receiver());

		this.dav.subscribeReceiver(this, fs, PruefeDatenLogisch.ddLzdEmpf, ReceiveOptions.delayed(),
				ReceiverRole.receiver());

		try {
			// CSV Importer initialisieren
			this.csvImp = new CSVImporter(csvQuelle);
		} catch (final Exception e) {
			PruefeDatenLogisch.LOGGER.error("Fehler beim öffnen der CSV Datei (" + csvQuelle + "): " + e); //$NON-NLS-1$ //$NON-NLS-2$
		}
		csvImp.getNaechsteZeile(); // Tabellenkopf in CSV ueberspringen
		csvEinlesen(); // CSV einlesen

		/*
		 * Initialisiere Vergleicherthreads
		 */
		vergleicheFS1 = new VergleicheDaten(this, 1);
		vergleicheFS2 = new VergleicheDaten(this, 2);
		vergleicheFS3 = new VergleicheDaten(this, 3);
	}

	/**
	 * Setzt den zu verwendenden Offset in der CSV-Datei und den Zeitstempel des
	 * Ergebnisses, auf das gewartet werden soll.
	 *
	 * @param csvOffset1
	 *            Zu verwendender Offset in CSV-Datei
	 * @param pruefZeitstempel1
	 *            Ergebniszeitstempel, auf den gewartet wird
	 */
	public void listen(final int csvOffset1, final long pruefZeitstempel1) {
		this.csvOffset = csvOffset1; // CSV-Index uebernehmen
		this.pruefZeitstempel = pruefZeitstempel1; // Zeitstempel uebernehmen

		// keine Pruefung abgeschlossen
		pruefungFS1Fertig = false;
		pruefungFS2Fertig = false;
		pruefungFS3Fertig = false;
	}

	/**
	 * CSV-Einlesen.
	 */
	private void csvEinlesen() {
		String[] aktZeile;

		try {
			while ((aktZeile = csvImp.getNaechsteZeile()) != null) {
				if (aktZeile.length > 0) {
					csvZeilenFS1.add(csvLeseZeile(aktZeile, 1));
					csvZeilenFS2.add(csvLeseZeile(aktZeile, 2));
					csvZeilenFS3.add(csvLeseZeile(aktZeile, 3));
				}
			}
		} catch (final Exception e) {
			PruefeDatenLogisch.LOGGER.error("Fehler beim einlesen der CSV Datei: " + e); //$NON-NLS-1$
		}
	}

	/**
	 * Gibt eine HashMap mit den Soll-Werten eines Fahrstreifens zurück.
	 *
	 * @param aktZeile
	 *            Der Prüf-CSV Offset
	 * @param fsIndex
	 *            Der zu verwendende Fahrstreifen
	 * @return Fahrstreifen-Soll-Daten (AttributPfad,Wert)
	 * @throws Exception
	 *             wird weitergereicht
	 */
	private HashMap<String, Integer> csvLeseZeile(final String[] aktZeile, final int fsIndex) throws Exception {
		final int verschiebung = (fsIndex - 1) * 2;

		final HashMap<String, Integer> hmCSV = new HashMap<>();
		int csvPosition = 0 + verschiebung;

		hmCSV.put("qKfz.Wert", Integer.parseInt(aktZeile[csvPosition]));
		hmCSV.put("qKfz.Wert.Kopie", hmCSV.get("qKfz.Wert"));
		hmCSV.putAll(csvLeseStatus(aktZeile[csvPosition + 1], "qKfz"));

		csvPosition = 6 + verschiebung;
		hmCSV.put("qPkw.Wert", Integer.parseInt(aktZeile[csvPosition]));
		hmCSV.put("qPkw.Wert.Kopie", hmCSV.get("qPkw.Wert"));
		hmCSV.putAll(csvLeseStatus(aktZeile[csvPosition + 1], "qPkw"));

		csvPosition = 12 + verschiebung;
		hmCSV.put("qLkw.Wert", Integer.parseInt(aktZeile[csvPosition]));
		hmCSV.put("qLkw.Wert.Kopie", hmCSV.get("qLkw.Wert"));
		hmCSV.putAll(csvLeseStatus(aktZeile[csvPosition + 1], "qLkw"));

		csvPosition = 18 + verschiebung;
		hmCSV.put("vKfz.Wert", Integer.parseInt(aktZeile[csvPosition]));
		hmCSV.put("vKfz.Wert.Kopie", hmCSV.get("vKfz.Wert"));
		hmCSV.putAll(csvLeseStatus(aktZeile[csvPosition + 1], "vKfz"));

		csvPosition = 24 + verschiebung;
		hmCSV.put("vPkw.Wert", Integer.parseInt(aktZeile[csvPosition]));
		hmCSV.put("vPkw.Wert.Kopie", hmCSV.get("vPkw.Wert"));
		hmCSV.putAll(csvLeseStatus(aktZeile[csvPosition + 1], "vPkw"));

		csvPosition = 30 + verschiebung;
		hmCSV.put("vLkw.Wert", Integer.parseInt(aktZeile[csvPosition]));
		hmCSV.put("vLkw.Wert.Kopie", hmCSV.get("vLkw.Wert"));
		hmCSV.putAll(csvLeseStatus(aktZeile[csvPosition + 1], "vLkw"));

		csvPosition = 36 + verschiebung;
		hmCSV.put("vgKfz.Wert", Integer.parseInt(aktZeile[csvPosition]));
		hmCSV.put("vgKfz.Wert.Kopie", hmCSV.get("vgKfz.Wert"));
		hmCSV.putAll(csvLeseStatus(aktZeile[csvPosition + 1], "vgKfz"));

		csvPosition = 42 + verschiebung;
		hmCSV.put("b.Wert", Integer.parseInt(aktZeile[csvPosition]));
		hmCSV.put("b.Wert.Kopie", hmCSV.get("b.Wert"));
		hmCSV.putAll(csvLeseStatus(aktZeile[csvPosition + 1], "b"));

		csvPosition = 48 + verschiebung;
		csvWerttNetto = Long.parseLong(aktZeile[csvPosition]) * 1000;
		hmCSV.putAll(csvLeseStatus(aktZeile[csvPosition + 1], "tNetto"));

		switch (fsIndex) {
		case 1:
			alCSVWerttNettoFS1.add(csvWerttNetto);
			break;
		case 2:
			alCSVWerttNettoFS2.add(csvWerttNetto);
			break;
		case 3:
			alCSVWerttNettoFS3.add(csvWerttNetto);
			break;
		default:
		}

		return hmCSV;
	}

	/**
	 * Liest ein Statusfeld, extrahiert Daten in eine HashMap und gibt diese
	 * zurück.
	 *
	 * @param status
	 *            Die Statuszeile. Parameter durch Leerzeichen getrennt
	 * @param praefix
	 *            Das Attribut des auszulesenden Status
	 * @return Statusdaten (Attributpfad,Wert)
	 */
	private HashMap<String, Integer> csvLeseStatus(final String status, final String praefix) {

		final HashMap<String, Integer> hmCSVStatus = new HashMap<>();

		final String[] statusGeteilt = status.split(" ");

		Float guete = null;

		Integer errCode = 0;

		for (final String element : statusGeteilt) {

			if (element.equalsIgnoreCase("Fehl")) {
				errCode = errCode - 2;
			}

			if (element.equalsIgnoreCase("nErm")) {
				errCode = errCode - 1;
			}

			if (element.equalsIgnoreCase("Impl")) {
				hmCSVStatus.put(praefix + ".Status.MessWertErsetzung.Implausibel", DUAKonstanten.JA);
			} else if (!hmCSVStatus.containsKey(praefix + ".Status.MessWertErsetzung.Implausibel")) {
				hmCSVStatus.put(praefix + ".Status.MessWertErsetzung.Implausibel", DUAKonstanten.NEIN);
			}

			if (element.equalsIgnoreCase("Intp")) {
				hmCSVStatus.put(praefix + ".Status.MessWertErsetzung.Interpoliert", DUAKonstanten.JA);
			} else if (!hmCSVStatus.containsKey(praefix + ".Status.MessWertErsetzung.Interpoliert")) {
				hmCSVStatus.put(praefix + ".Status.MessWertErsetzung.Interpoliert", DUAKonstanten.NEIN);
			}

			if (element.equalsIgnoreCase("nErf")) {
				hmCSVStatus.put(praefix + ".Status.Erfassung.NichtErfasst", DUAKonstanten.JA);
			} else if (!hmCSVStatus.containsKey(praefix + ".Status.Erfassung.NichtErfasst")) {
				hmCSVStatus.put(praefix + ".Status.Erfassung.NichtErfasst", DUAKonstanten.NEIN);
			}

			if (element.equalsIgnoreCase("wMaL")) {
				hmCSVStatus.put(praefix + ".Status.PlLogisch.WertMaxLogisch", DUAKonstanten.JA);
			} else if (!hmCSVStatus.containsKey(praefix + ".Status.PlLogisch.WertMaxLogisch")) {
				hmCSVStatus.put(praefix + ".Status.PlLogisch.WertMaxLogisch", DUAKonstanten.NEIN);
			}

			if (element.equalsIgnoreCase("wMax")) {
				hmCSVStatus.put(praefix + ".Status.PlFormal.WertMax", DUAKonstanten.JA);
			} else if (!hmCSVStatus.containsKey(praefix + ".Status.PlFormal.WertMax")) {
				hmCSVStatus.put(praefix + ".Status.PlFormal.WertMax", DUAKonstanten.NEIN);
			}

			if (element.equalsIgnoreCase("wMiL")) {
				hmCSVStatus.put(praefix + ".Status.PlLogisch.WertMinLogisch", DUAKonstanten.JA);
			} else if (!hmCSVStatus.containsKey(praefix + ".Status.PlLogisch.WertMinLogisch")) {
				hmCSVStatus.put(praefix + ".Status.PlLogisch.WertMinLogisch", DUAKonstanten.NEIN);
			}

			if (element.equalsIgnoreCase("wMin")) {
				hmCSVStatus.put(praefix + ".Status.PlFormal.WertMin", DUAKonstanten.JA);
			} else if (!hmCSVStatus.containsKey(praefix + ".Status.PlFormal.WertMin")) {
				hmCSVStatus.put(praefix + ".Status.PlFormal.WertMin", DUAKonstanten.NEIN);
			}

			try {
				guete = Float.parseFloat(element.replace(",", ".")) * 10000;
				hmCSVStatus.put(praefix + ".Güte.Index", guete.intValue());
			} catch (final Exception e) {
				// kein float wert
			}
		}

		if (errCode < 0) {
			if (!praefix.equals("tNetto")) {
				hmCSVStatus.put(praefix + ".Wert", errCode);
			} else {
				csvWerttNetto = errCode.longValue();
			}
		}

		// hmCSVStatus.put(praefix+".Güte.Index",100);

		return hmCSVStatus;
	}

	/**
	 * Übergebe CSV-Werte (tNetto) für FS1.
	 *
	 * @param csvOffset1
	 *            DS-Index des zu übergebende DS
	 * @return Der CSV-Wert tNetto für FS1
	 */
	public long getCSVWerttNettoFS1(final int csvOffset1) {
		return alCSVWerttNettoFS1.get(csvOffset1);
	}

	/**
	 * Übergebe CSV-Werte (tNetto) für FS2.
	 *
	 * @param csvOffset1
	 *            DS-Index des zu übergebende DS
	 * @return Der CSV-Wert tNetto für FS2
	 */
	public long getCSVWerttNettoFS2(final int csvOffset1) {
		return alCSVWerttNettoFS2.get(csvOffset1);
	}

	/**
	 * Übergebe CSV-Werte (tNetto) für FS3.
	 *
	 * @param csvOffset1
	 *            DS-Index des zu übergebende DS
	 * @return Der CSV-Wert tNetto für FS3
	 */
	public long getCSVWerttNettoFS3(final int csvOffset1) {
		return alCSVWerttNettoFS3.get(csvOffset1);
	}

	@Override
	public void update(final ResultData[] results) {
		for (final ResultData result : results) {
			// Pruefe Ergebnisdatensatz auf Zeitstempel
			if ((result.getDataDescription().equals(PruefeDatenLogisch.ddKzdEmpf)
					|| result.getDataDescription().equals(PruefeDatenLogisch.ddLzdEmpf)) && (result.getData() != null)
					&& (result.getDataTime() == pruefZeitstempel)) {

				try {
					// Ermittle FS und pruefe Daten
					if (result.getObject().getName().endsWith(".hfs") || result.getObject().getName().endsWith(".1")) {
						vergleicheFS1.vergleiche(result, csvZeilenFS1, csvOffset);
					} else if (result.getObject().getName().endsWith(".1üfs")
							|| result.getObject().getName().endsWith(".2")) {
						vergleicheFS2.vergleiche(result, csvZeilenFS2, csvOffset);
					} else if (result.getObject().getName().endsWith(".2üfs")
							|| result.getObject().getName().endsWith(".3")) {
						vergleicheFS3.vergleiche(result, csvZeilenFS3, csvOffset);
					}
				} catch (final Exception e) {
					//
				}

				PruefeDatenLogisch.LOGGER.info("Zu prüfendes Datum empfangen. Warte auf Prüfung..."); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Prueft ob alle 3 FS geprueft worden.
	 */
	private void pruefungFertig() {
		if (pruefungFS1Fertig && pruefungFS2Fertig && pruefungFS3Fertig) {
			PruefeDatenLogisch.LOGGER.info("Prüfung aller Fahrstreifen für diesen Intervall abgeschlossen"); //$NON-NLS-1$
			caller.doNotify(); // Benachrichtige aufrufende Klasse
		}
	}

	/**
	 * Diesen Thread wecken.
	 *
	 * @param fs
	 *            der Fahrstreifenindex
	 */
	public void doNotify(final int fs) {
		switch (fs) {
		case 1:
			pruefungFS1Fertig = true;
			break;
		case 2:
			pruefungFS2Fertig = true;
			break;
		case 3:
			pruefungFS3Fertig = true;
			break;
		default:
		}
		pruefungFertig();
	}

	/**
	 * Soll Assert zur Fehlermeldung genutzt werden?
	 *
	 * @param useAssert
	 *            solllen die Assert-Statements benutzte werden
	 */
	public void benutzeAssert(final boolean useAssert) {
		vergleicheFS1.benutzeAssert(useAssert);
		vergleicheFS2.benutzeAssert(useAssert);
		vergleicheFS3.benutzeAssert(useAssert);
	}

	/**
	 * Liefert ein Array mit der Anzahl der FehlerAlles aller Fehrstreifen.
	 *
	 * @return ein Array mit der Anzahl der FehlerAlles aller Fehrstreifen
	 */
	public int[] getFehlerAlles() {
		final int[] fehlerAlles = { vergleicheFS1.anzFehlerAlles, vergleicheFS2.anzFehlerAlles,
				vergleicheFS3.anzFehlerAlles };
		return fehlerAlles;
	}

	/**
	 * Liefert ein Array mit der Anzahl der FehlerLinks aller Fehrstreifen.
	 *
	 * @return ein Array mit der Anzahl der FehlerLinks aller Fehrstreifen
	 */
	public int[] getFehlerLinks() {
		final int[] fehlerLinks = { vergleicheFS1.anzFehlerLinks, vergleicheFS2.anzFehlerLinks,
				vergleicheFS3.anzFehlerLinks };
		return fehlerLinks;
	}

	/**
	 * Liefert ein Array mit der Anzahl der FehlerRechts aller Fehrstreifen.
	 *
	 * @return ein Array mit der Anzahl der FehlerRechts aller Fehrstreifen
	 */
	public int[] getFehlerRechts() {
		final int[] fehlerRechts = { vergleicheFS1.anzFehlerRechts, vergleicheFS2.anzFehlerRechts,
				vergleicheFS3.anzFehlerRechts };
		return fehlerRechts;
	}
}

/**
 * Vergleicht CSV Daten mit Ergebnisdaten.
 *
 * @author BitCtrl Systems GmbH, Görlitz
 */
class VergleicheDaten extends Thread {

	/**
	 * Assert-Statements benutzen?
	 */
	private boolean useAssert = true;

	/**
	 * Hält den aktuellen Warntext.
	 */
	private String warnung;

	/**
	 * Aufrufende Klasse.
	 */
	private final PruefeDatenLogisch caller;

	/**
	 * Übergebene Ergebnisdaten.
	 */
	private Data daten;

	/**
	 * tNetto werte der Ergebnisdaten für FS1.
	 */
	private long resultWerttNettoFS1;

	/**
	 * tNetto werte der Ergebnisdaten für FS2.
	 */
	private long resultWerttNettoFS2;

	/**
	 * tNetto werte der Ergebnisdaten für FS3.
	 */
	private long resultWerttNettoFS3;

	/**
	 * Attribut-Praefixe.
	 */
	private String[] attributNamenPraefix = null;

	/**
	 * Attributnamen.
	 */
	private final String[] attributNamen = { ".Wert", ".Status.Erfassung.NichtErfasst", ".Status.PlFormal.WertMax",
			".Status.PlFormal.WertMin", ".Status.PlLogisch.WertMaxLogisch", ".Status.PlLogisch.WertMinLogisch",
			".Status.MessWertErsetzung.Implausibel", ".Status.MessWertErsetzung.Interpoliert", ".Güte.Index" };

	/**
	 * Uebergebene CSV Zeilen des jeweiligen FS.
	 */
	ArrayList<HashMap<String, Integer>> csvZeilen;

	/**
	 * Uebergebener Fahrstreifenindex (1-3).
	 */
	int fsIndex;

	/**
	 * Uebergebener CSV Index.
	 */
	int csvOffset;

	/**
	 * Fehleranzahl der Tabelle mit erwarteten Werten Wert der linken Spalte
	 * eines Attributes stimmt nicht mit dem entsprechenden gelieferten Wert der
	 * SWE überein.
	 */
	protected int anzFehlerLinks = 0;

	/**
	 * Fehleranzahl der Tabelle mit erwarteten Werten Wert der rechten Spalte
	 * (Statusflags) eines Attributes stimmt. nicht mit dem entsprechenden
	 * gelieferten Wert der SWE überein
	 */
	protected int anzFehlerRechts = 0;

	/**
	 * Fehleranzahl der Tabelle mit erwarteten Werten Wert der linken und
	 * rechten Spalte (Statusflags) eines Attributes stimmt nicht mit dem
	 * entsprechenden gelieferten Wert der SWE überein.
	 */
	protected int anzFehlerAlles = 0;

	/**
	 * Erstellt einen Prüfthread welcher Soll-Werte und Ergebniswerte vergleicht
	 * und das Ergebnis ausgibt.
	 *
	 * @param caller
	 *            Die aufrufende Klasse
	 * @param fsIndex
	 *            Der zu prüfende Fahrstreifen
	 */
	public VergleicheDaten(final PruefeDatenLogisch caller, final int fsIndex) {
		this.caller = caller; // uebernehme aufrufende Klasse
		this.fsIndex = fsIndex; // uebernehme FS-Index
		this.start(); // starte Thread
	}

	/**
	 * Setze Attributnamen entsprechend der Attributgruppe.
	 *
	 * @param atg
	 *            Zu verwendende Attributgruppe
	 */
	private void setzeAttributNamen(final String atg) {
		if (atg.equals(DUAKonstanten.ATG_KZD)) {
			attributNamenPraefix = new String[] { "qKfz", "qPkw", "qLkw", "vKfz", "vPkw", "vLkw", "vgKfz", "b",
					"tNetto" };
		} else if (atg.equals(DUAKonstanten.ATG_LZD)) {
			attributNamenPraefix = new String[] { "qKfz", "qPkw", "qLkw", "vKfz", "vPkw", "vLkw" };
		}
	}

	/**
	 * Startet Thread.
	 */
	@Override
	public void run() {
		while (true) { // Thread läuft immer
			doWait(); // warte auf trigger
			try {
				doVergleiche(); // Pruefe Daten
			} catch (final Exception e) {
				//
			}
		}
	}

	/**
	 * Vergleicht IST- und SOLL-Ergebnisse.
	 *
	 * @param result
	 *            Das zu prüfende Ergebnis
	 * @param csvZeilen1
	 *            die Soll-CSV-Werte (aller Fahrstreifen)
	 * @param csvOffset1
	 *            Aktuelle Position in der CSV-Datei
	 */
	public void vergleiche(final ResultData result, final ArrayList<HashMap<String, Integer>> csvZeilen1,
			final int csvOffset1) {
		this.csvOffset = csvOffset1; // uebernehme CSV Index
		this.csvZeilen = csvZeilen1; // uebernehme CSV Daten
		this.daten = result.getData(); // uebernehme Ergebnisdaten
		setzeAttributNamen(result.getDataDescription().getAttributeGroup().getPid());
		synchronized (this) {
			this.notify(); // trigger thread
		}
	}

	/**
	 * Lässt Vergleicherthread warten.
	 *
	 */
	private void doWait() {
		synchronized (this) {
			try {
				this.wait();
			} catch (final Exception e) {
				//
			}
		}

	}

	/**
	 * Lese Ergebnisdaten.
	 *
	 * @param fsIndex1
	 *            der FS-Index
	 * @return ergebnis
	 */
	private HashMap<String, Integer> ergebnisLesen(final int fsIndex1) {
		final HashMap<String, Integer> hmResult = new HashMap<>();
		String attribut;
		int aktuellerWert;
		for (final String element : attributNamenPraefix) {
			for (final String element2 : attributNamen) {
				attribut = element + element2;
				if (!attribut.equals("tNetto.Wert")) {
					aktuellerWert = DUAUtensilien.getAttributDatum(attribut, daten).asUnscaledValue().intValue();
					hmResult.put(attribut, aktuellerWert);
				} else {
					switch (fsIndex1) {
					case 1:
						resultWerttNettoFS1 = DUAUtensilien.getAttributDatum("tNetto.Wert", daten).asUnscaledValue()
								.longValue();
						break;
					case 2:
						resultWerttNettoFS2 = DUAUtensilien.getAttributDatum("tNetto.Wert", daten).asUnscaledValue()
								.longValue();
						break;
					case 3:
						resultWerttNettoFS3 = DUAUtensilien.getAttributDatum("tNetto.Wert", daten).asUnscaledValue()
								.longValue();
						break;
					default:
					}
				}
			}
		}
		return hmResult;
	}

	/**
	 * prüfe Daten.
	 *
	 * @throws Exception
	 *             wird weitergereicht
	 */
	private void doVergleiche() throws Exception {
		final String ident = "[FS:" + fsIndex + "-Z:" + (csvOffset + 2) + "] "; // FS
		// +
		// CSV
		// Index
		Debug.getLogger().info("Pruefe Fahrstreifendatum " + ident);

		final HashMap<String, Integer> hmCSV = csvZeilen.get(csvOffset);
		final HashMap<String, Integer> hmResult = ergebnisLesen(fsIndex);

		long csvWerttNetto = -4;
		long resultWerttNetto = -4;

		switch (fsIndex) {
		case 1:
			csvWerttNetto = caller.getCSVWerttNettoFS1(csvOffset);
			resultWerttNetto = resultWerttNettoFS1;
			break;
		case 2:
			csvWerttNetto = caller.getCSVWerttNettoFS2(csvOffset);
			resultWerttNetto = resultWerttNettoFS2;
			break;
		case 3:
			csvWerttNetto = caller.getCSVWerttNettoFS3(csvOffset);
			resultWerttNetto = resultWerttNettoFS3;
			break;
		default:
		}

		String attribut;
		String attribWertKopie;
		String sollWertErl;
		String istWertErl;

		boolean fehlerLinks = false;
		boolean fehlerRechts = false;

		// Debug.getLogger().info(ident+"HashMap Groesse:
		// CSV("+(hmCSV.size()-8)+") <>
		// Results("+hmResult.size()+")");

		for (final String element : attributNamenPraefix) {
			for (final String element2 : attributNamen) {
				attribut = element + element2;
				attribWertKopie = attribut;

				fehlerLinks = false;
				fehlerRechts = false;

				if ((element2.length() == 5) && attribWertKopie.contains(".Wert")) {
					attribWertKopie = attribWertKopie.replace(".Wert", ".Wert.Kopie");
					/*
					 * Eingangsgeschwindigkeitswerte >= 255 werden als
					 * fehlerhaft interpretiert
					 */
					if (attribut.startsWith("v") && (hmCSV.get(attribut) >= 255)) {
						hmCSV.put(attribut, DUAKonstanten.FEHLERHAFT);
						hmCSV.put(element + ".Status.MessWertErsetzung.Implausibel", DUAKonstanten.JA);
					}
				}

				sollWertErl = "";
				istWertErl = "";
				warnung = "";

				/*
				 * tNetto wird separat behandelt
				 */
				if (!attribut.equals("tNetto.Wert")) {

					if (attribut.endsWith(".Wert")) {
						sollWertErl = wertErl(hmCSV.get(attribut));
						istWertErl = wertErl(hmResult.get(attribut));
					}

					/*
					 * Sonderbehandlung für vKfz- und qPkw-Werte (werden
					 * errechnet)
					 */
					if (attribut.equals("vKfz.Wert") || attribut.equals("qPkw.Wert")) {

						/*
						 * Prüfe Fehler links
						 */
						if (attribWertKopie.contains(".Wert.Kopie")
								&& !hmCSV.get(attribWertKopie).equals(hmResult.get(attribut))) {
							fehlerLinks = true;
						}

						/*
						 * Hier der eigentliche Wertvergleich von vKfz bzw. qPkw
						 */
						if (!hmCSV.get(attribut).equals(hmResult.get(attribut))) {
							warnung += ident + "DIFF (" + attribut + "):" + hmCSV.get(attribut) + sollWertErl
									+ " (SOLL)<>(IST) " + hmResult.get(attribut) + istWertErl;

							/*
							 * Wenn Werte nicht identisch wird zusätzlich mit
							 * dem separat in der Solltabelle aufgeführten
							 * errechneten Wert verglichen
							 */
							if (attribWertKopie.contains(".Wert.Kopie")
									&& hmCSV.get(attribWertKopie).equals(hmResult.get(attribut))) {
								warnung += "\n\r" + ident + "W-OK (" + attribWertKopie + "):"
										+ hmCSV.get(attribWertKopie) + " (SOLL)==(IST) " + hmResult.get(attribut);
							}

							fehlerRechts = true;

							if (useAssert) {
								Assert.assertTrue(warnung, false);
							} else {
								Debug.getLogger().warning(warnung);
							}
						} else {
							System.out.println(ident + " OK  (" + attribut + "):" + hmCSV.get(attribut) + sollWertErl
									+ " (SOLL)==(IST) " + hmResult.get(attribut) + istWertErl);
						}
					} else {
						/*
						 * Überprüfung der restlichen Attribute
						 */

						/*
						 * Prüfe Fehler links
						 */
						if (attribWertKopie.contains(".Wert.Kopie")
								&& !hmCSV.get(attribWertKopie).equals(hmResult.get(attribut))) {
							fehlerLinks = true;
						}

						if (!hmCSV.get(attribut).equals(hmResult.get(attribut))) {
							warnung += ident + "DIFF (" + attribut + "):" + hmCSV.get(attribut) + sollWertErl
									+ " (SOLL)<>(IST) " + hmResult.get(attribut) + istWertErl;

							/*
							 * Wenn Werte nicht identisch wird zusätzlich mit
							 * dem separat in der Solltabelle aufgeführten
							 * errechneten Wert verglichen
							 */
							if (attribWertKopie.contains(".Wert.Kopie")
									&& hmCSV.get(attribWertKopie).equals(hmResult.get(attribut))) {
								warnung += "\n\r" + ident + "W-OK (" + attribWertKopie + "):"
										+ hmCSV.get(attribWertKopie) + " (SOLL)==(IST) " + hmResult.get(attribut);
							}

							fehlerRechts = true;

							if (useAssert) {
								Assert.assertTrue(warnung, false);
							} else {
								Debug.getLogger().warning(warnung);
							}
						}
					}
				} else if (attribut.equals("tNetto.Wert")) {
					/*
					 * Sonderbehandlung für tNetto (long)
					 */
					sollWertErl = wertErl((int) csvWerttNetto);
					istWertErl = wertErl((int) resultWerttNetto);

					if (csvWerttNetto != resultWerttNetto) {
						if (useAssert) {
							Assert.assertTrue(ident + "DIFF (" + attribut + "):" + csvWerttNetto + sollWertErl
									+ " (SOLL)<>(IST) " + resultWerttNetto + istWertErl, false);
						} else {
							Debug.getLogger().error(ident + "DIFF (" + attribut + "):" + csvWerttNetto + sollWertErl
									+ " (SOLL)<>(IST) " + resultWerttNetto + istWertErl);
						}
					}
				}

				/*
				 * Fehlerausgabe
				 */
				if (fehlerLinks && fehlerRechts) {
					Debug.getLogger().warning("ERR:Fehler Alles @ " + ident + " (" + attribut + ")");
					anzFehlerAlles++;
				} else if (fehlerLinks) {
					Debug.getLogger().warning("ERR:Fehler Links @ " + ident + " (" + attribut + ")");
					anzFehlerLinks++;
				} else if (fehlerRechts) {
					Debug.getLogger().warning("ERR:Fehler Rechts @ " + ident + " (" + attribut + ")");
					anzFehlerRechts++;
				}
			}
		}

		// Debug.getLogger().info(pruefLog);
		Debug.getLogger().info("Prüfung der Fahrstreifendaten für diesen Intervall abgeschlossen");
		caller.doNotify(fsIndex); // Benachrichtige aufrufende Klasse
	}

	/**
	 * Übersetzt negative Werte in entsprechenden String.
	 *
	 * @param wert
	 *            Der zu übersetzende wert
	 * @return Wertbedeutung als String
	 */
	private String wertErl(final long wert) {
		if (wert == -1) {
			return " [nicht ermittelbar]";
		} else if (wert == -2) {
			return " [fehlerhaft]";
		} else if (wert == -3) {
			return " [nicht ermittelbar + fehlerhaft]";
		} else {
			return "";
		}
	}

	/**
	 * Soll Assert zur Fehlermeldung genutzt werden?
	 *
	 * @param useAssert1
	 *            <code>True</code> wenn Asserts verwendet werden sollen, sonst
	 *            <code>False</code>
	 */
	public void benutzeAssert(final boolean useAssert1) {
		this.useAssert = useAssert1;
	}
}
