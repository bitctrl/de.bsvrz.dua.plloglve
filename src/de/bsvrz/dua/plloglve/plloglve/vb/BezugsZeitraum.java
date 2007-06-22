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

package de.bsvrz.dua.plloglve.plloglve.vb;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.TreeSet;

import stauma.dav.clientside.ResultData;
import sys.funclib.operatingMessage.MessageGrade;
import sys.funclib.operatingMessage.MessageState;
import sys.funclib.operatingMessage.MessageType;
import de.bsvrz.dua.plloglve.plloglve.PlPruefungLogischLVE;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;

/**
 * Speichert für einen Bezugszeitraum und ein finales
 * DAV-Attribut des Datensatzes <code>atg.verkehrsDatenKurzZeitIntervall</code>
 * (z.B. <code>qKfz</code>) die ausgefallenen Werte
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class BezugsZeitraum {
	
	/**
	 * Verbindung zum Verwaltungsmodul
	 */
	private static IVerwaltung VERWALTUNG = null;
	
	/**
	 * alle ausgefallenen Datensätze in diesem Bezugszeitraum
	 */
	private Collection<AusfallEinzelDatum> ausgefalleneDaten =
			Collections.synchronizedCollection(new TreeSet<AusfallEinzelDatum>());
	
	/**
	 * Name des finalen DAV-Attributs, für den Werte in diesem
	 * Bezugszeitraum gespeichert werden (z.B. <code>qKfz</code>)
	 */
	private String name = null;
		
	/**
	 * aktueller Zustand der Vertrauensbereichsverletzung für dieses Datum
	 */
	private boolean vertrauensBereichVerletzt = false;
	
	
	/**
	 * Standardkonstruktor
	 * 
	 * @param Verbindung zum Verwaltungsmodul
	 * @param name name Name des finalen DAV-Attributs, für den Werte in diesem
	 * Bezugszeitraum gespeichert werden sollen (z.B. <code>qKfz</code>)
	 */
	protected BezugsZeitraum(final IVerwaltung verwaltung, 
							 final String name){
		if(VERWALTUNG == null){
			VERWALTUNG = verwaltung;
		}
		this.name = name;
	}

	
	/**
	 * Erfragt, ob im Moment der Vertrauensbereich verletzt ist. Dies ist der Fall,
	 * wenn die Einschaltschwelle von einem Datum überschritten, und von allen späteren
	 * Daten (bis jetzt) die Ausschaltschwelle nicht unterschritten wurde 
	 * 
	 * @return ob im Moment der Vertrauensbereich verletzt ist
	 */
	protected final boolean isVertrauensBereichVerletzt(){
		return this.vertrauensBereichVerletzt;
	}
	
		
	/**
	 * Aktualisiert diese Datenstruktur mit einem aktuellen Kurzzeitdatum und
	 * den aktuellen Parametern. Errechnet anhand der übergebenen Parameter, ob
	 * der Vertrauensbereich (immernoch, nicht mehr) verletzt ist und gibt ggf.
	 * eine Betriebsmeldung aus
	 * 
	 * @param originalDatum ein KZD eines Fahrstreifens
	 * @param parameter die aktuellen Parameter des Vertrauensbereichs für diesen Fahrstreifen
	 * @return der aktuelle Ausfall dieses Attributs im Bezugszeitraum, oder <code>null</code>
	 * wenn dieser nicht ermittelt werden konnte
	 */
	protected final BezugsZeitraumAusfall ermittleAusfall(final ResultData originalDatum,
												 		  final AtgVerkehrsDatenVertrauensBereichFs parameter){
		BezugsZeitraumAusfall ausfall = new BezugsZeitraumAusfall(0, 0, 0, 0);
		
		AusfallEinzelDatum neuesAusfallEinzelDatum = 
			new AusfallEinzelDatum(this.name, originalDatum);

		if(neuesAusfallEinzelDatum.isAusgefallen()){
			synchronized (this.ausgefalleneDaten) {
				ausgefalleneDaten.add(neuesAusfallEinzelDatum);	
			}					
		}

		Collection<AusfallEinzelDatum> veralteteDaten = new TreeSet<AusfallEinzelDatum>();
		long ausfallZeit = 0;
		
		synchronized (this.ausgefalleneDaten) {
			for(AusfallEinzelDatum ausfallEinzelDatum:this.ausgefalleneDaten){
				if(ausfallEinzelDatum.isDatumVeraltet(parameter.getBezugsZeitraum())){
					veralteteDaten.add(ausfallEinzelDatum);
				}else{
					ausfallZeit += ausfallEinzelDatum.getIntervallLaenge();
				}				
			}
			
			this.ausgefalleneDaten.removeAll(veralteteDaten);
		}
		
		final long bezugsZeitraumInMillis = parameter.getBezugsZeitraum() * Konstante.STUNDE_IN_MS;
		int ausfallInProzent = 0;
		if(bezugsZeitraumInMillis > 0){
			ausfallInProzent = (int)((ausfallZeit / bezugsZeitraumInMillis) * 100.0 + 0.5);
		}
				
		/**
		 * Läuft das Programm schon länger als der Bezugszeitraum groß ist?
		 * Nur dann ist eine Vertrauensbereichsprüfung sinnvoll
		 */
		if(PlPruefungLogischLVE.START_ZEIT + bezugsZeitraumInMillis < System.currentTimeMillis()){
			synchronized (this) {
				boolean	einschaltSchwelleUEBERschritten = false;
				boolean	ausschaltSchwelleUNTERSchritten = false;
				
				if(ausfallInProzent > parameter.getMaxAusfallProBezugsZeitraumEin()){
					einschaltSchwelleUEBERschritten = true;
				}
				if(ausfallInProzent < parameter.getMaxAusfallProBezugsZeitraumAus()){
					ausschaltSchwelleUNTERSchritten = true;
				}
				
				if(einschaltSchwelleUEBERschritten){
					if(!this.vertrauensBereichVerletzt){
						Date start = new Date(originalDatum.getDataTime() - parameter.getBezugsZeitraum() * Konstante.STUNDE_IN_MS);
						Date ende = new Date(originalDatum.getDataTime());
						long stunden = ausfallZeit / Konstante.STUNDE_IN_MS;
						long minuten = (ausfallZeit - (stunden * Konstante.STUNDE_IN_MS)) / Konstante.MINUTE_IN_MS;
						
						String nachricht = "Daten außerhalb des Vertrauensbereichs. Im Zeitraum von " +  //$NON-NLS-1$
								DUAKonstanten.BM_ZEIT_FORMAT.format(start) + " Uhr bis " + DUAKonstanten.BM_ZEIT_FORMAT.format(ende) + //$NON-NLS-1$ 
								" (" + parameter.getBezugsZeitraum() + " Stunde(n)) implausible Fahrstreifenwerte für den Wert " + //$NON-NLS-1$ //$NON-NLS-2$
								this.name + " am Fahrstreifen " + originalDatum.getObject() + " von " + ausfallInProzent + "% (> " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								parameter.getMaxAusfallProBezugsZeitraumEin() + "%) entspricht Ausfall von " + stunden + " Stunde(n) " + //$NON-NLS-1$ //$NON-NLS-2$
								minuten + " Minute(n). Fahrstreifenwerte werden auf Implausibel gesetzt."; //$NON-NLS-1$
						
						VERWALTUNG.sendeBetriebsMeldung("Vertrauensbereichsprüfung", //$NON-NLS-1$
								MessageType.APPLICATION_DOMAIN, Konstante.LEERSTRING,
								MessageGrade.WARNING, MessageState.NEW_MESSAGE, nachricht);
						this.vertrauensBereichVerletzt = true;
					}
				}
								
				if(ausschaltSchwelleUNTERSchritten){					
					if(this.vertrauensBereichVerletzt){
						this.vertrauensBereichVerletzt = false;
						long stunden = ausfallZeit / Konstante.STUNDE_IN_MS;
						long minuten = (ausfallZeit - (stunden * Konstante.STUNDE_IN_MS)) / Konstante.MINUTE_IN_MS;
						ausfall = new BezugsZeitraumAusfall(parameter.getMaxAusfallProBezugsZeitraumAus(), ausfallInProzent, stunden, minuten);
					}
				}
			}
		}
		
		return ausfall; 
	}
}
