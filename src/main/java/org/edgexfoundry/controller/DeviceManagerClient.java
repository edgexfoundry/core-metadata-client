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

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.edgexfoundry.domain.meta.DeviceManager;

public interface DeviceManagerClient {
	@GET
	@Path("/{id}")
	DeviceManager deviceManager(@PathParam("id") String id);

	@GET
	List<DeviceManager> deviceManagers();

	@GET
	@Path("/name/{name:.+}")
	DeviceManager deviceManagerForName(@PathParam("name") String name);

	@GET
	@Path("/label/{label:.+}")
	List<DeviceManager> deviceManagersByLabel(@PathParam("label") String label);

	@GET
	@Path("/service/{serviceId}")
	List<DeviceManager> deviceManagersForService(@PathParam("serviceId") String serviceId);

	@GET
	@Path("/servicename/{servicename:.+}")
	List<DeviceManager> deviceManagersForServiceByName(@PathParam("servicename") String servicename);

	@GET
	@Path("/profile/{profileId}")
	List<DeviceManager> deviceManagersForProfile(@PathParam("profileId") String profileId);

	@GET
	@Path("/profilename/{profilename:.+}")
	List<DeviceManager> deviceManagersForProfileByName(@PathParam("profilename") String profilename);

	@GET
	@Path("/device/{addressId}")
	List<DeviceManager> deviceManagersForAddressable(@PathParam("addressId") String addressId);

	@GET
	@Path("/addressablename/{addressablename:.+}")
	List<DeviceManager> deviceManagersForAddressableByName(@PathParam("addressablename") String addressablename);

	@POST
	@Consumes("application/json")
	String add(DeviceManager device);

	@PUT
	@Consumes("application/json")
	boolean update(DeviceManager device);

	@PUT
	@Consumes("application/json")
	@Path("/{id}/lastconnected/{time}")
	boolean updateLastConnected(@PathParam("id") String id, @PathParam("time") long time);

	@PUT
	@Consumes("application/json")
	@Path("/name/{name:.+}/lastconnected/{time}")
	boolean updateLastConnectedByName(@PathParam("name") String name, @PathParam("time") long time);

	@PUT
	@Consumes("application/json")
	@Path("/{id}/lastreported/{time}")
	boolean updateLastReported(@PathParam("id") String id, @PathParam("time") long time);

	@PUT
	@Consumes("application/json")
	@Path("/name/{name:.+}/lastreported/{time}")
	boolean updateLastReportedByName(@PathParam("name") String name, @PathParam("time") long time);

	@PUT
	@Consumes("application/json")
	@Path("/{id}/opstate/{opState}")
	boolean updateOpState(@PathParam("id") String id, @PathParam("opState") String opState);

	@PUT
	@Consumes("application/json")
	@Path("/name/{name:.+}/opstate/{opState}")
	boolean updateOpStateByName(@PathParam("name") String name, @PathParam("opState") String opState);

	@PUT
	@Consumes("application/json")
	@Path("/{id}/adminstate/{adminState}")
	boolean updateAdminState(@PathParam("id") String id, @PathParam("adminState") String adminState);

	@PUT
	@Consumes("application/json")
	@Path("/name/{name:.+}/adminstate/{adminState}")
	boolean updateAdminStateByName(@PathParam("name") String name, @PathParam("adminState") String adminState);

	@DELETE
	@Path("/id/{id}")
	boolean delete(@PathParam("id") String id);

	@DELETE
	@Path("/name/{name:.+}")
	boolean deleteByName(@PathParam("name") String name);

}
