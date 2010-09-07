/* 
 * Copyright (c) 2010, NHIN Direct Project
 * All rights reserved.
 *  
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in the 
 *    documentation and/or other materials provided with the distribution.  
 * 3. Neither the name of the the NHIN Direct Project (nhindirect.org)
 *    nor the names of its contributors may be used to endorse or promote products 
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY 
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND 
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.nhind.mail.service;

import ihe.iti.xds_b._2007.DocumentRepositoryPortType;
import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType;
import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType.Document;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import oasis.names.tc.ebxml_regrep.xsd.lcm._3.SubmitObjectsRequest;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.AssociationType1;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.ClassificationType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.ExternalIdentifierType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.ExtrinsicObjectType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.InternationalStringType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.LocalizedStringType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.RegistryObjectListType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.RegistryPackageType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.SlotType1;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.ValueListType;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;

import org.apache.commons.lang.StringUtils;
import org.nhind.ccddb.CCDParser;
import org.nhind.mail.util.DocumentRepositoryUtils;
import org.nhind.mail.util.MimeType;

/*
 * TODO there is a major assumption within this class and underlying classes.  That is the assumption of single document. 
 * This means that a message body (not attachment) has one document, an attachment has one document or one XDM and an XDM 
 * has one document.
 * Obviously this needs to be enhanced per the appropriate specs.
 * Also, for XDR purposes there is an assumption that the doc is CDA if its body or attached. XDM does not make this assumption.
 * As we go to XDD , this will be easy to fix. 
 * Thanks , Vince Lewis
 */

/**
 * Transform a MimeMessage into a XDR request.
 * 
 * @author vlewis
 */
public class MimeXDSTransformer {
    
    private static final Logger LOGGER = Logger.getLogger(MimeXDSTransformer.class.getName());

    /*
     * private static final String DEFAULT_LANGUAGE_CODE  = "en-us"; // TODO default from Locale
     * private static final String RANDOM_OID_ROOT        = "1.3.6.1.4.1.21367.3100.1.2.3";
     * private static final String NHINDIRECT_MESSAGE_ID_ASSIGNING_AUTHORITY_NAME = "NHINDirect";
     * private static final String NHINDIRECT_ADDR_OID    = "1.3.6.1.4.1.21367.3100.1";
     * private static final String NHINDIRECT_MESSAGE_ID_ASSIGNING_AUTHORITY_I  D = "1.3.6.1.4.1.21367.3100.1.1";
     * private static final String NHINDIRECT_MESSAGE_METADATA_CODESYSTEM_ID      = "1.3.6.1.4.1.21367.3100.1.2";
     * private static final String CODE_UNSPECIFIED       = "Unspecified";
     * private static final String CODE_CLINICALDATA      = "Clinical Data";
     * private static final String CODE_CONTENT_TYPE      = "Communication";
     * private static final String CODE_CONFIDENTIALITY   = "N";
     * private static final String CODE_FORMAT_CCRV1      = "CCR V1.0";
     * private static final String CODE_FORMAT_PDF        = "PDF";
     */
    
    private static final String CODE_FORMAT_TEXT = "TEXT";
    private static final String CODE_FORMAT_CDAR2 = "CDAR2/IHE 1.0";
    
    protected String endpoint = null;
    protected String messageId = null;
    protected String relatesTo = null;
    protected String action = null;
    protected String to = null;
    
    private String thisHost = null;
    private String remoteHost = null;
    private String pid = null;
    private String from = null;
    
//    private String suffix = null;
//    private String replyEmail = null;

    /**
     * Entry point for the MimeXDSTransformer class. This will forward a given
     * MimeMessage to the given XDR endpoint.
     * 
     * @param endpoint
     *            A URL representing an XDR endpoint.
     * @param mimeMessage
     *            The MimeMessage object to transform.
     * @throws Exception
     */
    public void forward(String endpoint, MimeMessage mimeMessage) throws Exception {
        if (StringUtils.isBlank(endpoint))
            throw new IllegalArgumentException("Endpoint must not be blank");
        if (mimeMessage == null)
            throw new IllegalArgumentException("MimeMessage must not be null");
        
        ProvideAndRegisterDocumentSetRequestType prds = createRequest(mimeMessage);
        forwardRequest(endpoint, prds);
    }

