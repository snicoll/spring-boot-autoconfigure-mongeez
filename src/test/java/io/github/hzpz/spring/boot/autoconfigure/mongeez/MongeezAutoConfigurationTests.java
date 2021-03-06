/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.hzpz.spring.boot.autoconfigure.mongeez;

import com.mongodb.Mongo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mongeez.Mongeez;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

public class MongeezAutoConfigurationTests {

    private AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    @Before
    public void init() {
    }

    @After
    public void close() {
        if (this.context != null) {
            this.context.close();
        }
    }

    private void registerAndRefresh(Class<?>... annotatedClasses) {
        this.context.register(annotatedClasses);
        this.context.refresh();

    }

    @Test
    public void shouldDoNothingIfNoMongoBean() {
        registerAndRefresh(MongeezAutoConfiguration.class);
        assertThat(this.context.getBeanNamesForType(Mongeez.class), emptyArray());
    }

    @Test
    public void shouldDoNothingIfDisabled() {
        EnvironmentTestUtils.addEnvironment(this.context, "mongeez.enabled:false");
        registerAndRefresh(MongoAutoConfiguration.class, MongeezAutoConfiguration.class);
        assumeThat(this.context.getBeanNamesForType(Mongo.class), not(emptyArray()));
        assertThat(this.context.getBeanNamesForType(Mongeez.class), emptyArray());
    }

    @Test
    public void shouldUseDatabaseFromMongoProperties() {
        String database = "foo";
        EnvironmentTestUtils.addEnvironment(this.context, "spring.data.mongodb.database:" + database);
        registerAndRefresh(DoNotExecuteMongeezPostProcessor.class,
                MongoAutoConfiguration.class, MongeezAutoConfiguration.class);
        Mongeez mongeez = this.context.getBean(Mongeez.class);
        Object mongeezDatabase = ReflectionTestUtils.getField(mongeez, "dbName");
        assertThat(mongeezDatabase.toString(), equalTo(database));
    }

    @Test
    public void shouldUseDatabaseOverrideFromMongeezProperties() {
        String mongoDatabase = "foo";
        String mongeezOverrideDatabase = "bar";
        EnvironmentTestUtils.addEnvironment(this.context, "spring.data.mongodb.database:" + mongoDatabase);
        EnvironmentTestUtils.addEnvironment(this.context, "mongeez.database:" + mongeezOverrideDatabase);
        registerAndRefresh(DoNotExecuteMongeezPostProcessor.class,
                MongoAutoConfiguration.class, MongeezAutoConfiguration.class);
        Mongeez mongeez = this.context.getBean(Mongeez.class);
        Object mongeezActualDatabase = ReflectionTestUtils.getField(mongeez, "dbName");
        assertThat(mongeezActualDatabase.toString(), equalTo(mongeezOverrideDatabase));
    }

    @Test(expected = BeanCreationException.class)
    public void shouldFailIfLocationDoesNotExist() {
        EnvironmentTestUtils.addEnvironment(this.context, "mongeez.location:does/not/exist");
        registerAndRefresh(DoNotExecuteMongeezPostProcessor.class,
                MongoAutoConfiguration.class, MongeezAutoConfiguration.class);
    }

}
