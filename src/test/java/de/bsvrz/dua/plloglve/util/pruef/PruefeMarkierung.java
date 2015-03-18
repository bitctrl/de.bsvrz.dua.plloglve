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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Assert;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.plloglve.util.PlPruefungInterface;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * KZD Listener Liest Ergebnis-CSV-Datei Wartet auf gesendete und gepruefte
 * Daten und gibt diese an Vergleicher-Klasse weiter.
 *
 * @author BitCtrl Systems GmbH, Görlitz
 *
 * @version $Id$
 */
public class PruefeMarkierung implements ClientReceiverInterface {

	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * Ob <code>Assert...</code> benutzt werden soll oder blos Warnungen
	 * ausgegeben werden sollen
	 */
	private boolean useAssert = true;

	/**
	 * Datenverteilerverbindung von der aufrufenden Klasse.
	 */
	private final ClientDavInterface dav;

	/**
	 * die Attribute, die nicht ueberprueft werden sollen.
	 */
	private final List<String> ignoreAttributeList = new ArrayList<String>();

	/**
	 * Aufrunfende Klasse.
	 */
	private final PlPruefungInterface caller;

	/**
	 * Zeitstempel der zu pruefenden Daten.
	 */
	private long pruefZeitstempel = -1;

	/**
	 * Gibt an, welches Attribut getestet werden soll "alle" = prüfe alle
	 * Attribute.
	 */
	private boolean pruefeAlleAttr = false;
	private String pruefeAttr;

	/**
	 * Gibt an, auf welchen Wert getestet werden soll
	 */
	private long sollWert;

	/**
	 * Gibt an, auf welchen Implausibelzustand getestet werden soll
	 */
	private int sollImplausibel;

	/**
	 * Der repräsentative Wert um die Prüfung des Wertes abzuschalten
	 */
	private static int SOLL_WERT_KEINE_PRUEFUNG = 0;

	/**
	 * Der repräsentative Wert für die Prüfung auf einen fehlerfreien Wert
	 */
	private static int SOLL_WERT_KEIN_FEHLER = 1;

	/**
	 * Der repräsentative Wert um die Prüfung des Implausibelzustandes
	 * abzuschalten
	 */
	private static int SOLL_IMPLAUSIBEL_KEINE_PRUEFUNG = -1;

	/**
	 * Empfange-Datenbeschreibung für KZD
	 */
	public static DataDescription DD_KZD_EMPF = null;

	/**
	 * Empfange-Datenbeschreibung für LZD
	 */
	public static DataDescription DD_LZD_EMPF = null;

	/**
	 * Prüft einen Ergebnisdatensatz mit entsprechendem Zeitstempel auf
	 * Fehlerfreiheit bzw. Fehlerhaftigkeit
	 *
	 * @param caller
	 *            Die aufrufende Klasse
	 * @param dav
	 *            Datenverteilerverbindung
	 * @param fs
	 *            Das zu prüfende Fahrstreifenobjekt
	 * @throws Exception
	 */
	public PruefeMarkierung(final PlPruefungInterface caller,
			final ClientDavInterface dav, final SystemObject fs)
			throws Exception {
		this.caller = caller; // aufrufende Klasse uebernehmen
		this.dav = dav;

		/*
		 * Melde Empfänger für KZD und LZD unter dem Aspekt PlPrüfung Logisch an
		 */
		PruefeMarkierung.DD_KZD_EMPF = new DataDescription(this.dav
				.getDataModel().getAttributeGroup(DUAKonstanten.ATG_KZD),
				this.dav.getDataModel().getAspect(
						DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH));

		this.dav.subscribeReceiver(this, fs, PruefeMarkierung.DD_KZD_EMPF,
				ReceiveOptions.normal(), ReceiverRole.receiver());
	}

	/**
	 * Konfiguriert dieses Objekt auf die Prüfung von fehlerfreien Daten eines
	 * Attribut
	 *
	 * @param pruefeAttr
	 *            Zu prüfende Attribut
	 * @param pruefZeitstempel
	 *            Der zu prüfende Zeitstempel
	 */
	public void listenOK(final String pruefeAttr, final long pruefZeitstempel) {
		pruefeAlleAttr = false;
		paramOK(pruefeAttr, pruefZeitstempel);
		LOGGER.info(
				"Prüfe Datum auf fehlerfreies Attribut: " + pruefeAttr); //$NON-NLS-1$
	}

