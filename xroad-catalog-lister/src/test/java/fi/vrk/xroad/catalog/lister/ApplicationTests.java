/**
 * The MIT License
 * Copyright (c) 2022, Population Register Centre (VRK)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fi.vrk.xroad.catalog.lister;

import fi.vrk.xroad.xroad_catalog_lister.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ClassUtils;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.SoapFaultClientException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.GregorianCalendar;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ListerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationTests {

	private final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

	@LocalServerPort
	private int port;

	@Before
	public void init() throws Exception {
		marshaller.setPackagesToScan(ClassUtils.getPackageName(ListMembers.class));
		marshaller.afterPropertiesSet();
	}


	@Test
	public void testListServices() {
		ListMembers request = new ListMembers();
		ListMembersResponse result = (ListMembersResponse)new WebServiceTemplate(marshaller).marshalSendAndReceive(
				"http://localhost:" + port + "/ws", request);
		assertNotNull(result);
		assertEquals("MemberList size", 3, result.getMemberList().getMember().size());
	}

	@Test
	public void testGetServiceType() {
		GetServiceType request = new GetServiceType();
		request.setXRoadInstance("dev-cs");
		request.setMemberClass("PUB");
		request.setMemberCode("14151328");
		request.setServiceCode("testService");
		request.setSubsystemCode("TestSubSystem");
		request.setServiceVersion("v1");

		GetServiceTypeResponse result = (GetServiceTypeResponse)new WebServiceTemplate(marshaller).marshalSendAndReceive(
				"http://localhost:" + port + "/ws/GetServiceType/", request);
		assertNotNull(result);
		assertEquals("Is given service a SOAP service", "SOAP", result.getType());

		request.setServiceCode("getAnotherRandom");
		result = (GetServiceTypeResponse)new WebServiceTemplate(marshaller).marshalSendAndReceive(
				"http://localhost:" + port + "/ws/GetServiceType/", request);
		assertNotNull(result);
		assertEquals("Is given service a SOAP service", "REST", result.getType());

		request.setServiceCode("getRandom");
		result = (GetServiceTypeResponse)new WebServiceTemplate(marshaller).marshalSendAndReceive(
				"http://localhost:" + port + "/ws/GetServiceType/", request);
		assertNotNull(result);
		assertEquals("Is given service a SOAP service", "OPENAPI", result.getType());

	}

	@Test
	public void testGetServiceTypeException() {
		boolean thrown = false;
		String exceptionMessage = null;
		try {
			GetServiceType request = new GetServiceType();
			request.setXRoadInstance("dev-cs");
			request.setMemberClass("PUB");
			request.setMemberCode("14151328");
			request.setServiceCode("testService123");
			request.setSubsystemCode("TestSubSystem");
			request.setServiceVersion("v1");
			new WebServiceTemplate(marshaller).marshalSendAndReceive(
					"http://localhost:" + port + "/ws/GetServiceType/", request);
		} catch (SoapFaultClientException e) {
			thrown = true;
			exceptionMessage = e.getMessage();
		}
		assertTrue(thrown);
		assertEquals(exceptionMessage, "Service with xRoadInstance \"dev-cs\", " +
				"memberClass \"PUB\", memberCode \"14151328\", subsystemCode \"TestSubSystem\", serviceCode \"testService123\" " +
				"and serviceVersion \"v1\" not found");
	}

	@Test
	public void testGetWsdl() {
		GetWsdl request = new GetWsdl();
		request.setExternalId("1000");
		GetWsdlResponse result = (GetWsdlResponse)new WebServiceTemplate(marshaller).marshalSendAndReceive(
				"http://localhost:" + port + "/ws/GetWsdl/", request);
		assertNotNull(result);
		assertEquals("getWsdl",
				"<?xml version=\"1.0\" standalone=\"no\"?><wsdl-6-1-1-1-changed/>",
				result.getWsdl());
	}

	@Test
	public void testGetWsdlException() {
		boolean thrown = false;
		String exceptionMessage = null;
		try {
			GetWsdl request = new GetWsdl();
			request.setExternalId("1001");
			new WebServiceTemplate(marshaller).marshalSendAndReceive(
					"http://localhost:" + port + "/ws/GetWsdl/", request);
		} catch (SoapFaultClientException e) {
			thrown = true;
			exceptionMessage = e.getMessage();
		}
		assertTrue(thrown);
		assertEquals("wsdl with external id 1001 not found", exceptionMessage);
	}

	@Test
	public void testGetOpenApi() {
		GetOpenAPI request = new GetOpenAPI();
		request.setExternalId("3003");
		GetOpenAPIResponse result = (GetOpenAPIResponse)new WebServiceTemplate(marshaller).marshalSendAndReceive(
				"http://localhost:" + port + "/ws/GetOpenAPI/", request);
		assertNotNull(result);
		assertEquals("getOpenAPI", "<openapi>", result.getOpenapi());
	}

	@Test
	public void testGetOpenApiException() {
		boolean thrown = false;
		String exceptionMessage = null;
		try {
			GetOpenAPI request = new GetOpenAPI();
			request.setExternalId("3001");
			new WebServiceTemplate(marshaller).marshalSendAndReceive(
					"http://localhost:" + port + "/ws/GetOpenAPI/", request);
		} catch (SoapFaultClientException e) {
			thrown = true;
			exceptionMessage = e.getMessage();
		}
		assertTrue(thrown);
		assertEquals("OpenApi with external id 3001 not found", exceptionMessage);
	}

	@Test
	public void testIsProvider() {
		IsProvider request = new IsProvider();
		request.setXRoadInstance("dev-cs");
		request.setMemberClass("PUB");
		request.setMemberCode("14151328");
		IsProviderResponse result = (IsProviderResponse)new WebServiceTemplate(marshaller).marshalSendAndReceive(
				"http://localhost:" + port + "/ws/IsProvider/", request);
		assertNotNull(result);
		assertEquals("Is given member a service provider", true, result.isProvider());
	}

	@Test
	public void testIsProviderFalse() {
		IsProvider request = new IsProvider();
		request.setXRoadInstance("dev-cs");
		request.setMemberClass("PUB");
		request.setMemberCode("88855888");
		IsProviderResponse result = (IsProviderResponse)new WebServiceTemplate(marshaller).marshalSendAndReceive(
				"http://localhost:" + port + "/ws/IsProvider/", request);
		assertNotNull(result);
		assertEquals("Is given member a service provider", false, result.isProvider());
	}

	@Test
	public void testIsProviderException() {
		boolean thrown = false;
		String exceptionMessage = null;
		try {
			IsProvider request = new IsProvider();
			request.setXRoadInstance("dev-cs");
			request.setMemberClass("PUB");
			request.setMemberCode("123");
			new WebServiceTemplate(marshaller).marshalSendAndReceive(
						"http://localhost:" + port + "/ws/IsProvider/", request);
		} catch (SoapFaultClientException e) {
				thrown = true;
				exceptionMessage = e.getMessage();
		}
		assertTrue(thrown);
		assertEquals("Member with xRoadInstance \"dev-cs\", memberClass \"PUB\" and memberCode \"123\" not found", exceptionMessage);
	}

	@Test
	public void testGetOrganizations() {
		GetOrganizations request = new GetOrganizations();
		request.setBusinessCode("0123456-9");
		GetOrganizationsResponse result = (GetOrganizationsResponse)new WebServiceTemplate(marshaller).marshalSendAndReceive(
				"http://localhost:" + port + "/ws/GetOrganizations/", request);
		assertNotNull(result);
		assertEquals("OrganizationList size", 1, result.getOrganizationList().getOrganization().size());
		assertEquals("Organization businessCode", "0123456-9", result.getOrganizationList().getOrganization().get(0).getBusinessCode());
		assertEquals("Organization guid", "abcdef123456", result.getOrganizationList().getOrganization().get(0).getGuid());
		assertEquals("Organization street address latitude", "6939589.246", result.getOrganizationList()
				.getOrganization().get(0).getAddresses().getAddress().get(0).getStreetAddresses().getStreetAddress().get(0).getLatitude());
		assertEquals("Organization street address longitude", "208229.722", result.getOrganizationList()
				.getOrganization().get(0).getAddresses().getAddress().get(0).getStreetAddresses().getStreetAddress().get(0).getLongitude());
		assertEquals("Organization e-mail address", "vaasa@vaasa.fi", result.getOrganizationList()
				.getOrganization().get(0).getEmails().getEmail().get(0).getValue());
		assertEquals("Organization web page", "https://www.vaasa.fi/", result.getOrganizationList()
				.getOrganization().get(0).getWebPages().getWebPage().get(0).getUrl());
	}

	@Test
	public void testGetOrganizationsException() {
		boolean thrown = false;
		String exceptionMessage = null;
		try {
			GetOrganizations request = new GetOrganizations();
			request.setBusinessCode("0123456-1");
			GetOrganizationsResponse result = (GetOrganizationsResponse)new WebServiceTemplate(marshaller).marshalSendAndReceive(
					"http://localhost:" + port + "/ws/GetOrganizations/", request);
		} catch (SoapFaultClientException e) {
			thrown = true;
			exceptionMessage = e.getMessage();
		}
		assertTrue(thrown);
		assertEquals("Organizations with businessCode 0123456-1 not found", exceptionMessage);
	}

	@Test
	public void testHasOrganizationChangedValueList() {
		HasOrganizationChanged request = new HasOrganizationChanged();
		request.setGuid("abcdef123456");
		LocalDateTime changedAfter = LocalDateTime.of(2015, Month.JULY, 29, 19, 30, 40);
		LocalDateTime changedUntil = LocalDateTime.of(2022, Month.JULY, 29, 19, 30, 40);
		GregorianCalendar calStart = GregorianCalendar.from(changedAfter.atZone(ZoneId.systemDefault()));
		GregorianCalendar calEnd = GregorianCalendar.from(changedUntil.atZone(ZoneId.systemDefault()));
		XMLGregorianCalendar startDateTime = null;
		XMLGregorianCalendar endDateTime = null;
		try {
			startDateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(calStart);
			endDateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(calEnd);
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
		request.setStartDateTime(startDateTime);
		request.setEndDateTime(endDateTime);
		HasOrganizationChangedResponse result = (HasOrganizationChangedResponse)new WebServiceTemplate(marshaller).marshalSendAndReceive(
				"http://localhost:" + port + "/ws/HasOrganizationChanged/", request);
		assertNotNull(result);
		assertEquals("Organization changed", true, result.isChanged());
		assertEquals("Organization changedValueList size", 19, result.getChangedValueList().getChangedValue().size());
	}

	@Test
	public void testHasOrganizationChangedSingleValue() {
		HasOrganizationChanged request = new HasOrganizationChanged();
		request.setGuid("abcdef123456");
		LocalDateTime changedAfter = LocalDateTime.of(2019, Month.JULY, 29, 19, 30, 40);
		LocalDateTime changedUntil = LocalDateTime.of(2022, Month.JULY, 29, 19, 30, 40);
		GregorianCalendar calStart = GregorianCalendar.from(changedAfter.atZone(ZoneId.systemDefault()));
		GregorianCalendar calEnd = GregorianCalendar.from(changedUntil.atZone(ZoneId.systemDefault()));
		XMLGregorianCalendar startDateTime = null;
		XMLGregorianCalendar endDateTime = null;
		try {
			startDateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(calStart);
			endDateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(calEnd);
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
		request.setStartDateTime(startDateTime);
		request.setEndDateTime(endDateTime);
		HasOrganizationChangedResponse result = (HasOrganizationChangedResponse)new WebServiceTemplate(marshaller).marshalSendAndReceive(
				"http://localhost:" + port + "/ws/HasOrganizationChanged/", request);
		assertNotNull(result);
		assertEquals("Organization changed", true, result.isChanged());
		assertEquals("Organization changedValueList size", 1, result.getChangedValueList().getChangedValue().size());
		assertEquals("Organization changed value", "Email", result.getChangedValueList().getChangedValue().get(0).getName());
	}

	@Test
	public void testHasOrganizationChangedFalse() {
		HasOrganizationChanged request = new HasOrganizationChanged();
		request.setGuid("abcdef123456");
		LocalDateTime changedAfter = LocalDateTime.of(2020, Month.JANUARY, 1, 0, 0, 0);
		LocalDateTime changedUntil = LocalDateTime.of(2022, Month.JULY, 29, 19, 30, 40);
		GregorianCalendar calStart = GregorianCalendar.from(changedAfter.atZone(ZoneId.systemDefault()));
		GregorianCalendar calEnd = GregorianCalendar.from(changedUntil.atZone(ZoneId.systemDefault()));
		XMLGregorianCalendar startDateTime = null;
		XMLGregorianCalendar endDateTime = null;
		try {
			startDateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(calStart);
			endDateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(calEnd);
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
		request.setStartDateTime(startDateTime);
		request.setEndDateTime(endDateTime);
		HasOrganizationChangedResponse result = (HasOrganizationChangedResponse)new WebServiceTemplate(marshaller).marshalSendAndReceive(
				"http://localhost:" + port + "/ws/HasOrganizationChanged/", request);
		assertNotNull(result);
		assertEquals("Organization changed", false, result.isChanged());
		assertEquals("Organization changedValueList size", 0, result.getChangedValueList().getChangedValue().size());
	}

	@Test
	public void testHasOrganizationChangedException() {
		boolean thrown = false;
		String exceptionMessage = null;
		try {
			HasOrganizationChanged request = new HasOrganizationChanged();
			request.setGuid("a123456");
			LocalDateTime changedAfter = LocalDateTime.of(2015, Month.JULY, 29, 19, 30, 40);
			LocalDateTime changedUntil = LocalDateTime.of(2022, Month.JULY, 29, 19, 30, 40);
			GregorianCalendar calStart = GregorianCalendar.from(changedAfter.atZone(ZoneId.systemDefault()));
			GregorianCalendar calEnd = GregorianCalendar.from(changedUntil.atZone(ZoneId.systemDefault()));
			XMLGregorianCalendar startDateTime = null;
			XMLGregorianCalendar endDateTime = null;
			try {
				startDateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(calStart);
				endDateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(calEnd);
			} catch (DatatypeConfigurationException e) {
				e.printStackTrace();
			}
			request.setStartDateTime(startDateTime);
			request.setEndDateTime(endDateTime);
			HasOrganizationChangedResponse result = (HasOrganizationChangedResponse)new WebServiceTemplate(marshaller).marshalSendAndReceive(
					"http://localhost:" + port + "/ws/HasOrganizationChanged/", request);
		} catch (SoapFaultClientException e) {
			thrown = true;
			exceptionMessage = e.getMessage();
		}
		assertTrue(thrown);
		assertEquals("Organization with guid a123456 not found", exceptionMessage);

	}

	@Test
	public void testHasOrganizationChangedGuidRequiredException() {
		boolean thrown = false;
		String exceptionMessage = null;
		try {
			HasOrganizationChanged request = new HasOrganizationChanged();
			request.setGuid(null);
			LocalDateTime changedAfter = LocalDateTime.of(2015, Month.JULY, 29, 19, 30, 40);
			LocalDateTime changedUntil = LocalDateTime.of(2022, Month.JULY, 29, 19, 30, 40);
			GregorianCalendar calStart = GregorianCalendar.from(changedAfter.atZone(ZoneId.systemDefault()));
			GregorianCalendar calEnd = GregorianCalendar.from(changedUntil.atZone(ZoneId.systemDefault()));
			XMLGregorianCalendar startDateTime = null;
			XMLGregorianCalendar endDateTime = null;
			try {
				startDateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(calStart);
				endDateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(calEnd);
			} catch (DatatypeConfigurationException e) {
				e.printStackTrace();
			}
			request.setStartDateTime(startDateTime);
			request.setEndDateTime(endDateTime);
			HasOrganizationChangedResponse result = (HasOrganizationChangedResponse)new WebServiceTemplate(marshaller).marshalSendAndReceive(
					"http://localhost:" + port + "/ws/HasOrganizationChanged/", request);
		} catch (SoapFaultClientException e) {
			thrown = true;
			exceptionMessage = e.getMessage();
		}
		assertTrue(thrown);
		assertEquals("Guid is a required parameter", exceptionMessage);

	}

	@Test
	public void testGetCompanies() {
		GetCompanies request = new GetCompanies();
		request.setBusinessId("1710128-9");
		GetCompaniesResponse result = (GetCompaniesResponse)new WebServiceTemplate(marshaller).marshalSendAndReceive(
				"http://localhost:" + port + "/ws/GetCompanies/", request);
		assertNotNull(result);
		assertEquals("CompanyList size", 1, result.getCompanyList().getCompany().size());
		assertEquals("Company businessId", "1710128-9", result.getCompanyList().getCompany().get(0).getBusinessId());
		assertEquals("Company detailsUri", "", result.getCompanyList().getCompany().get(0).getDetailsUri());
		assertEquals("Company companyForm", "OYJ", result.getCompanyList().getCompany().get(0).getCompanyForm());
		assertEquals("Company name", "Gofore Oyj", result.getCompanyList().getCompany().get(0).getName());
		assertEquals("Company BusinessAddress street", "Kalevantie 2",
				result.getCompanyList().getCompany().get(0).getBusinessAddresses().getBusinessAddress().get(0).getStreet());
		assertEquals("Company BusinessAuxiliaryName name", "Solinor",
				result.getCompanyList().getCompany().get(0).getBusinessAuxiliaryNames().getBusinessAuxiliaryName().get(0).getName());
		assertEquals("Company BusinessIdChange oldBusinessId", "1796717-0",
				result.getCompanyList().getCompany().get(0).getBusinessIdChanges().getBusinessIdChange().get(0).getOldBusinessId());
		assertEquals("Company BusinessLine name", "Dataprogrammering",
				result.getCompanyList().getCompany().get(0).getBusinessLines().getBusinessLine().get(0).getName());
		assertEquals("Company BusinessName language", "FI",
				result.getCompanyList().getCompany().get(0).getBusinessNames().getBusinessName().get(0).getLanguage());
		assertEquals("Company CompanyForm name", "Public limited company",
				result.getCompanyList().getCompany().get(0).getCompanyForms().getCompanyForm().get(0).getName());
		assertEquals("Company ContactDetails language", "EN",
				result.getCompanyList().getCompany().get(0).getContactDetails().getContactDetail().get(0).getLanguage());
		assertEquals("Company Language name", "Finska",
				result.getCompanyList().getCompany().get(0).getLanguages().getLanguage().get(0).getName());
		assertEquals("Company Liquidation language", "FI",
				result.getCompanyList().getCompany().get(0).getLiquidations().getLiquidation().get(0).getLanguage());
		assertEquals("Company RegisteredEntry descritpion", "Unregistered",
				result.getCompanyList().getCompany().get(0).getRegisteredEntries().getRegisteredEntry().get(0).getDescription());
		assertEquals("Company RegisteredOffice language", "FI",
				result.getCompanyList().getCompany().get(0).getRegisteredOffices().getRegisteredOffice().get(0).getLanguage());
	}

	@Test
	public void testGetCompaniesException() {
		boolean thrown = false;
		String exceptionMessage = null;
		try {
			GetCompanies request = new GetCompanies();
			request.setBusinessId("1710128-1");
			GetCompaniesResponse result = (GetCompaniesResponse)new WebServiceTemplate(marshaller).marshalSendAndReceive(
					"http://localhost:" + port + "/ws/GetCompanies/", request);
		} catch (SoapFaultClientException e) {
			thrown = true;
			exceptionMessage = e.getMessage();
		}
		assertTrue(thrown);
		assertEquals("Companies with businessId 1710128-1 not found", exceptionMessage);
	}

	@Test
	public void testGetCompaniesBusinessIdRequiredException() {
		boolean thrown = false;
		String exceptionMessage = null;
		try {
			GetCompanies request = new GetCompanies();
			request.setBusinessId(null);
			GetCompaniesResponse result = (GetCompaniesResponse)new WebServiceTemplate(marshaller).marshalSendAndReceive(
					"http://localhost:" + port + "/ws/GetCompanies/", request);
		} catch (SoapFaultClientException e) {
			thrown = true;
			exceptionMessage = e.getMessage();
		}
		assertTrue(thrown);
		assertEquals("Companies with businessId null not found", exceptionMessage);
	}

	@Test
	public void testHasCompanyChangedValueList() {
		HasCompanyChanged request = new HasCompanyChanged();
		request.setBusinessId("1710128-9");
		LocalDateTime changedAfter = LocalDateTime.of(2020, Month.MAY, 4, 0, 0, 0);
		LocalDateTime changedUntil = LocalDateTime.of(2022, Month.JULY, 29, 19, 30, 40);
		GregorianCalendar calStart = GregorianCalendar.from(changedAfter.atZone(ZoneId.systemDefault()));
		GregorianCalendar calEnd = GregorianCalendar.from(changedUntil.atZone(ZoneId.systemDefault()));
		XMLGregorianCalendar startDateTime = null;
		XMLGregorianCalendar endDateTime = null;
		try {
			startDateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(calStart);
			endDateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(calEnd);
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
		request.setStartDateTime(startDateTime);
		request.setEndDateTime(endDateTime);
		HasCompanyChangedResponse result = (HasCompanyChangedResponse)new WebServiceTemplate(marshaller).marshalSendAndReceive(
				"http://localhost:" + port + "/ws/HasCompanyChanged/", request);
		assertNotNull(result);
		assertEquals("Company changed", true, result.isChanged());
		assertEquals("Company changedValueList size", 12, result.getChangedValueList().getChangedValue().size());
	}

	@Test
	public void testHasCompanyChangedTwoValues() {
		HasCompanyChanged request = new HasCompanyChanged();
		request.setBusinessId("1710128-9");
		LocalDateTime changedAfter = LocalDateTime.of(2020, Month.MAY, 6, 0, 0, 0);
		LocalDateTime changedUntil = LocalDateTime.of(2022, Month.JULY, 29, 19, 30, 40);
		GregorianCalendar calStart = GregorianCalendar.from(changedAfter.atZone(ZoneId.systemDefault()));
		GregorianCalendar calEnd = GregorianCalendar.from(changedUntil.atZone(ZoneId.systemDefault()));
		XMLGregorianCalendar startDateTime = null;
		XMLGregorianCalendar endDateTime = null;
		try {
			startDateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(calStart);
			endDateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(calEnd);
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
		request.setStartDateTime(startDateTime);
		request.setEndDateTime(endDateTime);
		HasCompanyChangedResponse result = (HasCompanyChangedResponse)new WebServiceTemplate(marshaller).marshalSendAndReceive(
				"http://localhost:" + port + "/ws/HasCompanyChanged/", request);
		assertNotNull(result);
		assertEquals("Company changed", true, result.isChanged());
		assertEquals("Company changedValueList size", 4, result.getChangedValueList().getChangedValue().size());
	}

	@Test
	public void testHasCompanyChangedFalse() {
		HasCompanyChanged request = new HasCompanyChanged();
		request.setBusinessId("1710128-9");
		LocalDateTime changedAfter = LocalDateTime.of(2021, Month.MAY, 6, 12, 0, 0);
		LocalDateTime changedUntil = LocalDateTime.of(2022, Month.JULY, 29, 19, 30, 40);
		GregorianCalendar calStart = GregorianCalendar.from(changedAfter.atZone(ZoneId.systemDefault()));
		GregorianCalendar calEnd = GregorianCalendar.from(changedUntil.atZone(ZoneId.systemDefault()));
		XMLGregorianCalendar startDateTime = null;
		XMLGregorianCalendar endDateTime = null;
		try {
			startDateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(calStart);
			endDateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(calEnd);
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
		request.setStartDateTime(startDateTime);
		request.setEndDateTime(endDateTime);
		HasCompanyChangedResponse result = (HasCompanyChangedResponse)new WebServiceTemplate(marshaller).marshalSendAndReceive(
				"http://localhost:" + port + "/ws/HasCompanyChanged/", request);
		assertNotNull(result);
		assertEquals("Company changed", false, result.isChanged());
		assertEquals("Company changedValueList size", 0, result.getChangedValueList().getChangedValue().size());
	}

	@Test
	public void testHasCompanyChangedException() {
		boolean thrown = false;
		String exceptionMessage = null;
		try {
			HasCompanyChanged request = new HasCompanyChanged();
			request.setBusinessId("1710128-1");
			LocalDateTime changedAfter = LocalDateTime.of(2020, Month.MAY, 6, 12, 0, 0);
			LocalDateTime changedUntil = LocalDateTime.of(2022, Month.JULY, 29, 19, 30, 40);
			GregorianCalendar calStart = GregorianCalendar.from(changedAfter.atZone(ZoneId.systemDefault()));
			GregorianCalendar calEnd = GregorianCalendar.from(changedUntil.atZone(ZoneId.systemDefault()));
			XMLGregorianCalendar startDateTime = null;
			XMLGregorianCalendar endDateTime = null;
			try {
				startDateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(calStart);
				endDateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(calEnd);
			} catch (DatatypeConfigurationException e) {
				e.printStackTrace();
			}
			request.setStartDateTime(startDateTime);
			request.setEndDateTime(endDateTime);
			HasCompanyChangedResponse result = (HasCompanyChangedResponse)new WebServiceTemplate(marshaller).marshalSendAndReceive(
					"http://localhost:" + port + "/ws/HasCompanyChanged/", request);
		} catch (SoapFaultClientException e) {
			thrown = true;
			exceptionMessage = e.getMessage();
		}
		assertTrue(thrown);
		assertEquals("company with businessId 1710128-1 not found", exceptionMessage);

	}

	@Test
	public void testHasCompanyChangedBusinessIdRequiredException() {
		boolean thrown = false;
		String exceptionMessage = null;
		try {
			HasCompanyChanged request = new HasCompanyChanged();
			request.setBusinessId(null);
			LocalDateTime changedAfter = LocalDateTime.of(2020, Month.MAY, 6, 12, 0, 0);
			LocalDateTime changedUntil = LocalDateTime.of(2022, Month.JULY, 29, 19, 30, 40);
			GregorianCalendar calStart = GregorianCalendar.from(changedAfter.atZone(ZoneId.systemDefault()));
			GregorianCalendar calEnd = GregorianCalendar.from(changedUntil.atZone(ZoneId.systemDefault()));
			XMLGregorianCalendar startDateTime = null;
			XMLGregorianCalendar endDateTime = null;
			try {
				startDateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(calStart);
				endDateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(calEnd);
			} catch (DatatypeConfigurationException e) {
				e.printStackTrace();
			}
			request.setStartDateTime(startDateTime);
			request.setEndDateTime(endDateTime);
			HasCompanyChangedResponse result = (HasCompanyChangedResponse)new WebServiceTemplate(marshaller).marshalSendAndReceive(
					"http://localhost:" + port + "/ws/HasCompanyChanged/", request);
		} catch (SoapFaultClientException e) {
			thrown = true;
			exceptionMessage = e.getMessage();
		}
		assertTrue(thrown);
		assertEquals("BusinessId is a required parameter", exceptionMessage);

	}

	@Test
	public void testGetErrors() {
		GetErrors request = new GetErrors();
		LocalDateTime changedAfter = LocalDateTime.of(2001, Month.MAY, 6, 12, 0, 0);
		LocalDateTime changedUntil = LocalDateTime.of(2022, Month.JULY, 29, 19, 30, 40);
		GregorianCalendar calStart = GregorianCalendar.from(changedAfter.atZone(ZoneId.systemDefault()));
		GregorianCalendar calEnd = GregorianCalendar.from(changedUntil.atZone(ZoneId.systemDefault()));
		XMLGregorianCalendar startDateTime = null;
		XMLGregorianCalendar endDateTime = null;
		try {
			startDateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(calStart);
			endDateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(calEnd);
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
		request.setStartDateTime(startDateTime);
		request.setEndDateTime(endDateTime);
		GetErrorsResponse result = (GetErrorsResponse)new WebServiceTemplate(marshaller).marshalSendAndReceive(
				"http://localhost:" + port + "/ws/GetErrors/", request);
		assertNotNull(result);
		assertEquals("ErrorLogList size", 6, result.getErrorLogList().getErrorLog().size());
		assertEquals("ErrorLog message", "Service not found", result.getErrorLogList().getErrorLog().get(0).getMessage());
	}

	@Test
	public void testGetErrorsException() {
		boolean thrown = false;
		String exceptionMessage = null;
		GetErrors request = new GetErrors();
		try {
			LocalDateTime changedAfter = LocalDateTime.of(2021, Month.JANUARY, 6, 12, 0, 0);
			LocalDateTime changedUntil = LocalDateTime.of(2022, Month.JULY, 29, 19, 30, 40);
			GregorianCalendar calStart = GregorianCalendar.from(changedAfter.atZone(ZoneId.systemDefault()));
			GregorianCalendar calEnd = GregorianCalendar.from(changedUntil.atZone(ZoneId.systemDefault()));
			XMLGregorianCalendar startDateTime = null;
			XMLGregorianCalendar endDateTime = null;
			try {
				startDateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(calStart);
				endDateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(calEnd);
			} catch (DatatypeConfigurationException e) {
				e.printStackTrace();
			}
			request.setStartDateTime(startDateTime);
			request.setEndDateTime(endDateTime);
			GetErrorsResponse result = (GetErrorsResponse)new WebServiceTemplate(marshaller).marshalSendAndReceive(
					"http://localhost:" + port + "/ws/GetErrors/", request);
		} catch (SoapFaultClientException e) {
			thrown = true;
			exceptionMessage = e.getMessage();
		}
		assertTrue(thrown);
		assertEquals(exceptionMessage, "ErrorLog entries since " + request.getStartDateTime().toString() +
				" until " + request.getEndDateTime().toString() + " not found");
	}
}
