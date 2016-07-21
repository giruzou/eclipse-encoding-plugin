package mergedoc.encoding;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.editors.text.IEncodingSupport;

/**
 * This document handles editors which support IEncodingSupport for ActiveDocumentAgent.
 * @author Tsoi Yat Shing
 * @author Shinji Kashihara
 */
class ActiveDocument {

	protected IActiveDocumentAgentCallback callback;
	protected IEditorPart editor;
	protected IEncodingSupport encodingSupport;
	protected String currentEncoding;
	protected String inheritedEncoding;
	protected String detectedEncoding;
	protected String contentTypeEncoding;
	protected String lineSeparator;

	public ActiveDocument(IEditorPart editor, IActiveDocumentAgentCallback callback) {
		init(editor, callback);
	}

	protected void init(IEditorPart editor, IActiveDocumentAgentCallback callback) {

		this.editor = editor;
		this.callback = callback;
		if (editor == null) throw new IllegalArgumentException("editor must not be null.");
		if (callback == null) throw new IllegalArgumentException("callback must not be null.");

		this.encodingSupport = (IEncodingSupport) editor.getAdapter(IEncodingSupport.class);
		if (encodingSupport == null) throw new IllegalArgumentException("editor must provide IEncodingSupport.");

		updateEncodingInfoPrivately();
	}

	/**
	 * Get the editor associated with this handler.
	 * If the associated editor is different from the active editor, ActiveDocumentAgent will change handler.
	 */
	public IEditorPart getEditor() {
		return editor;
	}
	public IProject getProject() {
		return null;
	}
	public PackageRoot getPackageRoot() {
		return null;
	}
	public IFile getFile() {
		return null;
	}
	public IContentDescription getContentDescription() {
		return null;
	}

	/**
	 * Get the name of the active document, if supported by the editor and the editor input.
	 * @return the name or null.
	 */
	public String getFileName() {
		return editor.getEditorInput().getName();
	}

	/**
	 * Get the encoding setting of the active document, if supported by the editor.
	 * @return the encoding setting or null.
	 */
	public String getCurrentEncoding() {
		return currentEncoding;
	}
	public String getInheritedEncoding() {
		return inheritedEncoding;
	}
	public String getDetectedEncoding() {
		return detectedEncoding;
	}
	public String getContentTypeEncoding() {
		return contentTypeEncoding;
	}
	public String getLineSeparator() {
		return lineSeparator;
	}
	
	public boolean matchesEncoding() {
		return detectedEncoding != null && Encodings.areCharsetsEqual(detectedEncoding, currentEncoding);
	}
	public boolean mismatchesEncoding() {
		return detectedEncoding != null && !Encodings.areCharsetsEqual(detectedEncoding, currentEncoding);
	}

	public void propertyChanged(Object source, int propId) {
		// It seems that the editor's encoding will not change when it is dirty.
		if (!editor.isDirty()) {
			// The document may be just saved.
			if (updateEncodingInfo()) {
				// Invoke the callback if the encoding information is changed.
				callback.encodingInfoChanged();
			}
		}
	}

	public void resourceChanged(IResourceChangeEvent event) {
		// It seems that propertyChanged() can detect changes well already.
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		// It seems that propertyChanged() can detect encoding setting changes well already.
	}

	/**
	 * Set the encoding of the active document, if supported by the editor.
	 */
	public void setEncoding(String encoding) {
		try {
			// null is inheritance
			if (Encodings.areCharsetsEqual(encoding, contentTypeEncoding)) {
				encoding = null;
			}
			else if (Encodings.areCharsetsEqual(encoding, inheritedEncoding) && contentTypeEncoding == null) {
				encoding = null;
			}
			encodingSupport.setEncoding(encoding);
		} catch (Exception e) {
			// Ignore BackingStoreException for not sync project preferences store
		}
		if (updateEncodingInfo()) {
			// Invoke the callback if the encoding information is changed.
			callback.encodingInfoChanged();
		}
	}

	/**
	 * Update the encoding information in member variables.
	 * This method may be overrided, but should be called by the sub-class.
	 * @return true if the encoding information is updated.
	 */
	protected boolean updateEncodingInfo() {
		return updateEncodingInfoPrivately();
	}

	/**
	 * Update the encoding information in private member variables.
	 * @return true if the encoding information is updated.
	 */
	private boolean updateEncodingInfoPrivately() {
		String encoding = null;

		encoding = encodingSupport.getEncoding();
		if (encoding == null) {
			// workspace encoding
			encoding = encodingSupport.getDefaultEncoding();
		}

		boolean is_not_updated =
			(encoding == null ? this.currentEncoding == null : encoding.equals(this.currentEncoding));

		this.currentEncoding = encoding;

		return !is_not_updated;
	}

	public boolean canChangeFileEncoding() {
		return false;
	}
	public boolean canConvertLineSeparator() {
		return false;
	}
	public boolean enabledContentType() {
		return false;
	}

	protected InputStream getInputStream() {
		throw new UnsupportedOperationException("Non implements getInputStream method.");
	}
	protected void setContentString(String content, String storeEncoding) {
		throw new UnsupportedOperationException("Non implements setContentString method.");
	}

	// Note: Process on String, not stream. Unsupport big file.
	protected String getContentString() {
		InputStream is = null;
		try {
			is = getInputStream();
			InputStreamReader reader = new InputStreamReader(new BufferedInputStream(is), getCurrentEncoding());
			StringBuilder sb = new StringBuilder();
			char[] buff = new char[4096];
			int read;
			while((read = reader.read(buff)) != -1) {
			    sb.append(buff, 0, read);
			}
			return sb.toString();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} finally {
			IOs.closeQuietly(is);
		}
	}

	public void setLineSeparator(String newLineSeparator) {
		if (newLineSeparator.equals(lineSeparator)) {
			return;
		}
		String newSeparator = "\r\n";
		if (newLineSeparator.equals("CR")) {
			newSeparator = "\r";
		} else if (newLineSeparator.equals("LF")) {
			newSeparator = "\n";
		}
		String content = getContentString().replaceAll("(\\r\\n|\\r|\\n)", newSeparator);
		setContentString(content, getCurrentEncoding());
	}

	public void convertCharset(String newEncoding) {
		String content = getContentString();
		setContentString(content, newEncoding);
		setEncoding(newEncoding);
	}

	public void infoMessage(String message, Object... args) {
		setMessage("info", message, args);
	}

	public void warnMessage(String message, Object... args) {
		setMessage("warn", message, args);
	}

	private void setMessage(String imageIconKey, String message, Object... args) {
		if (editor != null) {
			IStatusLineManager statusLineManager = editor.getEditorSite().getActionBars().getStatusLineManager();
			if (message == null) {
				statusLineManager.setMessage(null);
			} else {
				statusLineManager.setMessage(Activator.getImage(imageIconKey), String.format(message, args));
			}
		}
	}
}
