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

package de.bsvrz.dua.plloglve.vew;

import java.util.Collection;

import stauma.dav.clientside.DataDescription;
import stauma.dav.clientside.ReceiveOptions;
import stauma.dav.clientside.ReceiverRole;
import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.SystemObject;
import sys.funclib.application.StandardApplicationRunner;
import sys.funclib.debug.Debug;
import de.bsvrz.dua.plformal.plformal.PlPruefungFormal;
import de.bsvrz.dua.plformal.vew.PPFStandardAspekteVersorger;
import de.bsvrz.dua.plloglve.plloglve.PlPruefungLogischLVE;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktVerwaltungsAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.SWETyp;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;

/**
 * Implementierung des Moduls Verwaltung der SWE Pl-Pr�fung logisch LVE.
 * Dieses Modul erfragt die zu �berpr�fenden Daten aus der Parametrierung
 * und initialisiert damit die Module Pl-Pr�fung formal und Pl-Pr�fung logisch LVE,
 * die dann die eigentliche Pr�fung durchf�hren.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class VerwaltungPlPruefungLogischLVE
extends AbstraktVerwaltungsAdapter{
	
	/**
	 * G�tefaktor f�r Ersetzungen (97%)<br>
	 * Wenn das Modul Pl-Pr�fung logisch LVE einen Messwert ersetzt (eigentlich
	 * nur bei Wertebereichspr�fung) so vermindert sich die G�te des Ausgangswertes
	 * um diesen Faktor  
	 */
	public static final double GUETE_FAKTOR = 0.97; 
	
	/**
	 * Debug-Logger
	 */
	protected static final Debug LOGGER = Debug.getLogger();

	/**
	 * Instanz des Moduls PL-Pr�fung formal
	 */
	private PlPruefungFormal plPruefungFormal = null;

	/**
	 * Instanz des Moduls PL-Pr�fung logisch LVE
	 */
	private PlPruefungLogischLVE plPruefungLogischLVE = null;
	
	
	/**
	 * {@inheritDoc}
	 */
	public SWETyp getSWETyp() {
		return SWETyp.PL_PRUEFUNG_LOGISCH_LVE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initialisiere()
	throws DUAInitialisierungsException {

		String infoStr = Konstante.LEERSTRING;
		Collection<SystemObject> plLogLveObjekte = DUAUtensilien.getBasisInstanzen(
				this.verbindung.getDataModel().getType(DUAKonstanten.TYP_FAHRSTREIFEN),
				this.verbindung, this.getKonfigurationsBereiche());
		this.objekte = plLogLveObjekte.toArray(new SystemObject[0]);
		
		for(SystemObject obj:this.objekte){
			infoStr += obj + "\n"; //$NON-NLS-1$
		}
		LOGGER.config("---\nBetrachtete Objekte:\n" + infoStr + "---\n"); //$NON-NLS-1$ //$NON-NLS-2$
		
		this.plPruefungFormal = new PlPruefungFormal(
				new PPFStandardAspekteVersorger(this).getStandardPubInfos());
		this.plPruefungFormal.setPublikation(true);
		this.plPruefungFormal.initialisiere(this);

		this.plPruefungLogischLVE = new PlPruefungLogischLVE(
				new PPLogLVEStandardAspekteVersorger(this).getStandardPubInfos());
		this.plPruefungLogischLVE.setPublikation(true);
		this.plPruefungLogischLVE.initialisiere(this);
		
		this.plPruefungFormal.setNaechstenBearbeitungsKnoten(this.plPruefungLogischLVE);		
		
		DataDescription anmeldungsBeschreibungKZD = new DataDescription(
				this.verbindung.getDataModel().getAttributeGroup(DUAKonstanten.ATG_KZD),
				this.verbindung.getDataModel().getAspect(DUAKonstanten.ASP_EXTERNE_ERFASSUNG),
				(short)0);
		DataDescription anmeldungsBeschreibungLZD = new DataDescription(
				this.verbindung.getDataModel().getAttributeGroup(DUAKonstanten.ATG_LZD),
				this.verbindung.getDataModel().getAspect(DUAKonstanten.ASP_EXTERNE_ERFASSUNG),
				(short)0);
			
		this.verbindung.subscribeReceiver(this, this.objekte, anmeldungsBeschreibungKZD,
					ReceiveOptions.normal(), ReceiverRole.receiver());
		this.verbindung.subscribeReceiver(this, this.objekte, anmeldungsBeschreibungLZD,
					ReceiveOptions.delayed(), ReceiverRole.receiver());
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void update(ResultData[] resultate) {
		this.plPruefungFormal.aktualisiereDaten(resultate);
	}
	
	/**
	 * Startet diese Applikation
	 * 
	 * @param args Argumente der Kommandozeile
	 */
	public static void main(String argumente[]){
        Thread.setDefaultUncaughtExceptionHandler(new Thread.
        				UncaughtExceptionHandler(){
            public void uncaughtException(@SuppressWarnings("unused")
			Thread t, Throwable e) {
                LOGGER.error("Applikation wird wegen" +  //$NON-NLS-1$
                		" unerwartetem Fehler beendet", e);  //$NON-NLS-1$
                Runtime.getRuntime().exit(0);
            }
        });
		StandardApplicationRunner.run(
					new VerwaltungPlPruefungLogischLVE(),argumente);
	}
	
}
