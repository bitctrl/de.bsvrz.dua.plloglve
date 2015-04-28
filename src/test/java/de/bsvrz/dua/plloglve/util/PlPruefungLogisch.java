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

package de.bsvrz.dua.plloglve.util;

import com.bitctrl.Constants;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.plloglve.plloglve.typen.OptionenPlausibilitaetsPruefungLogischVerkehr;
import de.bsvrz.dua.plloglve.test.Konfiguration;
import de.bsvrz.dua.plloglve.util.para.ParaKZDLogImport;
import de.bsvrz.dua.plloglve.util.para.ParaLZDLogImport;
import de.bsvrz.dua.plloglve.util.pruef.PruefeDatenLogisch;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Standardtest (TLS und Grenzwerte) für KZD und LZD.
 *
 * @author BitCtrl Systems GmbH, Görlitz
 *
 * @version $Id$
 */
public class PlPruefungLogisch implements ClientSenderInterface {

	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * Assert-Statements benutzen?
	 */
	private boolean useAssert = true;

	/**
	 * Die zur Prüfung zu verwendende CSV Datei mit Testdaten.
	 */
	private String csvPruefDatei = null;

	/**
	 * Gibt an, ob es sich um die Prüfung von TLS Daten handelt.
	 */
	private boolean tlsPruefung = false;

	/**
	 * Testfahrstreifen KZD FS1.
	 */
	public static SystemObject fs1 = null;

	/**
	 * Testfahrstreifen KZD FS2.
	 */
	public static SystemObject fs2 = null;

	/**
	 * Testfahrstreifen KZD FS3.
	 */
	public static SystemObject fs3 = null;

	/**
	 * Testfahrstreifen LZD FS1_LZD.
	 */
	public static SystemObject fs1Lz = null;

	/**
	 * Testfahrstreifen LZD FS2_LZD.
	 */
	public static SystemObject fs2Lz = null;

	/**
	 * Testfahrstreifen LZD FS3_LZD.
	 */
	public static SystemObject fs3Lz = null;

	/**
	 * Sende-Datenbeschreibung für LZD.
	 */
	public static DataDescription ddLzdSend = null;

	/**
	 * Sende-Datenbeschreibung für KZD.
	 */
	public static DataDescription ddKzdSend = null;

	/**
	 * Importer für die Parameter der KZD-FS1.
	 */
	private ParaKZDLogImport kzdImport1;

	/**
	 * Importer für die Parameter der KZD-FS2.
	 */
	private ParaKZDLogImport kzdImport2;

	/**
	 * Importer für die Parameter der KZD-FS3.
	 */
	private ParaKZDLogImport kzdImport3;

	/**
	 * Importer für die Parameter der LZD-FS1.
	 */
	private ParaLZDLogImport lzdImport1;

	/**
	 * Importer für die Parameter der LZD-FS2.
	 */
	private ParaLZDLogImport lzdImport2;

	/**
	 * Importer für die Parameter der LZD-FS3.
	 */
	private ParaLZDLogImport lzdImport3;

	/**
	 * Datenverteiler-Verbindung.
	 */
	private ClientDavInterface dav = null;

	/**
	 * Initialisiert Standardtest.
	 *
	 * @param dav
	 *            Die Datenverteilerverbindung
	 */
	public PlPruefungLogisch(final ClientDavInterface dav) {
		this.dav = dav;

		/*
		 * Meldet Sender für KZD und LZD unter dem Aspekt Externe Erfassung an
		 */
		PlPruefungLogisch.fs1 = this.dav.getDataModel().getObject(
				Konfiguration.PID_TESTFS1_KZD);
		PlPruefungLogisch.fs2 = this.dav.getDataModel().getObject(
				Konfiguration.PID_TESTFS2_KZD);
		PlPruefungLogisch.fs3 = this.dav.getDataModel().getObject(
				Konfiguration.PID_TESTFS3_KZD);

		PlPruefungLogisch.fs1Lz = this.dav.getDataModel().getObject(
				Konfiguration.PID_TESTFS1_LZD);
		PlPruefungLogisch.fs2Lz = this.dav.getDataModel().getObject(
				Konfiguration.PID_TESTFS2_LZD);
		PlPruefungLogisch.fs3Lz = this.dav.getDataModel().getObject(
				Konfiguration.PID_TESTFS3_LZD);

		PlPruefungLogisch.ddKzdSend = new DataDescription(this.dav
				.getDataModel().getAttributeGroup(DUAKonstanten.ATG_KZD),
				this.dav.getDataModel().getAspect(
						DUAKonstanten.ASP_EXTERNE_ERFASSUNG));

		PlPruefungLogisch.ddLzdSend = new DataDescription(this.dav
				.getDataModel().getAttributeGroup(DUAKonstanten.ATG_LZD),
				this.dav.getDataModel().getAspect(
						DUAKonstanten.ASP_EXTERNE_ERFASSUNG));
	}

