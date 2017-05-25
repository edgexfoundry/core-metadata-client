/*******************************************************************************
 * Copyright 2016-2017 Dell Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @microservice:  core-metadata-client
 * @author: Jim White, Dell
 * @version: 1.0.0
 *******************************************************************************/
package org.edgexfoundry.controller;

import static org.edgexfoundry.test.data.DeviceData.TEST_LABELS;
import static org.edgexfoundry.test.data.DeviceData.TEST_NAME;
import static org.edgexfoundry.test.data.DeviceData.checkTestData;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.List;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;

import org.edgexfoundry.controller.AddressableClient;
import org.edgexfoundry.controller.AddressableClientImpl;
import org.edgexfoundry.controller.DeviceClient;
import org.edgexfoundry.controller.DeviceClientImpl;
import org.edgexfoundry.controller.DeviceProfileClient;
import org.edgexfoundry.controller.DeviceProfileClientImpl;
import org.edgexfoundry.controller.DeviceServiceClient;
import org.edgexfoundry.controller.DeviceServiceClientImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import org.edgexfoundry.domain.meta.Addressable;
import org.edgexfoundry.domain.meta.AdminState;
import org.edgexfoundry.domain.meta.Device;
import org.edgexfoundry.domain.meta.DeviceProfile;
import org.edgexfoundry.domain.meta.DeviceService;
import org.edgexfoundry.domain.meta.OperatingState;
import org.edgexfoundry.test.category.RequiresMetaDataRunning;
import org.edgexfoundry.test.category.RequiresMongoDB;
import org.edgexfoundry.test.data.AddressableData;
import org.edgexfoundry.test.data.DeviceData;
import org.edgexfoundry.test.data.ProfileData;
import org.edgexfoundry.test.data.ServiceData;

@Category({ RequiresMongoDB.class, RequiresMetaDataRunning.class })
public class DeviceClientTest {

	private static final String ENDPT = "http://localhost:48081/api/v1/device";
	private static final String SRV_ENDPT = "http://localhost:48081/api/v1/deviceservice";
	private static final String PRO_ENDPT = "http://localhost:48081/api/v1/deviceprofile";
	private static final String ADDR_ENDPT = "http://localhost:48081/api/v1/addressable";

	private DeviceClient client;
	private DeviceServiceClient srvClient;
	private DeviceProfileClient proClient;
	private AddressableClient addrClient;
	private String id;

	// setup tests the add function
	@Before
	public void setup() throws Exception {
		client = new DeviceClientImpl();
		srvClient = new DeviceServiceClientImpl();
		proClient = new DeviceProfileClientImpl();
		addrClient = new AddressableClientImpl();
		setURL();
		Addressable a = AddressableData.newTestInstance();
		addrClient.add(a);
		DeviceService s = ServiceData.newTestInstance();
		s.setAddressable(a);
		srvClient.add(s);
		DeviceProfile p = ProfileData.newTestInstance();
		proClient.add(p);
		Device device = DeviceData.newTestInstance();
		device.setAddressable(a);
		device.setProfile(p);
		device.setService(s);
		id = client.add(device);
		assertNotNull("Device did not get created correctly", id);
	}

	private void setURL() throws Exception {
		Class<?> clientClass = client.getClass();
		Field temp = clientClass.getDeclaredField("url");
		temp.setAccessible(true);
		temp.set(client, ENDPT);
		Class<?> clientClass2 = proClient.getClass();
		Field temp2 = clientClass2.getDeclaredField("url");
		temp2.setAccessible(true);
		temp2.set(proClient, PRO_ENDPT);
		Class<?> clientClass3 = srvClient.getClass();
		Field temp3 = clientClass3.getDeclaredField("url");
		temp3.setAccessible(true);
		temp3.set(srvClient, SRV_ENDPT);
		Class<?> clientClass4 = addrClient.getClass();
		Field temp4 = clientClass4.getDeclaredField("url");
		temp4.setAccessible(true);
		temp4.set(addrClient, ADDR_ENDPT);
	}

