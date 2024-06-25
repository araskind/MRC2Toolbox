/*******************************************************************************
 *
 * (C) Copyright 2018-2020 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

public class WebUtils {

	/**
	 *
	 * @param urlname
	 * @return
	 */
	public static InputStream getInputStreamFromURL(String urlname) throws Exception {
		InputStream stream = null;
		try {
			URL url = new URL(urlname);
			java.net.HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			if (connection.getResponseCode() != 200 && connection.getResponseCode() != 404
					&& connection.getResponseCode() != 202) {
				throw new IOException(connection.getResponseMessage());
			}
			stream = connection.getInputStream();
		} catch (MalformedURLException mue) {
			System.err.println("Error: Could create URL object for " + urlname);
			throw new Exception();
		} catch (IOException e) {
			System.err.println("Error: Could not open URL connection!");
			System.err.println(urlname);
			//	throw new Exception();
		}
		catch(Exception e) {
			//	e.printStackTrace();
		}
		return stream;
	}

	public static InputStream getInputStreamFromURLsilent(String urlname) {
		InputStream stream = null;
		try {
			URL url = new URL(urlname);
			java.net.HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			if (connection.getResponseCode() != 200
					&& connection.getResponseCode() != 404
					&& connection.getResponseCode() != 202) {
				return null;
			}
			stream = connection.getInputStream();
		} catch (Exception mue) {

		}
		return stream;
	}

	/**
	 *
	 * @param urlname
	 * @return
	 */
	public static InputStream getInputStreamFromURL(String urlname, Object proxyServer, Object proxyPort)
			throws Exception {
		InputStream stream = null;

		try {
			URL url = new URL(urlname);
			java.net.HttpURLConnection connection = null;
			Proxy proxy = null;
			try {
				if (proxyServer != null && proxyPort != null) {
					proxy = new Proxy(Proxy.Type.HTTP,
							new InetSocketAddress((String) proxyServer, (Integer) proxyPort));
				}
			} catch (Exception e1) {
				System.err.println("Could not set proxy. Check settings.");
			}

			if (proxy == null)
				connection = (HttpURLConnection) url.openConnection();
			else
				connection = (HttpURLConnection) url.openConnection(proxy);

			if (connection.getResponseCode() != 200 && connection.getResponseCode() != 404
					&& connection.getResponseCode() != 202) {
				throw new IOException(connection.getResponseMessage());
			}
			stream = connection.getInputStream();
		} catch (MalformedURLException mue) {
			System.err.println("Error: Could create URL object!");
			throw new Exception();
		} catch (IOException e) {
			System.err.println("Error: Could not open URL connection!");
			System.err.println(urlname);
			//	throw new Exception();
		} catch (Exception e) {
	
		}
		return stream;
	}
	
	public static String readHTMLpageFromURL(String urlAddress) {
		
		String content = null;
		URLConnection connection = null;
		try {
		  connection =  new URL("http://www.google.com").openConnection();
		  Scanner scanner = new Scanner(connection.getInputStream());
		  scanner.useDelimiter("\\Z");
		  content = scanner.next();
		  scanner.close();
		}catch ( Exception ex ) {
		    ex.printStackTrace();
		}
		return content;
	}
	
	public static String readGetResponseAsHTMLpageFromURL(String urlAddress) throws Exception {
	
	    URL obj = new URL(urlAddress);
	    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	    con.setRequestMethod("GET");
	    con.setRequestProperty("User-Agent", 
	    		"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36");
	
	    int responseCode = con.getResponseCode();
		if (responseCode != 200 && responseCode != 404 && responseCode != 202) 
			return null;
			
	    BufferedReader in = new BufferedReader(
	            new InputStreamReader(con.getInputStream()));
	    String inputLine;
	    StringBuffer response = new StringBuffer();
	    while ((inputLine = in.readLine()) != null) 
	        response.append(inputLine);
	    
	    in.close();
	    return response.toString();	
	}
}












