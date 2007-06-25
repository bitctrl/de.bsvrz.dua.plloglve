package de.bsvrz.dua.plloglve.util.para;

import de.bsvrz.dua.plloglve.plloglve.standard.AtgVerkehrsDatenKZIPlPruefLogisch;
import stauma.dav.clientside.ClientDavInterface;
import stauma.dav.configuration.interfaces.AttributeGroup;

/**
 * 
 * @author Thierfelder
 *
 */
public class ParaKZDLogImport
extends AbstraktParameterImport{

	
	/**
	 * {@inheritDoc}
	 */
	public ParaKZDLogImport(ClientDavInterface dav, String csvQuelle)
	throws Exception {
		super(dav, csvQuelle);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getAttributPfadVon(String attributInCSVDatei) {
		if(attributInCSVDatei.equals("vGrenz")){ //$NON-NLS-1$
			return "vKfzGrenz"; //$NON-NLS-1$
		}
		if(attributInCSVDatei.equals("bGrenz")){ //$NON-NLS-1$
			return "bGrenz"; //$NON-NLS-1$
		}
		if(attributInCSVDatei.equals("vGrenz")){ //$NON-NLS-1$
			return "vKfzGrenz"; //$NON-NLS-1$
		}
		if(attributInCSVDatei.equals("vGrenz")){ //$NON-NLS-1$
			return "vKfzGrenz"; //$NON-NLS-1$
		}
		if(attributInCSVDatei.equals("vGrenz")){ //$NON-NLS-1$
			return "vKfzGrenz"; //$NON-NLS-1$
		}
		if(attributInCSVDatei.equals("vGrenz")){ //$NON-NLS-1$
			return "vKfzGrenz"; //$NON-NLS-1$
		}

	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AttributeGroup getParameterAtg() {
		return DAV.getDataModel().getAttributeGroup(AtgVerkehrsDatenKZIPlPruefLogisch.getPid());
	}

}
