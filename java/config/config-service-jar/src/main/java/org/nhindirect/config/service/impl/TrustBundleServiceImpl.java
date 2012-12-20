package org.nhindirect.config.service.impl;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import javax.jws.WebService;

import org.apache.camel.ProducerTemplate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nhindirect.config.service.ConfigurationServiceException;
import org.nhindirect.config.service.TrustBundleService;
import org.nhindirect.config.store.BundleRefreshError;
import org.nhindirect.config.store.TrustBundle;
import org.nhindirect.config.store.TrustBundleAnchor;
import org.nhindirect.config.store.dao.TrustBundleDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;


@WebService(endpointInterface = "org.nhindirect.config.service.TrustBundleService")
public class TrustBundleServiceImpl implements TrustBundleService
{
    private static final Log log = LogFactory.getLog(TrustBundleServiceImpl.class);

	protected ProducerTemplate template;
    
    private TrustBundleDao dao;

    /**
	 * Initialization method.
	 */
    public void init() 
    {
        log.info("TrustBundleServiceImpl initialized");
    }

	@Override
	public Collection<TrustBundle> getTrustBundles(boolean fetchAnchors)
			throws ConfigurationServiceException 
	{
		final Collection<TrustBundle> bundles = dao.getTrustBundles();
		
		if (!fetchAnchors)
		{
			for (TrustBundle bundle : bundles)
				bundle.setTrustBundleAnchors(new ArrayList<TrustBundleAnchor>());
		}
		
		return bundles;
	}

	@Override
	public TrustBundle getTrustBundleByName(String bundleName)
			throws ConfigurationServiceException 
	{
		return dao.getTrustBundleByName(bundleName);
	}

	@Override
	public TrustBundle getTrustBundleById(long id)
			throws ConfigurationServiceException 
	{
		return dao.getTrustBundleById(id);
	}

	@Override
	public void addTrustBundle(TrustBundle bundle)
			throws ConfigurationServiceException 
	{
		dao.addTrustBundle(bundle);
		template.sendBody(bundle);
	}

	@Override
	public void updateTrustBundleAnchors(long trustBundleId,
			Calendar attemptTime, Collection<TrustBundleAnchor> newAnchorSet)
			throws ConfigurationServiceException 
	{
		dao.updateTrustBundleAnchors(trustBundleId, attemptTime, newAnchorSet);		
	}

	@Override
	public void updateLastUpdateError(long trustBundleId, Calendar attemptTime,
			BundleRefreshError error) throws ConfigurationServiceException 
	{
		dao.updateLastUpdateError(trustBundleId, attemptTime, error);		
	}

	@Override
	public void deleteTrustBundles(long[] trustBundleIds)
			throws ConfigurationServiceException 
	{
		dao.deleteTrustBundles(trustBundleIds);
	}

	@Override
	public void updateTrustBundleSigningCertificate(long trustBundleId,
			X509Certificate signingCert) throws ConfigurationServiceException 
	{
		dao.updateTrustBundleSigningCertificate(trustBundleId, signingCert);	
	}

    /**
     * Set the value of the DNSDao object.
     * 
     * @param dao
     *            the value of the DNSDao object.
     */
    @Autowired
    public void setDao(TrustBundleDao dao) 
    {
        this.dao = dao;
    }

    /**
     * Return the value of the DNSDao object.
     * 
     * @return the value of the DNSDao object.
     */
    public TrustBundleDao getDao() 
    {
        return dao;
    }
    
    @Autowired
    @Qualifier("bundleRefresh")
    public void setTemplate(ProducerTemplate template) 
    {
        this.template = template;
    }
    
}