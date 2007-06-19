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

package de.bsvrz.dua.plloglve.plloglve.ausfall;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.TreeSet;

import stauma.dav.clientside.ClientReceiverInterface;
import stauma.dav.clientside.DataDescription;
import stauma.dav.clientside.ReceiveOptions;
import stauma.dav.clientside.ReceiverRole;
import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.SystemObject;
import sys.funclib.operatingMessage.MessageGrade;
import sys.funclib.operatingMessage.MessageState;
import sys.funclib.operatingMessage.MessageType;
import de.bsvrz.dua.plloglve.plloglve.PLLOGKonstanten;
import de.bsvrz.sys.funclib.bitctrl.daf.Konstanten;
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
public class AusfallFahrStreifen 
extends AbstractSystemObjekt
implements ClientReceiverInterface{
	
	/**
	 * Zeitpunkt, zu dem dieses Submodul gestartet wurde
	 */
	private static final long START_ZEIT = System.currentTimeMillis();
	
	/**
	 * Format der Zeitangabe innerhalb der Betriebsmeldung
	 */
	private static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm"); //$NON-NLS-1$
	
	/**
	 * Standard-Betriebsmeldungs-ID dieses Submoduls
	 */
	private static final String MELDUNGS_ID = "Ausfallhaeufigkeit"; //$NON-NLS-1$
	
	/**
	 * Verbindung zum Verwaltungsmodul
	 */
	private static IVerwaltung VERWALTUNG = null;

	/**
	 * Datenbeschreibung der Parameterattributgruppe
	 * <code>atg.verkehrsDatenAusfallHäufigkeitFs</code>
	 */
	private static DataDescription AUSFALL_BESCHREIBUNG = null;
	
	/**
	 * Maximal zulässige Ausfallhäufigkeit eines Fahrstreifens pro Tag
	 */
	private int maxAusfallProTag = -4;
	
	/**
	 * Datensätze mit Ausfallinformationen der letzten 24h
	 */
	private Collection<AusfallDatum> gleitenderTag = 
				Collections.synchronizedCollection(new TreeSet<AusfallDatum>());



	
	protected AusfallFahrStreifen(final IVerwaltung verwaltung, final SystemObject obj){
		super(obj);
		
		if(VERWALTUNG == null){
			VERWALTUNG = verwaltung;
			AUSFALL_BESCHREIBUNG = new DataDescription(
					VERWALTUNG.getVerbindung().getDataModel().getAttributeGroup("atg.verkehrsDatenAusfallHäufigkeitFs"), //$NON-NLS-1$
					VERWALTUNG.getVerbindung().getDataModel().getAspect(Konstanten.ASP_PARAMETER_SOLL),
					(short)0);
		}
		
		VERWALTUNG.getVerbindung().subscribeReceiver(this, obj, AUSFALL_BESCHREIBUNG,
				ReceiveOptions.normal(), ReceiverRole.receiver());
	}
	
	
	protected final void plausibilisiere(final ResultData resultat){		
		if(resultat != null && resultat.getData() != null){
			if(resultat.getDataDescription().getAttributeGroup().getPid().
					equals(PLLOGKonstanten.ATG_KZD)){
				AusfallDatum ausfallDatum = AusfallDatum.getAusfallDatumVon(resultat);
				if(ausfallDatum != null){
					synchronized (this.gleitenderTag) {
						gleitenderTag.add(ausfallDatum);	
					}					
				}
				this.testAufAusfall();
			}			
		}
	}
	
	
	private final void testAufAusfall(){
		Collection<AusfallDatum> veralteteDaten = new TreeSet<AusfallDatum>();
		double datensaetzeAusfall = 0;
		double datensaetzeInsgesamt = 0;
		long ausfallZeit = 0;
		
		synchronized (this.gleitenderTag) {
			for(AusfallDatum ausfallDatum:this.gleitenderTag){
				if(ausfallDatum.isDatumVeraltet()){
					veralteteDaten.add(ausfallDatum);
				}else{
					datensaetzeInsgesamt++;
					if(ausfallDatum.isAusgefallen()){
						datensaetzeAusfall++;
						ausfallZeit += ausfallDatum.getIntervallLaenge();
					}
				}				
			}
			
			for(AusfallDatum veraltet:veralteteDaten){
				this.gleitenderTag.remove(veraltet);
			}			
		}
		
		if(programmLaeuftSchonLaengerAlsEinTag()){
			synchronized (this) {
				if(this.maxAusfallProTag >= 0){
					if(datensaetzeInsgesamt > 0){
						int ausfallInProzent = (int)((datensaetzeAusfall / datensaetzeInsgesamt * 100.0) + .5);
						if(ausfallInProzent > this.maxAusfallProTag){
							long stunden = ausfallZeit / 1000l / 60l / 60l;
							long minuten = (ausfallZeit - (stunden * 1000l / 60l / 60l)) / 1000l / 60l;
								
							String nachricht = "Ausfallhäufigkeit innerhalb der letzten 24 Stunden überschritten. Im Zeitraum von " +  //$NON-NLS-1$
								FORMAT.format(new Date(System.currentTimeMillis() - PLLOGKonstanten.EIN_TAG_IN_MS)) + " Uhr bis " +  //$NON-NLS-1$
								FORMAT.format(new Date(System.currentTimeMillis())) + " Uhr (1 Tag) implausible Fahrstreifenwerte am Fahrstreifen " + //$NON-NLS-1$
								this.getSystemObject() + " von " + ausfallInProzent + "% (> " + this.maxAusfallProTag +  //$NON-NLS-1$//$NON-NLS-2$
									"%) entspricht Ausfall von " + stunden + " Stunde(n) " + minuten + " Minute(n).";  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
							
							VERWALTUNG.sendeBetriebsMeldung(MELDUNGS_ID, MessageType.APPLICATION_DOMAIN, Konstante.LEERSTRING,
									MessageGrade.WARNING, MessageState.MESSAGE, nachricht);
						}
					}
				}				
			}
		}		
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void update(ResultData[] davParameterFeld) {
		if(davParameterFeld != null){
			for(ResultData davParameter:davParameterFeld){
				if(davParameter != null && davParameter.getData() != null){
					synchronized (this) {
						this.maxAusfallProTag = davParameter.getData().getUnscaledValue(
												"maxAusfallProTag").intValue(); //$NON-NLS-1$
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
				return AusfallFahrStreifen.class;
			}

			public String getPid() {
				return getSystemObject().getType().getPid();
			}
			
		};
	}
	

	private static final boolean programmLaeuftSchonLaengerAlsEinTag(){
		return START_ZEIT + PLLOGKonstanten.EIN_TAG_IN_MS < System.currentTimeMillis(); 
	}	

}