	/**
	 * Konfiguriert Klasse für die Prüfung von KZD TLS, sendet Testdaten über
	 * den parametrierten Bereich und führt Prüfung durch.
	 *
	 * @param bereiche
	 *            Die Bereiche, über die Testdaten gesendet und geprüft werden
	 * @throws Exception
	 *             wird weitergereicht
	 */
	public void pruefeKZDTLS(final int[][] bereiche) throws Exception {
		this.csvPruefDatei = Konfiguration.DATENCSV_LVE_TLS;

		/*
		 * Es handelt sich um eine TLS- und KEINE Grenzwertprüfung
		 */
		this.tlsPruefung = true;

		LOGGER.info("Prüfe KZD TLS..."); //$NON-NLS-1$

		/*
		 * Importiere KZD TLS Parameter
		 */
		kzdImport1 = new ParaKZDLogImport(dav, PlPruefungLogisch.fs1,
				Konfiguration.TEST_DATEN_VERZ
						+ Konfiguration.DATENCSV_PARAMETER);
		kzdImport2 = new ParaKZDLogImport(dav, PlPruefungLogisch.fs2,
				Konfiguration.TEST_DATEN_VERZ
						+ Konfiguration.DATENCSV_PARAMETER);
		kzdImport3 = new ParaKZDLogImport(dav, PlPruefungLogisch.fs3,
				Konfiguration.TEST_DATEN_VERZ
						+ Konfiguration.DATENCSV_PARAMETER);

		/*
		 * Starte den Test
		 */
		doPruefeKZD(bereiche);
	}

	/**
	 * Konfiguriert Klasse für die Prüfung von KZD Grenzwerte, sendet Testdaten
	 * über den parametrierten Bereich und führt Prüfung durch.
	 *
	 * @param bereiche
	 *            Die Bereiche, über die Testdaten gesendet und geprüft werden
	 * @throws Exception
	 *             wird weitergereicht
	 */
	public void pruefeKZDGrenz(final int[][] bereiche) throws Exception {
		this.csvPruefDatei = Konfiguration.DATENCSV_LVE_GRENZ;

		LOGGER.info("Prüfe KZD Grenzwerte..."); //$NON-NLS-1$

		/*
		 * Importiere KZD Grenzwert Parameter
		 */
		kzdImport1 = new ParaKZDLogImport(dav, PlPruefungLogisch.fs1,
				Konfiguration.TEST_DATEN_VERZ
						+ Konfiguration.DATENCSV_PARAMETER);
		kzdImport2 = new ParaKZDLogImport(dav, PlPruefungLogisch.fs2,
				Konfiguration.TEST_DATEN_VERZ
						+ Konfiguration.DATENCSV_PARAMETER);
		kzdImport3 = new ParaKZDLogImport(dav, PlPruefungLogisch.fs3,
				Konfiguration.TEST_DATEN_VERZ
						+ Konfiguration.DATENCSV_PARAMETER);

		/*
		 * Beginne im ersten fünftel mit "nur Max"-Prüfung
		 */
		importOptionenKZD(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MAX);

		/*
		 * Starte Test
		 */
		doPruefeKZD(bereiche);
	}