    /**
     * Forward a given ProvideAndRegisterDocumentSetRequestType object to the
     * given XDR endpoint.
     * 
     * @param endpoint
     *            A URL representing an XDR endpoint.
     * @param prds
     *            The ProvideAndRegisterDocumentSetRequestType object.
     * @throws Exception
     */
    protected void forwardRequest(String endpoint, ProvideAndRegisterDocumentSetRequestType prds) throws Exception {
        if (StringUtils.isBlank(endpoint))
            throw new IllegalArgumentException("Endpoint must not be blank");
        if (prds == null)
            throw new IllegalArgumentException("ProvideAndRegisterDocumentSetRequestType must not be null");
        
        LOGGER.info(" SENDING TO ENDPOINT " + endpoint);
        
        this.relatesTo = this.messageId;
        this.action = "urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-bResponse";
        this.messageId = UUID.randomUUID().toString();
        this.to = endpoint;
        
        setHeaderData();
        
        DocumentRepositoryPortType port = null;
        
        try {
            port = DocumentRepositoryUtils.getDocumentRepositoryPortType(endpoint);
        } catch (Exception e) {
            LOGGER.warning("Unable to create port");
            e.printStackTrace();
            throw e;
        }
        
        RegistryResponseType rrt = port.documentRepositoryProvideAndRegisterDocumentSetB(prds);
        
        String test = rrt.getStatus();
        
        if (StringUtils.contains(test, "Failure")) {
            throw new Exception("Failure Returned from XDR forward");
        }
        
        LOGGER.info("Handling complete");
    }

    /**
     * Transform a MimeMessage object into a
     * ProvideAndRegisterDocumentSetRequestType object.
     * 
     * @param mimeMessage
     *            The MimeMessage to be transformed.
     * @return a ProvideAndRegisterDocumentSetRequestType object.
     */
    private ProvideAndRegisterDocumentSetRequestType createRequest(MimeMessage mimeMessage) {
        ProvideAndRegisterDocumentSetRequestType prsr = null;

        try {
            Date sentDate = null;

            Address[] froms = null;
            Address[] recips = null;
            
            byte[] xdsDocument = null;
            
            String subject = null;
            String xdsMimeType = null;
            String xdsFormatCode = null;
            String msgContentType = null;
            
            froms = mimeMessage.getFrom();
            subject = mimeMessage.getSubject();
            sentDate = mimeMessage.getSentDate();
            recips = mimeMessage.getAllRecipients();
            msgContentType = mimeMessage.getContentType();
            
            String auth = froms[0].toString(); // TODO one from for now
            String recip = recips[0].toString(); // TODO one recipient for now

            this.messageId = mimeMessage.getMessageID();
            
            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Message content type: " + msgContentType);
            if (StringUtils.startsWith(msgContentType, "multipart/mixed")) {
                LOGGER.info("Handling multipart/mixed");

                MimeMultipart mimeMultipart = (MimeMultipart) mimeMessage.getContent();

                /*
                 * Grab any CDA document attachments and add them to the
                 * submission
                 */
                for (int i = 0; i < mimeMultipart.getCount(); i++) {
                    BodyPart bodyPart = mimeMultipart.getBodyPart(i);
                    
                    String contentType = bodyPart.getContentType();
                    
                    if (LOGGER.isLoggable(Level.INFO))
                        LOGGER.info("BodyPart size: " + bodyPart.getSize());
                    if (bodyPart.getSize() > 0) {
                        String fname = bodyPart.getFileName();
                        
                        if (StringUtils.isBlank(fname) || StringUtils.equalsIgnoreCase(fname, "null")) {
                            continue;
                        }
                        
                        if (LOGGER.isLoggable(Level.INFO))
                            LOGGER.info("File name: " + fname);
                        if (StringUtils.contains(fname, ".zip")) {
                            try {
                                prsr = getXDMRequest(bodyPart);
                                 LOGGER.info("returning XDM based request");
                                return prsr;
                            } catch (Exception x) {
                                // TODO, probably not an XDM
                            }
                        }

                        InputStream is = bodyPart.getInputStream();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();

                        int j = 0;
                        byte[] buf = new byte[1024];
                        
                        while ((j = is.read(buf)) != -1) {
                            baos.write(buf, 0, j);
                        }
                        
                        String contentString = new String(baos.toByteArray());

                        try {
                            // special handling for recognized content types
                            if (MimeType.TEXT_PLAIN.matches(contentType)) {
                                if (StringUtils.isBlank(contentString)) {
                                    continue; // skip 'empty' parts
                                }
                                
                                xdsDocument = baos.toByteArray();
                                xdsMimeType = MimeType.TEXT_PLAIN.getType();
                                xdsFormatCode = CODE_FORMAT_TEXT;
                            } else if (MimeType.TEXT_XML.matches(contentType)) {
                                if (StringUtils.contains(contentString, "urn:hl7-org:v3")
                                        && StringUtils.contains(contentString, "POCD_HD000040")) {
                                    // CDA R2
                                    xdsFormatCode = CODE_FORMAT_CDAR2;
                                } else {
                                    // Other XML (possible CCR or HL7)
                                    // TODO: support more XML types
                                    xdsFormatCode = CODE_FORMAT_TEXT;
                                }
                                
                                xdsDocument = baos.toByteArray();
                                xdsMimeType = MimeType.TEXT_XML.getType();
                            } else {
                                // Otherwise make best effort passing MIME content type thru
                                xdsMimeType = contentType;
                                xdsFormatCode = CODE_FORMAT_TEXT;
                            }
                        }  finally {
                            is.close();
                        }
                    }
                }
            } else if (MimeType.TEXT_PLAIN.matches(msgContentType)) {
                // support for plain mail, no attachments
                xdsMimeType = MimeType.TEXT_PLAIN.getType();
                xdsFormatCode = CODE_FORMAT_TEXT;
                xdsDocument = ((String) mimeMessage.getContent()).getBytes();
            } else {
                // form prsr                
                throw new MessagingException("Message content type " + msgContentType + " is not supported.");
            }
            
            prsr = getRequest(subject, sentDate, xdsFormatCode, xdsMimeType, xdsDocument, auth, recip);
        } catch (Exception x) {
            // TODO
        }

        return prsr;
    }