	/**
	 * Konfiguriert dieses Objekt auf die Prüfung von fehlerfreien Daten aller
	 * Attribute
	 *
	 * @param pruefZeitstempel
	 *            Der zu prüfende Zeitstempel
	 */
	public void listenOK(final long pruefZeitstempel) {
		pruefeAlleAttr = true;
		paramOK("alle", pruefZeitstempel); //$NON-NLS-1$
		LOGGER.info(
				"Prüfe alle Attribute des Datums auf fehlerfreiheit"); //$NON-NLS-1$
	}

	/**
	 * Abschließende Konfiguration des Objektes auf die Prüfung von fehlerfreien
	 * Daten
	 *
	 * @param pruefeAttr
	 *            Zu prüfende(s) Attribut(e)
	 * @param pruefZeitstempel
	 *            Der zu prüfende Zeitstempel
	 */
	private void paramOK(final String pruefeAttr, final long pruefZeitstempel) {
		this.pruefZeitstempel = pruefZeitstempel;
		this.pruefeAttr = pruefeAttr;
		this.sollWert = PruefeMarkierung.SOLL_WERT_KEIN_FEHLER;
		this.sollImplausibel = DUAKonstanten.NEIN;
	}

	/**
	 * Konfiguriert dieses Objekt für die Prüfung auf Fehlerhaft-Markierung
	 * eines Attributes
	 *
	 * @param pruefeAttr
	 *            Zu prüfendes Attribut
	 * @param pruefZeitstempel
	 *            Der zu prüfende Zeitstempel
	 */
	public void listenFehl(final String pruefeAttr, final long pruefZeitstempel) {
		pruefeAlleAttr = false;
		paramFehl(pruefeAttr, pruefZeitstempel);
		LOGGER
		.info("Prüfe Datum auf Fehlerhaft-Markierung des Attributes: " + pruefeAttr); //$NON-NLS-1$
	}

	/**
	 * Konfiguriert dieses Objekt für die Prüfung auf Fehlerhaft-Markierung
	 * aller Attribute
	 *
	 * @param pruefZeitstempel
	 *            Der zu prüfende Zeitstempel
	 */
	public void listenFehl(final long pruefZeitstempel) {
		pruefeAlleAttr = true;
		paramFehl("alle", pruefZeitstempel); //$NON-NLS-1$
		LOGGER.info(
				"Prüfe Datum auf Fehlerhaft-Markierung aller Attribute"); //$NON-NLS-1$
	}

	/**
	 * Abschließende Konfiguration des Objektes für die Prüfung auf
	 * Fehlerhaft-Markierung
	 *
	 * @param pruefeAttr
	 *            Zu prüfende(s) Attribut(e)
	 * @param pruefZeitstempel
	 *            Der zu prüfende Zeitstempel
	 */
	private void paramFehl(final String pruefeAttr, final long pruefZeitstempel) {
		this.pruefZeitstempel = pruefZeitstempel;
		this.pruefeAttr = pruefeAttr;
		this.sollWert = DUAKonstanten.FEHLERHAFT;
		this.sollImplausibel = PruefeMarkierung.SOLL_IMPLAUSIBEL_KEINE_PRUEFUNG;
	}

	/**
	 * Konfiguriert dieses Objekt für die Prüfung auf Implausibel-Markierung
	 * eines Attributes
	 *
	 * @param pruefeAttr
	 *            Zu prüfendes Attribut
	 * @param pruefZeitstempel
	 *            Der zu prüfende Zeitstempel
	 */
	public void listenImpl(final String pruefeAttr, final long pruefZeitstempel) {
		pruefeAlleAttr = false;
		paramImpl(pruefeAttr, pruefZeitstempel);
		LOGGER
		.info("Prüfe Datum auf Implausibel-Markierung des Attributes: " + pruefeAttr); //$NON-NLS-1$
	}

	/**
	 * Konfiguriert dieses Objekt für die Prüfung auf Implausibel-Markierung
	 * aller Attribute
	 *
	 * @param pruefZeitstempel
	 *            Der zu prüfende Zeitstempel
	 */
	public void listenImpl(final long pruefZeitstempel) {
		pruefeAlleAttr = true;
		paramImpl("alle", pruefZeitstempel); //$NON-NLS-1$
		LOGGER.info(
				"Prüfe Datum auf Implausibel-Markierung aller Attribute"); //$NON-NLS-1$
	}

