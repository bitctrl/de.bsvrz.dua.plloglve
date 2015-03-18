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

import java.util.Random;

import junit.framework.Assert;
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
import de.bsvrz.dua.plloglve.util.pruef.FilterMeldung;
import de.bsvrz.dua.plloglve.util.pruef.PruefeMarkierung;
import de.bsvrz.dua.plloglve.vew.TestParameter;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Testet den Vertrauensbereich.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public class PlPruefungVertrauensbereich implements ClientSenderInterface,
		PlPruefungInterface {

	/**
	 * statischer Zufallsgenerator.
	 */
	private static final Random R = new Random(System.currentTimeMillis());

	/**
	 * Assert-Statements benutzen?
	 */
	private boolean useAssert = true;

	/**
	 * Testfahrstreifen KZD.
	 */
	public static SystemObject fs = null;

	/**
	 * KZD Importer.
	 */
	private ParaKZDLogImport kzdImport;

	/**
	 * Sende-Datenbeschreibung für KZD.
	 */
	public static DataDescription ddKzdSend = null;

	/**
	 * Datenverteiler-Verbindung.
	 */
	private ClientDavInterface dav = null;

	/**
	 * Intervalllänge in Millisekunden.
	 */
	static long intervall = TestParameter.INTERVALL_VB;

	/**
	 * Fehlerdatensätze.
	 */
	private Data zFSFehlB2;

	/**
	 * Abweichung zur erwarteten Anzahl von Meldungen.
	 */
	private int meldungHyst = 3;

	/**
	 * Initialisiert Vertrauensbereichstest.
	 * 
	 * @param dav
	 *            Datenverteilerverbindung
	 * @param alDebug.getLogger()
	 *            Testdatenverzeichnis
	 */
	public PlPruefungVertrauensbereich(ClientDavInterface dav,
			ArgumentList alLogger) {
		this.dav = dav;

		/*
		 * Melde Sender für FS an
		 */
		fs = this.dav.getDataModel().getObject(Konfiguration.PID_TESTFS1_KZD); //$NON-NLS-1$

		ddKzdSend = new DataDescription(this.dav.getDataModel()
				.getAttributeGroup(DUAKonstanten.ATG_KZD), this.dav
				.getDataModel().getAspect(DUAKonstanten.ASP_EXTERNE_ERFASSUNG));

		try {
			kzdImport = new ParaKZDLogImport(dav, fs,
					Konfiguration.TEST_DATEN_VERZ
							+ Konfiguration.DATENCSV_PARAMETER);
			kzdImport
					.setOptionen(OptionenPlausibilitaetsPruefungLogischVerkehr.KEINE_PRUEFUNG);
			kzdImport.importiereParameter(1);
			kzdImport.importParaVertrauensbereich();
		} catch (Exception e) {
			Debug.getLogger().error("Kann Test nicht konfigurieren: " + e);
		}

		try {
			TestFahrstreifenImporter paraImpFSFehler = null;
			paraImpFSFehler = new TestFahrstreifenImporter(this.dav,
					Konfiguration.TEST_DATEN_VERZ
							+ Konfiguration.DATENCSV_FS_FEHLER); //$NON-NLS-1$
			TestFahrstreifenImporter.setT(TestParameter.INTERVALL_VB);
			zFSFehlB2 = paraImpFSFehler.getNaechstenDatensatz(ddKzdSend
					.getAttributeGroup());
		} catch (Exception e) {
			Debug.getLogger().error("Kann Fehlerdatensätze nicht importieren: " + e);
		}

	}

	/**
	 * Startet den Vertrauensbereichstest.
	 * 
	 * @throws Exception wird weitergereicht
	 */
	public void pruefe() throws Exception {
		/*
		 * Sender anmelden
		 */
		this.dav.subscribeSender(this, fs, ddKzdSend, SenderRole.source());

		/*
		 * Initialisiere Parameter Importer für fehlerfreie DS
		 */
		TestFahrstreifenImporter paraImpFSOK = null;
		paraImpFSOK = new TestFahrstreifenImporter(this.dav,
				Konfiguration.TEST_DATEN_VERZ + Konfiguration.DATENCSV_FS_OK); //$NON-NLS-1$

		/*
		 * Setze Intervall auf 100MS
		 */
		TestFahrstreifenImporter.setT(TestParameter.INTERVALL_VB);

		/*
		 * Aktueller fehlerfreie Fahrstreifen-DS
		 */
		Data zeileFSOK;

		Long pruefZeit = System.currentTimeMillis();
		Long aktZeit;

		/*
		 * Prüfung
		 */
		System.out.println("Beginne Prüfung");

		/*
		 * Warte auf 371 Meldungen 36 x 4 (qKfz, qPkw, qLkw, vPkw) 37 (b) 39 x 4
		 * (qKfz, qPkw, qLkw, vPkw) 32 (b) 2 (Gutmeldung)
		 */
		FilterMeldung meldFilter = new FilterMeldung(this, dav,
				"Vertrauensbereichs", 352, meldungHyst);
		Debug.getLogger()
				.info("Meldungsfilter initialisiert: Erwarte 352 Meldungen mit \"Vertrauensbereichs\"");

		/*
		 * Sendet fehlerfreie DS für eine Stunde
		 */
		System.out.println("Sende fehlerfreie DS für 1 Stunde (60)");
		/*
		 * Testerobjekt
		 */
		PruefeMarkierung markPruefer = new PruefeMarkierung(this, dav, fs);

		for (int i = 1; i <= 60; i++) {

			if ((zeileFSOK = paraImpFSOK.getNaechstenDatensatz(ddKzdSend
					.getAttributeGroup())) == null) {
				paraImpFSOK.reset();
				paraImpFSOK.getNaechsteZeile();
				zeileFSOK = paraImpFSOK.getNaechstenDatensatz(ddKzdSend
						.getAttributeGroup());
			}

			ResultData resultat1 = new ResultData(fs, ddKzdSend, pruefZeit,
					zeileFSOK);
			this.dav.sendData(resultat1);

			// Sende Datensatz entsprechend Intervall
			pruefZeit = pruefZeit + intervall;

			// Warte bis Intervallende
			if ((aktZeit = System.currentTimeMillis()) < pruefZeit) {
				try {
					Thread.sleep(pruefZeit - aktZeit);
				} catch (InterruptedException ex) {
					//
				}
			}
		}

		markPruefer.benutzeAssert(useAssert);

		for (int i = 1; i <= 600; i++) {

			/*
			 * Konfiguriert Testerobjekt Prüfe Markierung der DS als Implausibel
			 */
			if ((i >= 29 && i < 72) || (i >= 511 && i < 552)) {
				markPruefer.listenImpl(pruefZeit);
				Debug.getLogger().info("Intervall " + i
						+ ": Erwarte alle Attribute als Implausiebel");
			} else {
				markPruefer.listenOK(pruefZeit);
			}

			/*
			 * Verlassen des VB vom 29. - 71. DS und vom 511. - 551. DS
			 * Innerhalb dieser Bereiche werden alle Werte des FS als
			 * Implausibel erwartet
			 */
			if ((i >= 4 && i <= 9) || (i >= 23 && i <= 29)
					|| (i >= 490 && i <= 494) || (i >= 501 && i <= 506)
					|| (i >= 512 && i <= 514)) {
				/*
				 * Es wird ein fehlerhafter DS (qKfz, qPkw, qLkw, vPkw) gesendet
				 * Dabei wird der VB entsprechend Afo beim 29. DS verlassen
				 * 
				 * Der prozentuale Ausfall der Attribute liegt ab dem 65. DS
				 * unter 20% wobei der VB jedoch aufgrund der anderen
				 * Fehlerdaten weiterhin verlassen bleibt
				 * 
				 * Für den zweiten Testbereich liegt der prozentuale Ausfall ab
				 * dem 513. DS über 20% wobei der VB bereits früher durch die
				 * anderen Fehlerdaten verlassen wird
				 * 
				 * Ab dem 552. DS liegt der prozentuale Ausfall entsprechend Afo
				 * wieder im VB
				 */
				System.out
						.println("Intervall "
								+ i
								+ ": Sende fehlerhaftes Datum (qKfz, qPkw, qLkw, vPkw)");
				markPruefer.addIgnore("b");
				synchronized (this) {
					sendeFehler2(pruefZeit);
					doWait();
				}
			} else if ((i >= 10 && i <= 16) || (i >= 30 && i <= 36)
					|| (i >= 482 && i <= 489) || (i >= 507 && i <= 511)) {
				System.out.println("Intervall " + i
						+ ": Sende fehlerhaftes Datum (b)");
				markPruefer.addIgnore("qKfz");
				markPruefer.addIgnore("qPkw");
				markPruefer.addIgnore("vLkw");
				markPruefer.addIgnore("vPkw");
				synchronized (this) {
					sendeFehler1(pruefZeit);
					doWait();
				}
			} else {
				if ((zeileFSOK = paraImpFSOK.getNaechstenDatensatz(ddKzdSend
						.getAttributeGroup())) == null) {
					paraImpFSOK.reset();
					paraImpFSOK.getNaechsteZeile();
					zeileFSOK = paraImpFSOK.getNaechstenDatensatz(ddKzdSend
							.getAttributeGroup());
				}
				ResultData resultat1 = new ResultData(fs, ddKzdSend,
						pruefZeit, zeileFSOK);
				System.out.println("Intervall " + i
						+ ": Sende fehlerfreies Datum");
				synchronized (this) {
					this.dav.sendData(resultat1);
					doWait();
				}
			}

			// Sende Datensatz entsprechend Intervall
			pruefZeit = pruefZeit + intervall;

			// Warte bis Intervallende
			if ((aktZeit = System.currentTimeMillis()) < pruefZeit) {
				try {
					Thread.sleep(pruefZeit - aktZeit);
				} catch (InterruptedException ex) {
					//
				}
			}
		}

		System.out.println("Warte auf Meldungsfilter");

		// Warte 30s auf Filterung der Betriebsmeldungen
		try {
			Thread.sleep(5000L);
		} catch (InterruptedException e) {
			//
		}

		String warnung = meldFilter.getErwarteteAnzahlMeldungen() + " Betriebsmeldungen erhalten";
		if (!meldFilter.wurdeAnzahlEingehalten()) {
			if (useAssert) {
				Assert.assertTrue(warnung, false);
			} else {
				System.out.println(warnung);
			}
		} else {
			System.out.println(warnung);
		}

		System.out.println("Prüfung erfolgreich abgeschlossen");

		/*
		 * Sender abmelden
		 */
		this.dav.unsubscribeSender(this, fs, ddKzdSend);
	}

	/**
	 * Sendet einen fehlerhaften DS Fehlerhafte Attribute: "qKfz", "qPkw",
	 * "vLkw", "vPkw".
	 * 
	 * @param pruefZeit
	 *            Zeitstempel des DS
	 * @throws Exception wird weitergereicht
	 */
	private void sendeFehler1(long pruefZeit) throws Exception {
		ResultData resultat1;
		Data data = zFSFehlB2.createModifiableCopy();
		for (String attribut : new String[] { "qKfz", "qPkw", "vLkw", "vPkw" }) {
			if (R.nextBoolean()) {
				data.getItem(attribut).getItem("Status").getItem(
						"MessWertErsetzung").getUnscaledValue("Implausibel")
						.set(DUAKonstanten.JA);
			} else {
				data.getItem(attribut).getUnscaledValue("Wert").set(
						DUAKonstanten.FEHLERHAFT);
			}
		}
		resultat1 = new ResultData(fs, ddKzdSend, pruefZeit, data);
		this.dav.sendData(resultat1);
	}

	/**
	 * Sendet einen fehlerhaften DS Fehlerhaftes Attribut: b.
	 * 
	 * @param pruefZeit
	 *            Zeitstempel des DS
	 * @throws Exception wird weitergereicht
	 */
	private void sendeFehler2(long pruefZeit) throws Exception {
		ResultData resultat2;
		Data data = zFSFehlB2.createModifiableCopy();
		if (R.nextBoolean()) {
			data.getItem("b").getItem("Status").getItem("MessWertErsetzung")
					.getUnscaledValue("Implausibel").set(DUAKonstanten.JA);
		} else {
			data.getItem("b").getUnscaledValue("Wert").set(
					DUAKonstanten.FEHLERHAFT);
		}
		resultat2 = new ResultData(fs, ddKzdSend, pruefZeit, data);
		this.dav.sendData(resultat2);
	}

	/**
	 * (Kein Javadoc).
	 * 
	 * @see de.bsvrz.dua.plloglve.util.PlPruefungInterface#doNotify()
	 */
	public void doNotify() {
		synchronized (this) {
			this.notify();
		}
	}

	/**
	 * Laesst diesen Thread warten.
	 */
	private void doWait() {
		synchronized (this) {
			try {
				this.wait();
			} catch (Exception e) {
				//
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dataRequest(SystemObject object,
			DataDescription dataDescription, byte state) {
		// VOID
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isRequestSupported(SystemObject object,
			DataDescription dataDescription) {
		return false;
	}

	/**
	 * Soll Assert zur Fehlermeldung genutzt werden?
	 * 
	 * @param useAssert1
	 *            <code>True</code> wenn Asserts verwendet werden sollen,
	 *            sonst <code>False</code>
	 */
	public void benutzeAssert(final boolean useAssert1) {
		this.useAssert = useAssert1;
	}

	/**
	 * Setzt die erlaubte Abweichung zur erwarteten Anzahl an Betriebsmeldungen.
	 * 
	 * @param meldungHyst1
	 *            Die erlaubte Abweichung zur erwarteten Anzahl an
	 *            Betriebsmeldungen.
	 */
	public void setMeldungHysterese(final int meldungHyst1) {
		this.meldungHyst = meldungHyst1;
	}
}
