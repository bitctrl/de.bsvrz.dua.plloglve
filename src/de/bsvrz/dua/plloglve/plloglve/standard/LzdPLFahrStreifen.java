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

package de.bsvrz.dua.plloglve.plloglve.standard;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.guete.GWert;
import de.bsvrz.dua.guete.GueteException;
import de.bsvrz.dua.guete.GueteVerfahren;
import de.bsvrz.dua.plloglve.plloglve.typen.OptionenPlausibilitaetsPruefungLogischVerkehr;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.GanzZahl;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;

/**
 * Klasse zum Durchführen der speziellen Standardplausibilisierung LVE
 * für KZD. Diese Klasse macht nichts weiter, als sich auf die Grenzwertparameter
 * anzumelden und einige Funktionen zur Plausibilisierung von KZD zur Verfügung zu stellen
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class LzdPLFahrStreifen 
extends KzdPLFahrStreifen{
	
	/**
	 * Alle Attribute, die innerhalb der PL-Prüfung logisch bzwl eines KZD veraendert werden
	 * koennen
	 */
	private static final String[] ATTRIBUT_NAMEN = {"qKfz", //$NON-NLS-1$
		"qLkw", //$NON-NLS-1$
		"qPkw", //$NON-NLS-1$
		"vPkw", //$NON-NLS-1$
		"vLkw", //$NON-NLS-1$
		"vKfz", //$NON-NLS-1$
		"sKfz"}; //$NON-NLS-1$
	
	/**
	 * Standartdkonstruktor 
	 * 
	 * @param verwaltung Verbindung zum Verwaltungsmodul
	 * @param obj das mit dem Fahrstreifen assoziierte Systemobjekt
	 */
	protected LzdPLFahrStreifen(final IVerwaltungMitGuete verwaltung, final SystemObject obj){
		super(verwaltung, obj);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void ueberpruefe(Data data, ResultData resultat) {
		if(this.parameterAtgLog != null){	
			synchronized (this.parameterAtgLog) {				
				/**
				 * Regel Nr.12 (aus SE-02.00.00.00.00-AFo-4.0, S.95)
				 */
				this.untersucheAufMaxVerletzung(data, resultat, "qKfz", this.parameterAtgLog.getQKfzBereichMax()); //$NON-NLS-1$

				/**
				 * Regel Nr.14 (aus SE-02.00.00.00.00-AFo-4.0, S.95)
				 */
				this.untersucheAufMaxVerletzung(data, resultat, "qLkw", this.parameterAtgLog.getQLkwBereichMax()); //$NON-NLS-1$
			}
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AttributeGroup getPlausibilisierungsParameterAtg(ClientDavInterface dav) {
		return dav.getDataModel().getAttributeGroup(AtgVerkehrsDatenLZIPlPruefLogisch.getPid());
	}
	
	
	/**
	 * Setzt im uebergebenen Datensatz alle Werte
	 * auf implausibel und fehlerhaft 
	 * 
	 * @param veraenderbaresDatum ein veraenderbarer LVE-Datensatz (muss <code>!= null</code> sein)
	 */
	protected void setAllesImplausibel(Data veraenderbaresDatum){
		veraenderbaresDatum.getItem("qKfz").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
		veraenderbaresDatum.getItem("qLkw").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
		veraenderbaresDatum.getItem("qPkw").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

		veraenderbaresDatum.getItem("vKfz").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
		veraenderbaresDatum.getItem("vLkw").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
		veraenderbaresDatum.getItem("vPkw").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

		veraenderbaresDatum.getItem("qKfz").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		veraenderbaresDatum.getItem("qLkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		veraenderbaresDatum.getItem("qPkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		
		veraenderbaresDatum.getItem("vKfz").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		veraenderbaresDatum.getItem("vLkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		veraenderbaresDatum.getItem("vPkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
				
		veraenderbaresDatum.getItem("sKfz").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

		veraenderbaresDatum.getItem("sKfz").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean untersucheAufMaxVerletzung(Data davDatum,
			ResultData resultat, String wertName, long max) {		
		if(this.parameterAtgLog != null){
			
			OptionenPlausibilitaetsPruefungLogischVerkehr optionen = this.parameterAtgLog.getOptionen();

			if(!optionen.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.KEINE_PRUEFUNG)){
				final long wert = davDatum.getItem(wertName).getUnscaledValue("Wert").longValue(); //$NON-NLS-1$

				GanzZahl sweGueteWert = GanzZahl.getGueteIndex();
				sweGueteWert.setSkaliertenWert(VERWALTUNG.getGueteFaktor());
				GWert sweGuete = new GWert(sweGueteWert, GueteVerfahren.STANDARD, false);

				/**
				 * sonst handelt es sich nicht um einen Messwert
				 */
				if(wert >= 0 && max >= 0){
					boolean maxVerletzt = wert > max;
		
					if(maxVerletzt){
						if(optionen.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MAX) || 
						   optionen.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MIN_MAX)){
							davDatum.getItem(wertName).getUnscaledValue("Wert").set(max); //$NON-NLS-1$
							davDatum.getItem(wertName).getItem("Status"). //$NON-NLS-1$
								getItem("PlLogisch").getUnscaledValue("WertMaxLogisch").set(DUAKonstanten.JA); //$NON-NLS-1$ //$NON-NLS-2$
							
							GWert guete = new GWert(davDatum, wertName);
							GWert neueGuete = GWert.getNichtErmittelbareGuete(guete.getVerfahren());
							try {
								neueGuete = GueteVerfahren.produkt(guete, sweGuete);
							} catch (GueteException e1) {
								LOGGER.error("Guete von " + wertName + " konnte nicht aktualisiert werden in " + resultat); //$NON-NLS-1$ //$NON-NLS-2$
								e1.printStackTrace();
							}							
							davDatum.getItem(wertName).getItem("Güte").//$NON-NLS-1$
								getUnscaledValue("Index").set(neueGuete.getIndexUnskaliert());  //$NON-NLS-1$
						}else
						if(optionen.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.NUR_PRUEFUNG)){							
							if(wertName.equals("vKfz") || wertName.startsWith("qKfz") ||  //$NON-NLS-1$ //$NON-NLS-2$
							   wertName.equals("vLkw") || wertName.startsWith("qLkw") ||  //$NON-NLS-1$ //$NON-NLS-2$
							   wertName.equals("vPkw") || wertName.startsWith("qPkw")){  //$NON-NLS-1$ //$NON-NLS-2$
								davDatum.getItem(wertName).getItem("Status"). //$NON-NLS-1$
								getItem("PlLogisch").getUnscaledValue("WertMaxLogisch").set(DUAKonstanten.JA); //$NON-NLS-1$ //$NON-NLS-2$
							}
						}
					}					
				}
			}
		}
		
		return false;
	}


	/**
	 *  {@inheritDoc}
	 */
	@Override
	protected String[] getAttributNamen() {
		return ATTRIBUT_NAMEN;
	}
	
}
