/*
 *  Copyright 1999-2019 Seata.io Group.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.seata.rm.tcc.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.seata.common.Constants;
import io.seata.common.exception.JsonParseException;
import io.seata.common.loader.LoadLevel;
import io.seata.integration.tx.api.json.JsonParser;

/**
 * @author zouwei
 */
@LoadLevel(name = Constants.JACKSON_JSON_PARSER_NAME)
public class JacksonJsonParser implements JsonParser {

    private final ObjectMapper mapper = new ObjectMapper();

    public JacksonJsonParser() {
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.mapper.activateDefaultTyping(this.mapper.getPolymorphicTypeValidator(),
            ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        this.mapper.setConfig(this.mapper.getSerializationConfig().with(MapperFeature.PROPAGATE_TRANSIENT_MARKER));
        this.mapper.setConfig(this.mapper.getDeserializationConfig().with(MapperFeature.PROPAGATE_TRANSIENT_MARKER));
        this.mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public String toJSONString(Object object) {
        try {
            return this.mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e);
        }
    }

    @Override
    public <T> T parseObject(String text, Class<T> clazz) {
        try {
            return this.mapper.readValue(text, clazz);
        } catch (JacksonException e) {
            throw new JsonParseException(e);
        }
    }

    @Override
    public String getName() {
        return Constants.JACKSON_JSON_PARSER_NAME;
    }
}
