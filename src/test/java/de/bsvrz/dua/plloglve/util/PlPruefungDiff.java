/*
 * Segment 4 Daten�bernahme und Aufbereitung (DUA), SWE 4.2 Pl-Pr�fung logisch LVE
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
 * Wei�enfelser Stra�e 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */

package de.bsvrz.dua.plloglve.util;

import org.junit.Assert;

import com.bitctrl.Constants;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.plloglve.test.Konfiguration;
import de.bsvrz.dua.plloglve.util.para.ParaKZDLogImport;
import de.bsvrz.dua.plloglve.util.pruef.FilterMeldung;
import de.bsvrz.dua.plloglve.util.pruef.PruefeMarkierung;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Test Differenzialkontrolle
 *
 * @author BitCtrl Systems GmbH, G�rlitz
 *
 *
 * @version $Id$
 */
public class PlPruefungDiff implements ClientSenderInterface,
PlPruefungInterface {

	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * Assert-Statements benutzen?
	 */
	private boolean useAssert = true;

	/**
	 * Testfahrstreifen KZD.
	 */
	public static SystemObject fahrStreifen;

	/**
	 * KZD Importer.
	 */
	private ParaKZDLogImport kzdImport;

	/**
	 * Sende-Datenbeschreibung f�r KZD
	 */
	public static DataDescription ddKzdSend;

	/**
	 * Datenverteiler-Verbindung
	 */
	private ClientDavInterface dav = null;

	/**
	 * Abweichung zur erwarteten Anzahl von Meldungen
	 */
	private int meldungHyst = 5;

	/**
	 * Initialisiert die Differenzielkontrolle
	 *
	 * @param dav
	 *            Die Datenteilerverbindung
	 * @param alDebug
	 *            .getLogger() Die Debug.getLogger()attribute
	 */
	public PlPruefungDiff(final ClientDavInterface dav,
			final ArgumentList alLogger) {
		this.dav = dav;

		/*
		 * Melde Sender f�r FS an
		 */
		PlPruefungDiff.fahrStreifen = this.dav.getDataModel().getObject(
				Konfiguration.PID_TESTFS1_KZD);

		PlPruefungDiff.ddKzdSend = new DataDescription(this.dav
				.getDataModel().getAttributeGroup(DUAKonstanten.ATG_KZD),
				this.dav.getDataModel().getAspect(
						DUAKonstanten.ASP_EXTERNE_ERFASSUNG));

		try {
			kzdImport = new ParaKZDLogImport(dav, PlPruefungDiff.fahrStreifen,
					Konfiguration.TEST_DATEN_VERZ
					+ Konfiguration.DATENCSV_PARAMETER);
			kzdImport.importParaDiff();
		} catch (final Exception e) {
			LOGGER.error("Kann Test nicht konfigurieren: " + e); //$NON-NLS-1$
		}
	}

	/**
	 * Sendet Testdaten f�r Differenzialkontrolle und �berpr�ft die
	 * Ergebnisdaten
	 *
	 * @throws Exception
	 */
	public void pruefe() throws Exception {
		/*
		 * Sender anmelden
		 */
		this.dav.subscribeSender(this, PlPruefungDiff.fahrStreifen,
				PlPruefungDiff.ddKzdSend, SenderRole.source());

		/*
		 * Initialisiere FS-Daten-Importer
		 */
		TestFahrstreifenImporter fsImpFSDiff = null;
		fsImpFSDiff = new TestFahrstreifenImporter(this.dav,
				Konfiguration.TEST_DATEN_VERZ + Konfiguration.DATEN_CSV_DIFF);

		/*
		 * Die zu sendenden Daten
		 */
		Data zeileFSDiff;

		/*
		 * Die aktuelle Zeit
		 */
		Long aktZeit = System.currentTimeMillis();

		/*
		 * Meldungsfilter Pr�ft die Anzahl der durch die Differenzialkontrolle
		 * erzeugten Betriebsmeldungen Wir erwarten insgesamt 63
		 * Betriebsmeldungen welche den Text "konstant" enthalten
		 */
		final FilterMeldung meldFilter = new FilterMeldung(this, dav,
				"konstant", 105, meldungHyst); //$NON-NLS-1$
		LOGGER
		.info("Meldungsfilter initialisiert: Erwarte 105 Meldungen mit \"konstant\""); //$NON-NLS-1$

		/*
		 * Markierungspr�fer Pr�ft die Ausgangsdaten der Differenzialkontrolle
		 * auf korrekte Markierung (OK bzw. fehlerhaft/implausibel)
		 */
		final PruefeMarkierung markPruefer = new PruefeMarkierung(this, dav,
				PlPruefungDiff.fahrStreifen);
		markPruefer.benutzeAssert(false);// useAssert);

		/*
		 * Gesamtintervall und Interval des aktuellen Durchlaufes
		 */
		int dsGesamt = 0;
		int dsDurchlauf;

		/*
		 * Senden der Testdaten �ber 3 Durchl�ufe
		 */
		for (int i = 0; i < 5; i++) {
			while ((zeileFSDiff = fsImpFSDiff
					.getNaechstenDatensatz(PlPruefungDiff.ddKzdSend
							.getAttributeGroup())) != null) {

				dsGesamt++;

				dsDurchlauf = dsGesamt - (480 * i);
				LOGGER
				.info("Durchlauf:" + (i + 1) + " - CSV-Zeile:" + (dsDurchlauf + 1) + " - Zeit:" + aktZeit + " -> Konfiguriere Pr�fer: Erwarte alle Attribute als fehlerfrei"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				System.out
						.println("Durchlauf:"
								+ (i + 1)
								+ " - CSV-Zeile:"
								+ (dsDurchlauf + 1)
								+ " - Zeit:"
								+ aktZeit
								+ " -> Konfiguriere Pr�fer: Erwarte alle Attribute als fehlerfrei");

				/*
				 * Konfiguriere Markierungspr�fer Wir erwarten generell alle
				 * Daten als OK
				 *
				 * �berpr�fung von s -> OK im Intervall 20-27
				 */
				markPruefer.listenOK(aktZeit);

				/*
				 * Im Intervall 4 und 13 erwarten wir jeweils qKfz als
				 * fehlerhaft/implausibel
				 */
				if ((dsDurchlauf == 4) || (dsDurchlauf == 13)) {
					LOGGER
					.info("CSV-Zeile:" + (dsDurchlauf + 1) + " - Zeit:" + aktZeit + " -> Konfiguriere Pr�fer: Erwarte qKfz als fehlerhaft und implausibel"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					System.out
							.println("CSV-Zeile:" + (dsDurchlauf + 1) + " - Zeit:" + aktZeit + " -> Konfiguriere Pr�fer: Erwarte qKfz als fehlerhaft und implausibel"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					markPruefer.listenFehlImpl("qKfz", aktZeit); //$NON-NLS-1$
				}

				/*
				 * Im Intervall 6 und 13-17 erwarten wir jeweils qLkw als
				 * fehlerhaft/implausibel
				 */
				if ((dsDurchlauf == 6)
						|| ((dsDurchlauf >= 13) && (dsDurchlauf <= 17))) {
					LOGGER
					.info("CSV-Zeile:" + (dsDurchlauf + 1) + " - Zeit:" + aktZeit + " -> Konfiguriere Pr�fer: Erwarte qLkw als fehlerhaft und implausibel"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					System.out
							.println("CSV-Zeile:" + (dsDurchlauf + 1) + " - Zeit:" + aktZeit + " -> Konfiguriere Pr�fer: Erwarte qLkw als fehlerhaft und implausibel"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					markPruefer.listenFehlImpl("qLkw", aktZeit); //$NON-NLS-1$
				}

				/*
				 * Im Intervall 9-13 erwarten wir jeweils qPkw als
				 * fehlerhaft/implausibel
				 */
				if ((dsDurchlauf >= 9) && (dsDurchlauf <= 13)) {
					LOGGER
					.info("CSV-Zeile:" + (dsDurchlauf + 1) + " - Zeit:" + aktZeit + " -> Konfiguriere Pr�fer: Erwarte qPkw als fehlerhaft und implausibel"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					System.out
							.println("CSV-Zeile:" + (dsDurchlauf + 1) + " - Zeit:" + aktZeit + " -> Konfiguriere Pr�fer: Erwarte qPkw als fehlerhaft und implausibel"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					markPruefer.listenFehlImpl("qPkw", aktZeit); //$NON-NLS-1$
				}

				/*
				 * Im Intervall 30-31 erwarten wir jeweils vKfz als
				 * fehlerhaft/implausibel
				 */
				if ((dsDurchlauf >= 30) && (dsDurchlauf <= 31)) {
					LOGGER
					.info("CSV-Zeile:" + (dsDurchlauf + 1) + " - Zeit:" + aktZeit + " -> Konfiguriere Pr�fer: Erwarte vKfz als fehlerhaft und implausibel"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					System.out
							.println("CSV-Zeile:" + (dsDurchlauf + 1) + " - Zeit:" + aktZeit + " -> Konfiguriere Pr�fer: Erwarte vKfz als fehlerhaft und implausibel"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					markPruefer.listenFehlImpl("vKfz", aktZeit); //$NON-NLS-1$
				}

				/*
				 * Im Intervall 36-38 erwarten wir jeweils vLkw als
				 * fehlerhaft/implausibel
				 */
				if ((dsDurchlauf >= 36) && (dsDurchlauf <= 38)) {
					LOGGER
					.info("CSV-Zeile:" + (dsDurchlauf + 1) + " - Zeit:" + aktZeit + " -> Konfiguriere Pr�fer: Erwarte vLkw als fehlerhaft und implausibel"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					System.out
							.println("CSV-Zeile:" + (dsDurchlauf + 1) + " - Zeit:" + aktZeit + " -> Konfiguriere Pr�fer: Erwarte vLkw als fehlerhaft und implausibel"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					markPruefer.listenFehlImpl("vLkw", aktZeit); //$NON-NLS-1$
				}

				/*
				 * Im Intervall 32 erwarten wir vPkw als fehlerhaft/implausibel
				 */
				if (dsDurchlauf == 32) {
					LOGGER
					.info("CSV-Zeile:" + (dsDurchlauf + 1) + " - Zeit:" + aktZeit + " -> Konfiguriere Pr�fer: Erwarte vPkw als fehlerhaft und implausibel"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					System.out
							.println("CSV-Zeile:" + (dsDurchlauf + 1) + " - Zeit:" + aktZeit + " -> Konfiguriere Pr�fer: Erwarte vPkw als fehlerhaft und implausibel"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					markPruefer.listenFehlImpl("vPkw", aktZeit); //$NON-NLS-1$
				}

				/*
				 * Im Intervall 31 und 35 erwarten wir jeweils b als
				 * fehlerhaft/implausibel
				 */
				if ((dsDurchlauf == 31) || (dsDurchlauf == 35)) {
					LOGGER
					.info("CSV-Zeile:" + (dsDurchlauf + 1) + " - Zeit:" + aktZeit + " -> Konfiguriere Pr�fer: Erwarte b als fehlerhaft und implausibel"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					System.out
							.println("CSV-Zeile:" + (dsDurchlauf + 1) + " - Zeit:" + aktZeit + " -> Konfiguriere Pr�fer: Erwarte b als fehlerhaft und implausibel"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					markPruefer.listenFehlImpl("b", aktZeit); //$NON-NLS-1$
				}

				final ResultData resultat1 = new ResultData(PlPruefungDiff.fahrStreifen,
						PlPruefungDiff.ddKzdSend, aktZeit, zeileFSDiff);

				synchronized (this) {
					this.dav.sendData(resultat1);
					doWait();
				}

				aktZeit = aktZeit + Constants.MILLIS_PER_MINUTE;
			}
			fsImpFSDiff.reset();
			fsImpFSDiff.getNaechsteZeile();
		}

		LOGGER.info("Warte 30 Sekunden auf Meldungsfilter"); //$NON-NLS-1$
		try {
			Thread.sleep(5000L);
		} catch (final InterruptedException e) {
		}

		/*
		 * Fehlertext des Meldungsfilters
		 */
		final String warnung = meldFilter.getErwarteteAnzahlMeldungen()
				+ " Betriebsmeldungen erhalten";

		/*
		 * Gibt bei Fehler den Meldungsfiltertext aus
		 */
		if (!meldFilter.wurdeAnzahlEingehalten()) {
			if (useAssert) {
				Assert.assertTrue(warnung, false);
			} else {
				LOGGER.warning(warnung);
			}
		} else {
			LOGGER.info(warnung);
		}

		LOGGER.info("Pr�fung erfolgreich abgeschlossen"); //$NON-NLS-1$

		/*
		 * Sender abmelden
		 */
		this.dav.unsubscribeSender(this, PlPruefungDiff.fahrStreifen,
				PlPruefungDiff.ddKzdSend);
	}

	/**
	 * Weckt diesen Thread
	 */
	@Override
	public void doNotify() {
		synchronized (this) {
			this.notify();
		}
	}

	/**
	 * L�ssten diesen Thread warten
	 */
	private void doWait() {
		synchronized (this) {
			try {
				this.wait();
			} catch (final InterruptedException e) {
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
	 * @param useAssert
	 *            <code>True</code> wenn Asserts benutzt werden sollen, sonst
	 *            <code>False</code>
	 */
	public void benutzeAssert(final boolean useAssert) {
		this.useAssert = useAssert;
	}

	/**
	 * Setzt die erlaubte Abweichung zur erwarteten Anzahl an Betriebsmeldungen
	 *
	 * @param meldungHyst
	 *            Die erlaubte Abweichung zur erwarteten Anzahl an
	 *            Betriebsmeldungen
	 */
	public void setMeldungHysterese(final int meldungHyst) {
		this.meldungHyst = meldungHyst;
	}
}
