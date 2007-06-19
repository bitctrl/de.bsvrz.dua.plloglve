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

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import stauma.dav.clientside.ClientDavInterface;
import stauma.dav.clientside.Data;
import stauma.dav.clientside.DataDescription;
import stauma.dav.clientside.ReceiveOptions;
import stauma.dav.clientside.ReceiverRole;
import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.AttributeGroup;
import stauma.dav.configuration.interfaces.SystemObject;
import de.bsvrz.dua.plloglve.plloglve.PLLOGKonstanten;
import de.bsvrz.sys.funclib.bitctrl.daf.Konstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;

/**
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class KzdPLFahrStreifen
extends AbstraktPLFahrStreifen{
	
	/**
	 * Datenbeschreibung für <code>atg.tlsLveBetriebsParameter</code> zur Bestimmung der
	 * Art der Mittelwertbildung
	 */
	private static DataDescription DE_BETRIEBS_PARAMETER = null;
	
	/**
	 * Die Art der Mittelwertbildung
	 */
	private boolean mittelWertBildungIstArithmethisch = true;
	
	/**
	 * letztes zur Plausibilisierung übergebenes Datum
	 */
	private ResultData letztesKZDatum = null;
	
	/**
	 * Alle statischen Instanzen dieser Klasse
	 */
	protected static Map<SystemObject, KzdPLFahrStreifen> INSTANZEN = Collections.synchronizedMap(
			new TreeMap<SystemObject, KzdPLFahrStreifen>());
	

	/**
	 * Standartdkonstruktor 
	 * 
	 * @param obj das mit dem Fahrstreifen assoziierte Systemobjekt
	 */
	private KzdPLFahrStreifen(final SystemObject obj)
	throws Exception{
		super(obj);
				
		/**
		 * Versuche die Daten der assoziierten DE auszulesen
		 * um die Art der Mittelwertbindung zu ermitteln
		 */
		DataDescription konfig = new DataDescription(
				VERWALTUNG.getVerbindung().getDataModel().getAttributeGroup("atg.fahrStreifen"), //$NON-NLS-1$
				VERWALTUNG.getVerbindung().getDataModel().getAspect("asp.eigenschaften"), //$NON-NLS-1$
				(short)0);
		ResultData konfigResult = VERWALTUNG.getVerbindung().getData(obj, konfig, 10000L);
		
		if(konfigResult != null && konfigResult.getData() != null){
			Data.ReferenceValue refValue = konfigResult.getData().getReferenceValue("FahrStreifenQuelle"); //$NON-NLS-1$
			if(refValue != null){
				SystemObject deObj = refValue.getSystemObject();
				if(deObj.isOfType("typ.deLve")){ //$NON-NLS-1$
					VERWALTUNG.getVerbindung().subscribeReceiver(this, deObj, DE_BETRIEBS_PARAMETER,
							ReceiveOptions.normal(), ReceiverRole.receiver());
				}else{
					LOGGER.error("Mit dem Fahrstreifen " +  //$NON-NLS-1$
							this.getPid() + " ist kein DE-LVE assoziiert"); //$NON-NLS-1$
				}
			}else{
				LOGGER.error("Mit dem Fahrstreifen " +  //$NON-NLS-1$
						this.getPid() + " ist keine Quelle assoziiert"); //$NON-NLS-1$
			}
		}else{
			LOGGER.error("Die DE-Konfiguration für den Fahrstreifen " +  //$NON-NLS-1$
					this.getPid() + " konnte nicht ausgelesen werden"); //$NON-NLS-1$
		}
	}
	

	/**
	 * 
	 * @param obj
	 * @param verwaltung
	 * @return
	 * @throws Exception
	 */
	public static final synchronized KzdPLFahrStreifen getInstanz(final SystemObject obj,
																  final IVerwaltung verwaltung)
	throws Exception{
		if(VERWALTUNG == null){
			VERWALTUNG = verwaltung;
			DE_BETRIEBS_PARAMETER = new DataDescription(
					VERWALTUNG.getVerbindung().getDataModel().getAttributeGroup("atg.tlsLveBetriebsParameter"), //$NON-NLS-1$
					VERWALTUNG.getVerbindung().getDataModel().getAspect(Konstanten.ASP_PARAMETER_SOLL),
					(short)0);
		}
		
		KzdPLFahrStreifen dummy = INSTANZEN.get(obj);
		
		if(dummy == null){
			dummy = new KzdPLFahrStreifen(obj);
			INSTANZEN.put(obj, dummy);
		}
		
		return dummy;
	}

	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(ResultData[] parameterFeld) {
		super.update(parameterFeld);
		
		if(parameterFeld != null){
			for(ResultData parameter:parameterFeld){
				if(parameter != null && parameter.getData() != null){
					if(parameter.getDataDescription().getAttributeGroup().equals(
							DE_BETRIEBS_PARAMETER.getAttributeGroup())){
						this.mittelWertBildungIstArithmethisch = parameter.getData().
								getUnscaledValue("ArtMittelWertBildung").intValue() == 1; //$NON-NLS-1$
					}
				}
			}
		}
	}
	

	@Override
	protected Data plausibilisiere(final ResultData resultat){
		Data copy = null;
		
		if(resultat.getData() != null){
			try{
				copy = resultat.getData().createModifiableCopy();
			}catch(IllegalStateException e){
				LOGGER.error(Konstante.LEERSTRING, e);
			}
			
			if(copy != null){
				this.berechneQPkw(copy);
				this.ueberpruefeKontextFehler(copy, resultat);
				this.grenzWertTests(copy);
			}else{
				LOGGER.warning("Es konnte keine Kopie von Datensatz erzeugt werden:\n" //$NON-NLS-1$
						+ resultat);
			}
		}
		this.letztesKZDatum = resultat;

		return copy;
	}
	

	@Override
	protected void ueberpruefeKontextFehler(Data data, ResultData resultat){

		final int qKfz = data.getItem("qKfz").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final int qLkw = data.getItem("qLkw").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final int qPkw = data.getItem("qPkw").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final int vPkw = data.getItem("vPkw").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final int vLkw = data.getItem("vLkw").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final int vKfz = data.getItem("vKfz").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final int vgKfz = data.getItem("vgKfz").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final long tNetto = data.getItem("tNetto").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final long T = data.getTimeValue("T").getMillis(); //$NON-NLS-1$
		final int b = data.getItem("b").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$

		int vgKfzLetztesIntervall = -4;
		if(this.letztesKZDatum != null){
			if(this.letztesKZDatum.getData() != null){
				if(T == resultat.getDataTime() - this.letztesKZDatum.getDataTime()){
					vgKfzLetztesIntervall = this.letztesKZDatum.getData().getItem("vgKfz"). //$NON-NLS-1$
					getUnscaledValue("Wert").intValue(); //$NON-NLS-1$
				}
			}
		}

		/**
		 * Regel Nr.1 (aus SE-02.00.00.00.00-AFo-4.0, S.94)
		 */
		if(qKfz == 0 && (qLkw != 0 || qPkw != 0)){
			data.getItem("qKfz").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
			data.getItem("qLkw").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
			data.getItem("qPkw").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

			data.getItem("qKfz").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
			data.getItem("qLkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
			data.getItem("qPkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		}

		if(this.mittelWertBildungIstArithmethisch){

			/**
			 * Regel Nr.2 (aus SE-02.00.00.00.00-AFo-4.0, S.94)
			 */
			if(qKfz - qLkw == 0 && (qPkw != 0 || vPkw != PLLOGKonstanten.NICHT_ERMITTELBAR)){
				data.getItem("qKfz").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
				data.getItem("qLkw").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
				data.getItem("qPkw").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
				data.getItem("vPkw").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

				data.getItem("qKfz").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
				data.getItem("qLkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
				data.getItem("qPkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
				data.getItem("vPkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
			}

			/**
			 * Regel Nr.3 (aus SE-02.00.00.00.00-AFo-4.0, S.94)
			 */
			if(qLkw == 0 && vLkw != PLLOGKonstanten.NICHT_ERMITTELBAR){
				data.getItem("qLkw").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
				data.getItem("vLkw").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

				data.getItem("qLkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
				data.getItem("vLkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$				
			}

			/**
			 * Regel Nr.4 (aus SE-02.00.00.00.00-AFo-4.0, S.94)
			 */
			if(qPkw == 0 && vPkw != PLLOGKonstanten.NICHT_ERMITTELBAR){
				data.getItem("qPkw").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
				data.getItem("vPkw").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

				data.getItem("qPkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
				data.getItem("vPkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$				
			}		
		}

		/**
		 * Regel Nr.5 (aus SE-02.00.00.00.00-AFo-4.0, S.94)
		 */
		if(qKfz < qLkw){
			data.getItem("qKfz").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
			data.getItem("qLkw").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

			data.getItem("qKfz").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
			data.getItem("vLkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$							
		}

		/**
		 * Regel Nr.6 (aus SE-02.00.00.00.00-AFo-4.0, S.94)
		 */
		if(qKfz - qLkw > 0 && vPkw <= 0){
			data.getItem("qKfz").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
			data.getItem("qLkw").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
			data.getItem("vPkw").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

			data.getItem("qKfz").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
			data.getItem("qLkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
			data.getItem("vPkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		}

		/**
		 * Regel Nr.7 (aus SE-02.00.00.00.00-AFo-4.0, S.94)
		 */
		if(qKfz > 0 && vKfz <= 0){
			data.getItem("qKfz").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
			data.getItem("vKfz").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

			data.getItem("qKfz").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
			data.getItem("vKfz").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		}

		/**
		 * Regel Nr.8 (aus SE-02.00.00.00.00-AFo-4.0, S.94)
		 */
		if(qLkw > 0 && vLkw <= 0){
			data.getItem("qLkw").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
			data.getItem("vLkw").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

			data.getItem("qLkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
			data.getItem("vLkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		}

		/**
		 * Regel Nr.9 (aus SE-02.00.00.00.00-AFo-4.0, S.94)
		 */
		if( !(0 < tNetto && tNetto <= T) ){
			data.getItem("tNetto").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

			data.getItem("tNetto").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		}

		/**
		 * Regel Nr.10 (aus SE-02.00.00.00.00-AFo-4.0, S.94)
		 */
		if(vgKfzLetztesIntervall != -4){
			if(qKfz == 0 && vgKfz != vgKfzLetztesIntervall){
				data.getItem("vgKfz").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

				data.getItem("vgKfz").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
			}
		}

		/**
		 * Regel Nr.11 (aus SE-02.00.00.00.00-AFo-4.0, S.94)
		 */
		if(vKfz > parameterAtgLog.getVKfzGrenz() && b >= parameterAtgLog.getBGrenz() ){
			data.getItem("b").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

			data.getItem("b").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$			
		}
	}
	
	@Override
	protected void grenzWertTests(Data data){
		if(this.parameterAtgLog != null){
			synchronized (this.parameterAtgLog) {				
				/**
				 * Regel Nr.12 (aus SE-02.00.00.00.00-AFo-4.0, S.95)
				 */
				this.untersucheWerteBereich(data, "qKfz", this.parameterAtgLog.getQKfzBereichMin(),  //$NON-NLS-1$
														  this.parameterAtgLog.getQKfzBereichMax());
	
				/**
				 * Regel Nr.13 (aus SE-02.00.00.00.00-AFo-4.0, S.95)
				 */
				this.untersucheWerteBereich(data, "qPkw", this.parameterAtgLog.getQPkwBereichMin(),  //$NON-NLS-1$
								  					      this.parameterAtgLog.getQPkwBereichMax());

				/**
				 * Regel Nr.14 (aus SE-02.00.00.00.00-AFo-4.0, S.95)
				 */
				this.untersucheWerteBereich(data, "qLkw", this.parameterAtgLog.getQLkwBereichMin(),  //$NON-NLS-1$
														  this.parameterAtgLog.getQLkwBereichMax());

				/**
				 * Regel Nr.15 (aus SE-02.00.00.00.00-AFo-4.0, S.95)
				 */
				this.untersucheWerteBereich(data, "vKfz", this.parameterAtgLog.getVKfzBereichMin(),  //$NON-NLS-1$
														  this.parameterAtgLog.getVKfzBereichMax());

				/**
				 * Regel Nr.16 (aus SE-02.00.00.00.00-AFo-4.0, S.96)
				 */
				this.untersucheWerteBereich(data, "vLkw", this.parameterAtgLog.getVLkwBereichMin(),  //$NON-NLS-1$
														  this.parameterAtgLog.getVLkwBereichMax());

				/**
				 * Regel Nr.17 (aus SE-02.00.00.00.00-AFo-4.0, S.96)
				 */
				this.untersucheWerteBereich(data, "vPkw", this.parameterAtgLog.getVPkwBereichMin(),  //$NON-NLS-1$
														  this.parameterAtgLog.getVPkwBereichMax());

				/**
				 * Regel Nr.18 (aus SE-02.00.00.00.00-AFo-4.0, S.96)
				 */
				this.untersucheWerteBereich(data, "vgKfz", this.parameterAtgLog.getVgKfzBereichMin(),  //$NON-NLS-1$
														   this.parameterAtgLog.getVgKfzBereichMax());

				/**
				 * Regel Nr.19 (aus SE-02.00.00.00.00-AFo-4.0, S.96)
				 */
				this.untersucheWerteBereich(data, "b", this.parameterAtgLog.getBelegungBereichMin(),  //$NON-NLS-1$
													   this.parameterAtgLog.getBelegungBereichMax());
			}
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AttributeGroup getPlausibilisierungsParameterAtg(ClientDavInterface dav) {
		return dav.getDataModel().getAttributeGroup(AtgVerkehrsDatenKZIPlPruefLogisch.getPid());
	}	
	
}
