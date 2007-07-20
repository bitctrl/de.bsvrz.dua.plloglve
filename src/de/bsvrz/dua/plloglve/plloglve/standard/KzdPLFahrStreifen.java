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

import stauma.dav.clientside.ClientDavInterface;
import stauma.dav.clientside.Data;
import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.AttributeGroup;
import stauma.dav.configuration.interfaces.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;

/**
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class KzdPLFahrStreifen
extends AbstraktPLFahrStreifen{
		
	/**
	 * letztes zur Plausibilisierung übergebenes Datum
	 */
	private ResultData letztesKZDatum = null;
	

	/**
	 * Standartdkonstruktor 
	 * 
	 * @param verwaltung Verbindung zum Verwaltungsmodul
	 * @param obj das mit dem Fahrstreifen assoziierte Systemobjekt
	 */
	public KzdPLFahrStreifen(final IVerwaltungMitGuete verwaltung, final SystemObject obj){
		super(verwaltung, obj);				
	}
	

	/**
	 * {@inheritDoc}
	 */
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
	

	/**
	 * {@inheritDoc}
	 */
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
			data.getItem("qKfz").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
			data.getItem("qLkw").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
			data.getItem("qPkw").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

			data.getItem("qKfz").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
			data.getItem("qLkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
			data.getItem("qPkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		}

		if(data.getUnscaledValue("ArtMittelwertbildung").intValue() == 1){ //$NON-NLS-1$

			/**
			 * Regel Nr.2 (aus SE-02.00.00.00.00-AFo-4.0, S.94)
			 */
			if(qKfz - qLkw == 0 && (qPkw != 0 || vPkw != DUAKonstanten.NICHT_ERMITTELBAR)){
				data.getItem("qKfz").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
				data.getItem("qLkw").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
				data.getItem("qPkw").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
				data.getItem("vPkw").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

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
			if(qLkw == 0 && vLkw != DUAKonstanten.NICHT_ERMITTELBAR){
				data.getItem("qLkw").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
				data.getItem("vLkw").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

				data.getItem("qLkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
				data.getItem("vLkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$				
			}

			/**
			 * Regel Nr.4 (aus SE-02.00.00.00.00-AFo-4.0, S.94)
			 */
			if(qPkw == 0 && vPkw != DUAKonstanten.NICHT_ERMITTELBAR){
				data.getItem("qPkw").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
				data.getItem("vPkw").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

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
			data.getItem("qKfz").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
			data.getItem("qLkw").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

			data.getItem("qKfz").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
			data.getItem("qLkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$							
		}

		/**
		 * Regel Nr.6 (aus SE-02.00.00.00.00-AFo-4.0, S.94)
		 */
		if(!(qKfz - qLkw > 0 && vPkw < 0)){
			data.getItem("qKfz").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
			data.getItem("qLkw").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
			data.getItem("vPkw").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

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
			data.getItem("qKfz").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
			data.getItem("vKfz").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

			data.getItem("qKfz").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
			data.getItem("vKfz").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		}

		/**
		 * Regel Nr.8 (aus SE-02.00.00.00.00-AFo-4.0, S.94)
		 */
		if(qLkw > 0 && vLkw <= 0){
			data.getItem("qLkw").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
			data.getItem("vLkw").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

			data.getItem("qLkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
			data.getItem("vLkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		}

		/**
		 * Regel Nr.9 (aus SE-02.00.00.00.00-AFo-4.0, S.94)
		 */
		if( !(0 < tNetto && tNetto <= T) ){
			data.getItem("tNetto").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

			data.getItem("tNetto").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		}

		/**
		 * Regel Nr.10 (aus SE-02.00.00.00.00-AFo-4.0, S.94)
		 */
		if(vgKfzLetztesIntervall != -4){
			if(qKfz == 0 && vgKfz != vgKfzLetztesIntervall){
				data.getItem("vgKfz").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

				data.getItem("vgKfz").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
			}
		}

		/**
		 * Regel Nr.11 (aus SE-02.00.00.00.00-AFo-4.0, S.94)
		 */
		if(this.parameterAtgLog != null){
			synchronized (this.parameterAtgLog) {				
				if(vKfz > parameterAtgLog.getVKfzGrenz() && b >= parameterAtgLog.getBGrenz() ){
					data.getItem("b").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

					data.getItem("b").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
					getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$			
				}
			}
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
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
