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

package de.bsvrz.dua.plloglve.plloglve.standard;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.guete.GWert;
import de.bsvrz.dua.guete.GueteException;
import de.bsvrz.dua.guete.GueteVerfahren;
import de.bsvrz.dua.guete.vorschriften.IGuete;
import de.bsvrz.dua.plloglve.plloglve.typen.OptionenPlausibilitaetsPruefungLogischVerkehr;
import de.bsvrz.sys.funclib.bitctrl.daf.Konstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.GanzZahl;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.bitctrl.modell.AbstractSystemObjekt;
import de.bsvrz.sys.funclib.bitctrl.modell.SystemObjekt;
import de.bsvrz.sys.funclib.bitctrl.modell.SystemObjektTyp;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Abstrakte Klasse zum Ablegen von Informationen der Standardplausibilisierung LVE
 * f�r LZD und KZD. Diese Klasse macht nichts weiter, als sich auf die Grenzwertparameter
 * anzumelden und einige Funktionen zur Plausibilisierung zur Verf�gung zu stellen
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
	 * Schnittstelle zu den Parametern der Grenzwertpr�fung 
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
		
		VERWALTUNG.getVerbindung().subscribeReceiver(this, obj,
				new DataDescription(
						this.getPlausibilisierungsParameterAtg(VERWALTUNG.getVerbindung()),
						VERWALTUNG.getVerbindung().getDataModel().getAspect(Konstanten.ASP_PARAMETER_SOLL),
						(short)0),
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
	 * Berechnet aus qKfz und qLkw qPkw und aus vPkw und vLkw vKfz:<br>
	 * <br>
	 * qPkw = qKfz - qLkw<br>
	 * <br>
	 * Wenn qKfz und/oder qLkw == -1, -2, -3 oder implausibel? <br>
	 * i.   F�r qKfz == -1, -2, -3 oder implausibel --> qPkw = nicht ermittelbar<br>
	 * ii.  F�r qKfz >= 0 und plausibel UND f�r qLkw == -1, -2, -3 oder implausibel --> qPkw = qKfz.<br>
	 * iii. F�r qKfz >= 0 und plausibel UND f�r qLkw >= 0 und plausibel UND qLkw > qKfz --> qPkw = nicht ermittelbar.<br>
	 * iv.  F�r qKfz >= 0 und plausibel UND f�r qLkw >= 0 und plausibel UND qLkw <= qKfz --> qPkw = qKfz � qLkw.<br><br>
	 * 
	 * Berechnung von vKfz (KZD und LZD):<br>
	 * Wenn einer der Faktoren im Z�hler -1, -2, -3 oder implausibel ist, wird er als 0 angenommen
	 * und mit dem Rest weitergerechnet
	 *  
	 * @param data ein KZD (darf nicht <code>null</code> sein)
	 * @return das um qPkw und vKfz erweiterte KZD
	 */
	protected Data berechneQPkwUndVKfz(Data data){
		final long qKfz = data.getItem("qKfz").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final boolean qKfzImplausibel = 
			data.getItem("qKfz").getItem("Status").getItem("MessWertErsetzung"). //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				getUnscaledValue("Implausibel").longValue() == DUAKonstanten.JA; //$NON-NLS-1$
		final long qLkw = data.getItem("qLkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final boolean qLkwImplausibel = 
			data.getItem("qLkw").getItem("Status").getItem("MessWertErsetzung"). //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				getUnscaledValue("Implausibel").longValue() == DUAKonstanten.JA; //$NON-NLS-1$
		
		long qPkw = DUAKonstanten.NICHT_ERMITTELBAR;
		GWert qPkwGuete = GueteVerfahren.STD_FEHLERHAFT_BZW_NICHT_ERMITTELBAR;
		
		if(qKfz >= 0 && !qKfzImplausibel){
			if(qLkw >= 0 && !qLkwImplausibel){
				if(qLkw > qKfz){
					qPkw = DUAKonstanten.NICHT_ERMITTELBAR;
				}else{
					qPkw = qKfz - qLkw;

					try {
						GWert qKfzG = new GWert(data, "qKfz"); //$NON-NLS-1$
						GWert qLkwG = new GWert(data, "qLkw");  //$NON-NLS-1$
						qPkwGuete = GueteVerfahren.differenz(qKfzG, qLkwG);
					} catch (GueteException e) {
						e.printStackTrace();
						LOGGER.error("Berechnung der Guete von qPkw fehlgeschlagen", e); //$NON-NLS-1$
					}
				}
			}else{
				qPkw = qKfz;
				qPkwGuete = new GWert(data, "qPkw"); //$NON-NLS-1$
			}
		}
		
		
		if(DUAUtensilien.isWertInWerteBereich(data.getItem("qPkw").getItem("Wert"), qPkw)){ //$NON-NLS-1$ //$NON-NLS-2$
			data.getItem("qPkw").getUnscaledValue("Wert").set(qPkw); //$NON-NLS-1$ //$NON-NLS-2$
			qPkwGuete.exportiere(data, "qPkw"); //$NON-NLS-1$
		}else{
			data.getItem("qPkw").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT);  //$NON-NLS-1$//$NON-NLS-2$
			data.getItem("qPkw").getItem("Status"). //$NON-NLS-1$ //$NON-NLS-2$
				getItem("MessWertErsetzung").getUnscaledValue("Implausibel").set(DUAKonstanten.JA);  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-2%
		}
		
		final long vPkw = data.getItem("vPkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final long vLkw = data.getItem("vLkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
		long vKfz = DUAKonstanten.NICHT_ERMITTELBAR;
		GWert vKfzGuete = GueteVerfahren.STD_FEHLERHAFT_BZW_NICHT_ERMITTELBAR;
		
		if(qKfz > 0){
			long qPkwDummy = qPkw >= 0?qPkw:0;
			long vPkwDummy = vPkw >= 0?vPkw:0;
			long qLkwDummy = qLkw >= 0?qLkw:0;
			long vLkwDummy = vLkw >= 0?vLkw:0;
			vKfz = (long)(((double)(qPkwDummy * vPkwDummy + qLkwDummy * vLkwDummy) / (double)qKfz) + 0.5);

			try {
				GWert qPkwG = new GWert(data, "qPkw"); //$NON-NLS-1$
				GWert vPkwG = new GWert(data, "vPkw"); //$NON-NLS-1$
				GWert qLkwG = new GWert(data, "qLkw"); //$NON-NLS-1$
				GWert vLkwG = new GWert(data, "vLkw"); //$NON-NLS-1$
				GWert qKfzG = new GWert(data, "qKfz"); //$NON-NLS-1$
				
				vKfzGuete = GueteVerfahren.quotient(
								GueteVerfahren.summe(
										GueteVerfahren.produkt(qPkwG, vPkwG),
										GueteVerfahren.produkt(qLkwG, vLkwG)
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
			vKfzGuete.exportiere(data, "vKfz"); //$NON-NLS-1$
		}else{
			data.getItem("vKfz").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT);  //$NON-NLS-1$//$NON-NLS-2$
			data.getItem("vKfz").getItem("Status"). //$NON-NLS-1$ //$NON-NLS-2$
				getItem("MessWertErsetzung").getUnscaledValue("Implausibel").set(DUAKonstanten.JA);  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-2%
		}
		
		data.getItem("qPkw").getItem("Status").  //$NON-NLS-1$//$NON-NLS-2$
			getItem("Erfassung").getUnscaledValue("NichtErfasst").set(DUAKonstanten.JA);  //$NON-NLS-1$//$NON-NLS-2$
		data.getItem("vKfz").getItem("Status").  //$NON-NLS-1$//$NON-NLS-2$
			getItem("Erfassung").getUnscaledValue("NichtErfasst").set(DUAKonstanten.JA);  //$NON-NLS-1$//$NON-NLS-2$
		
		return data;
	}
		
	
	/**
	 * Untersucht den Wertebereich eines Verkehrs-Datums und
	 * markiert ggf. verletzte Wertebereiche<br><br>
	 * 
	 *   i. Setze Min: Wenn Wert != -1, -2, -3 UND Wert < Min --> Ersetzung und Kennzeichnung mit MIN<br>
     *  ii. Setze Max: Wenn Wert != -1, -2, -3 UND Wert > Max --> Ersetzung und Kennzeichnung mit MAX<br>
     * iii. Setze MinMax: Wie Setze Min UND Setze Max<br>
     *  iv. Nur Pr�fung: Wenn Wert != -1, -2, -3 UND !(Min <= Wert <= Max) --> Kennzeichnung als Implausibel UND fehlerhaft<br>
     *   v. Keine Pr�fung --> mache nichts<br>
     *   
	 * @param davDatum ein zu ver�nderndes Verkehrs-Datums (darf nicht <code>null</code> sein)
	 * @param resultat das Originaldatum
	 * @param wertName der Name des final Attributs 
	 * @param min untere Grenze des Wertes
	 * @param max obere Grenze des Wertes
	 * @return das plaubilisierte (markierte) Datum 
	 */
	protected final Data untersucheWerteBereich(Data davDatum, final ResultData resultat, 
												final String wertName, final long min, final long max){
		
		if(this.parameterAtgLog != null){
			
			OptionenPlausibilitaetsPruefungLogischVerkehr optionen = this.parameterAtgLog.getOptionen();

			if(!optionen.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.KEINE_PRUEFUNG)){
				long wert = resultat.getData().getItem(wertName).getUnscaledValue("Wert").longValue(); //$NON-NLS-1$
				
				/**
				 * sonst handelt es sich nicht um einen Messwert
				 */
				if(wert >= 0){
					boolean minVerletzt = wert < min;
					boolean maxVerletzt = wert > max;
					boolean gueteNeuBerechnen = false;
		
					if(minVerletzt){
						if(optionen.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MIN) || 
						   optionen.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MIN_MAX)){
							davDatum.getItem(wertName).getUnscaledValue("Wert").set(min); //$NON-NLS-1$
							davDatum.getItem(wertName).getItem("Status"). //$NON-NLS-1$
								getItem("PlLogisch").getUnscaledValue("WertMinLogisch").set(DUAKonstanten.JA); //$NON-NLS-1$ //$NON-NLS-2$
							gueteNeuBerechnen = true;
						}else
						if(optionen.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.NUR_PRUEFUNG)){
							davDatum.getItem(wertName).getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$
							davDatum.getItem(wertName).getItem("Status"). //$NON-NLS-1$
								getItem("MessWertErsetzung").getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
					if(maxVerletzt){
						if(optionen.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MAX) || 
						   optionen.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MIN_MAX)){
							davDatum.getItem(wertName).getUnscaledValue("Wert").set(max); //$NON-NLS-1$
							davDatum.getItem(wertName).getItem("Status"). //$NON-NLS-1$
								getItem("PlLogisch").getUnscaledValue("WertMaxLogisch").set(DUAKonstanten.JA); //$NON-NLS-1$ //$NON-NLS-2$
							gueteNeuBerechnen = true;
						}else
						if(optionen.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.NUR_PRUEFUNG)){
							davDatum.getItem(wertName).getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$
							davDatum.getItem(wertName).getItem("Status"). //$NON-NLS-1$
								getItem("MessWertErsetzung").getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
					
					if(gueteNeuBerechnen){
						GanzZahl guete = GanzZahl.getGueteIndex();
						guete.setWert(davDatum.getItem(wertName).getItem("G�te").//$NON-NLS-1$
							getUnscaledValue("Index").longValue());  //$NON-NLS-1$
						if(!guete.isZustand()){
							double gueteIndex = guete.getSkaliertenWert();
							gueteIndex *= VERWALTUNG.getGueteFaktor();
							davDatum.getItem(wertName).getItem("G�te").//$NON-NLS-1$
								getScaledValue("Index").set(gueteIndex);  //$NON-NLS-1$
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
	 * Erfragt das Systemobjekt der Attributgruppe, unter der die
	 * Parameter f�r die Intervallgrenzwerte stehen
	 * 
	 * @param dav die Datenverteiler-Verbindung
	 * @return die Parameter-Attributgruppe
	 */
	protected abstract AttributeGroup getPlausibilisierungsParameterAtg(final ClientDavInterface dav);


	/**
	 * Plausibilisiert ein �bergebenes Datum
	 * 
	 * @param resultat ein Originaldatum
	 * @return das ver�nderte Datum oder <code>null</code>, wenn keine Ver�nderungen
	 * vorgenommen werden mussten
	 */
	protected abstract Data plausibilisiere(final ResultData resultat);

	
	/**
	 * F�hrt eine
	 * 
	 * @param data
	 * @param resultat der Original-Datensatz
	 */
	protected abstract void ueberpruefe(Data data, final ResultData resultat);

}