	/**
	 * Konfiguriert Klasse für die Prüfung von LZD Grenzwerte, sendet Testdaten
	 * über den parametrierten Bereich und führt Prüfung durch.
	 *
	 * @param bereiche
	 *            Die Bereiche, über die Testdaten gesendet und geprüft werden
	 * @throws Exception
	 *             wird weitergereicht
	 */
	public void pruefeLZDGrenz(final int[][] bereiche) throws Exception {
		this.csvPruefDatei = Konfiguration.DATENCSV_LZD;

		LOGGER.info("Prüfe LZD Grenzwerte...");

		/*
		 * Importiere LZD Grenzwert Parameter
		 */
		lzdImport1 = new ParaLZDLogImport(dav, PlPruefungLogisch.fs1Lz,
				Konfiguration.TEST_DATEN_VERZ
						+ Konfiguration.DATENCSV_PARAMETER);
		lzdImport2 = new ParaLZDLogImport(dav, PlPruefungLogisch.fs2Lz,
				Konfiguration.TEST_DATEN_VERZ
						+ Konfiguration.DATENCSV_PARAMETER);
		lzdImport3 = new ParaLZDLogImport(dav, PlPruefungLogisch.fs3Lz,
				Konfiguration.TEST_DATEN_VERZ
						+ Konfiguration.DATENCSV_PARAMETER);

		/*
		 * Beginne im ersten fünftel mit "nur Max"-Prüfung
		 */
		importOptionenLZD(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MAX);

		/*
		 * Starte Test
		 */
		doPruefeLZD(bereiche);
	}

