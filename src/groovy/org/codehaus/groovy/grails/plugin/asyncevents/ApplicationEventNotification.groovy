/*
 * Copyright 2010 Robert Fletcher
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
 */
package org.codehaus.groovy.grails.plugin.asyncevents

import grails.plugin.asyncevents.RetryableFailureException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener

class ApplicationEventNotification {

	private int retryCount = 0
	final ApplicationListener target
	final ApplicationEvent event

	private final Logger log = LoggerFactory.getLogger(ApplicationEventNotification)

	ApplicationEventNotification(ApplicationListener target, ApplicationEvent event) {
		this.target = target
		this.event = event
	}

	void notifyListener() {
		target.onApplicationEvent(event)
	}

	void incrementRetryCount() {
		retryCount++
	}

	long getRetryDelayMillis() {
		long retryDelay = target.retryPolicy.initialRetryDelayMillis
		retryCount.times {
			retryDelay *= target.retryPolicy.backoffMultiplier
		}
		return retryDelay
	}

	boolean shouldRetry() {
		if (target.hasProperty("retryPolicy")) {
			return target.retryPolicy.retryIndefinitely() || retryCount < target.retryPolicy.maxRetries
		} else {
			return false
		}
	}
}