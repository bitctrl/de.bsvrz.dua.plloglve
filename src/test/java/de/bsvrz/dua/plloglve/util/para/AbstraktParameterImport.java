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

package de.bsvrz.dua.plloglve.util.para;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.plloglve.plloglve.standard.AtgVerkehrsDatenLZIPlPruefLogisch;
import de.bsvrz.dua.plloglve.plloglve.typen.OptionenPlausibilitaetsPruefungLogischVerkehr;
import de.bsvrz.sys.funclib.bitctrl.daf.DaVKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.test.CSVImporter;

/**
 * Abstrakte Klasse zum Einlesen von Parametern aus der CSV-Datei innerhalb der
 * Prüfspezifikation.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public abstract class AbstraktParameterImport extends CSVImporter implements
ClientSenderInterface {

	/**
	 * Verbindung zum Datenverteiler.
	 */
	protected static ClientDavInterface sDav = null;

	/**
	 * Systemobjekt, für das die Parameter gesetzt werden sollen.
	 */
	protected SystemObject objekt = null;

	/**
	 * Attributgruppe VerkehrsDatenDifferenzialKontrolleFs.
	 */
	private final AttributeGroup diffFs;

	/**
	 * Attributgruppe VerkehrsDatenAusfallHäufigkeitFs.
	 */
	private final AttributeGroup ausfallHFs;

	/**
	 * Attributgruppe VerkehrsDatenVertrauensBereichFs.
	 */
	private final AttributeGroup vertrauensbereichFs;

	/**
	 * Datenbeschreibung Logisch.
	 */
	private final DataDescription ddLogisch;

	/**
	 * Datenbeschreibung Differentialkontrolle.
	 */
	private final DataDescription ddDiff;

	/**
	 * Datenbeschreibung Ausfallhäufigkeit.
	 */
	private final DataDescription ddAusfall;

	/**
	 * Datenbeschreibung Vertrauensbereich.
	 */
	private final DataDescription ddVertrauensBereich;

	/**
	 * Standardkonstruktor.
	 *
	 * @param dav
	 *            Datenverteier-Verbindung
	 * @param objekt
	 *            das Systemobjekt, für das die Parameter gesetzt werden sollen
	 * @param csvQuelle
	 *            Quelle der Daten (CSV-Datei)
	 * @throws Exception
	 *             falls dieses Objekt nicht vollständig initialisiert werden
	 *             konnte
	 */
	public AbstraktParameterImport(final ClientDavInterface dav,
			final SystemObject objekt, final String csvQuelle) throws Exception {
		super(csvQuelle);
		if (AbstraktParameterImport.sDav == null) {
			AbstraktParameterImport.sDav = dav;
		}

		diffFs = AbstraktParameterImport.sDav.getDataModel().getAttributeGroup(
				"atg.verkehrsDatenDifferenzialKontrolleFs"); //$NON-NLS-1$
		ausfallHFs = AbstraktParameterImport.sDav.getDataModel()
				.getAttributeGroup("atg.verkehrsDatenAusfallHäufigkeitFs"); //$NON-NLS-1$
		vertrauensbereichFs = AbstraktParameterImport.sDav.getDataModel()
				.getAttributeGroup("atg.verkehrsDatenVertrauensBereichFs"); //$NON-NLS-1$

		ddLogisch = new DataDescription(this.getParameterAtg(),
				AbstraktParameterImport.sDav.getDataModel().getAspect(
						DaVKonstanten.ASP_PARAMETER_VORGABE));

		ddDiff = new DataDescription(diffFs, AbstraktParameterImport.sDav
				.getDataModel().getAspect(DaVKonstanten.ASP_PARAMETER_VORGABE));

		ddAusfall = new DataDescription(ausfallHFs,
				AbstraktParameterImport.sDav.getDataModel().getAspect(
						DaVKonstanten.ASP_PARAMETER_VORGABE));

		ddVertrauensBereich = new DataDescription(vertrauensbereichFs,
				AbstraktParameterImport.sDav.getDataModel().getAspect(
						DaVKonstanten.ASP_PARAMETER_VORGABE));

		this.objekt = objekt;

		/**
		 * Tabellenkopf überspringen
		 */
		this.getNaechsteZeile();

		/**
		 * Parameter für Differentialkontrolle, Ausfallhäufigkeit und
		 * Vertrauensbereich deaktivieren bzw. zurücksetzen
		 */
		if (!AbstraktParameterImport.sDav.getDataModel()
				.getAttributeGroup(AtgVerkehrsDatenLZIPlPruefLogisch.getPid())
				.equals(this.getParameterAtg())) {
			AbstraktParameterImport.sDav.subscribeSender(this, objekt,
					ddLogisch, SenderRole.sender());
			AbstraktParameterImport.sDav.subscribeSender(this, objekt, ddDiff,
					SenderRole.sender());
			AbstraktParameterImport.sDav.subscribeSender(this, objekt,
					ddAusfall, SenderRole.sender());
			AbstraktParameterImport.sDav.subscribeSender(this, objekt,
					ddVertrauensBereich, SenderRole.sender());

			deaktiviereParaStandard(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MAX);
			deaktiviereParaStandard(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MIN);
			deaktiviereParaStandard(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MIN_MAX);
			deaktiviereParaStandard(OptionenPlausibilitaetsPruefungLogischVerkehr.NUR_PRUEFUNG);
			deaktiviereParaStandard(OptionenPlausibilitaetsPruefungLogischVerkehr.KEINE_PRUEFUNG);

			deaktiviereParaDiff();
			deaktiviereParaAusfall();
			deaktiviereParaVertrauensbereich();

			AbstraktParameterImport.sDav.unsubscribeSender(this, objekt,
					ddLogisch);
			AbstraktParameterImport.sDav
					.unsubscribeSender(this, objekt, ddDiff);
			AbstraktParameterImport.sDav.unsubscribeSender(this, objekt,
					ddAusfall);
			AbstraktParameterImport.sDav.unsubscribeSender(this, objekt,
					ddVertrauensBereich);
		}
	}

	/**
	 * Führt den Parameterimport aus.
	 *
	 * @param index
	 *            der Index
	 * @throws Exception
	 *             wenn die Parameter nicht vollständig importiert werden
	 *             konnten
	 */
	public final void importiereParameter(final int index) throws Exception {
		AbstraktParameterImport.sDav.subscribeSender(this, objekt, ddLogisch,
				SenderRole.sender());

		this.reset();
		this.getNaechsteZeile();
		String[] zeile = null;

		final Data parameter = AbstraktParameterImport.sDav.createData(this
				.getParameterAtg());

		while ((zeile = this.getNaechsteZeile()) != null) {
			final String attributInCSVDatei = zeile[0];
			final String wert = zeile[1];

			final String attPfad = getAttributPfadVon(attributInCSVDatei, index);
			if (attPfad != null) {
				try {
					final long l = Long.parseLong(wert);
					DUAUtensilien.getAttributDatum(attPfad, parameter)
					.asUnscaledValue().set(l);
				} catch (final NumberFormatException ex) {
					final double d = Double.parseDouble(wert);
					DUAUtensilien.getAttributDatum(attPfad, parameter)
					.asUnscaledValue().set(d);
				}
			}
		}

		final ResultData resultat = new ResultData(this.objekt,
				new DataDescription(this.getParameterAtg(),
						AbstraktParameterImport.sDav.getDataModel().getAspect(
								DaVKonstanten.ASP_PARAMETER_VORGABE)),
						System.currentTimeMillis(), this.fuelleRestAttribute(parameter));
		AbstraktParameterImport.sDav.sendData(resultat);

		AbstraktParameterImport.sDav.unsubscribeSender(this, objekt, ddLogisch);
	}

	/**
	 * Setzt Standard-Attribute zurück.
	 *
	 * @param optionen
	 *            Optioens-Parameter
	 * @throws Exception
	 *             wird weitergereicht
	 */
	private void deaktiviereParaStandard(
			final OptionenPlausibilitaetsPruefungLogischVerkehr optionen)
					throws Exception {
		final Data parameter = AbstraktParameterImport.sDav.createData(this
				.getParameterAtg());
		DUAUtensilien
		.getAttributDatum("Optionen", parameter).asUnscaledValue().set(optionen.getCode()); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("qKfzBereich.Min", parameter).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("qKfzBereich.Max", parameter).asUnscaledValue().set(999999); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("qLkwBereich.Min", parameter).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("qLkwBereich.Max", parameter).asUnscaledValue().set(999999); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("qPkwBereich.Min", parameter).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("qPkwBereich.Max", parameter).asUnscaledValue().set(999999); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("vKfzBereich.Min", parameter).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("vKfzBereich.Max", parameter).asUnscaledValue().set(254); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("vLkwBereich.Min", parameter).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("vLkwBereich.Max", parameter).asUnscaledValue().set(254); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("vPkwBereich.Min", parameter).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("vPkwBereich.Max", parameter).asUnscaledValue().set(254); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("vgKfzBereich.Min", parameter).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("vgKfzBereich.Max", parameter).asUnscaledValue().set(254); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("BelegungBereich.Min", parameter).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("BelegungBereich.Max", parameter).asUnscaledValue().set(100); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("vKfzGrenz", parameter).asUnscaledValue().set(101); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("bGrenz", parameter).asUnscaledValue().set(53); //$NON-NLS-1$

		final ResultData resultat = new ResultData(this.objekt,
				new DataDescription(this.getParameterAtg(),
						AbstraktParameterImport.sDav.getDataModel().getAspect(
								DaVKonstanten.ASP_PARAMETER_VORGABE)),
						System.currentTimeMillis(), parameter);
		AbstraktParameterImport.sDav.sendData(resultat);
	}

	/**
	 * Setzt Attribute der Differentialkontrolle entsprechend den PrSpez
	 * (5.1.3.10.2).
	 *
	 * @throws Exception
	 *             wird weitergereicht
	 */
	public final void importParaDiff() throws Exception {
		AbstraktParameterImport.sDav.subscribeSender(this, objekt, ddDiff,
				SenderRole.sender());

		final Data parameter = AbstraktParameterImport.sDav.createData(diffFs);
		DUAUtensilien
		.getAttributDatum("maxAnzKonstanzqKfz", parameter).asUnscaledValue().set(3); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("maxAnzKonstanzqLkw", parameter).asUnscaledValue().set(3); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("maxAnzKonstanzqPkw", parameter).asUnscaledValue().set(3); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("maxAnzKonstanzvKfz", parameter).asUnscaledValue().set(3); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("maxAnzKonstanzvLkw", parameter).asUnscaledValue().set(5); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("maxAnzKonstanzvPkw", parameter).asUnscaledValue().set(2); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("maxAnzKonstanzStreung", parameter).asUnscaledValue().set(10); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("maxAnzKonstanzBelegung", parameter).asUnscaledValue().set(3); //$NON-NLS-1$

		final ResultData resultat = new ResultData(this.objekt,
				new DataDescription(diffFs, AbstraktParameterImport.sDav
						.getDataModel().getAspect(
								DaVKonstanten.ASP_PARAMETER_VORGABE)),
						System.currentTimeMillis(), parameter);
		AbstraktParameterImport.sDav.sendData(resultat);

		AbstraktParameterImport.sDav.unsubscribeSender(this, objekt, ddDiff);
	}

	/**
	 * Setzt Attribute der Differentialkontrolle zurück.
	 *
	 * @throws Exception
	 *             wird weitergereicht
	 */
	private void deaktiviereParaDiff() throws Exception {
		final Data parameter = AbstraktParameterImport.sDav.createData(diffFs);
		DUAUtensilien
		.getAttributDatum("maxAnzKonstanzqKfz", parameter).asUnscaledValue().set(99999999); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("maxAnzKonstanzqLkw", parameter).asUnscaledValue().set(99999999); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("maxAnzKonstanzqPkw", parameter).asUnscaledValue().set(99999999); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("maxAnzKonstanzvKfz", parameter).asUnscaledValue().set(99999999); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("maxAnzKonstanzvLkw", parameter).asUnscaledValue().set(99999999); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("maxAnzKonstanzvPkw", parameter).asUnscaledValue().set(99999999); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("maxAnzKonstanzStreung", parameter).asUnscaledValue().set(99999999); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("maxAnzKonstanzBelegung", parameter).asUnscaledValue().set(99999999); //$NON-NLS-1$

		final ResultData resultat = new ResultData(this.objekt,
				new DataDescription(diffFs, AbstraktParameterImport.sDav
						.getDataModel().getAspect(
								DaVKonstanten.ASP_PARAMETER_VORGABE)),
						System.currentTimeMillis(), parameter);
		AbstraktParameterImport.sDav.sendData(resultat);
	}

	/**
	 * Setzt Attribute der Ausfallkontrolle entsprechend den PrSpez
	 * (5.1.3.10.2).
	 *
	 * @throws Exception
	 *             wird weitergereicht
	 */
	public final void importParaAusfall() throws Exception {
		AbstraktParameterImport.sDav.subscribeSender(this, objekt, ddAusfall,
				SenderRole.sender());

		final Data parameter = AbstraktParameterImport.sDav
				.createData(ausfallHFs);
		DUAUtensilien
		.getAttributDatum("maxAusfallProTag", parameter).asUnscaledValue().set(3); //$NON-NLS-1$

		final ResultData resultat = new ResultData(this.objekt,
				new DataDescription(ausfallHFs, AbstraktParameterImport.sDav
						.getDataModel().getAspect(
								DaVKonstanten.ASP_PARAMETER_VORGABE)),
						System.currentTimeMillis(), parameter);
		AbstraktParameterImport.sDav.sendData(resultat);

		AbstraktParameterImport.sDav.unsubscribeSender(this, objekt, ddAusfall);
	}

	/**
	 * Setzt Attribute der Ausfallkontrolle zurück.
	 *
	 * @throws Exception
	 *             wird weitergereicht
	 */
	private void deaktiviereParaAusfall() throws Exception {
		final Data parameter = AbstraktParameterImport.sDav
				.createData(ausfallHFs);
		DUAUtensilien
		.getAttributDatum("maxAusfallProTag", parameter).asUnscaledValue().set(99); //$NON-NLS-1$

		final ResultData resultat = new ResultData(this.objekt,
				new DataDescription(ausfallHFs, AbstraktParameterImport.sDav
						.getDataModel().getAspect(
								DaVKonstanten.ASP_PARAMETER_VORGABE)),
						System.currentTimeMillis(), parameter);
		AbstraktParameterImport.sDav.sendData(resultat);
	}

	/**
	 * Setzt Attribute des Vertrauensbereich entsprechend den PrSpez
	 * (5.1.3.10.2).
	 *
	 * @throws Exception
	 *             wird weitergereicht
	 */
	public final void importParaVertrauensbereich() throws Exception {
		AbstraktParameterImport.sDav.subscribeSender(this, objekt,
				ddVertrauensBereich, SenderRole.sender());

		final Data parameter = AbstraktParameterImport.sDav
				.createData(vertrauensbereichFs);

		DUAUtensilien
		.getAttributDatum("BezugsZeitraum", parameter).asUnscaledValue().set(1); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("maxAusfallProBezugsZeitraumEin", parameter).asUnscaledValue().set(20); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("maxAusfallProBezugsZeitraumAus", parameter).asUnscaledValue().set(20); //$NON-NLS-1$
		final ResultData resultat = new ResultData(this.objekt,
				new DataDescription(vertrauensbereichFs,
						AbstraktParameterImport.sDav.getDataModel().getAspect(
								DaVKonstanten.ASP_PARAMETER_VORGABE)),
						System.currentTimeMillis(), parameter);
		AbstraktParameterImport.sDav.sendData(resultat);

		AbstraktParameterImport.sDav.unsubscribeSender(this, objekt,
				ddVertrauensBereich);
	}

	/**
	 * Setzt Attribute des Vertrauensbereich zurück.
	 *
	 * @throws Exception
	 *             wird weitergereicht
	 */
	private void deaktiviereParaVertrauensbereich() throws Exception {
		final Data parameter = AbstraktParameterImport.sDav
				.createData(vertrauensbereichFs);

		DUAUtensilien
		.getAttributDatum("BezugsZeitraum", parameter).asUnscaledValue().set(24); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("maxAusfallProBezugsZeitraumEin", parameter).asUnscaledValue().set(99); //$NON-NLS-1$
		DUAUtensilien
		.getAttributDatum("maxAusfallProBezugsZeitraumAus", parameter).asUnscaledValue().set(99); //$NON-NLS-1$
		final ResultData resultat = new ResultData(this.objekt,
				new DataDescription(vertrauensbereichFs,
						AbstraktParameterImport.sDav.getDataModel().getAspect(
								DaVKonstanten.ASP_PARAMETER_VORGABE)),
						System.currentTimeMillis(), parameter);
		AbstraktParameterImport.sDav.sendData(resultat);
	}

	/**
	 * Setzt alle restlichen Attribute innerhalb von diesem Datensatz abhängig
	 * von der tatsächlichen Attributgruppe.
	 *
	 * @param datensatz
	 *            ein Datensatz
	 * @return der veränderte (vollständig ausgefüllte Datensatz)
	 */
	public Data fuelleRestAttribute(final Data datensatz) {
		return datensatz;
	}

	/**
	 * Erfragt den Attributpfad zu einem Attribut, das in der CSV-Datei den
	 * übergebenen Namen hat.
	 *
	 * @param attributInCSVDatei
	 *            Attributname innerhalb der CSV-Datei
	 * @param index
	 *            index innerhalb von CVS-Datei
	 * @return den kompletten Attributpfad zum assoziierten DAV-Attribut
	 */
	protected abstract String getAttributPfadVon(
			final String attributInCSVDatei, final int index);

	/**
	 * Erfragt die Parameter-Atg.
	 *
	 * @return die Parameter-Atg
	 */
	protected abstract AttributeGroup getParameterAtg();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dataRequest(final SystemObject object,
			final DataDescription dataDescription, final byte state) {
		// keine Überprüfung
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isRequestSupported(final SystemObject object,
			final DataDescription dataDescription) {
		return false;
	}

}