	/**
	 * Sendet Testdaten über den übergebenen Bereich und führt KZD Prüfung (TLS-
	 * oder Grenzwertprüfung entsprechend der Konfiguration) durch.
	 *
	 * @param bereiche
	 *            Die Bereiche, über die Testdaten gesendet und geprüft werden
	 * @throws Exception
	 *             wird weitergereicht
	 */
	private void doPruefeKZD(final int[][] bereiche) throws Exception {

		/*
		 * Sender anmelden
		 */
		this.dav.subscribeSender(this, new SystemObject[] {
				PlPruefungLogisch.fs1, PlPruefungLogisch.fs2,
				PlPruefungLogisch.fs3 }, PlPruefungLogisch.ddKzdSend,
				SenderRole.source());

		/*
		 * Initialisiere Testfahrstreifen-Datenimporter für FS1-FS3
		 */
		TestFahrstreifenImporter fsImpFS1 = null;
		TestFahrstreifenImporter fsImpFS2 = null;
		TestFahrstreifenImporter fsImpFS3 = null;

		fsImpFS1 = new TestFahrstreifenImporter(this.dav,
				Konfiguration.TEST_DATEN_VERZ + Konfiguration.DATENCSV_FS1);
		fsImpFS2 = new TestFahrstreifenImporter(this.dav,
				Konfiguration.TEST_DATEN_VERZ + Konfiguration.DATENCSV_FS2);
		fsImpFS3 = new TestFahrstreifenImporter(this.dav,
				Konfiguration.TEST_DATEN_VERZ + Konfiguration.DATENCSV_FS3);

		/*
		 * Initialisiert FS-Testerobjekt für den SOLL-IST-Vergleich
		 */
		final PruefeDatenLogisch fsPruefer = new PruefeDatenLogisch(this, dav,
				new SystemObject[] { PlPruefungLogisch.fs1,
						PlPruefungLogisch.fs2, PlPruefungLogisch.fs3 },
				Konfiguration.TEST_DATEN_VERZ + csvPruefDatei);
		fsPruefer.benutzeAssert(useAssert);

		int csvIndex = 0;

		/*
		 * Beinhalten den aktuellen Testdatensatz für FS1, FS2, FS3
		 */
		Data zeileFS1;
		Data zeileFS2;
		Data zeileFS3;

		/*
		 * Ergbnisdatensätze
		 */
		ResultData resultat1 = null;
		ResultData resultat2 = null;
		ResultData resultat3 = null;

		/*
		 * Gibt an, ob fuer den entsprechenden Fahrstriefen Testdaten vorhanden
		 * sind
		 */
		boolean datenFS1Vorhanden = true;
		boolean datenFS2Vorhanden = true;
		boolean datenFS3Vorhanden = true;

		/*
		 * Gibt an, ob für mind. 1 FS noch Testdaten vorliegen
		 */
		boolean csvDatenVorhanden = true;

		long aktZeit = System.currentTimeMillis();

		while (csvDatenVorhanden) {
			/*
			 * Übergebe CSV-Offset und zu prüfenden Zeitstempel an
			 * FS-Testerobjekt
			 */
			LOGGER.info("Setze CSV-Zeile und Zeitstempel fuer Prüfer -> Zeile:" + (csvIndex + 2) + " Zeit:" + aktZeit); //$NON-NLS-1$ //$NON-NLS-2$
			fsPruefer.listen(csvIndex, aktZeit);

			/*
			 * Lese Testdaten für FS1, FS2, FS3 ein
			 */
			if ((zeileFS1 = fsImpFS1
					.getNaechstenDatensatz(PlPruefungLogisch.ddKzdSend
							.getAttributeGroup())) != null) {
				LOGGER.info("Sende Daten fuer FS1 (CSV-Zeile " + (csvIndex + 2) + ")"); //$NON-NLS-1$ //$NON-NLS-2$
				resultat1 = new ResultData(PlPruefungLogisch.fs1,
						PlPruefungLogisch.ddKzdSend, aktZeit, zeileFS1);
			} else {
				datenFS1Vorhanden = false;
			}

			if ((zeileFS2 = fsImpFS2
					.getNaechstenDatensatz(PlPruefungLogisch.ddKzdSend
							.getAttributeGroup())) != null) {
				LOGGER.info("Sende Daten fuer FS2 (CSV-Zeile " + (csvIndex + 2) + ")"); //$NON-NLS-1$ //$NON-NLS-2$
				resultat2 = new ResultData(PlPruefungLogisch.fs2,
						PlPruefungLogisch.ddKzdSend, aktZeit, zeileFS2);
			} else {
				datenFS2Vorhanden = false;
			}

			if ((zeileFS3 = fsImpFS3
					.getNaechstenDatensatz(PlPruefungLogisch.ddKzdSend
							.getAttributeGroup())) != null) {
				LOGGER.info("Sende Daten fuer FS3 (CSV-Zeile " + (csvIndex + 2) + ")"); //$NON-NLS-1$ //$NON-NLS-2$
				resultat3 = new ResultData(PlPruefungLogisch.fs3,
						PlPruefungLogisch.ddKzdSend, aktZeit, zeileFS3);
			} else {
				datenFS3Vorhanden = false;
			}

			/*
			 * Prüft, ob noch Testdaten für die Fahrstreifen vorliegen
			 */
			if (!datenFS1Vorhanden || !datenFS2Vorhanden || !datenFS3Vorhanden) {
				csvDatenVorhanden = false;
				LOGGER.info("Keine Daten mehr vorhanedn. Beende Prüfung..."); //$NON-NLS-1$
			} else {
				/*
				 * Liegen noch Testdaten in dem konfigurierten Bereich vor so
				 * werden diese versendet
				 */
				for (final int[] bereich : bereiche) {
					if ((csvIndex >= (bereich[0] - 2))
							&& (csvIndex <= (bereich[1] - 2))) {
						LOGGER.info("Warte auf SOLL-IST-Vergleich (CSV-Zeile " + (csvIndex + 1) + ")..."); //$NON-NLS-1$//$NON-NLS-2$

						synchronized (this) {
							this.dav.sendData(new ResultData[] { resultat1,
									resultat2, resultat3 });
							doWait(); // Warte auf Ueberpruefung der FS1-FS3
						}
					}
				}
			}

			csvIndex++;

			/*
			 * Prüft und wechselt ggf. die Reaktionsart auf
			 * Grenzwerüberschreitung
			 */
			if (!tlsPruefung) {
				wechselReaktionKZD(csvIndex);
			}

			aktZeit = aktZeit + Constants.MILLIS_PER_MINUTE;
		}

		/*
		 * Fehlerausgabe des FS-Testerobjektes
		 */
		reportFehler(fsPruefer);

		/*
		 * Sender abmelden
		 */
		this.dav.unsubscribeSender(this, new SystemObject[] {
				PlPruefungLogisch.fs1, PlPruefungLogisch.fs2,
				PlPruefungLogisch.fs3 }, PlPruefungLogisch.ddKzdSend);
	}

