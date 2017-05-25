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

import static org.edgexfoundry.test.data.ProvisionWatcherData.KEY1;
import static org.edgexfoundry.test.data.ProvisionWatcherData.NAME;
import static org.edgexfoundry.test.data.ProvisionWatcherData.VAL1;
import static org.edgexfoundry.test.data.ProvisionWatcherData.checkTestData;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.List;

import javax.ws.rs.NotFoundException;

import org.edgexfoundry.controller.AddressableClient;
import org.edgexfoundry.controller.AddressableClientImpl;
import org.edgexfoundry.controller.DeviceProfileClient;
import org.edgexfoundry.controller.DeviceProfileClientImpl;
import org.edgexfoundry.controller.DeviceServiceClient;
import org.edgexfoundry.controller.DeviceServiceClientImpl;
import org.edgexfoundry.controller.ProvisionWatcherClient;
import org.edgexfoundry.controller.ProvisionWatcherClientImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import org.edgexfoundry.domain.meta.Addressable;
import org.edgexfoundry.domain.meta.DeviceProfile;
import org.edgexfoundry.domain.meta.DeviceService;
import org.edgexfoundry.domain.meta.ProvisionWatcher;
import org.edgexfoundry.test.category.RequiresMetaDataRunning;
import org.edgexfoundry.test.category.RequiresMongoDB;
import org.edgexfoundry.test.data.AddressableData;
import org.edgexfoundry.test.data.ProfileData;
import org.edgexfoundry.test.data.ProvisionWatcherData;
import org.edgexfoundry.test.data.ServiceData;

@Category({ RequiresMongoDB.class, RequiresMetaDataRunning.class })
public class ProvisionWatcherClientTest {

	private static final String ENDPT = "http://localhost:48081/api/v1/provisionwatcher";
	private static final String PRO_ENDPT = "http://localhost:48081/api/v1/deviceprofile";
	private static final String SRV_ENDPT = "http://localhost:48081/api/v1/deviceservice";
	private static final String ADDR_ENDPT = "http://localhost:48081/api/v1/addressable";

	private ProvisionWatcherClient client;
	private DeviceProfileClient proClient;
	private DeviceServiceClient srvClient;
	private AddressableClient addrClient;

	private String id;

	// setup tests the add function
	@Before
	public void setup() throws Exception {
		client = new ProvisionWatcherClientImpl();
		proClient = new DeviceProfileClientImpl();
		srvClient = new DeviceServiceClientImpl();
		addrClient = new AddressableClientImpl();
		setURL();
		Addressable a = AddressableData.newTestInstance();
		addrClient.add(a);
		DeviceService s = ServiceData.newTestInstance();
		s.setAddressable(a);
		srvClient.add(s);
		DeviceProfile p = ProfileData.newTestInstance();
		proClient.add(p);
		ProvisionWatcher provisionWatcher = ProvisionWatcherData.newTestInstance();
		provisionWatcher.setProfile(p);
		provisionWatcher.setService(s);
		id = client.add(provisionWatcher);
		assertNotNull("ProvisionWatcher did not get created correctly", id);
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
		List<ProvisionWatcher> pws = client.provisionWatchers();
		pws.forEach((pw) -> client.delete(pw.getId()));
		List<DeviceService> ds = srvClient.deviceServices();
		ds.forEach((d) -> srvClient.delete(d.getId()));
		List<DeviceProfile> ps = proClient.deviceProfiles();
		ps.forEach((p) -> proClient.delete(p.getId()));
		List<Addressable> as = addrClient.addressables();
		as.forEach((a) -> addrClient.delete(a.getId()));
	}

	@Test
	public void testProvisionWatcher() {
		ProvisionWatcher a = client.provisionWatcher(id);
		checkTestData(a, id);
	}

	@Test(expected = NotFoundException.class)
	public void testProvisionWatcherWithUnknownnId() {
		client.provisionWatcher("nosuchid");
	}

	@Test
	public void testProvisionWatchers() {
		List<ProvisionWatcher> as = client.provisionWatchers();
		assertEquals("Find all not returning a list with one object", 1, as.size());
		checkTestData(as.get(0), id);
	}

	@Test
	public void testProvisionWatcherForName() {
		ProvisionWatcher a = client.provisionWatcherForName(NAME);
		checkTestData(a, id);
	}

	@Test(expected = NotFoundException.class)
	public void testProvisionWatcherForNameWithNoneMatching() {
		client.provisionWatcherForName("badname");
	}

	@Test
	public void testProvisionWatchersForProfileByName() {
		List<ProvisionWatcher> ds = client.provisionWatcherForProfileByName(ProfileData.TEST_PROFILE_NAME);
		assertEquals("Find for profiles not returning appropriate list", 1, ds.size());
		checkTestData(ds.get(0), id);
	}

	@Test(expected = NotFoundException.class)
	public void testProvisionWatchersForProfileWithNone() {
		assertTrue("No provision watchers should be found with bad profile",
				client.provisionWatcherForProfile("badprofile").isEmpty());
	}

	@Test(expected = NotFoundException.class)
	public void testProvisionWatcherForProfileByNameWithNone() {
		client.provisionWatcherForProfileByName("badprofile");
	}

	@Test
	public void testProvisionWatchersForServiceByName() {
		List<ProvisionWatcher> ds = client.provisionWatcherForServiceByName(ServiceData.TEST_SERVICE_NAME);
		assertEquals("Find for service not returning appropriate list", 1, ds.size());
		checkTestData(ds.get(0), id);
	}

	@Test(expected = NotFoundException.class)
	public void testProvisionWatchersForServiceWithNone() {
		assertTrue("No provision watchers should be found with bad service",
				client.provisionWatcherForService("badservice").isEmpty());
	}

	@Test(expected = NotFoundException.class)
	public void testProvisionWatcherForServiceByNameWithNone() {
		client.provisionWatcherForServiceByName("badservice");
	}

	@Test
	public void testWatchersForIdentifier() {
		List<ProvisionWatcher> watchers = client.watchersForIdentifier(KEY1, VAL1);
		assertEquals("Find for key / value not returning appropriate list", 1, watchers.size());
		checkTestData(watchers.get(0), id);
	}

	@Test
	public void testWatchersForIdentifierWithNoMatching() {
		assertTrue("No watchers should be found with bad key/value identifier pair",
				client.watchersForIdentifier("badkey", "badvalue").isEmpty());
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
		assertTrue("Delete did not return correctly", client.deleteByName(NAME));
	}

	@Test(expected = NotFoundException.class)
	public void testDeleteByNameWithNone() {
		client.delete("badname");
	}

	@Test
	public void testUpdate() {
		ProvisionWatcher w = client.provisionWatcher(id);
		w.setOrigin(12345);
		assertTrue("Did not update correctly", client.update(w));
	}

	@Test(expected = NotFoundException.class)
	public void testUpdateWithNone() {
		ProvisionWatcher w = client.provisionWatcher(id);
		w.setId("badid");
		w.setName("badname");
		w.setOrigin(12345);
		client.update(w);
	}

}
