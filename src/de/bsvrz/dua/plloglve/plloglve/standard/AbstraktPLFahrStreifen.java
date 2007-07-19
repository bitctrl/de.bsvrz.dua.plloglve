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

import stauma.dav.clientside.ClientDavInterface;
import stauma.dav.clientside.ClientReceiverInterface;
import stauma.dav.clientside.Data;
import stauma.dav.clientside.DataDescription;
import stauma.dav.clientside.ReceiveOptions;
import stauma.dav.clientside.ReceiverRole;
import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.AttributeGroup;
import stauma.dav.configuration.interfaces.SystemObject;
import sys.funclib.debug.Debug;
import de.bsvrz.dua.guete.GueteException;
import de.bsvrz.dua.guete.GueteUtil;
import de.bsvrz.dua.guete.GueteVerfahren;
import de.bsvrz.dua.guete.IGuete;
import de.bsvrz.dua.plloglve.plloglve.typen.OptionenPlausibilitaetsPruefungLogischVerkehr;
import de.bsvrz.sys.funclib.bitctrl.daf.Konstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.bitctrl.modell.AbstractSystemObjekt;
import de.bsvrz.sys.funclib.bitctrl.modell.SystemObjekt;
import de.bsvrz.sys.funclib.bitctrl.modell.SystemObjektTyp;

