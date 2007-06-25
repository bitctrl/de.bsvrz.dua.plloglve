package de.bsvrz.dua.plloglve.util.para;

import stauma.dav.clientside.ClientDavInterface;
import stauma.dav.configuration.interfaces.AttributeGroup;

public class ParaDiffKontrolleImport 
extends AbstraktParameterImport{

	
	/**
	 * {@inheritDoc}
	 */
	public ParaDiffKontrolleImport(ClientDavInterface dav, String csvQuelle)
	throws Exception {
		super(dav, csvQuelle);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getAttributPfadVon(String attributInCSVDatei) {
		return null;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AttributeGroup getParameterAtg() {
		return DAV.getDataModel().getAttributeGroup("atg.verkehrsDatenDifferenzialKontrolleFs"); //$NON-NLS-1$
	}
	
}
