/** 
 * Segment 4 Daten�bernahme und Aufbereitung (DUA), SWE 4.2 Pl-Pr�fung logisch LVE
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
 * Wei�enfelser Stra�e 67<br>
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

import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.plloglve.plloglve.PlPruefungLogischLVE;
import de.bsvrz.dua.plloglve.plloglve.TestParameter;
import de.bsvrz.sys.funclib.bitctrl.daf.Konstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;
import de.bsvrz.sys.funclib.bitctrl.modell.AbstractSystemObjekt;
import de.bsvrz.sys.funclib.bitctrl.modell.SystemObjekt;
import de.bsvrz.sys.funclib.bitctrl.modell.SystemObjektTyp;
import de.bsvrz.sys.funclib.operatingMessage.MessageGrade;
import de.bsvrz.sys.funclib.operatingMessage.MessageState;
import de.bsvrz.sys.funclib.operatingMessage.MessageType;

/**
 * Speichert die Ausfallh�ufigkeit eine Fahrstreifens �ber einem gleitenden Tag
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class AusfallFahrStreifen 
extends AbstractSystemObjekt
implements ClientReceiverInterface{
		
	/**
	 * Format der Zeitangabe innerhalb der Betriebsmeldung
	 */
	private static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm"); //$NON-NLS-1$
	
	/**
	 * IP der ATG KZD
	 */
	private static long ATG_KZD_ID = -1;
	
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
	 * <code>atg.verkehrsDatenAusfallH�ufigkeitFs</code>
	 */
	private static DataDescription AUSFALL_BESCHREIBUNG = null;
	
	/**
	 * Maximal zul�ssige Ausfallh�ufigkeit eines Fahrstreifens pro Tag
	 */
	private long maxAusfallProTag = -4;
	
	/**
	 * Datens�tze mit Ausfallinformationen der letzten 24h
	 */
	private Collection<AusfallDatum> gleitenderTag = 
				Collections.synchronizedCollection(new TreeSet<AusfallDatum>());

	
	/**
	 * Standardkonstruktor
	 * 
	 * @param verwaltung Verbindung zum Verwaltungsmodul
	 * @param obj das mit einem Fahrstreifen assoziierte Systemobjekt
	 */
	protected AusfallFahrStreifen(final IVerwaltung verwaltung, final SystemObject obj){
		super(obj);
		
		if(VERWALTUNG == null){
			VERWALTUNG = verwaltung;
			AUSFALL_BESCHREIBUNG = new DataDescription(
					VERWALTUNG.getVerbindung().getDataModel().getAttributeGroup("atg.verkehrsDatenAusfallH�ufigkeitFs"), //$NON-NLS-1$
					VERWALTUNG.getVerbindung().getDataModel().getAspect(Konstanten.ASP_PARAMETER_SOLL),
					(short)0);
			ATG_KZD_ID = VERWALTUNG.getVerbindung().getDataModel().getAttributeGroup(DUAKonstanten.ATG_KZD).getId();
		}
		
		VERWALTUNG.getVerbindung().subscribeReceiver(this, obj, AUSFALL_BESCHREIBUNG,
				ReceiveOptions.normal(), ReceiverRole.receiver());
	}
	
	
	/**
	 * F�hrt die Plausibilisierung durch. (nur f�r KZD)
	 * 
	 * @param resultat ein Fahrstreifendatum (KZD)
	 */
	protected final void plausibilisiere(final ResultData resultat){		
		if(resultat != null && resultat.getData() != null && 
				resultat.getDataDescription().getAttributeGroup().getId() == ATG_KZD_ID){

			AusfallDatum ausfallDatum = AusfallDatum.getAusfallDatumVon(resultat);
			if(ausfallDatum != null){
				synchronized (this.gleitenderTag) {
					if(ausfallDatum.isAusgefallen()){
						gleitenderTag.add(ausfallDatum);
					}
				}					
			}
			this.testAufAusfall();
		}			
	}
	
	
	/**
	 * Erreichnet den Ausfall dieses Fahrstreifens und gibt ggf. eine Betriebsmeldung aus 
	 **/
	private final void testAufAusfall(){
		Collection<AusfallDatum> veralteteDaten = new TreeSet<AusfallDatum>();
		long ausfallZeit = 0;
		
		synchronized (this.gleitenderTag) {
			for(AusfallDatum ausfallDatum:this.gleitenderTag){
				if(ausfallDatum.isDatumVeraltet()){
					veralteteDaten.add(ausfallDatum);
				}else{
					ausfallZeit += ausfallDatum.getIntervallLaenge();
				}				
			}			
			
			for(AusfallDatum veraltet:veralteteDaten){
				this.gleitenderTag.remove(veraltet);
			}			
		}
		
		if(programmLaeuftSchonLaengerAlsEinTag()){
			synchronized (this) {
				if(this.maxAusfallProTag >= 0){
					
					double ausfallInProzent;
					if(TestParameter.TEST_AUSFALL) {
						ausfallInProzent = (((double)ausfallZeit / (double)144000) * 100.0);
					} else {
						ausfallInProzent = (((double)ausfallZeit / (double)Konstante.TAG_24_IN_MS) * 100.0);
					}
					
					if(ausfallInProzent > this.maxAusfallProTag){
						long stunden = ausfallZeit / Konstante.STUNDE_IN_MS;
						long minuten = (ausfallZeit - (stunden * Konstante.STUNDE_IN_MS)) / Konstante.MINUTE_IN_MS;

						String nachricht = "Ausfallh�ufigkeit innerhalb der letzten 24 Stunden �berschritten. Im Zeitraum von " +  //$NON-NLS-1$
						FORMAT.format(new Date(System.currentTimeMillis() - Konstante.TAG_24_IN_MS)) + " Uhr bis " +  //$NON-NLS-1$
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
	
	
	/**
	 * {@inheritDoc}
	 */
	public void update(ResultData[] davParameterFeld) {
		if(davParameterFeld != null){
			for(ResultData davParameter:davParameterFeld){
				if(davParameter != null && davParameter.getData() != null){
					synchronized (this) {
						this.maxAusfallProTag = davParameter.getData().getUnscaledValue(
												"maxAusfallProTag").longValue(); //$NON-NLS-1$
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
	

	/**
	 * Erfragt, ob diese Applikation (eigentlich das Modul Pl-Pr�fung logisch LVE)
	 * schon l�nger als einen Tag l�uft (erst dann solltes Objekt Daten markieren
	 * bzw. Betriebsmeldungen ausgeben)
	 * 
	 * @return ob diese Applikation schon l�nger als einen Tag l�uft
	 */
	private static final boolean programmLaeuftSchonLaengerAlsEinTag(){
		if(TestParameter.TEST_AUSFALL){
			return PlPruefungLogischLVE.START_ZEIT + 144000l < System.currentTimeMillis();
		}
		return PlPruefungLogischLVE.START_ZEIT + Konstante.TAG_24_IN_MS < System.currentTimeMillis(); 
	}	

}