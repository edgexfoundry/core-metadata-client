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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.edgexfoundry.meta.client.ConsulDiscoveryClientTemplate;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.edgexfoundry.domain.meta.DeviceManager;
import org.edgexfoundry.exception.controller.DataValidationException;

@Component
public class DeviceManagerClientImpl extends ConsulDiscoveryClientTemplate implements DeviceManagerClient {

	@Value("${meta.db.devicemanager.url}")
	String url;

	@Override
	public DeviceManager deviceManager(String id) {
		return getClient().deviceManager(id);
	}

	@Override
	public List<DeviceManager> deviceManagers() {
		return getClient().deviceManagers();
	}

	@Override
	public DeviceManager deviceManagerForName(String name) {
		return getClient().deviceManagerForName(name);
	}

	@Override
	public List<DeviceManager> deviceManagersByLabel(String label) {
		return getClient().deviceManagersByLabel(label);
	}

	@Override
	public List<DeviceManager> deviceManagersForService(String serviceId) {
		return getClient().deviceManagersForService(serviceId);
	}

	@Override
	public List<DeviceManager> deviceManagersForServiceByName(String servicename) {
		return getClient().deviceManagersForServiceByName(servicename);
	}

	@Override
	public List<DeviceManager> deviceManagersForProfile(String profileId) {
		return getClient().deviceManagersForProfile(profileId);
	}

	@Override
	public List<DeviceManager> deviceManagersForProfileByName(String profilename) {
		return getClient().deviceManagersForProfileByName(profilename);
	}

	@Override
	public List<DeviceManager> deviceManagersForAddressable(String addressableId) {
		return getClient().deviceManagersForAddressable(addressableId);
	}

	@Override
	public List<DeviceManager> deviceManagersForAddressableByName(String addressablename) {
		return getClient().deviceManagersForAddressableByName(addressablename);
	}

	@Override
	public String add(DeviceManager deviceManager) {
		return getClient().add(deviceManager);
	}

	@Override
	public boolean update(DeviceManager deviceManager) {
		return getClient().update(deviceManager);
	}

	@Override
	public boolean updateLastConnected(String id, long time) {
		return getClient().updateLastConnected(id, time);
	}

	@Override
	public boolean updateLastConnectedByName(String name, long time) {
		return getClient().updateLastConnectedByName(name, time);
	}

	@Override
	public boolean updateLastReported(String id, long time) {
		return getClient().updateLastReported(id, time);
	}

	@Override
	public boolean updateLastReportedByName(String name, long time) {
		return getClient().updateLastReportedByName(name, time);
	}

	@Override
	public boolean updateOpState(String id, String opState) {
		return getClient().updateOpState(id, opState);
	}

	@Override
	public boolean updateOpStateByName(String name, String opState) {
		return getClient().updateOpStateByName(name, opState);
	}

	@Override
	public boolean updateAdminState(String id, String adminState) {
		return getClient().updateAdminState(id, adminState);
	}

	@Override
	public boolean updateAdminStateByName(String name, String adminState) {
		return getClient().updateAdminStateByName(name, adminState);
	}

	@Override
	public boolean delete(String id) {
		return getClient().delete(id);
	}

	@Override
	public boolean deleteByName(String name) {
		return getClient().deleteByName(name);
	}

	private DeviceManagerClient getClient() {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target;
		
		String rootUrl = super.getRootUrl();
		if(rootUrl == null || rootUrl.isEmpty()) {
			target = client.target(url);
		} else {
			target = client.target(rootUrl + super.getPath());
		}
		
		return target.proxy(DeviceManagerClient.class);
	}
	
	@Override
	protected String extractPath() {
		String result = "";
		try {
			URL urlObject = new URL(url);
			result = urlObject.getPath();
		} catch (MalformedURLException e) {
			throw new DataValidationException("the URL is malformed, meta.db.devicemanager.url: " + url);
		}
		return result;
	}
}
