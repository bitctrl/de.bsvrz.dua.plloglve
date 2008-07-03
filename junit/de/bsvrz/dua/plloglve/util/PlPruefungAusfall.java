/** 
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.2 Pl-Prüfung logisch LVE
 * Copyright (C) 2007 BitCtrl Systems GmbH 
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

import java.util.Date; // import java.util.Random;

import junit.framework.Assert;
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
import de.bsvrz.dua.plloglve.vew.TestParameter;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Tests.
 * 
 * @author BitCtrl Systems GmbH, Görlitz
 * 
 * 
 * @version $Id$
 */
public class PlPruefungAusfall implements ClientSenderInterface,
		PlPruefungInterface {

	/**
	 * Zufallsgenerator.
	 */
	// private static final Random R = new Random(System.currentTimeMillis());
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
	static long intervall = 100L;

	/**
	 * Abweichung zur erwarteten Anzahl von Meldungen.
	 */
	private int meldungHyst = 3;

	/**
	 * Sendet Testdaten und prüft Ausfallkontrolle.
	 * 
	 * @param dav
	 *            Datenverteilerverbindung
	 * @param alLogger
	 *            Argumente
	 */
	public PlPruefungAusfall(ClientDavInterface dav, ArgumentList alLogger) {
		this.dav = dav;

		/*
		 * Melde Sender für FS an
		 */
		fs = this.dav.getDataModel().getObject(Konfiguration.PID_TESTFS1_KZD); //$NON-NLS-1$

		ddKzdSend = new DataDescription(this.dav.getDataModel()
				.getAttributeGroup(DUAKonstanten.ATG_KZD), this.dav
				.getDataModel().getAspect(DUAKonstanten.ASP_EXTERNE_ERFASSUNG),
				(short) 0);

		try {
			kzdImport = new ParaKZDLogImport(dav, fs,
					Konfiguration.TEST_DATEN_VERZ
							+ Konfiguration.DATENCSV_PARAMETER);
			kzdImport.importParaAusfall();
		} catch (Exception e) {
			Debug.getLogger().error("Kann Test nicht konfigurieren: " + e);
		}
	}

	/**
	 * Prüfung der Ausfallkontrolle.
	 * 
	 * @throws Exception
	 *             wird weitergereicht
	 */
	public void pruefe() throws Exception {
		/*
		 * Sender anmelden
		 */
		this.dav.subscribeSender(this, fs, ddKzdSend, SenderRole.source());

		/*
		 * Initialisiere Parameter Importer für fehlerfreie und fehlerhafte DS
		 */
		TestFahrstreifenImporter paraImpFSOK = null;
		TestFahrstreifenImporter paraImpFSFehler = null;

		paraImpFSOK = new TestFahrstreifenImporter(this.dav,
				Konfiguration.TEST_DATEN_VERZ + Konfiguration.DATENCSV_FS_OK);
		paraImpFSFehler = new TestFahrstreifenImporter(this.dav,
				Konfiguration.TEST_DATEN_VERZ
						+ Konfiguration.DATENCSV_FS_FEHLER);

		/*
		 * Setze Intervallparameter
		 */
		TestFahrstreifenImporter.setT(TestParameter.INTERVALL_VB);
		// paraImpFSOK.setT(INTERVALL);
		// paraImpFSFehler.setT(INTERVALL);
		// paraImpFSOK.setT(60000L);
		// paraImpFSFehler.setT(60000L);
		/*
		 * Aktuelle fehlerfreie und fehlerhafte Fahrstreifen-DS
		 */
		Data zeileFSOK;
		Data zeileFSFehler;

		Long pruefZeit = System.currentTimeMillis();
		Long aktZeit;

		/*
		 * Sendet fehlerfreie DS für einen Tag
		 */
		System.out.println("Sende fehlerfreie DS für 1 Tag (1440)");
		for (int i = 1; i <= 1440; i++) {

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

			// Erhöht Prüfzeitstempel entsprechend der Intervalllänge
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

		/*
		 * Prüfung
		 */
		System.out.println("Beginne Prüfung");

		/*
		 * Initialisiert Meldungsfilter
		 */
		FilterMeldung meldFilter = new FilterMeldung(this, dav,
				"Ausfallhäufigkeit", 1457, meldungHyst);
		Debug
				.getLogger()
				.info(
						"Meldungsfilter initialisiert: Erwarte 1457 Meldungen mit \"Ausfallhäufigkeit\"");

		/*
		 * Sende 2500 Datensätze
		 */
		for (int i = 1; i <= 2500; i++) {
			/*
			 * Sende im Intervall von 929 - 1032 fehlerhafte Daten Dabei wird
			 * zum Intervall 972 der Maximalwert der Ausfallhäufigkeit (3% =
			 * 43,2 Intervalle) überschritten und zum Intervall 2429 wieder
			 * unterschritten
			 * 
			 * Für die restlichen Intervalle werden fehlerfreie Daten gesendet
			 */
			if (i >= 929 && i <= 1032) {

				if ((zeileFSFehler = paraImpFSFehler
						.getNaechstenDatensatz(ddKzdSend.getAttributeGroup())) == null) {
					paraImpFSFehler.reset();
					paraImpFSFehler.getNaechsteZeile();
					zeileFSFehler = paraImpFSFehler
							.getNaechstenDatensatz(ddKzdSend
									.getAttributeGroup());
				}

				Data dummy = zeileFSFehler.createModifiableCopy();
				// boolean set = false;
				for (String attribut : new String[] { "qKfz", "qLkw", "qPkw",
						"vKfz", "vLkw", "vPkw", "b" }) {
					// if(R.nextBoolean()){
					// set = true;
					// if(R.nextBoolean()){
					dummy.getItem(attribut).getUnscaledValue("Wert").set(
							DUAKonstanten.FEHLERHAFT);
					// if(R.nextBoolean()){
					// dummy.getItem(attribut).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").set(DUAKonstanten.JA);
					// }
					// }else{
					// dummy.getItem(attribut).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").set(DUAKonstanten.JA);
					// if(R.nextBoolean()){
					// dummy.getItem(attribut).getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT);
					// }
					// }
					// }
				}
				// if(!set){
				// dummy.getItem("qPkw").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT);
				// }

				System.out.println(DUAKonstanten.ZEIT_FORMAT_GENAU
						.format(new Date(pruefZeit))
						+ ": Intervall " + i + ": Sende Datum FEHLER");
				ResultData resultat1 = new ResultData(fs, ddKzdSend, pruefZeit,
						dummy);
				this.dav.sendData(resultat1);
			} else {
				System.out.println(DUAKonstanten.ZEIT_FORMAT_GENAU
						.format(new Date(pruefZeit))
						+ ": Intervall " + i + ": Sende Datum OK");
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
			}
			// Erhöht Prüfzeitstempel entsprechend der Intervalllänge
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

		try {
			Thread.sleep(5000L);
		} catch (InterruptedException e) {
			//
		}

		String warnung = meldFilter.getErwarteteAnzahlMeldungen()
				+ " Betriebsmeldungen erhalten";
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
	 * (Kein Javadoc).
	 * 
	 * @see de.bsvrz.dua.plloglve.util.PlPruefungInterface#doNotify()
	 */
	public void doNotify() {
		// filterTimeout = false;
		synchronized (this) {
			this.notify();
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
	 *            Asserts?
	 */
	public void benutzeAssert(final boolean useAssert1) {
		this.useAssert = useAssert1;
	}

	/**
	 * Setzt die erlaubte Abweichung zur erwarteten Anzahl an Betriebsmeldungen.
	 * 
	 * @param meldungHyst1
	 *            meldungHyst?
	 */
	public void setMeldungHysterese(final int meldungHyst1) {
		this.meldungHyst = meldungHyst1;
	}
}
