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
package com.opentable.metrics.graphite;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.opentable.service.AppInfo;
import com.opentable.service.EnvInfo;
import com.opentable.service.ServiceInfo;

public class GraphiteReporterPrefixTest {
    @Inject
    private ScheduledReporter reporter;

    @Test
    public void withFlavorEnabled() {
        final String prefix = prefixFrom("type-location.flavor", "0", true);
        Assert.assertNotNull(prefix);
        Assert.assertEquals("app_metrics.test-server-flavor.type.location.instance-0", prefix);
    }

    @Test
    public void withFlavorNull() {
        final String prefix = prefixFrom("type-location.flavor", "0", null);
        Assert.assertNotNull(prefix);
        Assert.assertEquals("app_metrics.test-server-flavor.type.location.instance-0", prefix);
    }

    @Test
    public void withFlavorDisabled() {
        final String prefix = prefixFrom("type-location.flavor", "0", false);
        Assert.assertNotNull(prefix);
        Assert.assertEquals("app_metrics.test-server.type.location.instance-0", prefix);
    }

    @Test
    public void noFlavor() {
        final String prefix = prefixFrom("prod-uswest2", "3", false);
        Assert.assertNotNull(prefix);
        Assert.assertEquals("app_metrics.test-server.prod.uswest2.instance-3", prefix);
    }

    @Test
    public void noFlavorButEnabled() {
        final String prefix = prefixFrom("prod-uswest2", "3", true);
        Assert.assertNotNull(prefix);
        Assert.assertEquals("app_metrics.test-server.prod.uswest2.instance-3", prefix);
    }

    @Test
    public void bad() {
        Assert.assertNull(prefixFrom(null, null,  null));
    }

    private String prefixFrom(final String env, final String instanceNo, Boolean includeFlavor) {
        final SpringApplication app = new SpringApplication(
                TestConfiguration.class,
                GraphiteConfiguration.class
        );
        final Map<String, Object> mockEnv = new HashMap<>();
        if (env != null) {
            if (includeFlavor != null) {
                mockEnv.put("ot.graphite.reporting.include.flavors", includeFlavor);
            }
            mockEnv.put("OT_ENV_WHOLE", env);
            final String[] typeLoc = env.split("-");
            mockEnv.put("OT_ENV_TYPE", typeLoc[0]);
            final String[] locFlavor = typeLoc[1].split("\\.");
            mockEnv.put("OT_ENV_LOCATION", locFlavor[0]);
            mockEnv.put("OT_ENV_FLAVOR", locFlavor.length == 2 ? locFlavor[1] : "");
            mockEnv.put("OT_ENV", typeLoc[0] + "-" + locFlavor[0]);
            mockEnv.put("ot.graphite.graphite-host", "carbon-foo-bar-baz.otenv.com");
        }
        if (instanceNo != null) {
            mockEnv.put("INSTANCE_NO", instanceNo);
        }
        app.setDefaultProperties(mockEnv);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run().getAutowireCapableBeanFactory().autowireBean(this);
        if (reporter == null) {
            return null;
        }
        try {
            Field f = reporter.getClass().getDeclaredField("prefix");
            f.setAccessible(true);
            return Objects.toString(f.get(reporter));
        } catch (IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new AssertionError(e);
        }
    }

    @Configuration
    @Import({
            AppInfo.class,
            EnvInfo.class,
    })
    public static class TestConfiguration {
        @Bean
        public ServiceInfo getServiceInfo() {
            return new ServiceInfo() {
                @Override
                public String getName() {
                    return "test-server";
                }
            };
        }
        @Bean
        public MetricRegistry getMetrics() {
            return new MetricRegistry();
        }
    }
}
