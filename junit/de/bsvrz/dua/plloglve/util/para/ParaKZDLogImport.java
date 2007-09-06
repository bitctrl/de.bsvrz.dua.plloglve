/** 
 * Segment 4 Daten�bernahme und Aufbereitung (DUA)
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

package de.bsvrz.dua.plloglve.util.para;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.plloglve.plloglve.standard.AtgVerkehrsDatenKZIPlPruefLogisch;
import de.bsvrz.dua.plloglve.plloglve.typen.OptionenPlausibilitaetsPruefungLogischVerkehr;

/**
 * Importiert die Parameter f�r die Pl-Pr�fung logisch KZD
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class ParaKZDLogImport
extends AbstraktParameterImport{

	/**
	 * Pr�f-Optionen
	 */
	private OptionenPlausibilitaetsPruefungLogischVerkehr optionen = null;
	
	
	/**
	 * {@inheritDoc}
	 */
	public ParaKZDLogImport(ClientDavInterface dav, SystemObject objekt, String csvQuelle)
	throws Exception {
		super(dav, objekt, csvQuelle);
	}

	
	/**
	 * Setzte die Pr�f-Optionen
	 * 
	 * @param optionen aktuelle Pr�f-Optionen
	 */
	public final void setOptionen(final OptionenPlausibilitaetsPruefungLogischVerkehr optionen){
		this.optionen = optionen;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Data fuelleRestAttribute(Data datensatz) {
		datensatz.getUnscaledValue("Optionen").set(this.optionen.getCode()); //$NON-NLS-1$
		return datensatz;
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getAttributPfadVon(String attributInCSVDatei, int index) {
		if(attributInCSVDatei.equals("vGrenz")){ //$NON-NLS-1$
			return "vKfzGrenz"; //$NON-NLS-1$
		}
		if(attributInCSVDatei.equals("bGrenz")){ //$NON-NLS-1$
			return "bGrenz"; //$NON-NLS-1$
		}
		
		if(attributInCSVDatei.endsWith(")")){ //$NON-NLS-1$
			String nummerStr = attributInCSVDatei.substring(
					attributInCSVDatei.length() - 2, attributInCSVDatei.length() - 1);
			int nummer = -1;
			try{
				nummer = Integer.parseInt(nummerStr);
			}catch(Exception ex){
				//
			}

			if(nummer == index){
				if(attributInCSVDatei.startsWith("qKfzMin")){ //$NON-NLS-1$
					return "qKfzBereich.Min"; //$NON-NLS-1$
				}
				if(attributInCSVDatei.startsWith("qKfzMax")){ //$NON-NLS-1$
					return "qKfzBereich.Max"; //$NON-NLS-1$
				}
				if(attributInCSVDatei.startsWith("qLkwMax")){ //$NON-NLS-1$
					return "qLkwBereich.Max"; //$NON-NLS-1$
				}
				if(attributInCSVDatei.startsWith("qLkwMin")){ //$NON-NLS-1$
					return "qLkwBereich.Min"; //$NON-NLS-1$
				}
				if(attributInCSVDatei.startsWith("qPkwMax")){ //$NON-NLS-1$
					return "qPkwBereich.Max"; //$NON-NLS-1$
				}
				if(attributInCSVDatei.startsWith("qPkwMin")){ //$NON-NLS-1$
					return "qPkwBereich.Min"; //$NON-NLS-1$
				}
				if(attributInCSVDatei.startsWith("vKfzMin")){ //$NON-NLS-1$
					return "vKfzBereich.Min"; //$NON-NLS-1$
				}
				if(attributInCSVDatei.startsWith("vKfzMax")){ //$NON-NLS-1$
					return "vKfzBereich.Max"; //$NON-NLS-1$
				}
				if(attributInCSVDatei.startsWith("vLkwMax")){ //$NON-NLS-1$
					return "vLkwBereich.Max"; //$NON-NLS-1$
				}
				if(attributInCSVDatei.startsWith("vLkwMin")){ //$NON-NLS-1$
					return "vLkwBereich.Min"; //$NON-NLS-1$
				}
				if(attributInCSVDatei.startsWith("vPkwMax")){ //$NON-NLS-1$
					return "vPkwBereich.Max"; //$NON-NLS-1$
				}
				if(attributInCSVDatei.startsWith("vPkwMin")){ //$NON-NLS-1$
					return "vPkwBereich.Min"; //$NON-NLS-1$
				}
				if(attributInCSVDatei.startsWith("vgKfzMax")){ //$NON-NLS-1$
					return "vgKfzBereich.Max"; //$NON-NLS-1$
				}
				if(attributInCSVDatei.startsWith("vgKfzMin")){ //$NON-NLS-1$
					return "vgKfzBereich.Min"; //$NON-NLS-1$
				}				
				if(attributInCSVDatei.startsWith("bMax")){ //$NON-NLS-1$
					return "BelegungBereich.Max"; //$NON-NLS-1$
				}
				if(attributInCSVDatei.startsWith("bMin")){ //$NON-NLS-1$
					return "BelegungBereich.Min"; //$NON-NLS-1$
				}
			}
		}
		
		return null;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AttributeGroup getParameterAtg() {
		return DAV.getDataModel().getAttributeGroup(AtgVerkehrsDatenKZIPlPruefLogisch.getPid());
	}

}