    /**
     * Get an XDM Request from a BodyPart object.
     * 
     * @param bodyPart
     *            The BodyPart object containing the XDM request.
     * @return a ProvideAndRegisterDocumentSetRequestType object.
     * @throws Exception
     */
    protected static ProvideAndRegisterDocumentSetRequestType getXDMRequest(BodyPart bodyPart) throws Exception {
        LOGGER.info("Inside getMDMRequest");
        
        DataHandler dh = bodyPart.getDataHandler();
        
        XDMXDSTransformer xxt = new XDMXDSTransformer();
        ProvideAndRegisterDocumentSetRequestType prsr = xxt.getXDMRequest(dh);
        
        return prsr;
    }

    /**
     * @param subject
     * @param sentDate
     * @param formatCode
     * @param mimeType
     * @param doc
     * @param auth
     * @param recip
     * @return
     * @throws Exception
     */
    protected static ProvideAndRegisterDocumentSetRequestType getRequest(String subject, Date sentDate, String formatCode,
            String mimeType, byte[] doc, String auth, String recip) throws Exception {
        ProvideAndRegisterDocumentSetRequestType prsr = new ProvideAndRegisterDocumentSetRequestType();
        
        String sdoc = new String(doc);
        CCDParser cp = new CCDParser();
        cp.parseCCD(sdoc);
        
        String patientId = cp.getPatientId();
        String orgId = cp.getOrgId();
        String date = formatDate(sentDate);
        String subId = UUID.randomUUID().toString();
        String docId = UUID.randomUUID().toString();
        SimplePerson sp = null;

        SubmitObjectsRequest sor = getSubmitObjectsRequest(patientId, orgId, sp, subject, date, docId, subId,
                formatCode, mimeType, auth, recip);
        prsr.setSubmitObjectsRequest(sor);
        
        DataSource source = new ByteArrayDataSource(doc, MimeType.APPLICATION_XML.getType() + "; charset=UTF-8");
        DataHandler dhnew = new DataHandler(source);

        List<Document> docs = prsr.getDocument();
        Document pdoc = new Document();
        
        pdoc.setValue(dhnew);
        pdoc.setId(docId);
        docs.add(pdoc);

        return prsr;
    }

