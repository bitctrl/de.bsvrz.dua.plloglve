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
import de.bsvrz.sys.funclib.bitctrl.daf.DaVKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.GanzZahl;
import de.bsvrz.sys.funclib.bitctrl.dua.MesswertUnskaliert;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.bitctrl.modell.AbstractSystemObjekt;
import de.bsvrz.sys.funclib.bitctrl.modell.SystemObjekt;
import de.bsvrz.sys.funclib.bitctrl.modell.SystemObjektTyp;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Abstrakte Klasse zum Ablegen von Informationen der Standardplausibilisierung LVE
 * für LZD und KZD. Diese Klasse macht nichts weiter, als sich auf die Grenzwertparameter
 * anzumelden und einige Funktionen zur Plausibilisierung zur Verfügung zu stellen
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
	 * letztes zur Plausibilisierung übergebenes Datum
	 */
	protected ResultData letztesKZDatum = null;
	
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
		
		VERWALTUNG.getVerbindung().subscribeReceiver(this, obj,
				new DataDescription(
						this.getPlausibilisierungsParameterAtg(VERWALTUNG.getVerbindung()),
						VERWALTUNG.getVerbindung().getDataModel().getAspect(DaVKonstanten.ASP_PARAMETER_SOLL),
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
	 * i.   Für qKfz == -1, -2, -3 oder implausibel --> qPkw = nicht ermittelbar<br>
	 * ii.  Für qKfz >= 0 und plausibel UND für qLkw == -1, -2, -3 oder implausibel --> qPkw = qKfz.<br>
	 * iii. Für qKfz >= 0 und plausibel UND für qLkw >= 0 und plausibel UND qLkw > qKfz --> qPkw = nicht ermittelbar.<br>
	 * iv.  Für qKfz >= 0 und plausibel UND für qLkw >= 0 und plausibel UND qLkw <= qKfz --> qPkw = qKfz – qLkw.<br><br>
	 * 
	 * Berechnung von vKfz (KZD und LZD):<br>
	 * Wenn einer der Faktoren im Zähler -1, -2, -3 oder implausibel ist, wird er als 0 angenommen
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
	 * <b>Nach AFo 4.0</b><br>
	 * Untersucht den Wertebereich eines Verkehrs-Datums und
	 * markiert ggf. verletzte Wertebereiche<br><br> 
	 * 
	 *   i. Setze Min: Wenn Wert != -1, -2, -3 UND Wert < Min --> Ersetzung und Kennzeichnung mit MIN<br>
     *  ii. Setze Max: Wenn Wert != -1, -2, -3 UND Wert > Max --> Ersetzung und Kennzeichnung mit MAX<br>
     * iii. Setze MinMax: Wie Setze Min UND Setze Max<br>
     *  iv. Nur Prüfung: Wenn Wert != -1, -2, -3 UND !(Min <= Wert <= Max) --> Kennzeichnung als Implausibel UND fehlerhaft<br>
     *   v. Keine Prüfung --> mache nichts<br>
     *   
	 * @param davDatum ein zu veränderndes Verkehrs-Datums (darf nicht <code>null</code> sein)
	 * @param resultat das Originaldatum
	 * @param wertName der Name des final Attributs 
	 * @param min untere Grenze des Wertes
	 * @param max obere Grenze des Wertes
	 * @return das plaubilisierte (markierte) Datum 
	 */
	@Deprecated
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
						guete.setWert(davDatum.getItem(wertName).getItem("Güte").//$NON-NLS-1$
							getUnscaledValue("Index").longValue());  //$NON-NLS-1$
						if(!guete.isZustand()){
							double gueteIndex = guete.getSkaliertenWert();
							gueteIndex *= VERWALTUNG.getGueteFaktor();
							davDatum.getItem(wertName).getItem("Güte").//$NON-NLS-1$
								getScaledValue("Index").set(gueteIndex);  //$NON-NLS-1$
						}
					}
				}
			}
		}
		
		return davDatum;
	}
	
	
	/**
	 * <b>Nach Afo 5.2</b><br>
	 * Untersucht die obere Grenze des Wertebereichs eines Verkehrs-Datums und
	 * markiert ggf. Verletzungen<br><br>
	 * 
	 *   i. Setze Min: mache nichts<br>
     *  ii. Setze Max: Wenn Wert >= 0 UND Wert > Max --> Ersetzung und Kennzeichnung mit MAX<br>
     * iii. Setze MinMax: wie bei ii<br>
     *  iv. Nur Prüfung: Wenn Wert >= 0 UND Wert > Max --> Kennzeichnung als Implausibel UND fehlerhaft<br>
     *   v. Keine Prüfung: mache nichts<br>
     *   
	 * @param davDatum ein zu veränderndes Verkehrs-Datums (darf nicht <code>null</code> sein)
	 * @param resultat das Originaldatum
	 * @param wertName der Name des final Attributs 
	 * @param max obere Grenze des Wertes
	 * @return ob die Pl-Pruefung an dieser Stelle abgebrochen werden soll 
	 */
	protected final boolean untersucheAufMaxVerletzung(Data davDatum, final ResultData resultat, 
													   final String wertName, final long max){
		boolean abbruch = false;
		
		if(this.parameterAtgLog != null){
			
			OptionenPlausibilitaetsPruefungLogischVerkehr optionen = this.parameterAtgLog.getOptionen();

			if(!optionen.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.KEINE_PRUEFUNG)){
				final long wert = resultat.getData().getItem(wertName).getUnscaledValue("Wert").longValue(); //$NON-NLS-1$

				GanzZahl sweGueteWert = GanzZahl.getGueteIndex();
				sweGueteWert.setSkaliertenWert(VERWALTUNG.getGueteFaktor());
				GWert sweGuete = new GWert(sweGueteWert, GueteVerfahren.STANDARD, false);

				/**
				 * sonst handelt es sich nicht um einen Messwert
				 */
				if(wert >= 0 && max >= 0){
					boolean maxVerletzt = wert > max;
		
					if(maxVerletzt){
						if(optionen.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MAX) || 
						   optionen.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MIN_MAX)){
							davDatum.getItem(wertName).getUnscaledValue("Wert").set(max); //$NON-NLS-1$
							davDatum.getItem(wertName).getItem("Status"). //$NON-NLS-1$
								getItem("PlLogisch").getUnscaledValue("WertMaxLogisch").set(DUAKonstanten.JA); //$NON-NLS-1$ //$NON-NLS-2$
							davDatum.getItem(wertName).getItem("Güte").//$NON-NLS-1$
								getUnscaledValue("Index").set(sweGuete.getIndexUnskaliert());  //$NON-NLS-1$
							
							/**
							 * Neue Anforderungen nach AFo 5.2
							 */
							if(wertName.equals("qKfz")){ //$NON-NLS-1$
								/**
								 * Neu aus AFo 5.2:
								 * Ist qKfzMax >= qLkw ist qPkw=qKfzMax-qLkw zu setzen, sonst werden alle Werte qKfz, qLkw,
								 * qPkw, vKfz, vLkw, vPkw mit den Statusflags Implausibel und Fehlerhaft gekennzeichnet und
								 * die Prüfung abgebrochen.
								 */
								
								long qLkw = davDatum.getItem("qLkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
								GWert qLkwGuete = new GWert(davDatum, "qLkw"); //$NON-NLS-1$
								if(qLkw >= 0){
									if(max >= qLkw){
										long qPkw = max - qLkw;
										MesswertUnskaliert qPkwMW = new MesswertUnskaliert("qPkw"); //$NON-NLS-1$
										qPkwMW.setWertUnskaliert(qPkw);
										qPkwMW.setNichtErfasst(true);										
										qPkwMW.kopiereInhaltNach(davDatum);
										
										GWert qLkwGueteNeu = GWert.getNichtErmittelbareGuete(GueteVerfahren.STANDARD); 
										try {
											qLkwGueteNeu = GueteVerfahren.differenz(sweGuete, qLkwGuete);
										} catch (GueteException e) {
											LOGGER.error("Guete von qLkw konnte nicht aktualisiert werden in " + resultat); //$NON-NLS-1$
											e.printStackTrace();
										}
										
										davDatum.getItem(wertName).getItem("Güte").//$NON-NLS-1$
												getScaledValue("Index").set(qLkwGueteNeu.getIndexUnskaliert());  //$NON-NLS-1$
									}else{
										this.setQUndVImplausibel(davDatum);
										abbruch = true;										
									}
								}
							}else
							if(wertName.equals("qPkw")){ //$NON-NLS-1$
								/**
								 * Neu aus AFo 5.2:
								 * 13. qPkw <= qPkwMax, sonst Wert entsprechend Parametrierung setzen und kennzeichnen und
								 * qKfz = qKfz – (qPkw – qPkwMax) sowie qLkw = qKfz – qPkwMax zu setzen.
								 */
								final long qKfz = davDatum.getItem("qKfz").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
								if(qKfz >= 0){
									/**
									 * qKfz anpassen
									 */
									long qKfzNeu = qKfz - (wert - max);
									MesswertUnskaliert qKfzMW = new MesswertUnskaliert("qKfz"); //$NON-NLS-1$
									qKfzMW.setWertUnskaliert(qKfzNeu);
									qKfzMW.kopiereInhaltNach(davDatum);
									
									GWert qKfzGuete = new GWert(davDatum, "qKfz"); //$NON-NLS-1$
									GWert qKfzGueteNeu = GWert.getNichtErmittelbareGuete(GueteVerfahren.STANDARD);
									try {
										qKfzGueteNeu = GueteVerfahren.differenz(qKfzGuete, sweGuete);
									} catch (GueteException e) {
										LOGGER.error("Guete von qKfz konnte nicht aktualisiert werden in " + resultat); //$NON-NLS-1$
										e.printStackTrace();
									}
									
									davDatum.getItem("qKfz").getItem("Güte").//$NON-NLS-1$//$NON-NLS-2$
											getScaledValue("Index").set(qKfzGueteNeu.getIndexUnskaliert());  //$NON-NLS-1$
									
									/**
									 * qLkw anpassen
									 */
									long qLkwNeu = qKfzNeu - max;
									MesswertUnskaliert qLkwMW = new MesswertUnskaliert("qLkw"); //$NON-NLS-1$
									qLkwMW.setWertUnskaliert(qLkwNeu);
									qLkwMW.kopiereInhaltNach(davDatum);
									
									GWert qLkwGueteNeu = GWert.getNichtErmittelbareGuete(GueteVerfahren.STANDARD);
									try {
										qLkwGueteNeu = GueteVerfahren.differenz(qKfzGueteNeu, sweGuete);
									} catch (GueteException e) {
										LOGGER.error("Guete von qLkw konnte nicht aktualisiert werden in " + resultat); //$NON-NLS-1$
										e.printStackTrace();
									}
									
									davDatum.getItem("qLkw").getItem("Güte").//$NON-NLS-1$//$NON-NLS-2$
											getScaledValue("Index").set(qLkwGueteNeu.getIndexUnskaliert());  //$NON-NLS-1$									
								}
							}else
							if(wertName.equals("qLkw")){ //$NON-NLS-1$
								/**
								 * Neu aus AFo 5.2:
								 * 14. qLkw <= qLkwMax, sonst Wert entsprechend Parametrierung setzen und kennzeichnen und
								 * qPkw=qKfz-qLkw zu setzen.
								 */
								final long qKfz = davDatum.getItem("qKfz").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
								final long qLkw = davDatum.getItem("qLkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
								if(qKfz >= 0 && qLkw >= 0){
									/**
									 * qPkw anpassen
									 */
									long qPkwNeu = qKfz - qLkw;
									MesswertUnskaliert qPkwMW = new MesswertUnskaliert("qPkw"); //$NON-NLS-1$
									qPkwMW.setWertUnskaliert(qPkwNeu);
									qPkwMW.setNichtErfasst(true);
									qPkwMW.kopiereInhaltNach(davDatum);
									
									GWert qKfzGuete = new GWert(davDatum, "qKfz"); //$NON-NLS-1$
									GWert qLkwGuete = new GWert(davDatum, "qLkw"); //$NON-NLS-1$
									GWert qPkwGueteNeu = GWert.getNichtErmittelbareGuete(GueteVerfahren.STANDARD);
									try {
										qPkwGueteNeu = GueteVerfahren.differenz(qKfzGuete, qLkwGuete);
									} catch (GueteException e) {
										LOGGER.error("Guete von qPkw konnte nicht aktualisiert werden in " + resultat); //$NON-NLS-1$
										e.printStackTrace();
									}
									
									davDatum.getItem("qKfz").getItem("Güte").//$NON-NLS-1$//$NON-NLS-2$
											getScaledValue("Index").set(qPkwGueteNeu.getIndexUnskaliert());  //$NON-NLS-1$									
								}
							}							
						}else
						if(optionen.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.NUR_PRUEFUNG)){							
							if(wertName.equals("vKfz") || wertName.startsWith("qKfz") ||  //$NON-NLS-1$ //$NON-NLS-2$
							   wertName.equals("vLkw") || wertName.startsWith("qLkw") ||  //$NON-NLS-1$ //$NON-NLS-2$
							   wertName.equals("vPkw") || wertName.startsWith("qPkw")){  //$NON-NLS-1$ //$NON-NLS-2$
								this.setQUndVImplausibel(davDatum);
								abbruch = true;
							}
						}
					}					
				}
			}
		}
		
		return abbruch;
	}

	
	/**
	 * Plausibilisiert ein übergebenes Datum
	 * 
	 * @param resultat ein Originaldatum
	 * @return das veränderte Datum oder <code>null</code>, wenn keine Veränderungen
	 * vorgenommen werden mussten
	 */
	protected Data plausibilisiere(final ResultData resultat){
		Data copy = null;
		
		if(resultat.getData() != null){
			try{
				copy = resultat.getData().createModifiableCopy();			
				this.berechneQPkwUndVKfz(copy);
				this.ueberpruefe(copy, resultat);
			}catch(IllegalStateException e){
				LOGGER.error("Es konnte keine Kopie von Datensatz erzeugt werden:\n" //$NON-NLS-1$
						+ resultat, e);
			}
		}
		this.letztesKZDatum = resultat;

		return copy;
	}

	
	/**
	 * Setzt im uebergebenen Datensatz die Werte <code>qPkw</code>, <code>qLkw</code>, 
	 * <code>qKfz</code> und <code>vPkw</code>, <code>vLkw</code>, <code>vKfz</code>
	 * auf implausibel und fehlerhaft 
	 * 
	 * @param veraenderbaresDatum ein veraenderbarer LVE-Datensatz (muss <code>!= null</code> sein)
	 */
	protected final void setQUndVImplausibel(Data veraenderbaresDatum){
		veraenderbaresDatum.getItem("qKfz").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
		veraenderbaresDatum.getItem("qLkw").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
		veraenderbaresDatum.getItem("qPkw").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

		veraenderbaresDatum.getItem("vKfz").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
		veraenderbaresDatum.getItem("vLkw").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
		veraenderbaresDatum.getItem("vPkw").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

		veraenderbaresDatum.getItem("qKfz").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		veraenderbaresDatum.getItem("qLkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		veraenderbaresDatum.getItem("qPkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		
		veraenderbaresDatum.getItem("vKfz").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		veraenderbaresDatum.getItem("vLkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		veraenderbaresDatum.getItem("vPkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
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
	 * Führt eine
	 * 
	 * @param data
	 * @param resultat der Original-Datensatz
	 */
	protected abstract void ueberpruefe(Data data, final ResultData resultat);

}
