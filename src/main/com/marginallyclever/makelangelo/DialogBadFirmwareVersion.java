package com.marginallyclever.makelangelo;

import java.awt.Component;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.JTextComponent;

import com.marginallyclever.core.Translator;

public class DialogBadFirmwareVersion {
	/**
	 * @param html String of valid HTML.
	 * @return a
	 */
	private JTextComponent createHyperlinkListenableJEditorPane(String html) {
		final JEditorPane bottomText = new JEditorPane();
		bottomText.setContentType("text/html");
		bottomText.setEditable(false);
		bottomText.setText(html);
		bottomText.setOpaque(false);
		final HyperlinkListener hyperlinkListener = new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {
				if (hyperlinkEvent.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					if (Desktop.isDesktopSupported()) {
						try {
							Desktop.getDesktop().browse(hyperlinkEvent.getURL().toURI());
						} catch (IOException | URISyntaxException exception) {
							// Auto-generated catch block
							exception.printStackTrace();
						}
					}

				}
			}
		};
		bottomText.addHyperlinkListener(hyperlinkListener);
		return bottomText;
	}


	/**
	 * Display the about dialog.
	 */
	public void display(Component parent,String versionExpected, String versionFound) {
		final String aboutHtml = Translator.get("firmwareVersionBadMessage",versionExpected,versionFound);
		final JTextComponent bottomText = createHyperlinkListenableJEditorPane(aboutHtml);
		JOptionPane.showMessageDialog(parent, bottomText, Translator.get("firmwareVersionBadTitle"), JOptionPane.ERROR_MESSAGE);
	}
}