	// cleanup tests the delete function
	@After
	public void cleanup() {
		List<Device> devices = client.devices();
		devices.forEach((d) -> client.delete(d.getId()));
		List<DeviceProfile> ps = proClient.deviceProfiles();
		ps.forEach((p) -> proClient.delete(p.getId()));
		List<DeviceService> ds = srvClient.deviceServices();
		ds.forEach((d) -> srvClient.delete(d.getId()));
		List<Addressable> as = addrClient.addressables();
		as.forEach((a) -> addrClient.delete(a.getId()));
	}

	@Test
	public void testDevice() {
		Device d = client.device(id);
		checkTestData(d, id);
	}

	@Test(expected = NotFoundException.class)
	public void testDeviceWithUnknownnId() {
		client.device("nosuchid");
	}

	@Test
	public void testDevices() {
		List<Device> as = client.devices();
		assertEquals("Find all not returning a list with one device", 1, as.size());
		checkTestData(as.get(0), id);
	}

	@Test
	public void testDeviceForName() {
		Device a = client.deviceForName(TEST_NAME);
		checkTestData(a, id);
	}

	@Test(expected = NotFoundException.class)
	public void testDeviceForNameWithNoneMatching() {
		client.deviceForName("badname");
	}

	@Test
	public void testDeviceByLabel() {
		List<Device> ds = client.devicesByLabel(TEST_LABELS[0]);
		assertEquals("Find for labels not returning appropriate list", 1, ds.size());
		checkTestData(ds.get(0), id);
	}

	@Test
	public void testDevicesByLabelWithNoneMatching() {
		assertTrue("No devices should be found with bad label", client.devicesByLabel("badlabel").isEmpty());
	}

	@Test
	public void testDevicesForAddressableByName() {
		List<Device> ds = client.devicesForAddressableByName(AddressableData.TEST_ADDR_NAME);
		assertEquals("Find for address not returning appropriate list", 1, ds.size());
		checkTestData(ds.get(0), id);
	}

	@Test(expected = NotFoundException.class)
	public void testDevicesForAddressableWithNoneMatching() throws Exception {
		client.devicesForAddressable("badaddress");
	}

	@Test(expected = NotFoundException.class)
	public void testDevicesForAddressableByNameWithNoneMatching() throws Exception {
		client.devicesForAddressableByName("badaddress");
	}

	@Test
	public void testDevicesForServiceByName() {
		List<Device> ds = client.devicesForServiceByName(ServiceData.TEST_SERVICE_NAME);
		assertEquals("Find for services not returning appropriate list", 1, ds.size());
		checkTestData(ds.get(0), id);
	}

	@Test(expected = NotFoundException.class)
	public void testDevicesForServiceWithNone() {
		client.devicesForService("badservice");
	}

	@Test(expected = NotFoundException.class)
	public void testDevicesForServiceByNameWithNone() {
		client.devicesForServiceByName("badservice");
	}

	@Test
	public void testDevicesForProfileByName() {
		List<Device> ds = client.devicesForProfileByName(ProfileData.TEST_PROFILE_NAME);
		assertEquals("Find for profiles not returning appropriate list", 1, ds.size());
		checkTestData(ds.get(0), id);
	}

	@Test(expected = NotFoundException.class)
	public void testDevicesForProfileWithNone() {
		assertTrue("No devices should be found with bad profile", client.devicesForProfile("badprofile").isEmpty());
	}

	@Test(expected = NotFoundException.class)
	public void testDevicesForProfileByNameWithNone() {
		client.devicesForProfileByName("badprofile");
	}

	@Test(expected = ClientErrorException.class)
	public void testAddWithSameName() {
		Device d = client.device(id);
		d.setId(null);
		client.add(d);
	}

	@Test(expected = ClientErrorException.class)
	public void testAddWithNoDeviceService() {
		Device d = client.device(id);
		d.setId(null);
		d.setName("newname");
		d.setService(null);
		client.add(d);
	}

	@Test(expected = ClientErrorException.class)
	public void testAddWithNoDeviceProfile() {
		Device d = client.device(id);
		d.setId(null);
		d.setName("newname");
		d.setProfile(null);
		client.add(d);
	}