	/**
	 * Abschließende Konfiguration des Objektes für die Prüfung auf
	 * Implausibel-Markierung
	 *
	 * @param pruefeAttr
	 *            Zu prüfende(s) Attribut(e)
	 * @param pruefZeitstempel
	 *            Der zu prüfende Zeitstempel
	 */
	private void paramImpl(final String pruefeAttr, final long pruefZeitstempel) {
		this.pruefZeitstempel = pruefZeitstempel;
		this.pruefeAttr = pruefeAttr;
		this.sollWert = PruefeMarkierung.SOLL_WERT_KEINE_PRUEFUNG;
		this.sollImplausibel = DUAKonstanten.JA;
	}

	/**
	 * Konfiguriert dieses Objekt für die Prüfung auf Fehlerhaft- und
	 * Implausibel-Markierung eines Attributes
	 *
	 * @param pruefeAttr
	 *            Zu prüfendes Attribut
	 * @param pruefZeitstempel
	 *            Der zu prüfende Zeitstempel
	 */
	public void listenFehlImpl(final String pruefeAttr,
			final long pruefZeitstempel) {
		pruefeAlleAttr = false;
		paramFehlImpl(pruefeAttr, pruefZeitstempel);
		LOGGER
		.info("Prüfe Datum auf Fehlerhaft- und Implausibel-Markierung des Attributes: " + pruefeAttr); //$NON-NLS-1$
	}

	/**
	 * Konfiguriert dieses Objekt für die Prüfung auf Fehlerhaft- und
	 * Implausibel-Markierung aller Attribute
	 *
	 * @param pruefZeitstempel
	 *            Der zu prüfende Zeitstempel
	 */
	public void listenFehlImpl(final long pruefZeitstempel) {
		pruefeAlleAttr = true;
		paramFehlImpl("alle", pruefZeitstempel); //$NON-NLS-1$
		LOGGER
		.info("Prüfe Datum auf Fehlerhaft- und Implausibel-Markierung aller Attribute"); //$NON-NLS-1$
	}

	/**
	 * Abschließende Konfiguration des Objektes für die Prüfung auf Fehlerhaft-
	 * und Implausibel-Markierung
	 *
	 * @param pruefeAttr
	 *            Zu prüfende(s) Attribut(e)
	 * @param pruefZeitstempel
	 *            Der zu prüfende Zeitstempel
	 */
	private void paramFehlImpl(final String pruefeAttr,
			final long pruefZeitstempel) {
		this.pruefZeitstempel = pruefZeitstempel;
		this.pruefeAttr = pruefeAttr;
		this.sollWert = DUAKonstanten.FEHLERHAFT;
		this.sollImplausibel = DUAKonstanten.JA;
	}

	/**
	 * Fuegt ein Attribut hinzu, dass nicht ueberprueft werden soll
	 *
	 * @param attribut
	 *            der Name des Attributs
	 */
	public final void addIgnore(final String attribut) {
		synchronized (ignoreAttributeList) {
			this.ignoreAttributeList.add(attribut);
		}
	}

	/**
	 * Prueft Daten entsprechend der Konfiguration
	 *
	 * @param data
	 *            Ergebnisdatensatz
	 */
	private void pruefeDS(final Data data) {
		if (pruefeAlleAttr) {
			for (final String attribut : new String[] { "qKfz", "qLkw", "vPkw",
					"vLkw", "b", "sKfz" }) {
				boolean ignore = false;
				synchronized (this.ignoreAttributeList) {
					for (final String attrIgnore : this.ignoreAttributeList) {
						if (attribut.equals(attrIgnore)) {
							ignore = true;
							break;
						}
					}
				}
				if (!ignore) {
					pruefeAttr(attribut, data);
				}
			}
			synchronized (this.ignoreAttributeList) {
				this.ignoreAttributeList.clear();
			}
		} else {
			synchronized (this.ignoreAttributeList) {
				for (final String attrIgnore : this.ignoreAttributeList) {
					if (pruefeAttr.equals(attrIgnore)) {
						this.ignoreAttributeList.clear();
						return;
					}
				}
				this.ignoreAttributeList.clear();
			}
			pruefeAttr(pruefeAttr, data);
		}
	}