/**
 * Abstrakte Klasse zum Ablegen von Informationen der Standardplausibilisierung LVE
 * für LZD und KZD. Macht nichts weiter, als sich auf die Grenzwertparameter anzumeldnen
 * und einige Funktionen zur Plausibilisierung zur Verfügung zu stellen
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public abstract class AbstraktPLFahrStreifen
extends AbstractSystemObjekt
implements ClientReceiverInterface{
	
	/**
	 * Debug-Logger
	 */
	protected static final Debug LOGGER = Debug.getLogger();
	
	/**
	 * Standard-Verfahren der Gueteberechnung
	 */
	protected static final IGuete G = GueteVerfahren.STANDARD.getBerechnungsVorschrift();
	
	/**
	 * Verbindung zum Verwaltungsmodul mit Guetefaktor
	 */
	protected static IVerwaltungMitGuete VERWALTUNG = null;

	/**
	 * Schnittstelle zu den Parametern der Grenzwertprüfung 
	 */
	protected AbstraktAtgPLLogischLVEParameter parameterAtgLog = null;	
			

	/**
	 * Standartdkonstruktor 
	 * 
	 * @param verwaltung Verbindung zum Verwaltungsmodul
	 * @param obj das mit dem Fahrstreifen assoziierte Systemobjekt
	 */
	protected AbstraktPLFahrStreifen(final IVerwaltungMitGuete verwaltung, final SystemObject obj){
		super(obj);
		
		if(VERWALTUNG == null){
			VERWALTUNG = verwaltung;
		}
		
		DataDescription logParaDesc = new DataDescription(
				this.getPlausibilisierungsParameterAtg(VERWALTUNG.getVerbindung()),
				VERWALTUNG.getVerbindung().getDataModel().getAspect(Konstanten.ASP_PARAMETER_SOLL),
				(short)0);

		VERWALTUNG.getVerbindung().subscribeReceiver(this, obj, logParaDesc,
				ReceiveOptions.normal(), ReceiverRole.receiver());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void update(ResultData[] parameterFeld) {
		if(parameterFeld != null){
			for(ResultData parameter:parameterFeld){
				if(parameter != null && parameter.getData() != null){
					if(parameter.getDataDescription().getAttributeGroup().equals(
							this.getPlausibilisierungsParameterAtg(VERWALTUNG.getVerbindung()))){
						synchronized (this) {
							this.parameterAtgLog = AbstraktAtgPLLogischLVEParameter.getInstance(parameter);
						}
					}
				}
			}
		}
	}

	
	/**
	 * Berechnet aus qKfz und qLkw qPkw und aus vPkw und vLkw vKfz.
	 *  
	 * @param data ein KZD (darf nicht <code>null</code> sein)
	 * @return das um qPkw und vKfz erweiterte KZD
	 */
	protected Data berechneQPkw(Data data){
		final int qKfz = data.getItem("qKfz").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final int qLkw = data.getItem("qLkw").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		int qPkw = DUAKonstanten.NICHT_ERMITTELBAR;
		double qPkwGuete = -1;
		if(qKfz >= 0 && qLkw >= 0){
			qPkw = qKfz - qLkw;

			try {
				qPkwGuete = GueteVerfahren.getDVonData(data.getItem("qKfz").getItem("Güte"),  //$NON-NLS-1$ //$NON-NLS-2$
											data.getItem("qLkw").getItem("Güte"));  //$NON-NLS-1$ //$NON-NLS-2$
			} catch (GueteException e) {
				e.printStackTrace();
				LOGGER.error("Berechnung der Guete von qPkw fehlgeschlagen", e); //$NON-NLS-1$
			}
		}
		
		if(DUAUtensilien.isWertInWerteBereich(data.getItem("qPkw").getItem("Wert"), qPkw)){ //$NON-NLS-1$ //$NON-NLS-2$
			data.getItem("qPkw").getUnscaledValue("Wert").set(qPkw); //$NON-NLS-1$ //$NON-NLS-2$	
			if(qPkwGuete >= 0.0){
				data.getItem("qPkw").getItem("Güte").getScaledValue("Index").set(qPkwGuete); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}else{
			data.getItem("qPkw").getUnscaledValue("Wert").set(DUAKonstanten.NICHT_ERMITTELBAR_BZW_FEHLERHAFT);  //$NON-NLS-1$//$NON-NLS-2$
		}
		
		final int vPkw = data.getItem("vPkw").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final int vLkw = data.getItem("vLkw").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		long vKfz = DUAKonstanten.NICHT_ERMITTELBAR;
		double vKfzGuete = -1;
		if(qKfz > 0 && qPkw >= 0 && vPkw >= 0 && qLkw >= 0 && vLkw >= 0){
			vKfz = (long)((qPkw * vPkw + qLkw * vLkw) / qKfz + 0.5);

			try {
				double qPkwG = GueteUtil.getGueteIndex(data, "qPkw"); //$NON-NLS-1$
				double vPkwG = GueteUtil.getGueteIndex(data, "vPkw"); //$NON-NLS-1$
				double qLkwG = GueteUtil.getGueteIndex(data, "qLkw"); //$NON-NLS-1$
				double vLkwG = GueteUtil.getGueteIndex(data, "vLkw"); //$NON-NLS-1$
				double qKfzG = GueteUtil.getGueteIndex(data, "qKfz"); //$NON-NLS-1$
				
				vKfzGuete = G.q(
								G.s(
									G.p(qPkwG, vPkwG),
									G.p(qLkwG, vLkwG)
								),
								qKfzG
							);
				
			} catch (GueteException e) {
				e.printStackTrace();
				LOGGER.error("Berechnung der Guete von vKfz fehlgeschlagen", e); //$NON-NLS-1$
			}
		}
		
		if(DUAUtensilien.isWertInWerteBereich(data.getItem("vKfz").getItem("Wert"), vKfz)){ //$NON-NLS-1$ //$NON-NLS-2$
			data.getItem("vKfz").getUnscaledValue("Wert").set(vKfz); //$NON-NLS-1$ //$NON-NLS-2$
			if(vKfzGuete >= 0.0){
				data.getItem("vKfz").getItem("Güte").getScaledValue("Index").set(vKfzGuete); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}else{
			data.getItem("vKfz").getUnscaledValue("Wert").set(DUAKonstanten.NICHT_ERMITTELBAR_BZW_FEHLERHAFT);  //$NON-NLS-1$//$NON-NLS-2$
		}
		
		DUAUtensilien.getAttributDatum("qPkw.Status.Erfassung.NichtErfasst", data).asUnscaledValue().set(DUAKonstanten.JA); //$NON-NLS-1$
		DUAUtensilien.getAttributDatum("vKfz.Status.Erfassung.NichtErfasst", data).asUnscaledValue().set(DUAKonstanten.JA); //$NON-NLS-1$
		
		return data;
	}
		
	
	/**
	 * Untersucht den Wertebereich eines Verkehrs-Datums und markiert ggf. verletzte Wertebereiche
	 * 
	 * @param davDatum ein Verkehrs-Datums (darf nicht <code>null</code> sein)
	 * @param wertName der Name des final Attributs 
	 * @param min untere Grenze des Wertes
	 * @param max obere Grenze des Wertes
	 * @return das plaubilisierte (markierte) Datum 
	 */
	protected Data untersucheWerteBereich(Data davDatum, final String wertName, final int min, final int max){
		if(this.parameterAtgLog != null){
			OptionenPlausibilitaetsPruefungLogischVerkehr optionen = this.parameterAtgLog.getOptionen();

			if(!optionen.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.KEINE_PRUEFUNG)){
				int wert = davDatum.getItem(wertName).getUnscaledValue("Wert").intValue(); //$NON-NLS-1$
				
				/**
				 * sonst handelt es sich nicht um einen Messwert
				 */
				if(wert >= 0){
					boolean minVerletzt = wert < min;
					boolean maxVerletzt = wert > max;
					boolean gueteNeuBerechnen = false;
		
					if(minVerletzt){
						DUAUtensilien.getAttributDatum(wertName + ".Status.PlLogisch.WertMinLogisch", davDatum). //$NON-NLS-1$
									asUnscaledValue().set(DUAKonstanten.JA);
					}
					if(maxVerletzt){
						DUAUtensilien.getAttributDatum(wertName + ".Status.PlLogisch.WertMaxLogisch", davDatum). //$NON-NLS-1$
									asUnscaledValue().set(DUAKonstanten.JA);						
					}
						
					if(optionen.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MAX)){
						if(maxVerletzt){
							DUAUtensilien.getAttributDatum(wertName + ".Wert", davDatum). //$NON-NLS-1$
								asUnscaledValue().set(max);
							gueteNeuBerechnen = true;
						}					
					}else if(optionen.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MIN)){
						if(minVerletzt){
							DUAUtensilien.getAttributDatum(wertName + ".Wert", davDatum). //$NON-NLS-1$
								asUnscaledValue().set(min);
							gueteNeuBerechnen = true;
						}															
					}else if(optionen.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MIN_MAX)){
						if(maxVerletzt){
							DUAUtensilien.getAttributDatum(wertName + ".Wert", davDatum). //$NON-NLS-1$
								asUnscaledValue().set(max);
						}else					
						if(minVerletzt){
							DUAUtensilien.getAttributDatum(wertName + ".Wert", davDatum). //$NON-NLS-1$
								asUnscaledValue().set(min);
						}
						gueteNeuBerechnen = true;
					}
					
//					if(gueteNeuBerechnen){
//						double guete = DUAUtensilien.getAttributDatum(wertName + "Güte.Index", davDatum). //$NON-NLS-1$
//												asScaledValue().doubleValue();
//						guete *= VERWALTUNG.getGueteFaktor();
//						DUAUtensilien.getAttributDatum(wertName + "Güte.Index", davDatum). //$NON-NLS-1$
//												asScaledValue().set(guete);
//					}
				}
			}
		}
		
		return davDatum;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public SystemObjektTyp getTyp() {
		return new SystemObjektTyp(){

			public Class<? extends SystemObjekt> getKlasse() {
				return KzdPLFahrStreifen.class;
			}

			public String getPid() {
				return getSystemObject().getType().getPid();
			}
			
		};
	}
	
	
	/**
	 * Erfragt das Systemobjekt der Attributgruppe, unter der die
	 * Parameter für die Intervallgrenzwerte stehen
	 * 
	 * @param dav die Datenverteiler-Verbindung
	 * @return die Parameter-Attributgruppe
	 */
	protected abstract AttributeGroup getPlausibilisierungsParameterAtg(final ClientDavInterface dav);


	/**
	 * Plausibilisiert ein übergebenes Datum
	 * 
	 * @param resultat ein Originaldatum
	 * @return das veränderte Datum oder <code>null</code>, wenn keine Veränderungen
	 * vorgenommen werden mussten
	 */
	protected abstract Data plausibilisiere(final ResultData resultat);

	
	/**
	 * Führt eine
	 * 
	 * @param data
	 * @param resultat der Original-Datensatz
	 */
	protected abstract void ueberpruefeKontextFehler(Data data, final ResultData resultat);
	
	
	/**
	 * Testet das übergebene Datum darauf, ob es sich im parametrierten Intervall befindet
	 * 
	 * @param data ein FG1-Datum (dieses kann verändert werden)
	 */
	protected abstract void grenzWertTests(Data data);

}