	@Test(expected = ClientErrorException.class)
	public void testAddWithNoAddressable() {
		Device d = client.device(id);
		d.setId(null);
		d.setName("newname");
		d.setAddressable(null);
		client.add(d);
	}

	@Test
	public void testDelete() {
		assertTrue("Delete did not return correctly", client.delete(id));
	}

	@Test(expected = NotFoundException.class)
	public void testDeleteWithNone() {
		client.delete("badid");
	}

	@Test
	public void testDeleteByName() {
		assertTrue("Delete did not return correctly", client.deleteByName(TEST_NAME));
	}

	@Test(expected = NotFoundException.class)
	public void testDeleteByNameWithNone() {
		client.delete("badname");
	}

	@Test
	public void testUpdate() {
		Device d = client.device(id);
		d.setDescription("newdescription");
		assertTrue("Update did not complete successfully", client.update(d));
		Device d2 = client.device(id);
		assertEquals("Update did not work correclty", "newdescription", d2.getDescription());
		assertNotNull("Modified date is null", d2.getModified());
		assertNotNull("Create date is null", d2.getCreated());
		assertTrue("Modified date and create date should be different after update",
				d2.getModified() != d2.getCreated());
	}

	@Test
	public void testUpdateLastConnected() {
		assertTrue("Update did not complete successfully", client.updateLastConnected(id, 1000));
		Device d2 = client.device(id);
		assertEquals("Update last connected did not work correclty", 1000, d2.getLastConnected());
		assertNotNull("Modified date is null", d2.getModified());
		assertNotNull("Create date is null", d2.getCreated());
		assertTrue("Modified date and create date should be different after update",
				d2.getModified() != d2.getCreated());
	}

	@Test
	public void testUpdateLastConnectedAndNotify() {
		assertTrue("Update did not complete successfully", client.updateLastConnected(id, 1000, true));
		Device d2 = client.device(id);
		assertEquals("Update last connected and notify did not work correclty", 1000, d2.getLastConnected());
		assertNotNull("Modified date is null", d2.getModified());
		assertNotNull("Create date is null", d2.getCreated());
		assertTrue("Modified date and create date should be different after update",
				d2.getModified() != d2.getCreated());
	}

	@Test(expected = NotFoundException.class)
	public void testUpdateLastConnectedNoneFound() {
		client.updateLastConnected("badid", 1000);
	}

	@Test
	public void testUpdateLastConnectedByName() {
		assertTrue("Update did not complete successfully", client.updateLastConnectedByName(TEST_NAME, 1000));
		Device d2 = client.deviceForName(TEST_NAME);
		assertEquals("Update last connected did not work correclty", 1000, d2.getLastConnected());
		assertNotNull("Modified date is null", d2.getModified());
		assertNotNull("Create date is null", d2.getCreated());
		assertTrue("Modified date and create date should be different after update",
				d2.getModified() != d2.getCreated());
	}

	@Test
	public void testUpdateLastConnectedByNameAndNotify() {
		assertTrue("Update did not complete successfully", client.updateLastConnectedByName(TEST_NAME, 1000, true));
		Device d2 = client.deviceForName(TEST_NAME);
		assertEquals("Update last connected and notify did not work correclty", 1000, d2.getLastConnected());
		assertNotNull("Modified date is null", d2.getModified());
		assertNotNull("Create date is null", d2.getCreated());
		assertTrue("Modified date and create date should be different after update",
				d2.getModified() != d2.getCreated());
	}

	@Test(expected = NotFoundException.class)
	public void testUpdateLastConnectedByNameNoneFound() {
		client.updateLastConnectedByName("badname", 1000);
	}

	@Test
	public void testUpdateLastReported() {
		assertTrue("Update did not complete successfully", client.updateLastReported(id, 1000));
		Device d2 = client.device(id);
		assertEquals("Update last reported did not work correclty", 1000, d2.getLastReported());
		assertNotNull("Modified date is null", d2.getModified());
		assertNotNull("Create date is null", d2.getCreated());
		assertTrue("Modified date and create date should be different after update",
				d2.getModified() != d2.getCreated());
	}