	/**
	 * Sendet Testdaten über den übergebenen Bereich und führt KZD Prüfung (TLS-
	 * oder Grenzwertprüfung entsprechend der Konfiguration) durch.
	 *
	 * @param bereiche
	 *            Die Bereiche, über die Testdaten gesendet und geprüft werden
	 * @throws Exception
	 *             wird weitergereicht
	 */
	private void doPruefeLZD(final int[][] bereiche) throws Exception {
		/*
		 * Sender anmelden
		 */
		this.dav.subscribeSender(this, new SystemObject[] {
				PlPruefungLogisch.fs1Lz, PlPruefungLogisch.fs2Lz,
				PlPruefungLogisch.fs3Lz }, PlPruefungLogisch.ddLzdSend,
				SenderRole.source());
		/*
		 * Initialisiere Testfahrstreifen-Datenimporter für FS1-FS3
		 */
		TestFahrstreifenImporter fsImpFS1 = null;
		TestFahrstreifenImporter fsImpFS2 = null;
		TestFahrstreifenImporter fsImpFS3 = null;

		fsImpFS1 = new TestFahrstreifenImporter(this.dav,
				Konfiguration.TEST_DATEN_VERZ + Konfiguration.DATENCSV_FS1);
		fsImpFS2 = new TestFahrstreifenImporter(this.dav,
				Konfiguration.TEST_DATEN_VERZ + Konfiguration.DATENCSV_FS2);
		fsImpFS3 = new TestFahrstreifenImporter(this.dav,
				Konfiguration.TEST_DATEN_VERZ + Konfiguration.DATENCSV_FS3);

		/*
		 * Initialisiert FS-Testerobjekt für den SOLL-IST-Vergleich
		 */
		final PruefeDatenLogisch fsPruefer = new PruefeDatenLogisch(this, dav,
				new SystemObject[] { PlPruefungLogisch.fs1Lz,
						PlPruefungLogisch.fs2Lz, PlPruefungLogisch.fs3Lz },
				Konfiguration.TEST_DATEN_VERZ + csvPruefDatei);
		fsPruefer.benutzeAssert(useAssert);

		// Aktueller Index (Zeile) in CSV Datei
		int csvIndex = 0;

		/*
		 * Beinhalten den aktuellen Testdatensatz für FS1, FS2, FS3
		 */
		Data zeileFS1;
		Data zeileFS2;
		Data zeileFS3;

		/*
		 * Ergbnisdatensätze
		 */
		ResultData resultat1 = null;
		ResultData resultat2 = null;
		ResultData resultat3 = null;

		/*
		 * Gibt an, ob fuer den entsprechenden Fahrstriefen Testdaten vorhanden
		 * sind
		 */
		boolean datenFS1Vorhanden = true;
		boolean datenFS2Vorhanden = true;
		boolean datenFS3Vorhanden = true;

		/*
		 * Gibt an, ob für mind. 1 FS noch Testdaten vorliegen
		 */
		boolean csvDatenVorhanden = true;

		long aktZeit = System.currentTimeMillis();

		while (csvDatenVorhanden) {
			/*
			 * Übergebe CSV-Offset und zu prüfenden Zeitstempel an
			 * FS-Testerobjekt
			 */
			LOGGER.info("Setze CSV-Zeile und Zeitstempel für Pruefer -> Zeile:"
					+ (csvIndex + 2) + " Zeit:" + aktZeit);
			fsPruefer.listen(csvIndex, aktZeit);

			/*
			 * Lese Testdaten für FS1, FS2, FS3 ein
			 */
			if ((zeileFS1 = fsImpFS1
					.getNaechstenDatensatz(PlPruefungLogisch.ddLzdSend
							.getAttributeGroup())) != null) {
				LOGGER.info("Sende Daten fuer FS1 (CSV-Zeile " + (csvIndex + 2)
						+ ")");
				resultat1 = new ResultData(PlPruefungLogisch.fs1Lz,
						PlPruefungLogisch.ddLzdSend, aktZeit, zeileFS1);
			} else {
				datenFS1Vorhanden = false;
			}

			if ((zeileFS2 = fsImpFS2
					.getNaechstenDatensatz(PlPruefungLogisch.ddLzdSend
							.getAttributeGroup())) != null) {
				LOGGER.info("Sende Daten fuer FS2 (CSV-Zeile " + (csvIndex + 2)
						+ ")");
				resultat2 = new ResultData(PlPruefungLogisch.fs2Lz,
						PlPruefungLogisch.ddLzdSend, aktZeit, zeileFS2);
			} else {
				datenFS2Vorhanden = false;
			}

			if ((zeileFS3 = fsImpFS3
					.getNaechstenDatensatz(PlPruefungLogisch.ddLzdSend
							.getAttributeGroup())) != null) {
				LOGGER.info("Sende Daten fuer FS3 (CSV-Zeile " + (csvIndex + 2)
						+ ")");
				resultat3 = new ResultData(PlPruefungLogisch.fs3Lz,
						PlPruefungLogisch.ddLzdSend, aktZeit, zeileFS3);
			} else {
				datenFS3Vorhanden = false;
			}

			/*
			 * Prüft, ob noch Testdaten für die Fahrstreifen vorliegen
			 */
			if (!datenFS1Vorhanden && !datenFS2Vorhanden && !datenFS3Vorhanden) {
				csvDatenVorhanden = false;
				LOGGER.info("Keine Daten mehr vorhanedn. Beende Prüfung...");
			} else {
				/*
				 * Liegen noch Testdaten in dem konfigurierten Bereich vor so
				 * werden diese versendet
				 */
				for (final int[] bereich : bereiche) {
					if ((csvIndex >= (bereich[0] - 2))
							&& (csvIndex <= (bereich[1] - 2))) {
						LOGGER.info("Warte auf SOLL-IST-Vergleich (CSV-Zeile "
								+ (csvIndex + 1) + ")...");

						synchronized (this) {
							this.dav.sendData(new ResultData[] { resultat1,
									resultat2, resultat3 });
							doWait(); // Warte auf Ueberpruefung der FS1-FS3
						}
					}
				}
			}

			csvIndex++;

			/*
			 * Prüft und wechselt ggf. die Reaktionsart auf
			 * Grenzwerüberschreitung
			 */
			wechselReaktionLZD(csvIndex);

			aktZeit = aktZeit + Constants.MILLIS_PER_MINUTE;
		}

		/*
		 * Fehlerausgabe des FS-Testerobjekts
		 */
		reportFehler(fsPruefer);

		/*
		 * Sender abmelden
		 */
		this.dav.unsubscribeSender(this, new SystemObject[] {
				PlPruefungLogisch.fs1Lz, PlPruefungLogisch.fs2Lz,
				PlPruefungLogisch.fs3Lz }, PlPruefungLogisch.ddLzdSend);
	}

