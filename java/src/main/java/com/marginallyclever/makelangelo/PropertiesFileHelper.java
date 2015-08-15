package com.marginallyclever.makelangelo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created on 5/10/15.
 *
 * @author Peter Colapietro
 * @since v7.1.2
 */
public final class PropertiesFileHelper {

  /**
   *
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesFileHelper.class);

  /**
   *
   */
  private static final String MAKELANGELO_PROPERTIES_FILENAME = "makelangelo.properties";

  /**
   * @return version number in the form of vX.Y.Z where X is MAJOR, Y is MINOR version, and Z is PATCH
   * @see <a href="http://semver.org/">Semantic Versioning 2.0.0</a>
   */
  public static String getMakelangeloVersionPropertyValue() {
    final Properties prop = new Properties();
    String makelangeloVersionPropertyValue = "";
    try (final InputStream input = MainGUI.class.getClassLoader().getResourceAsStream(MAKELANGELO_PROPERTIES_FILENAME)) {
      if (input == null) {
        final String unableToFilePropertiesFileErrorMessage = "Sorry, unable to find " + MAKELANGELO_PROPERTIES_FILENAME;
        throw new IllegalStateException(unableToFilePropertiesFileErrorMessage);
      }
      //load a properties file from class path, inside static method
      prop.load(input);

      //get the property value and print it out
      makelangeloVersionPropertyValue = prop.getProperty("makelangelo.version");
      LOGGER.info("makelangelo.version={}", makelangeloVersionPropertyValue);

    } catch (IllegalStateException | IOException ex) {
      LOGGER.error("{}", ex);
    }
    return makelangeloVersionPropertyValue;
  }

  private PropertiesFileHelper() {
  }
}
