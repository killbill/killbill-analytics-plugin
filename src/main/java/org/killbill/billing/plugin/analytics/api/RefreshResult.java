/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2020 Groupon, Inc
 * Copyright 2020-2020 Equinix, Inc
 * Copyright 2014-2020 The Billing Project, LLC
 *
 * The Billing Project licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.killbill.billing.plugin.analytics.api;

public class RefreshResult {
	
	private final Long nbAccounts;
	private final Long nbRefreshes;

	public RefreshResult (Long nbAccounts, Long nbRefreshes) {
		this.nbAccounts = nbAccounts;
		this.nbRefreshes = nbRefreshes;
	}
	
	public Long getNbAccounts() {
		return nbAccounts;
	}

	public Long getNbRefreshes() {
		return nbRefreshes;
	}

}