	/**
	 * Importiert die Optionen für die Datenprüfung von KZD.
	 *
	 * @param option
	 *            Die zu importierende Option
	 * @throws Exception
	 *             wird weitergereicht
	 */
	private void importOptionenKZD(
			final OptionenPlausibilitaetsPruefungLogischVerkehr option)
			throws Exception {
		kzdImport1.setOptionen(option);
		kzdImport1.importiereParameter(1);

		kzdImport2.setOptionen(option);
		kzdImport2.importiereParameter(2);

		kzdImport3.setOptionen(option);
		kzdImport3.importiereParameter(3);
	}

	/**
	 * Importiert die Optionen für die Datenprüfung von LZD.
	 *
	 * @param option
	 *            Die zu importierende Option
	 * @throws Exception
	 *             wird weitergereicht
	 */
	private void importOptionenLZD(
			final OptionenPlausibilitaetsPruefungLogischVerkehr option)
			throws Exception {
		lzdImport1.setOptionen(option);
		lzdImport1.importiereParameter(1);

		lzdImport2.setOptionen(option);
		lzdImport2.importiereParameter(2);

		lzdImport3.setOptionen(option);
		lzdImport3.importiereParameter(3);
	}

	/**
	 * Wechselt die Reaktionsart auf Grenzwertüberschreitung bei KZD.
	 * Ausgegangen wird von 100 Datensätzen -> Wechsel alle 20 Datensätze. Im
	 * Falle eier Grenzwertprüfung muss die Option auf "nur Max"-Prüfung vorher
	 * gesetzt werden da diese hier nicht gesetzt wird
	 *
	 * @param csvIndex
	 *            Der DS-Index
	 * @throws Exception
	 *             wird weitergereicht
	 */
	private void wechselReaktionKZD(final int csvIndex) throws Exception {
		switch (csvIndex) {
		case 20:
			importOptionenKZD(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MIN);
			break;
		case 40:
			importOptionenKZD(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MIN_MAX);
			break;
		case 60:
			importOptionenKZD(OptionenPlausibilitaetsPruefungLogischVerkehr.NUR_PRUEFUNG);
			break;
		case 80:
			importOptionenKZD(OptionenPlausibilitaetsPruefungLogischVerkehr.KEINE_PRUEFUNG);
			break;
		default:
		}
	}

