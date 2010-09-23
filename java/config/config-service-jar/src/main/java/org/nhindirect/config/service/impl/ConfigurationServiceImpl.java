package org.nhindirect.config.service.impl;
/* 
Copyright (c) 2010, NHIN Direct Project
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer 
   in the documentation and/or other materials provided with the distribution.  
3. Neither the name of the The NHIN Direct Project (nhindirect.org) nor the names of its contributors may be used to endorse or promote 
   products derived from this software without specific prior written permission.
   
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS 
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
THE POSSIBILITY OF SUCH DAMAGE.
*/

import java.util.Collection;
import java.util.List;

import javax.jws.WebService;
import javax.xml.ws.FaultAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nhindirect.config.service.AddressService;
import org.nhindirect.config.service.AnchorService;
import org.nhindirect.config.service.CertificateService;
import org.nhindirect.config.service.ConfigurationFault;
import org.nhindirect.config.service.ConfigurationService;
import org.nhindirect.config.service.ConfigurationServiceException;
import org.nhindirect.config.service.DomainService;

import org.nhindirect.config.store.Address;
import org.nhindirect.config.store.Anchor;
import org.nhindirect.config.store.Certificate;
import org.nhindirect.config.store.Domain;
import org.nhindirect.config.store.EntityStatus;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implements the single Service Endpoint Interface.  Delegates everything to the individual
 * service implementations.
 *
 */
@WebService(endpointInterface = "org.nhindirect.config.service.ConfigurationService")
public class ConfigurationServiceImpl implements ConfigurationService {
	private static Log log = LogFactory.getLog(ConfigurationServiceImpl.class);

	private DomainService      domainSvc;
	
	private AddressService     addressSvc;
	
	private CertificateService certSvc;
	
	private AnchorService      anchorSvc;
	
	public void init() {
		log.info("ConfigurationService initialized");
	}
	

	/* (non-Javadoc)
	 * @see org.nhindirect.config.service.AddressService#addAddress(java.util.List)
	 */
	@Override
	@FaultAction(className=ConfigurationFault.class)
	public void addAddress(Collection<Address> address)
			throws ConfigurationServiceException {
		addressSvc.addAddress(address);
	}

	/* (non-Javadoc)
	 * @see org.nhindirect.config.service.AddressService#updateAddress(org.nhindirect.config.store.Address)
	 */
	@Override
	@FaultAction(className=ConfigurationFault.class)
	public void updateAddress(Address address)
			throws ConfigurationServiceException {
		addressSvc.updateAddress(address);
	}

	/* (non-Javadoc)
	 * @see org.nhindirect.config.service.AddressService#getAddressCount()
	 */
	@Override
	@FaultAction(className=ConfigurationFault.class)
	public int getAddressCount() throws ConfigurationServiceException {
		return addressSvc.getAddressCount();
	}

	/* (non-Javadoc)
	 * @see org.nhindirect.config.service.AddressService#getAddress(java.util.List, org.nhindirect.config.store.EntityStatus)
	 */
	@Override
	@FaultAction(className=ConfigurationFault.class)
	public Collection<Address> getAddress(Collection<String> addressNames, EntityStatus status) throws ConfigurationServiceException {
		return addressSvc.getAddress(addressNames, status);
	}

	/* (non-Javadoc)
	 * @see org.nhindirect.config.service.AddressService#removeAddress(java.lang.String)
	 */
	@Override
	@FaultAction(className=ConfigurationFault.class)
	public void removeAddress(String addressName)
			throws ConfigurationServiceException {
		addressSvc.removeAddress(addressName);

	}

	/* (non-Javadoc)
	 * @see org.nhindirect.config.service.AddressService#listAddresss(java.lang.String, int)
	 */
	@Override
	@FaultAction(className=ConfigurationFault.class)
	public Collection<Address> listAddresss(String lastAddressName, int maxResults)
			throws ConfigurationServiceException {
		return addressSvc.listAddresss(lastAddressName, maxResults);
	}

	/* (non-Javadoc)
	 * @see org.nhindirect.config.service.DomainService#addDomain(org.nhindirect.config.store.Domain)
	 */
	@Override
	@FaultAction(className=ConfigurationFault.class)
	public void addDomain(Domain domain) throws ConfigurationServiceException {
		domainSvc.addDomain(domain);
	}

	/* (non-Javadoc)
	 * @see org.nhindirect.config.service.DomainService#updateDomain(org.nhindirect.config.store.Domain)
	 */
	@Override
	@FaultAction(className=ConfigurationFault.class)
	public void updateDomain(Domain domain)
			throws ConfigurationServiceException {
		domainSvc.updateDomain(domain);
	}

