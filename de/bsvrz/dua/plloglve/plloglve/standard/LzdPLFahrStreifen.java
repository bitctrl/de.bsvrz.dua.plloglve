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
import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.AttributeGroup;
import stauma.dav.configuration.interfaces.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;

/**
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class LzdPLFahrStreifen 
extends AbstraktPLFahrStreifen{
	
	/**
	 * Alle statischen Instanzen dieser Klasse
	 */
	private static Map<SystemObject, LzdPLFahrStreifen> INSTANZEN = Collections.synchronizedMap(
			new TreeMap<SystemObject, LzdPLFahrStreifen>());
	
	
	/**
	 * {@inheritDoc}
	 */
	private LzdPLFahrStreifen(final SystemObject obj)
	throws Exception{
		super(obj);
	}
	
	/**
	 * Erfragt die statische Instanz dieser Klasse, die mit dem übergebenen
	 * Systemobjekt assoziiert ist 
	 * 
	 * @param obj ein Systemobjekt (vom Typ <code>typ.fahrStreifen</code>)
	 * @param verwaltung Verbindung zum Verwaltungsmodul
	 * @return eine statische Instanz dieser Klasse
	 * @throws Exception wenn die Instanz nicht ermittelt werden konnte
	 */
	public static final synchronized LzdPLFahrStreifen getInstanz(final SystemObject obj,
																  final IVerwaltung verwaltung)
	throws Exception{
		if(VERWALTUNG == null){
			VERWALTUNG = verwaltung;
		}
		
		LzdPLFahrStreifen dummy = INSTANZEN.get(obj);
		
		if(dummy == null){
			dummy = new LzdPLFahrStreifen(obj);
			INSTANZEN.put(obj, dummy);
		}
		
		return dummy;
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
			}catch(IllegalStateException ex){
				LOGGER.info(Konstante.LEERSTRING, ex);
			}
			
			if(copy != null){
				this.grenzWertTests(copy);
			}else{
				LOGGER.warning("Es konnte keine Kopie von Datensatz erzeugt werden:\n" //$NON-NLS-1$
						+ resultat);
			}
		}
		
		return copy;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void grenzWertTests(Data data) {
		if(this.parameterAtgLog != null){
			synchronized (this.parameterAtgLog) {				
				/**
				 * Regel Nr.12 (aus SE-02.00.00.00.00-AFo-4.0, S.95)
				 */
				this.untersucheWerteBereich(data, "qKfz", this.parameterAtgLog.getQKfzBereichMin(),  //$NON-NLS-1$
														  this.parameterAtgLog.getQKfzBereichMax());

				/**
				 * Regel Nr.14 (aus SE-02.00.00.00.00-AFo-4.0, S.95)
				 */
				this.untersucheWerteBereich(data, "qLkw", this.parameterAtgLog.getQLkwBereichMin(),  //$NON-NLS-1$
														  this.parameterAtgLog.getQLkwBereichMax());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void ueberpruefeKontextFehler(Data data, ResultData resultat) {
		// mach hier nichts
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AttributeGroup getPlausibilisierungsParameterAtg(ClientDavInterface dav) {
		return dav.getDataModel().getAttributeGroup(AtgVerkehrsDatenLZIPlPruefLogisch.getPid());
	}	
	
}