	/**
	 * Wechselt die Reaktionsart auf Grenzwertüberschreitung bei LZD.
	 * Ausgegangen wird von 100 Datensätzen -> Wechsel alle 20 Datensätze. Im
	 * Falle eier Grenzwertprüfung muss die Option auf "nur Max"-Prüfung vorher
	 * gesetzt werden da diese hier nicht gesetzt wird
	 *
	 * @param csvIndex
	 *            Der DS-Index
	 * @throws Exception
	 *             wird weitergereicht
	 */
	private void wechselReaktionLZD(final int csvIndex) throws Exception {
		switch (csvIndex) {
		case 20:
			importOptionenLZD(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MIN);
			break;
		case 40:
			importOptionenLZD(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MIN_MAX);
			break;
		case 60:
			importOptionenLZD(OptionenPlausibilitaetsPruefungLogischVerkehr.NUR_PRUEFUNG);
			break;
		case 80:
			importOptionenLZD(OptionenPlausibilitaetsPruefungLogischVerkehr.KEINE_PRUEFUNG);
			break;
		default:
		}
	}

	/**
	 * Weckt diesen Thread.
	 */
	public void doNotify() {
		synchronized (this) {
			this.notify();
		}
	}

	/**
	 * Lässten diesen Thread warten.
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
	 * {@inheritDoc}
	 */
	@Override
	public void dataRequest(final SystemObject object,
			final DataDescription dataDescription, final byte state) {
		// VOID
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isRequestSupported(final SystemObject object,
			final DataDescription dataDescription) {
		return false;
	}

	/**
	 * Soll Assert zur Fehlermeldung genutzt werden?
	 *
	 * @param useAssert1
	 *            <code>True</code> wenn Asserts benutzt werden sollen, sonst
	 *            <code>False</code>
	 */
	public void benutzeAssert(final boolean useAssert1) {
		this.useAssert = useAssert1;
	}

	/**
	 * Gibt die Fehler des FS-Testerobjektes aus.
	 *
	 * @param fsPruefer
	 *            Das FS-Testerobjekt dessen Fehler ausgegeben werden sollen
	 */
	private void reportFehler(final PruefeDatenLogisch fsPruefer) {
		/*
		 * Fehlerausgabe FehlerAlles
		 */
		for (int i = 0; i <= 2; i++) {
			if (fsPruefer.getFehlerAlles()[i] > 0) {
				LOGGER.warning("ERR: Insgesamt "
						+ fsPruefer.getFehlerAlles()[i] + " FehlerAlles auf FS"
						+ (i + 1));
			}
		}

		/*
		 * Fehlerausgabe FehlerLinks
		 */
		for (int i = 0; i <= 2; i++) {
			if (fsPruefer.getFehlerLinks()[i] > 0) {
				LOGGER.warning("ERR: Insgesamt "
						+ fsPruefer.getFehlerLinks()[i] + " FehlerLinks auf FS"
						+ (i + 1));
			}
		}

		/*
		 * Fehlerausgabe FehlerRechts
		 */
		for (int i = 0; i <= 2; i++) {
			if (fsPruefer.getFehlerRechts()[i] > 0) {
				LOGGER.warning("ERR: Insgesamt "
						+ fsPruefer.getFehlerRechts()[i]
						+ " FehlerRechts auf FS" + (i + 1));
			}
		}
	}
}
