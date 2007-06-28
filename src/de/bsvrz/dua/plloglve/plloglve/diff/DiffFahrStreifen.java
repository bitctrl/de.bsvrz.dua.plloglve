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

package de.bsvrz.dua.plloglve.plloglve.diff;

import java.util.ArrayList;
import java.util.Collection;

import stauma.dav.clientside.ClientReceiverInterface;
import stauma.dav.clientside.Data;
import stauma.dav.clientside.DataDescription;
import stauma.dav.clientside.ReceiveOptions;
import stauma.dav.clientside.ReceiverRole;
import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.SystemObject;
import sys.funclib.debug.Debug;
import sys.funclib.operatingMessage.MessageGrade;
import sys.funclib.operatingMessage.MessageState;
import sys.funclib.operatingMessage.MessageType;
import de.bsvrz.dua.plloglve.plloglve.PLLOGKonstanten;
import de.bsvrz.sys.funclib.bitctrl.daf.Konstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;
import de.bsvrz.sys.funclib.bitctrl.modell.AbstractSystemObjekt;
import de.bsvrz.sys.funclib.bitctrl.modell.SystemObjekt;
import de.bsvrz.sys.funclib.bitctrl.modell.SystemObjektTyp;

/**
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class DiffFahrStreifen 
extends AbstractSystemObjekt
implements Comparable<DiffFahrStreifen>, ClientReceiverInterface{
	
	/**
	 * Debug-Logger
	 */
	private static final Debug LOGGER = Debug.getLogger();
	
	/**
	 * Standard-Betriebsmeldungs-ID
	 */
	private static final String MELDUNGS_ID = "Differenzialkontrolle"; //$NON-NLS-1$
	
	/**
	 * Verbindung zum Verwaltungsmodul
	 */
	private static IVerwaltung VERWALTUNG = null;
	
	/**
	 * 
	 */
	private static DataDescription DIFF_PARA_BESCHREIBUNG = null;
	
	/**
	 * 
	 */
	private AtgVerkehrsDatenDifferenzialKontrolleFs parameter = null;
	
	/**
	 * 
	 */
	private WertMitKonstanzZaehler qKfzZaehler = 
								new WertMitKonstanzZaehler("qKfz");  //$NON-NLS-1$

	/**
	 * 
	 */
	private WertMitKonstanzZaehler qLkwZaehler = 
								new WertMitKonstanzZaehler("qLkw");  //$NON-NLS-1$

	/**
	 * 
	 */
	private WertMitKonstanzZaehler qPkwZaehler = 
								new WertMitKonstanzZaehler("qPkw");  //$NON-NLS-1$
	
	/**
	 * 
	 */
	private WertMitKonstanzZaehler vKfzZaehler = 
								new WertMitKonstanzZaehler("vKfz");  //$NON-NLS-1$
	
	/**
	 * 
	 */
	private WertMitKonstanzZaehler vLkwZaehler = 
								new WertMitKonstanzZaehler("vLkw");  //$NON-NLS-1$

	/**
	 * 
	 */
	private WertMitKonstanzZaehler vPkwZaehler = 
								new WertMitKonstanzZaehler("vPkw");  //$NON-NLS-1$

	/**
	 * 
	 */
	private WertMitKonstanzZaehler sKfzZaehler = 
								new WertMitKonstanzZaehler("sKfz");  //$NON-NLS-1$

	/**
	 * 
	 */
	private WertMitKonstanzZaehler bZaehler = 
								new WertMitKonstanzZaehler("b");  //$NON-NLS-1$



	
	protected DiffFahrStreifen(final IVerwaltung verwaltung, final SystemObject obj){
		super(obj);
		
		if(VERWALTUNG == null){
			VERWALTUNG = verwaltung;
			DIFF_PARA_BESCHREIBUNG = new DataDescription(
					VERWALTUNG.getVerbindung().getDataModel().getAttributeGroup("atg.verkehrsDatenDifferenzialKontrolleFs"), //$NON-NLS-1$
					VERWALTUNG.getVerbindung().getDataModel().getAspect(Konstanten.ASP_PARAMETER_SOLL),
					(short)0);

		}
		
		VERWALTUNG.getVerbindung().subscribeReceiver(this, obj, DIFF_PARA_BESCHREIBUNG,
				ReceiveOptions.normal(), ReceiverRole.receiver());
	}
	
	
	protected Data plausibilisiere(final ResultData resultat){
		Data copy = null;
		
		if(resultat != null && resultat.getData() != null){
			if(resultat.getDataDescription().getAttributeGroup().getPid().
					equals(DUAKonstanten.ATG_KZD)){
				Data data = resultat.getData();
			
				if(this.parameter != null){
					final int qKfz = data.getItem("qKfz").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
					final int qLkw = data.getItem("qLkw").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
					final int qPkw = data.getItem("qPkw").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
					final int vPkw = data.getItem("vPkw").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
					final int vLkw = data.getItem("vLkw").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
					final int vKfz = data.getItem("vKfz").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
					final int b = data.getItem("b").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
					final int sKfz = data.getItem("sKfz").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
					
					this.qKfzZaehler.update(qKfz);
					this.qLkwZaehler.update(qLkw);
					this.qPkwZaehler.update(qPkw);
					this.vKfzZaehler.update(vKfz);
					this.vLkwZaehler.update(vLkw);
					this.vPkwZaehler.update(vPkw);
					this.sKfzZaehler.update(sKfz);
					this.bZaehler.update(b);
					
					Collection<WertMitKonstanzZaehler> puffer = new ArrayList<WertMitKonstanzZaehler>();
					synchronized (this.parameter) {
						if(this.qKfzZaehler.getWert() > 0 &&
								this.qKfzZaehler.getWertIstKonstantSeit() > this.parameter.getMaxAnzKonstanzqKfz()){
							puffer.add(this.qKfzZaehler);
						}
						if(this.qLkwZaehler.getWert() > 0 &&
								this.qLkwZaehler.getWertIstKonstantSeit() > this.parameter.getMaxAnzKonstanzqLkw()){
							puffer.add(this.qLkwZaehler);
						}
						if(this.qPkwZaehler.getWert() > 0 &&
								this.qPkwZaehler.getWertIstKonstantSeit() > this.parameter.getMaxAnzKonstanzqPkw()){
							puffer.add(this.qPkwZaehler);
						}
						
						if(this.vKfzZaehler.getWert() > 0 &&
								this.vKfzZaehler.getWertIstKonstantSeit() > this.parameter.getMaxAnzKonstanzvKfz()){
							puffer.add(this.vKfzZaehler);
						}
						if(this.vLkwZaehler.getWert() > 0 &&
								this.vLkwZaehler.getWertIstKonstantSeit() > this.parameter.getMaxAnzKonstanzvLkw()){
							puffer.add(this.vLkwZaehler);
						}
						if(this.vPkwZaehler.getWert() > 0 &&
								this.vPkwZaehler.getWertIstKonstantSeit() > this.parameter.getMaxAnzKonstanzvPkw()){
							puffer.add(this.vPkwZaehler);
						}
						
						if(this.sKfzZaehler.getWert() > 0 &&
								this.sKfzZaehler.getWertIstKonstantSeit() > this.parameter.getMaxAnzKonstanzStreung()){
							puffer.add(this.sKfzZaehler);
						}
						if(this.bZaehler.getWert() > 0 &&
								this.bZaehler.getWertIstKonstantSeit() > this.parameter.getMaxAnzKonstanzBelegung()){
							puffer.add(this.bZaehler);
						}

						if(!puffer.isEmpty()){
							copy = VERWALTUNG.getVerbindung().createData(resultat.getDataDescription().getAttributeGroup());
							for(WertMitKonstanzZaehler wert:puffer){
								data.getItem(wert.getName()).getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$			
								data.getItem(wert.getName()).getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$
								getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$	
								VERWALTUNG.sendeBetriebsMeldung(MELDUNGS_ID, MessageType.APPLICATION_DOMAIN, Konstante.LEERSTRING,
										MessageGrade.WARNING, MessageState.MESSAGE, "Fahrstreifen " +  //$NON-NLS-1$
										this + ": " + wert); //$NON-NLS-1$
							}
						}
					}
				}else{
					LOGGER.warning("Fuer Fahrstreifen " + this +  //$NON-NLS-1$
							" wurden noch keine Parameter für die Differenzialkontrolle empfangen"); //$NON-NLS-1$
				}
			}			
		}
		
		return copy;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void update(ResultData[] davParameterFeld) {
		if(davParameterFeld != null){
			for(ResultData davParameter:davParameterFeld){
				if(davParameter != null && davParameter.getData() != null){
					synchronized (this) {
						this.parameter = new AtgVerkehrsDatenDifferenzialKontrolleFs(davParameter.getData());
					}
				}
			}
		}
	}

	
	/**
	 * {@inheritDoc}
	 */
	public SystemObjektTyp getTyp() {
		return new SystemObjektTyp(){

			public Class<? extends SystemObjekt> getKlasse() {
				return DiffFahrStreifen.class;
			}

			public String getPid() {
				return getSystemObject().getType().getPid();
			}
			
		};
	}

	
	/**
	 * {@inheritDoc}
	 */
	public int compareTo(DiffFahrStreifen that) {
		return new Long(this.getId()).compareTo(that.getId());
	}

}
