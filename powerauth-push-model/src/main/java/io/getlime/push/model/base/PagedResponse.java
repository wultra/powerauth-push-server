/*
 * Copyright 2016 Wultra s.r.o.
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
 */

package io.getlime.push.model.base;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import lombok.EqualsAndHashCode;

/**
 * Generic response class for paged results
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 *
 * @param <T> Type of the paged records.
 */
@EqualsAndHashCode(callSuper = true)
public class PagedResponse<T> extends ObjectResponse<T> {

    private int page;
    private int size;

    /**
     * No-arg constructor.
     */
    public PagedResponse() {
        super();
    }

    /**
     * Typed constructor.
     * @param t Type of generic parameter.
     */
    public PagedResponse(T t) {
        super(t);
    }

    /**
     * Typed constructor.
     * @param t Type of generic parameter.
     * @param page Page number.
     * @param size Page size.
     */
    public PagedResponse(T t, int page, int size) {
        super(t);
        this.page = page;
        this.size = size;
    }

    /**
     * Get page number.
     * @return Page number.
     */
    public int getPage() {
        return page;
    }

    /**
     * Set page number.
     * @param page Page number.
     */
    public void setPage(int page) {
        this.page = page;
    }

    /**
     * Get page size.
     * @return Page size.
     */
    public int getSize() {
        return size;
    }

    /**
     * Set page size.
     * @param size Page size.
     */
    public void setSize(int size) {
        this.size = size;
    }
}