	/* (non-Javadoc)
	 * @see org.nhindirect.config.service.DomainService#getDomainCount()
	 */
	@Override
	@FaultAction(className=ConfigurationFault.class)
	public int getDomainCount() throws ConfigurationServiceException {
		return domainSvc.getDomainCount();
	}

	/* (non-Javadoc)
	 * @see org.nhindirect.config.service.DomainService#getDomains(java.util.Collection, org.nhindirect.config.store.EntityStatus)
	 */
	@Override
	@FaultAction(className=ConfigurationFault.class)
	public Collection<Domain> getDomains(Collection<String> domainNames,
			EntityStatus status) throws ConfigurationServiceException {
		return domainSvc.getDomains(domainNames, status);
	}

	/* (non-Javadoc)
	 * @see org.nhindirect.config.service.DomainService#removeDomain(java.lang.String)
	 */
	@Override
	@FaultAction(className=ConfigurationFault.class)
	public void removeDomain(String domainName)
			throws ConfigurationServiceException {
		domainSvc.removeDomain(domainName);
	}

	/* (non-Javadoc)
	 * @see org.nhindirect.config.service.DomainService#listDomains(java.lang.String, int)
	 */
	@Override
	@FaultAction(className=ConfigurationFault.class)
	public Collection<Domain> listDomains(String lastDomainName, int maxResults)
			throws ConfigurationServiceException {
		return domainSvc.listDomains(lastDomainName, maxResults);
	}

	/* (non-Javadoc)
	 * @see org.nhindirect.config.service.DomainService#searchDomain(java.lang.String, org.nhindirect.config.store.EntityStatus)
	 */
	@Override
	@FaultAction(className=ConfigurationFault.class)
	public Collection<Domain> searchDomain(String domain, EntityStatus status) {
		return domainSvc.searchDomain(domain, status);
	}
	
	@Override
	@FaultAction(className=ConfigurationFault.class)
	public Domain getDomain(Long id) {
		return domainSvc.getDomain(id);
	}

	/* (non-Javadoc)
	 * @see org.nhindirect.config.service.CertificateService#addCertificates(java.util.Collection)
	 */
	@Override
	@FaultAction(className=ConfigurationFault.class)
	public void addCertificates(Collection<Certificate> certs)
			throws ConfigurationServiceException {
		certSvc.addCertificates(certs);
	}

	/* (non-Javadoc)
	 * @see org.nhindirect.config.service.CertificateService#setCertificateStatus(java.util.Collection, org.nhindirect.config.store.EntityStatus)
	 */
	@Override
	@FaultAction(className=ConfigurationFault.class)
	public void setCertificateStatus(Collection<Long> certificateIds, EntityStatus status) throws ConfigurationServiceException {
		certSvc.setCertificateStatus(certificateIds, status);
	}

	/* (non-Javadoc)
	 * @see org.nhindirect.config.service.CertificateService#setCertificateStatusForOwner(java.lang.String, org.nhindirect.config.store.EntityStatus)
	 */
	@Override
	@FaultAction(className=ConfigurationFault.class)
	public void setCertificateStatusForOwner(String owner, EntityStatus status)
			throws ConfigurationServiceException {
		certSvc.setCertificateStatusForOwner(owner, status);
	}

	/* (non-Javadoc)
	 * @see org.nhindirect.config.service.CertificateService#removeCertificates(java.util.Collection)
	 */
	@Override
	@FaultAction(className=ConfigurationFault.class)
	public void removeCertificates(Collection<Long> certificateIds)
			throws ConfigurationServiceException {
		certSvc.removeCertificates(certificateIds);
	}

	/* (non-Javadoc)
	 * @see org.nhindirect.config.service.CertificateService#removeCertificatesForOwner(java.lang.String)
	 */
	@Override
	@FaultAction(className=ConfigurationFault.class)
	public void removeCertificatesForOwner(String owner)
			throws ConfigurationServiceException {
		certSvc.removeCertificatesForOwner(owner);
	}

	/* (non-Javadoc)
	 * @see org.nhindirect.config.service.CertificateService#contains(java.security.cert.Certificate)
	 */
	@Override
	@FaultAction(className=ConfigurationFault.class)
	public boolean contains(Certificate cert) {
		return certSvc.contains(cert);
	}

	/* (non-Javadoc)
	 * @see org.nhindirect.config.service.AnchorService#addAnchors(java.util.List)
	 */
	@Override
	@FaultAction(className=ConfigurationFault.class)
	public void addAnchors(Collection<Anchor> anchors)
			throws ConfigurationServiceException {
		anchorSvc.addAnchors(anchors);

	}

