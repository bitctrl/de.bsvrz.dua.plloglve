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

package de.bsvrz.dua.plloglve.plloglve.vb;

import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

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
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
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
public class VertrauensFahrStreifen 
extends AbstractSystemObjekt
implements ClientReceiverInterface{
	
	private static final String[] ATTRIBUTE = new String[]{"qKfz", "qLkw", "qPkw", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
														   "vKfz", "vLkw", "vPkw", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
														   "b", "tNetto", "sKfz", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
														   "vgKfz"}; //$NON-NLS-1$

	/**
	 * Debug-Logger
	 */
	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * Verbindung zum Verwaltungsmodul
	 */
	protected static IVerwaltung VERWALTUNG = null;
	
	/**
	 * Datenbeschreibung für Parameterattributgruppe
	 * <code>atg.verkehrsDatenVertrauensBereichFs</code>
	 */
	protected static DataDescription PARA_VERTRAUEN_DD = null;
	
	/**
	 * aktuelle Parameter der Attributgruppe <code>atg.verkehrsDatenVertrauensBereichFs</code>
	 * für diesen Fahrstreifen
	 */
	private AtgVerkehrsDatenVertrauensBereichFs parameter = null;
	
	/**
	 * Datensätze mit Ausfallinformationen des aktuellen Bezugszeitraums
	 * für <code>qKfz</code>
	 */
	private BezugsZeitraum datenBezugsZeitraumQKfz = null;

	/**
	 * Datensätze mit Ausfallinformationen des aktuellen Bezugszeitraums
	 * für <code>qLkw</code>
	 */
	private BezugsZeitraum datenBezugsZeitraumQLkw = null;
	
	/**
	 * Datensätze mit Ausfallinformationen des aktuellen Bezugszeitraums
	 * für <code>qPkw</code>
	 */
	private BezugsZeitraum datenBezugsZeitraumQPkw = null;
	
	/**
	 * Datensätze mit Ausfallinformationen des aktuellen Bezugszeitraums
	 * für <code>vKfz</code>
	 */
	private BezugsZeitraum datenBezugsZeitraumVKfz = null;
	
	/**
	 * Datensätze mit Ausfallinformationen des aktuellen Bezugszeitraums
	 * für <code>vLkw</code>
	 */
	private BezugsZeitraum datenBezugsZeitraumVLkw = null;
	
	/**
	 * Datensätze mit Ausfallinformationen des aktuellen Bezugszeitraums
	 * für <code>vPkw</code>
	 */
	private BezugsZeitraum datenBezugsZeitraumVPkw = null;
	
	/**
	 * Datensätze mit Ausfallinformationen des aktuellen Bezugszeitraums
	 * für <code>sKfz</code>
	 */
	private BezugsZeitraum datenBezugsZeitraumSKfz = null;
	
	/**
	 * Datensätze mit Ausfallinformationen des aktuellen Bezugszeitraums
	 * für <code>b</code>
	 */
	private BezugsZeitraum datenBezugsZeitraumB = null;
	
	/**
	 * zeigt an, ob der Vertrauensbereich für diesen Fahrstreifen durch irgendein
	 * Attribut verletzt ist
	 */
	private boolean vertrauenVerletztAllgemein = false;
	
	
	
	/**
	 * Standardkonstruktor
	 * 
	 * @param verwaltung Verbindung zum Verwaltungsmodul
	 * @param objekt das mit einem Fahrstreifen assoziierte Systemobjekt
	 */
	protected VertrauensFahrStreifen(final IVerwaltung verwaltung, final SystemObject objekt){
		super(objekt);
		
		if(VERWALTUNG == null){
			VERWALTUNG = verwaltung;
			PARA_VERTRAUEN_DD = new DataDescription(
					VERWALTUNG.getVerbindung().getDataModel().getAttributeGroup("atg.verkehrsDatenVertrauensBereichFs"), //$NON-NLS-1$
					VERWALTUNG.getVerbindung().getDataModel().getAspect(Konstante.DAV_ASP_PARAMETER_SOLL),
					(short)0);
		}
		
		datenBezugsZeitraumQKfz = new BezugsZeitraum(VERWALTUNG, "qKfz");  //$NON-NLS-1$
		datenBezugsZeitraumQLkw = new BezugsZeitraum(VERWALTUNG, "qLkw");  //$NON-NLS-1$
		datenBezugsZeitraumQPkw = new BezugsZeitraum(VERWALTUNG, "qPkw");  //$NON-NLS-1$
		datenBezugsZeitraumVKfz = new BezugsZeitraum(VERWALTUNG, "vKfz");  //$NON-NLS-1$
		datenBezugsZeitraumVLkw = new BezugsZeitraum(VERWALTUNG, "vLkw");  //$NON-NLS-1$
		datenBezugsZeitraumVPkw = new BezugsZeitraum(VERWALTUNG, "vPkw");  //$NON-NLS-1$
		datenBezugsZeitraumSKfz = new BezugsZeitraum(VERWALTUNG, "sKfz");  //$NON-NLS-1$
		datenBezugsZeitraumB = new BezugsZeitraum(VERWALTUNG, "b");  //$NON-NLS-1$
		
		VERWALTUNG.getVerbindung().subscribeReceiver(this, objekt, PARA_VERTRAUEN_DD, 
				ReceiveOptions.normal(), ReceiverRole.receiver());
		
	}
	
	
	/**
	 * Führt eine Plausibilisierung des übergebenen Originaldatums durch
	 * und gibt ggf. eine Betriebsmeldung aus
	 * 
	 * @param originalDatum ein DAV-Originaldatum
	 * @return das plausibilisierte Datum oder <code>null</code>, wenn
	 * das emfangene Originaldatum nicht verändert werden musste
	 */
	protected final Data plausibilisiere(final ResultData originalDatum){
		Data copy = null;

		if(originalDatum != null && originalDatum.getData() != null &&
			originalDatum.getDataDescription().getAttributeGroup().getPid().equals(PLLOGKonstanten.ATG_KZD)){
			synchronized (this) {
				if(this.parameter != null && this.parameter.isAuswertbar()){

					SortedSet<BezugsZeitraumAusfall> ausfallErgebnisse = new TreeSet<BezugsZeitraumAusfall>();

					ausfallErgebnisse.add(this.datenBezugsZeitraumQKfz.ermittleAusfall(originalDatum, this.parameter));
					ausfallErgebnisse.add(this.datenBezugsZeitraumQLkw.ermittleAusfall(originalDatum, this.parameter));
					ausfallErgebnisse.add(this.datenBezugsZeitraumQPkw.ermittleAusfall(originalDatum, this.parameter));
					ausfallErgebnisse.add(this.datenBezugsZeitraumVKfz.ermittleAusfall(originalDatum, this.parameter));
					ausfallErgebnisse.add(this.datenBezugsZeitraumVLkw.ermittleAusfall(originalDatum, this.parameter));
					ausfallErgebnisse.add(this.datenBezugsZeitraumVPkw.ermittleAusfall(originalDatum, this.parameter));
					ausfallErgebnisse.add(this.datenBezugsZeitraumSKfz.ermittleAusfall(originalDatum, this.parameter));
					ausfallErgebnisse.add(this.datenBezugsZeitraumB.ermittleAusfall(originalDatum, this.parameter));

					boolean verletztAlt = this.vertrauenVerletztAllgemein;
					boolean verletztAktuell = this.datenBezugsZeitraumQKfz.isVertrauensBereichVerletzt() ||
						this.datenBezugsZeitraumQLkw.isVertrauensBereichVerletzt() ||
						this.datenBezugsZeitraumQPkw.isVertrauensBereichVerletzt() ||
						this.datenBezugsZeitraumVKfz.isVertrauensBereichVerletzt() ||
						this.datenBezugsZeitraumVLkw.isVertrauensBereichVerletzt() ||
						this.datenBezugsZeitraumVPkw.isVertrauensBereichVerletzt() ||
						this.datenBezugsZeitraumSKfz.isVertrauensBereichVerletzt() ||
						this.datenBezugsZeitraumB.isVertrauensBereichVerletzt();

					if(verletztAktuell){
						copy = VERWALTUNG.getVerbindung().createData(originalDatum.getDataDescription().getAttributeGroup());
						
						for(String attribut:ATTRIBUTE){
							DUAUtensilien.getAttributDatum(attribut + ".Status.MessWertErsetzung.Implausibel", copy). //$NON-NLS-1$
									asUnscaledValue().set(DUAKonstanten.JA);
						}
					}else{
						if(verletztAlt){
							Date start = new Date(originalDatum.getDataTime() - parameter.getBezugsZeitraum() * Konstante.STUNDE_IN_MS);
							Date ende = new Date(originalDatum.getDataTime());

							String nachricht = "Daten wieder innerhalb des Vertrauensbereichs. Im Zeitraum von " +  //$NON-NLS-1$
							DUAKonstanten.BM_ZEIT_FORMAT.format(start) + " Uhr bis " + DUAKonstanten.BM_ZEIT_FORMAT.format(ende) + //$NON-NLS-1$ 
							" (" + parameter.getBezugsZeitraum() + " Stunde(n)) implausible Fahrstreifenwerte am Fahrstreifen " +   //$NON-NLS-1$//$NON-NLS-2$
							originalDatum.getObject() + " von " + ausfallErgebnisse.last() + //$NON-NLS-1$ 
							". Fahrstreifenwerte werden wieder verarbeitet."; //$NON-NLS-1$

							VERWALTUNG.sendeBetriebsMeldung("Vertrauensbereichsprüfung", //$NON-NLS-1$
									MessageType.APPLICATION_DOMAIN, Konstante.LEERSTRING,
									MessageGrade.WARNING, MessageState.NEW_MESSAGE, nachricht);
						}
					}

					this.vertrauenVerletztAllgemein = verletztAktuell;

				}else{
					LOGGER.config("Datum kann nicht plausibilisiert werden, da keine" + //$NON-NLS-1$
							" (oder nicht verwertbare) Parameter vorliegen: " //$NON-NLS-1$
							+ this.getPid());
				}
			}
		}
		
		return copy;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void update(ResultData[] resultate) {
		if(resultate != null){
			for(ResultData resultat:resultate){
				if(resultat != null && resultat.getData() != null){
					synchronized (this) {
						this.parameter = new AtgVerkehrsDatenVertrauensBereichFs(resultat.getData());
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
				return VertrauensFahrStreifen.class;
			}

			public String getPid() {
				return getSystemObject().getType().getPid();
			}
			
		};
		
	}
	
}
