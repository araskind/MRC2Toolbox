/*******************************************************************************
 * 
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Contributors:
 * Alexander Raskind (araskind@med.umich.edu)
 *  
 ******************************************************************************/

package edu.umich.med.mrc2.datoolbox.data.lims;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.openscience.cdk.interfaces.IChemModel;

import edu.umich.med.mrc2.datoolbox.data.AnnotatedObject;
import edu.umich.med.mrc2.datoolbox.data.enums.AnnotatedObjectType;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.DocumentFormat;

public class ObjectAnnotation implements Comparable<ObjectAnnotation>, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 2504173546662713324L;

	private String uniqueId;
	private AnnotatedObjectType annotatedObjectType;
	private String annotatedObjectId;
	private String doi;
	private Date dateCreated;
	private Date lastModified;
	private LIMSUser createBy;
	private LIMSUser lastModifiedBy;
	private Document rtfDocument;
	private String linkedDocumentId;
	private String linkedDocumentName;
	private DocumentFormat linkedDocumentFormat;
	private IChemModel chemModel;
	private String chemModelNotes;
	private File linkedDocumentFile;
	
	/**
	 * Instantiate Annotation with linked document from values stored in the database
	 */
	public ObjectAnnotation(
			String uniqueId, 
			AnnotatedObjectType annotatedObjectType, 
			String annotatedObjectId,
			Date dateCreated, 
			Date lastModified, 
			LIMSUser createBy, 
			LIMSUser lastModifiedBy,
			String linkedDocumentId) {
		super();
		this.uniqueId = uniqueId;
		this.annotatedObjectType = annotatedObjectType;
		this.annotatedObjectId = annotatedObjectId;
		this.dateCreated = dateCreated;
		this.lastModified = lastModified;
		this.createBy = createBy;
		this.lastModifiedBy = lastModifiedBy;
		this.rtfDocument = null;
		this.chemModel = null;
		this.linkedDocumentId = linkedDocumentId;
	}

	/**
	 * Create new document-based annotation
	 * Needs database document ID, so document should be already uploaded to  database
	 */
	public ObjectAnnotation(
			AnnotatedObjectType annotatedObjectType,
			String annotatedObjectId,
			String documentId,
			LIMSUser createBy) {
		super();
		
		//	Replaced by sequence-based ID when inserted in the database
		//	Is necessary to calculate hash value
		uniqueId = DataPrefix.ANNOTATION.getName() + UUID.randomUUID().toString();
		
		this.annotatedObjectType = annotatedObjectType;
		this.annotatedObjectId = annotatedObjectId;
		this.linkedDocumentId = documentId;
		this.createBy = createBy;
		this.lastModifiedBy = createBy;
		this.dateCreated = new Date();
		this.lastModified = new Date();
		this.chemModel = null;
	}
	
	/**
	 * Create new empty annotation
	 */
	public ObjectAnnotation(
			AnnotatedObject annotatedObject,
			LIMSUser createBy) {
		super();
		
		//	Replaced by sequence-based ID when inserted in the database
		//	Is necessary to calculate hash value
		uniqueId = DataPrefix.ANNOTATION.getName() + UUID.randomUUID().toString();
		
		this.annotatedObjectType = annotatedObject.getAnnotatedObjectType();
		this.annotatedObjectId = annotatedObject.getId();
		this.createBy = createBy;
		this.lastModifiedBy = createBy;
		this.dateCreated = new Date();
		this.lastModified = new Date();
		this.rtfDocument = null;
		this.chemModel = null;
	}

	@Override
	public int compareTo(ObjectAnnotation o) {
		return uniqueId.compareTo(o.getUniqueId());
	}

    @Override
    public boolean equals(Object obj) {

        if (obj == null)
            return false;
        
		if (obj == this)
			return true;

        if (!ObjectAnnotation.class.isAssignableFrom(obj.getClass()))
            return false;

        final ObjectAnnotation other = (ObjectAnnotation) obj;

        if ((this.uniqueId == null) ? (other.getUniqueId() != null) : !this.uniqueId.equals(other.getUniqueId()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.uniqueId != null ? this.uniqueId.hashCode() : 0);
        return hash;
    }

	/**
	 * @return the uniqueId
	 */
	public String getUniqueId() {
		return uniqueId;
	}

	/**
	 * @return the rtfDocument
	 */
	public Document getRtfDocument() {
		return rtfDocument;
	}

	/**
	 * @param rtfDocument the rtfDocument to set
	 */
	public void setRtfDocument(Document rtfDocument) {
		this.rtfDocument = rtfDocument;
	}

	/**
	 * @return the annotatedObjectType
	 */
	public AnnotatedObjectType getAnnotatedObjectType() {
		return annotatedObjectType;
	}

	/**
	 * @return the annotatedObjectId
	 */
	public String getAnnotatedObjectId() {
		return annotatedObjectId;
	}

	/**
	 * @return the doi
	 */
	public String getDoi() {
		return doi;
	}

	/**
	 * @return the dateCreated
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @return the lastModified
	 */
	public Date getLastModified() {
		return lastModified;
	}

	/**
	 * @return the createBy
	 */
	public LIMSUser getCreateBy() {
		return createBy;
	}

	/**
	 * @return the lastModifiedBy
	 */
	public LIMSUser getLastModifiedBy() {
		return lastModifiedBy;
	}

	/**
	 * @return the linkedDocumentId
	 */
	public String getLinkedDocumentId() {
		return linkedDocumentId;
	}

	/**
	 * @return the linkedDocumentName
	 */
	public String getLinkedDocumentName() {
		return linkedDocumentName;
	}

	/**
	 * @return the linkedDocumentFormat
	 */
	public DocumentFormat getLinkedDocumentFormat() {
		return linkedDocumentFormat;
	}

	/**
	 * @param doi the doi to set
	 */
	public void setDoi(String doi) {
		this.doi = doi;
	}

	/**
	 * @param lastModified the lastModified to set
	 */
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	/**
	 * @param lastModifiedBy the lastModifiedBy to set
	 */
	public void setLastModifiedBy(LIMSUser lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	/**
	 * @param linkedDocumentId the linkedDocumentId to set
	 */
	public void setLinkedDocumentId(String linkedDocumentId) {
		this.linkedDocumentId = linkedDocumentId;
	}

	/**
	 * @param linkedDocumentName the linkedDocumentName to set
	 */
	public void setLinkedDocumentName(String linkedDocumentName) {
		this.linkedDocumentName = linkedDocumentName;
	}

	/**
	 * @param linkedDocumentFormat the linkedDocumentFormat to set
	 */
	public void setLinkedDocumentFormat(DocumentFormat linkedDocumentFormat) {
		this.linkedDocumentFormat = linkedDocumentFormat;
	}
	
	/**
	 * Set maxLength to negative number to get complete text
	 * @param maxLength
	 * @return
	 */
	public String getText(int maxLength) {
		
		String text = "";
		String trailing = "";
		if(rtfDocument != null) {
		
			int length = rtfDocument.getLength();
			if(length > maxLength && maxLength > 0) {
				length = maxLength;
				trailing = " ...";
			}			
			try {
				text = rtfDocument.getText(0, length);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return text + trailing;
		}
		if(linkedDocumentId != null)
			return linkedDocumentName;
		
		return text;
	}

	/**
	 * @param uniqueId the uniqueId to set
	 */
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	/**
	 * @return the chemModel
	 */
	public IChemModel getChemModel() {
		return chemModel;
	}

	/**
	 * @param chemModel the chemModel to set
	 */
	public void setChemModel(IChemModel chemModel) {
		this.chemModel = chemModel;
	}
	public boolean isEmpty() {
		
		if(rtfDocument != null)
			return false;
		
		if(chemModel != null)
			return false;
		
		if(linkedDocumentId != null)
			return false;
		
		return true;
	}

	/**
	 * @return the chemModelNotes
	 */
	public String getChemModelNotes() {
		return chemModelNotes;
	}

	/**
	 * @param chemModelNotes the chemModelNotes to set
	 */
	public void setChemModelNotes(String chemModelNotes) {
		this.chemModelNotes = chemModelNotes;
	}

	public File getLinkedDocumentFile() {
		return linkedDocumentFile;
	}

	public void setLinkedDocumentFile(File linkedDocumentFile) {
		this.linkedDocumentFile = linkedDocumentFile;
	}

	public void setAnnotatedObjectId(String annotatedObjectId) {
		this.annotatedObjectId = annotatedObjectId;
	}
}



















