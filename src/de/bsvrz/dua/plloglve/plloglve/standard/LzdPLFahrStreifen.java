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
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;

/**
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class LzdPLFahrStreifen 
extends KzdPLFahrStreifen{
	
	
	/**
	 * {@inheritDoc}
	 */
	protected LzdPLFahrStreifen(final IVerwaltungMitGuete verwaltung, final SystemObject obj){
		super(verwaltung, obj);
	}
	
	
//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	protected Data berechneQPkwUndVKfz(Data data){
//		final long qKfz = data.getItem("qKfz").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
//		final long qLkw = data.getItem("qLkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
//						
//		long qPkw = 0;
//		double qPkwGuete = -1;
//		if(qKfz >= 0){
//			long qPkwDummy = qKfz - (qLkw >= 0?qLkw:0);
//			if(qPkwDummy >= 0){
//				qPkw = qPkwDummy;	
//			}else{
//				qPkw = 0;
//				data.getItem("qPkw").getItem("Status").getItem("MessWertErsetzung").  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
//					getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
//			}
//			
//			try {
//				GWert qKfzG = new GWert(data.getItem("qKfz").getItem("Güte")); //$NON-NLS-1$ //$NON-NLS-2$
//				GWert qLkwG = new GWert(data.getItem("qLkw").getItem("Güte"));  //$NON-NLS-1$ //$NON-NLS-2$
//				qPkwGuete = GueteVerfahren.differenz(qKfzG, qLkwG).getIndex();
//			} catch (GueteException e) {
//				e.printStackTrace();
//				LOGGER.error("Berechnung der Guete von qPkw fehlgeschlagen", e); //$NON-NLS-1$
//			}
//		}
//		
//		if(DUAUtensilien.isWertInWerteBereich(data.getItem("qPkw").getItem("Wert"), qPkw)){ //$NON-NLS-1$ //$NON-NLS-2$
//			data.getItem("qPkw").getUnscaledValue("Wert").set(qPkw); //$NON-NLS-1$ //$NON-NLS-2$	
//			if(qPkwGuete >= 0.0){
//				data.getItem("qPkw").getItem("Güte").getScaledValue("Index").set(qPkwGuete); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//			}
//		}else{
//			data.getItem("qPkw").getItem("Status").getItem("MessWertErsetzung"). //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//				getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
//		}
//		
//		final long vPkw = data.getItem("vPkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
//		final long vLkw = data.getItem("vLkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
//		long vKfz = DUAKonstanten.NICHT_ERMITTELBAR;
//		double vKfzGuete = -1;
//		if(qKfz > 0){
//			long qPkwDummy = qPkw >= 0?qPkw:0;
//			long vPkwDummy = vPkw >= 0?vPkw:0;
//			long qLkwDummy = qLkw >= 0?qLkw:0;
//			long vLkwDummy = vLkw >= 0?vLkw:0;
//			vKfz = (long)(((double)(qPkwDummy * vPkwDummy + qLkwDummy * vLkwDummy) / (double)qKfz) + 0.5);
//
//			try {
//				GWert qPkwG = new GWert(data.getItem("qPkw").getItem("Güte")); //$NON-NLS-1$ //$NON-NLS-2$
//				GWert vPkwG = new GWert(data.getItem("vPkw").getItem("Güte")); //$NON-NLS-1$ //$NON-NLS-2$
//				GWert qLkwG = new GWert(data.getItem("qLkw").getItem("Güte")); //$NON-NLS-1$ //$NON-NLS-2$
//				GWert vLkwG = new GWert(data.getItem("vLkw").getItem("Güte")); //$NON-NLS-1$ //$NON-NLS-2$
//				GWert qKfzG = new GWert(data.getItem("qKfz").getItem("Güte")); //$NON-NLS-1$ //$NON-NLS-2$
//				
//				vKfzGuete = GueteVerfahren.quotient(
//								GueteVerfahren.summe(
//										GueteVerfahren.produkt(qPkwG, vPkwG),
//										GueteVerfahren.produkt(qLkwG, vLkwG)
//								),
//								qKfzG
//							).getIndex();
//				
//			} catch (GueteException e) {
//				e.printStackTrace();
//				LOGGER.error("Berechnung der Guete von vKfz fehlgeschlagen", e); //$NON-NLS-1$
//			}
//		}
//		
//		if(DUAUtensilien.isWertInWerteBereich(data.getItem("vKfz").getItem("Wert"), vKfz)){ //$NON-NLS-1$ //$NON-NLS-2$
//			data.getItem("vKfz").getUnscaledValue("Wert").set(vKfz); //$NON-NLS-1$ //$NON-NLS-2$
//			if(vKfzGuete >= 0.0){
//				data.getItem("vKfz").getItem("Güte").getScaledValue("Index").set(vKfzGuete); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//			}
//		}else{
//			data.getItem("vKfz").getUnscaledValue("Wert").set(DUAKonstanten.NICHT_ERMITTELBAR); //$NON-NLS-1$ //$NON-NLS-2$
//			//data.getItem("vKfz").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT);  //$NON-NLS-1$//$NON-NLS-2$
//			//DUAUtensilien.getAttributDatum("vKfz.Status.MessWertErsetzung.Implausibel", data).asUnscaledValue().set(DUAKonstanten.JA); //$NON-NLS-1$
//		}
//		
//		DUAUtensilien.getAttributDatum("qPkw.Status.Erfassung.NichtErfasst", data).asUnscaledValue().set(DUAKonstanten.JA); //$NON-NLS-1$
//		DUAUtensilien.getAttributDatum("vKfz.Status.Erfassung.NichtErfasst", data).asUnscaledValue().set(DUAKonstanten.JA); //$NON-NLS-1$
//		
//		return data;
//	}

	


	/* (Kein Javadoc)
	 * @see de.bsvrz.dua.plloglve.plloglve.standard.KzdPLFahrStreifen#ueberpruefe(stauma.dav.clientside.Data, stauma.dav.clientside.ResultData)
	 */
	@Override
	protected void ueberpruefe(Data data, ResultData resultat) {
		if(this.parameterAtgLog != null){	
			synchronized (this.parameterAtgLog) {				
				/**
				 * Regel Nr.12 (aus SE-02.00.00.00.00-AFo-4.0, S.95)
				 */
				this.untersucheWerteBereich(data, resultat, "qKfz", this.parameterAtgLog.getQKfzBereichMin(),  //$NON-NLS-1$
														  this.parameterAtgLog.getQKfzBereichMax());

				/**
				 * Regel Nr.14 (aus SE-02.00.00.00.00-AFo-4.0, S.95)
				 */
				this.untersucheWerteBereich(data, resultat, "qLkw", this.parameterAtgLog.getQLkwBereichMin(),  //$NON-NLS-1$
														  this.parameterAtgLog.getQLkwBereichMax());
			}
		}
	}




	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AttributeGroup getPlausibilisierungsParameterAtg(ClientDavInterface dav) {
		return dav.getDataModel().getAttributeGroup(AtgVerkehrsDatenLZIPlPruefLogisch.getPid());
	}	
	
}
