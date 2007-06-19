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

import stauma.dav.clientside.ResultData;
import sys.funclib.debug.Debug;
import de.bsvrz.dua.plloglve.plloglve.typen.OptionenPlausibilitaetsPruefungLogischVerkehr;
import de.bsvrz.sys.funclib.bitctrl.dua.AtgDatenObjekt;

/**
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class AbstraktAtgPLLogischLVEParameter 
extends AtgDatenObjekt{
	
	/**
	 * Debug-Logger
	 */
	protected static final Debug LOGGER = Debug.getLogger();

	/**
	 * Legt das Verhalten für den Umgang mit geprüften Werten nach der Wertebereichsprüfung fest. 
	 */
	protected OptionenPlausibilitaetsPruefungLogischVerkehr optionen;

	/**
	 * Minimum der erlaubten Verkehrsstärke (Anzahl der Fahrzeuge) als normierter Stundenwert.
	 */
	protected int qKfzBereichMin;
	
	/**
	 * Maximum der erlaubten Verkehrsstärke (Anzahl der Fahrzeuge) als normierter Stundenwert.
	 */
	protected int qKfzBereichMax;
	
	/**
	 * Minimum der erlaubten Verkehrsstärke (Anzahl der Fahrzeuge) als normierter Stundenwert.
	 */
	protected int qLkwBereichMin;  
	
	/**
	 * Maximum der erlaubten Verkehrsstärke (Anzahl der Fahrzeuge) als normierter Stundenwert.
	 */
	protected int qLkwBereichMax;  

	/**
	 * Grenzgeschwindigkeit für PL-Prüfung. Ist dieser Wert überschritten, 
	 * muss b kleiner bGrenz sein, sonst ist b inplausibel. 
	 */
	protected int vKfzGrenz;
	
	/**
	 * Ist vKfz größer als vKfzGrenz, so muss b kleiner bGrenz sein, sonst ist b inplausibel. 
	 */
	protected	int bGrenz;
		
	/**
	 * Minimum der erlaubten Verkehrsstärke (Anzahl der Fahrzeuge) als normierter Stundenwert.
	 */
	protected int qPkwBereichMin;
	
	/**
	 * Maximum der erlaubten Verkehrsstärke (Anzahl der Fahrzeuge) als normierter Stundenwert.
	 */
	protected int qPkwBereichMax;  
	
	/**
	 * Minimum der erlaubten Geschwindigkeit.
	 */
	protected int vKfzBereichMin;  
	
	/**
	 * Maximum der erlaubten Geschwindigkeit.
	 */
	protected int vKfzBereichMax;  
	
	/**
	 * Minimum der erlaubten Geschwindigkeit.
	 */
	protected int vLkwBereichMin;  
	
	/**
	 * Maximum der erlaubten Geschwindigkeit.
	 */
	protected int vLkwBereichMax;  
	
	/**
	 * Minimum der erlaubten Geschwindigkeit.
	 */
	protected int vPkwBereichMin;  
	
	/**
	 * Maximum der erlaubten Geschwindigkeit.
	 */
	protected int vPkwBereichMax;
	
	/**
	 * Minimum der erlaubten Geschwindigkeit.
	 */
	protected int vgKfzBereichMin;
	
	/**
	 * Maximum der erlaubten Geschwindigkeit.
	 */
	protected int vgKfzBereichMax;  
	
	/**
	 * Minimum des erlaubten Prozentwertes.
	 */
	protected int belegungBereichMin;

	/**
	 * Maximum des erlaubten Prozentwertes.
	 */
	protected int belegungBereichMax; 

	
	/**
	 * Erfragt eine Schnittstelle zu den Parametern der logischen Plausibilisierung
	 * 
	 * @return eine Schnittstelle zu den Parametern der logischen Plausibilisierung
	 */
	public static final AbstraktAtgPLLogischLVEParameter getInstance(final ResultData resultat){
		AbstraktAtgPLLogischLVEParameter dummy = null;
		
		if(resultat != null){
			if(resultat.getData() != null){
				if(resultat.getDataDescription().getAttributeGroup().getPid().equals(
						AtgVerkehrsDatenKZIPlPruefLogisch.getPid())){
					dummy = new AtgVerkehrsDatenKZIPlPruefLogisch(resultat.getData());
				}else
				if(resultat.getDataDescription().getAttributeGroup().getPid().equals(
						AtgVerkehrsDatenLZIPlPruefLogisch.getPid())){
					dummy = new AtgVerkehrsDatenLZIPlPruefLogisch(resultat.getData());
				}else{
					LOGGER.warning("Unbekannter Datensatz übergeben:\n" + //$NON-NLS-1$
							resultat.getDataDescription().getAttributeGroup().getPid());
				}
			}
		}
		
		return dummy; 
	}
	
	
	/**
	 * Erfragt qKfzBereichMax
	 * 
	 * @return qKfzBereichMax
	 */
	public final int getQKfzBereichMax(){
		return this.qKfzBereichMax;
	}


	/**
	 * Erfragt qKfzBereichMin
	 * 
	 * @return qKfzBereichMin
	 */
	public final int getQKfzBereichMin(){
		return this.qKfzBereichMin;
	}


	/**
	 * Erfragt qLkwBereichMax
	 * 
	 * @return qLkwBereichMax
	 */
	public final int getQLkwBereichMax(){
		return this.qLkwBereichMax;
	}


	/**
	 * Erfragt qLkwBereichMin
	 * 
	 * @return qLkwBereichMin
	 */
	public final int getQLkwBereichMin(){
		return this.qLkwBereichMin;
	}
	
	
	/**
	 * Erfragt die Optionen
	 * 
	 * @return optionen
	 */
	public final OptionenPlausibilitaetsPruefungLogischVerkehr getOptionen(){
		return this.optionen;
	}
	
	
	/**
	 * Erfragt BelegungBereichMax
	 * 
	 * @return belegungBereichMax
	 */
	public final int getBelegungBereichMax() {
		return belegungBereichMax;
	}


	/**
	 * Erfragt BelegungBereichMin
	 * 
	 * @return belegungBereichMin
	 */
	public final int getBelegungBereichMin() {
		return belegungBereichMin;
	}


	/**
	 * Erfragt bGrenz
	 * 
	 * @return bGrenz
	 */
	public final int getBGrenz() {
		return bGrenz;
	}


	/**
	 * Erfragt qPkwBereichMax
	 * 
	 * @return qPkwBereichMax
	 */
	public final int getQPkwBereichMax() {
		return qPkwBereichMax;
	}


	/**
	 * Erfragt qPkwBereichMin
	 * 
	 * @return qPkwBereichMin
	 */
	public final int getQPkwBereichMin() {
		return qPkwBereichMin;
	}


	/**
	 * Erfragt vgKfzBereichMax
	 * 
	 * @return vgKfzBereichMax
	 */
	public final int getVgKfzBereichMax() {
		return vgKfzBereichMax;
	}


	/**
	 * Erfragt vgKfzBereichMin
	 * 
	 * @return vgKfzBereichMin
	 */
	public final int getVgKfzBereichMin() {
		return vgKfzBereichMin;
	}


	/**
	 * Erfragt vKfzBereichMax
	 * 
	 * @return vKfzBereichMax
	 */
	public final int getVKfzBereichMax() {
		return vKfzBereichMax;
	}


	/**
	 * Erfragt vKfzBereichMin
	 * 
	 * @return vKfzBereichMin
	 */
	public final int getVKfzBereichMin() {
		return vKfzBereichMin;
	}


	/**
	 * Erfragt vKfzGrenz
	 * 
	 * @return vKfzGrenz
	 */
	public final int getVKfzGrenz() {
		return vKfzGrenz;
	}


	/**
	 * Erfragt vLkwBereichMax
	 * 
	 * @return vLkwBereichMax
	 */
	public final int getVLkwBereichMax() {
		return vLkwBereichMax;
	}


	/**
	 * Erfragt vLkwBereichMin
	 * 
	 * @return vLkwBereichMin
	 */
	public final int getVLkwBereichMin() {
		return vLkwBereichMin;
	}


	/**
	 * Erfragt vPkwBereichMax
	 * 
	 * @return vPkwBereichMax
	 */
	public final int getVPkwBereichMax() {
		return vPkwBereichMax;
	}


	/**
	 * Erfragt vPkwBereichMin
	 * 
	 * @return vPkwBereichMin
	 */
	public final int getVPkwBereichMin() {
		return vPkwBereichMin;
	}
	
}
