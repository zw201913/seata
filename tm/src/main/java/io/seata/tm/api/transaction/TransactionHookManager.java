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
package io.seata.tm.api.transaction;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;

import io.seata.common.util.CollectionUtils;
import io.seata.common.util.StringUtils;
import io.seata.core.context.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author guoyao
 */
public final class TransactionHookManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionHookManager.class);

    private TransactionHookManager() {

    }

    private static final ThreadLocal<Map<String, List<TransactionHook>>> LOCAL_HOOKS = new ThreadLocal<>();

    /**
     * get the current hooks
     *
     * @return TransactionHook list
     */
    public static List<TransactionHook> getHooks() {
        String xid = RootContext.getXID();
        LOGGER.info("current xid={}, current thread name is {}", xid, Thread.currentThread().getName());
        return getHooks(xid);
    }

    /**
     * get hooks by xid
     * 
     * @param xid global transaction id
     * @return TransactionHook list
     */
    public static List<TransactionHook> getHooks(String xid) {
        Map<String, List<TransactionHook>> hooksMap = LOCAL_HOOKS.get();
        if (CollectionUtils.isEmpty(hooksMap)) {
            LOGGER.info("hooksMap is empty, current thread name is {}", Thread.currentThread().getName());
            return Collections.emptyList();
        }
        List<TransactionHook> hooks = hooksMap.computeIfAbsent(xid, value -> new ArrayList<>());
        if (StringUtils.isNotBlank(xid)) {
            List<TransactionHook> virtualHooks = hooksMap.remove(null);
            if (CollectionUtils.isNotEmpty(virtualHooks)) {
                hooks.addAll(virtualHooks);
            }
        }
        return Collections.unmodifiableList(hooks);
    }

    /**
     * add new hook
     *
     * @param transactionHook transactionHook
     */
    public static void registerHook(TransactionHook transactionHook) {
        if (transactionHook == null) {
            throw new NullPointerException("transactionHook must not be null");
        }
        Map<String, List<TransactionHook>> hooksMap = LOCAL_HOOKS.get();
        if (hooksMap == null) {
            hooksMap = new HashMap<>();
            LOCAL_HOOKS.set(hooksMap);
        }
        String xid = RootContext.getXID();
        List<TransactionHook> hooks = hooksMap.computeIfAbsent(xid, key -> new ArrayList<>());
        hooks.add(transactionHook);
    }

    /**
     * clear hooks by xid
     * 
     * @param xid global transaction id
     */
    public static void clear(String xid) {
        Map<String, List<TransactionHook>> hooksMap = LOCAL_HOOKS.get();
        if (CollectionUtils.isEmpty(hooksMap)) {
            return;
        }
        if (StringUtils.isBlank(xid)) {
            hooksMap.remove(null);
        } else {
            hooksMap.remove(xid);
        }
    }
}