	/* (non-Javadoc)
	 * @see org.nhindirect.config.service.AnchorService#setAnchorStatusForOwner(java.lang.String, org.nhindirect.config.store.EntityStatus)
	 */
	@Override
	@FaultAction(className=ConfigurationFault.class)
	public void setAnchorStatusForOwner(String owner, EntityStatus status)
			throws ConfigurationServiceException {
		anchorSvc.setAnchorStatusForOwner(owner, status);
	}

	/* (non-Javadoc)
	 * @see org.nhindirect.config.service.AnchorService#removeAnchors(java.util.List)
	 */
	@Override
	@FaultAction(className=ConfigurationFault.class)
	public void removeAnchors(Collection<Long> anchorIds)
			throws ConfigurationServiceException {
		anchorSvc.removeAnchors(anchorIds);
	}

	/* (non-Javadoc)
	 * @see org.nhindirect.config.service.AnchorService#removeAnchorsForOwner(java.lang.String)
	 */
	@Override
	@FaultAction(className=ConfigurationFault.class)
	public void removeAnchorsForOwner(String owner)
			throws ConfigurationServiceException {
		anchorSvc.removeAnchorsForOwner(owner);
	}

	public DomainService getDomainSvc() {
		return domainSvc;
	}

	@Autowired
	public void setDomainSvc(DomainService domainSvc) {
		this.domainSvc = domainSvc;
	}

	public AddressService getAddressSvc() {
		return addressSvc;
	}

	@Autowired
	public void setAddressSvc(AddressService addressSvc) {
		this.addressSvc = addressSvc;
	}

	public CertificateService getCertSvc() {
		return certSvc;
	}

	@Autowired
	public void setCertSvc(CertificateService certSvc) {
		this.certSvc = certSvc;
	}

	public AnchorService getAnchorSvc() {
		return anchorSvc;
	}

	@Autowired
	public void setAnchorSvc(AnchorService anchorSvc) {
		this.anchorSvc = anchorSvc;
	}

	@Override
	@FaultAction(className=ConfigurationFault.class)
	public Certificate getCertificate(String owner, String thumbprint,
			CertificateGetOptions options) throws ConfigurationServiceException {
		return certSvc.getCertificate(owner, thumbprint, options);
	}

	@Override
	@FaultAction(className=ConfigurationFault.class)
	public Collection<Certificate> getCertificates(
			Collection<Long> certIds, CertificateGetOptions options)
			throws ConfigurationServiceException {
		return certSvc.getCertificates(certIds, options);
	}

	@Override
	@FaultAction(className=ConfigurationFault.class)
	public Collection<Certificate> getCertificatesForOwner(String owner,
			CertificateGetOptions options) throws ConfigurationServiceException {
		return certSvc.getCertificatesForOwner(owner, options);
	}

	@Override
	@FaultAction(className=ConfigurationFault.class)
	public Collection<Certificate> listCertificates(long lastCertificateId,
			int maxResults, CertificateGetOptions options)
			throws ConfigurationServiceException {
		return certSvc.listCertificates(lastCertificateId, maxResults, options);
	}

	@Override
	@FaultAction(className=ConfigurationFault.class)
	public Anchor getAnchor(String owner, String thumbprint,
			CertificateGetOptions options) throws ConfigurationServiceException {
		return anchorSvc.getAnchor(owner, thumbprint, options);
	}

	@Override
	@FaultAction(className=ConfigurationFault.class)
	public Collection<Anchor> getAnchors(Collection<Long> anchorIds,
			CertificateGetOptions options) throws ConfigurationServiceException {
		return anchorSvc.getAnchors(anchorIds, options);
	}

	@Override
	@FaultAction(className=ConfigurationFault.class)
	public Collection<Anchor> getAnchorsForOwner(String owner,
			CertificateGetOptions options) throws ConfigurationServiceException {
		return anchorSvc.getAnchorsForOwner(owner, options);
	}

	@Override
	@FaultAction(className=ConfigurationFault.class)
	public Collection<Anchor> getIncomingAnchors(String owner,
			CertificateGetOptions options) throws ConfigurationServiceException {
		return anchorSvc.getIncomingAnchors(owner, options);
	}

	@Override
	@FaultAction(className=ConfigurationFault.class)
	public Collection<Anchor> getOutgoingAnchors(String owner,
			CertificateGetOptions options) throws ConfigurationServiceException {
		return anchorSvc.getOutgoingAnchors(owner, options);
	}

	@Override
	@FaultAction(className=ConfigurationFault.class)
	public Collection<Anchor> listAnchors(Long lastAnchorID, int maxResults,
			CertificateGetOptions options) throws ConfigurationServiceException {
		return anchorSvc.listAnchors(lastAnchorID, maxResults, options);
	}

}