    /**
     * @param patientId
     * @param orgId
     * @param person
     * @param subject
     * @param sentDate
     * @param docId
     * @param subId
     * @param formatCode
     * @param mimeType
     * @param auth
     * @param recip
     * @return
     */
    protected static SubmitObjectsRequest getSubmitObjectsRequest(String patientId, String orgId, SimplePerson person,
            String subject, String sentDate, String docId, String subId, String formatCode, String mimeType,
            String auth, String recip) {
        SubmitObjectsRequest req = new SubmitObjectsRequest();
        RegistryObjectListType rolt = new RegistryObjectListType();

        ExtrinsicObjectType eot = getExtrinsicObject(patientId, orgId, person, sentDate, docId, formatCode, mimeType, auth);
        QName qname = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "ExtrinsicObject");
        JAXBElement eotj = new JAXBElement(qname, ExtrinsicObjectType.class, eot);

        RegistryPackageType rpt = getSubmissionSet(patientId, orgId, subject, sentDate, subId, auth, recip);
        qname = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "RegistryPackage");
        JAXBElement rptj = new JAXBElement(qname, RegistryPackageType.class, rpt);

        ClassificationType clas = getClassification(rpt.getId());
        qname = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Classification");
        JAXBElement clasj = new JAXBElement(qname, ClassificationType.class, clas);

        AssociationType1 ass = getAssociation(rpt.getId(), eot.getId());
        qname = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Association");
        JAXBElement assj = new JAXBElement(qname, AssociationType1.class, ass);

        List elems = rolt.getIdentifiable();
        elems.add(eotj);
        elems.add(rptj);
        elems.add(clasj);
        elems.add(assj);

        req.setRegistryObjectList(rolt);

        return req;
    }

    /**
     * @param patientId
     * @param orgId
     * @param person
     * @param sentDate
     * @param docId
     * @param formatCode
     * @param mimeType
     * @param auth
     * @return
     */
    protected static ExtrinsicObjectType getExtrinsicObject(String patientId, String orgId, SimplePerson person,
            String sentDate, String docId, String formatCode, String mimeType, String auth) {
        List<String> snames = null;
        List<String> slotNames = null;
        List<String> slotValues = null;
        List<SlotType1> slots = null;
        List<ClassificationType> classifs = null;
        List<ExternalIdentifierType> extIds = null;
        
        ExtrinsicObjectType document = null;
        
        final String obType = "urn:uuid:7edca82f-054d-47f2-a032-9b2a5b5186c1";

        document = new ExtrinsicObjectType();
        document.setId(docId);
        document.setObjectType(obType);
        document.setMimeType(mimeType);
               
        slots = document.getSlot();
        extIds = document.getExternalIdentifier();
        classifs = document.getClassification();
        
        slots.add(makeSlot("creationTime", sentDate));
        
        // slots.add(makeSlot("serviceStartTime", formatDate(new Date())));
        // GregorianCalendar gd = new GregorianCalendar();
        // gd.add(gd.YEAR, 100);
        // slots.add(makeSlot("serviceStopTime", formatDate(gd.getTime())));
        
        slots.add(makeSlot("sourcePatientId", patientId + "^^^&" + orgId));
        
        if (person != null) {
            slots.add(makePatientSlot("sourcePatientInfo", person, patientId, orgId));
        }

        snames = new ArrayList<String>();
        slotNames = new ArrayList<String>();
        slotValues = new ArrayList<String>();
        
        if (auth != null) {
            snames.add(null);
            slotNames.add("authorPerson");
            slotValues.add(auth);
        }
        
        snames.add(null);
        slotNames.add("authorInstitution");
        slotValues.add(orgId);
        
        snames.add(null);
        slotNames.add("authorRole");
        if (auth != null)
            slotValues.add(auth + "'s Role");// see if we need this
        else
            slotValues.add("System");

        addClassifications(classifs, docId, "c101", "urn:uuid:93606bcf-9494-43ec-9b4e-a7748d1a838d", null, slotNames,
                slotValues, snames);
        
        snames = Arrays.asList("History and Physical");
        slotNames = Arrays.asList("codingScheme");
        slotValues = Arrays.asList("Connect-a-thon classCodes");
        
        addClassifications(classifs, docId, "c102", "uuid:41a5887f-8865-4c09-adf7-e362475b143a",
                "History and Physical", slotNames, slotValues, snames);
        
        snames = Arrays.asList(formatCode);
        slotNames = Arrays.asList("codingScheme");
        slotValues = Arrays.asList("Connect-a-thon formatCodes");

        addClassifications(classifs, docId, "c104", "urn:uuid:a09d5840-386c-46f2-b5ad-9c3699a4309d", formatCode,
                slotNames, slotValues, snames);
        
        snames = Arrays.asList("OF");
        slotNames = Arrays.asList("codingScheme");
        slotValues = Arrays.asList("Connect-a-thon healthcareFacilityTypeCodes");

        addClassifications(classifs, docId, "c105", "urn:uuid:f33fb8ac-18af-42cc-ae0e-ed0b0bdb91e1", "OF", slotNames,
                slotValues, snames);
        
        snames = Arrays.asList("Multidisciplinary");
        slotNames = Arrays.asList("codingScheme");
        slotValues = Arrays.asList("Connect-a-thon practiceSettingCodes");        

        addClassifications(classifs, docId, "c106", "urn:uuid:cccf5598-8b07-4b77-a05e-ae952c785ead",
                "Multidisciplinary", slotNames, slotValues, snames);
        
        snames = Arrays.asList("34133-9");
        slotNames = Arrays.asList("codingScheme");
        slotValues = Arrays.asList("LOINC");    
        
        addClassifications(classifs, docId, "c107", "urn:uuid:f0306f51-975f-434e-a61c-c59651d33983", "34133-9",
                slotNames, slotValues, snames);

        addExternalIds(extIds, docId, "urn:uuid:58a6f841-87b3-4a3e-92fd-a8ffeff98427", "ei01",
                "XDSDocumentEntry.patientId", patientId + "^^^&" + orgId);
        addExternalIds(extIds, docId, "urn:uuid:2e82c1f6-a085-4c72-9da3-8640a32e42ab", "ei02",
                "XDSDocumentEntry.uniqueId", docId);

        return document;
    }

    /**
     * @param patientId
     * @param orgId
     * @param subject
     * @param sentDate
     * @param subId
     * @param auth
     * @param recip
     * @return
     */
    protected static RegistryPackageType getSubmissionSet(String patientId, String orgId, String subject, String sentDate,
            String subId, String auth, String recip) {
        List<String> snames = null;
        List<String> slotNames = null;
        List<String> slotValues = null;
        List<SlotType1> slots = null;
        List<ClassificationType> classifs = null;
        List<ExternalIdentifierType> extIds = null;
        
        RegistryPackageType subset = new RegistryPackageType();

        final String obType = "urn:uuid:7edca82f-054d-47f2-a032-9b2a5b5186c1";

        subset.setId(subId);
        subset.setObjectType(obType);
        
        slots = subset.getSlot();
        extIds = subset.getExternalIdentifier();
        classifs = subset.getClassification();

        slots.add(makeSlot("submissionTime", sentDate));
        String intendedRecipient = "|" + recip + "^last^first^^^prefix^^^&amp;1.3.6.1.4.1.21367.3100.1&amp;ISO";
        slots.add(makeSlot("intendedRecipient", intendedRecipient));

        snames = new ArrayList<String>();
        slotNames = new ArrayList<String>();
        slotValues = new ArrayList<String>();

        if (auth != null) {
            snames.add(null);
            slotNames.add("authorPerson");
            slotValues.add(auth);
        }
        
        snames.add(null);
        slotNames.add("authorInstitution");
        slotValues.add(orgId);
        
        snames.add(null);
        slotNames.add("authorRole");
        if (auth != null)
            slotValues.add(auth + "'s Role");// see if we need this
        else 
            slotValues.add("System");

        addClassifications(classifs, subId, "c101", "urn:uuid:a7058bb9-b4e4-4307-ba5b-e3f0ab85e12d", null, slotNames,
                slotValues, snames);

        snames = Arrays.asList(subject);
        slotNames = Arrays.asList("codingScheme");
        slotValues = Arrays.asList("Connect-a-thon contentTypeCodes");
        
        addClassifications(classifs, subId, "c102", "urn:uuid:aa543740-bdda-424e-8c96-df4873be8500", subject,
                slotNames, slotValues, snames);

        addExternalIds(extIds, subId, "urn:uuid:96fdda7c-d067-4183-912e-bf5ee74998a8", "ei01",
                "XDSSubmissionSet.uniqueId", subId);
        addExternalIds(extIds, subId, "urn:uuid:554ac39e-e3fe-47fe-b233-965d2a147832", "ei02",
                "XDSSubmissionSet.sourceId", orgId);
        addExternalIds(extIds, subId, "urn:uuid:6b5aea1a-874d-4603-a4bc-96a0a7b38446", "ei03",
                "XDSSubmissionSet.patientId", patientId + "^^^&" + orgId);

        return subset;
    }

    /**
     * Create a ClassificationType object using the given ID.
     * 
     * @param setId
     *            The ID to set within the ClassificationType object.
     * @return a ClassificationType object with the given ID.
     */
    protected static ClassificationType getClassification(String setId) {
        ClassificationType ct = new ClassificationType();
        ct.setClassificationNode("urn:uuid:a54d6aa5-d40d-43f9-88c5-b4633d873bdd");
        ct.setClassifiedObject(setId);

        return ct;
    }

    /**
     * Create an AssociationType1 object using the given source object ID and
     * document ID.
     * 
     * @param setId
     *            The source object ID.
     * @param docId
     *            The target object ID.
     * @return an AssociationType1 object with the given source object ID and
     *         document ID.
     */
    protected static AssociationType1 getAssociation(String setId, String docId) {
        AssociationType1 at = new AssociationType1();
        at.setAssociationType("HasMember");
        at.setSourceObject(setId);
        at.setTargetObject(docId);
        
        List<SlotType1> slots = at.getSlot();
        slots.add(makeSlot("SubmissionSetStatus", "Original"));
        
        return at;
    }

    /**
     * Create a SlotType1 object using the provided patient information.
     * 
     * @param name
     *            The slot name.
     * @param patient
     *            The SimplePerson object representing a patient.
     * @param patientId
     *            The patient ID.
     * @param orgId
     *            The organization ID.
     * @return a SlotType1 object containing the provided patient data.
     */
    protected static SlotType1 makePatientSlot(String name, SimplePerson patient, String patientId, String orgId) {
        List<String> vals = null;
        SlotType1 slot = new SlotType1();
        ValueListType values = new ValueListType();
        
        slot.setName(name);
        slot.setValueList(values);
        vals = values.getValue();

        /*
         * TODO: What should happen if patient is null?
         */
        if (patient != null) {
            StringBuffer sb = null;

            // <rim:Value>PID-3|pid1^^^domain</rim:Value>
            sb = new StringBuffer("PID-3|");
            sb.append(patientId);
            sb.append("^^^&amp;");
            sb.append(orgId);
            sb.append("&amp;ISO");
            vals.add(sb.toString());

            // <rim:Value>PID-5|Doe^John^^^</rim:Value>
            sb = new StringBuffer("PID-5|");
            sb.append(patient.getLastName());
            sb.append("^");
            sb.append(patient.getFirstName());
            sb.append("^");
            sb.append(patient.getMiddleName());
            vals.add(sb.toString());

            // <rim:Value>PID-7|19560527</rim:Value>
            sb = new StringBuffer("PID-7|");
            sb.append(formatDateFromMDM(patient.getBirthDateTime()));
            vals.add(sb.toString());

            // <rim:Value>PID-8|M</rim:Value>
            sb = new StringBuffer("PID-8|");
            sb.append(patient.getGenderCode());
            vals.add(sb.toString());

            // <rim:Value>PID-11|100 Main St^^Metropolis^Il^44130^USA</rim:Value>
            sb = new StringBuffer("PID-11|");
            sb.append(patient.getStreetAddress1());
            sb.append("^");
            sb.append(patient.getCity());
            sb.append("^^");
            sb.append(patient.getState());
            sb.append("^");
            sb.append(patient.getZipCode());
            sb.append("^");
            sb.append(patient.getCountry());
            vals.add(sb.toString());
        }

        return slot;
    }

    /**
     * @param classifs
     * @param docId
     * @param id
     * @param scheme
     * @param rep
     * @param slotNames
     * @param slotValues
     * @param snames
     */
    protected static void addClassifications(List<ClassificationType> classifs, String docId, String id, String scheme,
            String rep, List<String> slotNames, List<String> slotValues, List<String> snames) {
        if (classifs == null) {
            throw new IllegalArgumentException("Must include a live reference to a ClassificationType list");
        }
        
        ClassificationType ct = new ClassificationType();
        
        classifs.add(ct);
        ct.setClassifiedObject(docId);
        ct.setClassificationScheme(scheme);
        ct.setId(id);
        ct.setNodeRepresentation(rep);
        
        List<SlotType1> slots = ct.getSlot();
        Iterator is = slotNames.iterator();
        
        int i = 0;
        while (is.hasNext()) {
            String slotName = (String) is.next();
            SlotType1 slot = makeSlot(slotName, (String) slotValues.get(i));
            slots.add(slot);

            String sname = (String) snames.get(i);
            if (sname != null) {
                InternationalStringType name = new InternationalStringType();
                List<LocalizedStringType> names = name.getLocalizedString();
                LocalizedStringType lname = new LocalizedStringType();
                lname.setValue(sname);
                names.add(lname);
                ct.setName(name);
            }
            
            i++;
        }

    }

    /**
     * @param extIds
     * @param docId
     * @param scheme
     * @param id
     * @param sname
     * @param value
     */
    protected static void addExternalIds(List<ExternalIdentifierType> extIds, String docId, String scheme, String id,
            String sname, String value) {
        if (extIds == null)
            throw new IllegalArgumentException("Must include a live reference to an ExternalIdentifierType list");
        
        ExternalIdentifierType ei = new ExternalIdentifierType();
        
        extIds.add(ei);
        ei.setRegistryObject(docId);
        ei.setIdentificationScheme(scheme);
        ei.setId(id);

        if (StringUtils.isNotBlank(sname)) {
            InternationalStringType name = new InternationalStringType();
            List<LocalizedStringType> names = name.getLocalizedString();
            LocalizedStringType lname = new LocalizedStringType();
            lname.setValue(sname);
            names.add(lname);
            ei.setName(name);
        }
        
        ei.setValue(value);
    }

    /**
     * @param dateVal
     * @return
     */
    protected static String formatDate(Date dateVal) {
        final String formout = "yyyyMMddHHmmss";

        String ret = null;
        SimpleDateFormat dateOut = new SimpleDateFormat(formout);
        
        try {
            ret = dateOut.format(dateVal);
        } catch (Exception x) {
            x.printStackTrace();
        }
        
        return ret;
    }

    /**
     * @param value
     * @return
     */
    protected static String formatDateFromMDM(String value) {
        final String formin = "MM/dd/yyyy";
        final String formout = "yyyyMMddHHmmss";
        
        String ret = value;

        if (StringUtils.contains(value, "+")) {
            value = value.substring(0, value.indexOf("+"));
        }

        Date dateVal = null;
        SimpleDateFormat date = new SimpleDateFormat(formin);
        SimpleDateFormat dateOut = new SimpleDateFormat(formout);
        
        try {
            dateVal = date.parse(value);
            ret = dateOut.format(dateVal);
        } catch (Exception x) {
            x.printStackTrace();
        }
        
        return ret;
    }

    /**
     * Create a SlotType1 object using the given name and value.
     * 
     * @param name
     *            The slot name.
     * @param value
     *            The slot value.
     * @return a SlotType1 object.
     */
    protected static SlotType1 makeSlot(String name, String value) {
        SlotType1 slot = new SlotType1();
        slot.setName(name);
        ValueListType values = new ValueListType();
        slot.setValueList(values);
        List<String> vals = values.getValue();
        vals.add(value);
        
        return slot;
    }

    /**
     * Set header data.
     * 
     * TODO: Investigate the usefulness of this method. It sets null known null
     * values.
     */
    protected void setHeaderData() {
        Long threadId = Long.valueOf(Thread.currentThread().getId());
        LOGGER.info("THREAD ID " + threadId);

        ThreadData threadData = new ThreadData(threadId);
        threadData.setTo(this.to);
        threadData.setMessageId(this.messageId);
        threadData.setRelatesTo(this.relatesTo);
        threadData.setAction(this.action);
        threadData.setThisHost(this.thisHost);
        threadData.setRemoteHost(this.remoteHost);
        threadData.setPid(this.pid);
        threadData.setFrom(this.from);

        LOGGER.info(threadData.toString());
    }
    
}