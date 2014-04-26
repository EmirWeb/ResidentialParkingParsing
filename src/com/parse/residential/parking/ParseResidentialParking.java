package com.parse.residential.parking;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.parse.residential.parking.models.GeoCodeResponse;
import com.parse.residential.parking.models.Geometry;
import com.parse.residential.parking.models.LawnParking;
import com.parse.residential.parking.models.Location;
import com.parse.residential.parking.models.Result;

public class ParseResidentialParking {

	private static final Gson GSON = new Gson();
	private static final String UTF8 = "UTF8";
	private static final String FILE = "/Users/emir/Documents/workspace-e2/ParseResidentialParking/data/frontyardparking.xml";
	private static final String RESPONSE_FILE = "/Users/emir/Documents/workspace-e2/ParseResidentialParking/data/response.json";

	private static final String URL = "https://maps.googleapis.com/maps/api/geocode/json?address=%s,+Toronto,+ON&sensor=false&key=AIzaSyDenTRRlwVo-v7lFPMrab9AWMG7_1G5w0U";

	private static final int STARTING_POSITION = 2501 * 2; // the parser gets 2 items per row for some reason

	public static class Keys {
		public static final String ROWSET = "ROWSET";
		public static final String ADDRESS = "ADDRESS";
		public static final String PARKING_TYPE = "PARKING_TYPE";
		public static final String LICENSED_SPACES = "LICENSED_SPACES";
	}

	public static void main(final String[] args) {
		final JsonArray jsonArray = new JsonArray();
		final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		try {
			final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			final Document document = documentBuilder.parse(FILE);
			final NodeList rowSetnodeList = document.getElementsByTagName(Keys.ROWSET);
			for (int rowSetIndex = 0; rowSetIndex < rowSetnodeList.getLength(); rowSetIndex++) {
				final Node rowSetNode = rowSetnodeList.item(rowSetIndex);
				if (rowSetNode.getNodeType() == Node.ELEMENT_NODE) {
					if (rowSetNode.hasChildNodes()) {
						final NodeList rowNodeList = rowSetNode.getChildNodes();
						for (int rowIndex = STARTING_POSITION; rowIndex < rowNodeList.getLength(); rowIndex++) {
							final Node row = rowNodeList.item(rowIndex);
							if (row.getNodeType() == Node.ELEMENT_NODE) {
								final Element rowElement = (Element) row;
								final String address = getValueAt(rowElement, Keys.ADDRESS);
								final String parsedAddress = address.replaceAll("\\s", "+");
								final String url = String.format(URL, parsedAddress);
								final int index = jsonArray.size();
								final Location location = getLocation(url, index);
								if (location != null) {
									final Float latitude = location.mLatitude;
									final Float longitude = location.mLongitude;
									if (latitude != null && longitude != null) {
										final JsonObject jsonObject = new JsonObject();
										jsonObject.addProperty(LawnParking.Keys.ADDRESS, address);
										jsonObject.addProperty(LawnParking.Keys.LATITUDE, latitude);
										jsonObject.addProperty(LawnParking.Keys.LONGITUDE, longitude);
										jsonArray.add(jsonObject);
									}
								} else {
									break;
								}
							}
						}
					}
				}
			}
		} catch (final ParserConfigurationException parserConfigurationException) {
			parserConfigurationException.printStackTrace();
		} catch (final SAXException saxException) {
			saxException.printStackTrace();
		} catch (final IOException ioException) {
			ioException.printStackTrace();
		}
		final int parsedAddresses = jsonArray.size();
		final int startPosition = STARTING_POSITION;
		final int endPosition = startPosition + parsedAddresses;
		System.out.println("parsedAddresses: " + parsedAddresses + " startPosition: " + startPosition + " endPosition: " + endPosition);
		FileOutputStream fileOutputStream = null;
		try {
			final String fileName = "residential_addresses_" + startPosition + "-" + endPosition + ".json";
			fileOutputStream = new FileOutputStream(fileName);
			fileOutputStream.write(jsonArray.toString().getBytes());
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
				}
			}
		}

	}

	private static Location getLocation(final String url, final int index) {
		InputStream inputStream = null;
		InputStreamReader inputStreamReader = null;
		try {
			final URL httpUrl = new URL(url);
			inputStream = getStream(httpUrl, index);
			inputStreamReader = new InputStreamReader(inputStream, UTF8);
			final GeoCodeResponse geoCodeResponse = GSON.fromJson(inputStreamReader, GeoCodeResponse.class);
			if (geoCodeResponse == null) {
				return null;
			}

			final ArrayList<Result> results = geoCodeResponse.mResults;
			if (results == null || results.isEmpty()) {
				return null;
			}

			final Result result = results.get(0);
			if (result == null) {
				return null;
			}

			final Geometry geometry = result.mGeometry;
			if (geometry == null) {
				return null;
			}
			return geometry.mLocation;
		} catch (final UnsupportedEncodingException unsupportedEncodingException) {
		} catch (MalformedURLException e) {
		} finally {
			if (inputStreamReader != null) {
				try {
					inputStreamReader.close();
				} catch (final IOException ioException) {
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (final IOException ioException) {
				}
			}
		}
		return null;
	}

	private static InputStream getStream(final URL httpUrl, final int index) {
		InputStream inputStream = null;
		FileOutputStream fileOutputStream = null;
		final String rawDataFilename = "geocode_result_" + index + ".json";
		try {
			inputStream = httpUrl.openStream();
			fileOutputStream = new FileOutputStream(rawDataFilename);
			final byte[] buffer = new byte[1024 * 100];
			int bytesRead = 0;
			while ((bytesRead = inputStream.read(buffer)) > 0) {
				fileOutputStream.write(buffer, 0, bytesRead);
			}
			return new FileInputStream(rawDataFilename);
		} catch (final UnsupportedEncodingException unsupportedEncodingException) {
		} catch (final FileNotFoundException fileNotFoundException) {
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.flush();
					fileOutputStream.close();
				} catch (final IOException ioException) {
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (final IOException ioException) {
				}
			}
		}

		return null;
	}

	private static String getValueAt(final Element root, final String key) {
		final NodeList nodeList = root.getElementsByTagName(key);
		if (nodeList.getLength() == 0) {
			return null;
		}
		final Element element = (Element) nodeList.item(0);
		return element.getTextContent();
	}

}
