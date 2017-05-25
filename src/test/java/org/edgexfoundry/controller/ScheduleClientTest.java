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

import static org.edgexfoundry.test.data.ScheduleData.TEST_SCHEDULE_NAME;
import static org.edgexfoundry.test.data.ScheduleData.checkTestData;
import static org.edgexfoundry.test.data.ScheduleData.newTestInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.List;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;

import org.edgexfoundry.controller.AddressableClient;
import org.edgexfoundry.controller.AddressableClientImpl;
import org.edgexfoundry.controller.ScheduleClient;
import org.edgexfoundry.controller.ScheduleClientImpl;
import org.edgexfoundry.controller.ScheduleEventClient;
import org.edgexfoundry.controller.ScheduleEventClientImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import org.edgexfoundry.domain.meta.Addressable;
import org.edgexfoundry.domain.meta.Schedule;
import org.edgexfoundry.domain.meta.ScheduleEvent;
import org.edgexfoundry.test.category.RequiresMetaDataRunning;
import org.edgexfoundry.test.category.RequiresMongoDB;
import org.edgexfoundry.test.data.AddressableData;
import org.edgexfoundry.test.data.ScheduleData;
import org.edgexfoundry.test.data.ScheduleEventData;

@Category({ RequiresMongoDB.class, RequiresMetaDataRunning.class })
public class ScheduleClientTest {

	private static final String ENDPT = "http://localhost:48081/api/v1/schedule";
	private static final String EVT_ENDPT = "http://localhost:48081/api/v1/scheduleevent";
	private static final String ADDR_ENDPT = "http://localhost:48081/api/v1/addressable";

	private ScheduleClient client;
	private ScheduleEventClient evtClient;
	private AddressableClient addrClient;
	private String id;
	private String start;
	private String end;
	private Addressable addr;

	// setup tests add function
	@Before
	public void setup() throws Exception {
		evtClient = new ScheduleEventClientImpl();
		client = new ScheduleClientImpl();
		addrClient = new AddressableClientImpl();
		addr = AddressableData.newTestInstance();
		setURL();
		addrClient.add(addr);
		Schedule s = ScheduleData.newTestInstance();
		start = s.getStart();
		end = s.getEnd();
		id = client.add(s);
	}

	private void setURL() throws Exception {
		Class<?> clientClass = client.getClass();
		Field temp = clientClass.getDeclaredField("url");
		temp.setAccessible(true);
		temp.set(client, ENDPT);
		Class<?> clientClass2 = evtClient.getClass();
		Field temp2 = clientClass2.getDeclaredField("url");
		temp2.setAccessible(true);
		temp2.set(evtClient, EVT_ENDPT);
		Class<?> clientClass3 = addrClient.getClass();
		Field temp3 = clientClass3.getDeclaredField("url");
		temp3.setAccessible(true);
		temp3.set(addrClient, ADDR_ENDPT);
	}

	// cleanup tests delete function
	@After
	public void cleanup() throws Exception {
		List<ScheduleEvent> es = evtClient.scheduleEvents();
		es.forEach((e) -> evtClient.delete(e.getId()));
		List<Schedule> as = client.schedules();
		as.forEach((a) -> client.delete(a.getId()));
		List<Addressable> al = addrClient.addressables();
		al.forEach((a) -> addrClient.delete(a.getId()));
	}

	@Test
	public void testSchedule() {
		Schedule d = client.schedule(id);
		checkTestData(d, id, start, end, ScheduleData.TEST_RUN_ONCE_FALSE);
	}

	@Test(expected = NotFoundException.class)
	public void testScheduleWithUnknownId() {
		client.schedule("nosuchid");
	}

	@Test
	public void testSchedules() {
		List<Schedule> as = client.schedules();
		assertEquals("Find all not returning a list with one schedule", 1, as.size());
		checkTestData(as.get(0), id, start, end, ScheduleData.TEST_RUN_ONCE_FALSE);
	}

	@Test
	public void testScheduleForName() {
		Schedule d = client.scheduleForName(TEST_SCHEDULE_NAME);
		checkTestData(d, id, start, end, ScheduleData.TEST_RUN_ONCE_FALSE);
	}

	@Test(expected = NotFoundException.class)
	public void testScheduleForNameWithNoneMatching() {
		client.scheduleForName("badname");
	}

	@Test(expected = ClientErrorException.class)
	public void testAddWithBadCron() {
		Schedule d = newTestInstance();
		d.setName("NewName");
		d.setCron("badcron");
		client.add(d);
	}

	@Test(expected = ClientErrorException.class)
	public void testAddWithSameName() {
		Schedule d = client.schedule(id);
		d.setId(null);
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
		assertTrue("Delete did not return correctly", client.deleteByName(TEST_SCHEDULE_NAME));
	}

	@Test(expected = NotFoundException.class)
	public void testDeleteByNameWithNone() {
		client.delete("badname");
	}

	@Test(expected = ClientErrorException.class)
	public void testDeleteAssociatedToEvent() {
		ScheduleEvent event = ScheduleEventData.newTestInstance();
		event.setAddressable(addr);
		evtClient.add(event);
		client.delete(id);
	}

	@Test
	public void testUpdate() {
		Schedule d = client.schedule(id);
		d.setOrigin(1234);
		assertTrue("Update did not complete successfully", client.update(d));
		Schedule d2 = client.schedule(id);
		assertEquals("Update did not work correclty", 1234, d2.getOrigin());
		assertNotNull("Modified date is null", d2.getModified());
		assertNotNull("Create date is null", d2.getCreated());
		assertTrue("Modified date and create date should be different after update",
				d2.getModified() != d2.getCreated());
	}

	@Test(expected = ClientErrorException.class)
	public void testUpdateWithBadCron() {
		Schedule d = client.schedule(id);
		d.setCron("badcron");
		client.update(d);
	}

	@Test
	public void testUpdateChangeNameWhileNotAssocToEvent() {
		Schedule d = client.schedule(id);
		d.setName("newname");
		assertTrue("Update did not complete successfully", client.update(d));
	}

	@Test(expected = ClientErrorException.class)
	public void testUpdateChangeNameWhileAssocToEvent() {
		ScheduleEvent event = ScheduleEventData.newTestInstance();
		event.setAddressable(addr);
		evtClient.add(event);
		Schedule d = client.schedule(id);
		d.setName("newname");
		client.update(d);
	}

	@Test(expected = NotFoundException.class)
	public void testUpdateWithNone() {
		Schedule d = client.schedule(id);
		d.setId("badid");
		d.setName("badname");
		d.setOrigin(1234);
		client.update(d);
	}

}
