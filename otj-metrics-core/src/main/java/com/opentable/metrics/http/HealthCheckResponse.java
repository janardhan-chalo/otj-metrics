/*
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
 */
package com.opentable.metrics.http;

import java.util.Map;

import com.codahale.metrics.health.HealthCheck.Result;
import com.google.common.collect.ImmutableMap;

public class HealthCheckResponse implements MonitorResponse
{
    private final String name;
    private final Result result;

    public HealthCheckResponse(String name, Result result)
    {
        this.name = name;
        this.result = result;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public Map<String, Object> getMonitors()
    {
        return ImmutableMap.of("healthy", result.isHealthy(), "message", result.getMessage());
    }
}
