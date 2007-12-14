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
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;

/**
 * Klasse zum Durchf�hren der speziellen Standardplausibilisierung LVE
 * f�r LZD. Diese Klasse macht nichts weiter, als sich auf die Grenzwertparameter
 * anzumelden und einige Funktionen zur Plausibilisierung von LZD zur Verf�gung zu stellen
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class KzdPLFahrStreifen
extends AbstraktPLFahrStreifen{
	

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
	protected void ueberpruefe(Data data, ResultData resultat){
		
		long qKfz = data.getItem("qKfz").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
		long qLkw = data.getItem("qLkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
		long qPkw = data.getItem("qPkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
		long vPkw = data.getItem("vPkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
		long vLkw = data.getItem("vLkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
		long vKfz = data.getItem("vKfz").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
		long vgKfz = data.getItem("vgKfz").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
		long tNetto = data.getItem("tNetto").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
		long T = data.getTimeValue("T").getMillis(); //$NON-NLS-1$
		long b = data.getItem("b").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$

		if(tNetto==169000){
			System.out.println();
		}
		
		
		long vgKfzLetztesIntervall = -4;
		if(this.letztesKZDatum != null){
			if(this.letztesKZDatum.getData() != null){
				if(T == resultat.getDataTime() - this.letztesKZDatum.getDataTime()){
					vgKfzLetztesIntervall = this.letztesKZDatum.getData().getItem("vgKfz"). //$NON-NLS-1$
												getUnscaledValue("Wert").longValue(); //$NON-NLS-1$
				}
			}
		}

		/**
		 * Regel Nr.1 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
		 */
		if(qKfz == 0){
			if(!(qLkw == 0 && qPkw == 0)){
				this.setAllesImplausibel(data);
				return;
			}
		}

		if(data.getUnscaledValue("ArtMittelwertbildung").longValue() == DUAKonstanten.MWB_ARITHMETISCH){ //$NON-NLS-1$

			/**
			 * Regel Nr.2 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
			 */
			if(qKfz >= 0 && qLkw >= 0){
				if(qKfz - qLkw == 0){
					if(!(qPkw == 0 && vPkw == DUAKonstanten.NICHT_ERMITTELBAR)){
						this.setAllesImplausibel(data);
						return;
					}
				}
			}

			/**
			 * Regel Nr.3 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
			 */
			if(qLkw == 0){
				if(!(vLkw == DUAKonstanten.NICHT_ERMITTELBAR)){
					this.setAllesImplausibel(data);
					return;
				}
			}

			/**
			 * Regel Nr.4 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
			 */
			if(qPkw == 0){
				if(!(vPkw == DUAKonstanten.NICHT_ERMITTELBAR)){
					this.setAllesImplausibel(data);
					return;
				}
			}		
		}

		/**
		 * Regel Nr.5 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
		 */
		if(qKfz >= 0 && qLkw >= 0){
			if(qKfz < qLkw){
				this.setAllesImplausibel(data);
				return;
			}
		}

		/**
		 * Regel Nr.6 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
		 */
		if(qKfz >= 0 && qLkw >= 0){
			if(qKfz - qLkw > 0){
				if(vPkw >= 0){
					if(!(0 < vPkw)){
						this.setAllesImplausibel(data);
						return;
					}
				}
			}
		}

		/**
		 * Regel Nr.7 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
		 */
		if(qKfz > 0){
			if(vKfz >= 0){
				if(!(0 < vKfz)){
					this.setAllesImplausibel(data);
					return;
				}
			}
		}

		/**
		 * Regel Nr.8 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
		 */
		if(qLkw > 0){
			if(vLkw >= 0){
				if( !(0 < vLkw) ){
					this.setAllesImplausibel(data);
					return;
				}
			}
		}

		
		/**
		 * Regel Nr.9 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
		 */
		if(tNetto >= 0){
			if( !(0 < tNetto && tNetto <= T) ){
				data.getItem("tNetto").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
				data.getItem("tNetto").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
					getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
			}
		}

		/**
		 * Regel Nr.10 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
		 */
		if(qKfz == 0){
			if(vgKfz >= 0 && vgKfzLetztesIntervall >= 0){
				if(!(vgKfz == vgKfzLetztesIntervall)){
					data.getItem("vgKfz").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
					data.getItem("vgKfz").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
						getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
				}				
			}
		}

		/**
		 * Regel Nr.11 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
		 */
		if(this.parameterAtgLog != null){
			synchronized (this.parameterAtgLog) {
				if(vKfz >= 0 && parameterAtgLog.getVKfzGrenz() >= 0){
					if(vKfz > parameterAtgLog.getVKfzGrenz()){
						if(b >= 0 && parameterAtgLog.getBGrenz() >= 0){
							if( !(b < parameterAtgLog.getBGrenz()) ){
								data.getItem("b").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
								data.getItem("b").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
									getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
							}
						}
					}
				}
			}
		}
	
		if(this.parameterAtgLog != null){
			synchronized (this.parameterAtgLog) {				
				/**
				 * Regel Nr.12 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
				 */
				if(this.untersucheAufMaxVerletzung(data, resultat, "qKfz", //$NON-NLS-1$
														  this.parameterAtgLog.getQKfzBereichMax()))return;
	
				/**
				 * Regel Nr.13 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
				 */
				if(this.untersucheAufMaxVerletzung(data, resultat, "qPkw",  //$NON-NLS-1$
								  					      this.parameterAtgLog.getQPkwBereichMax()))return;

				/**
				 * Regel Nr.14 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
				 */
				if(this.untersucheAufMaxVerletzung(data, resultat, "qLkw", //$NON-NLS-1$
														  this.parameterAtgLog.getQLkwBereichMax()))return;

				/**
				 * Regel Nr.15 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
				 */
				if(this.untersucheAufMaxVerletzung(data, resultat, "vKfz",  //$NON-NLS-1$
														  this.parameterAtgLog.getVKfzBereichMax()))return;

				/**
				 * Regel Nr.16 (aus SE-02.00.00.00.00-AFo-5.2, S.96)
				 */
				if(this.untersucheAufMaxVerletzung(data, resultat, "vLkw", //$NON-NLS-1$
														  this.parameterAtgLog.getVLkwBereichMax()))return;

				/**
				 * Regel Nr.17 (aus SE-02.00.00.00.00-AFo-5.2, S.96)
				 */
				if(this.untersucheAufMaxVerletzung(data, resultat, "vPkw",  //$NON-NLS-1$
														  this.parameterAtgLog.getVPkwBereichMax()))return;

				/**
				 * Regel Nr.18 (aus SE-02.00.00.00.00-AFo-5.2, S.96)
				 */
				if(this.untersucheAufMaxVerletzung(data, resultat, "vgKfz",  //$NON-NLS-1$
														   this.parameterAtgLog.getVgKfzBereichMax()))return;

				/**
				 * Regel Nr.19 (aus SE-02.00.00.00.00-AFo-5.2, S.96)
				 */
				if(this.untersucheAufMaxVerletzung(data, resultat, "b",   //$NON-NLS-1$
													   this.parameterAtgLog.getBelegungBereichMax()))return;
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