	/**
	 * Prueft Attribut entsprechend der Konfiguration
	 *
	 * @param pruefeAttr
	 *            Das zu prüfende Attribut
	 * @param data
	 *            Ergebnisdatensatz
	 */
	private void pruefeAttr(final String pruefeAttr, final Data data) {
		final long wert = data.getItem(pruefeAttr)
				.getUnscaledValue("Wert").longValue(); //$NON-NLS-1$
		final int impl = data
				.getItem(pruefeAttr)
				.getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").intValue(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		final SimpleDateFormat dateFormat = new SimpleDateFormat(
				DUAKonstanten.ZEIT_FORMAT_GENAU_STR);
		if (sollWert < 0) {
			if (wert != sollWert) {
				final String fehler = "Fehlerhafter Attributwert (" + pruefeAttr + "): " + sollWert + " (SOLL)<>(IST) " + wert + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						"\n" + data;
				if (useAssert) {
					// System.out.println(DUAKonstanten.ZEIT_FORMAT_GENAU.format(new
					// Date(this.pruefZeitstempel)) + " --> FEHLER\n" + fehler);
					Assert.assertTrue(fehler, false);
				} else {
					System.out.println(dateFormat.format(new Date(
							this.pruefZeitstempel)) + " --> FEHLER\n" + fehler);
				}
			} else {
				System.out
				.println(dateFormat.format(new Date(
						this.pruefZeitstempel))
						+ " --> OK (" + pruefeAttr + "):\n" + sollWert + " (SOLL)==(IST) " + wert); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			}
		} else if (sollWert == PruefeMarkierung.SOLL_WERT_KEIN_FEHLER) {
			if (wert < 0) {
				final String fehler = "Fehlerhafter Attributwert (" + pruefeAttr + "): Wert >= 0 (SOLL)<>(IST) " + wert; //$NON-NLS-1$ //$NON-NLS-2$
				if (useAssert) {
					// System.out.println(DUAKonstanten.ZEIT_FORMAT_GENAU.format(new
					// Date(this.pruefZeitstempel)) + " --> FEHLER\n" + fehler);
					Assert.assertTrue(fehler, false);
				} else {
					System.out.println(dateFormat.format(new Date(
							this.pruefZeitstempel)) + " --> FEHLER\n" + fehler);
				}
			} else {
				System.out
				.println(dateFormat.format(new Date(
						this.pruefZeitstempel))
						+ " --> OK " + pruefeAttr + " :\n (Wert >= 0) (SOLL)==(IST) " + wert); //$NON-NLS-1$//$NON-NLS-2$
			}
		}

		if (sollImplausibel != PruefeMarkierung.SOLL_IMPLAUSIBEL_KEINE_PRUEFUNG) {
			if (sollImplausibel != impl) {
				final String fehler = "Fehlerhafte Implausibel-Markierung (" + pruefeAttr + "): " + sollImplausibel + " (SOLL)<>(IST) " + impl + ",\n" + data; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				if (useAssert) {
					// System.out.println(DUAKonstanten.ZEIT_FORMAT_GENAU.format(new
					// Date(this.pruefZeitstempel)) + " --> FEHLER\n" + fehler);
					Assert.assertTrue(fehler, false);
				} else {
					System.out.println(dateFormat.format(new Date(
							this.pruefZeitstempel)) + " --> FEHLER\n" + fehler);
				}
			} else {
				System.out
				.println(dateFormat.format(new Date(
						this.pruefZeitstempel))
						+ " --> OK " + pruefeAttr + " (" + sollImplausibel + "):\n" + (impl == 0 ? "nicht implausibel" : "implausibel") + " (SOLL)==(IST) " + (sollImplausibel == 0 ? "nicht implausibel" : "implausibel")); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(final ResultData[] results) {
		for (final ResultData result : results) {
			// Pruefe Ergebnisdatensatz auf Zeitstempel
			if ((pruefZeitstempel > 0) && (result.getData() != null)
					&& (result.getDataTime() == pruefZeitstempel)) {
				pruefeDS(result.getData());
				caller.doNotify();
			}
		}
	}

	/**
	 * Soll Assert zur Fehlermeldung genutzt werden?
	 *
	 * @param useAssert
	 *            <code>True</code> wenn Asserts verwendet werden sollen, sonst
	 *            <code>False</code>
	 */
	public void benutzeAssert(final boolean useAssert) {
		this.useAssert = useAssert;
	}

}