package org.nhindirect.stagent.cert.impl;

import java.io.File;
import java.io.OutputStream;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.nhindirect.common.options.OptionsManagerUtils;
import org.xbill.DNS.ResolverConfig;

import junit.framework.TestCase;

public class DNSCertificateStore_getServerQuerySettingsTest extends TestCase
{
	@Override
	public void setUp()
	{
		OptionsManagerUtils.clearOptionsManagerInstance();
	}
	
	@Override
	public void tearDown()
	{
		OptionsManagerUtils.clearOptionsManagerOptions();
	}
	
	public void testGetServerQuerySettingsTest_useDefaultSettings_assertSettings()
	{
		DNSCertificateStore service = new DNSCertificateStore();
		assertEquals(DNSCertificateStore.DEFAULT_DNS_RETRIES, service.retries);
		assertEquals(DNSCertificateStore.DEFAULT_DNS_TIMEOUT, service.timeout);
		assertTrue(service.useTCP);
		
		String[] configedServers = ResolverConfig.getCurrentConfig().servers();
		assertTrue(Arrays.equals(configedServers, service.servers.toArray()));
	}
	
	public void testGetServerQuerySettingsTest_useSettingsFromJVMParams_assertSettings()
	{
		System.setProperty("org.nhindirect.stagent.cert.dnsresolver.Servers", "10.3.4.3,google.lookup.com");
		System.setProperty("org.nhindirect.stagent.cert.dnsresolver.ServerRetries", "1");
		System.setProperty("org.nhindirect.stagent.cert.dnsresolver.ServerTimeout", "5");
		System.setProperty("org.nhindirect.stagent.cert.dnsresolver.ServerUseTCP", "false");
		
		try
		{
			DNSCertificateStore service = new DNSCertificateStore();
			assertEquals(1, service.retries);
			assertEquals(5, service.timeout);
			assertFalse(service.useTCP);
			assertTrue(Arrays.equals(new String[] {"10.3.4.3", "google.lookup.com"}, service.servers.toArray()));
		}
		finally
		{
			System.setProperty("org.nhindirect.stagent.cert.dnsresolver.Servers", "");
			System.setProperty("org.nhindirect.stagent.cert.dnsresolver.ServerRetries", "");
			System.setProperty("org.nhindirect.stagent.cert.dnsresolver.ServerTimeout", "");
			System.setProperty("org.nhindirect.stagent.cert.dnsresolver.ServerUseTCP", "");
		}
	}
	
	@SuppressWarnings("deprecation")
	public void testGetServerQuerySettingsTest_useSettingsFromPropertiesFile_assertSettings() throws Exception
	{
		File propFile = new File("./target/props/agentSettings.properties");
		if (propFile.exists())
			propFile.delete();
	
		System.setProperty("org.nhindirect.stagent.PropertiesFile", "./target/props/agentSettings.properties");
		
		OutputStream outStream = null;
	
		try
		{
			outStream = FileUtils.openOutputStream(propFile);
			outStream.write("org.nhindirect.stagent.cert.dnsresolver.Servers=10.3.4.3,google.lookup.com\r\n".getBytes());
			outStream.write("org.nhindirect.stagent.cert.dnsresolver.ServerRetries=5\r\n".getBytes());
			outStream.write("org.nhindirect.stagent.cert.dnsresolver.ServerTimeout=7\r\n".getBytes());
			outStream.write("org.nhindirect.stagent.cert.dnsresolver.ServerUseTCP=false\r\n".getBytes());
			outStream.flush();
			
		}
		finally
		{
			IOUtils.closeQuietly(outStream);
		}
		
		try
		{
			DNSCertificateStore service = new DNSCertificateStore();
			assertEquals(5, service.retries);
			assertEquals(7, service.timeout);
			assertFalse(service.useTCP);
			assertTrue(Arrays.equals(new String[] {"10.3.4.3", "google.lookup.com"}, service.servers.toArray()));
		}
		finally
		{
			System.setProperty("org.nhindirect.stagent.cert.dnsresolver.Servers", "");
			System.setProperty("org.nhindirect.stagent.cert.dnsresolver.ServerRetries", "");
			System.setProperty("org.nhindirect.stagent.cert.dnsresolver.ServerTimeout", "");
			System.setProperty("org.nhindirect.stagent.cert.dnsresolver.ServerUseTCP", "");
			System.setProperty("org.nhindirect.stagent.PropertiesFile", "");
			propFile.delete();
		}
	}
}