	@Test(expected = NotFoundException.class)
	public void testUpdateLastReportedNoneFound() {
		client.updateLastReported("badid", 1000);
	}

	@Test
	public void testUpdateLastReportedByName() {
		assertTrue("Update did not complete successfully", client.updateLastReportedByName(TEST_NAME, 1000));
		Device d2 = client.deviceForName(TEST_NAME);
		assertEquals("Update last reported did not work correclty", 1000, d2.getLastReported());
		assertNotNull("Modified date is null", d2.getModified());
		assertNotNull("Create date is null", d2.getCreated());
		assertTrue("Modified date and create date should be different after update",
				d2.getModified() != d2.getCreated());
	}

	@Test(expected = NotFoundException.class)
	public void testUpdateLastReportedByNameNoneFound() {
		client.updateLastReportedByName("badname", 1000);
	}

	@Test
	public void testUpdateOpState() {
		assertTrue("Update did not complete successfully",
				client.updateOpState(id, OperatingState.disabled.toString()));
		Device d2 = client.device(id);
		assertEquals("Update op state did not work correclty", OperatingState.disabled, d2.getOperatingState());
		assertNotNull("Modified date is null", d2.getModified());
		assertNotNull("Create date is null", d2.getCreated());
		assertTrue("Modified date and create date should be different after update",
				d2.getModified() != d2.getCreated());
	}

	@Test(expected = NotFoundException.class)
	public void testUpdateOpStateNoneFound() {
		client.updateOpState("badid", OperatingState.disabled.toString());
	}

	@Test
	public void testUpdateOpStateByName() {
		assertTrue("Update did not complete successfully",
				client.updateOpStateByName(TEST_NAME, OperatingState.disabled.toString()));
		Device d2 = client.deviceForName(TEST_NAME);
		assertEquals("Update op state did not work correclty", OperatingState.disabled, d2.getOperatingState());
		assertNotNull("Modified date is null", d2.getModified());
		assertNotNull("Create date is null", d2.getCreated());
		assertTrue("Modified date and create date should be different after update",
				d2.getModified() != d2.getCreated());
	}

	@Test(expected = NotFoundException.class)
	public void testUpdateOpStateByNameNoneFound() {
		client.updateOpStateByName("badname", OperatingState.disabled.toString());
	}

	@Test
	public void testUpdateAdminState() {
		assertTrue("Update did not complete successfully", client.updateAdminState(id, AdminState.locked.toString()));
		Device d2 = client.device(id);
		assertEquals("Update admin state did not work correclty", AdminState.locked, d2.getAdminState());
		assertNotNull("Modified date is null", d2.getModified());
		assertNotNull("Create date is null", d2.getCreated());
		assertTrue("Modified date and create date should be different after update",
				d2.getModified() != d2.getCreated());
	}

	@Test(expected = NotFoundException.class)
	public void testUpdateAdminStateNoneFound() {
		client.updateAdminState("badid", AdminState.locked.toString());
	}

	@Test
	public void testUpdateAdminStateByName() {
		assertTrue("Update did not complete successfully",
				client.updateAdminStateByName(TEST_NAME, AdminState.locked.toString()));
		Device d2 = client.deviceForName(TEST_NAME);
		assertEquals("Update admin state did not work correclty", AdminState.locked, d2.getAdminState());
		assertNotNull("Modified date is null", d2.getModified());
		assertNotNull("Create date is null", d2.getCreated());
		assertTrue("Modified date and create date should be different after update",
				d2.getModified() != d2.getCreated());
	}

	@Test(expected = NotFoundException.class)
	public void testUpdateAdminStateByNameNoneFound() {
		client.updateOpStateByName("badname", AdminState.locked.toString());
	}

	@Test(expected = NotFoundException.class)
	public void testUpdateWithNone() {
		Device d = client.device(id);
		d.setId("badid");
		d.setName("badname");
		d.setDescription("newdescription");
		client.update(d);
	}

}
