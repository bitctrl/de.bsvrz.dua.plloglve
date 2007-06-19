/** 
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.2 Plausibilitätsprüfung logisch LVE
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
import de.bsvrz.dua.plloglve.plloglve.PLLOGKonstanten;
import de.bsvrz.dua.plloglve.plloglve.typen.OptionenPlausibilitaetsPruefungLogischVerkehr;
import de.bsvrz.sys.funclib.bitctrl.daf.Konstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.modell.AbstractSystemObjekt;
import de.bsvrz.sys.funclib.bitctrl.modell.SystemObjekt;
import de.bsvrz.sys.funclib.bitctrl.modell.SystemObjektTyp;

/**
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public abstract class AbstraktPLFahrStreifen
extends AbstractSystemObjekt
implements Comparable<AbstraktPLFahrStreifen>, ClientReceiverInterface{
	
	/**
	 * Debug-Logger
	 */
	protected static final Debug LOGGER = Debug.getLogger();
	
	/**
	 * Verbindung zum Verwaltungsmodul
	 */
	protected static IVerwaltung VERWALTUNG = null;

	/**
	 * Schnittstelle zu den Parametern der Grenzwertprüfung 
	 */
	protected AbstraktAtgPLLogischLVEParameter parameterAtgLog = null;
			

	/**
	 * Standartdkonstruktor 
	 * 
	 * @param obj das mit dem Fahrstreifen assoziierte Systemobjekt
	 */
	protected AbstraktPLFahrStreifen(final SystemObject obj)
	throws Exception{
		super(obj);
		
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

	
	protected Data berechneQPkw(Data data){
		/**
		 * TODO: Güte
		 */
		final int qKfz = data.getItem("qKfz").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final int qLkw = data.getItem("qLkw").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		int qPkw = PLLOGKonstanten.NICHT_ERMITTELBAR;
		if(qKfz >= 0 && qLkw >= 0){
			qPkw = qKfz - qLkw;
		}		
		data.getItem("qPkw").getUnscaledValue("Wert").set(qPkw); //$NON-NLS-1$ //$NON-NLS-2$
		
		final int vPkw = data.getItem("vPkw").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final int vLkw = data.getItem("vLkw").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		int vKfz = PLLOGKonstanten.NICHT_ERMITTELBAR;
		if(qKfz > 0 && qPkw >= 0 && vPkw >= 0 && qLkw >= 0 && vLkw >= 0){
			vKfz = (qPkw * vPkw + qLkw * vLkw) / qKfz;
		}
		data.getItem("vKfz").getUnscaledValue("Wert").set(vKfz);  //$NON-NLS-1$//$NON-NLS-2$
		
		return data;
	}
		
	
	protected Data untersucheWerteBereich(Data davDatum, final String wertName, final int min, final int max){
		if(davDatum != null && this.parameterAtgLog != null){
			OptionenPlausibilitaetsPruefungLogischVerkehr optionen = this.parameterAtgLog.getOptionen();

			if(!optionen.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.KEINE_PRUEFUNG)){
				int wert = davDatum.getItem(wertName).getUnscaledValue("Wert").intValue(); //$NON-NLS-1$
				
				/**
				 * sonst handelt es sich nicht um einen Messwert
				 */
				if(wert >= 0){
					boolean minVerletzt = wert < min;
					boolean maxVerletzt = wert > max;
		
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
						}					
					}else if(optionen.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MIN)){
						if(minVerletzt){
							DUAUtensilien.getAttributDatum(wertName + ".Wert", davDatum). //$NON-NLS-1$
								asUnscaledValue().set(min);
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
					}
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
	 * {@inheritDoc}
	 */
	public int compareTo(AbstraktPLFahrStreifen that) {
		return new Long(this.getId()).compareTo(that.getId());
	}

	
	/**
	 * Erfragt das Systemobjekt der Attributgruppe, unter der die
	 * Parameter für die Intervallgrenzwerte stehen
	 * 
	 * @param dav die Datenverteiler-Verbindung
	 * @return die Parameter-Attributgruppe
	 */
	protected abstract AttributeGroup getPlausibilisierungsParameterAtg(final ClientDavInterface dav);


	protected abstract Data plausibilisiere(final ResultData resultat);

	protected abstract void ueberpruefeKontextFehler(Data data, final ResultData resultat);
	
	protected abstract void grenzWertTests(Data data);

}
